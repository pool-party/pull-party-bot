package com.github.pool_party.pull_party_bot.commands

import com.elbekd.bot.Bot
import com.elbekd.bot.model.TelegramApiError
import com.elbekd.bot.model.toChatId
import com.elbekd.bot.types.ChatMember
import com.elbekd.bot.types.Message
import com.elbekd.bot.types.ParseMode
import com.elbekd.bot.types.ReplyKeyboard
import mu.two.KotlinLogging

private val logger = KotlinLogging.logger { }

class SendingMessageException(val action: String, val reason: Throwable) : RuntimeException()

private fun String.escape(regex: Regex) = replace(regex) { "\\${it.groupValues[0]}" }

private val USED_MARKDOWN_SYMBOLS = """[_*`\[\]\\]""".toRegex()

private val UNUSED_MARKDOWN_SYMBOLS = """[()>~#+\-=|{}.!]""".toRegex()

/**
 * Used on a message.
 */
fun String.escapeSpecial() = escape(UNUSED_MARKDOWN_SYMBOLS)

/**
 * Used on party names.
 */
fun String.escapeMarkdown() = escape(USED_MARKDOWN_SYMBOLS)

suspend fun Bot.sendMessageLogging(
    chatId: Long,
    text: String,
    markup: ReplyKeyboard? = null,
    replyTo: Long? = null
): Message {
    logger.debug { "Sending '$text'" }
    return sendMessage(
        chatId.toChatId(),
        text.escapeSpecial(),
        parseMode = ParseMode.MarkdownV2,
        replyToMessageId = replyTo,
        replyMarkup = markup,
    )
}

suspend fun Bot.deleteMessageLogging(chatId: Long, messageId: Long): Boolean {
    logger.debug { "Deleting message $chatId/$messageId" }
    return try {
        deleteMessage(chatId.toChatId(), messageId)
    } catch (sendingMessageException: SendingMessageException) {
        if (sendingMessageException.reason !is TelegramApiError) {
            throw sendingMessageException
        } else {
            // already deleted
            true
        }
    }
}

suspend fun Bot.answerCallbackQueryLogging(id: String, text: String? = null): Boolean {
    logger.debug { "Answering callback query $id" }
    return answerCallbackQuery(id, text)
}

val ChatMember.user
    get() = when (this) {
        is ChatMember.Owner -> user
        is ChatMember.Member -> user
        is ChatMember.Left -> user
        is ChatMember.Banned -> user
        is ChatMember.Restricted -> user
        is ChatMember.Administrator -> user
    }
