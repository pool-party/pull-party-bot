package com.github.pool_party.pull_party_bot.commands.messages

val ON_ALIAS_PARSE_FAIL =
    """
    Don't mess up with the cloning 👯‍♀️

    Follow the /alias command with a new party name and existing party to clone

    Type `/help alias` for more information
    """.trimIndent()

fun onAliasSuccess(aliasName: String) =
    """Party $aliasName is good to go!"""

val ON_ALIAS_FAIL =
    """
    Don't mess up with the cloning 👯‍♀️

    Seems like a party you would like to clone doesn't exist
    You can check a list of parties with /list command
    Follow the /alias command with a new party name and existing party to clone

    Type `/help alias` for more information
    """.trimIndent()
