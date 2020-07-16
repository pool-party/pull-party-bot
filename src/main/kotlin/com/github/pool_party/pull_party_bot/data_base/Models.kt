package com.github.pool_party.pull_party_bot.data_base

import org.jetbrains.exposed.sql.*

object Parties : Table() {
    val name = varchar("name", 50)
    val chatId = long("chat_id")
    val users = text("users")

    override val primaryKey = PrimaryKey(chatId, name, name = "Parties_Name_Chat_Id")
}
