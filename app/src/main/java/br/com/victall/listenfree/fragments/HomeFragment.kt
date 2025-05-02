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

        if (AlbumCache.albumList != null) {
            Log.d(TAG, "Carregando álbuns do cache")
            albumAdapter.updateList(AlbumCache.albumList!!)
            return
        }

        try {
            val database = FirebaseDatabase.getInstance()
            database.setPersistenceEnabled(true)
            database.goOnline()
            dbRef = database.getReference("albuns")

            database.reference.child(".info/connected").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val connected = snapshot.getValue(Boolean::class.java) ?: false
                    Log.d(TAG, "Firebase conectado: $connected")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Erro ao verificar conexão: ${error.message}")
                }
            })

            valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "onDataChange chamado. Existe snapshot: ${snapshot.exists()}")
                    Log.d(TAG, "Número de álbuns: ${snapshot.childrenCount}")

                    val albuns = mutableListOf<Album>()
                    for (albumSnapshot in snapshot.children) {
                        val id = albumSnapshot.key ?: continue
                        val name = albumSnapshot.child("titulo").getValue(String::class.java) ?: ""
                        val artist = albumSnapshot.child("artista").getValue(String::class.java) ?: ""
                        val imageUrl = albumSnapshot.child("capaUrl").getValue(String::class.java) ?: ""
                        val releaseYear = albumSnapshot.child("ano").getValue(Int::class.java) ?: 0

                        val tracks = mutableListOf<Track>()
                        val tracksSnapshot = albumSnapshot.child("musicas")
                        for (trackSnapshot in tracksSnapshot.children) {
                            val trackId = trackSnapshot.key ?: continue
                            val titulo = trackSnapshot.child("titulo").getValue(String::class.java) ?: ""
                            val url = trackSnapshot.child("arquivoUrl").getValue(String::class.java) ?: ""
                            val duration = trackSnapshot.child("duracao").getValue(Int::class.java) ?: 0

                            val track = Track(
                                id = trackId,
                                name = titulo,
                                audioUrl = url,
                                duration = duration,
                                coverUrl = imageUrl,
                                albumId = id,
                                artistName = artist,
                                isDownloaded = false,
                            )

//                            if(duration == 0){
//                                recuperarDuracaoSeZero(track)
//                            }

                            tracks.add(track)
                            Log.d(TAG, "Música carregada - ID: $trackId, Título: $titulo")
                        }

                        val album = Album(
                            id = id,
                            titulo = name,
                            artist = artist,
                            imageUrl = imageUrl,
                            releaseYear = releaseYear,
                            tracks = tracks
                        )
                        albuns.add(album)
                    }

                    Log.d(TAG, "Total de álbuns processados: ${albuns.size}")
                    AlbumCache.albumList = albuns
                    albumAdapter.updateList(albuns)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Erro ao carregar álbuns: ${error.message}")
                }
            }

            dbRef?.addValueEventListener(valueEventListener!!)

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao acessar Firebase Database: ${e.message}")
            e.printStackTrace()
        }
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
