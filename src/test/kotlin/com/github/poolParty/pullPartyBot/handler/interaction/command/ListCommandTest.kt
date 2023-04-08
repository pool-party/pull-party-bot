package com.github.poolParty.pullPartyBot.handler.interaction.command

import com.github.poolParty.pullPartyBot.handler.interaction.AbstractBotTest
import com.github.poolParty.pullPartyBot.handler.message.ListMessages
import kotlin.test.Test

internal class ListCommandTest : AbstractBotTest() {

    @Test
    fun `different output formats test`() {
        -"/create party first"
        -"/create first another"
        -"/list first"

        verifyContains(ListMessages.argumentSuccess, "party", " first", "`first`")
    }

    @Test
    fun `multiple parties list test`() {
        -"/create party first second third"
        -"/alias alias party"
        -"/create second members"
        -"/list"

        verifyContains(ListMessages.success, "admins", "members", "second", "alias", "party")
    }

    @Test
    fun `a horses herd list test`() {
        val chevaux = (0..100).map { "${"cheval".repeat(7)}$it" }

        -"/create party first second third"
        chevaux.forEach { -"/alias $it party" }
        -"/list"
        chevaux.forEach { verifyContains("`$it`") }
    }
}
