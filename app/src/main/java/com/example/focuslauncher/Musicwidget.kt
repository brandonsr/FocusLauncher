package com.example.focuslauncher

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val cardBg    = Color.White.copy(alpha = 0.07f)
private val cardShape = RoundedCornerShape(20.dp)

@Composable
fun MusicWidget(
    musicState: MusicState?,
    isListenerEnabled: Boolean,
    onEnableListener: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        !isListenerEnabled -> PermissionCard(onEnableListener, modifier)
        musicState == null  -> IdleCard(modifier)
        else                -> PlayerCard(musicState, onPlayPause, onNext, onPrevious, modifier)
    }
}

// Permission prompt

@Composable
private fun PermissionCard(onEnable: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(cardBg, cardShape)
            .clip(cardShape)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("♪", fontSize = 20.sp, color = Color.White.copy(alpha = 0.35f))
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    "Music widget",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    "Tap to enable",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.45f)
                )
            }
        }
        Text(
            "Enable",
            fontSize = 13.sp,
            color = Color(0xFF4FC3F7),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onEnable() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

// Idle / nothing playing

@Composable
private fun IdleCard(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(cardBg, cardShape)
            .clip(cardShape)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("♪", fontSize = 18.sp, color = Color.White.copy(alpha = 0.25f))
        Spacer(Modifier.width(12.dp))
        Text(
            "No music playing",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.35f)
        )
    }
}

// Active mini-player

@Composable
private fun PlayerCard(
    state: MusicState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(cardBg, cardShape)
            .clip(cardShape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (state.albumArt != null) {
            Image(
                bitmap = state.albumArt.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("♫", fontSize = 20.sp, color = Color.White.copy(alpha = 0.4f))
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = state.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (state.artist.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = state.artist,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.width(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            ControlButton(label = "⏮", onClick = onPrevious)
            Spacer(Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(50))
                    .clip(RoundedCornerShape(50))
                    .clickable { onPlayPause() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (state.isPlaying) "⏸" else "▶",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
            Spacer(Modifier.width(4.dp))
            ControlButton(label = "⏭", onClick = onNext)
        }
    }
}

@Composable
private fun ControlButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(50))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 18.sp, color = Color.White.copy(alpha = 0.65f))
    }
}