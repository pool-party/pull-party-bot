package com.github.pool_party.pull_party_bot.commands

import com.elbekD.bot.Bot
import com.github.pool_party.pull_party_bot.database.*

fun Bot.initPingCommandHandlers() {

    // Initiate the dialog with bot (might ask to set chat members in the future).
    onCommand("/start") { msg, _ ->
        sendMessage(msg.chat.id, INIT_MSG, "Markdown")
    }

    // Return the help message.
    onCommand("/help") { msg, _ ->
        sendMessage(msg.chat.id, HELP_MSG, "Markdown")
    }

    // Create a new party with given members.
    onCommand("/create") { msg, list ->
        val parsedList = list?.split(' ')?.map { it.trim() }
        val chatId = msg.chat.id

        if (parsedList.isNullOrEmpty() || parsedList.size == 1) {
            sendMessage(chatId, ON_CREATE_FAIL)
            return@onCommand
        }

        val partyName = parsedList[0]
        val users = parsedList.drop(1)

        sendMessage(
            chatId,
            if (createCommandTransaction(chatId, partyName, users)) "Party $partyName successfully created!"
            else ON_CREATE_REQUEST_FAIL
        )
    }

    // Ping the party members.
    onCommand("/party") { msg, name ->
        if (name.isNullOrBlank()) {
            sendMessage(msg.chat.id, ON_PARTY_FAIL)
            return@onCommand
        }

        val res = partyCommandTransaction(msg.chat.id, name)

        sendMessage(msg.chat.id, if (res.isNullOrBlank()) ON_PARTY_REQUEST_FAIL else res)
    }

    // Delete existing party, return if absent.
    onCommand("/delete") { msg, name ->
        if (name.isNullOrBlank()) {
            sendMessage(msg.chat.id, ON_DELETE_EMPTY)
            return@onCommand
        }

        sendMessage(
            msg.chat.id,
            if (deleteCommandTransaction(msg.chat.id, name)) """Party $name is just a history now 👍"""
            else """Not like I knew this party, but now I don't know it at all 👍"""
        )
    }

    // Show all existing teams.
    onCommand("/list") { msg, _ ->
        val res = listCommandTransaction(msg.chat.id)

        sendMessage(
            msg.chat.id,
            if (res.isEmpty()) """I don't know any parties in this chat yet 😢"""
            else "The parties I know:\n$res"
        )
    }

    // Update existing party
    onCommand("/update") { msg, list ->
        val parsedList = list?.split(' ')?.map { it.trim() }
        val chatId = msg.chat.id

        if (parsedList.isNullOrEmpty() || parsedList.size == 1) {
            sendMessage(chatId, ON_UPDATE_FAIL)
            return@onCommand
        }

        val partyName = parsedList[0]
        val users = parsedList.drop(1)

        sendMessage(
            chatId,
            if (updateCommandTransaction(
                    chatId,
                    partyName,
                    users
                )
            ) "Party $partyName became even better now!" else ON_UPDATE_REQUEST_FAIL
        )
    }
}
