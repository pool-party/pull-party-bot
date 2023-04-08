package com.github.poolParty.pullPartyBot.handler.interaction.command

import com.elbekd.bot.Bot
import com.elbekd.bot.types.Message
import com.github.poolParty.pullPartyBot.Configuration
import com.github.poolParty.pullPartyBot.database.dao.ChatDao
import com.github.poolParty.pullPartyBot.handler.Button
import com.github.poolParty.pullPartyBot.handler.deleteMessageLogging
import com.github.poolParty.pullPartyBot.handler.interaction.callback.ClearConfirmationCallback
import com.github.poolParty.pullPartyBot.handler.message.DeleteMessages
import com.github.poolParty.pullPartyBot.handler.message.HelpMessages
import com.github.poolParty.pullPartyBot.handler.sendMessageLogging
import kotlinx.coroutines.delay

class ClearCommand(private val chatDao: ChatDao) :
    AdministratorCommand("clear", "shut down all the parties ever existed", HelpMessages.clear) {

    override suspend fun Bot.administratorAction(message: Message, args: List<String>) {
        val chatId = message.chat.id

        val sentMessage = sendMessageLogging(
            chatId,
            DeleteMessages.clearSuccess,
            buttons = listOf(
                Button("No, I changed my mind", ClearConfirmationCallback(chatId, message.from?.id, false)),
                Button("Yes, I am pretty sure", ClearConfirmationCallback(chatId, message.from?.id, true)),
            ),
        )

        delay(Configuration.STALE_PING_SECONDS * 1_000L)

        deleteMessageLogging(chatId, sentMessage.messageId)
    }
}
