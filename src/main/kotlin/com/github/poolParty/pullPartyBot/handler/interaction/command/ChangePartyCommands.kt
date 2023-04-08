package com.github.poolParty.pullPartyBot.handler.interaction.command

import com.elbekd.bot.Bot
import com.elbekd.bot.types.Message
import com.github.poolParty.pullPartyBot.Configuration
import com.github.poolParty.pullPartyBot.handler.sendMessageLogging
import com.github.poolParty.pullPartyBot.database.dao.PartyDao
import com.github.poolParty.pullPartyBot.handler.message.ChangePartyMessages
import com.github.poolParty.pullPartyBot.handler.message.HelpMessages

class CreateCommand(partyDao: PartyDao) :
    AbstractChangeCommand(
        "create",
        "create new party with mentioned users",
        HelpMessages.create,
        PartyChangeStatus.CREATE,
        partyDao,
    )

class ChangeCommand(partyDao: PartyDao) :
    AbstractChangeCommand(
        "change",
        "change an existing party",
        HelpMessages.change,
        PartyChangeStatus.CHANGE,
        partyDao,
    )

class AddCommand(partyDao: PartyDao) :
    AbstractChangeCommand(
        "add",
        "add new users to the given party",
        HelpMessages.add,
        PartyChangeStatus.ADD,
        partyDao,
    )

class RemoveCommand(partyDao: PartyDao) :
    AbstractChangeCommand(
        "remove",
        "remove given users from the provided party",
        HelpMessages.remove,
        PartyChangeStatus.REMOVE,
        partyDao,
    )

abstract class AbstractChangeCommand(
    command: String,
    description: String,
    helpMessage: String,
    private val status: PartyChangeStatus,
    private val partyDao: PartyDao,
) : AbstractCommand(command, description, helpMessage) {

    override suspend fun Bot.action(message: Message, args: List<String>) {
        val chatId = message.chat.id

        // TODO suggest alias instead of party, if possible

        if (args.size < 2) {
            sendMessageLogging(
                chatId,
                if (status == PartyChangeStatus.CREATE) ChangePartyMessages.createEmpty
                else ChangePartyMessages.changeEmpty,
            )
            return
        }

        val partyName = args[0].removePrefix("@")

        if (!validatePartyName(partyName)) {
            sendMessageLogging(chatId, ChangePartyMessages.partyNameFail)
            return
        }

        if (!modifyCommandAssertion(chatId, partyName)) {
            return
        }

        var (users, failedUsers) = args.asSequence().drop(1)
            .map { it.replace("@", "") }.distinct()
            .partition { it.matches("([a-z\\d_]{5,32})".toRegex()) }

        users = users.map { "@$it" }

        if (status.changesFull && users.singleOrNull()?.removePrefix("@") == partyName) {
            sendMessageLogging(chatId, ChangePartyMessages.singletonParty)
            return
        }

        if (failedUsers.isNotEmpty()) {
            sendMessageLogging(chatId, ChangePartyMessages.usersFail)

            if (users.isEmpty()) {
                sendMessageLogging(
                    chatId,
                    if (status == PartyChangeStatus.CREATE) ChangePartyMessages.createEmpty
                    else ChangePartyMessages.changeEmpty,
                )
                return
            }
        }

        if (status.transaction.invoke(partyDao, chatId, partyName, users)) {
            sendMessageLogging(chatId, status.onSuccess(partyName))
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
    CREATE(true, PartyDao::create, ChangePartyMessages.createRequestFail) {
        override fun onSuccess(partyName: String) = ChangePartyMessages.createSuccess(partyName)
    },

    CHANGE(true, PartyDao::changeUsers, ChangePartyMessages.changeRequestFail) {
        override fun onSuccess(partyName: String) = ChangePartyMessages.changeSuccess(partyName)
    },

    ADD(false, PartyDao::addUsers, ChangePartyMessages.changeRequestFail) {
        override fun onSuccess(partyName: String) = ChangePartyMessages.addSuccess(partyName)
    },

    REMOVE(false, PartyDao::removeUsers, ChangePartyMessages.removeRequestFail) {
        override fun onSuccess(partyName: String) = ChangePartyMessages.deleteSuccess(partyName)
    };

    abstract fun onSuccess(partyName: String): String
}
