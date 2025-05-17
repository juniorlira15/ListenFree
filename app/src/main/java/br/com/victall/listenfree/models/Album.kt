package br.com.victall.listenfree.models

data class Album(
    val id: String,
    val titulo: String,
    val artist: String,
    val imageUrl: String,
    val releaseYear: Int,
    val userId: String = "",
    val genre: String = "",
    val isPublished: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val tracks: List<Track> = emptyList()
)
