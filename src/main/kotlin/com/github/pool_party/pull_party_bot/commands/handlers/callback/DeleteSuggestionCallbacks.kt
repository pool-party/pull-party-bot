package com.github.pool_party.pull_party_bot.commands.handlers.callback

import com.elbekD.bot.Bot
import com.elbekD.bot.types.CallbackQuery
import com.github.pool_party.pull_party_bot.commands.Callback
import com.github.pool_party.pull_party_bot.commands.CallbackAction
import com.github.pool_party.pull_party_bot.commands.messages.ON_CALLBACK_SUCCESS
import com.github.pool_party.pull_party_bot.commands.messages.ON_PARTY_DELETE_SUCCESS
import com.github.pool_party.pull_party_bot.commands.messages.ON_PERMISSION_DENY_CALLBACK
import com.github.pool_party.pull_party_bot.commands.messages.onAliasDeleteSuccess
import com.github.pool_party.pull_party_bot.commands.validateAdministrator
import com.github.pool_party.pull_party_bot.database.dao.PartyDao

abstract class AbstractDeleteSuggestionCallback(override val callbackAction: CallbackAction) : Callback {

    abstract suspend fun Bot.delete(callbackQuery: CallbackQuery, partyId: Int)

    override suspend fun Bot.process(callbackQuery: CallbackQuery, partyId: Int) {
        val message = callbackQuery.message

        if (message == null) {
            answerCallbackQuery(callbackQuery.id)
            return
        }

        if (!validateAdministrator(callbackQuery.from, message.chat, false)) {
            answerCallbackQuery(
                callbackQuery.id,
                ON_PERMISSION_DENY_CALLBACK
            )
            return
        }

        delete(callbackQuery, partyId)
        deleteMessage(message.chat.id, message.message_id)
        answerCallbackQuery(callbackQuery.id)
    }
}

class DeleteSuggestionCallback(private val partyDao: PartyDao) :
    AbstractDeleteSuggestionCallback(CallbackAction.DELETE) {

    override suspend fun Bot.delete(callbackQuery: CallbackQuery, partyId: Int) {
        val partyName = partyDao.delete(partyId)

        if (partyName != null) {
            val message = callbackQuery.message ?: return

            answerCallbackQuery(callbackQuery.id, ON_CALLBACK_SUCCESS)
            sendMessage(message.chat.id, onAliasDeleteSuccess(partyName))
        }
    }
}

class DeleteNodeSuggestionCallback(private val partyDao: PartyDao) :
    AbstractDeleteSuggestionCallback(CallbackAction.DELETE_NODE) {

    override suspend fun Bot.delete(callbackQuery: CallbackQuery, partyId: Int) {
        if (partyDao.deleteNode(partyId)) {
            val message = callbackQuery.message ?: return

            answerCallbackQuery(callbackQuery.id, ON_CALLBACK_SUCCESS)
            sendMessage(message.chat.id, ON_PARTY_DELETE_SUCCESS)
        }
    }
}
