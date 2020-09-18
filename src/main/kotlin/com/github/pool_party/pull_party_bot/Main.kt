package com.github.pool_party.pull_party_bot

import com.elbekD.bot.Bot
import com.elbekD.bot.server
import com.elbekD.bot.util.AllowedUpdate
import com.github.pool_party.pull_party_bot.command.handler.initCommandHandlers
import com.github.pool_party.pull_party_bot.database.transaction.initDB

fun main() {
    val token = Configuration.TELEGRAM_TOKEN
    val userName = Configuration.USER_NAME

    val bot = if (Configuration.IS_LONGPOLL) {
        Bot.createPolling(userName, token)
    } else {
        Bot.createWebhook(userName, token) {
            url = "${Configuration.APP_URL}/$token"
            allowedUpdates = listOf(AllowedUpdate.Message)

            server {
                host = "0.0.0.0"
                port = Configuration.PORT
            }
        }
    }

    try {
        initDB()
    } catch (e: Exception) {
        println(e.message)
        return
    }
    bot.initCommandHandlers()
    bot.start()
}
