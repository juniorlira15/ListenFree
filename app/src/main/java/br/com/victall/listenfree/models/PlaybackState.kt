data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentTrack: Track? = null,
    val currentPosition: Int = 0, // em segundos
    val queue: List<Track> = emptyList(),
    val isShuffled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.NONE
)

enum class RepeatMode {
    NONE,
    ALL,
    ONE
} 