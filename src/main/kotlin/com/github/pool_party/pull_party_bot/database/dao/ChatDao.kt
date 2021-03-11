package com.github.pool_party.pull_party_bot.database.dao

import com.github.pool_party.pull_party_bot.database.Chat
import com.github.pool_party.pull_party_bot.database.Chats
import com.github.pool_party.pull_party_bot.database.loggingTransaction
import org.jetbrains.exposed.sql.update

interface ChatDao {

    fun getRude(chatId: Long): Boolean

    fun setRude(chatId: Long, newMode: Boolean): Boolean

    fun clear(chatId: Long)

    fun migrate(oldChatId: Long, newChatId: Long)
}

class ChatDaoImpl : ChatDao {

    private val rudeCache = mutableMapOf<Long, Boolean>()

    private fun invalidateCache(chatId: Long) {
        rudeCache.remove(chatId)
    }

    override fun getRude(chatId: Long): Boolean =
        rudeCache.getOrPut(chatId) {
            loggingTransaction("getRude($chatId)") { Chat.findById(chatId)?.isRude ?: false }
        }

    override fun setRude(chatId: Long, newMode: Boolean): Boolean =
        loggingTransaction("setRude($chatId, $newMode)") {
            invalidateCache(chatId)

            val chat = Chat.findById(chatId) ?: Chat.new(chatId) {}
            val oldMode = chat.isRude

            chat.isRude = newMode

            oldMode != newMode
        }

    override fun clear(chatId: Long): Unit =
        loggingTransaction("clear($chatId)") { Chat.findById(chatId)?.run { parties.forEach { it.delete() } } }

    override fun migrate(oldChatId: Long, newChatId: Long): Unit =
        loggingTransaction("migrate($oldChatId, $newChatId)") {
            invalidateCache(oldChatId)

            Chats.update({ Chats.id eq oldChatId }) {
                it[Chats.id] = newChatId
            }
        }
}
