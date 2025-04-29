package br.com.victall.listenfree.models

data class User(
    val id: String,
    val email: String,
    val name: String,
    val gender: String?,
    val profilePicUrl: String?,
    val favoriteArtists: List<Artist> = emptyList(),
    val favoritePodcasts: List<Podcast> = emptyList()
) 