package com.xpike.fit.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

internal data class FitUpdateInfo(
    val versionName: String,
    val versionCode: Long,
    val downloadUrl: String,
    val sha256: String,
    val sizeBytes: Long,
    val releaseNotes: String,
)

internal sealed interface FitUpdateCheckResult {
    data class Available(val update: FitUpdateInfo) : FitUpdateCheckResult
    data class UpToDate(val latestVersion: String) : FitUpdateCheckResult
    data object NoRelease : FitUpdateCheckResult
}

internal class FitUpdater(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences("fit_update_state", Context.MODE_PRIVATE)

    suspend fun checkForUpdate(): FitUpdateCheckResult = withContext(Dispatchers.IO) {
        val connection = openConnection(RELEASE_API_URL)
        try {
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) return@withContext FitUpdateCheckResult.NoRelease
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw IllegalStateException("GitHub returned HTTP $responseCode")
            }

            val releaseJson = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val release = JSONObject(releaseJson)
            val versionName = release.getString("tag_name").removePrefix("v").removePrefix("V")
            val versionCode = versionCodeFor(versionName)
            val asset = release.getJSONArray("assets").let { assets ->
                (0 until assets.length())
                    .map(assets::getJSONObject)
                    .firstOrNull { it.optString("name") == APK_ASSET_NAME }
            } ?: throw IllegalStateException("Release asset $APK_ASSET_NAME is missing")

            val downloadUrl = asset.getString("browser_download_url")
            val uri = Uri.parse(downloadUrl)
            require(uri.scheme == "https" && uri.host == "github.com") { "Unexpected release download host" }

            val digest = asset.optString("digest").removePrefix("sha256:").lowercase()
            require(SHA256_REGEX.matches(digest)) { "The release asset has no valid SHA-256 digest" }
            val sizeBytes = asset.optLong("size", -1L)
            require(sizeBytes in 1..MAX_APK_SIZE_BYTES) { "The release APK size is invalid" }

            val currentVersionCode = currentVersionCode()
            if (versionCode <= currentVersionCode) {
                FitUpdateCheckResult.UpToDate(versionName)
            } else {
                FitUpdateCheckResult.Available(
                    FitUpdateInfo(
                        versionName = versionName,
                        versionCode = versionCode,
                        downloadUrl = downloadUrl,
                        sha256 = digest,
                        sizeBytes = sizeBytes,
                        releaseNotes = release.optString("body").take(MAX_RELEASE_NOTES_LENGTH),
                    ),
                )
            }
        } finally {
            connection.disconnect()
        }
    }

    suspend fun downloadApk(
        update: FitUpdateInfo,
        onProgress: suspend (Int) -> Unit,
    ): File = withContext(Dispatchers.IO) {
        val directory = File(appContext.cacheDir, "fit_updates").apply { mkdirs() }
        val temporaryFile = File(directory, "$APK_ASSET_NAME.part")
        val apkFile = File(directory, APK_ASSET_NAME)
        temporaryFile.delete()

        val connection = openConnection(update.downloadUrl)
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IllegalStateException("APK download failed with HTTP ${connection.responseCode}")
            }

            val digest = MessageDigest.getInstance("SHA-256")
            var downloaded = 0L
            var lastProgress = -1
            connection.inputStream.use { input ->
                temporaryFile.outputStream().buffered().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        downloaded += count
                        require(downloaded <= MAX_APK_SIZE_BYTES) { "APK exceeds the allowed download size" }
                        output.write(buffer, 0, count)
                        digest.update(buffer, 0, count)

                        val progress = ((downloaded * 100L) / update.sizeBytes).toInt().coerceIn(0, 100)
                        if (progress != lastProgress) {
                            lastProgress = progress
                            onProgress(progress)
                        }
                    }
                }
            }

            require(downloaded == update.sizeBytes) { "The downloaded APK size does not match the release" }
            val actualDigest = digest.digest().joinToString("") { byte -> "%02x".format(byte) }
            require(actualDigest == update.sha256) { "The downloaded APK failed its SHA-256 check" }

            if (apkFile.exists()) apkFile.delete()
            check(temporaryFile.renameTo(apkFile)) { "Could not finalize the downloaded APK" }
            apkFile
        } catch (error: Throwable) {
            temporaryFile.delete()
            throw error
        } finally {
            connection.disconnect()
        }
    }

    fun canRequestPackageInstalls(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O || appContext.packageManager.canRequestPackageInstalls()

    fun unknownSourcesSettingsIntent(): Intent = Intent(
        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
        Uri.parse("package:${appContext.packageName}"),
    )

    fun installerIntent(apkFile: File): Intent {
        val apkUri = FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.fileprovider",
            apkFile,
        )
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, APK_MIME_TYPE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun shouldCheckAutomatically(nowMillis: Long = System.currentTimeMillis()): Boolean {
        val lastCheck = preferences.getLong(KEY_LAST_CHECK, 0L)
        return lastCheck == 0L || nowMillis < lastCheck || nowMillis - lastCheck >= AUTO_CHECK_INTERVAL_MS
    }

    fun markChecked(nowMillis: Long = System.currentTimeMillis()) {
        preferences.edit().putLong(KEY_LAST_CHECK, nowMillis).apply()
    }

    fun currentVersionName(): String =
        appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName ?: "bilinmiyor"

    private fun currentVersionCode(): Long {
        val packageInfo = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode else packageInfo.versionCode.toLong()
    }

    private fun openConnection(url: String): HttpURLConnection {
        val uri = Uri.parse(url)
        require(uri.scheme == "https" && uri.host in TRUSTED_HOSTS) { "Only trusted HTTPS update sources are allowed" }
        return (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = NETWORK_TIMEOUT_MS
            readTimeout = NETWORK_TIMEOUT_MS
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "Fit-Android-Updater")
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
        }
    }

    private fun versionCodeFor(versionName: String): Long {
        val match = VERSION_REGEX.matchEntire(versionName)
            ?: throw IllegalStateException("Release tag must use major.minor.patch, for example 0.2.0")
        val major = match.groupValues[1].toLong()
        val minor = match.groupValues[2].toLong()
        val patch = match.groupValues[3].toLong()
        require(major <= 2_147L && minor < 1_000L && patch < 1_000L) {
            "Release version is outside the supported version-code format"
        }
        val code = major * 1_000_000L + minor * 1_000L + patch
        require(code in 1L..Int.MAX_VALUE.toLong()) { "Release version code exceeds Android's supported range" }
        return code
    }

    private companion object {
        const val RELEASE_API_URL = "https://api.github.com/repos/xpike-dgm/fit/releases/latest"
        const val APK_ASSET_NAME = "fit-release.apk"
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
        const val KEY_LAST_CHECK = "last_check_millis"
        const val AUTO_CHECK_INTERVAL_MS = 12L * 60L * 60L * 1_000L
        const val NETWORK_TIMEOUT_MS = 20_000
        const val MAX_APK_SIZE_BYTES = 250L * 1024L * 1024L
        const val MAX_RELEASE_NOTES_LENGTH = 2_000
        val TRUSTED_HOSTS = setOf("api.github.com", "github.com")
        val VERSION_REGEX = Regex("(\\d+)\\.(\\d+)\\.(\\d+)")
        val SHA256_REGEX = Regex("[0-9a-f]{64}")
    }
}
