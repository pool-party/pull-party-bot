package com.github.pool_party.pull_party_bot.commands

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message

interface EveryMessageInteraction {

    suspend fun onMessage(bot: Bot, message: Message)
}

class EveryMessageProcessor(private val interactions: List<EveryMessageInteraction>) : Interaction {

    override fun onMessage(bot: Bot) = bot.onMessage { message ->
        loggingError(bot) {
            interactions.forEach { it.onMessage(bot, message) }
        }
    }
}
