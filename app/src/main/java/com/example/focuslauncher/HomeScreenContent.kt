package com.example.focuslauncher

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap

@Composable
fun HomeScreenContent(
    apps: List<AppInfo>,
    pinnedApps: List<AppInfo> = emptyList()
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {

        // App grid — takes all remaining space above the pinned row
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(apps) { app ->
                AppIcon(
                    app = app,
                    onClick = { launchApp(context, app.packageName) }
                )
            }
        }

        // Pinned apps row — only shown when there's at least one pinned app
        if (pinnedApps.isNotEmpty()) {
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.15f),
                thickness = 1.dp
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(pinnedApps, key = { it.packageName }) { app ->
                    AppIcon(
                        app = app,
                        onClick = { launchApp(context, app.packageName) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIcon(
    app: AppInfo,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            bitmap = app.icon.toBitmap(96, 96).asImageBitmap(),
            contentDescription = app.label,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = app.label,
            fontSize = 11.sp,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}