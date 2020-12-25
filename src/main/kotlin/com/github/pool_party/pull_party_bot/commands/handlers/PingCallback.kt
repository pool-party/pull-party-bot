package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekD.bot.Bot
import com.elbekD.bot.types.CallbackQuery
import com.github.pool_party.pull_party_bot.commands.Callback
import com.github.pool_party.pull_party_bot.commands.CallbackAction
import com.github.pool_party.pull_party_bot.database.dao.PartyDao

class PingCallback(private val partyDao: PartyDao) : Callback {

    override val callbackAction = CallbackAction.PING

    override suspend fun Bot.process(callbackQuery: CallbackQuery, partyId: Int) {
        answerCallbackQuery(callbackQuery.id)
        val message = callbackQuery.message ?: return
        val party = partyDao.getById(partyId) ?: return
        sendMessage(message.chat.id, party.users)
    }
}
