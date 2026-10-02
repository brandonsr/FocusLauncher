package com.example.focuslauncher

import android.app.Notification
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

class MusicNotificationListener : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var currentSbnKey: String? = null
    private var activeCallback: MediaController.Callback? = null
    private var activeController: MediaController? = null

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        tryExtractMedia(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (sbn.key != currentSbnKey) return

        serviceScope.launch {
            delay(2500L) // Delayed clear to prevent flickering
            if (sbn.key == currentSbnKey) {
                MusicRepository.clear()
                detachCallback()
            }
        }
    }

    override fun onListenerDisconnected() {
        MusicRepository.clear()
        detachCallback()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun tryExtractMedia(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras

        val token: MediaSession.Token? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                extras.getParcelable(
                    Notification.EXTRA_MEDIA_SESSION,
                    MediaSession.Token::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                extras.getParcelable(Notification.EXTRA_MEDIA_SESSION)
            }

        token ?: return

        currentSbnKey = sbn.key
        detachCallback()

        val controller = MediaController(this, token)
        activeController = controller

        val metadata = controller.metadata ?: return
        pushState(controller, metadata)

        val callback = object : MediaController.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackState?) {
                val meta = controller.metadata ?: return
                pushState(controller, meta)
            }

            override fun onMetadataChanged(metadata: MediaMetadata?) {
                metadata ?: return
                pushState(controller, metadata)
            }

            override fun onSessionDestroyed() {
                MusicRepository.clear()
                detachCallback()
            }
        }
        activeCallback = callback
        controller.registerCallback(callback)
    }

    private fun pushState(controller: MediaController, metadata: MediaMetadata) {
        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: return
        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""
        val albumArt = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
        val isPlaying = controller.playbackState?.state == PlaybackState.STATE_PLAYING

        MusicRepository.update(
            MusicState(title = title, artist = artist, albumArt = albumArt, isPlaying = isPlaying),
            controller
        )
    }

    private fun detachCallback() {
        activeCallback?.let { activeController?.unregisterCallback(it) }
        activeCallback = null
        activeController = null
    }
}