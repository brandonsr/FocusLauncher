package com.example.focuslauncher

import android.graphics.Bitmap
import android.media.session.MediaController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class MusicState(
    val title: String,
    val artist: String,
    val albumArt: Bitmap?,
    val isPlaying: Boolean
)

object MusicRepository {

    private val _state = MutableStateFlow<MusicState?>(null)
    val state: StateFlow<MusicState?> = _state

    private var controller: MediaController? = null

    fun update(state: MusicState?, controller: MediaController?) {
        _state.value = state
        this.controller = controller
    }

    fun clear() {
        _state.value = null
        controller = null
    }

    fun playPause() {
        val ctrl = controller ?: return
        if (_state.value?.isPlaying == true) ctrl.transportControls.pause()
        else ctrl.transportControls.play()
    }

    fun next() = controller?.transportControls?.skipToNext()

    fun previous() = controller?.transportControls?.skipToPrevious()
}