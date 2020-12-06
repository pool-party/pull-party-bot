package com.github.pool_party.pull_party_bot.database

import com.github.pool_party.pull_party_bot.Configuration
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

fun initDB() {
    Database.connect(
        Configuration.DATABASE_URL,
        user = Configuration.DATABASE_USERNAME,
        password = Configuration.DATABASE_PASSWORD
    )

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(Chats, Parties)
    }
}

fun listCommandTransaction(id: Long): List<Party> = transaction { Chat.findById(id)?.parties?.toList() } ?: emptyList()

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

fun addUsersCommandTransaction(id: Long, partyName: String, userList: List<String>): Boolean =
    changeUsersTransaction(id, partyName) { it.also { it.addAll(userList) } }

fun removeUsersCommandTransaction(id: Long, partyName: String, userList: List<String>): Boolean =
    changeUsersTransaction(id, partyName) { it.also { it.removeAll(userList) } }

fun changeCommandTransaction(id: Long, partyName: String, userList: List<String>): Boolean =
    changeUsersTransaction(id, partyName) { userList }

private fun changeUsersTransaction(
    id: Long,
    partyName: String,
    transform: (MutableSet<String>) -> Collection<String>
): Boolean =
    transaction {
        val party = Party.find(id, partyName) ?: return@transaction false
        val newUsers = transform(party.users.split(' ').toMutableSet())
        if (newUsers.isEmpty() || newUsers.singleOrNull() == "@${party.name}") {
            return@transaction false
        }

        party.users = newUsers.joinToString(" ")

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
