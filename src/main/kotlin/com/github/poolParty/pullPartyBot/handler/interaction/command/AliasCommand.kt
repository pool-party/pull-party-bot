package com.github.poolParty.pullPartyBot.handler.interaction.command

import com.elbekd.bot.Bot
import com.elbekd.bot.types.Message
import com.github.poolParty.pullPartyBot.database.dao.AliasCreationResult
import com.github.poolParty.pullPartyBot.database.dao.PartyDao
import com.github.poolParty.pullPartyBot.handler.message.AliasMessages
import com.github.poolParty.pullPartyBot.handler.message.ChangePartyMessages
import com.github.poolParty.pullPartyBot.handler.message.HelpMessages
import com.github.poolParty.pullPartyBot.handler.sendMessageLogging

class AliasCommand(private val partyDao: PartyDao) :
    AbstractCommand("alias", "create a new party with the same users", HelpMessages.alias) {

    override suspend fun Bot.action(message: Message, args: List<String>) {
        val chatId = message.chat.id

        if (args.size != 2) {
            sendMessageLogging(chatId, AliasMessages.parseFail)
            return
        }

        val (aliasName, partyName) = args

        if (!validatePartyName(aliasName)) {
            sendMessageLogging(chatId, ChangePartyMessages.partyNameFail)
            return
        }

        sendMessageLogging(
            chatId,
            when (partyDao.createAlias(chatId, aliasName, partyName)) {
                AliasCreationResult.SUCCESS -> AliasMessages.success(aliasName)
                AliasCreationResult.NAME_TAKEN -> ChangePartyMessages.createRequestFail
                AliasCreationResult.NO_PARTY -> AliasMessages.fail
            },
        )
    }
}
