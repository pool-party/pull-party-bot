package com.github.pool_party.pull_party_bot.commands.handlers.testcontainers

import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.commands.EveryMessageProcessor
import com.github.pool_party.pull_party_bot.commands.handlers.AbstractBotTest
import com.github.pool_party.pull_party_bot.commands.handlers.AddCommand
import com.github.pool_party.pull_party_bot.commands.handlers.AliasCommand
import com.github.pool_party.pull_party_bot.commands.handlers.ChangeCommand
import com.github.pool_party.pull_party_bot.commands.handlers.ClearCommand
import com.github.pool_party.pull_party_bot.commands.handlers.CreateCommand
import com.github.pool_party.pull_party_bot.commands.handlers.DeleteCommand
import com.github.pool_party.pull_party_bot.commands.handlers.FeedbackCommand
import com.github.pool_party.pull_party_bot.commands.handlers.ImplicitPartyHandler
import com.github.pool_party.pull_party_bot.commands.handlers.ListCommand
import com.github.pool_party.pull_party_bot.commands.handlers.MigrationHandler
import com.github.pool_party.pull_party_bot.commands.handlers.PartyCommand
import com.github.pool_party.pull_party_bot.commands.handlers.RemoveCommand
import com.github.pool_party.pull_party_bot.commands.handlers.RudeCommand
import com.github.pool_party.pull_party_bot.commands.handlers.StartCommand
import com.github.pool_party.pull_party_bot.database.Aliases
import com.github.pool_party.pull_party_bot.database.Chats
import com.github.pool_party.pull_party_bot.database.Parties
import com.github.pool_party.pull_party_bot.database.dao.ChatDaoImpl
import com.github.pool_party.pull_party_bot.database.dao.PartyDaoImpl
import io.mockk.every
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.BeforeTest

@Testcontainers
internal abstract class AbstractTestContainerTest : AbstractBotTest() {

    private val chatDao = ChatDaoImpl()

    private val partyDao = PartyDaoImpl()

    private val everyMessageProcessor =
        EveryMessageProcessor(listOf(MigrationHandler(chatDao), ImplicitPartyHandler(partyDao)))

    private lateinit var everyMessageAction: suspend (Message) -> Unit

    private val commandActions = mutableMapOf<String, suspend (Message, String?) -> Unit>()

    private val commands = listOf(
        StartCommand(),
        ListCommand(partyDao, chatDao),
        PartyCommand(partyDao),
        DeleteCommand(partyDao, chatDao),
        ClearCommand(chatDao),
        CreateCommand(partyDao, chatDao),
        AliasCommand(partyDao, chatDao),
        ChangeCommand(partyDao, chatDao),
        AddCommand(partyDao, chatDao),
        RemoveCommand(partyDao, chatDao),
        RudeCommand(chatDao),
        FeedbackCommand(),
    )

    @BeforeTest
    fun setupMock() {
        Database.connect(container.jdbcUrl, user = container.username, password = container.password)

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Aliases, Chats, Parties)
        }

        every { bot.onCommand(any(), any()) } answers { commandActions[firstArg()] = secondArg() }
        every { bot.onMessage(any()) } answers { everyMessageAction = firstArg() }

        commands.forEach { it.onMessage(bot) }
        everyMessageProcessor.onMessage(bot)
    }

    // bot interaction test DSL

    protected operator fun String.unaryMinus() {
        val split = split(" ")
        runBlocking {
            GlobalScope.launch {
                val command = commandActions[split[0]]
                val currentMessage = message.copy(text = this@unaryMinus)

                if (command != null) {
                    command(currentMessage, split.drop(1).joinToString(" "))
                } else {
                    everyMessageAction(currentMessage)
                }
            }.join()
        }
    }

    protected operator fun String.unaryPlus() = verifyMessages(chat.id, this)

    companion object {
        @Container
        private val container = KPostgreSQLContainer().withDatabaseName("database")
    }

    internal class KPostgreSQLContainer : PostgreSQLContainer<KPostgreSQLContainer>("postgres")
}
