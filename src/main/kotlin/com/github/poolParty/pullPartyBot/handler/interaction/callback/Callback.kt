package com.github.poolParty.pullPartyBot.handler.interaction.callback

import com.elbekd.bot.Bot
import com.elbekd.bot.types.CallbackQuery
import com.github.poolParty.pullPartyBot.database.dao.ChatDao
import com.github.poolParty.pullPartyBot.database.dao.PartyDao
import com.github.poolParty.pullPartyBot.handler.interaction.Interaction
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.decodeFromByteArray
import mu.two.KotlinLogging

@Serializable
sealed class Callback {

    val encoded: String
        get() = ProtoBuf.encodeToByteArray(this).asSequence().map { it.toInt().toChar() }.joinToString("")

    abstract suspend fun Bot.process(callbackQuery: CallbackQuery, partyDao: PartyDao, chatDao: ChatDao)

    companion object {
        fun of(string: String) = try {
            ProtoBuf.decodeFromByteArray<Callback>(string.map { it.code.toByte() }.toByteArray())
        } catch (e: SerializationException) {
            null
        }
    }
}

class CallbackDispatcher(private val partyDao: PartyDao, private val chatDao: ChatDao) : Interaction {

    private val logger = KotlinLogging.logger {}

    override fun Bot.apply() = onCallbackQuery { callbackQuery ->
        if (callbackQuery.message == null) return@onCallbackQuery

        val callback = callbackQuery.data?.let { data -> Callback.of(data) }

        logger.info { "callback ${callbackQuery.from.username}@${callbackQuery.message?.chat?.title}: $callback" }

        if (callback == null) return@onCallbackQuery

        with(callback) {
            try {
                process(callbackQuery, partyDao, chatDao)
            } catch (e: Throwable) {
                logger.error { "Callback processing failed:\n${e.stackTraceToString()}" }
            }
        }
    }
}
