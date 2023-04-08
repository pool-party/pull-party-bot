package com.github.poolParty.pullPartyBot.handler.message

object ListMessages {

    val success =
        """
        The parties I know:


        """.trimIndent()

    val empty =
        """
        I don't know any parties in this chat yet 😢
        """.trimIndent()

    val argumentSuccess =
        """
        Matched parties:


        """.trimIndent()

    val argumentEmpty =
        """
        I can't find who you are looking for 📭

        Perhaps you misspelled the name of party or user
        Follow the /list command with correct users and party names

        Type `/help list` for more information
        """.trimIndent()
}
