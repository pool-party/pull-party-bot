package com.github.pool_party.pull_party_bot.commands.messages

val ON_CLEAR_SUCCESS =
    """
    Life is a party. Chat is dead then. 😭

    All parties are over. Hoping you did it only to rock even bigger ones!
    Have a break and I'll be waiting for your new entries
    Use /create command to start all over again

    Type `/help create` for more information
    """.trimIndent()

fun onAliasDeleteSuccess(partyName: String) = """Alias $partyName is no longer exists 👍"""

fun onPartyDeleteSuccess(partyName: String) = """Party $partyName and its aliases are deleted 👍"""

fun onPartyDeleteSuggest(partyList: List<String>): String {
    val message = """Perhaps you want to delete all aliases of"""

    val format =
        when (partyList.size) {
            0 -> """""" // TODO check if case is possible.
            1 -> """ ${partyList.last()}"""
            2 -> """ ${partyList[0]} and ${partyList[1]}"""
            else -> """: ${partyList.joinToString(", ")}"""
        }

    return message + format
}

fun onPartyDeleteUnchanged(partyName: String) =
    """I am not familiar with $partyName 🤨"""

val ON_DELETE_EMPTY =
    """
    I'm not the police, but can stop the party. Which one though? 🚨

    Perhaps you forgot to enter the name of party to remove
    Follow the /delete command with the name of redundant party

    Type `/help delete` for more information
    """.trimIndent()

val ON_SENDER_FAIL =
    """
    Only the chosen ones can perform these actions. 😎

    I were not able to recognize the permissions you have in this chat
    Operation aborted, ask group admins if you still want to make it happen

    Type `/help delete` or `/help clear` for more information
    """.trimIndent()

val ON_PERMISSION_DENY =
    """
    Parties are all I have. You can't just do this. 🤬

    Sad enough for me, parties can be deleted, but only by admins
    Only group administrators can perform /delete and /clear commands
    You can pull @admins party to ask them to perform this command

    Type `/help delete` or `/help clear` for more information
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
