import com.elbekD.bot.Bot
import com.elbekD.bot.server
import com.elbekD.bot.util.AllowedUpdate

const val APP_URL = "https://pullpartybot.herokuapp.com"
const val USER_NAME = "PullPartyBot"
const val DEFAULT_PORT = 80

fun main() {
    val token = System.getenv("TELEGRAM_TOKEN") ?: throw RuntimeException("Unable to get system variable for token")
    val bot = Bot.createWebhook(USER_NAME,token) {
        url = "${APP_URL}/${token}"
        allowedUpdates = listOf(AllowedUpdate.Message)

        server {
            host = "0.0.0.0"
            port = System.getenv("PORT")?.toInt() ?: DEFAULT_PORT
        }
    }

    bot.onCommand("/start") { msg, _ ->
        bot.sendMessage(msg.chat.id, "Hello World!")
    }

    bot.onCommand("/help") { msg, _ ->
        bot.sendMessage(msg.chat.id, HELP_MSG);
    }

    bot.onCommand("/create") { msg, list ->
        val parsedList = list?.split(' ')?.map { it.trim() }
        val chatId = msg.chat.id

        if (parsedList.isNullOrEmpty() || parsedList.size == 1) {
            bot.sendMessage(chatId, onCreateFail)
        } else {
            val partyName = parsedList[0]
            val users = parsedList.drop(1)

            bot.sendMessage(chatId, "Party $partyName successfully created!")
        }
    }

    bot.start()
}

const val HELP_MSG =
    """
Available commands:

    /start    - awake the bot
    /help     - show this usage guide
    /create <name @user1 @user2 ...>    - create new party with given name and users
    """

const val onCreateFail =
    """
No people - no party 😔

Follow the create command with the party name and members of new group
At least name and single user should be provided

Type /help for more information
    """
