package com.github.pool_party.pull_party_bot.every

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.Configuration
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import com.github.pool_party.pull_party_bot.utils.pullParty
import com.github.pool_party.telegram_bot_utils.interaction.message.EveryMessageInteraction

class PullPartyHandler(private val partyDao: PartyDao) : EveryMessageInteraction {

    override val usage = "@partyName  - tag existing party right in your message \\(bot has to be an admin\\)"

    /**
     * Handle implicit `@party-name`-like calls
     */
    override suspend fun onMessage(bot: Bot, message: Message) {
        val text = message.text ?: message.caption

        if (message.forward_from != null || text == null) {
            return
        }

        val prohibitedSymbolsString = Configuration.PROHIBITED_SYMBOLS.joinToString("")
        val regex = Regex("(?<party>[^$prohibitedSymbolsString]*)[$prohibitedSymbolsString]*")

        val partyNames = text.lineSequence()
            .flatMap { it.split(' ', '\t').asSequence() }
            .filter { it.startsWith('@') }
            .map { it.removePrefix("@") }
            .mapNotNull { regex.matchEntire(it)?.groups?.get(1)?.value }

        bot.pullParty(partyNames, message, partyDao)
    }
}
