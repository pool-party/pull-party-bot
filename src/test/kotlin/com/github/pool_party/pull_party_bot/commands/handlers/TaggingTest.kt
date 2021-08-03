package com.github.pool_party.pull_party_bot.commands.handlers

import kotlin.test.Test

internal class TaggingTest : AbstractBotTest() {

    private val partyName = "party_name"
    private val aliasName = "alias_name"
    private val members = "@first_member @second_member @third_member"

    @Test
    fun `explicit tagging test`() {
        -"/create $partyName $members"
        -"/party $partyName"
        +members
    }

    @Test
    fun `implicit tagging test`() {
        -"/create $partyName $members"
        -"some prefix text @$partyName some postfix text"
        +members
    }

    @Test
    fun `tagging only alias test`() {
        -"/create $partyName $members"
        -"/alias $aliasName $partyName"
        -"/delete $partyName"
        -"/party $aliasName"
        +members
    }

    @Test
    fun `alias tagging test`() {
        -"/create $partyName $members"
        -"/alias $aliasName $partyName"
        -"/party $aliasName"
        +members
    }

    @Test
    fun `changing alias and tagging test`() {
        val newMembers = "@another_member"

        -"/create $partyName $members"
        -"/alias $aliasName $partyName"
        -"/change $aliasName $newMembers"
        -"/party $partyName"
        +newMembers
    }
}
