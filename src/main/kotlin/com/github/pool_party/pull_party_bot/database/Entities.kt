package com.github.pool_party.pull_party_bot.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*

class Party(id: EntityID<Int>) : IntEntity(id) {

    var name by Parties.name
    var isRude by Parties.isRude
    var chatId by Parties.chatId
    var users by Parties.users

    companion object : IntEntityClass<Party>(Parties) {
        fun find(chatId: Long, partyName: String): Party? =
            find { Parties.chatId.eq(chatId) and Parties.name.eq(partyName) }.firstOrNull()
    }
}
