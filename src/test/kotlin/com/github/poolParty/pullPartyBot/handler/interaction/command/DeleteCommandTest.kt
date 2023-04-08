package com.github.poolParty.pullPartyBot.handler.interaction.command

import com.github.poolParty.pullPartyBot.handler.interaction.AbstractBotTest
import com.github.poolParty.pullPartyBot.handler.message.ListMessages
import kotlin.test.Test

internal class DeleteCommandTest : AbstractBotTest() {

    @Test
    fun `list command with deleted party test`() {
        -"/create party first second third"
        -"/alias alias party"
        -"/delete party"
        -"/list alias"

        verifyMessage {
            ListMessages.argumentSuccess in it
                && "alias" in it
                && "first second third" in it
                && "party" !in it
                && "admins" !in it
        }
    }

    @Test
    fun `list command with deleted full party test`() {
        -"/create party first second third"
        -"/alias alias party"
        -"/delete alias"
        -"/delete party"
        -"/list"

        verifyMessage {
            ListMessages.success in it
                && "admins" in it
                && "alias" !in it
                && "party" !in it
                && "first second third" !in it
        }
    }
}
