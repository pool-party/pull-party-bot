package com.github.pool_party.pull_party_bot.command

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.flume.interaction.command.AbstractCommand
import com.github.pool_party.flume.utils.sendMessageLogging
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.message.HELP_RUDE
import com.github.pool_party.pull_party_bot.message.ON_RUDE_FAIL
import com.github.pool_party.pull_party_bot.message.onRudeSuccess

class RudeCommand(private val chatDao: ChatDao) :
    AbstractCommand(
        "rude",
        "switch RUDE\\(CAPS LOCK\\) mode",
        HELP_RUDE,
        listOf("on/off", "switch RUDE\\(CAPS LOCK\\) mode"),
    ) {

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

        sendMessageLogging(chatId, onRudeSuccess(res, parsedArg))
    }
}
