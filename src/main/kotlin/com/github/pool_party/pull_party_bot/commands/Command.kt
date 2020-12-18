package com.github.pool_party.pull_party_bot.commands

import com.elbekD.bot.Bot
import com.elbekD.bot.types.BotCommand
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.database.ChatDao
import mu.KotlinLogging
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

interface Command {

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

    fun onMessage(bot: Bot)

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
        name.equals("admins").not().also { if (!it) sendMessage(chatId, ON_ADMINS_PARTY_CHANGE, "Markdown") }

    protected fun parseArgs(args: String?): List<String>? =
        args?.split(' ')?.map { it.trim().toLowerCase() }?.filter { it.isNotBlank() }

}

abstract class CaseCommand(command: String, description: String, helpMessage: String, protected val chatDao: ChatDao) :
    AbstractCommand(command, description, helpMessage) {

    protected fun Bot.sendCaseMessage(
        chatId: Long,
        message: String,
        parseMode: String? = null,
        replyTo: Int? = null
    ) =
        sendMessage(
            chatId,
            if (chatDao.getRude(chatId)) message.toUpperCase() else message,
            parseMode,
            replyTo = replyTo
        )
}

abstract class AdministratorCommand(command: String, description: String, helpMessage: String, chatDao: ChatDao) :
    CaseCommand(command, description, helpMessage, chatDao) {

    abstract fun Bot.mainAction(message: Message, args: String?)

    override fun Bot.action(message: Message, args: String?) {
        val sender = message.from
        val chatId = message.chat.id
        if (sender == null) {
            sendMessage(chatId, ON_SENDER_FAIL, "Markdown")
            return
        }

        val chatType = message.chat.type
        if ((chatType == "group" || chatType == "supergroup") &&
            getChatAdministrators(chatId).join().all { it.user != sender }
        ) {
            sendMessage(chatId, ON_PERMISSION_DENY, "Markdown")
            return
        }

        mainAction(message, args)
    }
}
