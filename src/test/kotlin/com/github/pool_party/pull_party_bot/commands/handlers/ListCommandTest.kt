package com.github.pool_party.pull_party_bot.commands.handlers

import com.github.pool_party.pull_party_bot.commands.Command
import com.github.pool_party.pull_party_bot.commands.messages.ON_ARGUMENT_LIST_EMPTY
import com.github.pool_party.pull_party_bot.commands.messages.ON_LIST_EMPTY
import com.github.pool_party.pull_party_bot.database.Party
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test

internal class ListCommandTest : AbstractCommandTest() {

    override fun initializeCommand(partyDao: PartyDao, chatDao: ChatDao): Command = ListCommand(partyDao, chatDao)

    @Test
    fun `empty list returns data from DAO`() {
        val party = mockk<Party>()
        val name = "name"
        val users = "users"

        every { party.name } returns name
        every { party.users } returns users

        every { partyDao.getAll(message.chat.id) } returns listOf(party)
        every { chatDao.getRude(message.chat.id) } returns false

        onMessage(message)

        verify { bot.sendMessage(message.chat.id, match { it.contains(name) && it.contains(users) }) }
    }

    @Test
    fun `no party in a group`() {
        every { partyDao.getAll(message.chat.id) } returns listOf()
        every { chatDao.getRude(message.chat.id) } returns false

        onMessage(message)

        verifyMessages(message.chat.id, ON_LIST_EMPTY)
    }

    @Test
    fun `no party matched in an argument list`() {
        every { partyDao.getAll(message.chat.id) } returns listOf()
        every { chatDao.getRude(message.chat.id) } returns false

        onMessage(message, "somePartyName")

        verifyMessages(message.chat.id, ON_ARGUMENT_LIST_EMPTY)
    }

    @Test
    fun `party matched in an argument list`() {
        val party = mockk<Party>()
        val name = "name"
        val users = "users"

        every { party.name } returns name
        every { party.users } returns users

        every { partyDao.getAll(message.chat.id) } returns listOf(party)
        every { chatDao.getRude(message.chat.id) } returns false

        onMessage(message, name)

        verify { bot.sendMessage(message.chat.id, match { it.contains(name) && it.contains(users) }) }
    }
}
