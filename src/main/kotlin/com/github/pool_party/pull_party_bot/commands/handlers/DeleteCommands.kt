package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekd.bot.Bot
import com.elbekd.bot.types.InlineKeyboardButton
import com.elbekd.bot.types.InlineKeyboardMarkup
import com.elbekd.bot.types.Message
import com.github.pool_party.pull_party_bot.commands.AdministratorCommand
import com.github.pool_party.pull_party_bot.commands.CallbackAction
import com.github.pool_party.pull_party_bot.commands.CallbackData
import com.github.pool_party.pull_party_bot.commands.messages.HELP_CLEAR
import com.github.pool_party.pull_party_bot.commands.messages.HELP_DELETE
import com.github.pool_party.pull_party_bot.commands.messages.ON_CLEAR_SUCCESS
import com.github.pool_party.pull_party_bot.commands.messages.ON_DELETE_EMPTY
import com.github.pool_party.pull_party_bot.commands.messages.onAliasDeleteSuccess
import com.github.pool_party.pull_party_bot.commands.messages.onPartyDeleteSuggest
import com.github.pool_party.pull_party_bot.commands.messages.onPartyDeleteUnchanged
import com.github.pool_party.pull_party_bot.commands.sendMessageLogging
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DeleteCommand(private val partyDao: PartyDao, chatDao: ChatDao) :
    AdministratorCommand("delete", "delete the parties you provided", HELP_DELETE, chatDao) {

    override suspend fun Bot.mainAction(message: Message, args: String?) {
        val parsedArgs = parseArgs(args)?.distinct()
        val chatId = message.chat.id

        if (parsedArgs.isNullOrEmpty()) {
            sendMessageLogging(chatId, ON_DELETE_EMPTY)
            return
        }

        parsedArgs.asFlow()
            .mapNotNull {
                if (!modifyCommandAssertion(chatId, it)) return@mapNotNull null

                val alias = partyDao.getAliasByChatIdAndName(chatId, it)
                if (alias == null) {
                    sendCaseMessage(chatId, onPartyDeleteUnchanged(it))
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
                        sendCaseMessage(
                            chatId,
                            if (success) onAliasDeleteSuccess(name)
                            else onPartyDeleteUnchanged(name)
                        )
                    }
                    .toList()
                    .all { it.second }

                if (allSucceeded && aliasList.size < partySize) {
                    val partyList = aliasList.asSequence().map { it.name }.toList()
                    val json = Json.encodeToString(CallbackData(CallbackAction.DELETE_NODE, partyId))

                    sendCaseMessage(
                        chatId,
                        onPartyDeleteSuggest(partyList),
                        markup = InlineKeyboardMarkup(
                            listOf(listOf(InlineKeyboardButton("Delete", callbackData = json)))
                        )
                    )
                }
            }
    }
}

class ClearCommand(chatDao: ChatDao) :
    AdministratorCommand("clear", "shut down all the parties ever existed", HELP_CLEAR, chatDao) {

    override suspend fun Bot.mainAction(message: Message, args: String?) {
        val chatId = message.chat.id
        chatDao.clear(chatId)
        sendMessageLogging(chatId, ON_CLEAR_SUCCESS)
    }
}
