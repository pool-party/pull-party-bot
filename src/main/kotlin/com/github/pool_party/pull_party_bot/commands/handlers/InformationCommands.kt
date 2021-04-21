package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekD.bot.Bot
import com.elbekD.bot.types.InlineKeyboardButton
import com.elbekD.bot.types.InlineKeyboardMarkup
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.Configuration
import com.github.pool_party.pull_party_bot.commands.AbstractCommand
import com.github.pool_party.pull_party_bot.commands.CallbackAction
import com.github.pool_party.pull_party_bot.commands.CallbackData
import com.github.pool_party.pull_party_bot.commands.CaseCommand
import com.github.pool_party.pull_party_bot.commands.messages.HELP_FEEDBACK
import com.github.pool_party.pull_party_bot.commands.messages.HELP_LIST
import com.github.pool_party.pull_party_bot.commands.messages.HELP_MSG
import com.github.pool_party.pull_party_bot.commands.messages.HELP_START
import com.github.pool_party.pull_party_bot.commands.messages.INIT_MSG
import com.github.pool_party.pull_party_bot.commands.messages.ON_ARGUMENT_LIST_EMPTY
import com.github.pool_party.pull_party_bot.commands.messages.ON_ARGUMENT_LIST_SUCCESS
import com.github.pool_party.pull_party_bot.commands.messages.ON_FEEDBACK_SUCCESS
import com.github.pool_party.pull_party_bot.commands.messages.ON_HELP_ERROR
import com.github.pool_party.pull_party_bot.commands.messages.ON_LIST_EMPTY
import com.github.pool_party.pull_party_bot.commands.messages.ON_LIST_SUCCESS
import com.github.pool_party.pull_party_bot.commands.messages.ON_STALE_PARTY_REMOVE
import com.github.pool_party.pull_party_bot.commands.messages.onFeedback
import com.github.pool_party.pull_party_bot.database.Alias
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.joda.time.DateTime

class StartCommand : AbstractCommand("start", "awake the bot", HELP_START) {

    override fun Bot.action(message: Message, args: String?) {
        sendMessage(message.chat.id, INIT_MSG)
    }
}

class HelpCommand(private val helpMessages: Map<String, String>) :
    AbstractCommand("help", "show this usage guide", HELP_MSG) {

    override fun Bot.action(message: Message, args: String?) {
        val parsedArgs = parseArgs(args)?.distinct()

        if (parsedArgs.isNullOrEmpty()) {
            sendMessage(message.chat.id, HELP_MSG)
            return
        }

        if (parsedArgs.size > 1) {
            sendMessage(message.chat.id, ON_HELP_ERROR)
            return
        }

        sendMessage(
            message.chat.id,
            helpMessages[parsedArgs[0].removePrefix("/")] ?: ON_HELP_ERROR,
            "Markdown"
        )
    }
}

class ListCommand(private val partyDao: PartyDao, chatDao: ChatDao) :
    CaseCommand("list", "show the parties of the chat", HELP_LIST, chatDao) {

    override fun Bot.action(message: Message, args: String?) {

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

    private fun Bot.listAll(chatId: Long, partyLists: Collection<List<Alias>>, adminsPartySequence: Sequence<String>) {
        val formattedPartySequence = adminsPartySequence + partyLists.asSequence().flatMap { formatIntoStrings(it) }
        val formattedPartyList = formattedPartySequence.toList()
        if (formattedPartyList.isNotEmpty()) {
            sendMessages(chatId, ON_LIST_SUCCESS, formattedPartySequence.toList())
        } else {
            sendMessage(chatId, ON_LIST_EMPTY, "Markdown")
        }
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

                val userSequence = partyLists.asSequence()
                    .map { it.first() }
                    .filter { arg in it.users }
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
            val admins = adminsParty.splitToSequence(" ").map { it.replace("@", "").toLowerCase() }

            if ("admins" in parsedArgs || parsedArgs.any { it in admins }) {
                requestedPartiesSequence = adminsPartySequence + requestedPartiesSequence
            }
        }

        val requestedParties = requestedPartiesSequence.toList()

        if (requestedParties.isNotEmpty()) {
            sendMessages(chatId, ON_ARGUMENT_LIST_SUCCESS, requestedParties)
        } else {
            sendMessage(chatId, ON_ARGUMENT_LIST_EMPTY, "Markdown")
        }
    }

    private fun Bot.suggestDeleting(chatId: Long) {
        val topLost = partyDao.getTopLost(chatId) ?: return

        if (topLost.lastUse.plusWeeks(Configuration.STALE_PARTY_TIME_WEEKS) >= DateTime.now()) return

        sendCaseMessage(
            chatId,
            ON_STALE_PARTY_REMOVE,
            markup = InlineKeyboardMarkup(
                listOf(
                    listOf(
                        InlineKeyboardButton(
                            "Delete ${topLost.name}",
                            callback_data = Json.encodeToString(CallbackData(CallbackAction.DELETE, topLost.id.value))
                        )
                    )
                )
            )
        )
    }

    private fun Bot.sendMessages(chatId: Long, prefix: String, lines: List<String>) {
        val messages = mutableListOf<String>()
        var currentString = StringBuilder(prefix)

        for (line in lines) {
            if (currentString.length + line.length + 1 < Configuration.MESSAGE_LENGTH) {
                currentString.append("\n").append(line)
            } else {
                messages += currentString.toString()
                currentString = StringBuilder(line)
            }
        }

        messages += currentString.toString()

        for (messageText in messages) {
            sendCaseMessage(chatId, messageText, "Markdown").join()
        }
    }

    private fun formatParty(users: String, aliasNames: List<String>) =
        sequenceOf("- ${users.replace("@", "")}") +
            aliasNames.dropLast(1).map { "  ├── $it" } +
            "  └── ${aliasNames.last()}"

    private fun formatIntoStrings(aliases: List<Alias>) =
        formatParty(aliases.first().users, aliases.map { "`${it.name}`" })
}

class FeedbackCommand : AbstractCommand("feedback", "share your ideas and experience with developers", HELP_FEEDBACK) {

    override fun Bot.action(message: Message, args: String?) {
        val parsedArgs = args?.trim()
        val developChatId = Configuration.DEVELOP_CHAT_ID
        if (developChatId == 0L || parsedArgs.isNullOrBlank()) return

        sendMessage(
            developChatId,
            onFeedback(message.from?.username, message.chat.title) + parsedArgs
        )

        sendMessage(message.chat.id, ON_FEEDBACK_SUCCESS, replyTo = message.message_id)
    }
}
