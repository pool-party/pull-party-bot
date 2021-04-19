package com.github.pool_party.pull_party_bot.commands.handlers.mock

import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.commands.Command
import com.github.pool_party.pull_party_bot.commands.handlers.AbstractBotTest
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest

internal abstract class AbstractCommandTest : AbstractBotTest() {

    @MockK
    protected lateinit var partyDao: PartyDao

    @MockK
    protected lateinit var chatDao: ChatDao

    private lateinit var action: suspend (Message, String?) -> Unit

    abstract fun initializeCommand(partyDao: PartyDao, chatDao: ChatDao): Command

    @BeforeTest
    fun setUpMock() {
        MockKAnnotations.init(this)

        every { bot.onCommand(any(), any()) } answers { action = secondArg() }

        initializeCommand(partyDao, chatDao).onMessage(bot)
    }

    protected fun onMessage(message: Message, args: String? = null) {
        runBlocking { GlobalScope.launch { action(message, args) }.join() }
    }
}
