import com.github.theholywaffle.teamspeak3.api.exception.TS3QueryShutDownException
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
                    logger.info("connecting to ${config.ts3Host}")
                    tsWatcher.connect()
                    loop(tsWatcher)
                } catch (e: InterruptedException) {
                    tsWatcher.exit()
                    throw e
                } catch (e: Exception) {
                    logger.error("TS3 Connection error.. reconnecting after 30 seconds")
                    sleep(30_000)
                }
            }
        } catch (e: InterruptedException) {
            logger.info("Stopping this watcher")
        }

    }

    private fun loop(tsWatcher: TeamSpeakService) {

        while (true) {
            try {
                val clients = tsWatcher.listClients()
                TeleBot.sendClientInfo(chatId, clients)
            } catch (e: TS3QueryShutDownException) {
                throw e
            } catch (e: Exception) {
                logger.error("error listing clients", e)
            }
            sleep(5000L)
        }

    }
}