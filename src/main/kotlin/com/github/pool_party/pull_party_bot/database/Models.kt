package com.github.pool_party.pull_party_bot.database

import org.jetbrains.exposed.dao.id.IntIdTable

object Parties : IntIdTable() {
    val name = varchar("name", 50)
    val chatId = long("chat_id")
    val users = text("users")
}
