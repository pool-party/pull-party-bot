package com.github.pool_party.pull_party_bot.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.jodatime.CurrentDateTime
import org.jetbrains.exposed.sql.jodatime.datetime

object Parties : IntIdTable() {
    val users = text("users")
}

object Aliases : IntIdTable() {
    val partyId = reference("party_id", Parties, ReferenceOption.CASCADE, ReferenceOption.CASCADE)

    // performance boosting denormalization
    val chatId = reference("chat_id", Chats, ReferenceOption.CASCADE, ReferenceOption.CASCADE)

    val name = varchar("name", 50)
    val lastUse = datetime("last_use").defaultExpression(CurrentDateTime())

    init {
        index(false, name, chatId)
        uniqueIndex(chatId, name)
    }
}

object Chats : LongIdTable(columnName = "chat_id") {
    val isRude = bool("is_rude").default(false)
}
