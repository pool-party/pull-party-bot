package com.github.pool_party.pull_party_bot.command.cache

import java.util.LinkedHashMap

private const val loadFactor = 0.75f
private const val accessOrder = true

class LruCache<K, V>(private val capacity: Int = 256) :
    LinkedHashMap<K, V>(capacity, loadFactor, accessOrder), Cache<K, V> {

    override fun set(key: K, value: V) {
        put(key, value)
    }

    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?) = size > capacity
}
