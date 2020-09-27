package com.github.pool_party.pull_party_bot

import com.elbekD.bot.Bot
import com.elbekD.bot.server
import com.elbekD.bot.util.AllowedUpdate
import com.github.pool_party.pull_party_bot.command.handler.initCommandHandlers
import com.github.pool_party.pull_party_bot.database.transaction.initDB

fun main() {
    val bot = if (Configuration.IS_LONGPOLLING) {
        Bot.createPolling(Configuration.USER_NAME, Configuration.TELEGRAM_TOKEN)
    } else {
        Bot.createWebhook(Configuration.USER_NAME, Configuration.TELEGRAM_TOKEN) {
            url = "${Configuration.APP_URL}/${Configuration.TELEGRAM_TOKEN}"
            allowedUpdates = listOf(AllowedUpdate.Message)

            server {
                host = "0.0.0.0"
                port = Configuration.PORT
            }
        }
    }

    try {
        initDB(Configuration.DATABASE_URl, Configuration.DATABASE_USERNAME, Configuration.DATABASE_PASSWORD)
    } catch (e: Exception) {
        println(e.message)
        return
    }
    bot.initCommandHandlers()
    bot.start()
}
