package com.github.poolParty.pullPartyBot.handler.interaction.callback

import com.github.poolParty.pullPartyBot.handler.interaction.AbstractBotTest
import kotlin.test.Test

internal class PartyCallbackTest : AbstractBotTest() {

    @Test
    fun `stale party delete suggestion test`() {
        -"/create party first second third"
        -"/list"
        clickButton()
        -"/list"
        verifyMessage { "admins" in it && "party" !in it }
    }
}
