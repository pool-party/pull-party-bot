package com.github.pool_party.pull_party_bot.command

import com.elbekD.bot.Bot
import com.elbekD.bot.types.InlineKeyboardButton
import com.elbekD.bot.types.InlineKeyboardMarkup
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.Configuration
import com.github.pool_party.pull_party_bot.callback.DeleteSuggestionCallbackData
import com.github.pool_party.pull_party_bot.message.HELP_LIST
import com.github.pool_party.pull_party_bot.message.ON_ARGUMENT_LIST_EMPTY
import com.github.pool_party.pull_party_bot.message.ON_ARGUMENT_LIST_SUCCESS
import com.github.pool_party.pull_party_bot.message.ON_LIST_EMPTY
import com.github.pool_party.pull_party_bot.message.ON_LIST_SUCCESS
import com.github.pool_party.pull_party_bot.message.ON_STALE_PARTY_REMOVE
import com.github.pool_party.pull_party_bot.database.Alias
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import com.github.pool_party.telegram_bot_utils.utils.sendMessageLogging
import org.joda.time.DateTime

class ListCommand(private val partyDao: PartyDao, chatDao: ChatDao) :
    CaseCommand("list", "show the parties of the chat", HELP_LIST, chatDao) {

    override suspend fun Bot.action(message: Message, args: List<String>) {

        val parsedArgs = args.distinct()
        val chatId = message.chat.id
        val list = partyDao.getAll(chatId)
        val partyLists = list.asSequence().sortedByDescending { it.lastUse }.groupBy { it.party.id }.values
        val adminsParty = getAdminsParty(message)
        val adminsPartySequence = adminsParty?.let { formatParty(it, listOf("`admins` _(reserved)_")) }.orEmpty()

        if (parsedArgs.isEmpty()) {
            listAll(chatId, partyLists, adminsPartySequence)
        } else {
            listFind(chatId, list, parsedArgs, partyLists, adminsParty, adminsPartySequence)
        }

        suggestDeleting(chatId)
    }

    private fun Bot.listAll(chatId: Long, partyLists: Collection<List<Alias>>, adminsPartySequence: Sequence<String>) {
        val formattedPartySequence = adminsPartySequence + partyLists.asSequence().flatMap { formatIntoStrings(it) }
        sendMessages(chatId, ON_LIST_SUCCESS, formattedPartySequence, ON_LIST_EMPTY)
    }

    private fun Bot.listFind(
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

    private fun Bot.suggestDeleting(chatId: Long) {
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
                            callback_data = DeleteSuggestionCallbackData(topLost.id.value).encoded
                        )
                    )
                )
            )
        )
    }

    private fun Bot.sendMessages(chatId: Long, prefix: String, lines: Sequence<String>, onEmptyMessage: String) {
        var currentString = StringBuilder(prefix)
        var emptySequence = true

        for (line in lines) {
            emptySequence = false

            if (currentString.length + line.length + 1 < Configuration.MESSAGE_LENGTH) {
                currentString.append("\n").append(line)
            } else {
                sendCaseMessage(chatId, currentString.toString()).join()
                currentString = StringBuilder(line)
            }
        }

        if (emptySequence) {
            sendMessageLogging(chatId, onEmptyMessage)
        } else {
            sendCaseMessage(chatId, currentString.toString()).join()
        }
    }

    private fun formatParty(users: String, aliasNames: List<String>): Sequence<String> {
        val formattedUsers = users.replace("@", "")
            .replace("[_*`\\[]".toRegex()) { "\\${it.groups[0]!!.value}" }

        return sequenceOf("- $formattedUsers") +
            aliasNames.dropLast(1).map { "  ├── $it" } +
            "  └── ${aliasNames.last()}"
    }

    private fun formatIntoStrings(aliases: List<Alias>) =
        formatParty(aliases.first().users, aliases.map { "`${it.name}`" })
}
