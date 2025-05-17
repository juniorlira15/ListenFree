package br.com.victall.listenfree.fragments

import android.content.Intent
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import br.com.victall.listenfree.activities.PlayerActivity
import br.com.victall.listenfree.adapters.AlbumAdapter
import br.com.victall.listenfree.cache.AlbumCache
import br.com.victall.listenfree.databinding.FragmentHomeBinding
import br.com.victall.listenfree.models.Album
import br.com.victall.listenfree.models.Track
import br.com.victall.listenfree.player.PlayerManager
import com.google.firebase.database.*

class HomeFragment : Fragment() {
    private val TAG = "HomeFragment"
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var albumAdapter: AlbumAdapter
    private var dbRef: DatabaseReference? = null
    private var valueEventListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView iniciado")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAlbumSection()
    }

    private fun setupAlbumSection() {
        albumAdapter = AlbumAdapter(emptyList()) { album ->
            val musicaAtual = PlayerManager.getCurrentTrack()
            if (musicaAtual != null && musicaAtual.albumId == album.id && PlayerManager.isPlaying()) {
                startActivity(Intent(requireContext(), PlayerActivity::class.java))
                return@AlbumAdapter
            }

            PlayerManager.play(requireContext(), album.tracks.first(), album.tracks) {
                Log.d(TAG, "Player preparado para: ${album.tracks.first().name}")
            }

            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtra("albumId", album.id)
            startActivity(intent)
        }

        binding.rvAlbuns.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = albumAdapter
        }

        val database = FirebaseDatabase.getInstance()
        dbRef = database.getReference("albuns")
        val musicasRef = database.getReference("musicas")

        dbRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val albuns = mutableListOf<Album>()
                val musicasPorAlbum = mutableMapOf<String, MutableList<Track>>()

                // Primeiro: agrupar músicas do nó separado por albumId
                musicasRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(musicasSnapshot: DataSnapshot) {
                        for (musicaSnapshot in musicasSnapshot.children) {
                            val track = musicaSnapshot.getValue(Track::class.java)
                            if (track != null && track.albumId.isNotBlank()) {
                                musicasPorAlbum.getOrPut(track.albumId) { mutableListOf() }.add(track)
                            }
                        }

                        // Agora processar os álbuns
                        for (albumSnapshot in snapshot.children) {
                            val id = albumSnapshot.key ?: continue
                            val titulo = albumSnapshot.child("titulo").getValue(String::class.java) ?: ""
                            val artista = albumSnapshot.child("artista").getValue(String::class.java)
                                ?: albumSnapshot.child("artist").getValue(String::class.java) ?: ""
                            val capaUrl = albumSnapshot.child("capaUrl").getValue(String::class.java)
                                ?: albumSnapshot.child("imageUrl").getValue(String::class.java) ?: ""
                            val ano = albumSnapshot.child("ano").getValue(Int::class.java)
                                ?: albumSnapshot.child("releaseYear").getValue(Int::class.java) ?: 0

                            val tracks = mutableListOf<Track>()

                            // Caso 1: músicas embutidas
                            val musicasSnapshot = albumSnapshot.child("musicas")
                            if (musicasSnapshot.exists()) {
                                for (trackSnapshot in musicasSnapshot.children) {
                                    val trackId = trackSnapshot.key ?: continue
                                    val nome = trackSnapshot.child("titulo").getValue(String::class.java) ?: ""
                                    val url = trackSnapshot.child("arquivoUrl").getValue(String::class.java) ?: ""
                                    val duracao = trackSnapshot.child("duracao").getValue(Int::class.java) ?: 0

                                    val track = Track(
                                        id = trackId,
                                        name = nome,
                                        audioUrl = url,
                                        duration = duracao,
                                        coverUrl = capaUrl,
                                        albumId = id,
                                        artistName = artista,
                                        isDownloaded = false
                                    )
                                    tracks.add(track)
                                }
                            }

                            // Caso 2: músicas externas (estrutura nova)
                            if (tracks.isEmpty()) {
                                val musicasExternas = musicasPorAlbum[id]
                                if (musicasExternas != null) {
                                    tracks.addAll(musicasExternas.map {
                                        it.copy(coverUrl = capaUrl)
                                    })
                                }
                            }

                            if (tracks.isNotEmpty()) {
                                albuns.add(
                                    Album(
                                        id = id,
                                        titulo = titulo,
                                        artist = artista,
                                        imageUrl = capaUrl,
                                        releaseYear = ano,
                                        tracks = tracks
                                    )
                                )
                            }
                        }

                        AlbumCache.albumList = albuns
                        albumAdapter.updateList(albuns)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "Erro ao carregar músicas externas: ${error.message}")
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Erro ao carregar álbuns: ${error.message}")
            }
        })
    }

    fun recuperarDuracaoSeZero(track: Track) {
        if (track.duration > 0) return

        //rode em segundo plano

        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(track.audioUrl, HashMap())

            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
            retriever.release()

            if (durationMs != null) {
                val durationSec = (durationMs / 1000).toInt()
                Log.d("Duracao", "Track ${track.name} tem duração: $durationSec s")

                // Salva no Firebase
                FirebaseDatabase.getInstance()
                    .getReference("albuns/${track.albumId}/musicas/${track.id}/duracao")
                    .setValue(durationSec)
            }
        } catch (e: Exception) {
            Log.e("Duracao", "Erro ao recuperar duração da música ${track.name}: ${e.message}")
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        valueEventListener?.let { dbRef?.removeEventListener(it) }
        _binding = null
    }
}
