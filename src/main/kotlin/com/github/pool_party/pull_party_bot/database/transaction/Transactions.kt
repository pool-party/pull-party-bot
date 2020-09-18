package com.github.pool_party.pull_party_bot.database.transaction

import com.github.pool_party.pull_party_bot.database.ActivatedPacks
import com.github.pool_party.pull_party_bot.database.Chats
import com.github.pool_party.pull_party_bot.database.Parties
import com.github.pool_party.pull_party_bot.database.StickerAliases
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

fun initDB() { // change token for another app
    Database.connect(
        System.getenv("JDBC_DATABASE_URL"),
        user = System.getenv("JDBC_DATABASE_USERNAME"),
        password = System.getenv("JDBC_DATABASE_PASSWORD")
    )

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(Chats, Parties)
        SchemaUtils.create(Chats, Parties, StickerAliases, ActivatedPacks)
    }
}
