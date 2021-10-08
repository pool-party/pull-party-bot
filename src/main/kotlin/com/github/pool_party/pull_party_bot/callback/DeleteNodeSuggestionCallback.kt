package com.github.pool_party.pull_party_bot.callback

import com.elbekD.bot.Bot
import com.elbekD.bot.types.CallbackQuery
import com.github.pool_party.flume.utils.sendMessageLogging
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import com.github.pool_party.pull_party_bot.message.ON_CALLBACK_SUCCESS
import com.github.pool_party.pull_party_bot.message.ON_PARTY_DELETE_SUCCESS
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("node")
data class DeleteNodeSuggestionCallbackData(override val partyId: Int) : CallbackData()

class DeleteNodeSuggestionCallback(private val partyDao: PartyDao) : AbstractDeleteSuggestionCallback() {

    override val callbackDataKClass = DeleteNodeSuggestionCallbackData::class

    override suspend fun Bot.delete(callbackQuery: CallbackQuery, partyId: Int) {
        if (partyDao.deleteNode(partyId)) {
            val message = callbackQuery.message ?: return

            answerCallbackQuery(callbackQuery.id, ON_CALLBACK_SUCCESS)
            sendMessageLogging(message.chat.id, ON_PARTY_DELETE_SUCCESS)
        }
    }
}
