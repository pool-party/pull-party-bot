package com.github.poolParty.pullPartyBot.handler.interaction.callback

import com.elbekd.bot.Bot
import com.elbekd.bot.types.CallbackQuery
import com.github.poolParty.pullPartyBot.database.dao.ChatDao
import com.github.poolParty.pullPartyBot.database.dao.PartyDao
import com.github.poolParty.pullPartyBot.handler.answerCallbackQueryLogging
import com.github.poolParty.pullPartyBot.handler.deleteMessageLogging
import com.github.poolParty.pullPartyBot.handler.message.DeleteMessages
import com.github.poolParty.pullPartyBot.handler.message.InformationMessages
import com.github.poolParty.pullPartyBot.handler.sendMessageLogging
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("c")
data class ClearConfirmationCallback(val chatId: Long, val creator: Long?, val confirmed: Boolean) :
    Callback() {

    override suspend fun Bot.process(callbackQuery: CallbackQuery, partyDao: PartyDao, chatDao: ChatDao) {
        if (callbackQuery.from.id != creator) {
            answerCallbackQueryLogging(callbackQuery.id, InformationMessages.pingCreatorMismatch)
            return
        }

        if (confirmed) {
            chatDao.clear(chatId)
            sendMessageLogging(chatId, DeleteMessages.clearSuccess)
        } else {
            callbackQuery.message?.messageId?.let { deleteMessageLogging(chatId, it) }
        }
    }
}
