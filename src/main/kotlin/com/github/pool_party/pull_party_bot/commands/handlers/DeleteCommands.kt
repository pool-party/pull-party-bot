package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.commands.AdministratorCommand
import com.github.pool_party.pull_party_bot.commands.messages.HELP_CLEAR
import com.github.pool_party.pull_party_bot.commands.messages.HELP_DELETE
import com.github.pool_party.pull_party_bot.commands.messages.ON_CLEAR_SUCCESS
import com.github.pool_party.pull_party_bot.commands.messages.ON_DELETE_EMPTY
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.database.dao.PartyDao

class DeleteCommand(private val partyDao: PartyDao, chatDao: ChatDao) :
    AdministratorCommand("delete", "forget the parties as they have never happened", HELP_DELETE, chatDao) {

    override fun Bot.mainAction(message: Message, args: String?) {
        val parsedArgs = parseArgs(args)?.distinct()
        val chatId = message.chat.id

        if (parsedArgs.isNullOrEmpty()) {
            sendMessage(chatId, ON_DELETE_EMPTY, "Markdown")
            return
        }

        parsedArgs.forEach {
            if (modifyCommandAssertion(chatId, it)) {
                sendCaseMessage(
                    chatId,
                    if (partyDao.delete(chatId, it))
                        """Party $it is just a history now 👍"""
                    else """Not like I knew the $it party, but now I don't know it at all 👍"""
                )
            }
        }
    }
}

class ClearCommand(chatDao: ChatDao) :
    AdministratorCommand("clear", "shut down all the parties ever existed", HELP_CLEAR, chatDao) {

    override fun Bot.mainAction(message: Message, args: String?) {
        val chatId = message.chat.id
        chatDao.clear(chatId)
        sendMessage(chatId, ON_CLEAR_SUCCESS, "Markdown")
    }
}
