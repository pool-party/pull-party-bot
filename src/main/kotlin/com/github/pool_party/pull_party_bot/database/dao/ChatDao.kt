package com.github.pool_party.pull_party_bot.database.dao

import com.github.pool_party.pull_party_bot.database.ChatCache
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

    override fun getRude(chatId: Long): Boolean = ChatCache[chatId].isRude

    override fun setRude(chatId: Long, newMode: Boolean): Boolean =
        loggingTransaction("setRude($chatId, $newMode)") {

            val chat = ChatCache[chatId]
            val oldMode = chat.isRude

            chat.isRude = newMode

            oldMode != newMode
        }

    override fun clear(chatId: Long): Unit =
        loggingTransaction("clear($chatId)") { ChatCache[chatId].run { aliases.forEach { it.delete() } } }

    override fun migrate(oldChatId: Long, newChatId: Long): Unit =
        loggingTransaction("migrate($oldChatId, $newChatId)") {
            Chats.update({ Chats.id eq oldChatId }) { it[id] = newChatId }
            ChatCache.remove(oldChatId)
        }
}
