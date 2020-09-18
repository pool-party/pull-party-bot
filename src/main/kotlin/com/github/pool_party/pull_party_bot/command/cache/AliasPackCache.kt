package com.github.pool_party.pull_party_bot.command.cache

import com.github.pool_party.pull_party_bot.database.StickerAlias
import com.github.pool_party.pull_party_bot.database.transaction.activatePackTransaction
import com.github.pool_party.pull_party_bot.database.transaction.deactivatePackTransaction
import com.github.pool_party.pull_party_bot.database.transaction.getActivatedPacksTransaction
import com.github.pool_party.pull_party_bot.database.transaction.getAliasesMapTransaction
import com.github.pool_party.pull_party_bot.database.transaction.getPackNameTransaction
import com.github.pool_party.pull_party_bot.database.transaction.getStickerIdTransaction
import com.github.pool_party.pull_party_bot.database.transaction.removePackTransaction

object AliasPackCache {

    private val cache = LruCache<String, StickerAlias>(2048)

    private val activatedPacks = LruCache<Long, MutableSet<String>>(512)

    /**
     * Takes a sticker alias to look for and returns a sticker id, if an alias exists, and null otherwise.
     *
     * @param alias A sticker alias string.
     * @return Sticker id.
     */
    fun getStickerId(packNames: Set<String>, alias: String): String? {
        val stickerAlias = cache[alias] ?: return null
        return if (packNames.contains(getPackNameTransaction(stickerAlias))) {
            getStickerIdTransaction(stickerAlias)
        } else null
    }

    /**
     * Being given an id of a chat, it caches activated sticker alias packs in this chat, if it is needed.
     *
     * @param chatId An id of chat.
     * @return Activated packs in the chat.
     */
    fun load(chatId: Long): Set<String> = activatedPacks.getOrPut(chatId) { find(chatId) }

    private fun find(chatId: Long): MutableSet<String> {
        val activated = getActivatedPacksTransaction(chatId)
        cache.putAll(
            activated
                .asSequence()
                .flatMap { getAliasesMapTransaction(it) }
                .map { (k, v) -> k to v }
                .toMap()
        )

        return activated.asSequence()
            .map { it.name }
            .distinct()
            .toMutableSet()
    }

    /**
     * Activates a given pack in the given chat.
     */
    fun activatePack(chatId: Long, packName: String): Boolean {
        if (activatedPacks[chatId]?.contains(packName) == true || !activatePackTransaction(chatId, packName)) {
            return false
        }

        activatedPacks[chatId] = find(chatId)
        return true
    }

    /**
     * Deactivates a given pack in the given chat.
     */
    fun deactivatePack(chatId: Long, packName: String): Boolean {
        if (activatedPacks[chatId]?.contains(packName) != true) {
            return false
        }

        deactivatePackTransaction(chatId, packName)
        activatedPacks[chatId] = find(chatId)
        return true
    }

    /**
     * Removes a given pack.
     *
     * @return A list of chat ids to be notified about pack removing and deactivating.
     */
    fun removePack(packName: String): List<Long>? {
        if (!removePackTransaction(packName)) {
            return null
        }

        val notificationList = ArrayList<Long>()

        activatedPacks.forEach { (k, v) ->
            if (v.remove(packName)) {
                notificationList.add(k)
            }
        }

        return notificationList
    }
}
