package com.github.poolParty.pullPartyBot.handler.interaction

import com.elbekd.bot.Bot
import com.github.poolParty.pullPartyBot.database.dao.ChatDaoImpl
import com.github.poolParty.pullPartyBot.database.dao.PartyDaoImpl
import com.github.poolParty.pullPartyBot.handler.interaction.callback.CallbackDispatcher
import com.github.poolParty.pullPartyBot.handler.interaction.command.AddCommand
import com.github.poolParty.pullPartyBot.handler.interaction.command.AliasCommand
import com.github.poolParty.pullPartyBot.handler.interaction.command.ChangeCommand
import com.github.poolParty.pullPartyBot.handler.interaction.command.ClearCommand
import com.github.poolParty.pullPartyBot.handler.interaction.command.Command
import com.github.poolParty.pullPartyBot.handler.interaction.command.CreateCommand
import com.github.poolParty.pullPartyBot.handler.interaction.command.DeleteCommand
import com.github.poolParty.pullPartyBot.handler.interaction.command.FeedbackCommand
import com.github.poolParty.pullPartyBot.handler.interaction.command.HelpCommand
import com.github.poolParty.pullPartyBot.handler.interaction.command.ListCommand
import com.github.poolParty.pullPartyBot.handler.interaction.command.PartyCommand
import com.github.poolParty.pullPartyBot.handler.interaction.command.RemoveCommand
import com.github.poolParty.pullPartyBot.handler.interaction.command.StartCommand
import com.github.poolParty.pullPartyBot.handler.interaction.everyMessage.EveryMessageProcessor
import com.github.poolParty.pullPartyBot.handler.interaction.everyMessage.ImplicitPartyHandler
import com.github.poolParty.pullPartyBot.handler.interaction.everyMessage.MigrationHandler

suspend fun Bot.initHandlers() {
    val partyDaoImpl = PartyDaoImpl()

    val chatDaoImpl = ChatDaoImpl()

    val everyMessageInteractions = listOf(MigrationHandler(chatDaoImpl), ImplicitPartyHandler(partyDaoImpl))

    val interactions: MutableList<Interaction> = mutableListOf(
        StartCommand(),
        ListCommand(partyDaoImpl),
        PartyCommand(partyDaoImpl),
        DeleteCommand(partyDaoImpl),
        ClearCommand(),
        CreateCommand(partyDaoImpl),
        AliasCommand(partyDaoImpl),
        ChangeCommand(partyDaoImpl),
        AddCommand(partyDaoImpl),
        RemoveCommand(partyDaoImpl),
        FeedbackCommand(),
        CallbackDispatcher(partyDaoImpl, chatDaoImpl),
        EveryMessageProcessor(everyMessageInteractions),
    )

    val commands = interactions.mapNotNull { it as? Command }.toMutableList()

    val helpCommand = HelpCommand(commands.associate { it.command.removePrefix("/") to it.helpMessage })
    commands += helpCommand
    interactions.add(helpCommand)

    interactions.forEach { with(it) { apply() } }
    setMyCommands(commands.map { it.toBotCommand() })
}
