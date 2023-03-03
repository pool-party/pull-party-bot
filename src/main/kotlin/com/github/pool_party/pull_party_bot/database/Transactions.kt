package com.github.pool_party.pull_party_bot.database

import com.github.pool_party.pull_party_bot.Configuration
import mu.two.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.system.measureNanoTime

private val logger = KotlinLogging.logger {}

fun <T> loggingTransaction(info: String, action: Transaction.() -> T): T {
    logger.info { "=> $info" }

    val result: T
    val nanoseconds = measureNanoTime { result = transaction { action() } }

    logger.info {
        "<= $info finished in ${nanoseconds / 1000000000}.${nanoseconds % 1000000000}s"
    }
    return result
}

fun initDB() {
    Database.connect(
        Configuration.JDBC_DATABASE_URL,
        user = Configuration.JDBC_DATABASE_USERNAME,
        password = Configuration.JDBC_DATABASE_PASSWORD,
    )
}
