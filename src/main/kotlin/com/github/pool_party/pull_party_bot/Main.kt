package com.github.pool_party.pull_party_bot

import com.elbekD.bot.Bot
import com.elbekD.bot.server
import com.elbekD.bot.util.AllowedUpdate
import com.github.pool_party.pull_party_bot.commands.initializePingCommands

import org.jetbrains.exposed.sql.Database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


const val APP_URL = "https://somebodyoncetoldmepool.herokuapp.com"
const val USER_NAME = "PullPartyBot"
const val DEFAULT_PORT = 80


object Users : Table() {
    val id = varchar("id", 10) // Column<String>
    val name = varchar("name", length = 50) // Column<String>
    val cityId = (integer("city_id") references Cities.id).nullable() // Column<Int?>

    override val primaryKey = PrimaryKey(id, name = "PK_User_ID") // name is optional here
}

object Cities : Table() {
    val id = integer("id").autoIncrement() // Column<Int>
    val name = varchar("name", 50) // Column<String>

    override val primaryKey = PrimaryKey(id, name = "PK_Cities_ID")
}

fun main() {
    val token = System.getenv("TELEGRAM_TOKEN") ?: throw RuntimeException("Unable to get system variable for token")
    val isLongpoll = System.getenv("IS_LONGPOLL")?.toBoolean() ?: false

    val bot = if (isLongpoll) {
        Bot.createPolling(USER_NAME, token)
    } else {
        Bot.createWebhook(USER_NAME, token) {
            url = "$APP_URL/$token"
            allowedUpdates = listOf(AllowedUpdate.Message)

            server {
                host = "0.0.0.0"
                port = System.getenv("PORT")?.toInt() ?: DEFAULT_PORT
            }
        }
    }

    initializePingCommands(bot)


    Database.connect(System.getenv("HEROKU_POSTGRESQL_RED_JDBC_URL"), 
            user = System.getenv("HEROKU_POSTGRESQL_RED_JDBC_USERNAME"), 
            password = System.getenv("HEROKU_POSTGRESQL_RED_JDBC_PASSWORD"))
    

    bot.start()
}
