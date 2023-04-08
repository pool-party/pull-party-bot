package com.github.poolParty.pullPartyBot.handler.message

import com.github.poolParty.pullPartyBot.Configuration
import kotlin.math.min

// Comment template:
// """
// Interaction message
// *Blank line*
// Try to predict possible action led to this mistake and make a suggestion
// Small command usage guide
// *Blank line*
// /help command suggestion
// """

object InformationMessages {

    val init =
        """
        Hey! I'm a PullPartyBot!

        I can manage different parties inside your chat and tag their members whenever you need!

        You can start by creating a new party in your chat with /create command
        Or try to tag admins with default @admins party

        It would be very kind of you to promote me to admin, so I could tag the parties implicitly right in your messages

        Type /help for more information
        """.trimIndent()

    val stalePartyRemove =
        """
        Some of the parties are more dead than alive, you might get rid of the least recently used:
        """.trimIndent()

    val partyMisspell =
        """
        Hmm, sounds familiar... Perhaps you meant:
        """.trimIndent()

    val feedbackSuccess =
        """
        Your feedback has been sent to the maintainers 👨‍💻

        Thanks for helping us to improve your user experience!
        """.trimIndent()

    fun feedback(username: String?, title: String?) =
        "New #feedback from @$username in ${if (title != null) "\"$title\"" else "private chat"}:\n\n"

    fun error(throwable: Throwable): String {
        val intro = "New #error:\n\n`${throwable.message}`\n\n"

        val stackTrace = throwable.stackTraceToString()
        val tenLines = stackTrace.lineSequence().take(10).joinToString("\n")
        val stackTraceTrimmed = if (tenLines.length + 6 < Configuration.MESSAGE_LENGTH - intro.length) tenLines
        else stackTrace.substring(0 until min(stackTrace.length, 1000 - intro.length))

        return "$intro```$stackTraceTrimmed```"
    }

    val pingCreatorMismatch =
        """
        Available for initial requesting user only 🚫
        """.trimIndent()

    val adminsPartyFail =
        """
        Admins?! What admins?! 🥴

        There is no admins in private chats or channels
        @admins is a reserved group, you can't change or delete it

        Type /help for more information
        """.trimIndent()
}
