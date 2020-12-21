package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Chat
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.commands.messages.ON_RUDE_FAIL
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.CompletableFuture
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class RudeCommandTest {
    @MockK
    private lateinit var bot: Bot

    @MockK
    private lateinit var chatDao: ChatDao

    private lateinit var rude: RudeCommand
    private lateinit var action: suspend (Message, String?) -> Unit

    private val chat =
        Chat(1, "group", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)

    private val message = Message(
        1,
        null,
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

        rude = RudeCommand(chatDao)
        rude.onMessage(bot)
    }

    @Test
    fun wrongArgCall() {
        if (GlobalScope.launch { action(message, "party") }.isCompleted &&
            GlobalScope.launch { action(message, "rude") }.isCompleted
        ) {
            verify(exactly = 2) {
                bot.sendMessage(
                    message.chat.id,
                    ON_RUDE_FAIL,
                    any()
                )
            }
        }
    }

    @Test
    fun wrongAmountOfArgsCall() {
        if (GlobalScope.launch { action(message, "") }.isCompleted &&
            GlobalScope.launch { action(message, "first second") }.isCompleted
        ) {
            verify(exactly = 2) {
                bot.sendMessage(
                    message.chat.id,
                    ON_RUDE_FAIL,
                    any()
                )
            }
        }
    }

    // TODO check messages for all tests.
    @Test
    fun correctOnSameCall() {
        every { chatDao.getRude(message.chat.id) } returns true
        every {
            chatDao.setRude(
                message.chat.id,
                true
            )
        } answers { secondArg<Boolean>() != chatDao.getRude(firstArg()) }

        if (GlobalScope.launch { action(message, "on") }.isCompleted) {
            verify(exactly = 1) {
                bot.sendMessage(
                    message.chat.id,
                    any(),
                    any()
                )
            }
        }
    }

    @Test
    fun correctOnNewCall() {
        every { chatDao.getRude(message.chat.id) } returns false
        every {
            chatDao.setRude(
                message.chat.id,
                true
            )
        } answers { secondArg<Boolean>() != chatDao.getRude(firstArg()) }

        if (GlobalScope.launch { action(message, "on") }.isCompleted) {
            verify(exactly = 1) {
                bot.sendMessage(
                    message.chat.id,
                    any(),
                    any()
                )
            }
        }
    }

    @Test
    fun correctOffSameCall() {
        every { chatDao.getRude(message.chat.id) } returns false
        every {
            chatDao.setRude(
                message.chat.id,
                false
            )
        } answers { secondArg<Boolean>() != chatDao.getRude(firstArg()) }

        if (GlobalScope.launch { action(message, "off") }.isCompleted) {
            verify(exactly = 1) {
                bot.sendMessage(
                    message.chat.id,
                    any(),
                    any()
                )
            }
        }
    }

    @Test
    fun correctOffNewCall() {
        every { chatDao.getRude(message.chat.id) } returns true
        every {
            chatDao.setRude(
                message.chat.id,
                false
            )
        } answers { secondArg<Boolean>() != chatDao.getRude(firstArg()) }

        if (GlobalScope.launch { action(message, "off") }.isCompleted) {
            verify(exactly = 1) {
                bot.sendMessage(
                    message.chat.id,
                    any(),
                    any()
                )
            }
        }
    }
}
