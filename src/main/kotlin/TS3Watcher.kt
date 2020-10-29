import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException

class TS3Watcher(
    val chatId: ChatId,
): Thread() {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run() {
        val config = ChatManager.getChat(chatId)
            ?: throw IllegalArgumentException("config doesn't exist")

        val tsWatcher = TeamSpeakService(
            config.ts3Host ?: throw IllegalArgumentException("Host must be set"),
            config.ts3Username ?: throw IllegalArgumentException("Username must be set"),
            config.ts3Password ?: throw IllegalArgumentException("Password must be set"),
        )

        try {
            while (true) {
                try {
                    val clients = tsWatcher.listClients()
                    TeleBot.sendClientInfo(chatId, clients)
                } catch (e: Exception) {
                    logger.error("error listing clients", e)
                }
                sleep(5000L)
            }
        } catch (e: InterruptedException) {
            logger.info("interrupt received.. exiting loop")
        }

        tsWatcher.exit()
    }
}