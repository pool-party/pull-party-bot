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

    val APP_URL by Configured(stringType)
    val USERNAME by Configured(stringType)
    val PORT by Configured(intType)
    val HOST by Configured(stringType)

    val LONGPOLL by Configured(booleanType)

    val TELEGRAM_TOKEN by Configured(stringType)

    val JDBC_DATABASE_URL by Configured(stringType)
    val JDBC_DATABASE_USERNAME by Configured(stringType)
    val JDBC_DATABASE_PASSWORD by Configured(stringType)

    val DEVELOP_CHAT_ID by Configured(longType)

    val PROHIBITED_SYMBOLS = "!,.?:;()".toList()

    val STALE_PARTY_WEEKS by Configured(intType)

    val STALE_PING_SECONDS by Configured(intType)

    val PARTY_SIMILARITY_COEFFICIENT by Configured(doubleType)

    const val MESSAGE_LENGTH = 4096

    val CACHE_CAPACITY_ALIAS by Configured(intType)
    val CACHE_CAPACITY_PARTY by Configured(intType)
    val CACHE_CAPACITY_PARTYALIASES by Configured(intType)
    val CACHE_CAPACITY_CHAT by Configured(intType)

    private open class Configured<T>(private val parse: (PropertyLocation, String) -> T) {

        private var value: T? = null

        operator fun getValue(thisRef: Configuration, property: KProperty<*>): T {
            if (value == null) {
                value = configuration[Key(property.name.lowercase().replace('_', '.'), parse)]
            }
            return value!!
        }

        operator fun setValue(thisRef: Configuration, property: KProperty<*>, value: T) {
            this.value = value
        }
    }
}
