package com.github.pool_party.pull_party_bot

import com.elbekD.bot.Bot
import com.github.pool_party.pull_party_bot.commands.initializePingCommands

const val USER_NAME = "PullPartyBot"

fun main() {
    val token = System.getenv("TELEGRAM_TOKEN") ?: throw RuntimeException("Unable to get system variable for token")
    val bot = Bot.createPolling(USER_NAME, token)
    initializePingCommands(bot)
    bot.start()
}
