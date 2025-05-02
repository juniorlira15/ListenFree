package br.com.victall.listenfree.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.victall.listenfree.R
import br.com.victall.listenfree.adapters.TrackAdapter
import br.com.victall.listenfree.databinding.ActivityPlayerBinding
import br.com.victall.listenfree.models.Track
import br.com.victall.listenfree.player.PlayerManager
import com.bumptech.glide.Glide

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var trackAdapter: TrackAdapter
    private var currentTrack: Track? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isSeeking = false
    private val progressRunnable = object : Runnable {
        override fun run() {
            if (!isSeeking) {
                updateProgressBar()
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentTrack = PlayerManager.getCurrentTrack()
        val trackList = PlayerManager.getQueue()

        setupRecyclerView(trackList)
        currentTrack?.let { bindTrack(it) }

        PlayerManager.onBufferingUpdate = { percent ->
            binding.tvBuffering.text = if (percent < 100) "Buffering... $percent%" else ""
            binding.tvBuffering.visibility = if (percent < 100) View.VISIBLE else View.GONE

            val duration = PlayerManager.getMediaPlayer()?.duration ?: 0
            if (duration > 0) {
                val bufferPosition = (duration * percent) / 100
                binding.seekBar.secondaryProgress = bufferPosition / 1000
            }
        }

        binding.btnPlayPause.setOnClickListener {
            if (PlayerManager.isPlaying()) PlayerManager.pause() else PlayerManager.resume()
            updatePlayPauseIcon()
            trackAdapter.notifyItemChanged(PlayerManager.getCurrentIndexMusica())
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnPrevious.setOnClickListener {
            PlayerManager.playPrevious(this)
        }

        binding.btnNext.setOnClickListener {
            PlayerManager.playNext(this)
        }

        binding.seekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.tvCurrentTime.text = formatDuration(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {
                isSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                isSeeking = false
                seekBar?.let {
                    PlayerManager.seekTo(it.progress * 1000)
                }
            }
        })

        if (!PlayerManager.isPlaying()) {
            PlayerManager.resume()
            updatePlayPauseIcon()
        }

        PlayerManager.onTrackChanged = { track ->
            Log.d("PlayerActivity", "Track changed to: ${track.name}")
            runOnUiThread {
                currentTrack = track
                bindTrack(track)
                trackAdapter.setHighlightTrack(track.id)
                updatePlayPauseIcon()
                MainActivity.instance.updateMiniPlayer(track)
            }
        }
    }

    private fun setupRecyclerView(trackList: List<Track>) {
        Log.d("PlayerActivity", "Setting up RecyclerView with ${trackList.size} tracks")
        trackAdapter = TrackAdapter(trackList) { track ->
            Log.d("PlayerActivity", "Track clicked: ${track.name}")
            PlayerManager.play(this, track, trackList) {
                currentTrack = track
                bindTrack(track)
                trackAdapter.setHighlightTrack(track.id)
            }
        }
        binding.rvAlbumTracks.layoutManager = LinearLayoutManager(this)
        binding.rvAlbumTracks.adapter = trackAdapter

        currentTrack?.let { trackAdapter.setHighlightTrack(it.id) }
    }

    private fun bindTrack(track: Track) {
        Log.d("PlayerActivity", "Binding track: ${track.name}")
        binding.tvTitle.text = track.name
        binding.tvArtist.text = track.artistName
        binding.tvTotalTime.text = formatDuration(track.duration)
        binding.seekBar.max = track.duration
        binding.seekBar.progress = 0
        Glide.with(this).load(track.coverUrl.ifEmpty { R.drawable.ic_music_note }).into(binding.ivCover)
        updatePlayPauseIcon()
        handler.post(progressRunnable)

        if (!PlayerManager.isPlaying()) {
            PlayerManager.resume()
        }

        handler.postDelayed({ updatePlayPauseIcon() }, 500)
    }

    private fun updatePlayPauseIcon() {
        val icon = if (PlayerManager.isPlaying())
            android.R.drawable.ic_media_pause
        else
            android.R.drawable.ic_media_play
        binding.btnPlayPause.setImageResource(icon)
    }

    private fun updateProgressBar() {
        val mediaPlayer = PlayerManager.getMediaPlayer() ?: return
        if (PlayerManager.isPlaying()) {
            val currentSec = mediaPlayer.currentPosition / 1000
            binding.seekBar.progress = currentSec
            binding.tvCurrentTime.text = formatDuration(currentSec)
        }
    }

    private fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(progressRunnable)
        PlayerManager.onTrackChanged = null
    }
}
