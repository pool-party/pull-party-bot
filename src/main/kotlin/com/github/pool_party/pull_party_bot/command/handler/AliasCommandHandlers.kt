package com.github.pool_party.pull_party_bot.command.handler

import com.elbekD.bot.Bot
import com.elbekD.bot.feature.chain.chain
import com.elbekD.bot.feature.chain.jumpTo
import com.elbekD.bot.feature.chain.terminateChain
import com.elbekD.bot.types.KeyboardButton
import com.elbekD.bot.types.Message
import com.elbekD.bot.types.ReplyKeyboardMarkup
import com.elbekD.bot.types.ReplyKeyboardRemove
import com.github.pool_party.pull_party_bot.command.cache.AliasPackCache
import com.github.pool_party.pull_party_bot.command.onArgs
import com.github.pool_party.pull_party_bot.command.onNoArgumentsCommand
import com.github.pool_party.pull_party_bot.database.transaction.addAliasesTransaction
import com.github.pool_party.pull_party_bot.database.transaction.aliasExistsTransaction
import com.github.pool_party.pull_party_bot.database.transaction.getPackNamesTransaction
import com.github.pool_party.pull_party_bot.database.transaction.packExistsTransaction

fun Bot.initAliasCommandHandlers() {

    newPackCommand()
    onCommand("/activate", ::activateCommand)
    onCommand("/deactivate", ::deactivateCommand)
    onNoArgumentsCommand("/packlist", ::packListCommand)
    onCommand("/editpack") { _, _ -> TODO() }
    onCommand("/removepack", ::removePackCommand)

    onMessage(::stickerAnswerHandling)
}

private fun String.isWord() = split(' ', '\t').size == 1

private data class StickerAliasCreating(val name: String, val aliases: MutableMap<String, String> = HashMap())

fun Bot.newPackCommand() {
    val chatAliasPackMapping = HashMap<Long, StickerAliasCreating>()
    val midState = HashMap<Long, String>()

    chain("/newpack") { sendMessage(it.chat.id, "Send me your pack's name") }
        .then("name") { createPackOnName(it, chatAliasPackMapping) }
        .then("sticker") { createPackOnSticker(it, chatAliasPackMapping, midState) }
        .then("alias") { createPackOnAlias(it, chatAliasPackMapping, midState) }
        .build()
}

private fun Bot.createPackOnName(msg: Message, chatAliasPackMapping: MutableMap<Long, StickerAliasCreating>) {
    val chatId = msg.chat.id
    val trimmedName = msg.text?.trim()

    when {
        trimmedName?.isWord() != true -> {
            sendMessage(chatId, "Incorrect pack name")
            terminateChain(chatId)
        }
        packExistsTransaction(trimmedName) -> {
            sendMessage(chatId, "name is not available")
            terminateChain(chatId)
        }
        else -> {
            sendMessage(chatId, "Now send me a sticker for $trimmedName")
            chatAliasPackMapping[chatId] = StickerAliasCreating(trimmedName)
        }
    }
}

private fun Bot.createPackOnSticker(
    msg: Message,
    chatAliasPackMapping: MutableMap<Long, StickerAliasCreating>,
    midState: MutableMap<Long, String>
) {
    if (msg.text == "finish") {

        removeCallbackQueryAction()

        val chatId = msg.chat.id
        val pack = chatAliasPackMapping[chatId]!!

        val aliases = pack.aliases
        val name = pack.name

        sendMessage(
            chatId,
            when {
                aliases.isEmpty() -> "pack is empty, canceling creation..."
                addAliasesTransaction(aliases, name) -> "pack `$name` is ready"
                else -> "u'r too late"
            },
            "Markdown",
            markup = ReplyKeyboardRemove(true)
        )

        chatAliasPackMapping.remove(chatId)
        return
    }

    val chatId = msg.chat.id
    val pack = chatAliasPackMapping[chatId]!!
    val stickerId = msg.sticker?.file_id

    when {
        stickerId == null -> sendMessage(chatId, "send **sticker**", "Markdown")

        aliasExistsTransaction(stickerId, pack.name) ->
            sendMessage(chatId, "u set an alias for this sticker, choose another")

        else -> {
            midState[chatId] = stickerId
            sendMessage(
                chatId,
                "now send me an alias or cancel",
                markup = ReplyKeyboardMarkup(listOf(listOf(KeyboardButton("cancel"))), one_time_keyboard = true)
            )
        }
    }
}

private fun Bot.createPackOnAlias(
    msg: Message,
    chatAliasPackMapping: MutableMap<Long, StickerAliasCreating>,
    midState: MutableMap<Long, String>
) {
    val chatId = msg.chat.id
    val newAlias = msg.text?.trim()
    val pack = chatAliasPackMapping[chatId]!!

    when {
        newAlias?.isWord() != true -> {
            sendMessage(chatId, "send correct alias name")
            jumpTo("alias", msg)
        }

        newAlias == "cancel" -> {
            sendMessage(chatId, "Canceling pack creation", markup = ReplyKeyboardRemove(true))
            chatAliasPackMapping.remove(chatId)
            midState.remove(chatId)
            terminateChain(chatId)
        }

        else -> {
            val pairs = pack.aliases

            if (pairs.containsValue(newAlias)) {
                sendMessage(chatId, "this alias is not available, choose another")
            } else {
                pack.aliases[midState[chatId]!!] = newAlias.toLowerCase()
                midState.remove(chatId)
                sendMessage(
                    chatId,
                    "send me another sticker or \"finish\"",
                    markup = ReplyKeyboardMarkup(
                        listOf(listOf(KeyboardButton("finish"))),
                        one_time_keyboard = true
                    )
                )
            }
            jumpTo("sticker", msg)
        }
    }
}

/**
 * Activate sticker alias pack in the current chat
 */
fun Bot.activateCommand(msg: Message, args: String?) = onArgs(msg, args) { chatId, name ->
    if (AliasPackCache.activatePack(chatId, name)) "`$name` activated" else "no `$name` pack found"
}

/**
 * Deactivate sticker alias pack in the current chat
 */
fun Bot.deactivateCommand(msg: Message, args: String?) = onArgs(msg, args) { chatId, name ->
    if (AliasPackCache.deactivatePack(chatId, name)) "`$name` deactivated" else "no `$name` pack activated"
}

/**
 * Show a list of activated packs
 */
fun Bot.packListCommand(msg: Message) {
    sendMessage(msg.chat.id, getPackNamesTransaction().joinToString("\n"))
}

/**
 * Remove the given pack completely
 */
fun Bot.removePackCommand(msg: Message, args: String?) = onArgs(msg, args) { _, name ->
    val notificationList = AliasPackCache.removePack(name)

    if (notificationList == null) {
        "pack not found"
    } else {
        notificationList.forEach {
            sendMessage(it, "pack `$name` was removed, therefore deactivated", "Markdown")
        }
        "pack `$name` removed"
    }
}

/**
 * Twitch like sticker replacement
 */
fun Bot.stickerAnswerHandling(msg: Message) {
    val chatId = msg.chat.id

    val packs = AliasPackCache.load(chatId)

    if (packs.isEmpty()) {
        return
    }

    msg.text?.run {
        lineSequence()
            .flatMap { it.splitToSequence(' ', '\t') }
            .map { it.toLowerCase() }
            .mapNotNull { AliasPackCache.getStickerId(packs, it) }
            .distinct()
            .forEach { sendSticker(chatId, it, replyTo = msg.message_id) }
    }
}
