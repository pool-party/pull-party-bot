package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.commands.AbstractCommand
import com.github.pool_party.pull_party_bot.commands.messages.HELP_MSG
import com.github.pool_party.pull_party_bot.commands.messages.ON_HELP_ERROR

class HelpCommand(private val helpMessages: Map<String, String>) :
    AbstractCommand("help", "show this usage guide", HELP_MSG) {

    override fun Bot.action(message: Message, args: String?) {
        val parsedArgs = parseArgs(args)?.distinct()

        if (parsedArgs.isNullOrEmpty()) {
            sendMessage(message.chat.id, HELP_MSG)
            return
        }

        if (parsedArgs.size > 1) {
            sendMessage(message.chat.id, ON_HELP_ERROR)
            return
        }

        sendMessage(
            message.chat.id,
            helpMessages[parsedArgs[0].removePrefix("/")] ?: ON_HELP_ERROR,
            "Markdown"
        )
    }
}
