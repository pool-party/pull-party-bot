package com.github.poolParty.pullPartyBot.handler

import com.elbekd.bot.Bot
import com.elbekd.bot.model.TelegramApiError
import com.elbekd.bot.model.toChatId
import com.elbekd.bot.types.InlineKeyboardButton
import com.elbekd.bot.types.InlineKeyboardMarkup
import com.elbekd.bot.types.Message
import com.elbekd.bot.types.ParseMode
import com.github.poolParty.pullPartyBot.handler.interaction.callback.CallbackData
import mu.two.KotlinLogging

private val logger = KotlinLogging.logger { }

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

data class Button(val text: String, val callbackData: CallbackData)

suspend fun Bot.sendMessageLogging(
    chatId: Long,
    text: String,
    buttons: List<Button> = emptyList(),
    replyTo: Long? = null
): Message {
    logger.debug { "Sending '$text'" }
    return sendMessage(
        chatId.toChatId(),
        text.escapeSpecial(),
        parseMode = ParseMode.MarkdownV2,
        replyToMessageId = replyTo,
        replyMarkup = InlineKeyboardMarkup(
            buttons.map {
                listOf(InlineKeyboardButton(it.text, callbackData = it.callbackData.encoded))
            }
        ),
    )
}

suspend fun Bot.deleteMessageLogging(chatId: Long, messageId: Long): Boolean {
    logger.debug { "Deleting message $chatId/$messageId" }
    return try {
        deleteMessage(chatId.toChatId(), messageId)
    } catch (e: TelegramApiError) {
        // already deleted
        true
    }
}

suspend fun Bot.answerCallbackQueryLogging(id: String, text: String? = null): Boolean {
    logger.debug { "Answering callback query $id" }
    return answerCallbackQuery(id, text)
}
