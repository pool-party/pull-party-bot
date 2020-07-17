package com.github.pool_party.pull_party_bot.database

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

fun selectParty(id: Long, partyName: String) =
    Parties.select { Parties.chatId.eq(id) and Parties.name.eq(partyName) }

fun createCommandTransaction(id: Long, partyName: String, userList: List<String>): Boolean =
    transaction {
        selectParty(id, partyName).empty().also {
            if (it) {
                Parties.insert {
                    it[name] = partyName
                    it[chatId] = id
                    it[users] = userList.joinToString(" ")
                }
            }
        }
    }

fun partyCommandTransaction(id: Long, partyName: String): String? =
    transaction {
        selectParty(id, partyName).firstOrNull()?.get(Parties.users)
    }

fun deleteCommandTransaction(id: Long, partyName: String): Boolean =
    transaction {
        selectParty(id, partyName).empty().not().also {
            if (it) {
                Parties.deleteWhere { Parties.chatId.eq(id) and Parties.name.eq(partyName) }
            }
        }
    }

fun listCommandTransaction(id: Long): String =
    transaction {
        Parties.select { Parties.chatId.eq(id) }.map { it[Parties.name] }.joinToString("\n")
    }

fun updateCommandTransaction(id: Long, partyName: String, userList: List<String>): Boolean =
    transaction {
        if (selectParty(id, partyName).empty()) {
            return@transaction false
        }

        Parties.update({ Parties.chatId.eq(id) and Parties.name.eq(partyName) }) {
            it[Parties.users] = userList.joinToString(" ")
        } > 0
    }
