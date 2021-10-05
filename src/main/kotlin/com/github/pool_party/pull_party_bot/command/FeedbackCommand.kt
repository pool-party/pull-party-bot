package com.github.pool_party.pull_party_bot.command

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.Configuration
import com.github.pool_party.pull_party_bot.message.HELP_FEEDBACK
import com.github.pool_party.pull_party_bot.message.ON_FEEDBACK_SUCCESS
import com.github.pool_party.pull_party_bot.message.onFeedback
import com.github.pool_party.telegram_bot_utils.interaction.command.AbstractCommand
import com.github.pool_party.telegram_bot_utils.utils.sendMessageLogging

class FeedbackCommand : AbstractCommand("feedback", "share your ideas and experience with developers", HELP_FEEDBACK) {

    override suspend fun Bot.action(message: Message, args: List<String>) {
        if (args.isEmpty()) return

        val developChatId = Configuration.DEVELOP_CHAT_ID
        if (developChatId == 0L) return

        sendMessageLogging(
            developChatId,
            onFeedback(message.from?.username, message.chat.title) + args.joinToString(" "),
        )

        sendMessageLogging(message.chat.id, ON_FEEDBACK_SUCCESS, replyTo = message.message_id)
    }
}
