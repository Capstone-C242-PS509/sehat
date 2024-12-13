data class dataChat(
    val text: String,
    val isSender: Boolean,
    val details: ChatDetails? = null
)

data class ChatDetails(
    val prediction: String,
    val description: String,
    val firstAid: List<String>,
    val improvementTips: List<String>
)