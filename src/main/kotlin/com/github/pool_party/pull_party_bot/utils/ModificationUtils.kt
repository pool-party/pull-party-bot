package com.github.pool_party.pull_party_bot.utils

import com.elbekD.bot.Bot
import com.github.pool_party.pull_party_bot.message.ON_ADMINS_PARTY_CHANGE

fun Bot.modifyCommandAssertion(chatId: Long, name: String): Boolean {
    val isAdmins = name == "admins"
    if (isAdmins) {
        sendMessage(chatId, ON_ADMINS_PARTY_CHANGE)
    }
    return !isAdmins
}
