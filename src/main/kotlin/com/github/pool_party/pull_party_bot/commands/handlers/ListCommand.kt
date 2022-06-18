package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekd.bot.Bot
import com.elbekd.bot.types.InlineKeyboardButton
import com.elbekd.bot.types.InlineKeyboardMarkup
import com.elbekd.bot.types.Message
import com.github.pool_party.pull_party_bot.Configuration
import com.github.pool_party.pull_party_bot.commands.CallbackAction
import com.github.pool_party.pull_party_bot.commands.CallbackData
import com.github.pool_party.pull_party_bot.commands.CaseCommand
import com.github.pool_party.pull_party_bot.commands.escapeMarkdown
import com.github.pool_party.pull_party_bot.commands.messages.HELP_LIST
import com.github.pool_party.pull_party_bot.commands.messages.ON_ARGUMENT_LIST_EMPTY
import com.github.pool_party.pull_party_bot.commands.messages.ON_ARGUMENT_LIST_SUCCESS
import com.github.pool_party.pull_party_bot.commands.messages.ON_LIST_EMPTY
import com.github.pool_party.pull_party_bot.commands.messages.ON_LIST_SUCCESS
import com.github.pool_party.pull_party_bot.commands.messages.ON_STALE_PARTY_REMOVE
import com.github.pool_party.pull_party_bot.commands.sendMessageLogging
import com.github.pool_party.pull_party_bot.database.Alias
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.joda.time.DateTime

class ListCommand(private val partyDao: PartyDao, chatDao: ChatDao) :
    CaseCommand("list", "show all the parties of the chat and their members", HELP_LIST, chatDao) {

    override suspend fun Bot.action(message: Message, args: String?) {

        val parsedArgs = parseArgs(args)?.distinct()
        val chatId = message.chat.id
        val list = partyDao.getAll(chatId)
        val partyLists = list.asSequence().sortedByDescending { it.lastUse }.groupBy { it.party.id }.values
        val adminsParty = getAdminsParty(message)
        val adminsPartySequence = adminsParty?.let { formatParty(it, listOf("`admins` _(reserved)_")) }.orEmpty()

        if (parsedArgs.isNullOrEmpty()) {
            listAll(chatId, partyLists, adminsPartySequence)
        } else {
            listFind(chatId, list, parsedArgs, partyLists, adminsParty, adminsPartySequence)
        }

        suggestDeleting(chatId)
    }

    private suspend fun Bot.listAll(chatId: Long, partyLists: Collection<List<Alias>>, adminsPartySequence: Sequence<String>) {
        val formattedPartySequence = adminsPartySequence + partyLists.asSequence().flatMap { formatIntoStrings(it) }
        sendMessages(chatId, ON_LIST_SUCCESS, formattedPartySequence, ON_LIST_EMPTY)
    }

    private suspend fun Bot.listFind(
        chatId: Long,
        list: List<Alias>,
        parsedArgs: List<String>,
        partyLists: Collection<List<Alias>>,
        adminsParty: String?,
        adminsPartySequence: Sequence<String>,
    ) {
        val partyMap = list.associateBy { it.name }

        var requestedPartiesSequence = parsedArgs.asSequence()
            .flatMap { arg ->
                val party = partyMap[arg]

                val argRegex = "(^|\\W)@$arg(\\W|$)".toRegex()

                val userSequence = partyLists.asSequence()
                    .map { it.first() }
                    .filter { it.users.contains(argRegex) }
                    .map { it to true }

                if (party != null) {
                    userSequence + (party to false)
                } else {
                    userSequence
                }
            }
            .distinctBy { it.first }
            .flatMap { (it, flag) -> formatIntoStrings(if (flag) it.party.aliases else listOf(it)) }

        if (adminsParty != null) {
            val admins = adminsParty.splitToSequence(" ").map { it.replace("@", "").lowercase() }

            if ("admins" in parsedArgs || parsedArgs.any { it in admins }) {
                requestedPartiesSequence = adminsPartySequence + requestedPartiesSequence
            }
        }

        sendMessages(chatId, ON_ARGUMENT_LIST_SUCCESS, requestedPartiesSequence, ON_ARGUMENT_LIST_EMPTY)
    }

    private suspend fun Bot.suggestDeleting(chatId: Long) {
        val topLost = partyDao.getTopLost(chatId) ?: return

        if (topLost.lastUse.plusWeeks(Configuration.STALE_PARTY_WEEKS) >= DateTime.now()) return

        sendCaseMessage(
            chatId,
            ON_STALE_PARTY_REMOVE,
            markup = InlineKeyboardMarkup(
                listOf(
                    listOf(
                        InlineKeyboardButton(
                            "Delete ${topLost.name}",
                            callbackData = Json.encodeToString(CallbackData(CallbackAction.DELETE, topLost.id.value))
                        )
                    )
                )
            )
        )
    }

    private suspend fun Bot.sendMessages(chatId: Long, prefix: String, lines: Sequence<String>, onEmptyMessage: String) {
        var currentString = StringBuilder(prefix)
        var emptySequence = true

        for (line in lines) {
            emptySequence = false

            if (currentString.length + line.length + 1 < Configuration.MESSAGE_LENGTH) {
                currentString.append("\n").append(line)
            } else {
                sendCaseMessage(chatId, currentString.toString())
                currentString = StringBuilder(line)
            }
        }

        if (emptySequence) {
            sendMessageLogging(chatId, onEmptyMessage)
        } else {
            sendCaseMessage(chatId, currentString.toString())
        }
    }

    private fun formatParty(users: String, aliasNames: List<String>): Sequence<String> {
        val formattedUsers = users.replace("@", "").escapeMarkdown()

        return sequenceOf("- $formattedUsers") +
            aliasNames.dropLast(1).map { "  ├── $it" } +
            "  └── ${aliasNames.last()}"
    }

    private fun formatIntoStrings(aliases: List<Alias>) =
        formatParty(aliases.first().users, aliases.map { "`${it.name.escapeMarkdown()}`" })
}
