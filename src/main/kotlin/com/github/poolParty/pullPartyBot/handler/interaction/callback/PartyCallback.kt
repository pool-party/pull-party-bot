package com.github.poolParty.pullPartyBot.handler.interaction.callback

import com.elbekd.bot.Bot
import com.elbekd.bot.types.CallbackQuery
import com.github.poolParty.pullPartyBot.database.dao.ChatDao
import com.github.poolParty.pullPartyBot.database.dao.PartyDao
import com.github.poolParty.pullPartyBot.handler.answerCallbackQueryLogging
import com.github.poolParty.pullPartyBot.handler.deleteMessageLogging
import com.github.poolParty.pullPartyBot.handler.escapeMarkdown
import com.github.poolParty.pullPartyBot.handler.message.InformationMessages
import com.github.poolParty.pullPartyBot.handler.sendMessageLogging
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("p")
data class PartyCallback(val partyId: Int, val creator: Long?) : Callback() {

    override suspend fun Bot.process(callbackQuery: CallbackQuery, partyDao: PartyDao, chatDao: ChatDao) {
        val message = callbackQuery.message
        val party = partyDao.getByPartyId(partyId)
        val callbackQueryId = callbackQuery.id

        if (callbackQuery.from.id != creator) {
            answerCallbackQueryLogging(callbackQuery.id, InformationMessages.pingCreatorMismatch)
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
