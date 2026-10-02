package com.example.focuslauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val cardShape = RoundedCornerShape(20.dp)

@Composable
fun ScreenTimeScreen(
    stats: ScreenTimeStats?,
    onDismiss: () -> Unit,
    onEnablePermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 40.dp, vertical = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        if (stats == null) {
            // Permission or Error state
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Screen Time",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Thin,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    "Usage access required to view insights",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.45f),
                    modifier = Modifier.padding(horizontal = 32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                TextButton(onClick = onEnablePermission) {
                    Text("Grant Permission", color = Color(0xFF4FC3F7), fontSize = 14.sp)
                }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.3f), fontSize = 14.sp)
                }
            }
        } else {
            // Stats state
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    "SCREEN TIME",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Thin,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 3.sp
                )

                Text(
                    text = stats.totalTimeFormatted,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Thin,
                    color = Color.White,
                    letterSpacing = (-2).sp
                )

                Divider()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(stats.topApps) { app ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = app.name,
                                fontSize = 15.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Light
                            )
                            Text(
                                text = app.durationFormatted,
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.45f),
                                fontWeight = FontWeight.Thin
                            )
                        }
                    }
                }

                TextButton(onClick = onDismiss) {
                    Text("Dismiss", color = Color.White.copy(alpha = 0.3f), fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(Color.White.copy(alpha = 0.1f))
    )
}
