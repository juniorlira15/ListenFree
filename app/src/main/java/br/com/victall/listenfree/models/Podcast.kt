data class Podcast(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val author: String,
    val episodes: List<PodcastEpisode> = emptyList()
) 