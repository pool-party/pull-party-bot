package com.github.pool_party.pull_party_bot

import com.github.pool_party.pull_party_bot.callback.CallbackDispatcher
import com.github.pool_party.pull_party_bot.callback.DeleteNodeSuggestionCallback
import com.github.pool_party.pull_party_bot.callback.DeleteSuggestionCallback
import com.github.pool_party.pull_party_bot.callback.PingCallback
import com.github.pool_party.pull_party_bot.command.AddCommand
import com.github.pool_party.pull_party_bot.command.AliasCommand
import com.github.pool_party.pull_party_bot.command.ClearCommand
import com.github.pool_party.pull_party_bot.command.ChangeCommand
import com.github.pool_party.pull_party_bot.command.CreateCommand
import com.github.pool_party.pull_party_bot.command.DeleteCommand
import com.github.pool_party.pull_party_bot.command.FeedbackCommand
import com.github.pool_party.pull_party_bot.command.ImplicitPartyHandler
import com.github.pool_party.pull_party_bot.command.ListCommand
import com.github.pool_party.pull_party_bot.command.MigrationHandler
import com.github.pool_party.pull_party_bot.command.PartyCommand
import com.github.pool_party.pull_party_bot.command.RemoveCommand
import com.github.pool_party.pull_party_bot.command.RudeCommand
import com.github.pool_party.pull_party_bot.command.StartCommand
import com.github.pool_party.pull_party_bot.database.dao.ChatDaoImpl
import com.github.pool_party.pull_party_bot.database.dao.PartyDaoImpl
import com.github.pool_party.telegram_bot_utils.bot.BotBuilder

val partyDaoImpl = PartyDaoImpl()

val chatDaoImpl = ChatDaoImpl()

val callbacks = listOf(
    DeleteNodeSuggestionCallback(partyDaoImpl),
    DeleteSuggestionCallback(partyDaoImpl),
    PingCallback(partyDaoImpl)
)

val messageInteractions = listOf(MigrationHandler(chatDaoImpl), ImplicitPartyHandler(partyDaoImpl))

val commands = listOf(
    StartCommand(),
    ListCommand(partyDaoImpl, chatDaoImpl),
    PartyCommand(partyDaoImpl),
    DeleteCommand(partyDaoImpl),
    ClearCommand(chatDaoImpl),
    CreateCommand(partyDaoImpl, chatDaoImpl),
    AliasCommand(partyDaoImpl, chatDaoImpl),
    ChangeCommand(partyDaoImpl, chatDaoImpl),
    AddCommand(partyDaoImpl, chatDaoImpl),
    RemoveCommand(partyDaoImpl, chatDaoImpl),
    RudeCommand(chatDaoImpl),
    FeedbackCommand(),
)

val callbackDispatcher = CallbackDispatcher(callbacks)

fun main() {
    BotBuilder(Configuration).apply {
        everyMessageInteractions = messageInteractions
        interactions = listOf(commands + callbackDispatcher)
    }.start()
}
