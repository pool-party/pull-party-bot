package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Chat
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.commands.messages.ON_ADMINS_PARTY_FAIL
import com.github.pool_party.pull_party_bot.commands.messages.ON_PARTY_EMPTY
import com.github.pool_party.pull_party_bot.commands.messages.ON_PARTY_REQUEST_FAIL
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.verify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.CompletableFuture
import kotlin.test.BeforeTest
import kotlin.test.Test


internal class PullPartyCommandsTest {
    @MockK
    private lateinit var bot: Bot

    @MockK
    private lateinit var chatDao: ChatDao

    @MockK
    private lateinit var partyDao: PartyDao

    private lateinit var party: PartyCommand
    private lateinit var action: suspend (Message, String?) -> Unit

    private var chat =
        Chat(1, "group", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)

    private var message = Message(
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

        party = PartyCommand(partyDao)

        every { bot.onCommand(any(), any()) } answers { action = secondArg() }

        party.onMessage(bot)

        every { bot.sendMessage(any(), any(), any()) } returns CompletableFuture()
    }


    @Test
    fun emptyPartyCall() {
        GlobalScope.launch { action(message, "") }
        GlobalScope.launch { action(message, null) }

        verify(exactly = 2) {
            bot.sendMessage(
                any(),
                ON_PARTY_EMPTY,
                any(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
            )
        }
    }

    @Test
    fun adminsPartyCall() {
        GlobalScope.launch { action(message.copy(chat = chat.copy(type = "private")), "admins") }

        verify(exactly = 1) {
            bot.sendMessage(
                any(),
                ON_ADMINS_PARTY_FAIL,
                any(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
            )
        }
    }
}
