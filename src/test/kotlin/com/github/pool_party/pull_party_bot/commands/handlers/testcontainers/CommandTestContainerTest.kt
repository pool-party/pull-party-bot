package com.github.pool_party.pull_party_bot.commands.handlers.testcontainers

import com.github.pool_party.pull_party_bot.commands.messages.ON_ARGUMENT_LIST_SUCCESS
import kotlin.test.Test

internal class CommandTestContainerTest : AbstractTestContainerTest() {

    private val partyName = "partyName"
    private val aliasName = "aliasName"
    private val members = "@firstMember @secondMember @thirdMember"
    private val taggingMembers = members.toLowerCase()

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
    fun `alias tagging test`() {
        -"/create $partyName $members"
        -"/alias $aliasName $partyName"
        -"/party $aliasName"
        +taggingMembers
    }

    @Test
    fun `list command test`() {
        -"/create $partyName $members"
        -"/alias $aliasName $partyName"
        -"/list $aliasName"
        +"$ON_ARGUMENT_LIST_SUCCESS- ${taggingMembers.filter { it != '@' }}"
    }
}
