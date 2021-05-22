package com.github.pool_party.pull_party_bot.database

import com.github.pool_party.pull_party_bot.Configuration
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.jetbrains.exposed.sql.select
import java.util.LinkedHashMap

private const val loadFactor = 0.75f

abstract class LruCache<K, V>(capacity: Int) {

    private val storage = LinkedHashMap<K, V>(capacity, loadFactor, true)

    abstract fun supply(key: K): V

    operator fun get(key: K) = storage.getOrPut(key) { supply(key) }

    operator fun set(key: K, value: V) = storage.put(key, value)

    fun remove(key: K) = storage.remove(key)
}

object AliasCache : LruCache<Long, MutableMap<String, Alias>>(Configuration.ALIAS_CACHE_CAPACITY) {

    private val logger = KotlinLogging.logger {}

    override fun supply(key: Long): MutableMap<String, Alias> {
        val aliases = loggingTransaction("Updating alias cache: getAllAliases($key)") {
            Aliases.innerJoin(Parties)
                .select { Aliases.chatId eq key }
                .map {
                    PartyCache[it[Parties.id].value] = Party.wrapRow(it)
                    Alias.wrapRow(it)
                }
        }

        GlobalScope.launch {
            aliases.groupBy { it.partyId.value }.forEach { (k, v) -> PartyAliasesCache[k] = v.toMutableList() }
        }

        logger.info("Updated alias cache for $key with ${aliases.size} aliases")

        return aliases.associateBy { it.name }.toMutableMap()
    }
}

object PartyCache : LruCache<Int, Party?>(Configuration.PARTY_CACHE_CAPACITY) {

    override fun supply(key: Int) = loggingTransaction("Updating party cache: getPartyById($key)") {
        Party.findById(key)
    }
}

object PartyAliasesCache : LruCache<Int, MutableList<Alias>>(Configuration.PARTY_ALIASES_CACHE_CAPACITY) {

    override fun supply(key: Int) = loggingTransaction("Updating party cache: getPartyById($key)") {
        Alias.find { Aliases.partyId eq key }.toMutableList()
    }
}

object ChatCache : LruCache<Long, Chat>(Configuration.CHAT_CACHE_CAPACITY) {

    override fun supply(key: Long) = loggingTransaction("Updating chat cache: findOrCreateChat($key)") {
        Chat.findById(key) ?: Chat.new(key) {}
    }
}
