package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.Configuration
import com.github.pool_party.pull_party_bot.commands.AbstractCommand
import com.github.pool_party.pull_party_bot.commands.messages.HELP_FEEDBACK
import com.github.pool_party.pull_party_bot.commands.messages.ON_FEEDBACK_SUCCESS
import com.github.pool_party.pull_party_bot.commands.messages.onFeedback

class FeedbackCommand : AbstractCommand("feedback", "share your ideas and experience with developers", HELP_FEEDBACK) {

    override suspend fun Bot.action(message: Message, args: String?) {
        val parsedArgs = args?.trim()
        val developChatId = Configuration.DEVELOP_CHAT_ID
        if (developChatId == 0L || parsedArgs.isNullOrBlank()) return

        sendMessage(
            developChatId,
            onFeedback(message.from?.username, message.chat.title) + parsedArgs
        )

        sendMessage(message.chat.id, ON_FEEDBACK_SUCCESS, replyTo = message.message_id)
    }
}
