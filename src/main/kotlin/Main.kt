import com.elbekD.bot.Bot
import com.elbekD.bot.server
import com.elbekD.bot.util.AllowedUpdate

fun main() {
    val app = "https://pullpartybot.herokuapp.com"
    val token = System.getenv("TELEGRAM_TOKEN") ?: throw RuntimeException("Unable to get system variable for token")
    val userName = "PullPartyBot"
    val bot = Bot.createWebhook(userName,token) {
        url = "${app}/${token}"
        allowedUpdates = listOf(AllowedUpdate.Message)
        server {
            host = "0.0.0.0"
            port = System.getenv("PORT").toInt()
        }
    }
    
    bot.onCommand("/start") { msg, _ ->
        bot.sendMessage(msg.chat.id, "Hello World!")
    }

    bot.start()
}
