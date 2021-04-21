package com.github.pool_party.pull_party_bot.database

import com.github.pool_party.pull_party_bot.Configuration
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

fun <T> loggingTransaction(info: String, action: Transaction.() -> T): T {
    logger.info { "${LocalDateTime.now()} $info" }
    return transaction { action() }
}

fun initDB() {
    Database.connect(
        Configuration.DATABASE_URL,
        user = Configuration.DATABASE_USERNAME,
        password = Configuration.DATABASE_PASSWORD
    )
}
