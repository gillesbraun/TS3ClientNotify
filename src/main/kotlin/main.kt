import org.slf4j.LoggerFactory
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi

fun main(args: Array<String>) {
    Main()
}

typealias ChatId = Long

class Main {

    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        ApiContextInitializer.init()
        val botsApi = TelegramBotsApi()
        botsApi.registerBot(TeleBot)
        logger.info("Running!")
        ChatManager.loadSavedChats()
    }

}