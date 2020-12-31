package com.github.pool_party.pull_party_bot.commands.handlers

import com.github.pool_party.pull_party_bot.commands.Command
import com.github.pool_party.pull_party_bot.commands.messages.INIT_MSG
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import kotlin.test.Test

// TODO we could test help command then too)

internal class StartCommandTest : AbstractCommandTest() {

    override fun initializeCommand(partyDao: PartyDao, chatDao: ChatDao): Command = StartCommand()

    @Test
    fun `start sends message`() {
        onMessage(message)
        verifyMessages(message.chat.id, INIT_MSG)
    }
}
