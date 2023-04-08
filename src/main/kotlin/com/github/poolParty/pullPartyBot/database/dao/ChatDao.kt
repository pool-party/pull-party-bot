package com.github.poolParty.pullPartyBot.database.dao

import com.github.poolParty.pullPartyBot.database.*
import org.jetbrains.exposed.sql.update

interface ChatDao {

    fun clear(chatId: Long)

    fun migrate(oldChatId: Long, newChatId: Long)
}

class ChatDaoImpl : ChatDao {

    private fun removeFromCache(chatId: Long) {
        ChatCache[chatId].aliases.forEach {
            PartyCache -= it.partyId.value
            PartyAliasesCache -= it.partyId.value
        }
        AliasCache -= chatId
        ChatCache -= chatId
    }

    override fun clear(chatId: Long): Unit =
        loggingTransaction("clear($chatId)") {
            ChatCache[chatId].run { aliases.forEach { it.delete() } }
            removeFromCache(chatId)
        }

    override fun migrate(oldChatId: Long, newChatId: Long): Unit =
        loggingTransaction("migrate($oldChatId, $newChatId)") {
            Chats.update({ Chats.id eq oldChatId }) { it[id] = newChatId }
            removeFromCache(oldChatId)
        }
}
