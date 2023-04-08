package com.github.poolParty.pullPartyBot.handler.interaction.everyMessage

import com.github.poolParty.pullPartyBot.handler.interaction.AbstractBotTest
import kotlin.test.Test

internal class MigrationHandlerTest : AbstractBotTest() {

    @Test
    fun `moving parties test`() {
        -"/create party first second third"

        val newChatId = 1234L
        -message.copy(migrateFromChatId = message.chat.id, migrateToChatId = newChatId)

        -message.copy(chat = chat.copy(id = newChatId), text = "@party")
        verifyMessage(chatId = newChatId) { it == "@first @second @third" }
    }

    @Test
    fun `removing previous chat parties test`() {
        -"/create party first second third"

        val newChatId = 1234L
        -message.copy(migrateFromChatId = message.chat.id, migrateToChatId = newChatId)

        -"@party"
        verifyMessage(exactly = 0, chatId = message.chat.id) { it == "@first @second @third" }
        verifyMessage(exactly = 0, chatId = newChatId) { it == "@first @second @third" }
    }
}
