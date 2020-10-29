import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.Exception
import java.util.*


object ChatManager {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val chats = hashMapOf<ChatId, ChatConfiguration>()
    private val watchers = hashMapOf<ChatId, TS3Watcher>()

    fun isChatConfigured(chatId: ChatId): Boolean {
        val chatData = chats[chatId]
        return chatData?.ts3Password != null && chatData.ts3Username != null
    }

    fun setCredentialsRequested(chatId: ChatId, messageId: Int) {
        chats[chatId] = ChatConfiguration(ts3CredentialsRequestMessageId = messageId)
    }

    fun isMessageReplyToConfigRequest(chatId: ChatId, configRequestMessageId: Int): Boolean {
        return chats.entries
            .firstOrNull { it.value.ts3CredentialsRequestMessageId == configRequestMessageId }
            ?.key == chatId
    }

    fun getChat(chatId: ChatId) = chats[chatId]

    fun updateChat(chatId: ChatId, chatConfiguration: ChatConfiguration) {
        chats[chatId] = chatConfiguration
        save()
    }

    fun setCredentialsAndStart(
        chatId: ChatId,
        ts3Host: String,
        ts3Username: String,
        ts3Password: String,
    ) {
        chats[chatId] = ChatConfiguration(
            ts3Host = ts3Host,
            ts3Username = ts3Username,
            ts3Password = ts3Password
        )
        startWatcher(chatId)
        save()
    }

    fun startWatcher(chatId: ChatId) {
        watchers[chatId] = TS3Watcher(chatId).also {
            it.start()
            logger.info("Started watcher for host ${getChat(chatId)?.ts3Host}")
        }
    }

    fun stop(chatId: ChatId) {
        getChat(chatId)?.ts3Host?.let {
            logger.info("Stopped watcher for host $")
        }
        watchers[chatId]?.interrupt()
        watchers.remove(chatId)
        chats.remove(chatId)
    }

    private fun configFile(): File {
        val dir = System.getenv("CONFIG_DIR") ?: "teamspeak-notifier"
        File(dir).mkdirs()
        return File(dir, "config.json")
    }

    fun loadSavedChats() {
        if(!configFile().exists()) {
            return
        }
        try {
            val typeRef = object : TypeReference<HashMap<ChatId, ChatConfiguration>>() {}
            val loaded = ObjectMapper().readValue(configFile(), typeRef)
            chats.clear()
            chats.putAll(loaded)
            chats.keys.forEach(::startWatcher)
        } catch (e: Exception) {
            logger.error("Error loading saved config!", e)
        }
    }

    private fun save() {
        try {
            ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValue(configFile(), chats)
        } catch (e: Exception) {
            logger.error("Error saving config!", e)
        }
    }

}

