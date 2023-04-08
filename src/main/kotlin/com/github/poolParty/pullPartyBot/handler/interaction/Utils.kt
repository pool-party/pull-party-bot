package com.github.poolParty.pullPartyBot.handler.interaction

import com.elbekd.bot.Bot
import com.elbekd.bot.model.toChatId
import com.elbekd.bot.types.Chat
import com.elbekd.bot.types.ParseMode
import com.elbekd.bot.types.User
import com.github.poolParty.pullPartyBot.Configuration
import com.github.poolParty.pullPartyBot.handler.escapeSpecial
import com.github.poolParty.pullPartyBot.handler.message.ChangePartyMessages
import com.github.poolParty.pullPartyBot.handler.message.InformationMessages
import com.github.poolParty.pullPartyBot.handler.sendMessageLogging
import mu.two.KotlinLogging

suspend fun Bot.validateAdministrator(user: User?, chat: Chat, sendMessage: Boolean = true): Boolean {
    val chatId = chat.id

    if (user == null) {
        sendMessageLogging(chatId, ChangePartyMessages.senderFail)
        return false
    }

    val chatType = chat.type
    if ((chatType == "group" || chatType == "supergroup") &&
        getChatAdministrators(chatId.toChatId()).all { it.user != user }
    ) {
        if (sendMessage) {
            sendMessageLogging(chatId, ChangePartyMessages.permissionDeny)
        }
        return false
    }
    return true
}

private val logger = KotlinLogging.logger { }

suspend fun <T> Bot.loggingError(context: String? = null, action: suspend () -> T): T? =
    try {
        action()
    } catch (throwable: Throwable) {
        logger.error {
            val contextInfo = if (context == null) "" else "$context:\n"
            "$contextInfo${throwable.message}:\n${throwable.stackTraceToString()}"
        }
        sendMessage(
            Configuration.DEVELOP_CHAT_ID.toChatId(),
            InformationMessages.error(throwable).escapeSpecial(),
            parseMode = ParseMode.MarkdownV2,
        )

        null
    }
