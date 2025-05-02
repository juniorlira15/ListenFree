package br.com.victall.listenfree.player

import android.content.Context
import android.media.MediaPlayer
import android.widget.Toast
import br.com.victall.listenfree.models.Track
import java.lang.ref.WeakReference

object PlayerManager {
    private var audioBufferManager: AudioBufferManager? = null
    private var currentTrack: Track? = null
    private var isPrepared = false
    private var queue: List<Track> = emptyList()
    var onTrackChanged: ((Track) -> Unit)? = null
    var currentIdexMusca: Int = 0
    var onBufferingUpdate: ((Int) -> Unit)? = null

    fun play(context: Context, track: Track, albumTracks: List<Track> = listOf(), onPrepared: (() -> Unit)? = null) {
        if (track == currentTrack && audioBufferManager?.getMediaPlayer()?.isPlaying == true) return

        stop()
        currentTrack = track
        queue = albumTracks
        currentIdexMusca = queue.indexOfFirst { it.id == track.id }

        val contextRef = WeakReference(context)

        audioBufferManager = AudioBufferManager(
            onBufferingUpdate = { percent ->
                onBufferingUpdate?.invoke(percent)
            },
            onError = { message ->
                contextRef.get()?.let { ctx ->
                    Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()
                }
            },
            onPrepared = {
                isPrepared = true
                audioBufferManager?.getMediaPlayer()?.start()
                onPrepared?.invoke()

                // Configurar completion listener
                audioBufferManager?.getMediaPlayer()?.setOnCompletionListener {
                    contextRef.get()?.let { ctx ->
                        playNext(ctx)
                    }
                }
            }
        ).also { 
            it.prepareAsync(track.audioUrl)
        }
    }

    fun playNext(context: Context) {
        if (queue.isEmpty()) return

        val nextIndex = currentIdexMusca + 1
        if (nextIndex < queue.size) {
            val nextTrack = queue[nextIndex]
            play(context, nextTrack, queue)
            onTrackChanged?.invoke(nextTrack)
        }
    }

    fun playPrevious(context: Context) {
        if (queue.isEmpty()) return

        val previousIndex = currentIdexMusca - 1
        if (previousIndex >= 0) {
            val previousTrack = queue[previousIndex]
            play(context, previousTrack, queue)
            onTrackChanged?.invoke(previousTrack)
        }
    }

    fun seekTo(position: Int) {
        audioBufferManager?.getMediaPlayer()?.seekTo(position)
    }

    fun pause() {
        audioBufferManager?.getMediaPlayer()?.takeIf { it.isPlaying }?.pause()
    }

    fun resume() {
        audioBufferManager?.getMediaPlayer()?.takeIf { isPrepared && !it.isPlaying }?.start()
    }

    fun stop() {
        audioBufferManager?.release()
        audioBufferManager = null
        currentTrack = null
        isPrepared = false
        queue = emptyList()
    }

    fun isPlaying(): Boolean = audioBufferManager?.getMediaPlayer()?.isPlaying == true
    fun getCurrentTrack(): Track? = currentTrack
    fun getQueue(): List<Track> = queue
    fun getMediaPlayer(): MediaPlayer? = audioBufferManager?.getMediaPlayer()

    fun getCurrentPosition(): Int {
        return audioBufferManager?.getMediaPlayer()?.currentPosition ?: 0
    }

    fun getCurrentIndexMusica(): Int {
        return currentIdexMusca
    }
}
