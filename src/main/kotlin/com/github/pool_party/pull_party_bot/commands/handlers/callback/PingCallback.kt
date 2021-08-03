package com.github.pool_party.pull_party_bot.commands.handlers.callback

import com.elbekD.bot.Bot
import com.elbekD.bot.types.CallbackQuery
import com.github.pool_party.pull_party_bot.Configuration
import com.github.pool_party.pull_party_bot.commands.Callback
import com.github.pool_party.pull_party_bot.commands.CallbackAction
import com.github.pool_party.pull_party_bot.commands.CallbackData
import com.github.pool_party.pull_party_bot.commands.messages.ON_PING_CREATOR_MISMATCH
import com.github.pool_party.pull_party_bot.commands.messages.ON_PING_OUTDATE
import com.github.pool_party.pull_party_bot.database.dao.PartyDao

class PingCallback(private val partyDao: PartyDao) : Callback {

    override val callbackAction = CallbackAction.PING

    override suspend fun Bot.process(callbackQuery: CallbackQuery, callbackData: CallbackData) {
        val message = callbackQuery.message
        val party = partyDao.getByPartyId(callbackData.partyId)
        val callbackQueryId = callbackQuery.id

        if (callbackQuery.from.id != callbackData.creator) {
            answerCallbackQuery(callbackQuery.id, ON_PING_CREATOR_MISMATCH)
            return
        }

        if (party == null || message == null) {
            answerCallbackQuery(callbackQueryId)
            return
        }

        if (System.currentTimeMillis() / 1000 - message.date > Configuration.STALE_PING_SECONDS) {
            answerCallbackQuery(callbackQuery.id, ON_PING_OUTDATE)
        } else {
            answerCallbackQuery(callbackQueryId)
            sendMessage(message.chat.id, party.users)
        }

        deleteMessage(message.chat.id, message.message_id)
    }
}
