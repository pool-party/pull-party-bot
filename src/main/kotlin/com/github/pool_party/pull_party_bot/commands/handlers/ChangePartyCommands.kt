package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.Configuration
import com.github.pool_party.pull_party_bot.commands.CaseCommand
import com.github.pool_party.pull_party_bot.commands.HELP_ADD
import com.github.pool_party.pull_party_bot.commands.HELP_CHANGE
import com.github.pool_party.pull_party_bot.commands.HELP_CREATE
import com.github.pool_party.pull_party_bot.commands.HELP_REMOVE
import com.github.pool_party.pull_party_bot.commands.HELP_RUDE
import com.github.pool_party.pull_party_bot.commands.ON_CHANGE_EMPTY
import com.github.pool_party.pull_party_bot.commands.ON_CHANGE_REQUEST_FAIL
import com.github.pool_party.pull_party_bot.commands.ON_CREATE_EMPTY
import com.github.pool_party.pull_party_bot.commands.ON_CREATE_REQUEST_FAIL
import com.github.pool_party.pull_party_bot.commands.ON_PARTY_NAME_FAIL
import com.github.pool_party.pull_party_bot.commands.ON_REMOVE_REQUEST_FAIL
import com.github.pool_party.pull_party_bot.commands.ON_RUDE_FAIL
import com.github.pool_party.pull_party_bot.commands.ON_SINGLETON_PARTY
import com.github.pool_party.pull_party_bot.commands.ON_USERS_FAIL
import com.github.pool_party.pull_party_bot.database.ChatDao
import com.github.pool_party.pull_party_bot.database.PartyDao

class RudeCommand(chatDao: ChatDao) : CaseCommand("rude", "switch RUDE(CAPS LOCK) mode", HELP_RUDE, chatDao) {

    override fun Bot.action(message: Message, args: String?) {
        val parsedArg = parseArgs(args)?.singleOrNull()
        val chatId = message.chat.id

        val res = when (parsedArg) {
            "on" -> chatDao.setRude(chatId, true)
            "off" -> chatDao.setRude(chatId, false)
            else -> {
                sendMessage(chatId, ON_RUDE_FAIL, "Markdown")
                return
            }
        }

        val curStatus = if (parsedArg == "on") """😈""" else """😇"""
        sendCaseMessage(chatId, """Rude mode ${if (res) "is now" else "was already"} $parsedArg $curStatus!""")
    }
}

class CreateCommand(partyDao: PartyDao, chatDao: ChatDao) :
    AbstractChangeCommand("create", "create new party", HELP_CREATE, PartyChangeStatus.CREATE, partyDao, chatDao)

class ChangeCommand(partyDao: PartyDao, chatDao: ChatDao) :
    AbstractChangeCommand("create", "create new party", HELP_CHANGE, PartyChangeStatus.CHANGE, partyDao, chatDao)

class AddCommand(partyDao: PartyDao, chatDao: ChatDao) :
    AbstractChangeCommand("create", "create new party", HELP_ADD, PartyChangeStatus.ADD, partyDao, chatDao)

class RemoveCommand(partyDao: PartyDao, chatDao: ChatDao) :
    AbstractChangeCommand("create", "create new party", HELP_REMOVE, PartyChangeStatus.REMOVE, partyDao, chatDao)

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

        val users = parsedArgs.asSequence().drop(1)
            .map { it.replace("@", "") }.distinct()
            .filter { it.matches("([a-z0-9_]{5,32})".toRegex()) }
            .map { "@$it" }.toList()

        if (status.changesFull && users.singleOrNull()?.removePrefix("@") == partyName) {
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
        override fun onSuccess(partyName: String) = "Party $partyName successfully created!"
    },

    CHANGE(true, PartyDao::changeUsers, ON_CHANGE_REQUEST_FAIL) {
        override fun onSuccess(partyName: String) = "Party $partyName changed beyond recognition!"
    },

    ADD(false, PartyDao::addUsers, ON_CHANGE_REQUEST_FAIL) {
        override fun onSuccess(partyName: String) = "Party $partyName is getting bigger and bigger!"
    },

    REMOVE(false, PartyDao::removeUsers, ON_REMOVE_REQUEST_FAIL) {
        override fun onSuccess(partyName: String) = "Party $partyName lost somebody, but not the vibe!"
    };

    abstract fun onSuccess(partyName: String): String
}
