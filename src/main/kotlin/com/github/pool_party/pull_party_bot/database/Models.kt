package com.github.pool_party.pull_party_bot.database

import org.jetbrains.exposed.dao.id.IntIdTable

object Parties : IntIdTable() {
    val name = varchar("name", 50)
    val chatId = (long("chat_id") references Chats.chatId)
    val users = text("users")
}

object Chats : IntIdTable() {
    val chatId = long("chat_id")
    val isRude = bool("is_rude").default(false)
}
