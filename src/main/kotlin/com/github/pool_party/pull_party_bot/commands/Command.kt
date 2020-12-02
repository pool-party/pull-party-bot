package com.github.pool_party.pull_party_bot.commands

import com.elbekD.bot.Bot
import com.elbekD.bot.types.BotCommand
import com.elbekD.bot.types.Message
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

fun Bot.registerCommands() {
    val commands = Command.all
    commands.forEach {
        onCommand(it.command) { msg, args ->
            logger.info { "${it.command} <- ${msg.from?.username}@\"${msg.chat.title}\": \"${msg.text}\"" }
            it.action.invoke(this, msg, args)
        }
    }
    setMyCommands(commands.map { it.toBotCommand() }).join()
}

class Command(
    val command: String,
    val description: String,
    val helpMessage: String,
    val action: Bot.(Message, String?) -> Unit
) {
    init {
        all.add(this)
        helpMessages[command.drop(1)] = helpMessage
    }

    fun toBotCommand() = BotCommand(command, description)

    companion object {
        val all = mutableListOf<Command>()
        val helpMessages = mutableMapOf<String, String>()
    }
}

fun newCommand(commandName: String, description: String, helpMessage: String, action: Bot.(Message, String?) -> Unit) =
    Command("/$commandName", description, helpMessage, action)

fun newNoArgumentCommand(commandName: String, description: String, helpMessage: String, action: Bot.(Message) -> Unit) =
    newCommand(commandName, description, helpMessage) { msg, _ -> action(msg) }

fun newAdministratorCommand(
    commandName: String,
    description: String,
    helpMessage: String,
    action: Bot.(Message, String?) -> Unit
) =
    newCommand(commandName, description, helpMessage) { msg, args ->
        val sender = msg.from
        val chatId = msg.chat.id
        if (sender == null) {
            sendMessage(chatId, ON_SENDER_FAIL, "Markdown")
            return@newCommand
        }

        val chatType = msg.chat.type
        if ((chatType == "group" || chatType == "supergroup") &&
            getChatAdministrators(chatId).join().all { it.user != sender }
        ) {
            sendMessage(chatId, ON_PERMISSION_DENY, "Markdown")
            return@newCommand
        }

        action(msg, args)
    }
