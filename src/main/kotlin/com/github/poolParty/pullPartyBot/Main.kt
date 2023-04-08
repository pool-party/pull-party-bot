package com.github.poolParty.pullPartyBot

import com.elbekd.bot.Bot
import com.elbekd.bot.server
import com.github.poolParty.pullPartyBot.handler.interaction.initHandlers
import com.github.poolParty.pullPartyBot.database.initDB

suspend fun main() {
    val token = Configuration.TELEGRAM_TOKEN
    val userName = Configuration.USERNAME

    val bot = if (Configuration.LONGPOLL) {
        Bot.createPolling(token, userName)
    } else {
        Bot.createWebhook(token, userName) {
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
