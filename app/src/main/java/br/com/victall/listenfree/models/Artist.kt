data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String,
    val albums: List<Album> = emptyList()
) 