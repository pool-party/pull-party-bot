package com.github.pool_party.pull_party_bot.callback

import com.elbekD.bot.Bot
import com.elbekD.bot.types.CallbackQuery
import com.github.pool_party.flume.interaction.callback.Callback
import com.github.pool_party.flume.utils.deleteMessageLogging
import com.github.pool_party.flume.utils.validateAdministrator
import com.github.pool_party.pull_party_bot.message.ON_PERMISSION_DENY_CALLBACK

abstract class AbstractDeleteSuggestionCallback : Callback<CallbackData> {

    abstract suspend fun Bot.delete(callbackQuery: CallbackQuery, partyId: Int)

    override suspend fun Bot.process(callbackQuery: CallbackQuery, callbackData: CallbackData) {
        val message = callbackQuery.message

        if (message == null) {
            answerCallbackQuery(callbackQuery.id)
            return
        }

        if (!validateAdministrator(callbackQuery.from, message.chat)) {
            answerCallbackQuery(
                callbackQuery.id,
                ON_PERMISSION_DENY_CALLBACK
            )
            return
        }

        delete(callbackQuery, callbackData.partyId)
        deleteMessageLogging(message.chat.id, message.message_id)
        answerCallbackQuery(callbackQuery.id)
    }
}
