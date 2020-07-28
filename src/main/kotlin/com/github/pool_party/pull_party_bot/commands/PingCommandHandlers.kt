package com.github.pool_party.pull_party_bot.commands

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.database.clearCommandTransaction
import com.github.pool_party.pull_party_bot.database.createCommandTransaction
import com.github.pool_party.pull_party_bot.database.deleteCommandTransaction
import com.github.pool_party.pull_party_bot.database.listCommandTransaction
import com.github.pool_party.pull_party_bot.database.partyCommandTransaction
import com.github.pool_party.pull_party_bot.database.rudeCheckTransaction
import com.github.pool_party.pull_party_bot.database.rudeCommandTransaction
import com.github.pool_party.pull_party_bot.database.updateCommandTransaction

fun Bot.initPingCommandHandlers() {
    onNoArgumentsCommand("/start", ::handleStart)
    onNoArgumentsCommand("/help", ::handleHelp)
    onNoArgumentsCommand("/list", ::handleList)

    onCommand("/party", ::handleParty)
    onAdministratorCommand("/delete", ::handleDelete)
    onAdministratorCommand("/clear") { msg, _ -> handleClear(msg) }

    onCommand("/create", ::handleCreate)
    onCommand("/update", ::handleUpdate)

    onCommand("/rude", ::handleRude)

    onMessage(::handleImplicitParty)
}

private fun Bot.onNoArgumentsCommand(command: String, action: (Message) -> Unit) =
    onCommand(command) { msg, _ -> action(msg) }

private fun Bot.onAdministratorCommand(command: String, action: (Message, String?) -> Unit) =
    onCommand(command) { msg, args ->
        val sender = msg.from
        val chatId = msg.chat.id
        if (sender == null) {
            sendMessage(chatId, ON_SENDER_FAIL)
            return@onCommand
        }

        val chatType = msg.chat.type
        if ((chatType == "group" || chatType == "supergroup") &&
            getChatAdministrators(chatId).join().all { it.user != sender }
        ) {
            sendMessage(chatId, ON_PERMISSION_DENY, "Markdown")
            return@onCommand
        }

        action(msg, args)
    }

private fun Bot.modifyCommandAssertion(chatId: Long, name: String): Boolean =
    name.equals("admins").not().also { if (!it) sendMessage(chatId, ON_ADMINS_PARTY_CHANGE, "Markdown") }

/**
 * Initiate the dialog with bot.
 */
fun Bot.handleStart(msg: Message) {
    sendMessage(msg.chat.id, INIT_MSG, "Markdown")
}

/**
 * Return the help message.
 */
fun Bot.handleHelp(msg: Message) {
    sendMessage(msg.chat.id, HELP_MSG, "Markdown")
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
suspend fun Bot.handleParty(msg: Message, args: String?) {
    val parsedArgs = parseArgs(args)
    val chatId = msg.chat.id

    if (parsedArgs.isNullOrEmpty()) {
        sendMessage(chatId, ON_PARTY_EMPTY)
        return
    }

    var hasInvalidRes = false
    parsedArgs.forEach {
        if (it == "admins") {
            pullAdminsParty(msg)
            return@forEach
        }

        val res = partyCommandTransaction(chatId, it)
        if (res.isNullOrBlank()) {
            hasInvalidRes = true
            return@forEach
        }

        sendCaseMessage(chatId, pullParty(it, res))
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
 * Deletes all the parties of the chat.
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
 * Update existing party.
 */
suspend fun Bot.handleUpdate(msg: Message, args: String?) = handlePartyChangeRequest(false, msg, args)

/**
 * Handle both `update` and `create` commands.
 */
private fun Bot.handlePartyChangeRequest(isNew: Boolean, msg: Message, args: String?) {
    val parsedList = args?.split(' ')?.map { it.trim().toLowerCase() }
    val chatId = msg.chat.id

    if (parsedList.isNullOrEmpty() || parsedList.size < 2) {
        sendMessage(chatId, if (isNew) ON_CREATE_EMPTY else ON_UPDATE_EMPTY)
        return
    }

    val partyName = parsedList[0].replace("@", "")

    if (partyName.isBlank()) {
        sendMessage(chatId, ON_PARTY_NAME_FAIL)
        return
    }

    if (!modifyCommandAssertion(chatId, partyName)) {
        return
    }

    val users = parsedList.drop(1)
        .map { it.replace("@","") }.distinct()
        .filter { it.matches("([a-z0-9_]{5,32})".toRegex()) }
        .map { "@$it" }

    if (users.size < parsedList.distinct().size - 1) {
        sendMessage(chatId, ON_USERS_FAIL)
    }

    if (if (isNew) createCommandTransaction(chatId, partyName, users)
        else updateCommandTransaction(chatId, partyName, users)
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
    sendCaseMessage(chatId, """Rude mode ${if (res) "is now" else "was already"} $parsedArg $curStatus!""")
}

/**
 * Handle explicit `@party-name`-like calls
 */
suspend fun Bot.handleImplicitParty(msg: Message) {
    if (msg.forward_from != null) {
        return
    }
    val chatId = msg.chat.id

    msg.text?.let {
        it.lineSequence()
            .flatMap { it.split(' ', '\t').asSequence() }
            .filter { it.startsWith('@') }
            .mapNotNull {
                if (it == "@admins") {
                    pullAdminsParty(msg)
                    null
                } else {
                    partyCommandTransaction(chatId, it.substring(1))?.to(it)
                }
            }
            .forEach { (users, partyName) ->
                sendCaseMessage(chatId, pullParty(partyName, users))
            }
    }
}

// TODO create utils package with these functions
private fun parseArgs(args: String?): List<String>? = args?.split(' ')?.map { it.trim().toLowerCase() }?.distinct()

private fun Bot.sendCaseMessage(chatId: Long, msg: String, parseMode: String? = null) =
    sendMessage(
        chatId,
        if (rudeCheckTransaction(chatId)) msg.toUpperCase() else msg,
        parseMode
    )

private fun pullParty(partyName: String, res: String): String =
    """
    $ON_PARTY_SUCCESS $partyName:

    $res
    """.trimIndent()

private fun Bot.pullAdminsParty(msg: Message) {
    val chatId = msg.chat.id
    val chatType = msg.chat.type

    if (chatType != "group" && chatType != "supergroup") {
        sendMessage(chatId, ON_ADMINS_PARTY_FAIL, "Markdown")
        return
    }

    val adminsParty = getChatAdministrators(chatId)
        .join()
        .asSequence()
        .map { "@" + it.user.username }
        .joinToString(" ")

    sendMessage(chatId, adminsParty)
}
