package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.commands.CaseCommand
import com.github.pool_party.pull_party_bot.commands.messages.HELP_ALIAS
import com.github.pool_party.pull_party_bot.commands.messages.ON_ALIAS_FAIL
import com.github.pool_party.pull_party_bot.commands.messages.ON_ALIAS_PARSE_FAIL
import com.github.pool_party.pull_party_bot.commands.messages.ON_CREATE_REQUEST_FAIL
import com.github.pool_party.pull_party_bot.commands.messages.ON_PARTY_NAME_FAIL
import com.github.pool_party.pull_party_bot.commands.messages.onAliasSuccess
import com.github.pool_party.pull_party_bot.commands.sendMessageLogging
import com.github.pool_party.pull_party_bot.database.dao.AliasCreationResult
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.database.dao.PartyDao

class AliasCommand(
    private val partyDao: PartyDao,
    chatDao: ChatDao,
) : CaseCommand("alias", "create a party alias", HELP_ALIAS, chatDao) {

    override suspend fun Bot.action(message: Message, args: String?) {
        val parsedArgs = parseArgs(args)
        val chatId = message.chat.id

        if (parsedArgs.isNullOrEmpty() || parsedArgs.size != 2) {
            sendMessageLogging(chatId, ON_ALIAS_PARSE_FAIL)
            return
        }

        val (aliasName, partyName) = parsedArgs

        if (!validatePartyName(aliasName)) {
            sendMessageLogging(chatId, ON_PARTY_NAME_FAIL)
            return
        }

        sendMessageLogging(
            chatId,
            when (partyDao.createAlias(chatId, aliasName, partyName)) {
                AliasCreationResult.SUCCESS -> onAliasSuccess(aliasName)
                AliasCreationResult.NAME_TAKEN -> ON_CREATE_REQUEST_FAIL
                AliasCreationResult.NO_PARTY -> ON_ALIAS_FAIL
            },
        )
    }
}
