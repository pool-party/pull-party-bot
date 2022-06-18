package com.github.pool_party.pull_party_bot.commands

import com.elbekd.bot.Bot
import com.elbekd.bot.types.Message

interface EveryMessageInteraction {

    suspend fun onMessage(bot: Bot, message: Message)
}

class EveryMessageProcessor(private val interactions: List<EveryMessageInteraction>) : Interaction {

    override fun onMessage(bot: Bot) = bot.onMessage { message ->
        bot.loggingError {
            interactions.forEach { it.onMessage(bot, message) }
        }
    }
}
