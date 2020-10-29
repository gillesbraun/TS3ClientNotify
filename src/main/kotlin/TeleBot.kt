import com.github.theholywaffle.teamspeak3.api.wrapper.Channel
import com.github.theholywaffle.teamspeak3.api.wrapper.Client
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinChatMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Update
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit

object TeleBot : TelegramLongPollingBot() {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun getBotUsername() = System.getenv("TELEGRAM_BOT_NAME")
        ?: throw RuntimeException("Need to specify following env variables: TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN")

    override fun getBotToken() = System.getenv("TELEGRAM_BOT_TOKEN")
        ?: throw RuntimeException("Need to specify following env variables: TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN")

    override fun onUpdateReceived(update: Update) {
        checkStartReceived(update)
        checkStopReceived(update)
        checkConfigurationReceived(update)
    }

    private fun checkStartReceived(update: Update) {
        val chatId = update.message?.chatId ?: return
        if(update.message?.text?.startsWith("/start") != true) {
            return
        }
        if(ChatManager.isChatConfigured(chatId)) {
            val lastMsg = ChatManager.getChat(chatId)?.pinnedMessageId
            execute(
                SendMessage()
                    .setChatId(chatId)
                    .setText("Already running")
                    .setReplyToMessageId(lastMsg)
            )
        } else {
            askCredentials(chatId)
        }
    }

    private fun askCredentials(chatId: ChatId) {
        val message = execute(
            SendMessage()
                .setChatId(chatId)
                .setText("Reply me hostname, username and password each on separate lines")
        )
        ChatManager.setCredentialsRequested(chatId, message.messageId)
    }

    private fun checkStopReceived(update: Update) {
        val chatId = update.message?.chatId ?: return
        if(update.message?.text?.startsWith("/stop") != true) {
            return
        }
        ChatManager.getChat(chatId) ?: return
        ChatManager.stop(chatId)
        execute(
            SendMessage()
                .setChatId(chatId)
                .setText("Stopping")
        )
    }

    private fun checkConfigurationReceived(update: Update) {
        val chatId = update.message?.chatId ?: return
        val replyTo = update.message?.replyToMessage?.messageId ?: return
        if(!ChatManager.isMessageReplyToConfigRequest(chatId, replyTo)) {
            return
        }
        try {
            execute(DeleteMessage(chatId, update.message.messageId))
            execute(DeleteMessage(chatId, replyTo))
        } catch (e: Exception) {
            logger.warn("Cannot delete user's config message", e)
        }
        val split = update.message.text.split("\n")
        if(split.size != 3) {
            execute(
                SendMessage()
                    .setChatId(chatId)
                    .setText("Format was incorrect, put hostname, username and password each on separate lines.")
            )
            askCredentials(chatId)
            return
        }
        val (host, user, pass) = split
        ChatManager.setCredentialsAndStart(chatId, host, user, pass)
    }

    fun sendClientInfo(chatId: ChatId, clients: Map<Channel, List<Client>>) {
        var config = ChatManager.getChat(chatId) ?: return
        val msg = toTelegramString(clients)

        if (config.lastMessage == msg) {
            return
        }

        if (config.pinnedMessageId == null) {
            val result = execute(
                SendMessage()
                    .setText(msg)
                    .setChatId(chatId)
            )
            config = config.copy(pinnedMessageId = result.messageId)

//            if (result.chat.pinnedMessage?.from?.bot == false) {
//                return
//            }

            if (result.chat.pinnedMessage?.messageId != config.pinnedMessageId) {
                execute(UnpinChatMessage(result.chatId))
                execute(
                    PinChatMessage(result.chatId, result.messageId)
                        .setDisableNotification(true)
                )
            }
        } else {
            val result = execute(
                EditMessageText()
                    .setChatId(chatId)
                    .setMessageId(config.pinnedMessageId)
                    .setText(msg)
            )
        }
        config = config.copy(lastMessage = msg)
        ChatManager.updateChat(chatId, config)
    }

    private fun toTelegramString(clients: Map<Channel, List<Client>>): String {
        if(clients.isEmpty()) {
            return "The server is empty"
        }
        return clients.map { entry ->
            entry.key.name + "\n" + entry.value.joinToString("\n") {
                "    " + it.nickname + " " + it.lastConnectedDate.dateDiff()
            }
        }.joinToString("\n")
    }

    private fun Date.dateDiff(): String {
        val diff = Date().time - time
        val m = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
        var since = " (since"
        val d = TimeUnit.MILLISECONDS.toDays(diff)
        if (d > 0) {
            since += " ${d}d"
        }
        val h = TimeUnit.MILLISECONDS.toHours(diff) % 60
        if (h > 0) {
            since += " ${h}h"
        }
        if (m > 0) {
            since += " ${m}m"
        } else {
            return "just now"
        }
        since += ")"
        return since
    }

}
