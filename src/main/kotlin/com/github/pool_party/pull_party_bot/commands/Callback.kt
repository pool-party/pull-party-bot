package com.github.pool_party.pull_party_bot.commands

import com.elbekD.bot.Bot
import com.elbekD.bot.types.CallbackQuery
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

enum class CallbackAction {
    DELETE, PING
}

@Serializable
data class CallbackData(val callbackAction: CallbackAction, val partyId: Int)

interface Callback {

    val callbackAction: CallbackAction

    suspend fun processBot(bot: Bot, callbackQuery: CallbackQuery, partyId: Int) = bot.process(callbackQuery, partyId)

    suspend fun Bot.process(callbackQuery: CallbackQuery, partyId: Int)
}

class CallbackDispatcher(val callbacks: Map<CallbackAction, Callback>) : Interaction {

    override fun onMessage(bot: Bot) = bot.onCallbackQuery {
        logger.info { "callback ${it.from.username}@${it.message?.chat?.title}: ${it.data}" }

        val callbackData = it.data?.let { Json.decodeFromString<CallbackData>(it) } ?: return@onCallbackQuery
        val callback = callbacks[callbackData.callbackAction] ?: return@onCallbackQuery

        callback.processBot(bot, it, callbackData.partyId)
    }
}
