package com.github.pool_party.pull_party_bot.commands.handlers

import com.github.pool_party.pull_party_bot.commands.Command
import com.github.pool_party.pull_party_bot.commands.messages.ON_ADMINS_PARTY_FAIL
import com.github.pool_party.pull_party_bot.commands.messages.ON_PARTY_EMPTY
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import io.mockk.every
import kotlin.test.Test

internal class PullPartyCommandTest : AbstractCommandTest() {

    override fun initializeCommand(partyDao: PartyDao, chatDao: ChatDao): Command = PartyCommand(partyDao)

    private val privateChat = chat.copy(id = 2, type = "private")

    private val privateMessage = message.copy(message_id = 2, date = 2, chat = privateChat)

    @Test
    fun `empty party call`() {
        onMessage(message, "")
        onMessage(message)

        verifyMessages(message.chat.id, ON_PARTY_EMPTY, 2)
    }

    @Test
    fun `admins party call`() {
        onMessage(privateMessage, "admins")

        verifyMessages(privateMessage.chat.id, ON_ADMINS_PARTY_FAIL)
    }

    @Test
    fun `correct single party call`() {
        val response = "@albertshady @komour"

        every { partyDao.getByIdAndName(message.chat.id, "gym") } returns response

        onMessage(message, "gym")

        verifyMessages(message.chat.id, response, replyTo = message.message_id)
    }

    @Test
    fun `correct multiple party calls`() {
        val responseGym = "@albertshady @komour"
        val responseSamara = "@g4nkedbymom @albertshady"

        val expectedResponse = "$responseGym $responseSamara".split(' ').distinct().joinToString(" ")

        every { partyDao.getByIdAndName(message.chat.id, "gym") } returns responseGym
        every { partyDao.getByIdAndName(message.chat.id, "samara") } returns responseSamara

        onMessage(message, "gym Samara")

        verifyMessages(message.chat.id, expectedResponse, replyTo = message.message_id)
    }
}
