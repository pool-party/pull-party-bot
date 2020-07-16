package com.github.pool_party.pull_party_bot.data_base

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun initDB() { // change token for another app
    Database.connect(
        System.getenv("JDBC_DATABASE_URL"),
        user = System.getenv("JDBC_DATABASE_USERNAME"),
        password = System.getenv("JDBC_DATABASE_PASSWORD")
    )

    transaction {
        addLogger(StdOutSqlLogger)

        SchemaUtils.create(Parties)
    }
}

fun createCommandTransaction(id: Long, partyName: String, userList: List<String>) =
    transaction {
        Parties.insert {
            it[name] = partyName
            it[chatId] = id
            it[users] = userList.joinToString(" ")
        }
    }

fun partyCommandTransaction(id: Long, partyName: String): String? =
    transaction {
        Parties.select { Parties.chatId.eq(id) and Parties.name.eq(partyName) }.firstOrNull()?.get(Parties.users)
    }

fun deleteCommandTransaction(id: Long, partyName: String) =
    transaction {
        Parties.deleteWhere { Parties.chatId.eq(id) and Parties.name.eq(partyName) }
    }

fun listCommandTransaction(id: Long): String? {
    var ans : String? = null
    transaction {
        Parties.select { Parties.chatId.eq(id) }.forEach{
            ans += "\n" + it[Parties.name]
        }
    }
    return ans
}

fun updateCommandTransaction(id: Long, partyName: String, userList: List<String>): Boolean =
    transaction {
        Parties.update ({ Parties.chatId.eq(id) and Parties.name.eq(partyName) }) {
            it[Parties.users] = userList.joinToString(" ")
          }

        return@transaction Parties.select {Parties.chatId.eq(id) and Parties.name.eq(partyName)}.empty()
    }
