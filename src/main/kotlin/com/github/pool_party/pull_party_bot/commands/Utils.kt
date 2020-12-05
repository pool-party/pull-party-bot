package com.github.pool_party.pull_party_bot.commands

import com.elbekD.bot.Bot
import com.github.pool_party.pull_party_bot.database.addUsersCommandTransaction
import com.github.pool_party.pull_party_bot.database.changeCommandTransaction
import com.github.pool_party.pull_party_bot.database.createCommandTransaction
import com.github.pool_party.pull_party_bot.database.removeUsersCommandTransaction
import com.github.pool_party.pull_party_bot.database.rudeCheckTransaction

val PROHIBITED_SYMBOLS = listOf('!', ',', '.', '?', ':', ';', '(', ')')

enum class PartyChangeStatus(
    val changesFull: Boolean,
    val transaction: (Long, String, List<String>) -> Boolean,
    val onFailure: String
) {
    CREATE(true, ::createCommandTransaction, ON_CREATE_REQUEST_FAIL) {
        override fun onSuccess(partyName: String) = "Party $partyName successfully created!"
    },

    CHANGE(true, ::changeCommandTransaction, ON_CHANGE_REQUEST_FAIL) {
        override fun onSuccess(partyName: String) = "Party $partyName changed beyond recognition!"
    },

    ADD(false, ::addUsersCommandTransaction, ON_ADD_REQUEST_FAIL) {
        override fun onSuccess(partyName: String) = "Party $partyName is getting bigger and bigger!"
    },

    REMOVE(false, ::removeUsersCommandTransaction, ON_REMOVE_REQUEST_FAIL) {
        override fun onSuccess(partyName: String) = "Party $partyName lost somebody, but not the vibe!"
    };

    abstract fun onSuccess(partyName: String): String
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
