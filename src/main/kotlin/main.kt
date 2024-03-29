import lu.braungilles.BuildConfig
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

fun main(args: Array<String>) {
    Main()
}

typealias ChatId = String

class Main {

    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        logger.info("Starting Version " + BuildConfig.VERSION)
        val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
        botsApi.registerBot(TeleBot)
        logger.info("Running!")
        ChatManager.loadSavedChats()
    }

}