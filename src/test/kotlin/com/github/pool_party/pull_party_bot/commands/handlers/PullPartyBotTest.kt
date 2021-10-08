package com.github.pool_party.pull_party_bot.commands.handlers

import com.github.pool_party.pull_party_bot.botBuilder
import com.github.pool_party.pull_party_bot.database.Aliases
import com.github.pool_party.pull_party_bot.database.AliasCache
import com.github.pool_party.pull_party_bot.database.Chats
import com.github.pool_party.pull_party_bot.database.ChatCache
import com.github.pool_party.pull_party_bot.database.Parties
import com.github.pool_party.pull_party_bot.database.PartyAliasesCache
import com.github.pool_party.pull_party_bot.database.PartyCache
import com.github.pool_party.flume.AbstractDatabaseBotTest
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.AfterTest

abstract class PullPartyBotTest : AbstractDatabaseBotTest(botBuilder) {

    @AfterTest
    override fun clearDatabases() {
        transaction {
            Aliases.deleteAll()
            Parties.deleteAll()
            Chats.deleteAll()
        }

        AliasCache.clear()
        PartyCache.clear()
        PartyAliasesCache.clear()
        ChatCache.clear()
    }
}
