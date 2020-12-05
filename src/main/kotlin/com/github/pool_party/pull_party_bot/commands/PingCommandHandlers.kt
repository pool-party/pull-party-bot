package com.github.pool_party.pull_party_bot.commands

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.database.Party
import com.github.pool_party.pull_party_bot.database.addUsersCommandTransaction
import com.github.pool_party.pull_party_bot.database.changeCommandTransaction
import com.github.pool_party.pull_party_bot.database.clearCommandTransaction
import com.github.pool_party.pull_party_bot.database.createCommandTransaction
import com.github.pool_party.pull_party_bot.database.deleteCommandTransaction
import com.github.pool_party.pull_party_bot.database.listCommandTransaction
import com.github.pool_party.pull_party_bot.database.partyCommandTransaction
import com.github.pool_party.pull_party_bot.database.removeUsersCommandTransaction
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

        if (partyList.isNotBlank()) {
            sendCaseMessage(chatId, ON_LIST_SUCCESS + partyList)
        } else {
            sendMessage(chatId, ON_LIST_EMPTY, "Markdown")
        }

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

    if (requestedParties.isNotBlank()) {
        sendCaseMessage(chatId, ON_ARGUMENT_LIST_SUCCESS + requestedParties)
        return@newCommand
    }

    sendMessage(chatId, ON_ARGUMENT_LIST_EMPTY, "Markdown")
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
        sendMessage(chatId, ON_ADMINS_PARTY_FAIL, "Markdown")
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
        sendMessage(chatId, ON_DELETE_EMPTY, "Markdown")
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
    sendMessage(chatId, ON_CLEAR_SUCCESS, "Markdown")
}

val create = newCommand("create", "create new party", HELP_CREATE) { msg, args ->
    handlePartyChangeRequest(msg, args, PartyChangeStatus.CREATE, ::createCommandTransaction)
}

val change = newCommand("change", "change an existing party", HELP_CHANGE) { msg, args ->
    handlePartyChangeRequest(msg, args, PartyChangeStatus.CHANGE, ::changeCommandTransaction)
}

val add = newCommand("add", "add new users to the given party", HELP_ADD) { msg, args ->
    handlePartyChangeRequest(msg, args, PartyChangeStatus.ADD, ::addUsersCommandTransaction)
}

val remove = newCommand("remove", "remove given users from the provided party", HELP_REMOVE) { msg, args ->
    handlePartyChangeRequest(msg, args, PartyChangeStatus.REMOVE, ::removeUsersCommandTransaction)
}

private fun Bot.handlePartyChangeRequest(
    msg: Message,
    args: String?,
    status: PartyChangeStatus,
    transaction: (Long, String, List<String>) -> Boolean
) {
    val parsedArgs = parseArgs(args)
    val chatId = msg.chat.id

    if (parsedArgs.isNullOrEmpty() || parsedArgs.size < 2) {
        sendMessage(
            chatId,
            if (status == PartyChangeStatus.CREATE) ON_CREATE_EMPTY
            else ON_CHANGE_EMPTY,
            "Markdown"
        )
        return
    }

    val partyName = parsedArgs[0].removePrefix("@")

    val regex = Regex("(.*[@${PROHIBITED_SYMBOLS.joinToString("")}].*)|(.*\\-)")
    if (partyName.length > 50 || partyName.matches(regex)) {
        sendMessage(chatId, ON_PARTY_NAME_FAIL, "Markdown")
        return
    }

    if (!modifyCommandAssertion(chatId, partyName)) {
        return
    }

    val users = parsedArgs.asSequence().drop(1)
        .map { it.replace("@", "") }.distinct()
        .filter { it.matches("([a-z0-9_]{5,32})".toRegex()) }
        .map { "@$it" }.toList()

    if (status.mode < 2 && users.singleOrNull()?.removePrefix("@") == partyName) {
        sendMessage(chatId, ON_SINGLETON_PARTY, "Markdown")
        return
    }

    if (users.size < parsedArgs.drop(1).distinct().size) {
        if (users.isEmpty()) {
            sendMessage(
                chatId,
                if (status == PartyChangeStatus.CREATE) ON_CREATE_EMPTY
                else ON_CHANGE_EMPTY,
                "Markdown"
            )
            return
        }

        sendMessage(chatId, ON_USERS_FAIL, "Markdown")
    }

    if (transaction(chatId, partyName, users)) {
        sendCaseMessage(
            chatId,
            when (status) {
                PartyChangeStatus.CREATE -> """Party $partyName successfully created!"""
                PartyChangeStatus.CHANGE -> """Party $partyName changed beyond recognition!"""
                PartyChangeStatus.ADD    -> """Party $partyName is getting bigger and bigger!"""
                PartyChangeStatus.REMOVE -> """Party $partyName lost somebody, but not the vibe!"""
            }
        )
        return
    }

    sendMessage(
        chatId,
        when (status) {
            PartyChangeStatus.CREATE -> ON_CREATE_REQUEST_FAIL
            PartyChangeStatus.CHANGE -> ON_CHANGE_REQUEST_FAIL
            PartyChangeStatus.ADD    -> ON_ADD_REQUEST_FAIL
            PartyChangeStatus.REMOVE -> ON_REMOVE_REQUEST_FAIL
        },
        "Markdown"
    )
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
