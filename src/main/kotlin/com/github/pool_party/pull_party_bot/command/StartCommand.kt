package com.github.pool_party.pull_party_bot.command

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.message.HELP_START
import com.github.pool_party.pull_party_bot.message.INIT_MSG
import com.github.pool_party.telegram_bot_utils.interaction.command.AbstractCommand
import com.github.pool_party.telegram_bot_utils.utils.sendMessageLogging

class StartCommand : AbstractCommand("start", "awake the bot", HELP_START) {

    override suspend fun Bot.action(message: Message, args: List<String>) {
        sendMessageLogging(message.chat.id, INIT_MSG)
    }
}
