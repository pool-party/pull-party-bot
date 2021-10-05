package com.github.pool_party.pull_party_bot.command

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.database.dao.AliasCreationResult
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import com.github.pool_party.pull_party_bot.message.HELP_ALIAS
import com.github.pool_party.pull_party_bot.message.ON_ALIAS_FAIL
import com.github.pool_party.pull_party_bot.message.ON_ALIAS_PARSE_FAIL
import com.github.pool_party.pull_party_bot.message.ON_CREATE_REQUEST_FAIL
import com.github.pool_party.pull_party_bot.message.ON_PARTY_NAME_FAIL
import com.github.pool_party.pull_party_bot.message.onAliasSuccess
import com.github.pool_party.telegram_bot_utils.interaction.command.AbstractCommand
import com.github.pool_party.telegram_bot_utils.utils.sendMessageLogging

class AliasCommand(private val partyDao: PartyDao) :
    AbstractCommand(
        "alias",
        "create a party alias",
        HELP_ALIAS,
        listOf("alias-name", "party-name", "create a new party with the same users"),
    ) {

    override suspend fun Bot.action(message: Message, args: List<String>) {
        val chatId = message.chat.id

        if (args.isEmpty() || args.size != 2) {
            sendMessageLogging(chatId, ON_ALIAS_PARSE_FAIL)
            return
        }

        val (aliasName, partyName) = args

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
