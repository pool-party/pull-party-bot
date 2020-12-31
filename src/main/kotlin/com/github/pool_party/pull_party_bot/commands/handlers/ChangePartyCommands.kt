package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
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
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.database.dao.PartyDao

class CreateCommand(partyDao: PartyDao, chatDao: ChatDao) :
    AbstractChangeCommand("create", "create new party", HELP_CREATE, PartyChangeStatus.CREATE, partyDao, chatDao)

class ChangeCommand(partyDao: PartyDao, chatDao: ChatDao) :
    AbstractChangeCommand("change", "changing existing party", HELP_CHANGE, PartyChangeStatus.CHANGE, partyDao, chatDao)

class AddCommand(partyDao: PartyDao, chatDao: ChatDao) :
    AbstractChangeCommand("add", "add people to a party", HELP_ADD, PartyChangeStatus.ADD, partyDao, chatDao)

class RemoveCommand(partyDao: PartyDao, chatDao: ChatDao) :
    AbstractChangeCommand(
        "remove",
        "remove people from a party",
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
) :
    CaseCommand(command, description, helpMessage, chatDao) {

    override fun Bot.action(message: Message, args: String?) {

        val parsedArgs = parseArgs(args)
        val chatId = message.chat.id

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

        val regex = Regex("(.*[@${Configuration.PROHIBITED_SYMBOLS.joinToString("")}].*)|(.*-)")
        if (partyName.length > 50 || partyName.matches(regex)) {
            sendMessage(chatId, ON_PARTY_NAME_FAIL, "Markdown")
            return
        }

        if (!modifyCommandAssertion(chatId, partyName)) {
            return
        }

        var (users, failedUsers) = parsedArgs.asSequence().drop(1)
            .map { it.replace("@", "") }.distinct()
            .partition { it.matches("([a-z0-9_]{5,32})".toRegex()) }

        users = users.map { "@$it" }

        if (status.changesFull && users.singleOrNull()?.removePrefix("@") == partyName) {
            sendMessage(chatId, ON_SINGLETON_PARTY, "Markdown")
            return
        }

        if (failedUsers.isNotEmpty()) {
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

        if (status.transaction.invoke(partyDao, chatId, partyName, users)) {
            sendCaseMessage(chatId, status.onSuccess(partyName))
            return
        }

        sendMessage(chatId, status.onFailure, "Markdown")
    }
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
