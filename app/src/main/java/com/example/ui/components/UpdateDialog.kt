package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.update.UpdateInfo
import com.example.ui.theme.AnomalyAmber
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DeepVoid
import com.example.ui.theme.KineticEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    downloadProgress: Float?,
    currentRepo: String,
    onSaveRepo: (String) -> Unit,
    onStartDownload: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var isEditingRepo by remember { mutableStateOf(false) }
    var repoInput by remember { mutableStateOf(currentRepo) }

    AlertDialog(
        onDismissRequest = {
            if (downloadProgress == null) onDismiss()
        },
        icon = {
            Icon(
                imageVector = Icons.Default.SystemUpdate,
                contentDescription = null,
                tint = if (updateInfo.hasUpdate) KineticEmerald else NeonCyan,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = if (updateInfo.hasUpdate) "Új Verzió Elérhető!" else "BioMatch xG Frissítés",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Jelenlegi verzió: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "v${updateInfo.currentVersionName} (Build #${BuildConfig.BUILD_NUMBER})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (updateInfo.hasUpdate) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Új elérhető verzió: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = KineticEmerald
                        )
                        Text(
                            text = "v${updateInfo.latestVersionName}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Black,
                            color = KineticEmerald,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "A legfrissebb kiadást használod.",
                        style = MaterialTheme.typography.bodySmall,
                        color = KineticEmerald
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Release notes
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepVoid.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = updateInfo.releaseNotes.take(300),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // GitHub repo slug configuration
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "GitHub tároló: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = currentRepo,
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(
                        onClick = { isEditingRepo = !isEditingRepo },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Módosítás",
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                AnimatedVisibility(visible = isEditingRepo) {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                        OutlinedTextField(
                            value = repoInput,
                            onValueChange = { repoInput = it },
                            label = { Text("GitHub Repo (felhasználó/repo)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                onSaveRepo(repoInput)
                                isEditingRepo = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepVoid),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Mentés", fontSize = 12.sp)
                        }
                    }
                }

                // Download Progress indicator
                downloadProgress?.let { progress ->
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Letöltés: ${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = NeonCyan,
                            trackColor = BorderSubtle
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (updateInfo.hasUpdate && updateInfo.apkDownloadUrl != null) {
                Button(
                    onClick = { onStartDownload(updateInfo.apkDownloadUrl) },
                    enabled = downloadProgress == null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KineticEmerald,
                        contentColor = DeepVoid
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (downloadProgress != null) "Letöltés..." else "Frissítés Most",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = downloadProgress == null
            ) {
                Text(
                    text = if (updateInfo.hasUpdate) "Később" else "Bezárás",
                    color = TextMuted
                )
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(16.dp)
    )
}
