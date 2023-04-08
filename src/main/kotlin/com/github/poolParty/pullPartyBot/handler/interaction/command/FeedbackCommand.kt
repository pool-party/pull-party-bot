package com.github.poolParty.pullPartyBot.handler.interaction.command

import com.elbekd.bot.Bot
import com.elbekd.bot.types.Message
import com.github.poolParty.pullPartyBot.Configuration
import com.github.poolParty.pullPartyBot.handler.message.HelpMessages
import com.github.poolParty.pullPartyBot.handler.message.InformationMessages
import com.github.poolParty.pullPartyBot.handler.sendMessageLogging

class FeedbackCommand :
    AbstractCommand("feedback", "share your ideas and experience with developers", HelpMessages.feedback) {

    override suspend fun Bot.action(message: Message, args: List<String>) {
        val developChatId = Configuration.DEVELOP_CHAT_ID
        if (developChatId == 0L || args.isEmpty()) return

        sendMessageLogging(
            developChatId,
            InformationMessages.feedback(message.from?.username, message.chat.title) + args,
        )

        sendMessageLogging(message.chat.id, InformationMessages.feedbackSuccess, replyTo = message.messageId)
    }
}
