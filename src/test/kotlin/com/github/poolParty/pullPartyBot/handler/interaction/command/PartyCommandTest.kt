package com.github.poolParty.pullPartyBot.handler.interaction.command

import com.github.poolParty.pullPartyBot.handler.interaction.AbstractBotTest
import kotlin.test.Test

internal class PartyCommandTest : AbstractBotTest() {

    @Test
    fun `explicit tagging test`() {
        -"/create party first second third"
        -"/party party"
        +"@first @second @third"
    }

    @Test
    fun `tagging only alias test`() {
        -"/create party first second third"
        -"/alias alias party"
        -"/delete party"
        -"/party alias"
        +"@first @second @third"
    }

    @Test
    fun `alias tagging test`() {
        -"/create party first second third"
        -"/alias alias party"
        -"/party alias"
        +"@first @second @third"
    }

    @Test
    fun `changing alias and tagging test`() {
        -"/create party first second third"
        -"/alias alias party"
        -"/change alias another"
        -"/party party"
        +"@another"
    }
}
