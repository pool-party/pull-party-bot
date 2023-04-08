package com.github.poolParty.pullPartyBot.database.dao

import com.github.poolParty.pullPartyBot.database.Alias
import com.github.poolParty.pullPartyBot.database.AliasCache
import com.github.poolParty.pullPartyBot.database.ChatCache
import com.github.poolParty.pullPartyBot.database.Party
import com.github.poolParty.pullPartyBot.database.PartyAliasesCache
import com.github.poolParty.pullPartyBot.database.PartyCache
import com.github.poolParty.pullPartyBot.database.loggingTransaction
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
     * if there is some, and it hasn't been used for time set in configuration.
     */
    fun getTopLost(chatId: Long): Alias?

    fun getByPartyId(partyId: Int): Party?

    fun getAliasByChatIdAndName(chatId: Long, aliasName: String): Alias?

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

    fun deleteNode(partyId: Int): Boolean
}

class PartyDaoImpl : PartyDao {

    override fun getAll(chatId: Long) = AliasCache[chatId].values.toList()

    override fun getTopLost(chatId: Long) = loggingTransaction("getTopLost($chatId)") { Alias.topLost(chatId) }

    override fun getByPartyId(partyId: Int) = PartyCache[partyId]

    override fun getAliasByChatIdAndName(chatId: Long, aliasName: String): Alias? {
        GlobalScope.launch {
            loggingTransaction("updateLastUse($chatId, $aliasName)") {
                Alias.find(chatId, aliasName)?.run { lastUse = DateTime.now() }
            }
        }
        return AliasCache[chatId][aliasName]
    }

    override fun getByChatIdAndName(chatId: Long, partyName: String): String? =
        getAliasByChatIdAndName(chatId, partyName)?.users

    override fun create(chatId: Long, partyName: String, userList: List<String>): Boolean {
        val newParty = loggingTransaction("createParty($chatId, $partyName, $userList)") {
            if (getAliasByChatIdAndName(chatId, partyName) != null) {
                return@loggingTransaction null
            }

            Party.new { users = userList.joinToString(" ") }
        } ?: return false

        PartyCache[newParty.id.value] = newParty

        return loggingTransaction("createParty($chatId, $partyName, $userList)") {
            if (getAliasByChatIdAndName(chatId, partyName) != null) {
                return@loggingTransaction false
            }

            val alias = Alias.new {
                chat = ChatCache[chatId]
                party = newParty
                name = partyName
            }

            AliasCache[chatId][partyName] = alias

            true
        }
    }

    override fun createAlias(chatId: Long, aliasName: String, partyName: String): AliasCreationResult =
        loggingTransaction("createAlias($chatId, $aliasName, $partyName)") {
            if (getAliasByChatIdAndName(chatId, aliasName) != null) {
                return@loggingTransaction AliasCreationResult.NAME_TAKEN
            }

            val oldAlias = getAliasByChatIdAndName(chatId, partyName)
                ?: return@loggingTransaction AliasCreationResult.NO_PARTY

            val alias = Alias.new {
                party = oldAlias.party
                chat = oldAlias.chat
                name = aliasName
            }

            AliasCache[chatId][aliasName] = alias
            PartyAliasesCache[oldAlias.party.id.value].add(alias)

            AliasCreationResult.SUCCESS
        }

    override fun addUsers(chatId: Long, partyName: String, userList: List<String>): Boolean =
        changeUsers(chatId, partyName) { it.also { it.addAll(userList) } }

    override fun removeUsers(chatId: Long, partyName: String, userList: List<String>): Boolean =
        changeUsers(chatId, partyName) { it.also { it.removeAll(userList.toSet()) } }

    override fun changeUsers(chatId: Long, partyName: String, userList: List<String>): Boolean =
        changeUsers(chatId, partyName) { userList }

    override fun delete(chatId: Long, partyName: String): Boolean =
        loggingTransaction("delete($chatId, $partyName)") {
            val alias = Alias.find(chatId, partyName) ?: return@loggingTransaction false
            val count = alias.party.aliases.count()

            alias.delete()
            AliasCache[chatId].remove(partyName)

            if (count == 1) {
                alias.party.delete()
                PartyCache.remove(alias.party.id.value)
            }

            true
        }

    override fun delete(aliasId: Int): String? = loggingTransaction("delete($aliasId)") {
        val alias = Alias.findById(aliasId) ?: return@loggingTransaction null

        alias.delete()

        val name = alias.name
        AliasCache[alias.chat.id.value].remove(name)
        PartyAliasesCache.remove(alias.party.id.value)
        name
    }

    override fun deleteNode(partyId: Int) = loggingTransaction("deleteNode($partyId)") {
        val party = Party.findById(partyId) ?: return@loggingTransaction false

        party.aliases.forEach { AliasCache[it.chat.id.value].remove(it.name) }

        party.delete()
        PartyCache.remove(partyId)
        PartyAliasesCache.remove(partyId)

        true
    }

    private fun changeUsers(
        chatId: Long,
        partyName: String,
        transform: (MutableSet<String>) -> Collection<String>,
    ): Boolean = loggingTransaction("changeUsers($chatId, $partyName)") {
        val alias = getAliasByChatIdAndName(chatId, partyName) ?: return@loggingTransaction false

        val newUsers = transform(alias.users.split(' ').toMutableSet())
        if (newUsers.isEmpty() || newUsers.singleOrNull() == "@${alias.name}") {
            return@loggingTransaction false
        }

        alias.users = newUsers.joinToString(" ")

        true
    }
}
