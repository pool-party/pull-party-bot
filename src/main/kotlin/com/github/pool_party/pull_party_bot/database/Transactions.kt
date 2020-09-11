package com.github.pool_party.pull_party_bot.database

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
    }
}

fun listCommandTransaction(id: Long): String =
    transaction {
        Chat.findById(id)
            ?.run { parties.asSequence().map { "${it.name}: ${it.users.replace("@", "")}" }
                .joinToString("\n") } ?: ""
    }

fun partyCommandTransaction(id: Long, partyName: String): String? = transaction { Party.find(id, partyName)?.users }

fun deleteCommandTransaction(id: Long, partyName: String): Boolean =
    transaction {
        val party = Party.find(id, partyName)
        party?.delete()

        party != null
    }

fun clearCommandTransaction(id: Long) = transaction { Chat.findById(id)?.run { parties.forEach { it.delete() } } }

fun createCommandTransaction(id: Long, partyName: String, userList: List<String>): Boolean =
    transaction {
        val curChat = Chat.findById(id) ?: Chat.new(id) {}

        if (Party.find(id, partyName) != null) {
            return@transaction false
        }

        Party.new {
            name = partyName
            chat = curChat
            users = userList.joinToString(" ")
        }

        true
    }

fun changeCommandTransaction(id: Long, partyName: String, userList: List<String>): Boolean =
    transaction {
        val party = Party.find(id, partyName) ?: return@transaction false
        party.users = userList.joinToString(" ")

        true
    }

fun rudeCommandTransaction(id: Long, newMode: Boolean): Boolean =
    transaction {
        val chat = Chat.findById(id) ?: Chat.new(id) {}
        val oldMode = chat.isRude

        chat.isRude = newMode

        oldMode != newMode
    }

fun rudeCheckTransaction(id: Long): Boolean = transaction { Chat.findById(id)?.isRude ?: false }
