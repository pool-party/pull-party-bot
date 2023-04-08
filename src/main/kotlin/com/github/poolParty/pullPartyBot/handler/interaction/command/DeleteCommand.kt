package com.github.poolParty.pullPartyBot.handler.interaction.command

import com.elbekd.bot.Bot
import com.elbekd.bot.types.InlineKeyboardButton
import com.elbekd.bot.types.InlineKeyboardMarkup
import com.elbekd.bot.types.Message
import com.github.poolParty.pullPartyBot.handler.sendMessageLogging
import com.github.poolParty.pullPartyBot.database.dao.PartyDao
import com.github.poolParty.pullPartyBot.handler.Button
import com.github.poolParty.pullPartyBot.handler.interaction.callback.DeleteNodeSuggestionCallbackData
import com.github.poolParty.pullPartyBot.handler.message.DeleteMessages
import com.github.poolParty.pullPartyBot.handler.message.HelpMessages
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList

class DeleteCommand(private val partyDao: PartyDao) :
    AdministratorCommand("delete", "delete the parties you provided", HelpMessages.delete) {

    override suspend fun Bot.administratorAction(message: Message, args: List<String>) {
        val parsedArgs = args.distinct()
        val chatId = message.chat.id

        if (parsedArgs.isEmpty()) {
            sendMessageLogging(chatId, DeleteMessages.deleteEmpty)
            return
        }

        parsedArgs.asFlow()
            .mapNotNull {
                if (!modifyCommandAssertion(chatId, it)) return@mapNotNull null

                val alias = partyDao.getAliasByChatIdAndName(chatId, it)
                if (alias == null) {
                    sendMessageLogging(chatId, DeleteMessages.partyDeleteUnchanged(it))
                }
                alias
            }
            .toList()
            .groupBy { it.party.id.value }
            .forEach { (partyId, aliasList) ->
                val partySize = aliasList.first().party.aliases.size

                val allSucceeded = aliasList.asSequence()
                    .map {
                        val name = it.name
                        name to partyDao.delete(chatId, name)
                    }
                    .asFlow()
                    .onEach { (name, success) ->
                        sendMessageLogging(
                            chatId,
                            if (success) DeleteMessages.aliasDeleteSuccess(name)
                            else DeleteMessages.partyDeleteUnchanged(name)
                        )
                    }
                    .toList()
                    .all { it.second }

                if (allSucceeded && aliasList.size < partySize) {
                    val partyList = aliasList.asSequence().map { it.name }.toList()

                    sendMessageLogging(
                        chatId,
                        DeleteMessages.partyDeleteSuggest(partyList),
                        buttons = listOf(Button("Delete", DeleteNodeSuggestionCallbackData(partyId))),
                    )
                }
            }
    }
}
