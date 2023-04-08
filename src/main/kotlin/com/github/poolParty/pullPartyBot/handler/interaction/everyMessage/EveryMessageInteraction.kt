package com.github.poolParty.pullPartyBot.handler.interaction.everyMessage

import com.elbekd.bot.Bot
import com.elbekd.bot.types.Message
import com.github.poolParty.pullPartyBot.handler.interaction.Interaction
import com.github.poolParty.pullPartyBot.handler.interaction.loggingError

interface EveryMessageInteraction {

    suspend fun Bot.onMessage(message: Message)
}

class EveryMessageProcessor(private val interactions: List<EveryMessageInteraction>) : Interaction {

    override fun Bot.apply() = onMessage { message ->
        loggingError("Failed processing message ${message.text}") {
            interactions.forEach {
                with(it) { onMessage(message) }
            }
        }
    }
}
