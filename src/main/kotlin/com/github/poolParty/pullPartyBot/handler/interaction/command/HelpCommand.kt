package com.github.poolParty.pullPartyBot.handler.interaction.command

import com.elbekd.bot.Bot
import com.elbekd.bot.types.Message
import com.github.poolParty.pullPartyBot.handler.message.HelpMessages
import com.github.poolParty.pullPartyBot.handler.sendMessageLogging

class HelpCommand(private val helpMessages: Map<String, String>) :
    AbstractCommand("help", "show this usage guide", HelpMessages.common) {

    override suspend fun Bot.action(message: Message, args: List<String>) {
        sendMessageLogging(
            message.chat.id,
            when (args.size) {
                0 -> HelpMessages.common
                1 -> helpMessages[args[0].removePrefix("/")] ?: HelpMessages.onError
                else -> HelpMessages.onError
            },
        )
    }
}
