package com.github.pool_party.pull_party_bot.command

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.Configuration
import com.github.pool_party.pull_party_bot.message.HELP_ADD
import com.github.pool_party.pull_party_bot.message.HELP_CHANGE
import com.github.pool_party.pull_party_bot.message.HELP_CREATE
import com.github.pool_party.pull_party_bot.message.HELP_REMOVE
import com.github.pool_party.pull_party_bot.message.ON_CHANGE_EMPTY
import com.github.pool_party.pull_party_bot.message.ON_CHANGE_REQUEST_FAIL
import com.github.pool_party.pull_party_bot.message.ON_CREATE_EMPTY
import com.github.pool_party.pull_party_bot.message.ON_CREATE_REQUEST_FAIL
import com.github.pool_party.pull_party_bot.message.ON_PARTY_NAME_FAIL
import com.github.pool_party.pull_party_bot.message.ON_REMOVE_REQUEST_FAIL
import com.github.pool_party.pull_party_bot.message.ON_SINGLETON_PARTY
import com.github.pool_party.pull_party_bot.message.ON_USERS_FAIL
import com.github.pool_party.pull_party_bot.message.onAddSuccess
import com.github.pool_party.pull_party_bot.message.onChangeSuccess
import com.github.pool_party.pull_party_bot.message.onCreateSuccess
import com.github.pool_party.pull_party_bot.message.onDeleteSuccess
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import com.github.pool_party.telegram_bot_utils.utils.sendMessageLogging

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
) : CaseCommand(command, description, helpMessage, chatDao) {

    override suspend fun Bot.action(message: Message, args: List<String>) {

        val chatId = message.chat.id

        // TODO suggest alias instead of party, if possible

        if (args.isEmpty() || args.size < 2) {
            sendMessageLogging(
                chatId,
                if (status == PartyChangeStatus.CREATE) ON_CREATE_EMPTY
                else ON_CHANGE_EMPTY,
            )
            return
        }

        val partyName = args[0].removePrefix("@")

        if (!validatePartyName(partyName)) {
            sendMessageLogging(chatId, ON_PARTY_NAME_FAIL)
            return
        }

        if (!modifyCommandAssertion(chatId, partyName)) {
            return
        }

        var (users, failedUsers) = args.asSequence().drop(1)
            .map { it.replace("@", "") }.distinct()
            .partition { it.matches("([a-z0-9_]{5,32})".toRegex()) }

        users = users.map { "@$it" }

        if (status.changesFull && users.singleOrNull()?.removePrefix("@") == partyName) {
            sendMessageLogging(chatId, ON_SINGLETON_PARTY)
            return
        }

        if (failedUsers.isNotEmpty()) {
            sendMessage(chatId, ON_USERS_FAIL)

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

        sendMessage(chatId, status.onFailure, "Markdown")
    }
}

fun validatePartyName(partyName: String): Boolean {
    val regex = Regex("(.*[@${Configuration.PROHIBITED_SYMBOLS.joinToString("")}].*)|(.*-)")
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
