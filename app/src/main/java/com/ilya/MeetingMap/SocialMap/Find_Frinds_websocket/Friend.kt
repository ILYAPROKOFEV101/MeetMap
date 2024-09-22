import kotlinx.serialization.Serializable

@Serializable
data class Friend(
    val key: String,
    val name: String,
    val img: String,
    val friend: Boolean
)