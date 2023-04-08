package com.github.poolParty.pullPartyBot.handler.interaction.command

import com.elbekd.bot.Bot
import com.elbekd.bot.types.Message
import com.github.poolParty.pullPartyBot.handler.message.HelpMessages
import com.github.poolParty.pullPartyBot.handler.message.InformationMessages
import com.github.poolParty.pullPartyBot.handler.sendMessageLogging

class StartCommand : AbstractCommand("start", "start the conversation and see welcoming message", HelpMessages.start) {

    override suspend fun Bot.action(message: Message, args: List<String>) {
        sendMessageLogging(message.chat.id, InformationMessages.init)
    }
}
