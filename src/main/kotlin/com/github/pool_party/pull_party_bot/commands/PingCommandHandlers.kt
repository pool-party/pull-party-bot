package com.github.pool_party.pull_party_bot.commands

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.database.changeCommandTransaction
import com.github.pool_party.pull_party_bot.database.clearCommandTransaction
import com.github.pool_party.pull_party_bot.database.createCommandTransaction
import com.github.pool_party.pull_party_bot.database.deleteCommandTransaction
import com.github.pool_party.pull_party_bot.database.listCommandTransaction
import com.github.pool_party.pull_party_bot.database.partyCommandTransaction
import com.github.pool_party.pull_party_bot.database.rudeCommandTransaction

fun Bot.initPingCommandHandlers() {
    onNoArgumentsCommand("/start", ::handleStart)
    onNoArgumentsCommand("/list", ::handleList)

    onCommand("/help", ::handleHelp)
    onCommand("/party", ::handleExplicitParty)
    onAdministratorCommand("/delete", ::handleDelete)
    onAdministratorCommand("/clear") { msg, _ -> handleClear(msg) }

    onCommand("/create", ::handleCreate)
    onCommand("/change", ::handleChange)

    onCommand("/rude", ::handleRude)

    onMessage(::handleImplicitParty)
}

val prohibitedSymbols = listOf('!', ',', '.', '?', ':', ';', '(', ')')

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

/**
 * Show all existing teams.
 */
fun Bot.handleList(msg: Message) {
    val chatId = msg.chat.id
    val res = listCommandTransaction(chatId)

    sendCaseMessage(
        chatId,
        if (res.isNotBlank()) ON_LIST_SUCCESS + res else ON_LIST_EMPTY
    )
}

/**
 * Ping the members of given parties.
 */
suspend fun Bot.handleExplicitParty(msg: Message, args: String?) {
    val parsedArgs = parseArgs(args)
    val chatId = msg.chat.id

    if (parsedArgs.isNullOrEmpty()) {
        sendMessage(chatId, ON_PARTY_EMPTY)
        return
    }

    handleParty(parsedArgs.asSequence(), msg) {
        sendMessage(
            chatId,
            if (parsedArgs.size == 1) ON_PARTY_REQUEST_FAIL
            else ON_PARTY_REQUEST_FAILS
        )
    }
}

/**
 * Handle implicit `@party-name`-like calls
 */
suspend fun Bot.handleImplicitParty(msg: Message) {
    val text = msg.text

    if (msg.forward_from != null || text == null) {
        return
    }

    val prohibitedSymbolsString = prohibitedSymbols.joinToString("")
    val regex = Regex("(?<party>[^$prohibitedSymbolsString]*)[$prohibitedSymbolsString]*")

    val partyNames = text.lineSequence()
        .flatMap { it.split(' ', '\t').asSequence() }
        .filter { it.startsWith('@') }
        .map { it.removePrefix("@") }
        .mapNotNull { regex.matchEntire(it)?.groups?.get(1)?.value }

    handleParty(partyNames, msg)
}

private fun Bot.handleParty(partyNames: Sequence<String>, msg: Message, onFailure: () -> Unit = {}) {
    val chatId = msg.chat.id
    var failure = false

    val res = partyNames
        .map { it.toLowerCase() }
        .distinct()
        .mapNotNull {
            if (it == "admins") {
                handleAdminsParty(msg)
            } else {
                val users = partyCommandTransaction(chatId, it)
                if (users.isNullOrBlank()) {
                    failure = true
                }
                users
            }
        }
        .flatMap { it.split(" ").asSequence() }
        .distinct()
        .joinToString(" ")

    sendCaseMessage(chatId, res, replyTo = msg.message_id)

    if (failure) {
        onFailure()
    }
}

private fun Bot.handleAdminsParty(msg: Message): String? {
    val chatId = msg.chat.id
    val chatType = msg.chat.type

    if (chatType != "group" && chatType != "supergroup") {
        sendMessage(chatId, ON_ADMINS_PARTY_FAIL)
        return null
    }

    return getChatAdministrators(chatId)
        .join()
        .asSequence()
        .map { "@" + it.user.username }
        .joinToString(" ")
}

/**
 * Delete given parties from DataBase.
 */
fun Bot.handleDelete(msg: Message, args: String?) {
    val parsedArgs = parseArgs(args)
    val chatId = msg.chat.id

    if (parsedArgs.isNullOrEmpty()) {
        sendMessage(chatId, ON_DELETE_EMPTY)
        return
    }

    parsedArgs.forEach {
        if (modifyCommandAssertion(chatId, it)) {
            sendCaseMessage(
                chatId,
                if (deleteCommandTransaction(chatId, it))
                    """Party $it is just a history now 👍"""
                else """Not like I knew the $it party, but now I don't know it at all 👍"""
            )
        }
    }
}

/**
 * Delete all the parties of the chat.
 */
fun Bot.handleClear(msg: Message) {
    val chatId = msg.chat.id
    clearCommandTransaction(chatId)
    sendMessage(chatId, ON_CLEAR_SUCCESS)
}

/**
 * Create a new party with given members.
 */
suspend fun Bot.handleCreate(msg: Message, args: String?) = handlePartyChangeRequest(true, msg, args)

/**
 * Change an existing party.
 */
suspend fun Bot.handleChange(msg: Message, args: String?) = handlePartyChangeRequest(false, msg, args)

/**
 * Handle both `change` and `create` commands.
 */
private fun Bot.handlePartyChangeRequest(isNew: Boolean, msg: Message, args: String?) {
    val parsedList = args?.split(' ')?.map { it.trim().toLowerCase() }
    val chatId = msg.chat.id

    if (parsedList.isNullOrEmpty() || parsedList.size < 2) {
        sendMessage(chatId, if (isNew) ON_CREATE_EMPTY else ON_CHANGE_EMPTY)
        return
    }

    val partyName = parsedList[0].removePrefix("@")

    val regex = Regex("(.*[@${prohibitedSymbols.joinToString("")}].*)|(.*\\-)")
    if (partyName.matches(regex)) {
        sendMessage(chatId, ON_PARTY_NAME_FAIL, "Markdown")
        return
    }

    if (!modifyCommandAssertion(chatId, partyName)) {
        return
    }

    val users = parsedList.asSequence().drop(1)
        .map { it.replace("@", "") }.distinct()
        .filter { it.matches("([a-z0-9_]{5,32})".toRegex()) }
        .map { "@$it" }.toList()

    if (users.size < parsedList.drop(1).distinct().size) {
        sendMessage(chatId, ON_USERS_FAIL)
        if (users.isEmpty()) {
            sendMessage(chatId, if (isNew) ON_CREATE_EMPTY else ON_CHANGE_EMPTY)
            return
        }
    }

    if (if (isNew) createCommandTransaction(chatId, partyName, users)
        else changeCommandTransaction(chatId, partyName, users)
    ) {
        sendCaseMessage(
            chatId,
            if (isNew) "Party $partyName successfully created!"
            else """Party $partyName became even better now!"""
        )
    } else {
        sendMessage(
            chatId,
            if (isNew) ON_CREATE_REQUEST_FAIL
            else ON_CHANGE_REQUEST_FAIL
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
    sendCaseMessage(chatId, """Rude mode ${if (res) "is now" else "was already"} $parsedArg $curStatus!""")
}
