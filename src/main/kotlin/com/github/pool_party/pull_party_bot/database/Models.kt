package com.github.pool_party.pull_party_bot.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable

object Parties : IntIdTable() {
    val name = varchar("name", 50)
    val chatId = reference("chat_id", Chats)
    val users = text("users")

    init {
        index(false, name, chatId)
        uniqueIndex(chatId, name)
    }
}

object Chats : LongIdTable() {
    override val id = long("chat_id").entityId()
    val isRude = bool("is_rude").default(false)
}
