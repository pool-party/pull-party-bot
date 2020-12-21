package com.github.pool_party.pull_party_bot

import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.booleanType
import com.natpryce.konfig.intType
import com.natpryce.konfig.longType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType

object Configuration {

    private val configuration = EnvironmentVariables()
        .overriding(ConfigurationProperties.fromResource("defaults.properties"))
        .let {
            val testProperties = "test.properties"
            if (ClassLoader.getSystemClassLoader().getResource(testProperties) != null)
                ConfigurationProperties.fromResource(testProperties) overriding it
            else it
        }

    val APP_URL by lazy { configuration[Key("app.url", stringType)] }
    val USER_NAME by lazy { configuration[Key("username", stringType)] }
    val PORT by lazy { configuration[Key("port", intType)] }
    const val HOST = "0.0.0.0"

    val IS_LONGPOLL by lazy { configuration[Key("longpoll", booleanType)] }

    val TELEGRAM_TOKEN by lazy { configuration[Key("telegram.token", stringType)] }

    val DATABASE_URL by lazy { configuration[Key("jdbc.database.url", stringType)] }
    val DATABASE_USERNAME by lazy { configuration[Key("jdbc.database.username", stringType)] }
    val DATABASE_PASSWORD by lazy { configuration[Key("jdbc.database.password", stringType)] }

    val DEVELOP_CHAT_ID by lazy { configuration[Key("develop.chat.id", longType)] }

    val PROHIBITED_SYMBOLS = "!,.?:;()".toList()

    const val JARO_WINKLER_SIMILARITY = 0.863
}
