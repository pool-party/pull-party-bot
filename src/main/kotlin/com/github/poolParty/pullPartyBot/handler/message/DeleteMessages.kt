package com.github.poolParty.pullPartyBot.handler.message

import com.github.poolParty.pullPartyBot.handler.escapeMarkdown

object DeleteMessages {

    fun aliasDeleteSuccess(partyName: String) = """Alias ${partyName.escapeMarkdown()} no longer exists 👍"""

    fun partyDeleteSuggest(parties: List<String>): String {
        val partyList = parties.map { it.escapeMarkdown() }
        val message = "Perhaps you want to delete all aliases of"

        val format =
            when (partyList.size) {
                1 -> " ${partyList.last()}"
                2 -> " ${partyList[0]} and ${partyList[1]}"
                else -> ": ${partyList.joinToString(", ")}"
            }

        return message + format
    }

    fun partyDeleteUnchanged(partyName: String) = """I am not familiar with ${partyName.escapeMarkdown()} 🤨"""

    const val partyDeleteSuccess = """All aliases of deleted party are also vanished 💨"""

    const val callbackSuccess = """Successfully deleted 😉"""

    val clearSuccess =
        """
        Life is a party. Chat is dead then. 😭

        All parties are over. Hoping you did it only to rock even bigger ones!
        Have a break and I'll be waiting for your new entries
        Use /create command to start all over again

        Type `/help create` for more information
        """.trimIndent()

    val deleteEmpty =
        """
        I'm not the police, but can stop the party. Which one though? 🚨

        Perhaps you forgot to enter the name of party to remove
        Follow the /delete command with the name of redundant party

        Type `/help delete` for more information
        """.trimIndent()

    val permissionDenyCallback =
        """
        Action is available for admins only. 🙅‍
        """.trimIndent()
}
