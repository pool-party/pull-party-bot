package com.github.pool_party.pull_party_bot.commands.handlers

import com.github.pool_party.pull_party_bot.commands.messages.ON_ARGUMENT_LIST_SUCCESS
import com.github.pool_party.pull_party_bot.commands.messages.ON_LIST_SUCCESS
import kotlin.test.Test

internal class ListingTest : AbstractBotTest() {

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
    fun `list command untagged test`() {
        -"/create $partyName $listedMembers"
        -"/alias $aliasName $partyName"
        -"/list $aliasName"

        verifyContains(ON_ARGUMENT_LIST_SUCCESS, aliasName, listedMembers)
    }

    @Test
    fun `list command tagged test`() {
        -"/create $partyName $taggedMembers"
        -"/alias $aliasName $partyName"
        -"/list $aliasName"

        verifyContains(ON_ARGUMENT_LIST_SUCCESS, aliasName, listedMembers)
    }

    @Test
    fun `list command with deleted party test`() {
        -"/create $partyName $listedMembers"
        -"/alias $aliasName $partyName"
        -"/delete $partyName"
        -"/list $aliasName"

        verifyMessage { ON_ARGUMENT_LIST_SUCCESS in it && aliasName in it && listedMembers in it && partyName !in it }
    }

    @Test
    fun `list command with deleted full party test`() {
        -"/create $partyName $listedMembers"
        -"/alias $aliasName $partyName"
        -"/delete $aliasName"
        -"/delete $partyName"
        -"/list"

        verifyContains(ON_LIST_SUCCESS, "admins")
    }

    @Test
    fun `different output formats test`() {
        -"/create $partyName first"
        -"/create first another"
        -"/list first"

        verifyContains(ON_ARGUMENT_LIST_SUCCESS, partyName, " first", "`first`")
    }

    @Test
    fun `multiple parties list test`() {
        val secondPartyName = "second"
        val secondPartyMembers = "members"

        -"/create $partyName $listedMembers"
        -"/alias $aliasName $partyName"
        -"/create $secondPartyName $secondPartyMembers"
        -"/list"

        verifyContains(ON_LIST_SUCCESS, "admins", secondPartyMembers, secondPartyName, aliasName, partyName)
    }

    @Test
    fun `a horses herd list test`() {
        val chevaux = (0..100).map { "${"cheval".repeat(7)}$it" }

        -"/create $partyName $listedMembers"
        chevaux.forEach { -"/alias $it $partyName" }
        -"/list"
        chevaux.forEach { verifyContains("`$it`") }
    }
}
