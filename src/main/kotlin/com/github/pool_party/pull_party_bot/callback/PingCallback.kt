package com.github.pool_party.pull_party_bot.callback

import com.elbekD.bot.Bot
import com.elbekD.bot.types.CallbackQuery
import com.github.pool_party.flume.interaction.callback.Callback
import com.github.pool_party.pull_party_bot.message.ON_PING_CREATOR_MISMATCH
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ping")
data class PingCallbackData(override val partyId: Int, val creator: Int? = null) : CallbackData()

class PingCallback(private val partyDao: PartyDao) : Callback<CallbackData> {

    override val callbackDataKClass = PingCallbackData::class

    override suspend fun Bot.process(callbackQuery: CallbackQuery, callbackData: CallbackData) {
        val pingCallbackData = callbackData as? PingCallbackData ?: return
        val message = callbackQuery.message
        val party = partyDao.getByPartyId(callbackData.partyId)
        val callbackQueryId = callbackQuery.id

        if (callbackQuery.from.id != pingCallbackData.creator) {
            answerCallbackQuery(callbackQuery.id, ON_PING_CREATOR_MISMATCH)
            return
        }

        answerCallbackQuery(callbackQueryId)

        if (party == null || message == null) {
            return
        }

        val chatId = message.chat.id
        sendMessage(chatId, party.users)
        deleteMessage(chatId, message.message_id)
    }
}
