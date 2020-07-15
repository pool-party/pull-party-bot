import com.elbekD.bot.Bot
import com.elbekD.bot.server
import com.elbekD.bot.types.Message
import com.elbekD.bot.util.AllowedUpdate
import io.ktor.application.*

import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.sslConnector

fun main() {
    val ngrok = "https://gankedbymomtestdeploy.herokuapp.com"
    val token = System.getenv("TELEGRAM_TOKEN") ?: throw RuntimeException("Unable to get system variable for token")
    val userName = "PullPartyBot"
    val bot = Bot.createWebhook(userName,token) {
        url = "${ngrok}/${token}"
        allowedUpdates = listOf(AllowedUpdate.Message)
        server {
            host = "0.0.0.0"
            port = 8443
        }
    }

    bot.onCommand("/start") { msg, _ ->
        bot.sendMessage(msg.chat.id, "Hello World!")
    }


//    embeddedServer(Netty,80) {
//        routing {
//            get("/"){
//                call.respondText("Hi Mark", ContentType.Text.Html)
//            }
//            post("/${token}") {
//                val response = call.receiveText()
//                print(response)
//                call.respond(HttpStatusCode.OK)
//            }
//        }
//    }.start()
    bot.start()
}
