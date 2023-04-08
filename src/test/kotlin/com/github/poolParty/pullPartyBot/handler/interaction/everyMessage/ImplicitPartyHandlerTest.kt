package com.github.poolParty.pullPartyBot.handler.interaction.everyMessage

import com.github.poolParty.pullPartyBot.handler.interaction.AbstractBotTest
import kotlin.test.Test

internal class ImplicitPartyHandlerTest : AbstractBotTest() {

    @Test
    fun `implicit tagging test`() {
        -"/create party first second third"
        -"some prefix text @party some postfix text"
        +"@first @second @third"
    }
}
