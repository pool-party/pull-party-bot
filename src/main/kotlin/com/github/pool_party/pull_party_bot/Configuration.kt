package com.github.pool_party.pull_party_bot

import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.booleanType
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType

object Configuration {

    private val configuration = EnvironmentVariables() overriding
        ConfigurationProperties.fromResource("defaults.properties")

    val APP_URL = configuration[Key("app.url", stringType)]
    val USER_NAME = configuration[Key("username", stringType)]
    val PORT = configuration[Key("port", intType)]

    val IS_LONGPOLL = configuration[Key("longpoll", booleanType)]

    val TELEGRAM_TOKEN = configuration[Key("telegram.token", stringType)]

    val DATABASE_URL = configuration[Key("jdbc.database.url", stringType)]
    val DATABASE_USERNAME = configuration[Key("jdbc.database.username", stringType)]
    val DATABASE_PASSWORD = configuration[Key("jdbc.database.password", stringType)]
}
