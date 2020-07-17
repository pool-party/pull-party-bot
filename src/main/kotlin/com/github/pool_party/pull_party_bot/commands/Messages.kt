package com.github.pool_party.pull_party_bot.commands

// Comment template:
// """
// Interaction message
// *Blank line*
// Try to predict possible action led to this mistake and make a suggestion
// Small command usage guide
// *Blank line*
// /help command suggestion
// """

val INIT_MSG =
    """
    Hey! I'm a PullPartyBot!

    I can manage different parties inside your chat and tag their members whenever you need!

    It would be very kind of you to start with creating the group of all members
    Type in `/create all` and follow it with tags of all users
    You can skip this step and simply create parties on your own if you want

    Type /help for more information
    """.trimIndent()

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
    Follow the /create command with the new party name and members of a new group

    Type /help for more information
    """.trimIndent()

val ON_CREATE_REQUEST_FAIL =
    """
    Someone is already rocking this party. 🥳

    Perhaps you wanted to update existing group with /update
    Follow the /create command with the new party name and members of a new group

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

val ON_DELETE_EMPTY =
    """
    I'm not the police, but can stop the party. Which one though? 🚨

    Perhaps you forgot to enter the name of party to remove
    Follow the /delete command with the name of redundant party

    Type /help for more information
    """.trimIndent()

val ON_UPDATE_FAIL =
    """
    The wind of change is blowing. But where? 🤨

    At least name and a single user should be provided
    Follow the /update command with the existing party name and its new members

    Type /help for more information
    """.trimIndent()

val ON_UPDATE_REQUEST_FAIL =
    """
    Party didn't started yet, but you already changing the plans. 😥

    Perhaps you wanted to create a new party with /create
    Follow the /update command with the existing party name and its new members

    Type /help for more information
    """.trimIndent()
