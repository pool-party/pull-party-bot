package com.github.pool_party.pull_party_bot.commands.messages

val ON_ALIAS_PARSE_FAIL =
    """
    Don't mess up with the cloning 👯‍♀️

    Follow the /alias command with a new party name and existing party to clone

    Type `/help alias` for more information
    """.trimIndent()

fun onAliasSuccess(aliasName: String) =
    """Party $aliasName is good to go!"""

fun onAliasFail(aliasName: String) =
    """

    """.trimIndent() // TODO wait for partyDao.createAlias update.
