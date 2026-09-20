package com.example.data.update

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class UpdateInfo(
    val hasUpdate: Boolean,
    val latestVersionName: String,
    val currentVersionName: String,
    val releaseNotes: String,
    val apkDownloadUrl: String?,
    val publishedAt: String?
)

class AppUpdateManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("biomatch_update_prefs", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val KEY_GITHUB_REPO = "github_repo_slug"
        // Default repository placeholder or user's repo:
        const val DEFAULT_REPO = "ganrax/biomatch-xg"
    }

    var githubRepo: String
        get() = prefs.getString(KEY_GITHUB_REPO, DEFAULT_REPO) ?: DEFAULT_REPO
        set(value) = prefs.edit().putString(KEY_GITHUB_REPO, value.trim()).apply()

    suspend fun checkForUpdates(): UpdateInfo = withContext(Dispatchers.IO) {
        val currentVersion = BuildConfig.VERSION_NAME
        val repo = githubRepo
        val url = "https://api.github.com/repos/$repo/releases/latest"

        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "BioMatch-xG-Android-App")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext UpdateInfo(
                    hasUpdate = false,
                    latestVersionName = currentVersion,
                    currentVersionName = currentVersion,
                    releaseNotes = "Nem érhető el nyilvános release a(z) $repo tárhelyen (HTTP ${response.code}).",
                    apkDownloadUrl = null,
                    publishedAt = null
                )
            }

            val bodyString = response.body?.string() ?: ""
            val json = JSONObject(bodyString)
            val tagName = json.optString("tag_name", "").removePrefix("v").trim()
            val releaseBody = json.optString("body", "Automatikus GitHub Action frissítés.")
            val publishedAt = json.optString("published_at", "")

            var apkUrl: String? = null
            val assets = json.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.optString("browser_download_url")
                        break
                    }
                }
            }

            // Fallback direct release URL if asset array is empty
            if (apkUrl == null && repo.isNotBlank()) {
                apkUrl = "https://github.com/$repo/releases/download/latest/BioMatch-xG-latest.apk"
            }

            val hasUpdate = tagName.isNotBlank() && (tagName != currentVersion && tagName != "latest") || (tagName == "latest")

            UpdateInfo(
                hasUpdate = hasUpdate,
                latestVersionName = if (tagName.isNotBlank()) tagName else "Legújabb",
                currentVersionName = currentVersion,
                releaseNotes = releaseBody,
                apkDownloadUrl = apkUrl,
                publishedAt = publishedAt
            )
        } catch (e: Throwable) {
            Log.e("AppUpdateManager", "Failed to check updates", e)
            UpdateInfo(
                hasUpdate = false,
                latestVersionName = currentVersion,
                currentVersionName = currentVersion,
                releaseNotes = "Hálózati hiba a frissítések keresésekor: ${e.localizedMessage}",
                apkDownloadUrl = null,
                publishedAt = null
            )
        }
    }

    suspend fun downloadApk(
        downloadUrl: String,
        onProgress: (Float) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(downloadUrl)
            .header("User-Agent", "BioMatch-xG-Android-App")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IllegalStateException("A letöltés meghiúsult: HTTP ${response.code}")
        }

        val body = response.body ?: throw IllegalStateException("Üres válasz a szervertől.")
        val contentLength = body.contentLength()

        val cacheDir = context.externalCacheDir ?: context.cacheDir
        val apkFile = File(cacheDir, "BioMatch_Update.apk")
        if (apkFile.exists()) {
            apkFile.delete()
        }

        body.byteStream().use { input ->
            FileOutputStream(apkFile).use { output ->
                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    if (contentLength > 0) {
                        onProgress(totalBytesRead.toFloat() / contentLength.toFloat())
                    }
                }
                output.flush()
            }
        }

        apkFile
    }

    fun promptInstall(apkFile: File) {
        val authority = "${context.packageName}.fileprovider"
        val apkUri: Uri = FileProvider.getUriForFile(context, authority, apkFile)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(intent)
    }
}
