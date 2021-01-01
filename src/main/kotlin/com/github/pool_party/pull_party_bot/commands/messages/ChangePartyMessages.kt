package com.github.pool_party.pull_party_bot.commands.messages

import com.github.pool_party.pull_party_bot.Configuration

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

    Type /help for more information
    """.trimIndent()

val ON_CHANGE_REQUEST_FAIL =
    """
    Party didn't started yet, but you already changing the plans. 😥

    Perhaps you wanted to create a new party with /create
    Follow /add or /change command with the name of existing party and new users

    Type /help for more information
    """.trimIndent()

val ON_REMOVE_REQUEST_FAIL =
    """
    It is easier to break than to make. 🧱

    Perhaps this action will leave the party invalid or given party doesn't exist at all
    Follow /remove command with the name of existing party and users to delete

    Type `/help remove` for more information
    """.trimIndent()

val ON_PARTY_NAME_FAIL =
    """
    As you name the boat, so shall it float. ☝️

    Party name should consist of less than 50 non-blank symbols
    `@`, ${Configuration.PROHIBITED_SYMBOLS.joinToString { "`$it`" }} symbols and trailing `-` are not allowed in the party name

    Type help for more information
    """.trimIndent()

val ON_USERS_FAIL =
    """
    Some guests couldn't pass the face control. 👮

    Usernames should consist of latin letters, digits and underscores only
    Allowed length of telegram username is from 5 to 32 characters

    Type /help for more information
    """.trimIndent()

fun onCreateSuccess(partyName: String) =
    """Party $partyName successfully created!"""

fun onChangeSuccess(partyName: String) =
    """Party $partyName changed beyond recognition!"""

fun onAddSuccess(partyName: String) =
    """Party $partyName is getting bigger and bigger!"""

fun onDeleteSuccess(partyName: String) =
    """Party $partyName lost somebody, but not the vibe!"""
