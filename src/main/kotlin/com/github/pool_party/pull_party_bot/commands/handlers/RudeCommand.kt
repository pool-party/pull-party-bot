package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekd.bot.Bot
import com.elbekd.bot.types.Message
import com.github.pool_party.pull_party_bot.commands.CaseCommand
import com.github.pool_party.pull_party_bot.commands.messages.HELP_RUDE
import com.github.pool_party.pull_party_bot.commands.messages.ON_RUDE_FAIL
import com.github.pool_party.pull_party_bot.commands.messages.onRudeSuccess
import com.github.pool_party.pull_party_bot.commands.sendMessageLogging
import com.github.pool_party.pull_party_bot.database.dao.ChatDao

class RudeCommand(chatDao: ChatDao) : CaseCommand("rude", "switch RUDE(CAPS LOCK) mode", HELP_RUDE, chatDao) {

    override suspend fun Bot.action(message: Message, args: String?) {
        val parsedArg = parseArgs(args)?.singleOrNull()
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
