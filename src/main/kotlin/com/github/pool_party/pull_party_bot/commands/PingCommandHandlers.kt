package com.github.pool_party.pull_party_bot.commands

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.database.Party
import com.github.pool_party.pull_party_bot.database.changeCommandTransaction
import com.github.pool_party.pull_party_bot.database.clearCommandTransaction
import com.github.pool_party.pull_party_bot.database.createCommandTransaction
import com.github.pool_party.pull_party_bot.database.deleteCommandTransaction
import com.github.pool_party.pull_party_bot.database.listCommandTransaction
import com.github.pool_party.pull_party_bot.database.partyCommandTransaction
import com.github.pool_party.pull_party_bot.database.rudeCommandTransaction

fun Bot.initPingCommandHandlers() {
    onMessage(::handleImplicitParty)
    registerCommands()
}

val start = newNoArgumentCommand("start", "awake the bot", HELP_START) { msg ->
    sendMessage(msg.chat.id, INIT_MSG)
}

val help = newCommand("help", "show this usage guide", HELP_MSG) { msg, args ->
    val parsedArgs = parseArgs(args)?.distinct()

    if (parsedArgs.isNullOrEmpty()) {
        sendMessage(msg.chat.id, HELP_MSG)
        return@newCommand
    }

    if (parsedArgs.size > 1) {
        sendMessage(msg.chat.id, ON_HELP_ERROR)
        return@newCommand
    }

    sendMessage(
        msg.chat.id,
        Command.helpMessages[parsedArgs[0].removePrefix("/")] ?: ON_HELP_ERROR,
        "Markdown"
    )
}

val list = newCommand("list", "show the parties of the chat", HELP_LIST) { msg, args ->
    fun Party.format() = "$name: ${users.replace("@", "")}"

    val parsedArgs = parseArgs(args)?.distinct()
    val chatId = msg.chat.id
    val list = listCommandTransaction(chatId)

    if (parsedArgs.isNullOrEmpty()) {
        val partyList = list.asSequence()
            .map { it.format() }
            .joinToString("\n")

        sendCaseMessage(
            chatId,
            if (partyList.isNotBlank()) ON_LIST_SUCCESS + partyList else ON_LIST_EMPTY
        )
        return@newCommand
    }

    val partyMap = list.associateBy { it.name }
    val requestedParties = parsedArgs.asSequence()
        .flatMap { arg ->
            val party = partyMap[arg]
            if (party != null) {
                sequenceOf(party)
            } else {
                partyMap.values.asSequence().filter { arg in it.users }
            }
        }
        .distinct()
        .map { it.format() }
        .joinToString("\n")

    sendCaseMessage(
        chatId,
        if (requestedParties.isNotBlank()) ON_ARGUMENT_LIST_SUCCESS + requestedParties else ON_ARGUMENT_LIST_EMPTY
    )
}

val party = newCommand("party", "tag the members of existing parties", HELP_PARTY) { msg, args ->
    val parsedArgs = parseArgs(args)?.distinct()
    val chatId = msg.chat.id

    if (parsedArgs.isNullOrEmpty()) {
        sendMessage(chatId, ON_PARTY_EMPTY, "Markdown")
        return@newCommand
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
        .mapNotNull { it.user.username }
        .filter { it.substring(it.length - 3).toLowerCase() != "bot" }
        .map { "@$it" }
        .joinToString(" ")
}

val delete = newAdministratorCommand(
    "delete",
    "forget the parties as they have never happened",
    HELP_DELETE
) { msg, args ->
    val parsedArgs = parseArgs(args)?.distinct()
    val chatId = msg.chat.id

    if (parsedArgs.isNullOrEmpty()) {
        sendMessage(chatId, ON_DELETE_EMPTY)
        return@newAdministratorCommand
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

val clear = newAdministratorCommand("clear", "shut down all the parties ever existed", HELP_CLEAR) { msg, _ ->
    val chatId = msg.chat.id
    clearCommandTransaction(chatId)
    sendMessage(chatId, ON_CLEAR_SUCCESS)
}

val create = newCommand("create", "create new party", HELP_CREATE) { msg, args ->
    handlePartyChangeRequest(true, msg, args)
}

val change = newCommand("change", "change an existing party", HELP_CHANGE) { msg, args ->
    handlePartyChangeRequest(false, msg, args)
}

val add = newCommand("add", "add a member to an existing party", HELP_ADD) { msg, args ->
    handlePartyChangeMembersRequest(msg, args, List<String>::plus)
}

val remove = newCommand("remove", "remove a member to an existing party", HELP_REMOVE) { msg, args ->
    handlePartyChangeMembersRequest(msg, args) { newArgs, party ->
        val partyName = newArgs[0]
        val argSet = newArgs.subList(1, newArgs.size).asSequence().map { it.removePrefix("@") }.toSet()
        listOf(partyName) + party.asSequence().map { it.removePrefix("@") }.filter { !argSet.contains(it) }
    }
}

private fun Bot.handlePartyChangeMembersRequest(
    msg: Message,
    args: String?,
    concat: (List<String>, List<String>) -> List<String>
) {
    val chatId = msg.chat.id
    val parsedArgs = parseArgs(args)?.distinct()

    if (parsedArgs.isNullOrEmpty()) {
        sendCaseMessage(chatId, ON_CHANGE_EMPTY, parseMode = "Markdown")
        return
    }

    val partyList = partyCommandTransaction(chatId, parsedArgs[0])
    if (partyList == null) {
        sendCaseMessage(chatId, ON_CHANGE_REQUEST_FAIL, parseMode = "Markdown")
        return
    }

    val newArgs = concat(parsedArgs, partyList.split(" "))
        .asSequence()
        .distinct()
        .joinToString(" ")

    handlePartyChangeRequest(false, msg, newArgs)
}

/**
 * Handle both `change` and `create` commands.
 */
private fun Bot.handlePartyChangeRequest(isNew: Boolean, msg: Message, args: String?) {
    val parsedList = parseArgs(args)
    val chatId = msg.chat.id

    if (parsedList.isNullOrEmpty() || parsedList.size < 2) {
        sendMessage(chatId, if (isNew) ON_CREATE_EMPTY else ON_CHANGE_EMPTY, "Markdown")
        return
    }

    val partyName = parsedList[0].removePrefix("@")

    val regex = Regex("(.*[@${PROHIBITED_SYMBOLS.joinToString("")}].*)|(.*\\-)")
    if (partyName.length > 50 || partyName.matches(regex)) {
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

val rude = newCommand("rude", "switch RUDE(CAPS LOCK) mode", HELP_RUDE) { msg, args ->
    val parsedArg = parseArgs(args)?.singleOrNull()
    val chatId = msg.chat.id

    val res = when (parsedArg) {
        "on" -> rudeCommandTransaction(chatId, true)
        "off" -> rudeCommandTransaction(chatId, false)
        else -> {
            sendMessage(chatId, ON_RUDE_FAIL, "Markdown")
            return@newCommand
        }
    }

    val curStatus = if (parsedArg == "on") """😈""" else """😇"""
    sendCaseMessage(chatId, """Rude mode ${if (res) "is now" else "was already"} $parsedArg $curStatus!""")
}
