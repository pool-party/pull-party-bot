package com.github.poolParty.pullPartyBot.handler.interaction.command

import com.github.poolParty.pullPartyBot.handler.interaction.AbstractBotTest
import com.github.poolParty.pullPartyBot.handler.message.ListMessages
import kotlin.test.Test

internal class ClearCommandTest : AbstractBotTest() {

    @Test
    fun `list cleared chat parties test`() {
        -"/create party first second third"
        -"/alias alias party"
        -"/clear"
        clickButton(1)
        -"/list"

        verifyMessage {
            ListMessages.success in it &&
                "admins" in it &&
                "alias" !in it &&
                "party" !in it &&
                "first second third" !in it
        }
    }

    @Test
    fun `clearing canceled test`() {
        -"/create party first second third"
        -"/alias alias party"
        -"/clear"
        clickButton(0)
        -"/list"

        verifyMessage {
            ListMessages.success in it &&
                "admins" in it &&
                "alias" in it &&
                "party" in it &&
                "first second third" in it
        }
    }
}
