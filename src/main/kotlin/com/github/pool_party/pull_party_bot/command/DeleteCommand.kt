package com.github.pool_party.pull_party_bot.command

import com.elbekD.bot.Bot
import com.elbekD.bot.types.InlineKeyboardButton
import com.elbekD.bot.types.InlineKeyboardMarkup
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.callback.DeleteNodeSuggestionCallbackData
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import com.github.pool_party.pull_party_bot.message.HELP_DELETE
import com.github.pool_party.pull_party_bot.message.ON_DELETE_EMPTY
import com.github.pool_party.pull_party_bot.message.onAliasDeleteSuccess
import com.github.pool_party.pull_party_bot.message.onPartyDeleteSuggest
import com.github.pool_party.pull_party_bot.message.onPartyDeleteUnchanged
import com.github.pool_party.pull_party_bot.utils.modifyCommandAssertion
import com.github.pool_party.telegram_bot_utils.interaction.command.AdministratorCommand
import com.github.pool_party.telegram_bot_utils.utils.sendMessageLogging

class DeleteCommand(private val partyDao: PartyDao) :
    AdministratorCommand(
        "delete",
        "forget the parties as they have never happened",
        HELP_DELETE,
        listOf("party-names", "delete the parties you provided"),
    ) {

    override fun Bot.mainAction(message: Message, args: List<String>) {
        val parsedArgs = args.distinct()
        val chatId = message.chat.id

        if (parsedArgs.isEmpty()) {
            sendMessageLogging(chatId, ON_DELETE_EMPTY)
            return
        }

        parsedArgs.asSequence()
            .mapNotNull {
                if (!modifyCommandAssertion(chatId, it)) return@mapNotNull null

                val alias = partyDao.getAliasByChatIdAndName(chatId, it)
                if (alias == null) {
                    sendMessageLogging(chatId, onPartyDeleteUnchanged(it))
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
                        sendMessageLogging(
                            chatId,
                            if (success) onAliasDeleteSuccess(name)
                            else onPartyDeleteUnchanged(name),
                        )
                    }
                    .all { it.second }

                if (allSucceeded && aliasList.size < partySize) {
                    val partyList = aliasList.asSequence().map { it.name }.toList()
                    val json = DeleteNodeSuggestionCallbackData(partyId).encoded

                    sendMessageLogging(
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
