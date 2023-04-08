package com.github.poolParty.pullPartyBot.handler.interaction.callback

import com.github.poolParty.pullPartyBot.handler.interaction.AbstractBotTest
import kotlin.test.Test

internal class DeleteNodeSuggestionCallbackTest : AbstractBotTest() {

    @Test
    fun `delete node suggestion test`() {
        -"/create party first second third"
        -"/alias alias party"
        -"/delete party"
        clickButton()
        -"/list"
        verifyMessage { "admins" in it && "party" !in it && "alias" !in it }
    }
}
