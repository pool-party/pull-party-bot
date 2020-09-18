package com.github.pool_party.pull_party_bot.command.handler

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.command.ON_ADMINS_PARTY_FAIL
import com.github.pool_party.pull_party_bot.command.ON_CHANGE_EMPTY
import com.github.pool_party.pull_party_bot.command.ON_CHANGE_REQUEST_FAIL
import com.github.pool_party.pull_party_bot.command.ON_CLEAR_SUCCESS
import com.github.pool_party.pull_party_bot.command.ON_CREATE_EMPTY
import com.github.pool_party.pull_party_bot.command.ON_CREATE_REQUEST_FAIL
import com.github.pool_party.pull_party_bot.command.ON_DELETE_EMPTY
import com.github.pool_party.pull_party_bot.command.ON_LIST_EMPTY
import com.github.pool_party.pull_party_bot.command.ON_LIST_SUCCESS
import com.github.pool_party.pull_party_bot.command.ON_PARTY_EMPTY
import com.github.pool_party.pull_party_bot.command.ON_PARTY_NAME_FAIL
import com.github.pool_party.pull_party_bot.command.ON_PARTY_REQUEST_FAIL
import com.github.pool_party.pull_party_bot.command.ON_PARTY_REQUEST_FAILS
import com.github.pool_party.pull_party_bot.command.ON_RUDE_FAIL
import com.github.pool_party.pull_party_bot.command.ON_SINGLETON_PARTY
import com.github.pool_party.pull_party_bot.command.ON_USERS_FAIL
import com.github.pool_party.pull_party_bot.command.PROHIBITED_SYMBOLS
import com.github.pool_party.pull_party_bot.command.modifyCommandAssertion
import com.github.pool_party.pull_party_bot.command.onAdministratorCommand
import com.github.pool_party.pull_party_bot.command.onNoArgumentsCommand
import com.github.pool_party.pull_party_bot.command.parseArgs
import com.github.pool_party.pull_party_bot.command.sendCaseMessage
import com.github.pool_party.pull_party_bot.database.transaction.changeCommandTransaction
import com.github.pool_party.pull_party_bot.database.transaction.clearCommandTransaction
import com.github.pool_party.pull_party_bot.database.transaction.createCommandTransaction
import com.github.pool_party.pull_party_bot.database.transaction.deleteCommandTransaction
import com.github.pool_party.pull_party_bot.database.transaction.listCommandTransaction
import com.github.pool_party.pull_party_bot.database.transaction.partyCommandTransaction
import com.github.pool_party.pull_party_bot.database.transaction.rudeCommandTransaction

fun Bot.initPingCommandHandlers() {
    onNoArgumentsCommand("/list", ::handleList)

    onAdministratorCommand("/delete", ::handleDelete)
    onAdministratorCommand("/clear") { msg, _ -> handleClear(msg) }

    onCommand("/party", ::handleExplicitParty)
    onCommand("/create", ::handleCreate)
    onCommand("/change", ::handleChange)

    onCommand("/rude", ::handleRude)

    onMessage(::handleImplicitParty)
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
fun Bot.handleExplicitParty(msg: Message, args: String?) {
    val parsedArgs = parseArgs(args)
    val chatId = msg.chat.id

    if (parsedArgs.isNullOrEmpty()) {
        sendMessage(chatId, ON_PARTY_EMPTY, "Markdown")
        return
    }

    handleParty(parsedArgs.asSequence(), msg) {
        sendMessage(
            chatId,
            if (parsedArgs.size == 1) ON_PARTY_REQUEST_FAIL
            else ON_PARTY_REQUEST_FAILS,
            "Markdown"
        )
    }
}

/**
 * Handle implicit `@party-name`-like calls
 */
suspend fun Bot.handleImplicitParty(msg: Message) {
    val text = msg.text ?: msg.caption

    if (msg.forward_from != null || text == null) {
        return
    }

    val prohibitedSymbolsString = PROHIBITED_SYMBOLS.joinToString("")
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
fun Bot.handleCreate(msg: Message, args: String?) = handlePartyChangeRequest(true, msg, args)

/**
 * Change an existing party.
 */
fun Bot.handleChange(msg: Message, args: String?) = handlePartyChangeRequest(false, msg, args)

/**
 * Handle both `change` and `create` commands.
 */
private fun Bot.handlePartyChangeRequest(isNew: Boolean, msg: Message, args: String?) {
    val parsedList = args?.split(' ')?.map { it.trim().toLowerCase() }
    val chatId = msg.chat.id

    if (parsedList.isNullOrEmpty() || parsedList.size < 2) {
        sendMessage(chatId, if (isNew) ON_CREATE_EMPTY else ON_CHANGE_EMPTY, "Markdown")
        return
    }

    val partyName = parsedList[0].removePrefix("@")

    val regex = Regex("(.*[@${PROHIBITED_SYMBOLS.joinToString("")}].*)|(.*-)")
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

    if (users.singleOrNull()?.removePrefix("@") == partyName) {
        sendMessage(chatId, ON_SINGLETON_PARTY, "Markdown")
        return
    }

    if (users.size < parsedList.drop(1).distinct().size) {
        sendMessage(chatId, ON_USERS_FAIL, "Markdown")
        if (users.isEmpty()) {
            sendMessage(chatId, if (isNew) ON_CREATE_EMPTY else ON_CHANGE_EMPTY, "Markdown")
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
            else ON_CHANGE_REQUEST_FAIL,
            "Markdown"
        )
    }
}

/**
 * Switch RUDE mode on and off.
 */
fun Bot.handleRude(msg: Message, args: String?) {
    val parsedArg = args?.trim()?.toLowerCase()
    val chatId = msg.chat.id

    val res = when (parsedArg) {
        "on" -> rudeCommandTransaction(chatId, true)
        "off" -> rudeCommandTransaction(chatId, false)
        else -> {
            sendMessage(chatId, ON_RUDE_FAIL, "Markdown")
            return
        }
    }

    val curStatus = if (parsedArg == "on") """😈""" else """😇"""
    sendCaseMessage(chatId, """Rude mode ${if (res) "is now" else "was already"} $parsedArg $curStatus!""")
}
