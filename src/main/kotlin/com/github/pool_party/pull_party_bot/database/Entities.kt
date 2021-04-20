package com.github.pool_party.pull_party_bot.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class Party(id: EntityID<Int>) : IntEntity(id) {

    var users by Parties.users
    private val privateAliases by Alias referrersOn Aliases.partyId

    val aliases
        get() = transaction { privateAliases.toList() }

    companion object : IntEntityClass<Party>(Parties)
}

class Alias(id: EntityID<Int>) : IntEntity(id) {

    var chat by Chat referencedOn Aliases.chatId
    var name by Aliases.name
    var lastUse by Aliases.lastUse

    private var privateParty by Party referencedOn Aliases.partyId

    var party
        get() = transaction { privateParty }
        set(value) = transaction { privateParty = value }

    var users
        get() = transaction { party.users }
        set(value) = transaction { party.users = value }

    companion object : IntEntityClass<Alias>(Aliases) {
        fun find(chatId: Long, partyName: String): Alias? =
            find { Aliases.chatId.eq(chatId) and Aliases.name.eq(partyName) }.firstOrNull()

        fun topLost(chatId: Long): Alias? =
            find { Aliases.chatId.eq(chatId) }
                .orderBy(Aliases.lastUse to SortOrder.ASC)
                .limit(1)
                .toList()
                .singleOrNull()
    }
}

class Chat(id: EntityID<Long>) : LongEntity(id) {

    var isRude by Chats.isRude
    val aliases by Alias referrersOn Aliases.chatId

    companion object : LongEntityClass<Chat>(Chats)
}
