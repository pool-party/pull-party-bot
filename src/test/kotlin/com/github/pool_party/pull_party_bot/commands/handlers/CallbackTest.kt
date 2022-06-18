package com.github.pool_party.pull_party_bot.commands.handlers

import kotlin.test.Test

internal class CallbackTest : AbstractBotTest() {

    private val partyName = "party"
    private val aliasName = "alias"

    private val taggedMembers: String
    private val listedMembers: String

    init {
        val members = listOf("first", "second", "third")
        taggedMembers = members.asSequence().map { "@$it" }.joinToString(" ")
        listedMembers = members.joinToString(" ")
    }

    @Test
    fun `tag suggestion test`() {
        -"/create abcdef $listedMembers"
        -"@abcde"
        clickButton()
        +taggedMembers
    }

    @Test
    fun `delete node suggestion test`() {
        -"/create $partyName $listedMembers"
        -"/alias $aliasName $partyName"
        -"/delete $partyName"
        clickButton()
        -"/list"
        verifyMessage { "admins" in it && partyName !in it && aliasName !in it }
    }

    @Test
    fun `stale party delete suggestion test`() {
        -"/create $partyName $listedMembers"
        -"/list"
        clickButton()
        -"/list"
        verifyMessage { "admins" in it && partyName !in it }
    }
}
