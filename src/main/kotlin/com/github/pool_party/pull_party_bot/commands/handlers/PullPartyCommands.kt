package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekd.bot.Bot
import com.elbekd.bot.model.toChatId
import com.elbekd.bot.types.InlineKeyboardButton
import com.elbekd.bot.types.InlineKeyboardMarkup
import com.elbekd.bot.types.Message
import com.github.pool_party.pull_party_bot.Configuration
import com.github.pool_party.pull_party_bot.commands.AbstractCommand
import com.github.pool_party.pull_party_bot.commands.CallbackAction
import com.github.pool_party.pull_party_bot.commands.CallbackData
import com.github.pool_party.pull_party_bot.commands.EveryMessageInteraction
import com.github.pool_party.pull_party_bot.commands.deleteMessageLogging
import com.github.pool_party.pull_party_bot.commands.escapeMarkdown
import com.github.pool_party.pull_party_bot.commands.messages.HELP_PARTY
import com.github.pool_party.pull_party_bot.commands.messages.ON_ADMINS_PARTY_FAIL
import com.github.pool_party.pull_party_bot.commands.messages.ON_PARTY_EMPTY
import com.github.pool_party.pull_party_bot.commands.messages.ON_PARTY_MISSPELL
import com.github.pool_party.pull_party_bot.commands.messages.ON_PARTY_REQUEST_FAIL
import com.github.pool_party.pull_party_bot.commands.messages.ON_PARTY_REQUEST_FAILS
import com.github.pool_party.pull_party_bot.commands.sendMessageLogging
import com.github.pool_party.pull_party_bot.commands.user
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import info.debatty.java.stringsimilarity.JaroWinkler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ImplicitPartyHandler(private val partyDao: PartyDao) : EveryMessageInteraction {

    /**
     * Handle implicit `@party-name`-like calls
     */
    override suspend fun onMessage(bot: Bot, message: Message) {
        val text = message.text ?: message.caption

        if (message.forwardFrom != null || text == null) {
            return
        }

        val prohibitedSymbolsString = Configuration.PROHIBITED_SYMBOLS.joinToString("")
        val regex = Regex("(?<party>[^$prohibitedSymbolsString]*)[$prohibitedSymbolsString]*")

        val partyNames = text.lineSequence()
            .flatMap { it.split(' ', '\t').asSequence() }
            .filter { it.startsWith('@') }
            .map { it.removePrefix("@") }
            .mapNotNull { regex.matchEntire(it)?.groups?.get(1)?.value }

        bot.handleParty(partyNames, message, partyDao)
    }
}

class PartyCommand(private val partyDao: PartyDao) :
    AbstractCommand("party", "tag the members of the given parties", HELP_PARTY) {

    override suspend fun Bot.action(message: Message, args: String?) {
        val parsedArgs = parseArgs(args)?.distinct()
        val chatId = message.chat.id

        if (parsedArgs.isNullOrEmpty()) {
            sendMessageLogging(chatId, ON_PARTY_EMPTY)
            return
        }

        handleParty(parsedArgs.asSequence(), message, partyDao) {
            sendMessageLogging(
                chatId,
                if (parsedArgs.size == 1) ON_PARTY_REQUEST_FAIL
                else ON_PARTY_REQUEST_FAILS,
            )
        }
    }
}

private suspend fun Bot.handleParty(
    partyNames: Sequence<String>,
    message: Message,
    partyDao: PartyDao,
    onFailure: suspend () -> Unit = {}
) {
    val chatId = message.chat.id
    val failed = mutableListOf<String>()

    val res = partyNames
        .map { it.lowercase() }
        .distinct()
        .asFlow()
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
        .toList()
        .asSequence()
        .flatMap { it.split(" ").asSequence() }
        .distinct()
        .joinToString(" ")

    if (res.isNotBlank()) sendMessageLogging(chatId, res.escapeMarkdown(), replyTo = message.messageId)

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
                    val json = Json.encodeToString(
                        CallbackData(CallbackAction.PING, it.party.id.value, message.from?.id)
                    )
                    listOf(InlineKeyboardButton("@${it.name}", callbackData = json))
                }
                .toList()
        )
    )

    delay(Configuration.STALE_PING_SECONDS * 1000L)

    deleteMessageLogging(chatId, sentMessage.messageId)
}

suspend fun Bot.getAdminsParty(message: Message): String? {
    val chatId = message.chat.id
    val chatType = message.chat.type

    if (chatType != "group" && chatType != "supergroup") return null

    return getChatAdministrators(chatId.toChatId())
        .asSequence()
        .mapNotNull {it.user.username }
        .filter { it.substring(it.length - 3).lowercase() != "bot" }
        .map { "@$it" }
        .joinToString(" ")
}

suspend fun Bot.handleAdminsParty(message: Message): String? {
    val adminsParty = getAdminsParty(message)
    if (adminsParty == null) sendMessageLogging(message.chat.id, ON_ADMINS_PARTY_FAIL)
    return adminsParty
}
