package com.github.pool_party.pull_party_bot.commands

import com.elbekD.bot.Bot
import com.elbekD.bot.http.TelegramApiError
import com.elbekD.bot.types.Message
import com.elbekD.bot.types.ReplyKeyboard
import mu.KotlinLogging
import java.util.concurrent.CompletableFuture

private val logger = KotlinLogging.logger { }

class SendingMessageException(val action: String, val reason: Throwable) : RuntimeException()

context(Bot)
fun <T> CompletableFuture<T>.logging(action: String = ""): T =
    try {
        join()
    } catch (throwable: Throwable) {
        throw SendingMessageException(action, throwable)
    }

private fun String.escape(symbols: String) = replace("[$symbols]".toRegex()) { "\\${it.groupValues[0]}" }

fun String.escapeSpecial() = escape("#\\-!.<>\\(\\)")

fun String.escapeMarkdown() = escapeSpecial().escape("_*\\[\\]`")

fun Bot.sendMessageLogging(
    chatId: Long,
    text: String,
    markup: ReplyKeyboard? = null,
    replyTo: Long? = null
): Message {
    logger.debug { "Sending '$text'" }
    return sendMessage(chatId, text.escapeSpecial(), "MarkdownV2", replyTo = replyTo, markup = markup)
        .logging("Failed to send message \"$text\"")
}

fun Bot.deleteMessageLogging(chatId: Long, messageId: Long): Boolean {
    logger.debug { "Deleting message $chatId/$messageId" }
    return try {
        deleteMessage(chatId, messageId).logging("Failed to delete message $chatId/$messageId")
    } catch (sendingMessageException: SendingMessageException) {
        if (sendingMessageException.reason !is TelegramApiError) {
            throw sendingMessageException
        } else {
            // already deleted
            true
        }
    }
}

fun Bot.answerCallbackQueryLogging(id: String, text: String? = null): Boolean {
    logger.debug { "Answering callback query $id" }
    return answerCallbackQuery(id, text).logging("Answering callback query $id")
}
