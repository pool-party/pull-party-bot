package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.commands.CaseCommand
import com.github.pool_party.pull_party_bot.commands.messages.HELP_ALIAS
import com.github.pool_party.pull_party_bot.commands.messages.ON_PARTY_NAME_FAIL
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.database.dao.PartyDao

class AliasCommand(
    private val partyDao: PartyDao,
    chatDao: ChatDao,
) : CaseCommand("alias", "create a party alias", HELP_ALIAS, chatDao) {

    override fun Bot.action(message: Message, args: String?) {
        val parsedArgs = parseArgs(args)
        val chatId = message.chat.id

        if (parsedArgs.isNullOrEmpty() || parsedArgs.size != 2) {
            sendMessage(chatId, "TODO: fail", "Markdown")
            return
        }

        val (aliasName, partyName) = parsedArgs

        if (!validatePartyName(partyName)) {
            sendMessage(chatId, ON_PARTY_NAME_FAIL, "Markdown")
            return
        }

        sendMessage(
            chatId,
            if (partyDao.createAlias(chatId, aliasName, partyName)) "TODO: Good"
            else "TODO: Bad",
            "Markdown"
        )
    }
}
