package com.github.pool_party.pull_party_bot.database.dao

import com.github.pool_party.pull_party_bot.database.Chat
import com.github.pool_party.pull_party_bot.database.Party
import com.github.pool_party.pull_party_bot.database.loggingTransaction

interface PartyDao {

    fun getAll(chatId: Long): List<Party>

    fun getByIdAndName(chatId: Long, partyName: String): String?

    fun create(chatId: Long, partyName: String, userList: List<String>): Boolean

    fun addUsers(chatId: Long, partyName: String, userList: List<String>): Boolean

    fun removeUsers(chatId: Long, partyName: String, userList: List<String>): Boolean

    fun changeUsers(chatId: Long, partyName: String, userList: List<String>): Boolean

    fun delete(chatId: Long, partyName: String): Boolean
}

class PartyDaoImpl : PartyDao {

    private val partyUsersCache = mutableMapOf<Pair<Long, String>, String?>()

    private fun invalidateCache(chatId: Long, partyName: String) {
        partyUsersCache.remove(chatId to partyName)
    }

    override fun getAll(chatId: Long): List<Party> =
        loggingTransaction("getAll($chatId)") { Chat.findById(chatId)?.parties?.toList() } ?: emptyList()

    override fun getByIdAndName(chatId: Long, partyName: String): String? =
        partyUsersCache.getOrPut(chatId to partyName) {
            loggingTransaction("getByIdAndName($chatId, $partyName)") { Party.find(chatId, partyName)?.users }
        }

    override fun create(chatId: Long, partyName: String, userList: List<String>): Boolean =
        loggingTransaction("create($chatId, $partyName, $userList)") {
            val curChat = Chat.findById(chatId) ?: Chat.new(chatId) {}

            if (Party.find(chatId, partyName) != null) {
                return@loggingTransaction false
            }

            Party.new {
                name = partyName
                chat = curChat
                users = userList.joinToString(" ")
            }

            true
        }

    override fun addUsers(chatId: Long, partyName: String, userList: List<String>): Boolean =
        changeUsers(chatId, partyName) { it.also { it.addAll(userList) } }

    override fun removeUsers(chatId: Long, partyName: String, userList: List<String>): Boolean =
        changeUsers(chatId, partyName) { it.also { it.removeAll(userList) } }

    override fun changeUsers(chatId: Long, partyName: String, userList: List<String>): Boolean =
        changeUsers(chatId, partyName) { userList }

    override fun delete(chatId: Long, partyName: String): Boolean =
        loggingTransaction("delete($chatId, $partyName)") {
            invalidateCache(chatId, partyName)

            val party = Party.find(chatId, partyName)
            party?.delete()

            party != null
        }

    private fun changeUsers(
        chatId: Long,
        partyName: String,
        transform: (MutableSet<String>) -> Collection<String>
    ): Boolean =
        loggingTransaction("changeUsers($chatId, $partyName)") {
            invalidateCache(chatId, partyName)

            val party = Party.find(chatId, partyName) ?: return@loggingTransaction false
            val newUsers = transform(party.users.split(' ').toMutableSet())
            if (newUsers.isEmpty() || newUsers.singleOrNull() == "@${party.name}") {
                return@loggingTransaction false
            }

            party.users = newUsers.joinToString(" ")

            true
        }
}
