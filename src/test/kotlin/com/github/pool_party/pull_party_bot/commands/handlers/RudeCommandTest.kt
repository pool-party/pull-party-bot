package com.github.pool_party.pull_party_bot.commands.handlers

import com.github.pool_party.pull_party_bot.commands.Command
import com.github.pool_party.pull_party_bot.commands.messages.ON_RUDE_FAIL
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import io.mockk.every
import kotlin.test.Test

internal class RudeCommandTest : AbstractCommandTest() {

    override fun initializeCommand(partyDao: PartyDao, chatDao: ChatDao): Command = RudeCommand(chatDao)

    @Test
    fun `wrong argument call`() {
        onMessage(message, "party")
        onMessage(message, "rude")

        verifyMessages(message.chat.id, ON_RUDE_FAIL, 2)
    }

    @Test
    fun `wrong amount of args call`() {
        onMessage(message, "")
        onMessage(message, "first second")

        verifyMessages(message.chat.id, ON_RUDE_FAIL, 2)
    }

    // TODO check messages for all tests.
    @Test
    fun `correct on same call`() {
        every { chatDao.getRude(message.chat.id) } returns true
        every { chatDao.setRude(message.chat.id, true) } answers { secondArg<Boolean>() != chatDao.getRude(firstArg()) }

        onMessage(message, "on")

        verifyMessage(message.chat.id)
    }

    @Test
    fun `correct on new call`() {
        every { chatDao.getRude(message.chat.id) } returns false
        every { chatDao.setRude(message.chat.id, true) } answers { secondArg<Boolean>() != chatDao.getRude(firstArg()) }

        onMessage(message, "on")

        verifyMessage(message.chat.id)
    }

    @Test
    fun `correct off same call`() {
        every { chatDao.getRude(message.chat.id) } returns false
        every {
            chatDao.setRude(
                message.chat.id,
                false
            )
        } answers { secondArg<Boolean>() != chatDao.getRude(firstArg()) }

        onMessage(message, "off")
        verifyMessage(message.chat.id)
    }

    @Test
    fun `correct off new call`() {
        every { chatDao.getRude(message.chat.id) } returns true
        every {
            chatDao.setRude(
                message.chat.id,
                false
            )
        } answers { secondArg<Boolean>() != chatDao.getRude(firstArg()) }

        onMessage(message, "off")
        verifyMessage(message.chat.id)
    }
}
