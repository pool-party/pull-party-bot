package com.github.poolParty.pullPartyBot.handler.interaction.callback

import com.elbekd.bot.Bot
import com.elbekd.bot.types.CallbackQuery
import com.github.poolParty.pullPartyBot.database.dao.ChatDao
import com.github.poolParty.pullPartyBot.database.dao.PartyDao
import com.github.poolParty.pullPartyBot.handler.answerCallbackQueryLogging
import com.github.poolParty.pullPartyBot.handler.deleteMessageLogging
import com.github.poolParty.pullPartyBot.handler.interaction.validateAdministrator
import com.github.poolParty.pullPartyBot.handler.message.DeleteMessages
import com.github.poolParty.pullPartyBot.handler.sendMessageLogging
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private suspend fun Bot.processDeleteCallback(callbackQuery: CallbackQuery, delete: suspend Bot.() -> Unit) {
    val message = callbackQuery.message

    if (message == null) {
        answerCallbackQueryLogging(callbackQuery.id)
        return
    }

    if (!validateAdministrator(callbackQuery.from, message.chat, false)) {
        answerCallbackQueryLogging(callbackQuery.id, DeleteMessages.permissionDenyCallback)
        return
    }

    delete()
    deleteMessageLogging(message.chat.id, message.messageId)
    answerCallbackQueryLogging(callbackQuery.id)
}

@Serializable
@SerialName("d")
data class DeleteSuggestionCallback(val partyId: Int) : Callback() {

    override suspend fun Bot.process(callbackQuery: CallbackQuery, partyDao: PartyDao, chatDao: ChatDao) {
        processDeleteCallback(callbackQuery) {
            val partyName = partyDao.delete(partyId) ?: return@processDeleteCallback
            val message = callbackQuery.message ?: return@processDeleteCallback

            answerCallbackQueryLogging(callbackQuery.id, DeleteMessages.callbackSuccess)
            sendMessageLogging(message.chat.id, DeleteMessages.aliasDeleteSuccess(partyName))
        }
    }
}

@Serializable
@SerialName("dn")
data class DeleteNodeSuggestionCallback(val partyId: Int) : Callback() {

    override suspend fun Bot.process(callbackQuery: CallbackQuery, partyDao: PartyDao, chatDao: ChatDao) {
        processDeleteCallback(callbackQuery) {
            if (partyDao.deleteNode(partyId)) {
                val message = callbackQuery.message ?: return@processDeleteCallback

                answerCallbackQueryLogging(callbackQuery.id, DeleteMessages.callbackSuccess)
                sendMessageLogging(message.chat.id, DeleteMessages.partyDeleteSuccess)
            }
        }
    }
}
