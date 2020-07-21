package com.github.pool_party.pull_party_bot.commands

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.database.createCommandTransaction
import com.github.pool_party.pull_party_bot.database.deleteCommandTransaction
import com.github.pool_party.pull_party_bot.database.listCommandTransaction
import com.github.pool_party.pull_party_bot.database.partyCommandTransaction
import com.github.pool_party.pull_party_bot.database.updateCommandTransaction

fun Bot.initPingCommandHandlers() {

    // Initiate the dialog with bot (might ask to set chat members in the future).
    onCommand("/start") { msg, _ -> suspendStart(msg) }

    // Return the help message.
    onCommand("/help") { msg, _ -> suspendHelp(msg) }

    // Show all existing teams.
    onCommand("/list") { msg, _ -> suspendList(msg) }


    // Ping the party members.
    onCommand("/party", ::suspendParty)

    // Delete existing party, return if absent.
    onCommand("/delete", ::suspendDelete)


    // Create a new party with given members.
    onCommand("/create", ::suspendCreate)

    // Update existing party
    onCommand("/update", ::suspendUpdate)
}


suspend fun Bot.suspendStart(msg: Message) =
    sendMessage(msg.chat.id, INIT_MSG, "Markdown")

suspend fun Bot.suspendHelp(msg: Message) =
    sendMessage(msg.chat.id, HELP_MSG, "Markdown")

suspend fun Bot.suspendList(msg: Message) {
    val res = listCommandTransaction(msg.chat.id)

    sendMessage(
        msg.chat.id,
        if (res.isNotBlank()) ON_LIST_SUCCESS + res else ON_LIST_EMPTY
    )
}


suspend fun Bot.suspendParty(msg: Message, args: String?) {
    val parsedArgs = args?.split(' ')?.map { it.trim().toLowerCase() }
    val chatId = msg.chat.id

    if (parsedArgs.isNullOrEmpty()) {
        sendMessage(chatId, ON_PARTY_EMPTY)
        return
    }

    var hasInvalidRes = false
    for (arg in parsedArgs) {
        val res = partyCommandTransaction(chatId, arg)
        if (res.isNullOrBlank()) {
            hasInvalidRes = true
            continue
        }

        sendMessage(chatId, "$ON_PARTY_SUCCESS $arg:\n\n$res")
    }

    if (hasInvalidRes) {
        sendMessage(
            chatId,
            if (parsedArgs.size == 1) ON_PARTY_REQUEST_FAIL else ON_PARTY_REQUEST_FAILS
        )
    }
}

suspend fun Bot.suspendDelete(msg: Message, args: String?) {
    val parsedArgs = args?.split(' ')?.map { it.trim().toLowerCase() }
    val chatId = msg.chat.id

    if (parsedArgs.isNullOrEmpty()) {
        sendMessage(chatId, ON_DELETE_EMPTY)
        return
    }

    parsedArgs.forEach {
        sendMessage(
            chatId,
            if (deleteCommandTransaction(msg.chat.id, it))
                """Party $it is just a history now 👍"""
            else """Not like I knew the $it party, but now I don't know it at all 👍"""
        )
    }
}


suspend fun Bot.suspendCreate(msg: Message, args: String?) {
    val parsedList = args?.split(' ')?.map { it.trim().toLowerCase() }
    val chatId = msg.chat.id

    if (parsedList.isNullOrEmpty() || parsedList.size < 2) {
        sendMessage(chatId, ON_CREATE_EMPTY)
        return
    }

    val partyName = parsedList[0].removePrefix("@")
    val users = parsedList.drop(1).map { if (!it.startsWith("@")) "@$it" else it }

    sendMessage(
        chatId,
        if (createCommandTransaction(chatId, partyName, users))
            "Party $partyName successfully created!"
        else ON_CREATE_REQUEST_FAIL
    )
}

suspend fun Bot.suspendUpdate(msg: Message, args: String?) {
    val parsedList = args?.split(' ')?.map { it.trim().toLowerCase() }
    val chatId = msg.chat.id

    if (parsedList.isNullOrEmpty() || parsedList.size < 2) {
        sendMessage(chatId, ON_UPDATE_EMPTY)
        return
    }

    val partyName = parsedList[0].removePrefix("@")
    val users = parsedList.drop(1).map { if (!it.startsWith("@")) "@$it" else it }

    sendMessage(
        chatId,
        if (updateCommandTransaction(chatId, partyName, users))
            """Party $partyName became even better now!"""
        else ON_UPDATE_REQUEST_FAIL
    )
}
