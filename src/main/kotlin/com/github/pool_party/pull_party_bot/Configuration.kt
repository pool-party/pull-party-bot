package com.github.pool_party.pull_party_bot

import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.PropertyLocation
import com.natpryce.konfig.booleanType
import com.natpryce.konfig.doubleType
import com.natpryce.konfig.intType
import com.natpryce.konfig.longType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import kotlin.reflect.KProperty

object Configuration {

    private val configuration = EnvironmentVariables()
        .overriding(ConfigurationProperties.fromResource("defaults.properties"))
        .let {
            val testProperties = "test.properties"
            if (ClassLoader.getSystemClassLoader().getResource(testProperties) != null)
                ConfigurationProperties.fromResource(testProperties) overriding it
            else it
        }

    val APP_URL by Configured("app.url", stringType)
    val USER_NAME by Configured("username", stringType)
    val PORT by Configured("port", intType)
    val HOST by Configured("host", stringType)

    val IS_LONGPOLL by Configured("longpoll", booleanType)

    val TELEGRAM_TOKEN by Configured("telegram.token", stringType)

    val DATABASE_URL by Configured("jdbc.database.url", stringType)
    val DATABASE_USERNAME by Configured("jdbc.database.username", stringType)
    val DATABASE_PASSWORD by Configured("jdbc.database.password", stringType)

    val DEVELOP_CHAT_ID by Configured("develop.chat.id", longType)

    val PROHIBITED_SYMBOLS = "!,.?:;()".toList()

    val STALE_PARTY_TIME_WEEKS by Configured("stalePartyTimeWeeks", intType)

    val JARO_WINKLER_SIMILARITY by Configured("partySimilarity.coefficient", doubleType)

    private class Configured<T>(private val name: String, private val parse: (PropertyLocation, String) -> T) {
        operator fun getValue(thisRef: Configuration, property: KProperty<*>): T = configuration[Key(name, parse)]
    }
}
