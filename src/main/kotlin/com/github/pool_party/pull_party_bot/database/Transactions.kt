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

fun listCommandTransaction(id: Long): String =
    transaction {
        Party.find { Parties.chatId.eq(id) }
            .map { it.name }
            .joinToString("\n")
    }

fun partyCommandTransaction(id: Long, partyName: String): String? =
    transaction { Party.find(id, partyName)?.users }

fun deleteCommandTransaction(id: Long, partyName: String): Boolean =
    transaction {
        val party = Party.find(id, partyName)
        party?.delete()

        party != null
    }

fun createCommandTransaction(id: Long, partyName: String, userList: List<String>): Boolean =
    transaction {
        if (Party.find(id, partyName) != null) {
            return@transaction false
        }

        val rudeMode = Parties.select { Parties.chatId.eq(id) }
            .firstOrNull()?.get(Parties.isRude) ?: false //Will be better after DataBase update

        Party.new {
            name = partyName
            isRude = rudeMode
            chatId = id
            users = userList.joinToString(" ")
        }

        true
    }

fun updateCommandTransaction(id: Long, partyName: String, userList: List<String>): Boolean =
    transaction {
        val party = Party.find(id, partyName) ?: return@transaction false
        party.users = userList.joinToString(" ")

        true
    }

fun rudeCommandTransaction(id: Long, newMode: Boolean): Boolean =
    transaction {
        val oldMode = Parties.select { Parties.chatId.eq(id) }.first()[Parties.isRude]

        Parties.update({ Parties.chatId.eq(id) }) { it[Parties.isRude] = newMode }

        oldMode != newMode
    }
