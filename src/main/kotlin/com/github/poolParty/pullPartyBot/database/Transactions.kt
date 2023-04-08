package com.github.poolParty.pullPartyBot.database

import com.github.poolParty.pullPartyBot.Configuration
import mu.two.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

fun <T> loggingTransaction(info: String, action: Transaction.() -> T): T {
    logger.info { "=> $info" }

    val result: T
    val millis = measureTimeMillis { result = transaction { action() } }

    logger.info {
        "<= $info finished in ${millis / 1_000}.${millis % 1_000}s"
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
