package com.github.poolParty.pullPartyBot.handler.message

import com.github.poolParty.pullPartyBot.Configuration
import com.github.poolParty.pullPartyBot.handler.escapeMarkdown

object ChangePartyMessages {

    val changeEmpty =
        """
        The wind of change is blowing. But where? 🤨

        At least name and a single valid user should be provided
        You can change the party with /change, /add and /remove commands

        Type /help for more information
        """.trimIndent()

    val changeRequestFail =
        """
        Party didn't started yet, but you already changing the plans. 😥

        Perhaps you wanted to create a new party with /create
        Follow /add or /change command with the name of existing party and new users

        Type /help for more information
        """.trimIndent()

    val createEmpty =
        """
        No people - no party. 😔

        At least name and a single valid user should be provided
        Follow the /create command with the new party name and members of a new group

        Type `/help create` for more information
        """.trimIndent()

    val createRequestFail =
        """
        Someone is already rocking this party. 🥳

        Perhaps you wanted to change existing group with /change
        Follow the /create or /alias command only with a new party name

        Type `/help create`, `/help alias` or `/help change` for more information
        """.trimIndent()

    val removeRequestFail =
        """
        It is easier to break than to make. 🧱

        Perhaps this action will leave the party invalid or given party doesn't exist at all
        Follow /remove command with the name of existing party and users to delete

        Type `/help remove` for more information
        """.trimIndent()

    val singletonParty =
        """
        A person may not be a party, but if you had a few... 👫

        Perhaps you want to create a party for a single user with an alternative name though
        You can mention a single user by his original name with telegram functionality

        Type `/help create` or `/help change` for more information
        """.trimIndent()

    val partyNameFail =
        """
        As you name the boat, so shall it float. ☝️

        Party or alias name should consist of less than 50 non-blank symbols
        `@`, ${Configuration.PROHIBITED_SYMBOLS.joinToString { "`$it`" }} symbols and trailing `-` are not allowed in the party name

        Type /help for more information
        """.trimIndent()

    val usersFail =
        """
        Some guests couldn't pass the face control. 👮

        Usernames should consist of latin letters, digits and underscores only
        Allowed length of telegram username is from 5 to 32 characters

        Type /help for more information
        """.trimIndent()

    val adminsPartyChange =
        """
        I would be careful disturbing these ladies and gentlemen. 🤫

        @admins is a reserved group, you can't create, change or delete it
        Try to make a new party instead with /create command

        Type /help for more information
        """.trimIndent()

    val senderFail =
        """
        Only the chosen ones can perform these actions. 😎

        I were not able to recognize the permissions you have in this chat
        Operation aborted, ask group admins if you still want to make it happen

        Type `/help delete` or `/help clear` for more information
        """.trimIndent()

    val permissionDeny =
        """
        Parties are all I have. You can't just do this. 🤬

        Sad enough for me, parties can be deleted, but only by admins
        Only group administrators can perform /delete and /clear commands
        You can pull @admins party to ask them to perform this command

        Type `/help delete` or `/help clear` for more information
        """.trimIndent()

    fun addSuccess(partyName: String) = "Party ${partyName.escapeMarkdown()} is getting bigger and bigger!"

    fun changeSuccess(partyName: String) = "Party ${partyName.escapeMarkdown()} changed beyond recognition!"

    fun createSuccess(partyName: String) = "Party ${partyName.escapeMarkdown()} successfully created!"

    fun deleteSuccess(partyName: String) = "Party ${partyName.escapeMarkdown()} lost somebody, but not the vibe!"
}
