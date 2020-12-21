package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Chat
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.commands.messages.*
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.CompletableFuture
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class DeleteCommandsTest {
    @MockK
    private lateinit var bot: Bot

    @MockK
    private lateinit var partyDao: PartyDao

    @MockK
    private lateinit var chatDao: ChatDao

    private lateinit var delete: DeleteCommand
    private lateinit var clear: ClearCommand

    private lateinit var mainAction: suspend (Message, String?) -> Unit

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

        every { bot.onCommand(any(), any()) } answers { mainAction = secondArg() }
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

        delete = DeleteCommand(partyDao, chatDao)
        delete.onMessage(bot)

        clear = ClearCommand(chatDao)
        clear.onMessage(bot)
    }

    @Test
    fun emptyDeleteCall() {
        if (GlobalScope.launch { mainAction(message, "") }.isCompleted &&
            GlobalScope.launch { mainAction(message, null) }.isCompleted
        ) {
            verify(exactly = 2) {
                bot.sendMessage(
                    message.chat.id,
                    ON_DELETE_EMPTY,
                    any()
                )
            }
        }
    }

    @Test
    fun clearCall() {
        every { chatDao.clear(message.chat.id) } returns Unit

        if (GlobalScope.launch { mainAction(message, null) }.isCompleted) {
            verify(exactly = 1) {
                bot.sendMessage(
                    message.chat.id,
                    ON_CLEAR_SUCCESS,
                    any()
                )
            }
        }
    }

    @Test
    fun adminsDeleteCall() {
        if (GlobalScope.launch { mainAction(message, "admins") }.isCompleted) {
            verify(exactly = 1) {
                bot.sendMessage(
                    message.chat.id,
                    ON_ADMINS_PARTY_CHANGE,
                    any()
                )
            }
        }
    }

    @Test
    fun fewDeleteCalls() {
        every { partyDao.delete(message.chat.id, any()) } answers { true } andThen { false }

        if (GlobalScope.launch { mainAction(message, "realParty admins NotParty") }.isCompleted) {
            verify(exactly = 1) {
                bot.sendMessage(
                    message.chat.id,
                    """Party realParty is just a history now 👍""",
                    any()
                )
            }

            verify(exactly = 1) {
                bot.sendMessage(
                    message.chat.id,
                    ON_ADMINS_PARTY_CHANGE,
                    any()
                )
            }

            verify(exactly = 1) {
                bot.sendMessage(
                    message.chat.id,
                    """Not like I knew the NotParty party, but now I don't know it at all 👍""",
                    any()
                )
            }
        }
    }
}
