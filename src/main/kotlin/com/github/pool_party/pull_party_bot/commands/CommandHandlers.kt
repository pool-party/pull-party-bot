package com.github.pool_party.pull_party_bot.commands

import com.elbekD.bot.Bot
import com.github.pool_party.pull_party_bot.data_base.*
import org.jetbrains.exposed.exceptions.ExposedSQLException

fun initCommandHandlers(bot: Bot) {

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

        // TODO DataBase work.

        bot.sendMessage(chatId, "Party $partyName successfully created!")
    }

    // Ping the party members.
    bot.onCommand("/party") { msg, name ->
        if (name.isNullOrBlank()) {
            bot.sendMessage(msg.chat.id, ON_PARTY_FAIL)
            return@onCommand
        }

        // TODO DataBase work.

        bot.sendMessage(msg.chat.id, if (res.isNullOrBlank()) ON_PARTY_REQUEST_FAIL else res)
    }

    // Delete existing party, return if absent.
    bot.onCommand("/delete") { msg, name ->
        if (name.isNullOrBlank()) {
            bot.sendMessage(msg.chat.id, ON_DELETE_EMPTY)
            return@onCommand
        }

        // TODO DataBase work.

        bot.sendMessage(msg.chat.id, "Party `$name` is just a history now \uD83D\uDC4D")
    }

    // Show all existing teams.
    bot.onCommand("/list") { msg, _ ->

        val res = "\n" + listCommandTransaction(msg.chat.id)

        bot.sendMessage(msg.chat.id, if (res == "\n") ON_PARTY_REQUEST_LIST_FAIL else "The parties I know: $res")
    }

    // Update existing party
    bot.onCommand("/update") { msg, list ->
        val parsedList = list?.split(' ')?.map { it.trim() }
        val chatId = msg.chat.id

        if (parsedList.isNullOrEmpty() || parsedList.size == 1) {
            bot.sendMessage(chatId, ON_UPDATE_FAIL)
            return@onCommand
        }

        val partyName = parsedList[0]
        val users = parsedList.drop(1)

        bot.sendMessage(
            chatId,
            if (updateCommandTransaction(
                    chatId,
                    partyName,
                    users
                )
            ) "Party `$partyName` successfully updated!" else "$ON_UPDATE_REQUEST_FAIL`$partyName`"
        )
    }

    // bot.onCommand("/stickerAlias") { msg, _ ->   // handle next message from this user ???

    // }

}
