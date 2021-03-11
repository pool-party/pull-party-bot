package com.github.pool_party.pull_party_bot.database.dao

class ChatInMemoryDao(private val partyDao: PartyDao) : ChatDao {

    private val chats = mutableMapOf<Long, Boolean>()

    override fun getRude(chatId: Long): Boolean = chats[chatId] ?: false

    override fun setRude(chatId: Long, newMode: Boolean): Boolean {
        val previous = getRude(chatId)
        chats[chatId] = newMode
        return previous != newMode
    }

    override fun clear(chatId: Long) = partyDao.getAll(chatId).forEach { partyDao.delete(chatId, it.name) }

    override fun migrate(oldChatId: Long, newChatId: Long) {
        val previous = chats[oldChatId] ?: return
        chats[newChatId] = previous
        chats.remove(oldChatId)
    }
}
