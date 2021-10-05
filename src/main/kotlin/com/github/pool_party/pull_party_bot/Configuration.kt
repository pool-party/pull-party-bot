package com.github.pool_party.pull_party_bot

import com.github.pool_party.telegram_bot_utils.configuration.AbstractConfiguration

object Configuration : AbstractConfiguration() {

    val JDBC_DATABASE_URL by string()
    val JDBC_DATABASE_USERNAME by string()
    val JDBC_DATABASE_PASSWORD by string()

    val DEVELOP_CHAT_ID by long()

    val PROHIBITED_SYMBOLS = "!,.?:;()".toList()

    val STALE_PARTY_WEEKS by int()

    val STALE_PING_SECONDS by int()

    val PARTY_SIMILARITY_COEFFICIENT by double()

    const val MESSAGE_LENGTH = 4096

    val CACHE_CAPACITY_ALIAS by int()
    val CACHE_CAPACITY_PARTY by int()
    val CACHE_CAPACITY_PARTYALIASES by int()
    val CACHE_CAPACITY_CHAT by int()
}
