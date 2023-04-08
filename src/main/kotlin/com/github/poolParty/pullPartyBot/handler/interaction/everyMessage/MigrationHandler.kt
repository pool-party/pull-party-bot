package com.github.poolParty.pullPartyBot.handler.interaction.everyMessage

import com.elbekd.bot.Bot
import com.elbekd.bot.types.Message
import com.github.poolParty.pullPartyBot.database.dao.ChatDao
import mu.two.KotlinLogging

class MigrationHandler(private val chatDao: ChatDao) : EveryMessageInteraction {

    private val logger = KotlinLogging.logger {}

    override suspend fun Bot.onMessage(message: Message) {
        val fromChatId = message.chat.id
        val toChatId = message.migrateToChatId ?: return

        logger.info { "Migration: $fromChatId -> $toChatId" }

        chatDao.migrate(fromChatId, toChatId)
    }
}
