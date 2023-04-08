package com.github.poolParty.pullPartyBot.handler.interaction.command

import com.elbekd.bot.Bot
import com.elbekd.bot.model.toChatId
import com.elbekd.bot.types.BotCommand
import com.elbekd.bot.types.Message
import com.github.poolParty.pullPartyBot.handler.interaction.loggingError
import com.github.poolParty.pullPartyBot.handler.interaction.Interaction
import com.github.poolParty.pullPartyBot.handler.interaction.validateAdministrator
import com.github.poolParty.pullPartyBot.handler.message.ChangePartyMessages
import mu.two.KotlinLogging
import java.time.LocalDateTime
import kotlin.system.measureNanoTime

private val logger = KotlinLogging.logger {}

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

    abstract suspend fun Bot.action(message: Message, args: List<String>)

    override fun Bot.apply() = onCommand(command) { (message, args) ->
        logger.info {
            "${LocalDateTime.now()} $command <- ${message.from?.username}@${message.chat.title}: \"${message.text}\""
        }

        val argsList = args.orEmpty()
            .splitToSequence(" ")
            .filterNot { it.isBlank() }
            .map { it.lowercase() }
            .toList()

        val nanoseconds = loggingError("Failed processing $command $args") {
            measureNanoTime { action(message, argsList) }
        } ?: return@onCommand

        logger.info {
            "$command -> finished in ${nanoseconds / 1000000000}.${nanoseconds % 1000000000}s"
        }
    }

    protected suspend fun Bot.modifyCommandAssertion(chatId: Long, name: String): Boolean =
        if (name == "admins") {
            sendMessage(chatId.toChatId(), ChangePartyMessages.adminsPartyChange)
            false
        } else {
            true
        }
}

abstract class AdministratorCommand(command: String, description: String, helpMessage: String) :
    AbstractCommand(command, description, helpMessage) {

    abstract suspend fun Bot.administratorAction(message: Message, args: List<String>)

    override suspend fun Bot.action(message: Message, args: List<String>) {
        if (validateAdministrator(message.from, message.chat)) administratorAction(message, args)
    }
}
