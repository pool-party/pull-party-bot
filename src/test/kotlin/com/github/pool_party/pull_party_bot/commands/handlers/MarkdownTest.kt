package com.github.pool_party.pull_party_bot.commands.handlers

import com.github.pool_party.pull_party_bot.Configuration
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

internal class MarkdownTest : AbstractBotTest() {

    @TestFactory
    fun `create markdown reserved characters test`() = """_*[]()~`>#+-=|{}.!\""".asSequence()
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
