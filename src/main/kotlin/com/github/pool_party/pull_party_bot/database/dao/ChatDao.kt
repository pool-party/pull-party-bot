package com.github.pool_party.pull_party_bot.database.dao

import com.github.pool_party.pull_party_bot.database.Chat
import com.github.pool_party.pull_party_bot.database.loggingTransaction

interface ChatDao {

    fun getRude(chatId: Long): Boolean

    fun setRude(chatId: Long, newMode: Boolean): Boolean

    fun clear(chatId: Long)
}

class ChatDaoImpl : ChatDao {

    override fun getRude(chatId: Long): Boolean =
        loggingTransaction("getRude($chatId)") { Chat.findById(chatId)?.isRude ?: false }

    override fun setRude(chatId: Long, newMode: Boolean): Boolean =
        loggingTransaction("setRude($chatId, $newMode)") {
            val chat = Chat.findById(chatId) ?: Chat.new(chatId) {}
            val oldMode = chat.isRude

            chat.isRude = newMode

            oldMode != newMode
        }

    override fun clear(chatId: Long): Unit =
        loggingTransaction("clear($chatId)") { Chat.findById(chatId)?.run { parties.forEach { it.delete() } } }
}
