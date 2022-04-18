package com.github.pool_party.pull_party_bot.commands

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.elbekD.bot.types.ReplyKeyboard
import mu.KotlinLogging
import java.util.concurrent.CompletableFuture

private val logger = KotlinLogging.logger { }

fun <T> CompletableFuture<T>.logging(prefix: String = ""): CompletableFuture<T> = handleAsync { value, throwable ->
    if (throwable != null) {
        logger.error { "$prefix: ${throwable.message}:\n${throwable.stackTraceToString()}" }
        throw throwable
    }
    value
}

fun Bot.sendMessageLogging(
    chatId: Long,
    text: String,
    markup: ReplyKeyboard? = null,
    replyTo: Long? = null
): CompletableFuture<out Message> {
    logger.debug { "Sending '$text'" }
    return sendMessage(chatId, text, "MarkdownV2", replyTo = replyTo, markup = markup)
        .logging("Failed to send message \"$text\"")
}

fun Bot.deleteMessageLogging(chatId: Long, messageId: Long): CompletableFuture<out Boolean> {
    logger.debug { "Deleting message $chatId/$messageId" }
    return deleteMessage(chatId, messageId).logging("Failed to delete message $chatId/$messageId")
}

fun Bot.answerCallbackQueryLogging(id: String, text: String? = null): CompletableFuture<out Boolean> {
    logger.debug { "Answering callback query $id" }
    return answerCallbackQuery(id, text).logging("Answering callback query $id")
}
