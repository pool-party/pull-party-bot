package com.github.poolParty.pullPartyBot.handler.interaction.callback

import com.github.poolParty.pullPartyBot.handler.interaction.AbstractBotTest
import kotlin.test.Test

internal class DeleteSuggestionCallbackTest : AbstractBotTest() {

    @Test
    fun `tag suggestion test`() {
        -"/create abcdef first second third"
        -"@abcde"
        clickButton()
        +"@first @second @third"
    }
}
