package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekd.bot.Bot
import com.elbekd.bot.types.Message
import com.github.pool_party.pull_party_bot.commands.AbstractCommand
import com.github.pool_party.pull_party_bot.commands.messages.HELP_START
import com.github.pool_party.pull_party_bot.commands.messages.INIT_MSG
import com.github.pool_party.pull_party_bot.commands.sendMessageLogging

class StartCommand : AbstractCommand("start", "start the conversation and see welcoming message", HELP_START) {

    override suspend fun Bot.action(message: Message, args: String?) {
        sendMessageLogging(message.chat.id, INIT_MSG)
    }
}
