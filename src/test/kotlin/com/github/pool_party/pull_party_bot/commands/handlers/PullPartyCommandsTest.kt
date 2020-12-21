package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Chat
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.commands.messages.ON_ADMINS_PARTY_FAIL
import com.github.pool_party.pull_party_bot.commands.messages.ON_PARTY_EMPTY
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

internal class PullPartyCommandsTest {
    @MockK
    private lateinit var bot: Bot

    @MockK
    private lateinit var partyDao: PartyDao

    private lateinit var party: PartyCommand
    private lateinit var action: suspend (Message, String?) -> Unit

    private val groupChat =
        Chat(1, "group", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)

    private val privateChat = groupChat.copy(id = 2, type = "private")

    private val groupMessage = Message(
        1,
        null,
        null,
        1,
        groupChat,
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

    private val privateMessage = groupMessage.copy(message_id = 2, date = 2, chat = privateChat)

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

        party = PartyCommand(partyDao)
        party.onMessage(bot)
    }

    @Test
    fun emptyPartyCall() {
        if (GlobalScope.launch { action(groupMessage, "") }.isCompleted &&
            GlobalScope.launch { action(groupMessage, null) }.isCompleted
        ) {
            verify(exactly = 2) {
                bot.sendMessage(
                    groupMessage.chat.id,
                    ON_PARTY_EMPTY,
                    any()
                )
            }
        }
    }

    @Test
    fun adminsPartyCall() {
        if (GlobalScope.launch { action(privateMessage, "admins") }.isCompleted) {
            verify(exactly = 1) {
                bot.sendMessage(
                    privateMessage.chat.id,
                    ON_ADMINS_PARTY_FAIL,
                    any()
                )
            }
        }
    }

    @Test
    fun correctSinglePartyCall() {
        val response = "@albertshady @komour"

        every { partyDao.getByIdAndName(groupMessage.chat.id, "gym") } returns response

        if (GlobalScope.launch { action(groupMessage, "gym") }.isCompleted) {
            verify(exactly = 1) {
                bot.sendMessage(
                    groupMessage.chat.id,
                    response,
                    any(),
                    isNull(),
                    isNull(),
                    isNull(),
                    groupMessage.message_id
                )
            }
        }
    }

    @Test
    fun correctMultiplePartyCalls() {
        val responseGym = "@albertshady @komour"
        val responseSamara = "@g4nkedbymom @albertshady"

        val expectedResponse = "$responseGym $responseSamara".split(' ').distinct().joinToString(" ")

        every { partyDao.getByIdAndName(groupMessage.chat.id, "gym") } returns responseGym
        every { partyDao.getByIdAndName(groupMessage.chat.id, "samara") } returns responseSamara

        if (GlobalScope.launch { action(groupMessage, "gym Samara") }.isCompleted) {
            verify(exactly = 1) {
                bot.sendMessage(
                    groupMessage.chat.id,
                    expectedResponse,
                    any(),
                    isNull(),
                    isNull(),
                    isNull(),
                    groupMessage.message_id
                )
            }
        }
    }
}
