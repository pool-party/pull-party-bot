package com.github.pool_party.pull_party_bot

import com.github.pool_party.pull_party_bot.callback.CallbackDispatcher
import com.github.pool_party.pull_party_bot.callback.DeleteNodeSuggestionCallback
import com.github.pool_party.pull_party_bot.callback.DeleteSuggestionCallback
import com.github.pool_party.pull_party_bot.callback.PingCallback
import com.github.pool_party.pull_party_bot.command.AddCommand
import com.github.pool_party.pull_party_bot.command.AliasCommand
import com.github.pool_party.pull_party_bot.command.ChangeCommand
import com.github.pool_party.pull_party_bot.command.ClearCommand
import com.github.pool_party.pull_party_bot.command.CreateCommand
import com.github.pool_party.pull_party_bot.command.DeleteCommand
import com.github.pool_party.pull_party_bot.command.FeedbackCommand
import com.github.pool_party.pull_party_bot.command.ListCommand
import com.github.pool_party.pull_party_bot.every.MigrationHandler
import com.github.pool_party.pull_party_bot.command.PartyCommand
import com.github.pool_party.pull_party_bot.command.RemoveCommand
import com.github.pool_party.pull_party_bot.command.RudeCommand
import com.github.pool_party.pull_party_bot.command.StartCommand
import com.github.pool_party.pull_party_bot.database.dao.ChatDaoImpl
import com.github.pool_party.pull_party_bot.database.dao.PartyDaoImpl
import com.github.pool_party.pull_party_bot.database.initDB
import com.github.pool_party.pull_party_bot.every.PullPartyHandler
import com.github.pool_party.telegram_bot_utils.bot.BotBuilder
import com.github.pool_party.telegram_bot_utils.interaction.Interaction

val partyDaoImpl = PartyDaoImpl()

val chatDaoImpl = ChatDaoImpl()

val callbacks = listOf(
    DeleteNodeSuggestionCallback(partyDaoImpl),
    DeleteSuggestionCallback(partyDaoImpl),
    PingCallback(partyDaoImpl)
)

val pullPartyEveryMessageInteractions = listOf(MigrationHandler(chatDaoImpl), PullPartyHandler(partyDaoImpl))

val callbackDispatcher = CallbackDispatcher(callbacks)

val pullPartyInteractions: List<List<Interaction>> = listOf(
    listOf(
        StartCommand(),
        ListCommand(partyDaoImpl),
        ClearCommand(chatDaoImpl),
    ),
    listOf(
        PartyCommand(partyDaoImpl),
        DeleteCommand(partyDaoImpl),
    ),
    listOf(
        CreateCommand(partyDaoImpl),
        AliasCommand(partyDaoImpl),
        ChangeCommand(partyDaoImpl),
        AddCommand(partyDaoImpl),
        RemoveCommand(partyDaoImpl),
    ),
    listOf(
        RudeCommand(chatDaoImpl),
        FeedbackCommand(),
    ),
    listOf(callbackDispatcher),
)

fun main() {
    try {
        initDB()
    } catch (e: Exception) {
        println(e.message)
        return
    }

    BotBuilder(Configuration).apply {
        everyMessageInteractions = pullPartyEveryMessageInteractions
        interactions = pullPartyInteractions
    }.start()
}
