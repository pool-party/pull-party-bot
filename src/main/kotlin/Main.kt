import com.elbekD.bot.Bot

fun main() {
    val token = System.getenv("TELEGRAM_TOKEN") ?: throw RuntimeException("Unable to get system variable for token")
    val userName = "PullPartyBot"
    val bot = Bot.createPolling(userName, token)

    bot.onCommand("/start") { msg, _ ->
        bot.sendMessage(msg.chat.id, "Hello World!")
    }

    bot.start()
}
