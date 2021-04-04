package com.github.pool_party.pull_party_bot.commands.handlers.testcontainers

import com.github.pool_party.pull_party_bot.commands.messages.ON_ARGUMENT_LIST_SUCCESS
import com.github.pool_party.pull_party_bot.commands.messages.ON_LIST_EMPTY
import kotlin.test.Test

internal class CommandTestContainerTest : AbstractTestContainerTest() {

    private val partyName = "partyName"
    private val aliasName = "aliasName"
    private val members = "@firstMember @secondMember @thirdMember"
    private val taggingMembers = members.toLowerCase()
    private val listOutput = "$ON_ARGUMENT_LIST_SUCCESS- ${taggingMembers.filter { it != '@' }}"

    // tagging

    @Test
    fun `explicit tagging test`() {
        -"/create $partyName $members"
        -"/party $partyName"
        +taggingMembers
    }

    @Test
    fun `implicit tagging test`() {
        -"/create $partyName $members"
        -"some prefix text @$partyName some postfix text"
        +taggingMembers
    }

    @Test
    fun `tagging only alias test`() {
        -"/create $partyName $members"
        -"/alias $aliasName $partyName"
        -"/delete $partyName"
        -"/party $aliasName"
        +taggingMembers
    }

    @Test
    fun `alias tagging test`() {
        -"/create $partyName $members"
        -"/alias $aliasName $partyName"
        -"/party $aliasName"
        +taggingMembers
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

    // listing

    @Test
    fun `list command test`() {
        -"/create $partyName $members"
        -"/alias $aliasName $partyName"
        -"/list $aliasName"
        +listOutput
    }

    @Test
    fun `list command with deleted party test`() {
        -"/create $partyName $members"
        -"/alias $aliasName $partyName"
        -"/delete $partyName"
        -"/list $aliasName"
        +listOutput
    }

    @Test
    fun `list command with deleted full party test`() {
        -"/create $partyName $members"
        -"/alias $aliasName $partyName"
        -"/delete $aliasName"
        -"/delete $partyName"
        -"/list"
        +ON_LIST_EMPTY
    }
}