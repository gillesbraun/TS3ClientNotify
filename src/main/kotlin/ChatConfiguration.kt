import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChatConfiguration(
    val ts3CredentialsRequestMessageId: Int? = null,
    val ts3Host: String? = null,
    val ts3Username: String? = null,
    val ts3Password: String? = null,
    val lastMessage: String? = null,
    val pinnedMessageId: Int? = null,
    val messageId: Int? = null,
)