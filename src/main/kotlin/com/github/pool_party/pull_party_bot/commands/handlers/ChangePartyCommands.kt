package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekd.bot.Bot
import com.elbekd.bot.types.Message
import com.github.pool_party.pull_party_bot.Configuration
import com.github.pool_party.pull_party_bot.commands.CaseCommand
import com.github.pool_party.pull_party_bot.commands.messages.HELP_ADD
import com.github.pool_party.pull_party_bot.commands.messages.HELP_CHANGE
import com.github.pool_party.pull_party_bot.commands.messages.HELP_CREATE
import com.github.pool_party.pull_party_bot.commands.messages.HELP_REMOVE
import com.github.pool_party.pull_party_bot.commands.messages.ON_CHANGE_EMPTY
import com.github.pool_party.pull_party_bot.commands.messages.ON_CHANGE_REQUEST_FAIL
import com.github.pool_party.pull_party_bot.commands.messages.ON_CREATE_EMPTY
import com.github.pool_party.pull_party_bot.commands.messages.ON_CREATE_REQUEST_FAIL
import com.github.pool_party.pull_party_bot.commands.messages.ON_PARTY_NAME_FAIL
import com.github.pool_party.pull_party_bot.commands.messages.ON_REMOVE_REQUEST_FAIL
import com.github.pool_party.pull_party_bot.commands.messages.ON_SINGLETON_PARTY
import com.github.pool_party.pull_party_bot.commands.messages.ON_USERS_FAIL
import com.github.pool_party.pull_party_bot.commands.messages.onAddSuccess
import com.github.pool_party.pull_party_bot.commands.messages.onChangeSuccess
import com.github.pool_party.pull_party_bot.commands.messages.onCreateSuccess
import com.github.pool_party.pull_party_bot.commands.messages.onDeleteSuccess
import com.github.pool_party.pull_party_bot.commands.sendMessageLogging
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.database.dao.PartyDao

class CreateCommand(partyDao: PartyDao, chatDao: ChatDao) :
    AbstractChangeCommand(
        "create",
        "create new party with mentioned users",
        HELP_CREATE,
        PartyChangeStatus.CREATE,
        partyDao,
        chatDao
    )

class ChangeCommand(partyDao: PartyDao, chatDao: ChatDao) :
    AbstractChangeCommand(
        "change",
        "change an existing party",
        HELP_CHANGE,
        PartyChangeStatus.CHANGE,
        partyDao,
        chatDao
    )

class AddCommand(partyDao: PartyDao, chatDao: ChatDao) :
    AbstractChangeCommand("add", "add new users to the given party", HELP_ADD, PartyChangeStatus.ADD, partyDao, chatDao)

class RemoveCommand(partyDao: PartyDao, chatDao: ChatDao) :
    AbstractChangeCommand(
        "remove",
        "remove given users from the provided party",
        HELP_REMOVE,
        PartyChangeStatus.REMOVE,
        partyDao,
        chatDao
    )

abstract class AbstractChangeCommand(
    command: String,
    description: String,
    helpMessage: String,
    private val status: PartyChangeStatus,
    private val partyDao: PartyDao,
    chatDao: ChatDao
) : CaseCommand(command, description, helpMessage, chatDao) {

    override suspend fun Bot.action(message: Message, args: String?) {

        val parsedArgs = parseArgs(args)
        val chatId = message.chat.id

        // TODO suggest alias instead of party, if possible

        if (parsedArgs.isNullOrEmpty() || parsedArgs.size < 2) {
            sendMessageLogging(
                chatId,
                if (status == PartyChangeStatus.CREATE) ON_CREATE_EMPTY
                else ON_CHANGE_EMPTY,
            )
            return
        }

        val partyName = parsedArgs[0].removePrefix("@")

        if (!validatePartyName(partyName)) {
            sendMessageLogging(chatId, ON_PARTY_NAME_FAIL)
            return
        }

        if (!modifyCommandAssertion(chatId, partyName)) {
            return
        }

        var (users, failedUsers) = parsedArgs.asSequence().drop(1)
            .map { it.replace("@", "") }.distinct()
            .partition { it.matches("([a-z\\d_]{5,32})".toRegex()) }

        users = users.map { "@$it" }

        if (status.changesFull && users.singleOrNull()?.removePrefix("@") == partyName) {
            sendMessageLogging(chatId, ON_SINGLETON_PARTY)
            return
        }

        if (failedUsers.isNotEmpty()) {
            sendMessageLogging(chatId, ON_USERS_FAIL)

            if (users.isEmpty()) {
                sendMessageLogging(
                    chatId,
                    if (status == PartyChangeStatus.CREATE) ON_CREATE_EMPTY
                    else ON_CHANGE_EMPTY,
                )
                return
            }
        }

        if (status.transaction.invoke(partyDao, chatId, partyName, users)) {
            sendCaseMessage(chatId, status.onSuccess(partyName))
            return
        }

        sendMessageLogging(chatId, status.onFailure)
    }
}

fun validatePartyName(partyName: String): Boolean {
    val regex = Regex(".*[@${Configuration.PROHIBITED_SYMBOLS.joinToString("")}].*")
    return partyName.isNotBlank() && partyName.length <= 50 && !partyName.matches(regex)
}

enum class PartyChangeStatus(
    val changesFull: Boolean,
    val transaction: PartyDao.(Long, String, List<String>) -> Boolean,
    val onFailure: String
) {
    CREATE(true, PartyDao::create, ON_CREATE_REQUEST_FAIL) {
        override fun onSuccess(partyName: String) = onCreateSuccess(partyName)
    },

    CHANGE(true, PartyDao::changeUsers, ON_CHANGE_REQUEST_FAIL) {
        override fun onSuccess(partyName: String) = onChangeSuccess(partyName)
    },

    ADD(false, PartyDao::addUsers, ON_CHANGE_REQUEST_FAIL) {
        override fun onSuccess(partyName: String) = onAddSuccess(partyName)
    },

    REMOVE(false, PartyDao::removeUsers, ON_REMOVE_REQUEST_FAIL) {
        override fun onSuccess(partyName: String) = onDeleteSuccess(partyName)
    };

    abstract fun onSuccess(partyName: String): String
}
