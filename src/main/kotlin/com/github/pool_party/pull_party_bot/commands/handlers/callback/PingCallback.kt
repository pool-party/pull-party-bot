package com.github.pool_party.pull_party_bot.commands.handlers.callback

import com.elbekd.bot.Bot
import com.elbekd.bot.types.CallbackQuery
import com.github.pool_party.pull_party_bot.commands.Callback
import com.github.pool_party.pull_party_bot.commands.CallbackAction
import com.github.pool_party.pull_party_bot.commands.CallbackData
import com.github.pool_party.pull_party_bot.commands.answerCallbackQueryLogging
import com.github.pool_party.pull_party_bot.commands.deleteMessageLogging
import com.github.pool_party.pull_party_bot.commands.escapeMarkdown
import com.github.pool_party.pull_party_bot.commands.messages.ON_PING_CREATOR_MISMATCH
import com.github.pool_party.pull_party_bot.commands.sendMessageLogging
import com.github.pool_party.pull_party_bot.database.dao.PartyDao

class PingCallback(private val partyDao: PartyDao) : Callback {

    override val callbackAction = CallbackAction.PING

    override suspend fun Bot.process(callbackQuery: CallbackQuery, callbackData: CallbackData) {
        val message = callbackQuery.message
        val party = partyDao.getByPartyId(callbackData.partyId)
        val callbackQueryId = callbackQuery.id

        if (callbackQuery.from.id != callbackData.creator) {
            answerCallbackQueryLogging(callbackQuery.id, ON_PING_CREATOR_MISMATCH)
            return
        }

        answerCallbackQueryLogging(callbackQueryId)

        if (party == null || message == null) {
            return
        }

        val chatId = message.chat.id
        sendMessageLogging(chatId, party.users.escapeMarkdown())
        deleteMessageLogging(chatId, message.messageId)
    }
}
