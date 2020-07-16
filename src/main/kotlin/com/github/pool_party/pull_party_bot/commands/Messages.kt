package com.github.pool_party.pull_party_bot.commands

val HELP_MSG =
    """
    Available commands:

        /start - awake the bot
        /help  - show this usage guide
        /list  - show the parties of the chat

        /party  <party-name> - tag the members of existing party
        /delete <party-name> - forget the party like it had never happened

        /create <party-name users-list> - create new party
        /update <party-name users-list> - update an existing party
    """.trimIndent()

val ON_CREATE_FAIL =
    """
    No people - no party. 😔

    At least name and a single user should be provided
    Follow the /create command with the party name and members of a new group

    Type /help for more information
    """.trimIndent()

val ON_CREATE_REQUEST_FAIL =
    """
    This team is already exists
    """.trimIndent()//TODO better message

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

val ON_DELETE_EMPTY =
    """
    I'm not the police, but can stop the party. Which one though? 🚨

    Perhaps you forgot to enter the name of party to remove
    Follow the /delete command with the name of redundant party

    Type /help for more information
    """.trimIndent()

val ON_PARTY_REQUEST_LIST_FAIL =
    """
    I don't know any teams in this chat 😢
    """.trimIndent()

val ON_UPDATE_REQUEST_FAIL =
    """
    No such team with this name:
    """.trimIndent()
