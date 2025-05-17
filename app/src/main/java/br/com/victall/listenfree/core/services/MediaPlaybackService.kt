package br.com.victall.listenfree.core.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import br.com.victall.listenfree.R
import br.com.victall.listenfree.activities.PlayerActivity
import br.com.victall.listenfree.models.Track
import br.com.victall.listenfree.player.PlayerManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

class MediaPlaybackService : MediaBrowserServiceCompat() {
    private val TAG = "MediaPlaybackService"
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private var currentTrack: Track? = null
    private var currentArtwork: Bitmap? = null
    private lateinit var notificationManager: NotificationManager

    companion object {
        private const val CHANNEL_ID = "media_playback_channel"
        private const val NOTIFICATION_ID = 1
        private const val REQUEST_CODE = 100
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        setupMediaSession()
    }

    private fun setupMediaSession() {
        mediaSession = MediaSessionCompat(this, TAG).apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
            stateBuilder = PlaybackStateCompat.Builder().setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE
            )
            setPlaybackState(stateBuilder.build())
            setCallback(mediaSessionCallback)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken

        PlayerManager.addTrackChangeListener { track ->
            currentTrack = track
            loadArtwork(track.coverUrl)
            updateMetadataAndState(track)
        }

        PlayerManager.addPlaybackStateListener { isPlaying ->
            updatePlaybackState(isPlaying)
            showNotification()
        }

        showNotification()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Player de Música",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controles do player de música"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateMetadataAndState(track: Track) {
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.name)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artistName)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, track.coverUrl)
            .build()

        mediaSession.setMetadata(metadata)
        showNotification()
    }

    private fun updatePlaybackState(isPlaying: Boolean) {
        val state = if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        stateBuilder.setState(state, PlayerManager.getCurrentPosition().toLong(), 1f)
        mediaSession.setPlaybackState(stateBuilder.build())
    }

    private fun loadArtwork(url: String) {
        if (url.isEmpty()) {
            showNotification()
            return
        }

        Glide.with(applicationContext)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    currentArtwork = resource
                    showNotification()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    currentArtwork = null
                    showNotification()
                }
            })
    }

    private fun showNotification() {
        val track = currentTrack ?: PlayerManager.getCurrentTrack()
        val isPlaying = PlayerManager.isPlaying()

        val intent = Intent(this, PlayerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, REQUEST_CODE, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(track?.name ?: "ListenFree")
            .setContentText(track?.artistName ?: "Reproduzindo música...")
            .setSmallIcon(R.drawable.ic_music_note)
            .setLargeIcon(currentArtwork)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(isPlaying)
            .setAutoCancel(false)
            .setSilent(true)
            .addAction(R.drawable.ic_skip_previous, "Anterior", createActionIntent(PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS))
            .addAction(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                if (isPlaying) "Pausar" else "Reproduzir",
                createActionIntent(if (isPlaying) PlaybackStateCompat.ACTION_PAUSE else PlaybackStateCompat.ACTION_PLAY)
            )
            .addAction(R.drawable.ic_skip_next, "Próxima", createActionIntent(PlaybackStateCompat.ACTION_SKIP_TO_NEXT))
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0, 1, 2))
            .build()

        try {
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao iniciar serviço em foreground: ${e.message}")
        }
    }

    private fun createActionIntent(action: Long): PendingIntent? {
        return MediaButtonReceiver.buildMediaButtonPendingIntent(this, action)
    }


    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            PlayerManager.resume()
        }

        override fun onPause() {
            PlayerManager.pause()
        }

        override fun onSkipToNext() {
            PlayerManager.playNext(applicationContext)
        }

        override fun onSkipToPrevious() {
            PlayerManager.playPrevious(applicationContext)
        }
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot("root", null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(mutableListOf())
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
    }
}
