package com.github.poolParty.pullPartyBot.handler.interaction.command

import com.elbekd.bot.Bot
import com.elbekd.bot.types.Message
import com.github.poolParty.pullPartyBot.handler.sendMessageLogging
import com.github.poolParty.pullPartyBot.database.dao.PartyDao
import com.github.poolParty.pullPartyBot.handler.interaction.common.pullParty
import com.github.poolParty.pullPartyBot.handler.message.HelpMessages
import com.github.poolParty.pullPartyBot.handler.message.PullPartyMessages

class PartyCommand(private val partyDao: PartyDao) :
    AbstractCommand("party", "tag the members of the given parties", HelpMessages.party) {

    override suspend fun Bot.action(message: Message, args: List<String>) {
        val parsedArgs = args.distinct()
        val chatId = message.chat.id

        if (parsedArgs.isEmpty()) {
            sendMessageLogging(chatId, PullPartyMessages.empty)
            return
        }

        pullParty(parsedArgs.asSequence(), message, partyDao) {
            sendMessageLogging(
                chatId,
                if (parsedArgs.size == 1) PullPartyMessages.requestFail else PullPartyMessages.requestFails,
            )
        }
    }
}
