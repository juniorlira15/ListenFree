package br.com.victall.listenfree.core.services

import br.com.victall.listenfree.models.Album
import br.com.victall.listenfree.models.Track
import com.google.firebase.database.FirebaseDatabase

class FirebaseService {

    private val db = FirebaseDatabase.getInstance().reference

    fun salvarAlbumGlobal(album: Album, onComplete: (Boolean, String?) -> Unit) {
        db.child("albuns").child(album.id)
            .setValue(album)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful, task.exception?.message)
            }
    }

    fun salvarTrackGlobal(track: Track, onComplete: (Boolean, String?) -> Unit) {
        db.child("musicas").child(track.id)
            .setValue(track)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful, task.exception?.message)
            }
    }
}
