package com.github.pool_party.pull_party_bot.commands

import com.github.pool_party.pull_party_bot.database.PartyDao

val PROHIBITED_SYMBOLS = "!,.?:;()".toList()

enum class PartyChangeStatus(
    val changesFull: Boolean,
    val transaction: PartyDao.(Long, String, List<String>) -> Boolean,
    val onFailure: String
) {
    CREATE(true, PartyDao::create, ON_CREATE_REQUEST_FAIL) {
        override fun onSuccess(partyName: String) = "Party $partyName successfully created!"
    },

    CHANGE(true, PartyDao::changeUsers, ON_CHANGE_REQUEST_FAIL) {
        override fun onSuccess(partyName: String) = "Party $partyName changed beyond recognition!"
    },

    ADD(false, PartyDao::addUsers, ON_CHANGE_REQUEST_FAIL) {
        override fun onSuccess(partyName: String) = "Party $partyName is getting bigger and bigger!"
    },

    REMOVE(false, PartyDao::removeUsers, ON_REMOVE_REQUEST_FAIL) {
        override fun onSuccess(partyName: String) = "Party $partyName lost somebody, but not the vibe!"
    };

    abstract fun onSuccess(partyName: String): String
}
