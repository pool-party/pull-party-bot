package com.github.pool_party.pull_party_bot.commands.messages

val ON_RUDE_FAIL =
    """
    With great power comes great responsibility. 🧐

    RUDE mode is definitely a great power and you have to use it right!
    Follow the /rude command with either "on" or "off"

    Type `/help rude` for more information
    """.trimIndent()

fun onRudeSuccess(isChanged: Boolean, status: String, statusEmoji: String) =
    """Rude mode ${if (isChanged) "is now" else "was already"} $status $statusEmoji!"""
