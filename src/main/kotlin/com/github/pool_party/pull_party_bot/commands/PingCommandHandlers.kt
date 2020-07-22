package com.github.pool_party.pull_party_bot.commands

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.database.createCommandTransaction
import com.github.pool_party.pull_party_bot.database.deleteCommandTransaction
import com.github.pool_party.pull_party_bot.database.listCommandTransaction
import com.github.pool_party.pull_party_bot.database.partyCommandTransaction
import com.github.pool_party.pull_party_bot.database.rudeCheckTransaction
import com.github.pool_party.pull_party_bot.database.rudeCommandTransaction
import com.github.pool_party.pull_party_bot.database.updateCommandTransaction

fun Bot.initPingCommandHandlers() {
    onCommand("/start") { msg, _ -> handleStart(msg) }
    onCommand("/help") { msg, _ -> handleHelp(msg) }
    onCommand("/list") { msg, _ -> handleList(msg) }

    onCommand("/party", ::handleParty)
    onCommand("/delete", ::handleDelete)

    onCommand("/create", ::handleCreate)
    onCommand("/update", ::handleUpdate)

    onCommand("/rude", ::handleRude)
}

/**
 * Initiate the dialog with bot.
 */
fun Bot.handleStart(msg: Message) =
    sendMessage(msg.chat.id, INIT_MSG, "Markdown")

/**
 * Return the help message.
 */
fun Bot.handleHelp(msg: Message) =
    sendMessage(msg.chat.id, HELP_MSG, "Markdown")

/**
 * Show all existing teams.
 */
fun Bot.handleList(msg: Message) {
    val res = listCommandTransaction(msg.chat.id)

    sendCaseMessage(
        msg.chat.id,
        if (res.isNotBlank()) ON_LIST_SUCCESS + res else ON_LIST_EMPTY
    )
}

/**
 * Ping the members of given parties.
 */
suspend fun Bot.handleParty(msg: Message, args: String?) {
    val parsedArgs = parseArgs(args)
    val chatId = msg.chat.id

    if (parsedArgs.isNullOrEmpty()) {
        sendMessage(chatId, ON_PARTY_EMPTY)
        return
    }

    var hasInvalidRes = false
    parsedArgs.forEach {
        val res = partyCommandTransaction(chatId, it)
        if (res.isNullOrBlank()) {
            hasInvalidRes = true
            return@forEach
        }

        sendCaseMessage(chatId, """
            $ON_PARTY_SUCCESS $it:

            $res""".trimIndent())
    }

    if (hasInvalidRes) {
        sendMessage(
            chatId,
            if (parsedArgs.size == 1) ON_PARTY_REQUEST_FAIL
            else ON_PARTY_REQUEST_FAILS
        )
    }
}

/**
 * Delete given parties from DataBase.
 */
suspend fun Bot.handleDelete(msg: Message, args: String?) {
    val parsedArgs = parseArgs(args)
    val chatId = msg.chat.id

    if (parsedArgs.isNullOrEmpty()) {
        sendMessage(chatId, ON_DELETE_EMPTY)
        return
    }

    parsedArgs.forEach {
        sendCaseMessage(
            chatId,
            if (deleteCommandTransaction(msg.chat.id, it))
                """Party $it is just a history now 👍"""
            else """Not like I knew the $it party, but now I don't know it at all 👍"""
        )
    }
}

/**
 * Create a new party with given members.
 */
suspend fun Bot.handleCreate(msg: Message, args: String?) =
    handlePartyPostRequest(true, msg, args)

/**
 * Update existing party.
 */
suspend fun Bot.handleUpdate(msg: Message, args: String?) =
    handlePartyPostRequest(false, msg, args)

/**
 * Handle both `update` and `create` commands.
 */
fun Bot.handlePartyPostRequest(isNew: Boolean, msg: Message, args: String?) {
    val parsedList = args?.split(' ')?.map { it.trim().toLowerCase() }
    val chatId = msg.chat.id

    if (parsedList.isNullOrEmpty() || parsedList.size < 2) {
        sendMessage(chatId, if (isNew) ON_CREATE_EMPTY else ON_UPDATE_EMPTY)
        return
    }

    val partyName = parsedList[0].removePrefix("@")
    val users = parsedList.drop(1)
        .map { if (!it.startsWith("@")) "@$it" else it }.distinct()

    if (if (isNew) createCommandTransaction(chatId, partyName, users)
        else updateCommandTransaction(chatId, partyName, users)) {
        sendCaseMessage(
            chatId,
            if (isNew) "Party $partyName successfully created!"
            else """Party $partyName became even better now!"""
        )
    } else {
        sendMessage(
            chatId,
            if (isNew) ON_CREATE_REQUEST_FAIL
            else ON_UPDATE_REQUEST_FAIL
        )
    }
}

/**
 * Switch RUDE mode on and off.
 */
suspend fun Bot.handleRude(msg: Message, args: String?) {
    val parsedArg = args?.trim()?.toLowerCase()
    val chatId = msg.chat.id

    val res = when (parsedArg) {
        "on" -> rudeCommandTransaction(chatId, true)
        "off" -> rudeCommandTransaction(chatId, false)
        else -> {
            sendMessage(chatId, ON_RUDE_FAIL)
            return
        }
    }

    val curStatus = if (parsedArg == "on") """😈""" else """😇"""
    sendCaseMessage(
        chatId,
        if (res) """Rude mode is now $parsedArg $curStatus!"""
        else """Rude mode was already $parsedArg $curStatus!"""
    )
}

// TODO create utils package with these functions
fun parseArgs(args: String?): List<String>? =
    args?.split(' ')?.map { it.trim().toLowerCase() }?.distinct()

fun Bot.sendCaseMessage(chatId: Long, msg: String) =
    sendCaseMessage(chatId, msg, null)

fun Bot.sendCaseMessage(chatId: Long, msg: String, parseMode: String?) =
    sendMessage(
        chatId,
        if (rudeCheckTransaction(chatId)) msg.toUpperCase() else msg,
        parseMode
    )
