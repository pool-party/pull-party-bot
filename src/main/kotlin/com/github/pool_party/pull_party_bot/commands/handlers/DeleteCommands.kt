package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekD.bot.Bot
import com.elbekD.bot.types.InlineKeyboardButton
import com.elbekD.bot.types.InlineKeyboardMarkup
import com.elbekD.bot.types.Message
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DeleteCommand(private val partyDao: PartyDao, chatDao: ChatDao) :
    AdministratorCommand("delete", "forget the parties as they have never happened", HELP_DELETE, chatDao) {

    override fun Bot.mainAction(message: Message, args: String?) {
        val parsedArgs = parseArgs(args)?.distinct()
        val chatId = message.chat.id

        if (parsedArgs.isNullOrEmpty()) {
            sendMessageLogging(chatId, ON_DELETE_EMPTY)
            return
        }

        parsedArgs.asSequence()
            .mapNotNull {
                if (!modifyCommandAssertion(chatId, it)) return@mapNotNull null

                val alias = partyDao.getAliasByChatIdAndName(chatId, it)
                if (alias == null) {
                    sendCaseMessage(chatId, onPartyDeleteUnchanged(it))
                }
                alias
            }
            .groupBy { it.party.id.value }
            .forEach { (partyId, aliasList) ->
                val partySize = aliasList.first().party.aliases.size

                val allSucceeded = aliasList.asSequence()
                    .map {
                        val name = it.name
                        name to partyDao.delete(chatId, name)
                    }
                    .onEach { (name, success) ->
                        sendCaseMessage(
                            chatId,
                            if (success) onAliasDeleteSuccess(name)
                            else onPartyDeleteUnchanged(name)
                        )
                    }
                    .all { it.second }

                if (allSucceeded && aliasList.size < partySize) {
                    val partyList = aliasList.asSequence().map { it.name }.toList()
                    val json = Json.encodeToString(CallbackData(CallbackAction.DELETE_NODE, partyId))

                    sendCaseMessage(
                        chatId,
                        onPartyDeleteSuggest(partyList),
                        markup = InlineKeyboardMarkup(
                            listOf(listOf(InlineKeyboardButton("Delete", callback_data = json)))
                        )
                    )
                }
            }
    }
}

class ClearCommand(chatDao: ChatDao) :
    AdministratorCommand("clear", "shut down all the parties ever existed", HELP_CLEAR, chatDao) {

    override fun Bot.mainAction(message: Message, args: String?) {
        val chatId = message.chat.id
        chatDao.clear(chatId)
        sendMessageLogging(chatId, ON_CLEAR_SUCCESS)
    }
}
