package br.com.victall.listenfree.models

data class Album(
    val id: String,
    val titulo: String,
    val artist: String,
    val imageUrl: String,
    val releaseYear: Int,
    val tracks: List<Track> = emptyList()
) 