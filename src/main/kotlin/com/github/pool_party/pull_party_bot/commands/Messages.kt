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

    You can start by creating a new party in your chat with /create command
    Or try to tag admins with default @admins party

    Type /help for more information
    """.trimIndent()

val HELP_MSG =
    """
    Available commands:

        /start - awake the bot
        /help  - show this usage guide
        /list  - show the parties of the chat

        /party  <party-names> - tag the members of existing parties
        /delete <party-names> - forget the parties as they had never happened

        /create <party-name users-list> - create new party
        /change <party-name users-list> - change an existing party

        /rude <on/off> - switch RUDE(CAPS LOCK) mode
    """.trimIndent()

val ON_LIST_SUCCESS =
    """
    The parties I know:


    """.trimIndent()

val ON_LIST_EMPTY =
    """
    I don't know any parties in this chat yet 😢
    """.trimIndent()

val ON_PARTY_EMPTY =
    """
    I could call up all parties, but it doesn't sound like a good idea. 🤪

    Perhaps you forgot to enter the party names
    Follow the /party command with the names of existing parties

    Type /help for more information
    """.trimIndent()

val ON_PARTY_REQUEST_FAIL =
    """
    I am not aware of this party. You didn't invite me? 🤔

    Perhaps you wanted to /create the party or misspelled its name
    Follow the /party command with the names of existing parties

    Type /help for more information
    """.trimIndent()

val ON_PARTY_REQUEST_FAILS =
    """
    I'm not that impudent to call up the parties I don't know. 😅

    Perhaps you misspelled some names
    Follow the /party command with the names of existing parties

    Type /help for more information
    """.trimIndent()

val ON_DELETE_EMPTY =
    """
    I'm not the police, but can stop the party. Which one though? 🚨

    Perhaps you forgot to enter the name of party to remove
    Follow the /delete command with the name of redundant party

    Type /help for more information
    """.trimIndent()

val ON_CREATE_EMPTY =
    """
    No people - no party. 😔

    At least name and a single valid user should be provided
    Follow the /create command with the new party name and members of a new group

    Type /help for more information
    """.trimIndent()

val ON_CREATE_REQUEST_FAIL =
    """
    Someone is already rocking this party. 🥳

    Perhaps you wanted to change existing group with /change
    Follow the /create command with the new party name and members of a new group

    Type /help for more information
    """.trimIndent()

val ON_CHANGE_EMPTY =
    """
    The wind of change is blowing. But where? 🤨

    At least name and a single valid user should be provided
    Follow the /change command with the existing party name and its new members

    Type /help for more information
    """.trimIndent()

val ON_CHANGE_REQUEST_FAIL =
    """
    Party didn't started yet, but you already changing the plans. 😥

    Perhaps you wanted to create a new party with /create
    Follow the /change command with the existing party name and its new members

    Type /help for more information
    """.trimIndent()

val ON_RUDE_FAIL =
    """
    With great power comes great responsibility. 🧐
    RUDE mode is definitely a great power and you have to use it right!

    Follow the /rude command with either "on" or "off"

    Type /help for more information
    """.trimIndent()

val ON_SENDER_FAIL =
    """
    Only the chosen ones can perform these actions. 😎

    I were not able to recognize the permissions you have in this chat
    Operation aborted, ask group admins if you still want to make it happen

    Type /help for more information
    """.trimIndent()

val ON_PERMISSION_DENY =
    """
    Parties are all I have. You can't just do this. 🤬

    Sad enough for me, parties can be lost and forgotten, but only by admins
    Only group administrators can perform /delete and /clear commands
    You can pull @admins party to ask them to perform this command

    Type /help for more information
    """.trimIndent()

val ON_CLEAR_SUCCESS =
    """
    Life is a party. Chat is dead then. 😭

    All parties are over. Hope you did it only to rock even bigger ones
    Let's rest just a little and I'll be waiting for your new entries
    Use /create command to start all over again

    Type /help for more information
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

val ON_PARTY_NAME_FAIL =
    """
    As you name the boat, so shall it float. ☝️

    Party name should consist of some non-blank symbols
    `@` symbols are not allowed in the party name

    Type /help for more information
    """.trimIndent()

val ON_USERS_FAIL =
    """
    Some guests couldn't pass the face control. 👮

    Usernames should consist of latin letters, digits and underscores only
    Allowed length of telegram username is from 5 to 32 characters

    Type /help for more information
    """.trimIndent()
