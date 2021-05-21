package com.github.pool_party.pull_party_bot.commands.handlers.mock

import com.github.pool_party.pull_party_bot.commands.Command
import com.github.pool_party.pull_party_bot.commands.handlers.RudeCommand
import com.github.pool_party.pull_party_bot.commands.messages.ON_RUDE_FAIL
import com.github.pool_party.pull_party_bot.commands.messages.onRudeSuccess
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import io.mockk.every
import kotlin.test.Test

internal class RudeCommandTest : AbstractCommandTest() {

    override fun initializeCommand(partyDao: PartyDao, chatDao: ChatDao): Command = RudeCommand(chatDao)

    private fun parseSingleArg(args: String): String =
        args.split(' ').map { it.trim().lowercase() }.single { it.isNotBlank() }

    @Test
    fun `wrong arguments call`() {
        onMessage(message, "")
        onMessage(message, "rude")
        onMessage(message, "party")
        onMessage(message, "first second")

        verifyMessages(message.chat.id, ON_RUDE_FAIL, 4)
    }

    @Test
    fun `correct on same call`() {
        val args = "on"

        every { chatDao.getRude(message.chat.id) } returns true
        every { chatDao.setRude(message.chat.id, true) } answers { secondArg<Boolean>() != chatDao.getRude(firstArg()) }

        onMessage(message, args)

        verifyMessages(
            message.chat.id,
            onRudeSuccess(chatDao.setRude(message.chat.id, true), parseSingleArg(args)).uppercase()
        )
    }

    @Test
    fun `correct on new call`() {
        val args = "on"

        every { chatDao.getRude(message.chat.id) } returns false andThen true andThen false
        every { chatDao.setRude(message.chat.id, true) } answers { secondArg<Boolean>() != chatDao.getRude(firstArg()) }

        onMessage(message, args)

        verifyMessages(
            message.chat.id,
            onRudeSuccess(chatDao.setRude(message.chat.id, true), parseSingleArg(args)).uppercase()
        )
    }

    @Test
    fun `correct off same call`() {
        val args = "off"

        every { chatDao.getRude(message.chat.id) } returns false andThen false andThen true
        every { chatDao.setRude(message.chat.id, true) } answers { secondArg<Boolean>() != chatDao.getRude(firstArg()) }
        every {
            chatDao.setRude(
                message.chat.id,
                false
            )
        } answers { secondArg<Boolean>() != chatDao.getRude(firstArg()) }

        onMessage(message, args)

        verifyMessages(
            message.chat.id,
            onRudeSuccess(chatDao.setRude(message.chat.id, true), parseSingleArg(args))
        )
    }

    @Test
    fun `correct off new call`() {
        val args = "off"

        every { chatDao.getRude(message.chat.id) } returns true andThen false
        every { chatDao.setRude(message.chat.id, true) } answers { secondArg<Boolean>() != chatDao.getRude(firstArg()) }
        every {
            chatDao.setRude(
                message.chat.id,
                false
            )
        } answers { secondArg<Boolean>() != chatDao.getRude(firstArg()) }

        onMessage(message, args)

        verifyMessages(
            message.chat.id,
            onRudeSuccess(chatDao.setRude(message.chat.id, true), parseSingleArg(args))
        )
    }
}
