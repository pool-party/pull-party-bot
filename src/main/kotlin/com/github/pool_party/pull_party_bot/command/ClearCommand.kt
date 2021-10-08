package com.github.pool_party.pull_party_bot.command

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.flume.interaction.command.AdministratorCommand
import com.github.pool_party.flume.utils.sendMessageLogging
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.message.HELP_CLEAR
import com.github.pool_party.pull_party_bot.message.ON_CLEAR_SUCCESS

class ClearCommand(private val chatDao: ChatDao) :
    AdministratorCommand("clear", "shut down all the parties ever existed", HELP_CLEAR) {

    override fun Bot.mainAction(message: Message, args: List<String>) {
        val chatId = message.chat.id
        chatDao.clear(chatId)
        sendMessageLogging(chatId, ON_CLEAR_SUCCESS)
    }
}
