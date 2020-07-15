import com.elbekD.bot.Bot
import com.elbekD.bot.server
import com.elbekD.bot.util.AllowedUpdate

const val APP_URL = "https://pullpartybot.herokuapp.com"
const val USER_NAME = "PullPartyBot"

fun main() {
    val token = System.getenv("TELEGRAM_TOKEN") ?: throw RuntimeException("Unable to get system variable for token")
    val bot = Bot.createWebhook(USER_NAME,token) {
        url = "${APP_URL}/${token}"
        allowedUpdates = listOf(AllowedUpdate.Message)

        server {
            host = "0.0.0.0"
            port = (System.getenv("PORT") ?: throw RuntimeException("Unable to get system variable for port")).toInt()
        }
    }

    bot.onCommand("/start") { msg, _ ->
        bot.sendMessage(msg.chat.id, "Hello World!")
    }

    bot.start()
}
