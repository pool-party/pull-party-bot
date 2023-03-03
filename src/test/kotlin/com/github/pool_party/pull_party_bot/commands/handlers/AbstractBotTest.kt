package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekd.bot.Bot
import com.elbekd.bot.types.CallbackQuery
import com.elbekd.bot.types.Chat
import com.elbekd.bot.types.ChatMember
import com.elbekd.bot.types.InlineKeyboardMarkup
import com.elbekd.bot.types.Message
import com.elbekd.bot.types.User
import com.github.pool_party.pull_party_bot.commands.initHandlers
import com.github.pool_party.pull_party_bot.database.AliasCache
import com.github.pool_party.pull_party_bot.database.Aliases
import com.github.pool_party.pull_party_bot.database.ChatCache
import com.github.pool_party.pull_party_bot.database.Chats
import com.github.pool_party.pull_party_bot.database.Parties
import com.github.pool_party.pull_party_bot.database.PartyAliasesCache
import com.github.pool_party.pull_party_bot.database.PartyCache
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertNotNull

internal abstract class AbstractBotTest {

    @MockK
    protected lateinit var bot: Bot

    private val chat =
        Chat(1, "group", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)

    private val user = User(-1, false, "first name", null, "admin", null, null, null, null)

    private val chatMember = ChatMember.Owner("creator", user)

    protected val message = Message(
        1,
        null,
        user,
        null,
        System.currentTimeMillis().div(1000).toLong(),
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
        listOf(),
        null,
        null,
        null,
        listOf(),
        null,
        null,
        null,
        null,
        null,
        listOf(),
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        listOf(),
        null,
        null,
        listOf(),
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

    private lateinit var everyMessageAction: suspend (Message) -> Unit

    private lateinit var callbackAction: suspend (CallbackQuery) -> Unit

    private val commandActions = mutableMapOf<String, suspend (Pair<Message, String?>) -> Unit>()

    protected var lastCallbackData: String? = null

    @BeforeTest
    fun setupMock() {
        MockKAnnotations.init(this, relaxed = true, relaxUnitFun = true)

        // start and migrate database
        container.start()
        Database.connect(
            "${container.jdbcUrl}&gssEncMode=disable",
            user = container.username,
            password = container.password
        )
        Flyway.configure().dataSource(container.jdbcUrl, container.username, container.password).load().migrate()

        // mock basic functions
        coEvery { bot.sendMessage(allAny(), allAny()) } answers {
            println(">> ${secondArg<String>()}")
            message
        }
        coEvery { bot.getChatAdministrators(any()) } returns arrayListOf(chatMember)
        coEvery { bot.onCommand(any(), any()) } answers { commandActions[firstArg()] = secondArg() }
        every { bot.onMessage(any()) } answers { everyMessageAction = firstArg() }
        every { bot.onCallbackQuery(any()) } answers { callbackAction = firstArg() }
        coEvery { bot.deleteMessage(any(), any()) } returns true

        // extract functions
        runBlocking { bot.initHandlers() }

        // mock callbacks
        coEvery { bot.sendMessage(allAny(), allAny()) } answers {
            val inlineKeyboardMarkup = arg(9) as? InlineKeyboardMarkup
            println(">> ${secondArg<String>()}\n*button*: $inlineKeyboardMarkup")
            lastCallbackData = inlineKeyboardMarkup?.inlineKeyboard?.singleOrNull()?.singleOrNull()?.callbackData
            message
        }
        coEvery { bot.answerCallbackQuery(allAny(), allAny()) } answers {
            println("^^ ${secondArg<String>()}")
            true
        }
        coEvery { bot.answerCallbackQuery(allAny()) } returns true
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

    protected fun callback(callbackData: String) = runBlocking {
        callbackAction(CallbackQuery("id", user, message, null, "chat instance", callbackData, null))
    }

    protected fun clickButton() = runBlocking {
        assertNotNull(lastCallbackData)
        callbackAction(CallbackQuery("id", user, message, null, "chat instance", lastCallbackData!!, null))
    }

    protected operator fun String.unaryMinus() = runBlocking {
        println("<< ${this@unaryMinus}")
        val split = split(" ")
        val command = commandActions[split[0]]
        val currentMessage = message.copy(text = this@unaryMinus)

        if (command != null) {
            command(currentMessage to split.drop(1).joinToString(" "))
        } else {
            everyMessageAction(currentMessage)
        }
    }

    protected fun verifyMessage(exactly: Int = 1, replyTo: Long? = null, matcher: (String) -> Boolean) {
        coVerify(exactly = exactly) {
            bot.sendMessage(
                match { it.toString() == chat.id.toString() },
                match(matcher),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                replyTo ?: any(),
                any(),
                any(),
            )
        }
    }

    protected fun verifyContains(vararg substrings: String, exactly: Int = 1) =
        verifyMessage(exactly = exactly) { message -> substrings.all { it in message } }

    protected operator fun String.unaryPlus() = verifyMessage { it == this }

    companion object {
        @Container
        private val container = KPostgreSQLContainer().withDatabaseName("database")
    }

    internal class KPostgreSQLContainer : PostgreSQLContainer<KPostgreSQLContainer>("postgres")
}
