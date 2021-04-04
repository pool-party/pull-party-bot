package com.github.pool_party.pull_party_bot.commands.handlers.testcontainers

import kotlin.test.Test

internal class CommandTestContainerTest : AbstractTestContainerTest() {

    @Test
    fun `explicit tagging test`() {
        val partyName = "partyName"
        val members = "@firstMember @secondMember @thirdMember"

        -"/create $partyName $members"
        -"/party $partyName"
        +members.toLowerCase()
    }

    @Test
    fun `implicit tagging test`() {
        val partyName = "partyName"
        val members = "@firstMember @secondMember @thirdMember"

        -"/create $partyName $members"
        -"some prefix text @$partyName some postfix text"
        +members.toLowerCase()
    }

    @Test
    fun `alias tagging test`() {
        val partyName = "partyName"
        val aliasName = "aliasName"
        val members = "@firstMember @secondMember @thirdMember"

        -"/create $partyName $members"
        -"/alias $aliasName $partyName"
        -"/party $aliasName"
        +members.toLowerCase()
    }
}
