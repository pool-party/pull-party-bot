package com.github.poolParty.pullPartyBot.handler.interaction.common

import com.elbekd.bot.Bot
import com.elbekd.bot.model.toChatId
import com.elbekd.bot.types.Message
import com.github.poolParty.pullPartyBot.Configuration
import com.github.poolParty.pullPartyBot.database.dao.PartyDao
import com.github.poolParty.pullPartyBot.handler.Button
import com.github.poolParty.pullPartyBot.handler.deleteMessageLogging
import com.github.poolParty.pullPartyBot.handler.escapeMarkdown
import com.github.poolParty.pullPartyBot.handler.interaction.callback.PartyCallback
import com.github.poolParty.pullPartyBot.handler.message.InformationMessages
import com.github.poolParty.pullPartyBot.handler.sendMessageLogging
import info.debatty.java.stringsimilarity.JaroWinkler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList

suspend fun Bot.pullParty(
    partyNames: Sequence<String>,
    message: Message,
    partyDao: PartyDao,
    onFailure: suspend () -> Unit = {},
) {
    val chatId = message.chat.id
    val failed = mutableListOf<String>()

    val res = partyNames
        .map { it.lowercase() }
        .distinct()
        .asFlow()
        .mapNotNull {
            if (it == "admins") {
                pullAdminParty(message)
            } else {
                val users = partyDao.getByChatIdAndName(chatId, it)
                if (users.isNullOrBlank()) {
                    failed += it
                }
                users
            }
        }
        .toList()
        .asSequence()
        .flatMap { it.split(" ").asSequence() }
        .distinct()
        .joinToString(" ")

    if (res.isNotBlank()) sendMessageLogging(chatId, res.escapeMarkdown(), replyTo = message.messageId)

    misspellSuggestions(partyDao, message, chatId, failed, onFailure)
}

private suspend fun Bot.misspellSuggestions(
    partyDao: PartyDao,
    message: Message,
    chatId: Long,
    failed: List<String>,
    onFailure: suspend () -> Unit = {},
) {
    if (failed.isEmpty()) return

    val parties = partyDao.getAll(chatId)
    val similarityAlgorithm = JaroWinkler()

    val suggestions = failed.asSequence()
        .mapNotNull { fail ->
            parties.asSequence()
                .map { it to similarityAlgorithm.similarity(it.name, fail) }
                .filter { it.second >= Configuration.PARTY_SIMILARITY_COEFFICIENT }
                .maxByOrNull { it.second }
                ?.let { it.first to fail }
        }
        .take(10)
        .toList()

    if (suggestions.size != failed.size) {
        onFailure()
    }

    if (suggestions.isEmpty()) return

    val sentMessage = sendMessageLogging(
        chatId,
        InformationMessages.partyMisspell,
        buttons = suggestions.asSequence()
            .map { it.first }
            .distinctBy { it.name }
            .map { Button("@${it.name}", PartyCallback(it.party.id.value, message.from?.id)) }
            .toList(),
    )

    delay(Configuration.STALE_PING_SECONDS * 1_000L)

    deleteMessageLogging(chatId, sentMessage.messageId)
}

suspend fun Bot.getAdminsParty(message: Message): String? {
    val chatId = message.chat.id
    val chatType = message.chat.type

    if (chatType != "group" && chatType != "supergroup") return null

    return getChatAdministrators(chatId.toChatId())
        .asSequence()
        .mapNotNull { it.user.username }
        .filter { it.substring(it.length - 3).lowercase() != "bot" }
        .map { "@$it" }
        .joinToString(" ")
}

private suspend fun Bot.pullAdminParty(message: Message): String? {
    val adminsParty = getAdminsParty(message)
    if (adminsParty == null) sendMessageLogging(message.chat.id, InformationMessages.adminsPartyFail)
    return adminsParty
}
