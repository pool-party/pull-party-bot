package com.github.pool_party.pull_party_bot.commands.messages

import com.github.pool_party.pull_party_bot.Configuration

val HELP_START =
    """
    /start - start the conversation and see welcoming message

    Or simply remember how it all started 😉
    """.trimIndent()

val HELP_LIST =
    """
    /list entry1? entry2?... - show the parties of the chat and their members

    Returns all parties with no arguments given
    Entry is either user or party-name:
      - on the given party shows its members
      - on the given user shows all parties he is part of
    Doesn't show @admins party
    """.trimIndent()

val HELP_CLEAR =
    """
    /clear - delete all parties of the chat

    Type `/clear delete` for more information
    """.trimIndent()

val HELP_PARTY =
    """
    /party <party-names> - tag the members of the given parties

    Keep in mind that you can simply tag the parties with `@<party-name>` syntax (bot has to be an admin)
    If you mention multiple parties - their members will be gathered in a single message and will have no repeats
    """.trimIndent()

val HELP_DELETE =
    """
    /delete <party-names> - delete the parties you provided

    Only admins have access to /delete and /clear commands
    @admins is a reserved party and can't be deleted
    """.trimIndent()

val HELP_CREATE =
    """
    /create <party-name users-list> - create new party with mentioned users

    Users within the party are not repeating
    Party should consist of at least one user
    You can enter users with or without `@` symbol
    @admins is a reserved party and already exists
    `@`, ${Configuration.PROHIBITED_SYMBOLS.joinToString { "`$it`" }} symbols and trailing `-` are not allowed in the party name
    """.trimIndent()

val HELP_CHANGE =
    """
    /change <party-name users-list> - change an existing party

    A shorter way to delete and create the party again 👍
    @admins party can't be changed, follows all /create rules

    Type `/help create` for more information
    """.trimIndent()

val HELP_ADD =
    """
    /add <party-name users-list> - add new users to the given party

    You can enter users with or without `@` symbol
    You can't update @admins party
    """.trimIndent()

val HELP_REMOVE =
    """
    /remove <party-name users-list> - remove given users from the provided party

    Action can't leave the party empty
    Action can't leave the party with a single user if name of the user is equal to the party name
    You can enter users with or without `@` symbol
    You can't change @admins party
    """.trimIndent()

val HELP_RUDE =
    """
    /rude <on/off> - switch RUDE(CAPS LOCK) mode

    To enable or disable mode follow the command with either `on` or `off`
    The mode is turned off by default and never affects error messages
    """.trimIndent()

val HELP_FEEDBACK =
    """
    /feedback <message> - share your ideas and experience with developers

    You might suggest the functionality you want to have, report the bugs or share your experience
    We are always willing to get better for the comfort of our users!
    """.trimIndent()
