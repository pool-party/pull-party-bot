package com.github.pool_party.pull_party_bot.callback

import com.elbekD.bot.Bot
import com.elbekD.bot.types.CallbackQuery
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import com.github.pool_party.pull_party_bot.message.ON_CALLBACK_SUCCESS
import com.github.pool_party.pull_party_bot.message.onAliasDeleteSuccess
import com.github.pool_party.telegram_bot_utils.utils.sendMessageLogging
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("delete")
data class DeleteSuggestionCallbackData(override val partyId: Int) : CallbackData()

class DeleteSuggestionCallback(private val partyDao: PartyDao) : AbstractDeleteSuggestionCallback() {

    override val callbackDataKClass = DeleteSuggestionCallbackData::class

    override suspend fun Bot.delete(callbackQuery: CallbackQuery, partyId: Int) {
        val partyName = partyDao.delete(partyId)

        if (partyName != null) {
            val message = callbackQuery.message ?: return

            answerCallbackQuery(callbackQuery.id, ON_CALLBACK_SUCCESS)
            sendMessageLogging(message.chat.id, onAliasDeleteSuccess(partyName))
        }
    }
}
