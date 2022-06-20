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

    val APP_URL by string()
    val USERNAME by string()
    val PORT by int()
    val HOST by string()

    val LONGPOLL by boolean()

    val TELEGRAM_TOKEN by string()

    private val DATABASE_URL by string()

    val JDBC_DATABASE_URL by lazy {
        val (_, _, host, port, database) = getDatabaseUrl().destructured
        "jdbc:postgresql://$host:$port/$database"
    }
    val JDBC_DATABASE_USERNAME by lazy { getDatabaseUrl().groupValues[1] }
    val JDBC_DATABASE_PASSWORD by lazy { getDatabaseUrl().groupValues[2] }

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

    private fun boolean() = Configured(booleanType)

    private fun string() = Configured(stringType)

    private fun int() = Configured(intType)

    private fun long() = Configured(longType)

    private fun double() = Configured(doubleType)

    private fun getDatabaseUrl(): MatchResult =
        """postgres://(\w+):([\w\d]+)@([\w\d\-\.]+):(\d+)/([\w\d]+)""".toRegex().matchEntire(DATABASE_URL)
            ?: throw RuntimeException("Bad DATABASE_URL format")

    private val <T> KProperty<T>.configName
        get() = name.lowercase().replace('_', '.')

    private class Configured<T>(private val parse: (PropertyLocation, String) -> T) {

        private var value: T? = null

        operator fun getValue(thisRef: Configuration, property: KProperty<*>): T {
            if (value == null) {
                value = configuration[Key(property.configName, parse)]
            }
            return value!!
        }

        operator fun setValue(thisRef: Configuration, property: KProperty<*>, value: T) {
            this.value = value
        }
    }
}
