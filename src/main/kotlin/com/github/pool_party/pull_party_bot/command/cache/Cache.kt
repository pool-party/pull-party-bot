package com.github.pool_party.pull_party_bot.command.cache

interface Cache<K, V> {

    val size: Int

    operator fun get(key: K): V?

    operator fun set(key: K, value: V)
}
