package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.Configuration
import com.github.pool_party.pull_party_bot.commands.AbstractCommand
import com.github.pool_party.pull_party_bot.commands.CaseCommand
import com.github.pool_party.pull_party_bot.commands.messages.HELP_FEEDBACK
import com.github.pool_party.pull_party_bot.commands.messages.HELP_LIST
import com.github.pool_party.pull_party_bot.commands.messages.HELP_MSG
import com.github.pool_party.pull_party_bot.commands.messages.HELP_START
import com.github.pool_party.pull_party_bot.commands.messages.INIT_MSG
import com.github.pool_party.pull_party_bot.commands.messages.ON_ARGUMENT_LIST_EMPTY
import com.github.pool_party.pull_party_bot.commands.messages.ON_ARGUMENT_LIST_SUCCESS
import com.github.pool_party.pull_party_bot.commands.messages.ON_HELP_ERROR
import com.github.pool_party.pull_party_bot.commands.messages.ON_LIST_EMPTY
import com.github.pool_party.pull_party_bot.commands.messages.ON_LIST_SUCCESS
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.database.Party
import com.github.pool_party.pull_party_bot.database.dao.PartyDao

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

        fun Party.format() = "$name: ${users.replace("@", "")}"

        val parsedArgs = parseArgs(args)?.distinct()
        val chatId = message.chat.id
        val list = partyDao.getAll(chatId)

        if (parsedArgs.isNullOrEmpty()) {
            val partyList = list.asSequence().map { it.format() }.joinToString("\n")

            if (partyList.isNotBlank()) {
                sendCaseMessage(chatId, ON_LIST_SUCCESS + partyList)
            } else {
                sendMessage(chatId, ON_LIST_EMPTY, "Markdown")
            }

            return
        }

        val partyMap = list.associateBy { it.name }
        val requestedParties = parsedArgs.asSequence()
            .flatMap { arg ->
                val party = partyMap[arg]
                if (party != null) {
                    sequenceOf(party)
                } else {
                    partyMap.values.asSequence().filter { arg in it.users }
                }
            }
            .distinct()
            .map { it.format() }
            .joinToString("\n")

        if (requestedParties.isNotBlank()) {
            sendCaseMessage(chatId, ON_ARGUMENT_LIST_SUCCESS + requestedParties)
            return
        }

        sendMessage(chatId, ON_ARGUMENT_LIST_EMPTY, "Markdown")
    }
}

class FeedbackCommand : AbstractCommand("feedback", "share your ideas and experience with developers", HELP_FEEDBACK) {

    override fun Bot.action(message: Message, args: String?) {
        val parsedArgs = args?.trim()
        val developChatId = Configuration.DEVELOP_CHAT_ID
        if (developChatId == 0L || parsedArgs.isNullOrBlank()) return

        sendMessage(
            developChatId,
            "New #feedback from @${message.from?.username} in \"${message.chat.title}\":\n\n" + parsedArgs
        )
    }
}
