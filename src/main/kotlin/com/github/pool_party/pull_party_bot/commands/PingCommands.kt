package com.github.pool_party.pull_party_bot.commands

import com.elbekD.bot.Bot

fun initializePingCommands(bot: Bot) {

    // Initiate the dialog with bot (might ask to set chat members in the future).
    bot.onCommand("/start") { msg, _ ->
        bot.sendMessage(msg.chat.id, "Hello World!")
    }

    // Return the help message.
    bot.onCommand("/help") { msg, _ ->
        bot.sendMessage(msg.chat.id, HELP_MSG)
    }

    // Create a new party with given members.
    bot.onCommand("/create") { msg, list ->
        val parsedList = list?.split(' ')?.map { it.trim() }
        val chatId = msg.chat.id

        if (parsedList.isNullOrEmpty() || parsedList.size == 1) {
            bot.sendMessage(chatId, ON_CREATE_FAIL)
            return@onCommand
        }

        val partyName = parsedList[0]
        val users = parsedList.drop(1)

        // DataBase work.

        bot.sendMessage(chatId, "Party $partyName successfully created!")
    }

    // Ping the party members.
    bot.onCommand("/party") { msg, name ->
        if (name.isNullOrBlank()) {
            bot.sendMessage(msg.chat.id, ON_PARTY_FAIL)
            return@onCommand
        }

        // DataBase work.

        bot.sendMessage(
            msg.chat.id,
            if (true) { // success condition of DataBase.
                "SUCCESS_RESPONSE" // response on success.
            } else {
                ON_PARTY_REQUEST_FAIL
            }
        )
    }

    // Delete existing party.
    bot.onCommand("/delete") { msg, name ->

        // DataBase work.

        bot.sendMessage(msg.chat.id, "Party $name is just a history now \uD83D\uDC4D")
    }
}
