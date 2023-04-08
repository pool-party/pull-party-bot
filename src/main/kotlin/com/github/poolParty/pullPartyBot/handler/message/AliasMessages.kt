package com.github.poolParty.pullPartyBot.handler.message

import com.github.poolParty.pullPartyBot.handler.escapeMarkdown

object AliasMessages {

    val fail =
        """
        Don't mess up with the cloning 👯‍♀️

        Seems like a party you would like to clone doesn't exist
        You can check a list of parties with /list command
        Follow the /alias command with a new party name and existing party to clone

        Type `/help alias` for more information
        """.trimIndent()

    val parseFail =
        """
        Don't mess up with the cloning 👯‍♀️

        Follow the /alias command with a new party name and existing party to clone

        Type `/help alias` for more information
        """.trimIndent()

    fun success(aliasName: String) = "Party ${aliasName.escapeMarkdown()} is good to go!"
}
