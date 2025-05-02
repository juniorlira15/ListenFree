package br.com.victall.listenfree.player

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.util.Log
import br.com.victall.listenfree.models.Track
import br.com.victall.listenfree.services.MediaPlaybackService

object PlayerManager {
    private const val TAG = "PlayerManager"
    private var mediaPlayer: MediaPlayer? = null
    private var currentTrack: Track? = null
    private var queue: List<Track> = emptyList()
    private var currentIndex: Int = 0
    private var isPlaying = false

    private val trackChangeListeners = mutableListOf<(Track) -> Unit>()
    private val playbackStateListeners = mutableListOf<(Boolean) -> Unit>()
    private val bufferingListeners = mutableListOf<(Int) -> Unit>()

    var onTrackChanged: ((Track) -> Unit)? = null
    var onPlaybackStateChanged: ((Boolean) -> Unit)? = null
    var onBufferingUpdate: ((Int) -> Unit)? = null

    fun addTrackChangeListener(listener: (Track) -> Unit) {
        trackChangeListeners.add(listener)
    }

    fun addPlaybackStateListener(listener: (Boolean) -> Unit) {
        playbackStateListeners.add(listener)
    }

    fun addBufferingListener(listener: (Int) -> Unit) {
        bufferingListeners.add(listener)
    }

    fun play(context: Context, track: Track, trackList: List<Track>, onPrepared: (Track) -> Unit = {}) {
        Log.d(TAG, "Iniciando reprodução: ${track.name}")

        queue = trackList
        currentIndex = queue.indexOf(track)

        try {
            val intent = Intent(context, MediaPlaybackService::class.java)
            Log.d(TAG, "Iniciando serviço em foreground")
            context.startForegroundService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao iniciar serviço: ${e.message}")
        }

        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setOnPreparedListener {
                    start()
                    isPlaying
                    onPrepared(track)
                    playbackStateListeners.forEach { it(true) }
                    onPlaybackStateChanged?.invoke(true)
                }

                setOnCompletionListener {
                    playNext(context)
                }

                setOnBufferingUpdateListener { _, percent ->
                    bufferingListeners.forEach { it(percent) }
                    onBufferingUpdate?.invoke(percent)
                }

                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "Erro no MediaPlayer: $what, $extra")
                    false
                }

                setDataSource(track.audioUrl)
                prepareAsync()
            }
            currentTrack = track
            trackChangeListeners.forEach { it(track) }
            onTrackChanged?.invoke(track)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao preparar mídia: ${e.message}")
        }
    }

    fun pause() {
        Log.d(TAG, "Pausando reprodução")
        mediaPlayer?.pause()
        isPlaying = false
        playbackStateListeners.forEach { it(false) }
        onPlaybackStateChanged?.invoke(false)
    }

    fun resume() {
        Log.d(TAG, "Retomando reprodução")
        mediaPlayer?.start()
        isPlaying = true
        playbackStateListeners.forEach { it(true) }
        onPlaybackStateChanged?.invoke(true)
    }

    fun playNext(context: Context) {
        if (queue.isEmpty()) return
        currentIndex = (currentIndex + 1) % queue.size
        play(context, queue[currentIndex], queue)
    }

    fun playPrevious(context: Context) {
        if (queue.isEmpty()) return
        currentIndex = if (currentIndex > 0) currentIndex - 1 else queue.size - 1
        play(context, queue[currentIndex], queue)
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0
    fun getDuration(): Int = mediaPlayer?.duration ?: 0
    fun isPlaying(): Boolean = isPlaying
    fun getMediaPlayer(): MediaPlayer? = mediaPlayer
    fun getCurrentTrack(): Track? = currentTrack
    fun getCurrentIndexMusica(): Int = currentIndex
    fun getQueue(): List<Track> = queue

    fun setQueue(tracks: List<Track>) {
        queue = tracks
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        currentTrack = null
        queue = emptyList()
        currentIndex = 0
        isPlaying = false
        trackChangeListeners.clear()
        playbackStateListeners.clear()
        bufferingListeners.clear()
        onTrackChanged = null
        onPlaybackStateChanged = null
        onBufferingUpdate = null
    }
}
