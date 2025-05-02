package br.com.victall.listenfree.activities

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.victall.listenfree.databinding.ActivityEpisodePlayerBinding
import br.com.victall.listenfree.models.Podcast
import br.com.victall.listenfree.models.PodcastEpisode
import com.bumptech.glide.Glide
import java.util.concurrent.TimeUnit

class EpisodePlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEpisodePlayerBinding
    private var episode: PodcastEpisode? = null
    private var podcast: Podcast? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())
    private val updateSeekBar = object : Runnable {
        override fun run() {
            updateSeekBarProgress()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEpisodePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recupera o episódio e o podcast passados como extras
        episode = intent.getSerializableExtra("episode") as? PodcastEpisode
        podcast = intent.getSerializableExtra("podcast") as? Podcast
        
        if (episode == null || podcast == null) {
            Toast.makeText(this, "Erro ao carregar episódio", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        setupPlayer()
    }

    private fun setupViews() {
        binding.apply {
            // Configura o botão de voltar
            btnBack.setOnClickListener {
                finish()
            }

            // Configura o botão de mais opções
            btnMore.setOnClickListener {
                // TODO: Implementar menu de opções
                Toast.makeText(this@EpisodePlayerActivity, "Opções", Toast.LENGTH_SHORT).show()
            }

            // Preenche os dados do episódio e podcast
            episode?.let { episode ->
                tvEpisodeTitle.text = episode.title
                tvEpisodeDescription.text = episode.description
                tvTotalTime.text = episode.getFormattedDuration()
                
                // Carrega a imagem do episódio
                Glide.with(this@EpisodePlayerActivity)
                    .load(episode.imageUrl)
                    .into(ivEpisodeCover)
            }
            
            podcast?.let { podcast ->
                tvPodcastName.text = podcast.name
            }

            // Configura os controles de reprodução
            btnPlayPause.setOnClickListener {
                togglePlayPause()
            }
            
            btnRewind.setOnClickListener {
                rewindEpisode()
            }
            
            btnForward.setOnClickListener {
                forwardEpisode()
            }
            
            seekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mediaPlayer?.seekTo(progress)
                        updateCurrentTimeText()
                    }
                }
                
                override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
                
                override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
            })
        }
    }

    private fun setupPlayer() {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(episode?.audioUrl)
                prepareAsync()
                setOnPreparedListener {
                    binding.seekBar.max = duration
                    updateCurrentTimeText()
                }
                setOnCompletionListener {
                    this@EpisodePlayerActivity.isPlaying = false
                    updatePlayPauseButton()
                    handler.removeCallbacks(updateSeekBar)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao preparar o player: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun togglePlayPause() {
        if (mediaPlayer == null) return
        
        if (isPlaying) {
            mediaPlayer?.pause()
            handler.removeCallbacks(updateSeekBar)
        } else {
            mediaPlayer?.start()
            handler.post(updateSeekBar)
        }
        
        isPlaying = !isPlaying
        updatePlayPauseButton()
    }

    private fun updatePlayPauseButton() {
        binding.btnPlayPause.setImageResource(
            if (isPlaying) android.R.drawable.ic_media_pause
            else android.R.drawable.ic_media_play
        )
    }

    private fun updateSeekBarProgress() {
        mediaPlayer?.let { player ->
            binding.seekBar.progress = player.currentPosition
            updateCurrentTimeText()
        }
    }

    private fun updateCurrentTimeText() {
        mediaPlayer?.let { player ->
            val currentTime = player.currentPosition
            val totalTime = player.duration
            binding.tvCurrentTime.text = formatTime(currentTime)
            binding.tvTotalTime.text = formatTime(totalTime)
        }
    }

    private fun formatTime(milliseconds: Int): String {
        return String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong()))
        )
    }

    private fun rewindEpisode() {
        mediaPlayer?.let { player ->
            val newPosition = player.currentPosition - 10000 // 10 segundos
            if (newPosition >= 0) {
                player.seekTo(newPosition)
                updateCurrentTimeText()
            }
        }
    }

    private fun forwardEpisode() {
        mediaPlayer?.let { player ->
            val newPosition = player.currentPosition + 10000 // 10 segundos
            if (newPosition <= player.duration) {
                player.seekTo(newPosition)
                updateCurrentTimeText()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacks(updateSeekBar)
    }
} 