package com.github.pool_party.pull_party_bot.command

import com.elbekD.bot.Bot
import com.elbekD.bot.types.ReplyKeyboard
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.message.ON_ADMINS_PARTY_CHANGE
import com.github.pool_party.telegram_bot_utils.interaction.command.AbstractCommand
import com.github.pool_party.telegram_bot_utils.utils.sendMessageLogging

fun Bot.modifyCommandAssertion(chatId: Long, name: String): Boolean =
    (name == "admins").not().also { if (!it) sendMessage(chatId, ON_ADMINS_PARTY_CHANGE) }

abstract class CaseCommand(command: String, description: String, helpMessage: String, protected val chatDao: ChatDao) :
    AbstractCommand(command, description, helpMessage) {

    protected fun Bot.sendCaseMessage(
        chatId: Long,
        message: String,
        replyTo: Int? = null,
        markup: ReplyKeyboard? = null
    ) =
        sendMessageLogging(
            chatId,
            if (chatDao.getRude(chatId)) message.uppercase() else message,
            replyTo = replyTo,
            markup = markup
        )
}
