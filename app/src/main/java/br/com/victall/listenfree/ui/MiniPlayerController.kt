package br.com.victall.listenfree.ui

import android.os.Handler
import android.os.Looper
import br.com.victall.listenfree.databinding.ViewMiniPlayerBinding
import br.com.victall.listenfree.models.Track
import br.com.victall.listenfree.player.PlayerManager
import com.bumptech.glide.Glide
import br.com.victall.listenfree.R

class MiniPlayerController(
    private val binding: ViewMiniPlayerBinding,
    private val onExpandRequested: () -> Unit
) {
    private val tvTitle = binding.tvMiniTitle
    private val tvArtist = binding.tvMiniArtist
    private val ivCover = binding.ivMiniCover
    private val btnPlayPause = binding.btnMiniPlayPause
    private val progressBar = binding.progressMiniPlayer

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateProgressBar()
            handler.postDelayed(this, 1000)
        }
    }

    init {
        binding.root.setOnClickListener { onExpandRequested() }
        btnPlayPause.setOnClickListener {
            if (PlayerManager.isPlaying()) PlayerManager.pause() else PlayerManager.resume()
            updatePlayPauseIcon()
        }
    }

    fun bind(track: Track) {
        tvTitle.text = track.name
        tvArtist.text = track.artistName
        Glide.with(binding.root.context).load(track.coverUrl).into(ivCover)
        progressBar.progress = 0
        progressBar.max = track.duration
        updatePlayPauseIcon()
        handler.post(updateRunnable)
    }

    private fun updatePlayPauseIcon() {
        val iconRes = if (PlayerManager.isPlaying())
            android.R.drawable.ic_media_pause
        else
            android.R.drawable.ic_media_play
        btnPlayPause.setImageResource(iconRes)
    }

    private fun updateProgressBar() {
        val mediaPlayer = PlayerManager.getMediaPlayer() ?: return
        if (PlayerManager.isPlaying()) {
            val currentPosition = mediaPlayer.currentPosition / 1000
            progressBar.progress = currentPosition
        }
    }

    fun stopProgressUpdates() {
        handler.removeCallbacks(updateRunnable)
    }
}
