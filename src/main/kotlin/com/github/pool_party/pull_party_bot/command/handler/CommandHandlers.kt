package com.github.pool_party.pull_party_bot.command.handler

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.command.HELP_CHANGE
import com.github.pool_party.pull_party_bot.command.HELP_CLEAR
import com.github.pool_party.pull_party_bot.command.HELP_CREATE
import com.github.pool_party.pull_party_bot.command.HELP_DELETE
import com.github.pool_party.pull_party_bot.command.HELP_LIST
import com.github.pool_party.pull_party_bot.command.HELP_MSG
import com.github.pool_party.pull_party_bot.command.HELP_PARTY
import com.github.pool_party.pull_party_bot.command.HELP_RUDE
import com.github.pool_party.pull_party_bot.command.HELP_START
import com.github.pool_party.pull_party_bot.command.INIT_MSG
import com.github.pool_party.pull_party_bot.command.ON_HELP_ERROR
import com.github.pool_party.pull_party_bot.command.onNoArgumentsCommand
import com.github.pool_party.pull_party_bot.command.parseArgs

fun Bot.initCommandHandlers() {
    initPingCommandHandlers()
    initAliasCommandHandlers()

    onNoArgumentsCommand("/start", ::handleStart)
    onCommand("/help", ::handleHelp)
}

/**
 * Initiate the dialog with bot.
 */
fun Bot.handleStart(msg: Message) {
    sendMessage(msg.chat.id, INIT_MSG)
}

/**
 * Return the help message.
 */
fun Bot.handleHelp(msg: Message, args: String?) {
    val parsedArgs = parseArgs(args)

    if (parsedArgs.isNullOrEmpty()) {
        sendMessage(msg.chat.id, HELP_MSG)
        return
    }

    if (parsedArgs.size > 1) {
        sendMessage(msg.chat.id, ON_HELP_ERROR)
        return
    }

    sendMessage(
        msg.chat.id,
        when (parsedArgs[0].removePrefix("/")) {
            "start" -> HELP_START
            "list" -> HELP_LIST
            "party" -> HELP_PARTY
            "delete" -> HELP_DELETE
            "clear" -> HELP_CLEAR
            "create" -> HELP_CREATE
            "change" -> HELP_CHANGE
            "rude" -> HELP_RUDE
            else -> ON_HELP_ERROR
        },
        "Markdown"
    )
}
