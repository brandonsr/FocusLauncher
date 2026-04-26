package com.example.focuslauncher

import android.app.Notification
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class MusicNotificationListener : NotificationListenerService() {

    private var activeCallback: MediaController.Callback? = null
    private var activeController: MediaController? = null

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        tryExtractMedia(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (MusicRepository.state.value != null) {
            MusicRepository.clear()
            detachCallback()
        }
    }

    override fun onListenerDisconnected() {
        MusicRepository.clear()
        detachCallback()
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