package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekD.bot.Bot
import com.elbekD.bot.types.CallbackQuery
import com.github.pool_party.pull_party_bot.commands.Callback
import com.github.pool_party.pull_party_bot.commands.CallbackAction
import com.github.pool_party.pull_party_bot.commands.validateAdministrator
import com.github.pool_party.pull_party_bot.database.dao.PartyDao

class RemoveSuggestionCallback(private val partyDao: PartyDao) : Callback {

    override val callbackAction: CallbackAction = CallbackAction.DELETE

    override suspend fun Bot.process(callbackQuery: CallbackQuery, partyId: Int) {
        val message = callbackQuery.message ?: return

        if (!validateAdministrator(callbackQuery.from, message.chat, false)) {
            answerCallbackQuery(
                callbackQuery.id,
                "You don't have such permission, only administrators can delete parties"
            )
            return
        }

        val partyName = partyDao.delete(partyId)

        if (partyName != null) {
            answerCallbackQuery(callbackQuery.id, "Party $partyName is just a history now")
        }

        deleteMessage(message.chat.id, message.message_id)
    }
}
