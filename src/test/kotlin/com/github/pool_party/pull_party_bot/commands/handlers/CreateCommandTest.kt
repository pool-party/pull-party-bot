package com.github.pool_party.pull_party_bot.commands.handlers

import com.github.pool_party.pull_party_bot.commands.Command
import com.github.pool_party.pull_party_bot.commands.messages.ON_CREATE_EMPTY
import com.github.pool_party.pull_party_bot.commands.messages.ON_SINGLETON_PARTY
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import org.junit.Test

internal class CreateCommandTest : AbstractCommandTest() {
    override fun initializeCommand(partyDao: PartyDao, chatDao: ChatDao): Command = CreateCommand(partyDao, chatDao)

    @Test
    fun `create empty party`() {
        onMessage(message, "party shrt reallyReallyReallyReallyReallyName")
        onMessage(message, "party john-smith ålesund")
        onMessage(message, "party")
        onMessage(message, "")
        onMessage(message)

        verifyMessages(message.chat.id, ON_CREATE_EMPTY, 5)
    }

    @Test
    fun `make singleton party`() {
        onMessage(message, "party party")
        onMessage(message, "party party party")
        onMessage(message, "newUser newUser")
        onMessage(message, "newUser newUser @newUser")
        onMessage(message, "sameTag @sameTag")
        onMessage(message, "@sameTag sameTag")

        verifyMessages(message.chat.id, ON_SINGLETON_PARTY, 6)
    }
}
