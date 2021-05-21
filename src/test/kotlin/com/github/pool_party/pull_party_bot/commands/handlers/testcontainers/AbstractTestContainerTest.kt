package com.github.pool_party.pull_party_bot.commands.handlers.testcontainers

import com.elbekD.bot.types.CallbackQuery
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.commands.CallbackDispatcher
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
import com.github.pool_party.pull_party_bot.commands.handlers.callback.DeleteNodeSuggestionCallback
import com.github.pool_party.pull_party_bot.commands.handlers.callback.DeleteSuggestionCallback
import com.github.pool_party.pull_party_bot.commands.handlers.callback.PingCallback
import com.github.pool_party.pull_party_bot.database.Aliases
import com.github.pool_party.pull_party_bot.database.Chats
import com.github.pool_party.pull_party_bot.database.Parties
import com.github.pool_party.pull_party_bot.database.dao.ChatDaoImpl
import com.github.pool_party.pull_party_bot.database.dao.PartyDaoImpl
import io.mockk.every
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@Testcontainers
internal abstract class AbstractTestContainerTest : AbstractBotTest() {

    private val chatDao = ChatDaoImpl()

    private val partyDao = PartyDaoImpl()

    private val everyMessageProcessor =
        EveryMessageProcessor(listOf(MigrationHandler(chatDao), ImplicitPartyHandler(partyDao)))

    private lateinit var everyMessageAction: suspend (Message) -> Unit

    private lateinit var callbackAction: suspend (CallbackQuery) -> Unit

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

    private val callbacks = listOf(
        DeleteNodeSuggestionCallback(partyDao),
        DeleteSuggestionCallback(partyDao),
        PingCallback(partyDao)
    )

    private val callbackDispatcher = CallbackDispatcher(callbacks.associateBy { it.callbackAction })

    @BeforeTest
    fun setupMock() {
        Database.connect(container.jdbcUrl, user = container.username, password = container.password)

        Flyway.configure().dataSource(container.jdbcUrl, container.username, container.password).load().migrate()

        every { bot.onCommand(any(), any()) } answers { commandActions[firstArg()] = secondArg() }
        every { bot.onMessage(any()) } answers { everyMessageAction = firstArg() }
        every { bot.onCallbackQuery(any()) } answers { callbackAction = firstArg() }

        commands.forEach { it.onMessage(bot) }
        everyMessageProcessor.onMessage(bot)
        callbackDispatcher.onMessage(bot)
    }

    @AfterTest
    fun clearDatabases() {
        transaction {
            Aliases.deleteAll()
            Parties.deleteAll()
            Chats.deleteAll()
        }
    }

    // bot interaction test DSL

    protected fun callback(callbackData: String) {
        runBlocking {
            GlobalScope.launch {
                callbackAction(CallbackQuery("id", user, message, null, "chat instance", callbackData, null))
            }.join()
        }
    }

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
