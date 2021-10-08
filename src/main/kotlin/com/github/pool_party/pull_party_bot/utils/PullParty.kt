package com.github.pool_party.pull_party_bot.utils

import com.elbekD.bot.Bot
import com.elbekD.bot.types.InlineKeyboardButton
import com.elbekD.bot.types.InlineKeyboardMarkup
import com.elbekD.bot.types.Message
import com.github.pool_party.flume.utils.sendMessageLogging
import com.github.pool_party.flume.utils.unformatted
import com.github.pool_party.pull_party_bot.Configuration
import com.github.pool_party.pull_party_bot.callback.PingCallbackData
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import com.github.pool_party.pull_party_bot.message.ON_ADMINS_PARTY_FAIL
import com.github.pool_party.pull_party_bot.message.ON_PARTY_MISSPELL
import info.debatty.java.stringsimilarity.JaroWinkler
import kotlinx.coroutines.delay

suspend fun Bot.pullParty(
    partyNames: Sequence<String>,
    message: Message,
    partyDao: PartyDao,
    onFailure: () -> Unit = {}
) {
    val chatId = message.chat.id
    val failed = mutableListOf<String>()

    val res = partyNames
        .map { it.lowercase() }
        .distinct()
        .mapNotNull {
            if (it == "admins") {
                handleAdminsParty(message)
            } else {
                val users = partyDao.getByChatIdAndName(chatId, it)
                if (users.isNullOrBlank()) {
                    failed.add(it)
                }
                users
            }
        }
        .flatMap { it.split(" ").asSequence() }
        .distinct()
        .joinToString(" ")

    if (res.isNotBlank()) sendMessageLogging(chatId, res.unformatted(), replyTo = message.message_id)

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
        ON_PARTY_MISSPELL,
        markup = InlineKeyboardMarkup(
            suggestions.asSequence()
                .map { it.first }
                .distinctBy { it.name }
                .map {
                    val json = PingCallbackData(it.party.id.value, message.from?.id).encoded
                    listOf(InlineKeyboardButton("@${it.name}", callback_data = json))
                }
                .toList()
        )
    ).join()

    delay(Configuration.STALE_PING_SECONDS * 1000L)

    deleteMessage(chatId, sentMessage.message_id)
}

fun Bot.getAdminsParty(message: Message): String? {
    val chatId = message.chat.id
    val chatType = message.chat.type

    if (chatType != "group" && chatType != "supergroup") return null

    return getChatAdministrators(chatId)
        .join()
        .asSequence()
        .mapNotNull { it.user.username }
        .filter { it.substring(it.length - 3).lowercase() != "bot" }
        .map { "@$it" }
        .joinToString(" ")
}

private fun Bot.handleAdminsParty(message: Message): String? {
    val adminsParty = getAdminsParty(message)
    if (adminsParty == null) sendMessageLogging(message.chat.id, ON_ADMINS_PARTY_FAIL)
    return adminsParty
}
