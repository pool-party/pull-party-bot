package com.github.pool_party.pull_party_bot

import com.elbekD.bot.Bot
import com.elbekD.bot.server
import com.elbekD.bot.util.AllowedUpdate
import com.github.pool_party.pull_party_bot.commands.initPingCommands
import com.github.pool_party.pull_party_bot.data_base.initDB


const val APP_URL = "https://pullpartydb.herokuapp.com"
const val USER_NAME = "PullPartyBot"
const val DEFAULT_PORT = 80


fun main() {
    val token = System.getenv("TELEGRAM_TOKEN") ?: throw RuntimeException("Unable to get system variable for token")
    val isLongpoll = System.getenv("IS_LONGPOLL")?.toBoolean() ?: false

    val bot = if (isLongpoll) {
        Bot.createPolling(USER_NAME, token)
    } else {
        Bot.createWebhook(USER_NAME, token) {
            url = "$APP_URL/$token"
            allowedUpdates = listOf(AllowedUpdate.Message)

            server {
                host = "0.0.0.0"
                port = System.getenv("PORT")?.toInt() ?: DEFAULT_PORT
            }
        }
    }

    initDB()
    initPingCommands(bot)

    bot.start()
}
