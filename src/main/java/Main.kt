import com.elbekD.bot.Bot

fun main(args: Array<String>) {
    val token = "<TOKEN>"
    val userName = "<USER_NAME>"
    val bot = Bot.createPolling(token, userName)

    bot.onCommand("/start") { msg, _ ->
        bot.sendMessage(msg.chat.id, "Hello World!")
    }
    
    bot.start()
}