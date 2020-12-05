package com.github.pool_party.pull_party_bot.commands

import com.elbekD.bot.Bot
import com.github.pool_party.pull_party_bot.database.rudeCheckTransaction

val PROHIBITED_SYMBOLS = listOf('!', ',', '.', '?', ':', ';', '(', ')')

enum class PartyChangeStatus(val mode: Int) {
    CREATE(0),
    CHANGE(1),
    ADD(2),
    REMOVE(3)
}

fun Bot.modifyCommandAssertion(chatId: Long, name: String): Boolean =
    name.equals("admins").not().also { if (!it) sendMessage(chatId, ON_ADMINS_PARTY_CHANGE, "Markdown") }

fun parseArgs(args: String?): List<String>? =
    args?.split(' ')?.map { it.trim().toLowerCase() }?.filter { it.isNotBlank() }

fun Bot.sendCaseMessage(chatId: Long, msg: String, parseMode: String? = null, replyTo: Int? = null) =
    sendMessage(
        chatId,
        if (rudeCheckTransaction(chatId)) msg.toUpperCase() else msg,
        parseMode,
        replyTo = replyTo
    )
