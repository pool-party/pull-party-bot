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

    It would be very kind of you to promote me to admin, so I could tag the parties implicitly right in your messages

    Type /help for more information
    """.trimIndent()

val HELP_MSG =
    """
    Available commands:

        /start - start the conversation and see welcoming message
        /clear - delete all parties of the chat
        /help  - show this usage guide
        /help <command> - show the usage guide of given command
        /list  - show all the parties of the chat and their members
        /list <entries> - show the parties and their members according to entries

                  @partyName  - tag existing party right in your message
        /party  <party-names> - tag the members of existing parties
        /delete <party-names> - delete the parties you provided

        /create <party-name users-list> - create new party with mentioned users
        /change <party-name users-list> - change an existing party

        /rude <on/off> - switch RUDE(CAPS LOCK) mode
    """.trimIndent()

val HELP_START =
    """
    /start - start the conversation and see welcoming message

    Or simply remember how it all started 😉
    """.trimIndent()

val HELP_LIST =
    """
    /list entry1? entry2?... - show the parties of the chat and their members

    Returns all parties with no arguments given
    Entry is either user or partyName:
      - on the given party shows its members
      - on the given user shows all parties he is part of
    Doesn't show @admins party
    """.trimIndent()

val HELP_PARTY =
    """
    /party <party-names> - tag the members of existing parties

    Keep in mind that you can simply tag the parties with `@<party-name>` syntax
    If you mention multiple parties - their members will be gathered in a single message and will have no repeats
    """.trimIndent()

val HELP_DELETE =
    """
    /delete <party-names> - delete the parties you provided

    Only admins have access to /delete and /clear commands
    @admins is a reserved party and can't be deleted
    """.trimIndent()

val HELP_CLEAR =
    """
    /clear - delete all parties of the chat

    Type `/clear delete` for more information
    """.trimIndent()

val HELP_CREATE =
    """
    /create <party-name users-list> - create new party with mentioned users

    Users within the party are not repeating
    Party should consist of at least one user
    You can enter users with or without `@` symbol
    @admins is a reserved party and already exists
    `@`, ${PROHIBITED_SYMBOLS.joinToString { "`$it`" }} symbols and trailing `-` are not allowed in the party name
    """.trimIndent()

val HELP_CHANGE =
    """
    /change <party-name users-list> - change an existing party

    A shorter way to delete and create the party again 👍
    @admins party can't be changed, follows all /create rules

    Type `/help create` for more information
    """.trimIndent()

val HELP_RUDE =
    """
    /rude <on/off> - switch RUDE(CAPS LOCK) mode

    To enable or disable mode follow the command with either `on` or `off`
    The mode is turned off by default and never affects error messages
    """.trimIndent()

val ON_HELP_ERROR =
    """
    The Lord helps those who help themselves 👼

    Expected no arguments or command to explain
    Follow /help with the unclear command or leave empty for general guide
    """.trimIndent()

val ON_LIST_SUCCESS =
    """
    The parties I know:


    """.trimIndent()

val ON_LIST_EMPTY =
    """
    I don't know any parties in this chat yet 😢
    """.trimIndent()

val ON_ARGUMENT_LIST_SUCCESS =
    """
    Matched parties:


    """.trimIndent()

val ON_ARGUMENT_LIST_EMPTY =
    """
    I can't find who you are looking for 📭

    Perhaps you misspelled the name of party or user
    Follow the /list command with correct users and party names

    Type `/help list` for more information
    """.trimIndent()

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

val ON_DELETE_EMPTY =
    """
    I'm not the police, but can stop the party. Which one though? 🚨

    Perhaps you forgot to enter the name of party to remove
    Follow the /delete command with the name of redundant party

    Type `/help delete` for more information
    """.trimIndent()

val ON_SINGLETON_PARTY =
    """
    A person may not be a party, but if you had a few... 👫

    Perhaps you want to create a party for a single user with an alternative name though
    You can mention a single user by his original name with telegram functionality

    Type `/help create` or `/help change` for more information
    """.trimIndent()

val ON_CREATE_EMPTY =
    """
    No people - no party. 😔

    At least name and a single valid user should be provided
    Follow the /create command with the new party name and members of a new group

    Type `/help create` for more information
    """.trimIndent()

val ON_CREATE_REQUEST_FAIL =
    """
    Someone is already rocking this party. 🥳

    Perhaps you wanted to change existing group with /change
    Follow the /create command with the new party name and members of a new group

    Type `/help create` or `/help change` for more information
    """.trimIndent()

val ON_CHANGE_EMPTY =
    """
    The wind of change is blowing. But where? 🤨

    At least name and a single valid user should be provided
    Follow the /change command with the existing party name and its new members

    Type `/help change` for more information
    """.trimIndent()

val ON_CHANGE_REQUEST_FAIL =
    """
    Party didn't started yet, but you already changing the plans. 😥

    Perhaps you wanted to create a new party with /create
    Follow the /change command with the existing party name and its new members

    Type `/help create` or `/help change` for more information
    """.trimIndent()

val ON_RUDE_FAIL =
    """
    With great power comes great responsibility. 🧐

    RUDE mode is definitely a great power and you have to use it right!
    Follow the /rude command with either "on" or "off"

    Type `/help rude` for more information
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

    Sad enough for me, parties can be lost and forgotten, but only by admins
    Only group administrators can perform /delete and /clear commands
    You can pull @admins party to ask them to perform this command

    Type `/help delete` or `/help clear` for more information
    """.trimIndent()

val ON_CLEAR_SUCCESS =
    """
    Life is a party. Chat is dead then. 😭

    All parties are over. Hope you did it only to rock even bigger ones
    Let's rest just a little and I'll be waiting for your new entries
    Use /create command to start all over again

    Type `/help create` for more information
    """.trimIndent()

val ON_ADMINS_PARTY_CHANGE =
    """
    I would be careful disturbing these ladies and gentlemen. 🤫

    @admins is a reserved group, you can't create, change or delete it
    Try to make a new party instead with /create command

    Type `/help change` or `/help delete` for more information
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
    `@`, ${PROHIBITED_SYMBOLS.joinToString { "`$it`" }} symbols and trailing `-` are not allowed in the party name

    Type `/help create` for more information
    """.trimIndent()

val ON_USERS_FAIL =
    """
    Some guests couldn't pass the face control. 👮

    Usernames should consist of latin letters, digits and underscores only
    Allowed length of telegram username is from 5 to 32 characters

    Type `/help create` or `/help change` for more information
    """.trimIndent()
