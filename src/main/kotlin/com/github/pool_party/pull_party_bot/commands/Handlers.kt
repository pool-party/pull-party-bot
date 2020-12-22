package com.github.pool_party.pull_party_bot.commands

import com.elbekD.bot.Bot
import com.github.pool_party.pull_party_bot.commands.handlers.AddCommand
import com.github.pool_party.pull_party_bot.commands.handlers.ChangeCommand
import com.github.pool_party.pull_party_bot.commands.handlers.ClearCommand
import com.github.pool_party.pull_party_bot.commands.handlers.CreateCommand
import com.github.pool_party.pull_party_bot.commands.handlers.DeleteCommand
import com.github.pool_party.pull_party_bot.commands.handlers.FeedbackCommand
import com.github.pool_party.pull_party_bot.commands.handlers.HelpCommand
import com.github.pool_party.pull_party_bot.commands.handlers.ImplicitPartyHandler
import com.github.pool_party.pull_party_bot.commands.handlers.ListCommand
import com.github.pool_party.pull_party_bot.commands.handlers.PartyCommand
import com.github.pool_party.pull_party_bot.commands.handlers.RemoveCommand
import com.github.pool_party.pull_party_bot.commands.handlers.RemoveSuggestionCallback
import com.github.pool_party.pull_party_bot.commands.handlers.RudeCommand
import com.github.pool_party.pull_party_bot.commands.handlers.StartCommand
import com.github.pool_party.pull_party_bot.database.dao.ChatDaoImpl
import com.github.pool_party.pull_party_bot.database.dao.PartyDaoImpl

fun Bot.initHandlers() {

    val partyDaoImpl = PartyDaoImpl()

    val chatDaoImpl = ChatDaoImpl()

    // TODO probably use some kind of injections
    val interactions: MutableList<Interaction> = mutableListOf(
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
        FeedbackCommand(),
        ImplicitPartyHandler(partyDaoImpl),
        RemoveSuggestionCallback(partyDaoImpl)
    )

    val commands = interactions.mapNotNull { it as? Command }

    val helpCommand = HelpCommand(commands.associate { it.command.removePrefix("/") to it.helpMessage })
    interactions.add(helpCommand)

    interactions.forEach { it.onMessage(this) }
    setMyCommands(commands.map { it.toBotCommand() })
}
