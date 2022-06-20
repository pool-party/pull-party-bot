package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekd.bot.Bot
import com.elbekd.bot.types.Message
import com.github.pool_party.pull_party_bot.commands.EveryMessageInteraction
import com.github.pool_party.pull_party_bot.database.dao.ChatDao
import mu.KotlinLogging

class MigrationHandler(private val chatDao: ChatDao) : EveryMessageInteraction {

    private val logger = KotlinLogging.logger {}

    override suspend fun onMessage(bot: Bot, message: Message) {

        val fromChatId = message.chat.id
        val toChatId = message.migrateToChatId ?: return

        logger.info { "Migration: $fromChatId -> $toChatId" }

        chatDao.migrate(fromChatId, toChatId)
    }
}
