import com.github.theholywaffle.teamspeak3.TS3Api
import com.github.theholywaffle.teamspeak3.TS3Config
import com.github.theholywaffle.teamspeak3.TS3Query
import com.github.theholywaffle.teamspeak3.api.wrapper.Channel
import com.github.theholywaffle.teamspeak3.api.wrapper.Client
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.lang.IllegalStateException
import kotlin.concurrent.thread
import kotlin.time.seconds

class TeamSpeakService(
    private val host: String,
    private val user: String,
    private val pass: String,
){

    private val logger = LoggerFactory.getLogger(javaClass)

    private var api: TS3Api? = null
    private var query: TS3Query? = null

    fun connect() {
        val config = TS3Config().apply {
            setHost(host)
            setQueryPort(10011)
        }
        val query = TS3Query(config)
        query.connect()
        api = query.api.apply {
            login(user, pass)
            selectVirtualServerById(1)
        }
    }

    fun listClients(): Map<Channel, List<Client>> {
        val api = api ?: throw IllegalStateException("TS3 Api not initialized")
        val channels = api.channels
        val clients = api.clients
            .filter { it.isRegularClient }

        return clients
            .groupBy { it.channelId }
            .mapKeys { entry ->
                channels.first { it.id == entry.key }
            }
            .filter { it.value.isNotEmpty() }

    }

    fun exit() {
        api?.logout()
        query?.exit()
    }

}