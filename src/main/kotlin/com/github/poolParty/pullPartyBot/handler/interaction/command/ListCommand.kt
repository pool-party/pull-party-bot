package com.github.poolParty.pullPartyBot.handler.interaction.command

import com.elbekd.bot.Bot
import com.elbekd.bot.types.InlineKeyboardButton
import com.elbekd.bot.types.InlineKeyboardMarkup
import com.elbekd.bot.types.Message
import com.github.poolParty.pullPartyBot.Configuration
import com.github.poolParty.pullPartyBot.handler.escapeMarkdown
import com.github.poolParty.pullPartyBot.handler.sendMessageLogging
import com.github.poolParty.pullPartyBot.database.Alias
import com.github.poolParty.pullPartyBot.database.dao.PartyDao
import com.github.poolParty.pullPartyBot.handler.Button
import com.github.poolParty.pullPartyBot.handler.interaction.callback.DeleteSuggestionCallbackData
import com.github.poolParty.pullPartyBot.handler.interaction.common.getAdminsParty
import com.github.poolParty.pullPartyBot.handler.message.HelpMessages
import com.github.poolParty.pullPartyBot.handler.message.InformationMessages
import com.github.poolParty.pullPartyBot.handler.message.ListMessages
import org.joda.time.DateTime

class ListCommand(private val partyDao: PartyDao) :
    AbstractCommand("list", "show all the parties of the chat and their members", HelpMessages.list) {

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

    private suspend fun Bot.listAll(
        chatId: Long,
        partyLists: Collection<List<Alias>>,
        adminsPartySequence: Sequence<String>,
    ) {
        val formattedPartySequence = adminsPartySequence + partyLists.asSequence().flatMap { formatIntoStrings(it) }
        sendMessages(chatId, ListMessages.success, formattedPartySequence, ListMessages.empty)
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

        sendMessages(chatId, ListMessages.argumentSuccess, requestedPartiesSequence, ListMessages.argumentEmpty)
    }

    private suspend fun Bot.suggestDeleting(chatId: Long) {
        val topLost = partyDao.getTopLost(chatId) ?: return

        if (topLost.lastUse.plusWeeks(Configuration.STALE_PARTY_WEEKS) >= DateTime.now()) return

        sendMessageLogging(
            chatId,
            InformationMessages.stalePartyRemove,
            buttons = listOf(Button("Delete ${topLost.name}", DeleteSuggestionCallbackData(topLost.id.value))),
        )
    }

    private suspend fun Bot.sendMessages(
        chatId: Long,
        prefix: String,
        lines: Sequence<String>,
        onEmptyMessage: String,
    ) {
        var currentString = StringBuilder(prefix)
        var emptySequence = true

        for (line in lines) {
            emptySequence = false

            if (currentString.length + line.length + 1 < Configuration.MESSAGE_LENGTH) {
                currentString.append("\n").append(line)
            } else {
                sendMessageLogging(chatId, currentString.toString())
                currentString = StringBuilder(line)
            }
        }

        sendMessageLogging(
            chatId,
            if (emptySequence) onEmptyMessage else currentString.toString(),
        )
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
