package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekD.bot.Bot
import com.elbekD.bot.types.CallbackQuery
import com.elbekD.bot.types.Chat
import com.elbekD.bot.types.ChatMember
import com.elbekD.bot.types.Message
import com.elbekD.bot.types.User
import com.github.pool_party.pull_party_bot.commands.CallbackDispatcher
import com.github.pool_party.pull_party_bot.commands.EveryMessageProcessor
import com.github.pool_party.pull_party_bot.commands.handlers.callback.DeleteNodeSuggestionCallback
import com.github.pool_party.pull_party_bot.commands.handlers.callback.DeleteSuggestionCallback
import com.github.pool_party.pull_party_bot.commands.handlers.callback.PingCallback
import com.github.pool_party.pull_party_bot.database.AliasCache
import com.github.pool_party.pull_party_bot.database.Aliases
import com.github.pool_party.pull_party_bot.database.ChatCache
import com.github.pool_party.pull_party_bot.database.Chats
import com.github.pool_party.pull_party_bot.database.Parties
import com.github.pool_party.pull_party_bot.database.PartyAliasesCache
import com.github.pool_party.pull_party_bot.database.PartyCache
import com.github.pool_party.pull_party_bot.database.dao.ChatDaoImpl
import com.github.pool_party.pull_party_bot.database.dao.PartyDaoImpl
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import java.util.concurrent.CompletableFuture
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

internal abstract class AbstractBotTest {

    @MockK
    protected lateinit var bot: Bot

    protected val chat =
        Chat(1, "group", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)

    protected val user = User(-1, false, "first name", null, "admin", null, null, null, null)

    protected val chatMember = ChatMember(
        user,
        "status",
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    )

    protected val message = Message(
        1,
        user,
        null,
        System.currentTimeMillis().div(1000).toInt(),
        chat,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    )

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
        PingCallback(partyDao),
    )

    private val callbackDispatcher = CallbackDispatcher(callbacks.associateBy { it.callbackAction })

    @BeforeTest
    fun setupMock() {
        MockKAnnotations.init(this, relaxed = true, relaxUnitFun = true)

        container.start()
        Database.connect(
            "${container.jdbcUrl}&gssEncMode=disable",
            user = container.username,
            password = container.password
        )
        Flyway.configure().dataSource(container.jdbcUrl, container.username, container.password).load().migrate()

        every { bot.sendMessage(allAny(), allAny()) } answers {
            println(">> ${secondArg<String>()}")
            CompletableFuture.completedFuture(message)
        }
        every { bot.getChatAdministrators(chat.id) } returns CompletableFuture.completedFuture(arrayListOf(chatMember))
        every { bot.onCommand(any(), any()) } answers { commandActions[firstArg()] = secondArg() }
        every { bot.onMessage(any()) } answers { everyMessageAction = firstArg() }
        every { bot.onCallbackQuery(any()) } answers { callbackAction = firstArg() }
        every { bot.deleteMessage(any(), any()) } returns CompletableFuture.completedFuture(true)

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

        AliasCache.clear()
        PartyCache.clear()
        PartyAliasesCache.clear()
        ChatCache.clear()
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
        println("<< $this")
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

    protected fun verifyMessages(chatId: Long, text: String, exactly: Int = 1, replyTo: Int? = null) {
        coVerify(exactly = exactly) {
            bot.sendMessage(chatId, text, any(), any(), any(), any(), replyTo ?: any(), any(), any())
        }
    }

    protected fun verifyMessage(matcher: (String) -> Boolean) {
        coVerify {
            bot.sendMessage(chat.id, match(matcher), any(), any(), any(), any(), any(), any(), any())
        }
    }

    protected fun verifyContains(vararg substrings: String) =
        verifyMessage { message -> substrings.all { it in message } }

    protected operator fun String.unaryPlus() = verifyMessages(chat.id, this)

    companion object {
        @Container
        private val container = KPostgreSQLContainer()
            .withDatabaseName("database")
    }

    internal class KPostgreSQLContainer : PostgreSQLContainer<KPostgreSQLContainer>("postgres")
}
