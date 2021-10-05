package com.github.pool_party.pull_party_bot.command

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.message.HELP_PARTY
import com.github.pool_party.pull_party_bot.message.ON_PARTY_EMPTY
import com.github.pool_party.pull_party_bot.message.ON_PARTY_REQUEST_FAIL
import com.github.pool_party.pull_party_bot.message.ON_PARTY_REQUEST_FAILS
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import com.github.pool_party.pull_party_bot.utils.pullParty
import com.github.pool_party.telegram_bot_utils.interaction.command.AbstractCommand
import com.github.pool_party.telegram_bot_utils.utils.sendMessageLogging

class PartyCommand(private val partyDao: PartyDao) :
    AbstractCommand(
        "party",
        "tag the members of existing parties",
        HELP_PARTY,
        listOf("party-names", "tag the members of the given parties"),
    ) {

    override suspend fun Bot.action(message: Message, args: List<String>) {
        val parsedArgs = args.distinct()
        val chatId = message.chat.id

        if (parsedArgs.isEmpty()) {
            sendMessageLogging(chatId, ON_PARTY_EMPTY)
            return
        }

        pullParty(parsedArgs.asSequence(), message, partyDao) {
            sendMessageLogging(
                chatId,
                if (parsedArgs.size == 1) ON_PARTY_REQUEST_FAIL
                else ON_PARTY_REQUEST_FAILS,
            )
        }
    }
}
