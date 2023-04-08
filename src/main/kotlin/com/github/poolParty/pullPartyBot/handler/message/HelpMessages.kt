package com.github.poolParty.pullPartyBot.handler.message

import com.github.poolParty.pullPartyBot.Configuration

object HelpMessages {

    val common =
        """
        Available commands:

            /start - start the conversation and see welcoming message
            /help  - show this usage guide
            /help <command> - show the usage guide of given command
            /list  - show all the parties of the chat and their members
            /list <entries> - show the parties and their members according to entries
            /clear - delete all parties of the chat

            @partyName  - tag existing party right in your message (bot has to be an admin)
            /party  <party-names> - tag the members of the given parties
            /delete <party-names> - delete the parties you provided

            /create <party-name users-list> - create new party with mentioned users
            /alias  <alias-name party-name> - create a new party with the same users
            /change <party-name users-list> - change an existing party
            /add    <party-name users-list> - add new users to the given party
            /remove <party-name users-list> - remove given users from the provided party

            /rude      <on/off> - switch RUDE(CAPS LOCK) mode
            /feedback <message> - share your ideas and experience with developers
        """.trimIndent()

    val onError =
        """
        The Lord helps those who help themselves 👼

        Expected no arguments or command to explain
        Follow /help with the unclear command or leave empty for general guide
        """.trimIndent()

    val start =
        """
        /start - start the conversation and see welcoming message

        Or simply remember how it all started 😉
        """.trimIndent()

    val list =
        """
        /list entry1? entry2?... - show the parties of the chat and their members

        Returns all parties with no arguments given
        Entry is either user or party-name:
          - on the given party shows its members
          - on the given user shows all parties he is part of
        Doesn't show @admins party
        """.trimIndent()

    val clear =
        """
        /clear - delete all parties of the chat

        Type `/clear delete` for more information
        """.trimIndent()

    val party =
        """
        /party <party-names> - tag the members of the given parties

        Keep in mind that you can simply tag the parties with `@<party-name>` syntax (bot has to be an admin)
        If you mention multiple parties - their members will be gathered in a single message and will have no repeats
        """.trimIndent()

    val delete =
        """
        /delete <party-names> - delete the parties you provided

        Only admins have access to /delete and /clear commands
        @admins is a reserved party and can't be deleted
        """.trimIndent()

    val create =
        """
        /create <party-name users-list> - create new party with mentioned users

        Users within the party are not repeating
        Party should consist of at least one user
        You can enter users with or without `@` symbol
        @admins is a reserved party and already exists
        `@`, ${Configuration.PROHIBITED_SYMBOLS.joinToString { "`$it`" }} symbols and trailing `-` are not allowed in the party name
        """.trimIndent()

    val change =
        """
        /change <party-name users-list> - change an existing party

        A shorter way to delete and create the party again 👍
        @admins party can't be changed, follows all /create rules

        Type `/help create` for more information
        """.trimIndent()

    val add =
        """
        /add <party-name users-list> - add new users to the given party

        You can enter users with or without `@` symbol
        You can't update @admins party
        """.trimIndent()

    val remove =
        """
        /remove <party-name users-list> - remove given users from the provided party

        Action can't leave the party empty
        Action can't leave the party with a single user if name of the user is equal to the party name
        You can enter users with or without `@` symbol
        You can't change @admins party
        """.trimIndent()

    val feedback =
        """
        /feedback <message> - share your ideas and experience with developers

        You might suggest the functionality you want to have, report the bugs or share your experience
        We are always willing to get better for the comfort of our users!
        """.trimIndent()

    val alias =
        """
        /alias  <alias-name party-name> - create a new party with the same users

        Party party-name should exist
        `@`, ${Configuration.PROHIBITED_SYMBOLS.joinToString { "`$it`" }} symbols and trailing `-` are not allowed in the alias name
        """.trimIndent()
}
