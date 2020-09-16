package com.github.pool_party.pull_party_bot.commands

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.database.rudeCheckTransaction

val PROHIBITED_SYMBOLS = listOf('!', ',', '.', '?', ':', ';', '(', ')')

fun Bot.onNoArgumentsCommand(command: String, action: (Message) -> Unit) =
    onCommand(command) { msg, _ -> action(msg) }

fun Bot.onAdministratorCommand(command: String, action: (Message, String?) -> Unit) =
    onCommand(command) { msg, args ->
        val sender = msg.from
        val chatId = msg.chat.id
        if (sender == null) {
            sendMessage(chatId, ON_SENDER_FAIL, "Markdown")
            return@onCommand
        }

        val chatType = msg.chat.type
        if ((chatType == "group" || chatType == "supergroup") &&
            getChatAdministrators(chatId).join().all { it.user != sender }
        ) {
            sendMessage(chatId, ON_PERMISSION_DENY, "Markdown")
            return@onCommand
        }

        action(msg, args)
    }

fun Bot.modifyCommandAssertion(chatId: Long, name: String): Boolean =
    name.equals("admins").not().also { if (!it) sendMessage(chatId, ON_ADMINS_PARTY_CHANGE, "Markdown") }

fun parseArgs(args: String?): List<String>? = args?.split(' ')?.map { it.trim().toLowerCase() }?.distinct()

fun Bot.sendCaseMessage(chatId: Long, msg: String, parseMode: String? = null, replyTo: Int? = null) =
    sendMessage(
        chatId,
        if (rudeCheckTransaction(chatId)) msg.toUpperCase() else msg,
        parseMode,
        replyTo = replyTo
    )
