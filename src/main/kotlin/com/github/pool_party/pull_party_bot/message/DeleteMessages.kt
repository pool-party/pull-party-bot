package com.github.pool_party.pull_party_bot.message

import com.github.pool_party.flume.utils.unformatted

fun onAliasDeleteSuccess(partyName: String) = """Alias ${partyName.unformatted()} is no longer exists 👍"""

fun onPartyDeleteSuggest(partyList: List<String>): String {
    val message = "Perhaps you want to delete all aliases of"

    val format =
        when (partyList.size) {
            1 -> " ${partyList.last()}"
            2 -> " ${partyList[0]} and ${partyList[1]}"
            else -> ": ${partyList.joinToString(", ")}"
        }

    return message + format.unformatted()
}

fun onPartyDeleteUnchanged(partyName: String) = """I am not familiar with ${partyName.unformatted()} 🤨"""

const val ON_PARTY_DELETE_SUCCESS = """All aliases of deleted party are also vanished 💨"""

const val ON_CALLBACK_SUCCESS = """Successfully deleted 😉"""

val ON_CLEAR_SUCCESS =
    """
    Life is a party. Chat is dead then. 😭

    All parties are over. Hoping you did it only to rock even bigger ones!
    Have a break and I'll be waiting for your new entries
    Use /create command to start all over again

    Type `/help create` for more information
    """.trimIndent()

val ON_DELETE_EMPTY =
    """
    I'm not the police, but can stop the party. Which one though? 🚨

    Perhaps you forgot to enter the name of party to remove
    Follow the /delete command with the name of redundant party

    Type `/help delete` for more information
    """.trimIndent()

val ON_PERMISSION_DENY_CALLBACK =
    """
    Action is available for admins only. 🙅‍
    """.trimIndent()

val ON_ADMINS_PARTY_CHANGE =
    """
    I would be careful disturbing these ladies and gentlemen. 🤫

    @admins is a reserved group, you can't create, change or delete it
    Try to make a new party instead with /create command

    Type /help for more information
    """.trimIndent()

val ON_ADMINS_PARTY_FAIL =
    """
    Admins?! What admins?! 🥴

    There is no admins in private chats or channels
    @admins is a reserved group, you can't change or delete it

    Type /help for more information
    """.trimIndent()
