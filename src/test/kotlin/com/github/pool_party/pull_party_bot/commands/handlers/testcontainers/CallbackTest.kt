package com.github.pool_party.pull_party_bot.commands.handlers.testcontainers

import com.elbekD.bot.types.InlineKeyboardMarkup
import com.github.pool_party.pull_party_bot.commands.messages.ON_LIST_SUCCESS
import io.mockk.every
import java.util.concurrent.CompletableFuture
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

internal class CallbackTest : AbstractTestContainerTest() {

    private val standardList = "$ON_LIST_SUCCESS\n$- admin\n  └── `admins` _(reserved)_"

    private var lastCallbackData: String? = null

    @BeforeTest
    fun setUpCallbackData() {
        every { bot.sendMessage(any(), any(), any(), any(), any(), any(), any(), any(), any()) } answers {
            lastCallbackData = (arg(8) as? InlineKeyboardMarkup)
                ?.inline_keyboard
                ?.singleOrNull()
                ?.singleOrNull()
                ?.callback_data
            CompletableFuture()
        }
    }

    @Test
    fun `tag suggestion test`() {
        -"/create abcdef qwertyuiop"
        -"@abcde"

        click()

        +"@abcdef"
    }

    @Test
    fun `delete node suggestion test`() {
        -"/create a qwertyuiop"
        -"/alias b a"
        -"/delete a"

        click()

        -"/list"
        +standardList
    }

    @Test
    fun `stale party delete suggestion test`() {
        -"/create a qwertyuiop"
        -"/list"

        click()

        -"/list"
        +standardList
    }

    private fun click() {
        assertNotNull(lastCallbackData)
        callback(lastCallbackData!!)
    }
}
