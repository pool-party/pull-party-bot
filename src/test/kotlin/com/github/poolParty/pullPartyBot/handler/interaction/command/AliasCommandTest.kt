package com.github.poolParty.pullPartyBot.handler.interaction.command

import com.github.poolParty.pullPartyBot.handler.interaction.AbstractBotTest
import com.github.poolParty.pullPartyBot.handler.message.ListMessages
import kotlin.test.Test

internal class AliasCommandTest : AbstractBotTest() {

    @Test
    fun `list command untagged test`() {
        -"/create party first second third"
        -"/alias alias party"
        -"/list alias"

        verifyContains(ListMessages.argumentSuccess, "alias", "first second third")
    }

    @Test
    fun `list command tagged test`() {
        -"/create party @first @second @third"
        -"/alias alias party"
        -"/list alias"

        verifyContains(ListMessages.argumentSuccess, "alias", "first second third")
    }
}
