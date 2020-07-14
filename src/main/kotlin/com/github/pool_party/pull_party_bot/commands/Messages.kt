package com.github.pool_party.pull_party_bot.commands

val HELP_MSG =
    """
    Available commands:

        /start    - awake the bot
        /help     - show this usage guide
        /create <name @user1 @user2 ...>    - create new party with given name and users
        /delete <name>    - delete party, if exists
        /party  <name>    - tag the members of existing party
    """.trimIndent()

val ON_CREATE_FAIL =
    """
    No people - no party 😔

    At least name and the single user should be provided
    Follow the /create command with the party name and members of a new group

    Type /help for more information
    """.trimIndent()

val ON_PARTY_FAIL =
    """
    I could call up all parties, but it doesn't sound like a good idea. 🤪

    Perhaps you forgot to enter the party name
    Follow the /party command with the name of existing party

    Type /help for more information
    """.trimIndent()

val ON_PARTY_REQUEST_FAIL =
    """
    I am not aware of this party. You didn't invite me? 🤔

    Perhaps you wanted to create the party or misspelled its name
    Follow the /party command with the name of existing party

    Type /help for more information
    """.trimIndent()
