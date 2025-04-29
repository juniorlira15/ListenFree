data class PodcastEpisode(
    val id: String,
    val podcastId: String,
    val title: String,
    val description: String,
    val duration: Int, // em segundos
    val audioUrl: String,
    val releaseDate: Long,
    val isDownloaded: Boolean = false
) 