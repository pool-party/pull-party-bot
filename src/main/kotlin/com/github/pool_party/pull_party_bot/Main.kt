package com.github.pool_party.pull_party_bot

import com.elbekD.bot.Bot
import com.elbekD.bot.server
import com.github.pool_party.pull_party_bot.commands.initHandlers
import com.github.pool_party.pull_party_bot.database.initDB

fun main() {
    val token = Configuration.TELEGRAM_TOKEN
    val userName = Configuration.USERNAME

    val bot = if (Configuration.LONGPOLL) {
        Bot.createPolling(userName, token)
    } else {
        Bot.createWebhook(userName, token) {
            url = "${Configuration.APP_URL}/$token"

            server {
                host = Configuration.HOST
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
    bot.initHandlers()
    bot.start()
}
