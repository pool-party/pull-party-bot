package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekD.bot.types.ChatMember
import com.github.pool_party.pull_party_bot.commands.Command
import com.github.pool_party.pull_party_bot.commands.messages.ON_CLEAR_SUCCESS
import com.github.pool_party.pull_party_bot.commands.messages.ON_PERMISSION_DENY
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import io.mockk.every
import io.mockk.justRun
import kotlin.test.Test

internal class ClearCommandTest : AbstractCommandTest() {

    override fun initializeCommand(partyDao: PartyDao, chatDao: ChatDao): Command = ClearCommand(chatDao)

    private fun chatMembers(vararg chatMembers: ChatMember) =
        every { bot.getChatAdministrators(chat.id).join() } returns ArrayList(chatMembers.toList())

    @Test
    fun `permission denied on simple user call`() {
        chatMembers()
        justRun { chatDao.clear(message.chat.id) }

        onMessage(message)
        verifyMessages(message.chat.id, ON_PERMISSION_DENY)
    }

    @Test
    fun `success with administrator call`() {
        chatMembers(chatMember)
        justRun { chatDao.clear(message.chat.id) }

        onMessage(message)
        verifyMessages(message.chat.id, ON_CLEAR_SUCCESS)
    }
}
