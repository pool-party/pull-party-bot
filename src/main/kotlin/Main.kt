import com.elbekD.bot.Bot

fun main() {
    val token = System.getenv("TELEGRAM_TOKEN") ?: throw RuntimeException("Unable to get system variable for token")
    val userName = "PullPartyBot"
    val bot = Bot.createPolling(userName, token)

    /**
     * Initiate the dialog with bot (might ask to set chat members in the future).
     */
    bot.onCommand("/start") { msg, _ ->
        bot.sendMessage(msg.chat.id, "Hello World!")
    }

    /**
     * Return the help message.
     */
    bot.onCommand("/help") { msg, _ ->
        bot.sendMessage(msg.chat.id, HELP_MSG);
    }

    /**
     * Create a new party with given members.
     */
    bot.onCommand("/create") { msg, list ->
        val parsedList = list?.split(' ')?.map { it.trim() }
        val chatId = msg.chat.id

        if (parsedList.isNullOrEmpty() || parsedList.size == 1) {
            bot.sendMessage(chatId, ON_CREATE_FAIL)
        } else {
            val partyName = parsedList[0]
            val users = parsedList.drop(1)

            // DataBase work.

            bot.sendMessage(chatId, "Party $partyName successfully created!")
        }
    }

    /**
     * Ping the party members.
     */
    bot.onCommand("/party") { msg, name ->
        if (name.isNullOrBlank()) {
            bot.sendMessage(msg.chat.id, ON_PARTY_FAIL)
        } else {
            // DataBase work.

            bot.sendMessage(
                msg.chat.id,
                if (true) { // success condition of DataBase.
                    "SUCCESS_RESPONSE" // response on success.
                } else {
                    ON_PARTY_REQUEST_FAIL
                }
            )
        }
    }

    /**
     * Delete existing party.
     */
    bot.onCommand("/delete") { msg, name ->

        // DataBase work.

        bot.sendMessage(msg.chat.id, "Party $name is just a history now \uD83D\uDC4D")
    }

    bot.start()
}

val HELP_MSG =
    """
Available commands:

    /start    - awake the bot
    /help     - show this usage guide
    /create <name @user1 @user2 ...>    - create new party with given name and users
    """.trimIndent()

val ON_CREATE_FAIL =
    """
No people - no party 😔

At least name and single user should be provided
Follow the /create command with the party name and members of new group

Type /help for more information
    """.trimIndent()

val ON_PARTY_FAIL =
    """
I could call up all parties, but it doesn't sound like a good idea. 🤪

Perhaps you forgot to enter the party name.
Follow the /party command with the name of existing party

Type /help for more information
    """.trimIndent()

val ON_PARTY_REQUEST_FAIL =
    """
I am not aware of this party. You didn't invite me? 🤔

Perhaps you wanted to create the party or misspelled it's name?
Follow the /party command with the name of existing party

Type /help for more information
    """.trimIndent()
