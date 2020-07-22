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

        SchemaUtils.create(Chats, Parties)
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
        if (Chat.find(id) == null) {
            Chat.new {
                chatId = id
                isRude = false
            }
        }

        if (Party.find(id, partyName) != null) {
            return@transaction false
        }

        Party.new {
            name = partyName
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
        val chat = Chat.find(id) ?: Chat.new {
            chatId = id
            isRude = false
        }
        val oldMode = chat.isRude

        chat.isRude = newMode

        oldMode != newMode
    }
