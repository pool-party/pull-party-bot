package com.github.poolParty.pullPartyBot.handler.interaction.command

import com.github.poolParty.pullPartyBot.Configuration
import com.github.poolParty.pullPartyBot.handler.interaction.AbstractBotTest
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

internal class CreateCommandTest : AbstractBotTest() {

    @TestFactory
    fun `create markdown reserved characters test`(): List<DynamicTest> =
        """_*[]()~`>#+-=|{}.!\""".asSequence()
            .filter { it !in Configuration.PROHIBITED_SYMBOLS }
            .map {
                dynamicTest("create reserved markdown \"$it\" party") {
                    -"/create $it abcdef"
                    verifyContains("successfully", "\\$it")

                    -"/list"
                    verifyContains("─ `\\$it`")
                }
            }
            .toList()
}
