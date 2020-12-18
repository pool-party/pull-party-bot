package com.github.pool_party.pull_party_bot.commands

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.Configuration
import com.github.pool_party.pull_party_bot.database.ChatDao
import com.github.pool_party.pull_party_bot.database.ChatDaoImpl
import com.github.pool_party.pull_party_bot.database.Party
import com.github.pool_party.pull_party_bot.database.PartyDao
import com.github.pool_party.pull_party_bot.database.PartyDaoImpl

// TODO split into couple of files

fun Bot.initPingCommandHandlers() {

    val partyDaoImpl = PartyDaoImpl()

    val chatDaoImpl = ChatDaoImpl()

    // TODO probably use some kind of injections
    val commands = mutableListOf(
        StartCommand(),
        ListCommand(partyDaoImpl, chatDaoImpl),
        PartyCommand(partyDaoImpl),
        DeleteCommand(partyDaoImpl, chatDaoImpl),
        ClearCommand(chatDaoImpl),
        CreateCommand(partyDaoImpl, chatDaoImpl),
        ChangeCommand(partyDaoImpl, chatDaoImpl),
        AddCommand(partyDaoImpl, chatDaoImpl),
        RemoveCommand(partyDaoImpl, chatDaoImpl),
        RudeCommand(chatDaoImpl),
        FeedbackCommand()
    )

    val helpCommand = HelpCommand(commands.associate { it.command.removePrefix("/") to it.helpMessage })
    commands.add(helpCommand)

    val implicitPartyHandler = ImplicitPartyHandler(partyDaoImpl)

    implicitPartyHandler.onMessage(this)
    commands.forEach { it.onMessage(this) }
    setMyCommands(commands.map { it.toBotCommand() })
}

class ImplicitPartyHandler(private val partyDao: PartyDao) {

    /**
     * Handle implicit `@party-name`-like calls
     */
    fun onMessage(bot: Bot) = bot.onMessage { bot.action(it) }

    private fun Bot.action(message: Message) {
        val text = message.text ?: message.caption

        if (message.forward_from != null || text == null) {
            return
        }

        val prohibitedSymbolsString = PROHIBITED_SYMBOLS.joinToString("")
        val regex = Regex("(?<party>[^$prohibitedSymbolsString]*)[$prohibitedSymbolsString]*")

        val partyNames = text.lineSequence()
            .flatMap { it.split(' ', '\t').asSequence() }
            .filter { it.startsWith('@') }
            .map { it.removePrefix("@") }
            .mapNotNull { regex.matchEntire(it)?.groups?.get(1)?.value }

        handleParty(partyNames, message, partyDao)
    }
}

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

class PartyCommand(private val partyDao: PartyDao) :
    AbstractCommand("party", "tag the members of existing parties", HELP_PARTY) {

    override fun Bot.action(message: Message, args: String?) {
        val parsedArgs = parseArgs(args)?.distinct()
        val chatId = message.chat.id

        if (parsedArgs.isNullOrEmpty()) {
            sendMessage(chatId, ON_PARTY_EMPTY, "Markdown")
            return
        }

        handleParty(parsedArgs.asSequence(), message, partyDao) {
            sendMessage(
                chatId,
                if (parsedArgs.size == 1) ON_PARTY_REQUEST_FAIL
                else ON_PARTY_REQUEST_FAILS,
                "Markdown"
            )
        }
    }
}

private fun Bot.handleParty(
    partyNames: Sequence<String>,
    message: Message,
    partyDao: PartyDao,
    onFailure: () -> Unit = {}
) {
    val chatId = message.chat.id
    var failure = false

    val res = partyNames
        .map { it.toLowerCase() }
        .distinct()
        .mapNotNull {
            if (it == "admins") {
                handleAdminsParty(message)
            } else {
                val users = partyDao.getByIdAndName(chatId, it)
                if (users.isNullOrBlank()) {
                    failure = true
                }
                users
            }
        }
        .flatMap { it.split(" ").asSequence() }
        .distinct()
        .joinToString(" ")

    sendMessage(chatId, res, replyTo = message.message_id)

    if (failure) {
        onFailure()
    }
}

private fun Bot.handleAdminsParty(msg: Message): String? {
    val chatId = msg.chat.id
    val chatType = msg.chat.type

    if (chatType != "group" && chatType != "supergroup") {
        sendMessage(chatId, ON_ADMINS_PARTY_FAIL, "Markdown")
        return null
    }

    return getChatAdministrators(chatId)
        .join()
        .asSequence()
        .mapNotNull { it.user.username }
        .filter { it.substring(it.length - 3).toLowerCase() != "bot" }
        .map { "@$it" }
        .joinToString(" ")
}

class DeleteCommand(private val partyDao: PartyDao, chatDao: ChatDao) :
    AdministratorCommand("delete", "forget the parties as they have never happened", HELP_DELETE, chatDao) {

    override fun Bot.mainAction(message: Message, args: String?) {
        val parsedArgs = parseArgs(args)?.distinct()
        val chatId = message.chat.id

        if (parsedArgs.isNullOrEmpty()) {
            sendMessage(chatId, ON_DELETE_EMPTY, "Markdown")
            return
        }

        parsedArgs.forEach {
            if (modifyCommandAssertion(chatId, it)) {
                sendCaseMessage(
                    chatId,
                    if (partyDao.delete(chatId, it))
                        """Party $it is just a history now 👍"""
                    else """Not like I knew the $it party, but now I don't know it at all 👍"""
                )
            }
        }
    }
}

class ClearCommand(chatDao: ChatDao) :
    AdministratorCommand("clear", "shut down all the parties ever existed", HELP_CLEAR, chatDao) {

    override fun Bot.mainAction(message: Message, args: String?) {
        val chatId = message.chat.id
        chatDao.clear(chatId)
        sendMessage(chatId, ON_CLEAR_SUCCESS, "Markdown")
    }
}

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

        val regex = Regex("(.*[@${PROHIBITED_SYMBOLS.joinToString("")}].*)|(.*-)")
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

class CreateCommand(partyDao: PartyDao, chatDao: ChatDao) :
    AbstractChangeCommand("create", "create new party", HELP_CREATE, PartyChangeStatus.CREATE, partyDao, chatDao)

class ChangeCommand(partyDao: PartyDao, chatDao: ChatDao) :
    AbstractChangeCommand("create", "create new party", HELP_CREATE, PartyChangeStatus.CHANGE, partyDao, chatDao)

class AddCommand(partyDao: PartyDao, chatDao: ChatDao) :
    AbstractChangeCommand("create", "create new party", HELP_CREATE, PartyChangeStatus.ADD, partyDao, chatDao)

class RemoveCommand(partyDao: PartyDao, chatDao: ChatDao) :
    AbstractChangeCommand("create", "create new party", HELP_CREATE, PartyChangeStatus.REMOVE, partyDao, chatDao)

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
