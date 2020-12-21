package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Chat
import com.elbekD.bot.types.ChatMember
import com.elbekD.bot.types.Message
import com.elbekD.bot.types.User
import com.github.pool_party.pull_party_bot.commands.Command
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CompletableFuture
import kotlin.test.BeforeTest

internal abstract class AbstractCommandTest {

    @MockK
    protected lateinit var bot: Bot

    @MockK
    protected lateinit var partyDao: PartyDao

    @MockK
    protected lateinit var chatDao: ChatDao

    private lateinit var action: suspend (Message, String?) -> Unit

    protected val chat =
        Chat(1, "group", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)

    protected val user = User(-1, false, "first name", null, null, null, null, null, null)

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
        1,
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

    abstract fun initializeCommand(partyDao: PartyDao, chatDao: ChatDao): Command

    @BeforeTest
    fun setupMock() {
        MockKAnnotations.init(this)

        every { bot.onCommand(any(), any()) } answers { action = secondArg() }
        every {
            bot.sendMessage(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns CompletableFuture()

        initializeCommand(partyDao, chatDao).onMessage(bot)
    }

    protected fun onMessage(message: Message, args: String? = null) {
        runBlocking { GlobalScope.launch { action(message, args) }.join() }
    }

    protected fun verifyMessage(chatId: Long) {
        coVerify(exactly = 1) { bot.sendMessage(chatId, any(), any()) }
    }

    protected fun verifyMessages(chatId: Long, text: String, exactly: Int = 1, replyTo: Int? = null) {
        coVerify(exactly = exactly) {
            bot.sendMessage(
                chatId,
                text,
                any(),
                any(),
                any(),
                any(),
                replyTo ?: isNull(),
                any(),
                any()
            )
        }
    }
}
