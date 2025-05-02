package br.com.victall.listenfree.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.victall.listenfree.adapters.PodcastEpisodeAdapter
import br.com.victall.listenfree.databinding.ActivityPodcastDetailBinding
import br.com.victall.listenfree.models.Podcast
import br.com.victall.listenfree.models.PodcastEpisode
import com.bumptech.glide.Glide

class PodcastDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPodcastDetailBinding
    private lateinit var episodeAdapter: PodcastEpisodeAdapter
    private var podcast: Podcast? = null
    private val episodes = mutableListOf<PodcastEpisode>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPodcastDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recupera o podcast passado como extra
        podcast = intent.getSerializableExtra("podcast") as? Podcast
        
        if (podcast == null) {
            Toast.makeText(this, "Erro ao carregar podcast", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        setupRecyclerView()
        loadEpisodes()
    }

    private fun setupViews() {
        binding.apply {
            // Configura o botão de voltar
            btnBack.setOnClickListener {
                finish()
            }

            // Configura o botão de seguir
            btnFollow.setOnClickListener {
                // TODO: Implementar lógica para seguir o podcast
                Toast.makeText(this@PodcastDetailActivity, "Podcast seguido!", Toast.LENGTH_SHORT).show()
            }

            // Configura o botão de compartilhar
            btnShare.setOnClickListener {
                // TODO: Implementar lógica para compartilhar o podcast
                Toast.makeText(this@PodcastDetailActivity, "Compartilhando...", Toast.LENGTH_SHORT).show()
            }

            // Preenche os dados do podcast
            podcast?.let { podcast ->
                tvPodcastName.text = podcast.name
                tvPodcastAuthor.text = podcast.author
                tvPodcastDescription.text = podcast.description

                // Carrega a imagem do podcast
                Glide.with(this@PodcastDetailActivity)
                    .load(podcast.imageUrl)
                    .into(ivPodcastCover)
            }
        }
    }

    private fun setupRecyclerView() {
        episodeAdapter = PodcastEpisodeAdapter(
            onEpisodeClick = { episode ->
                navigateToEpisodePlayer(episode)
            },
            onDownloadClick = { episode ->
                handleEpisodeDownload(episode)
            }
        )

        binding.rvEpisodes.apply {
            layoutManager = LinearLayoutManager(this@PodcastDetailActivity)
            adapter = episodeAdapter
        }
    }

    private fun loadEpisodes() {
        // TODO: Carregar episódios da API
        // Por enquanto, vamos usar dados mockados
        val mockEpisodes = listOf(
            PodcastEpisode(
                id = "1",
                title = "Episódio 1: Introdução",
                description = "Neste episódio, apresentamos o podcast e discutimos os temas que serão abordados.",
                duration = 3600, // 1 hora
                releaseDate = "2023-01-01",
                audioUrl = "https://example.com/episode1.mp3",
                imageUrl = "https://example.com/episode1.jpg",
                podcastId = podcast?.id ?: "1"
            ),
            PodcastEpisode(
                id = "2",
                title = "Episódio 2: Tema Principal",
                description = "Discussão aprofundada sobre o tema principal do podcast.",
                duration = 5400, // 1 hora e 30 minutos
                releaseDate = "2023-01-08",
                audioUrl = "https://example.com/episode2.mp3",
                imageUrl = "https://example.com/episode2.jpg",
                podcastId = podcast?.id ?: "1"
            ),
            PodcastEpisode(
                id = "3",
                title = "Episódio 3: Entrevista Especial",
                description = "Entrevista com um convidado especial sobre o tema do podcast.",
                duration = 7200, // 2 horas
                releaseDate = "2023-01-15",
                audioUrl = "https://example.com/episode3.mp3",
                imageUrl = "https://example.com/episode3.jpg",
                podcastId = podcast?.id ?: "1"
            )
        )
        
        episodes.clear()
        episodes.addAll(mockEpisodes)
        episodeAdapter.submitList(episodes)
    }

    private fun navigateToEpisodePlayer(episode: PodcastEpisode) {
        // TODO: Implementar navegação para o player de episódio
        val intent = Intent(this, EpisodePlayerActivity::class.java).apply {
            putExtra("episode", episode)
            putExtra("podcast", podcast)
        }
        startActivity(intent)
    }

    private fun handleEpisodeDownload(episode: PodcastEpisode) {
        if (episode.isDownloaded) {
            // TODO: Implementar lógica para remover download
            Toast.makeText(this, "Download removido", Toast.LENGTH_SHORT).show()
        } else {
            // TODO: Implementar lógica para baixar episódio
            Toast.makeText(this, "Iniciando download...", Toast.LENGTH_SHORT).show()
        }
    }
} 