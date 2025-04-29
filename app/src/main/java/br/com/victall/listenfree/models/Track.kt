package br.com.victall.listenfree.models

data class Track(
    val id: String,
    val name: String,
    val albumId: String,
    val artistId: String,
    val duration: Int, // em segundos
    val audioUrl: String,
    val isDownloaded: Boolean = false
) 