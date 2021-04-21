package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Chat
import com.elbekD.bot.types.ChatMember
import com.elbekD.bot.types.Message
import com.elbekD.bot.types.User
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import java.util.concurrent.CompletableFuture
import kotlin.test.BeforeTest

internal abstract class AbstractBotTest {

    @MockK
    protected lateinit var bot: Bot

    protected val chat =
        Chat(1, "group", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)

    private val user = User(-1, false, "first name", null, "admin", null, null, null, null)

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

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)

        every {
            bot.sendMessage(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns CompletableFuture()

        // mock
        every {
            bot.sendMessage(any(), any(), any(), any(), any(), any(), any(), any(), any()).join()
        } returns message

        every { bot.getChatAdministrators(chat.id) } returns CompletableFuture.completedFuture(arrayListOf(chatMember))
    }

    protected fun verifyMessage(chatId: Long) {
        coVerify(exactly = 1) { bot.sendMessage(chatId, any(), any()) }
    }

    protected fun verifyMessages(chatId: Long, text: String, exactly: Int = 1, replyTo: Int? = null) {
        coVerify(exactly = exactly) {
            bot.sendMessage(chatId, text, any(), any(), any(), any(), replyTo ?: any(), any(), any())
        }
    }
}
