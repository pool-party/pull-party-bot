package com.github.pool_party.pull_party_bot.commands.messages

val ON_PARTY_EMPTY =
    """
    I could call up all parties, but it doesn't sound like a good idea. 🤪

    Perhaps you forgot to enter the party names
    Follow the /party command with the names of existing parties

    Type `/help party` for more information
    """.trimIndent()

val ON_PARTY_REQUEST_FAIL =
    """
    I am not aware of this party. You didn't invite me? 🤔

    Perhaps you wanted to /create the party or misspelled its name
    Follow the /party command with the names of existing parties

    Type `/help party` or `/help create` for more information
    """.trimIndent()

val ON_PARTY_REQUEST_FAILS =
    """
    I'm not that impudent to call up the parties I don't know. 😅

    Perhaps you misspelled some names
    Follow the /party command with the names of existing parties

    Type `/help party` for more information
    """.trimIndent()
