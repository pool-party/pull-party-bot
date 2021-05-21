package com.github.pool_party.pull_party_bot.commands

import com.elbekD.bot.Bot
import com.elbekD.bot.types.BotCommand
import com.elbekD.bot.types.Chat
import com.elbekD.bot.types.Message
import com.elbekD.bot.types.ReplyKeyboard
import com.elbekD.bot.types.User
import com.github.pool_party.pull_party_bot.commands.messages.ON_ADMINS_PARTY_CHANGE
import com.github.pool_party.pull_party_bot.commands.messages.ON_PERMISSION_DENY
import com.github.pool_party.pull_party_bot.commands.messages.ON_SENDER_FAIL
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import mu.KotlinLogging
import java.time.LocalDateTime
import java.util.Locale

private val logger = KotlinLogging.logger {}

interface Interaction {

    fun onMessage(bot: Bot)
}

interface Command : Interaction {

    /**
     * Command name starting with "/".
     */
    val command: String

    /**
     * Command description that will be displayed in a small popup in Telegram as you type commands.
     */
    val description: String

    /**
     * Command help message that will be displayed on command /help <command>
     */
    val helpMessage: String

    fun toBotCommand() = BotCommand(command, description)
}

abstract class AbstractCommand(
    commandName: String,
    override val description: String,
    override val helpMessage: String
) : Command {
    override val command = "/$commandName"

    abstract fun Bot.action(message: Message, args: String?)

    override fun onMessage(bot: Bot) = bot.onCommand(command) { message, args ->
        logger.info {
            "${LocalDateTime.now()} $command <- ${message.from?.username}@${message.chat.title}: \"${message.text}\""
        }
        bot.action(message, args)
    }

    protected fun Bot.modifyCommandAssertion(chatId: Long, name: String): Boolean =
        (name == "admins").not().also { if (!it) sendMessage(chatId, ON_ADMINS_PARTY_CHANGE) }

    protected fun parseArgs(args: String?): List<String>? =
        args?.split(' ')?.map { it.trim().lowercase(Locale.getDefault()) }?.filter { it.isNotBlank() }
}

abstract class CaseCommand(command: String, description: String, helpMessage: String, protected val chatDao: ChatDao) :
    AbstractCommand(command, description, helpMessage) {

    protected fun Bot.sendCaseMessage(
        chatId: Long,
        message: String,
        parseMode: String? = null,
        replyTo: Int? = null,
        markup: ReplyKeyboard? = null
    ) =
        sendMessage(
            chatId,
            if (chatDao.getRude(chatId)) message.toUpperCase() else message,
            parseMode,
            replyTo = replyTo,
            markup = markup
        )
}

abstract class AdministratorCommand(command: String, description: String, helpMessage: String, chatDao: ChatDao) :
    CaseCommand(command, description, helpMessage, chatDao) {

    abstract fun Bot.mainAction(message: Message, args: String?)

    override fun Bot.action(message: Message, args: String?) {
        if (validateAdministrator(message.from, message.chat)) mainAction(message, args)
    }
}

fun Bot.validateAdministrator(user: User?, chat: Chat, sendMessage: Boolean = true): Boolean {
    val chatId = chat.id

    if (user == null) {
        sendMessage(chatId, ON_SENDER_FAIL, "Markdown")
        return false
    }

    val chatType = chat.type
    if ((chatType == "group" || chatType == "supergroup") &&
        getChatAdministrators(chatId).join().all { it.user != user }
    ) {
        if (sendMessage) sendMessage(chatId, ON_PERMISSION_DENY, "Markdown")
        return false
    }
    return true
}
