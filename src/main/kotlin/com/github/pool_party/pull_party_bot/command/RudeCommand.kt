package com.github.pool_party.pull_party_bot.command

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.message.HELP_RUDE
import com.github.pool_party.pull_party_bot.message.ON_RUDE_FAIL
import com.github.pool_party.pull_party_bot.message.onRudeSuccess
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.telegram_bot_utils.utils.sendMessageLogging

class RudeCommand(chatDao: ChatDao) : CaseCommand("rude", "switch RUDE(CAPS LOCK) mode", HELP_RUDE, chatDao) {

    override suspend fun Bot.action(message: Message, args: List<String>) {
        val parsedArg = args.singleOrNull()
        val chatId = message.chat.id

        val res = when (parsedArg) {
            "on" -> chatDao.setRude(chatId, true)
            "off" -> chatDao.setRude(chatId, false)
            else -> {
                sendMessageLogging(chatId, ON_RUDE_FAIL)
                return
            }
        }

        sendCaseMessage(chatId, onRudeSuccess(res, parsedArg))
    }
}
