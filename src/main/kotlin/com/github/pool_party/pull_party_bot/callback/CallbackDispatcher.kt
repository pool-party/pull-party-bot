package com.github.pool_party.pull_party_bot.callback

import com.github.pool_party.flume.interaction.callback.AbstractCallbackDispatcher
import com.github.pool_party.flume.interaction.callback.Callback
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

@Serializable
sealed class CallbackData {
    abstract val partyId: Int

    val encoded: String
        get() = ProtoBuf.encodeToByteArray(this).joinToString("") { it.toInt().toChar().toString() }

    companion object {
        fun of(string: String) =
            ProtoBuf.decodeFromByteArray<CallbackData>(string.map { it.code.toByte() }.toByteArray())
    }
}

class CallbackDispatcher(vararg callbacks: Callback<CallbackData>) :
    AbstractCallbackDispatcher<CallbackData>(callbacks.toList()) {

    override fun getCallbackData(data: String) = CallbackData.of(data)
}
