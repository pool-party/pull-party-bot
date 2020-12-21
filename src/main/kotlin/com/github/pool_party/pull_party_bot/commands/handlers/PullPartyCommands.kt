package com.github.pool_party.pull_party_bot.commands.handlers

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.github.pool_party.pull_party_bot.Configuration
import com.github.pool_party.pull_party_bot.commands.AbstractCommand
import com.github.pool_party.pull_party_bot.commands.messages.HELP_PARTY
import com.github.pool_party.pull_party_bot.commands.messages.ON_ADMINS_PARTY_FAIL
import com.github.pool_party.pull_party_bot.commands.messages.ON_PARTY_EMPTY
import com.github.pool_party.pull_party_bot.commands.messages.ON_PARTY_REQUEST_FAIL
import com.github.pool_party.pull_party_bot.commands.messages.ON_PARTY_REQUEST_FAILS
import com.github.pool_party.pull_party_bot.database.dao.PartyDao
import info.debatty.java.stringsimilarity.JaroWinkler

class ImplicitPartyHandler(private val partyDao: PartyDao) {

    /**
     * Handle implicit `@party-name`-like calls
     */
    fun onMessage(bot: Bot) = bot.onMessage { bot.action(it) }

    private fun Bot.action(message: Message) {
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

        handleParty(partyNames, message, partyDao)
    }
}

class PartyCommand(private val partyDao: PartyDao) :
    AbstractCommand("party", "tag the members of existing parties", HELP_PARTY) {

    override fun Bot.action(message: Message, args: String?) {
        val parsedArgs = parseArgs(args)?.distinct()
        val chatId = message.chat.id

        if (parsedArgs.isNullOrEmpty()) {
            sendMessage(chatId, ON_PARTY_EMPTY, "Markdown")
            return
        }

        handleParty(parsedArgs.asSequence(), message, partyDao) {
            sendMessage(
                chatId,
                if (parsedArgs.size == 1) ON_PARTY_REQUEST_FAIL
                else ON_PARTY_REQUEST_FAILS,
                "Markdown"
            )
        }
    }
}

private fun Bot.handleParty(
    partyNames: Sequence<String>,
    message: Message,
    partyDao: PartyDao,
    onFailure: () -> Unit = {}
) {
    val chatId = message.chat.id
    val failed = mutableListOf<String>()

    val res = partyNames
        .map { it.toLowerCase() }
        .distinct()
        .mapNotNull {
            if (it == "admins") {
                handleAdminsParty(message)
            } else {
                val users = partyDao.getByIdAndName(chatId, it)
                if (users.isNullOrBlank()) {
                    failed.add(it)
                }
                users
            }
        }
        .flatMap { it.split(" ").asSequence() }
        .distinct()
        .joinToString(" ")

    if (res.isNotBlank()) sendMessage(chatId, res, replyTo = message.message_id)

    if (failed.isEmpty()) return

    val parties = partyDao.getAll(chatId).map { it.name }
    val similarityAlgorithm = JaroWinkler()

    val suggestions = failed.asSequence()
        .mapNotNull { fail ->
            parties.asSequence()
                .map { it to similarityAlgorithm.similarity(it, fail) }
                .filter { it.second >= Configuration.JARO_WINKLER_SIMILARITY }
                .maxByOrNull { it.second }
                ?.let { it.first to fail }
        }
        .map { (possible, fail) -> "Perhaps you meant `@$possible` instead of @$fail" }
        .toList()

    if (suggestions.size != failed.size) {
        onFailure()
    }

    if (suggestions.isNotEmpty()) {
        sendMessage(chatId, suggestions.joinToString("\n"), "Markdown")
    }
}

private fun Bot.handleAdminsParty(msg: Message): String? {
    val chatId = msg.chat.id
    val chatType = msg.chat.type

    if (chatType != "group" && chatType != "supergroup") {
        sendMessage(chatId, ON_ADMINS_PARTY_FAIL, "Markdown")
        return null
    }

    return getChatAdministrators(chatId)
        .join()
        .asSequence()
        .mapNotNull { it.user.username }
        .filter { it.substring(it.length - 3).toLowerCase() != "bot" }
        .map { "@$it" }
        .joinToString(" ")
}
