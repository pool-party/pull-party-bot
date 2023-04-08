package com.github.poolParty.pullPartyBot.handler.interaction.callback

import com.elbekd.bot.Bot
import com.elbekd.bot.types.CallbackQuery
import com.github.poolParty.pullPartyBot.database.dao.ChatDao
import com.github.poolParty.pullPartyBot.database.dao.PartyDao
import com.github.poolParty.pullPartyBot.handler.interaction.Interaction
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.two.KotlinLogging

@Serializable
sealed class CallbackData {

    val encoded: String
        get() = Json.encodeToString(this)

    abstract suspend fun Bot.process(callbackQuery: CallbackQuery, partyDao: PartyDao, chatDao: ChatDao)

    companion object {
        fun of(string: String) = try {
            Json.decodeFromString<CallbackData>(string)
        } catch (e: SerializationException) {
            null
        }
    }
}

class CallbackDispatcher(private val partyDao: PartyDao, private val chatDao: ChatDao) : Interaction {

    private val logger = KotlinLogging.logger {}

    override fun Bot.apply() = onCallbackQuery { callbackQuery ->
        if (callbackQuery.message == null) return@onCallbackQuery

        val callbackData = callbackQuery.data?.let { data -> CallbackData.of(data) }

        logger.info { "callback ${callbackQuery.from.username}@${callbackQuery.message?.chat?.title}: $callbackData" }

        if (callbackData == null) return@onCallbackQuery

        with(callbackData) {
            try {
                process(callbackQuery, partyDao, chatDao)
            } catch (e: Throwable) {
                logger.error { "Callback processing failed:\n${e.stackTraceToString()}" }
            }
        }
    }
}
