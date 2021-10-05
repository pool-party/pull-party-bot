package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekD.bot.types.InlineKeyboardMarkup
import io.mockk.every
import java.util.concurrent.CompletableFuture
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

internal class CallbackTest : PullPartyBotTest() {

    private var lastCallbackData: String? = null

    @BeforeTest
    fun setUpCallbackData() {
        every { bot.sendMessage(allAny(), allAny()) } answers {
            val inlineKeyboardMarkup = arg(8) as? InlineKeyboardMarkup
            println(">> ${secondArg<String>()}\n*button*: $inlineKeyboardMarkup")
            lastCallbackData = inlineKeyboardMarkup?.inline_keyboard?.singleOrNull()?.singleOrNull()?.callback_data
            CompletableFuture.completedFuture(message)
        }

        every { bot.answerCallbackQuery(allAny(), allAny()) } answers {
            println("^^ ${secondArg<String>()}")
            CompletableFuture.completedFuture(true)
        }

        every { bot.answerCallbackQuery(allAny()) } returns CompletableFuture.completedFuture(true)
    }

    @Test
    fun `tag suggestion test`() {
        -"/create abcdef qwertyuiop"
        -"@abcde"
        click()
        +"@qwertyuiop"
    }

    @Test
    fun `delete node suggestion test`() {
        -"/create partyname qwertyuiop"
        -"/alias alias partyname"
        -"/delete partyname"
        click()
        -"/list"
        verifyMessage { "admins" in it && "partyname" !in it && "alias" !in it }
    }

    @Test
    fun `stale party delete suggestion test`() {
        -"/create partyname qwertyuiop"
        -"/list"
        click()
        -"/list"
        verifyMessage { "admins" in it && "partyname" !in it }
    }

    private fun click() {
        assertNotNull(lastCallbackData)
        callback(lastCallbackData!!)
    }
}
