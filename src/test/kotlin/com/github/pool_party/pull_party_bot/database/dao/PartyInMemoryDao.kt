package com.github.pool_party.pull_party_bot.database.dao

import com.github.pool_party.pull_party_bot.database.Parties
import com.github.pool_party.pull_party_bot.database.Party
import io.mockk.every
import io.mockk.mockk

class PartyInMemoryDao : PartyDao {

    private var partyId = 0
    private val parties = mutableMapOf<Long, MutableMap<String, Party>>()

    private fun party(partyName: String, userList: List<String>): Party {
        val party = mockk<Party>()
        val newPartyId = partyId++
        every { party.id.value } returns newPartyId
        every { party.name } returns partyName
        every { party.users } returns userList.joinToString(" ")
        return party
    }

    override fun getAll(chatId: Long): List<Party> = parties[chatId]?.values?.toList() ?: emptyList()

    override fun getTopLost(chatId: Long): Party? =
        parties[chatId]?.values?.minByOrNull { Parties.lastUse }

    override fun getById(partyId: Int): Party? =
        parties.values.map { it.values.find { party -> party.id.value == partyId } }.singleOrNull()

    override fun getByIdAndName(chatId: Long, partyName: String): String? = parties[chatId]?.get(partyName)?.users

    override fun create(chatId: Long, partyName: String, userList: List<String>): Boolean {
        if (parties[chatId]?.get(partyName) != null) return false

        val currentParty = party(partyName, userList)
        parties.getOrPut(chatId) { mutableMapOf() }.set(partyName, currentParty)
        return true
    }

    override fun addUsers(chatId: Long, partyName: String, userList: List<String>): Boolean {
        val currentParty = parties[chatId]?.get(partyName) ?: return false
        parties[chatId]!![partyName] = party(partyName, currentParty.users.split(' ') + userList)
        return true
    }

    override fun removeUsers(chatId: Long, partyName: String, userList: List<String>): Boolean {
        val currentParty = parties[chatId]?.get(partyName) ?: return false
        parties[chatId]!!.set(partyName, party(partyName, currentParty.users.split(' ') - userList))
        return true
    }

    override fun changeUsers(chatId: Long, partyName: String, userList: List<String>): Boolean {
        if (parties[chatId]?.get(partyName) == null) return false
        parties[chatId]!!.set(partyName, party(partyName, userList))
        return true
    }

    override fun delete(chatId: Long, partyName: String): Boolean {
        if (chatId !in parties || partyName !in parties[chatId]!!) return false
        parties[chatId]!!.remove(partyName)
        return true
    }

    override fun delete(partyId: Int): String? {
        val party = getById(partyId) ?: return null
        parties.values.map { it.values.remove(party) }
        return party.name
    }
}
