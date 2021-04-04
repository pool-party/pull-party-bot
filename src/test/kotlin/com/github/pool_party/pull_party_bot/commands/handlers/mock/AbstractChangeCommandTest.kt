package com.github.pool_party.pull_party_bot.commands.handlers.mock

import com.github.pool_party.pull_party_bot.commands.Command
import com.github.pool_party.pull_party_bot.commands.handlers.AddCommand
import com.github.pool_party.pull_party_bot.commands.messages.ON_ADMINS_PARTY_CHANGE
import com.github.pool_party.pull_party_bot.commands.messages.ON_CHANGE_EMPTY
import com.github.pool_party.pull_party_bot.commands.messages.ON_CHANGE_REQUEST_FAIL
import com.github.pool_party.pull_party_bot.commands.messages.ON_PARTY_NAME_FAIL
import com.github.pool_party.pull_party_bot.commands.messages.ON_USERS_FAIL
import com.github.pool_party.pull_party_bot.commands.messages.onAddSuccess
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import io.mockk.every
import kotlin.test.Test

internal class AbstractChangeCommandTest : AbstractCommandTest() {

    override fun initializeCommand(partyDao: PartyDao, chatDao: ChatDao): Command = AddCommand(partyDao, chatDao)

    @Test
    fun `change to empty party`() {
        onMessage(message, "party shrt reallyReallyReallyReallyReallyName")
        onMessage(message, "party john-smith ålesund")
        onMessage(message, "party")
        onMessage(message, "")
        onMessage(message)

        verifyMessages(message.chat.id, ON_CHANGE_EMPTY, 5)
    }

    @Test
    fun `change party with wrong name`() {
        onMessage(message, "reallyReallyReallyReallyReallyReallyReallyLongPartyName user")
        onMessage(message, "party?maybe someone")
        onMessage(message, "shuffle- shuffle45")
        onMessage(message, "@new@Party shuffle45")

        verifyMessages(message.chat.id, ON_PARTY_NAME_FAIL, 4)
    }

    @Test
    fun `change admins party`() {
        onMessage(message, "admins user")

        verifyMessages(message.chat.id, ON_ADMINS_PARTY_CHANGE, 1)
    }

    @Test
    fun `change with invalid users`() {
        val partyName = "party"

        every { chatDao.getRude(message.chat.id) } returns false
        every { partyDao.addUsers(message.chat.id, any(), any()) } returns true

        onMessage(message, "$partyName notShort reallyReallyReallyReallyReallyName")
        onMessage(message, "nextParty john-smith alesund")

        verifyMessages(message.chat.id, ON_USERS_FAIL, 2)
        verifyMessages(message.chat.id, onAddSuccess(partyName), 1)
    }

    @Test
    fun `change with failed transaction`() {
        every { chatDao.getRude(message.chat.id) } returns false
        every { partyDao.addUsers(message.chat.id, any(), any()) } returns false

        onMessage(message, "party notShort reallyReallyReallyReallyReallyName")
        onMessage(message, "nextParty john-smith alesund")

        verifyMessages(message.chat.id, ON_USERS_FAIL, 2)
        verifyMessages(message.chat.id, ON_CHANGE_REQUEST_FAIL, 2)
    }

    // TODO test specific commands transactions?
}
