package com.github.pool_party.pull_party_bot.command

import com.elbekD.bot.Bot
import com.elbekD.bot.types.InlineKeyboardButton
import com.elbekD.bot.types.InlineKeyboardMarkup
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.Configuration
import com.github.pool_party.pull_party_bot.callback.PingCallbackData
import com.github.pool_party.pull_party_bot.message.HELP_PARTY
import com.github.pool_party.pull_party_bot.message.ON_ADMINS_PARTY_FAIL
import com.github.pool_party.pull_party_bot.message.ON_PARTY_EMPTY
import com.github.pool_party.pull_party_bot.message.ON_PARTY_MISSPELL
import com.github.pool_party.pull_party_bot.message.ON_PARTY_REQUEST_FAIL
import com.github.pool_party.pull_party_bot.message.ON_PARTY_REQUEST_FAILS
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import com.github.pool_party.telegram_bot_utils.interaction.command.AbstractCommand
import com.github.pool_party.telegram_bot_utils.interaction.message.EveryMessageInteraction
import com.github.pool_party.telegram_bot_utils.utils.sendMessageLogging
import info.debatty.java.stringsimilarity.JaroWinkler
import kotlinx.coroutines.delay

class ImplicitPartyHandler(private val partyDao: PartyDao) : EveryMessageInteraction {

    /**
     * Handle implicit `@party-name`-like calls
     */
    override suspend fun onMessage(bot: Bot, message: Message) {
        val text = message.text ?: message.caption

        if (message.forward_from != null || text == null) {
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
    AbstractCommand("party", "tag the members of existing parties", HELP_PARTY) {

    override suspend fun Bot.action(message: Message, args: List<String>) {
        val parsedArgs = args.distinct()
        val chatId = message.chat.id

        if (parsedArgs.isEmpty()) {
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

    if (res.isNotBlank()) sendMessageLogging(chatId, res, replyTo = message.message_id)

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

fun Bot.handleAdminsParty(message: Message): String? {
    val adminsParty = getAdminsParty(message)
    if (adminsParty == null) sendMessageLogging(message.chat.id, ON_ADMINS_PARTY_FAIL)
    return adminsParty
}
