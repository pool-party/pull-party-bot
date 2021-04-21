package com.github.pool_party.pull_party_bot.commands.handlers.mock

import com.elbekD.bot.types.ChatMember
import com.github.pool_party.pull_party_bot.commands.Command
import com.github.pool_party.pull_party_bot.commands.handlers.DeleteCommand
import com.github.pool_party.pull_party_bot.commands.messages.ON_ADMINS_PARTY_CHANGE
import com.github.pool_party.pull_party_bot.commands.messages.ON_DELETE_EMPTY
import com.github.pool_party.pull_party_bot.commands.messages.ON_PERMISSION_DENY
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import io.mockk.every
import kotlin.test.Ignore
import kotlin.test.Test

internal class DeleteCommandTest : AbstractCommandTest() {

    override fun initializeCommand(partyDao: PartyDao, chatDao: ChatDao): Command = DeleteCommand(partyDao, chatDao)

    private fun chatMembers(vararg chatMembers: ChatMember) =
        every { bot.getChatAdministrators(chat.id).join() } returns ArrayList(chatMembers.toList())

    @Test
    fun `permission denied for a simple user`() {
        chatMembers()

        onMessage(message)

        verifyMessages(message.chat.id, ON_PERMISSION_DENY)
    }

    @Test
    fun `empty delete call`() {
        chatMembers(chatMember)

        onMessage(message, "")
        onMessage(message)

        verifyMessages(message.chat.id, ON_DELETE_EMPTY, 2)
    }

    @Test
    fun `deleting 'admins' party`() {
        chatMembers(chatMember)

        onMessage(message, "admins")

        verifyMessages(message.chat.id, ON_ADMINS_PARTY_CHANGE)
    }

    @Test
    @Ignore
    fun `consequent delete calls`() {
        chatMembers(chatMember)

        val chatId = message.chat.id
        every { partyDao.delete(chatId, any()) } answers { true } andThenAnswer { false }
        every { chatDao.getRude(chatId) } returns false

        onMessage(message, "realParty admins NotParty")

        verifyMessages(message.chat.id, """Party realparty is just a history now 👍""")
        verifyMessages(message.chat.id, ON_ADMINS_PARTY_CHANGE)
        verifyMessages(message.chat.id, """Not like I knew the notparty party, but now I don't know it at all 👍""")
    }
}
