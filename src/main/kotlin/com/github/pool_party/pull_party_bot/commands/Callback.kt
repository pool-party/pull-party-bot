package com.github.pool_party.pull_party_bot.commands

import com.elbekd.bot.Bot
import com.elbekd.bot.types.CallbackQuery
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.two.KotlinLogging

private val logger = KotlinLogging.logger {}

enum class CallbackAction {
    DELETE, DELETE_NODE, PING
}

@Serializable
data class CallbackData(
    val callbackAction: CallbackAction,
    val partyId: Int,
    val creator: Long? = null,
)

interface Callback {

    val callbackAction: CallbackAction

    suspend fun Bot.process(callbackQuery: CallbackQuery, callbackData: CallbackData)
}

class CallbackDispatcher(val callbacks: Map<CallbackAction, Callback>) : Interaction {

    override fun onMessage(bot: Bot) = bot.onCallbackQuery {
        bot.loggingError {
            logger.info { "callback ${it.from.username}@${it.message?.chat?.title}: ${it.data}" }

            val callbackData = it.data?.let { Json.decodeFromString<CallbackData>(it) } ?: return@loggingError
            val callback = callbacks[callbackData.callbackAction] ?: return@loggingError

            with(callback) { bot.process(it, callbackData) }
        }
    }
}
