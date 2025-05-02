package br.com.victall.listenfree.player

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import java.io.IOException
import java.util.concurrent.Executors

class AudioBufferManager(
    private val onBufferingUpdate: (Int) -> Unit,
    private val onError: (String) -> Unit,
    private val onPrepared: () -> Unit
) {
    private var mediaPlayer: MediaPlayer? = null
    private val bufferSize = 2048 * 1024 // 2MB buffer
    private val minBufferSize = 512 * 1024 // 512KB minimum buffer
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var retryCount = 0
    private val maxRetries = 3
    private var currentUrl: String? = null
    private var isPreparing = false

    fun prepareAsync(url: String) {
        currentUrl = url
        isPreparing = true
        
        executor.execute {
            try {
                releasePlayer()
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    
                    setOnBufferingUpdateListener { _, percent ->
                        mainHandler.post {
                            onBufferingUpdate(percent)
                        }
                    }
                    
                    setOnPreparedListener {
                        isPreparing = false
                        retryCount = 0
                        mainHandler.post { onPrepared() }
                    }
                    
                    setOnErrorListener { _, what, extra ->
                        handleError(what, extra)
                        true
                    }

                    setOnInfoListener { _, what, _ ->
                        when (what) {
                            MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                                if (!isPreparing) {
                                    rebufferIfNeeded()
                                }
                            }
                        }
                        true
                    }

                    setDataSource(url)
                    setOnBufferingUpdateListener { _, percent ->
                        mainHandler.post {
                            onBufferingUpdate(percent)
                        }
                    }
                    prepareAsync()
                }
            } catch (e: IOException) {
                handleError(-1, -1)
            }
        }
    }

    private fun handleError(what: Int, extra: Int) {
        if (retryCount < maxRetries) {
            retryCount++
            mainHandler.post {
                onError("Tentando reconectar... Tentativa $retryCount de $maxRetries")
            }
            mainHandler.postDelayed({
                currentUrl?.let { prepareAsync(it) }
            }, 1000L * retryCount)
        } else {
            mainHandler.post {
                onError("Erro ao carregar áudio. Verifique sua conexão.")
            }
        }
    }

    private fun rebufferIfNeeded() {
        executor.execute {
            try {
                val currentPosition = mediaPlayer?.currentPosition ?: 0
                mediaPlayer?.seekTo(currentPosition)
            } catch (e: IllegalStateException) {
                // Ignora erro de estado inválido durante rebuffering
            }
        }
    }

    fun release() {
        executor.execute {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        mediaPlayer?.apply {
            try {
                if (isPlaying) {
                    stop()
                }
                reset()
                release()
            } catch (e: IllegalStateException) {
                // Ignora erro de estado inválido durante release
            }
        }
        mediaPlayer = null
    }

    fun getMediaPlayer(): MediaPlayer? = mediaPlayer
} 