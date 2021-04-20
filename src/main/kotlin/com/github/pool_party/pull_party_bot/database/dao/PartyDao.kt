package com.github.pool_party.pull_party_bot.database.dao

import com.github.pool_party.pull_party_bot.database.Alias
import com.github.pool_party.pull_party_bot.database.Chat
import com.github.pool_party.pull_party_bot.database.Party
import com.github.pool_party.pull_party_bot.database.loggingTransaction
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.joda.time.DateTime

enum class AliasCreationResult {
    SUCCESS, NAME_TAKEN, NO_PARTY
}

interface PartyDao {

    fun getAll(chatId: Long): List<Alias>

    /**
     * Returns the party alias that is being used most rarely,
     * if there is some and it hasn't been used for time set in configuration.
     */
    fun getTopLost(chatId: Long): Alias?

    fun getByPartyId(partyId: Int): Party?

    fun getByChatIdAndName(chatId: Long, partyName: String): String?

    /**
     * @return true on success, false if partyName is already taken.
     */
    fun create(chatId: Long, partyName: String, userList: List<String>): Boolean

    fun createAlias(chatId: Long, aliasName: String, partyName: String): AliasCreationResult

    /**
     * @return true on success, false if party with partyName wasn't found.
     */
    fun addUsers(chatId: Long, partyName: String, userList: List<String>): Boolean

    fun removeUsers(chatId: Long, partyName: String, userList: List<String>): Boolean

    fun changeUsers(chatId: Long, partyName: String, userList: List<String>): Boolean

    fun delete(chatId: Long, partyName: String): Boolean

    fun delete(aliasId: Int): String?

    // TODO deleteParty
}

class PartyDaoImpl : PartyDao {

    private val cache = mutableMapOf<Pair<Long, String>, String?>()

    private fun invalidateCache(chatId: Long, partyName: String) {
        cache.remove(chatId to partyName)
    }

    override fun getAll(chatId: Long): List<Alias> = loggingTransaction("getAll($chatId)") {
        Chat.findById(chatId)?.aliases?.toList() ?: emptyList()
    }

    override fun getTopLost(chatId: Long) = loggingTransaction("getTopLost($chatId)") {
        Alias.topLost(chatId)
    }

    override fun getByPartyId(partyId: Int) = loggingTransaction("getById($partyId)") { Party.findById(partyId) }

    override fun getByChatIdAndName(chatId: Long, partyName: String): String? {
        val users = cache.getOrPut(chatId to partyName) {
            loggingTransaction("getByIdAndName($chatId, $partyName)") {
                Alias.find(chatId, partyName)?.users
            }
        }
        GlobalScope.launch {
            loggingTransaction("updateLastUse($chatId, $partyName)") {
                Alias.find(chatId, partyName)?.run {
                    lastUse = DateTime.now()
                }
            }
        }
        return users
    }

    override fun create(chatId: Long, partyName: String, userList: List<String>): Boolean {
        val newParty = loggingTransaction("createParty($chatId, $partyName, $userList)") {
            if (Alias.find(chatId, partyName) != null) {
                return@loggingTransaction null
            }

            Party.new { users = userList.joinToString(" ") }
        } ?: return false

        return loggingTransaction("createParty($chatId, $partyName, $userList)") {
            if (Alias.find(chatId, partyName) != null) {
                return@loggingTransaction false
            }

            Alias.new {
                chat = getChat(chatId)
                party = newParty
                name = partyName
            }

            true
        }
    }

    override fun createAlias(chatId: Long, aliasName: String, partyName: String): AliasCreationResult =
        loggingTransaction("createAlias($chatId, $aliasName, $partyName)") {
            if (Alias.find(chatId, aliasName) != null) {
                return@loggingTransaction AliasCreationResult.NAME_TAKEN
            }

            val oldAlias = Alias.find(chatId, partyName) ?: return@loggingTransaction AliasCreationResult.NO_PARTY

            Alias.new {
                party = oldAlias.party
                chat = oldAlias.chat
                name = aliasName
            }

            AliasCreationResult.SUCCESS
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

            val alias = Alias.find(chatId, partyName) ?: return@loggingTransaction false
            val count = alias.party.aliases.count()

            alias.delete()

            if (count == 1) {
                alias.party.delete()
            }

            true
        }

    override fun delete(aliasId: Int): String? = loggingTransaction("delete($aliasId)") {
        val alias = Alias.findById(aliasId) ?: return@loggingTransaction null

        invalidateCache(alias.chat.id.value, alias.name)

        alias.delete()
        alias.name
    }

    private fun changeUsers(
        chatId: Long,
        partyName: String,
        transform: (MutableSet<String>) -> Collection<String>
    ): Boolean = loggingTransaction("changeUsers($chatId, $partyName)") {
        invalidateCache(chatId, partyName)

        val party = Alias.find(chatId, partyName) ?: return@loggingTransaction false
        val newUsers = transform(party.users.split(' ').toMutableSet())
        if (newUsers.isEmpty() || newUsers.singleOrNull() == "@${party.name}") {
            return@loggingTransaction false
        }

        party.users = newUsers.joinToString(" ")

        true
    }

    private fun getChat(chatId: Long) = Chat.findById(chatId) ?: Chat.new(chatId) {}
}
