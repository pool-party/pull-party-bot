package com.github.pool_party.pull_party_bot.commands.messages

import com.github.pool_party.pull_party_bot.Configuration

val ON_HELP_ERROR =
    """
    The Lord helps those who help themselves 👼

    Expected no arguments or command to explain
    Follow /help with the unclear command or leave empty for general guide
    """.trimIndent()

val ON_LIST_EMPTY =
    """
    I don't know any parties in this chat yet 😢
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
    You can change the party with /change, /add and /remove commands

    Type `/help` for more information
    """.trimIndent()

val ON_CHANGE_REQUEST_FAIL =
    """
    Party didn't started yet, but you already changing the plans. 😥

    Perhaps you wanted to create a new party with /create
    Follow /add or /change command with the name of existing party and new users

    Type `/help` for more information
    """.trimIndent()

val ON_REMOVE_REQUEST_FAIL =
    """
    It is easier to break than to make. 🧱

    Perhaps this action will leave the party invalid or given party doesn't exist at all
    Follow /remove command with the name of existing party and users to delete

    Type `/help remove` for more information
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

val ON_ADMINS_PARTY_CHANGE =
    """
    I would be careful disturbing these ladies and gentlemen. 🤫

    @admins is a reserved group, you can't create, change or delete it
    Try to make a new party instead with /create command

    Type `/help` for more information
    """.trimIndent()

val ON_ADMINS_PARTY_FAIL =
    """
    Admins?! What admins?! 🥴

    There is no admins in private chats or channels
    @admins is a reserved group, you can't change or delete it

    Type `/help` for more information
    """.trimIndent()

val ON_PARTY_NAME_FAIL =
    """
    As you name the boat, so shall it float. ☝️

    Party name should consist of less than 50 non-blank symbols
    `@`, ${Configuration.PROHIBITED_SYMBOLS.joinToString { "`$it`" }} symbols and trailing `-` are not allowed in the party name

    Type `/help` for more information
    """.trimIndent()

val ON_USERS_FAIL =
    """
    Some guests couldn't pass the face control. 👮

    Usernames should consist of latin letters, digits and underscores only
    Allowed length of telegram username is from 5 to 32 characters

    Type `/help` for more information
    """.trimIndent()
