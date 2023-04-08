package com.github.poolParty.pullPartyBot.handler.interaction.everyMessage

import com.elbekd.bot.Bot
import com.elbekd.bot.types.Message
import com.github.poolParty.pullPartyBot.Configuration
import com.github.poolParty.pullPartyBot.database.dao.PartyDao
import com.github.poolParty.pullPartyBot.handler.interaction.common.pullParty

class ImplicitPartyHandler(private val partyDao: PartyDao) : EveryMessageInteraction {

    /**
     * Handle implicit `@party-name`-like calls
     */
    override suspend fun Bot.onMessage(message: Message) {
        val text = message.text ?: message.caption

        if (message.forwardFrom != null || text == null) {
            return
        }

        val prohibitedSymbolsString = Configuration.PROHIBITED_SYMBOLS.joinToString("")
        val regex = Regex("(?<party>[^$prohibitedSymbolsString]*)[$prohibitedSymbolsString]*")

        val partyNames = text.lineSequence()
            .flatMap { it.split(' ', '\t').asSequence() }
            .filter { it.startsWith('@') }
            .map { it.removePrefix("@") }
            .mapNotNull { regex.matchEntire(it)?.groups?.get(1)?.value }

        pullParty(partyNames, message, partyDao)
    }
}
