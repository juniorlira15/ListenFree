package br.com.victall.listenfree.models

data class Track(
    val id: String = "",
    val name: String = "",
    val albumId: String = "",
    val artistName: String = "",
    val duration: Int = 0,
    val audioUrl: String = "",
    val isDownloaded: Boolean = false,
    val coverUrl: String = "",
    val userId: String = "",
    val genre: String = "",
    val playCount: Int = 0,
    val releaseDate: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()

)
