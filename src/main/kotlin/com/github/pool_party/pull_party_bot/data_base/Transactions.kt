package com.github.pool_party.pull_party_bot.data_base

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun initDB() {
    Database.connect(
        System.getenv("HEROKU_POSTGRESQL_RED_JDBC_URL"),
        user = System.getenv("HEROKU_POSTGRESQL_RED_JDBC_USERNAME"),
        password = System.getenv("HEROKU_POSTGRESQL_RED_JDBC_PASSWORD")
    )

    transaction {
        addLogger(StdOutSqlLogger)

        SchemaUtils.create(Parties)
    }
}

fun createCommandTransaction(id: Long, partyName: String, userList: List<String>) {
    transaction {
        Parties.insert {
            it[name] = partyName
            it[chatId] = id
            it[users] = userList.joinToString(" ")
        }
    }
}

fun partyCommandTransaction(id: Long, partyName: String): String? =
    transaction {
        Parties.select { Parties.name.eq(partyName) and Parties.chatId.eq(id) }.firstOrNull()?.get(Parties.users)
    }
