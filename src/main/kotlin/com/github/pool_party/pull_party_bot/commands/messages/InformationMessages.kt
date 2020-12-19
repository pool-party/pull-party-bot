package com.github.pool_party.pull_party_bot.commands.messages

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
        /help  - show this usage guide
        /help <command> - show the usage guide of given command
        /list  - show all the parties of the chat and their members
        /list <entries> - show the parties and their members according to entries
        /clear - delete all parties of the chat

                  @partyName  - tag existing party right in your message (bot has to be an admin)
        /party  <party-names> - tag the members of the given parties
        /delete <party-names> - delete the parties you provided

        /create <party-name users-list> - create new party with mentioned users
        /change <party-name users-list> - change an existing party
        /add    <party-name users-list> - add new users to the given party
        /remove <party-name users-list> - remove given users from the provided party

        /rude      <on/off> - switch RUDE(CAPS LOCK) mode
        /feedback <message> - share your ideas and experience with developers
    """.trimIndent()
