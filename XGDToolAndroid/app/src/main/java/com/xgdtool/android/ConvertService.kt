package com.xgdtool.android

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.documentfile.provider.DocumentFile
import java.io.File

data class ConversionSettings(
    val fileType: Int,
    val scrubType: Int,
    val offlineMode: Boolean
)

interface ConvertServiceListener {
    fun onQueueProgress(fileIndex: Int, fileCount: Int, currentFileName: String)
    /** [indeterminate] true means "working, duration unknown" (show a spinning bar, not a stuck one). */
    fun onPhase(phaseLabel: String, indeterminate: Boolean)
    fun onFileProgress(processed: Long, total: Long)
    fun onLog(line: String)
    fun onFinished(succeeded: Int, failed: Int, cancelled: Boolean)
}

/**
 * Foreground service that owns the actual conversion work so it survives
 * the user backgrounding the app (Xbox 360 ISOs can be several GB; a plain
 * background thread tied to the Activity would get killed mid-conversion).
 *
 * Every file goes through 4 explicit, UI-visible phases so it's never
 * ambiguous whether the app is stuck or just working on something slow:
 *
 *   1. COPIA LOCALE   - SAF input Uri -> app cache (real path the native
 *                        code can open). Determinate, byte-accurate.
 *   2. CONVERSIONE    - native XGDTool core: connectivity check + online
 *                        title lookup (bounded by curl timeouts) then the
 *                        actual read/write pass. Starts indeterminate
 *                        (we don't know how long the network checks take)
 *                        and switches to determinate the moment the writer
 *                        reports real byte progress.
 *   3. SCRITTURA OUTPUT - cache -> user's SAF destination folder.
 *                        Determinate, byte-accurate.
 *   4. FINE            - done / error / cancelled, with the real reason
 *                        surfaced from native logs (see XGDLog.h fix).
 */
class ConvertService : Service() {

    private val binder = LocalBinder()
    private var listener: ConvertServiceListener? = null
    private var workerThread: Thread? = null
    @Volatile private var cancelRequested = false

    inner class LocalBinder : Binder() {
        fun getService(): ConvertService = this@ConvertService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    fun setListener(l: ConvertServiceListener?) {
        listener = l
    }

    fun isRunning(): Boolean = workerThread?.isAlive == true

    fun startQueue(inputs: List<Uri>, outputTree: Uri, settings: ConversionSettings) {
        if (isRunning()) return
        cancelRequested = false

        startForeground(NOTIF_ID, buildNotification(getString(R.string.notif_starting), 0, 0, true))

        workerThread = Thread {
            runQueue(inputs, outputTree, settings)
        }.also { it.start() }
    }

    fun requestCancel() {
        cancelRequested = true
        XgdNative.cancel()
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private fun postToUi(block: () -> Unit) = mainHandler.post(block)

    private fun phase(label: String, indeterminate: Boolean, notifText: String) {
        postToUi { listener?.onPhase(label, indeterminate) }
        updateNotification(notifText, 0, 1000, indeterminate)
    }

    private fun runQueue(inputs: List<Uri>, outputTree: Uri, settings: ConversionSettings) {
        var succeeded = 0
        var failed = 0
        val cacheIn = File(cacheDir, "xgd_in").apply { mkdirs() }
        val cacheOut = File(cacheDir, "xgd_out").apply { mkdirs() }

        for ((index, uri) in inputs.withIndex()) {
            if (cancelRequested) break

            val displayName = queryDisplayName(uri) ?: "file_$index"
            val fileTag = "(${index + 1}/${inputs.size}) $displayName"
            postToUi { listener?.onQueueProgress(index + 1, inputs.size, displayName) }

            cacheIn.listFiles()?.forEach { it.delete() }
            cacheOut.listFiles()?.forEach { it.deleteRecursively() }

            // --- Phase 1: copy SAF input into local cache ---
            val localInput = File(cacheIn, displayName)
            val sourceSize = querySize(uri)
            phase(getString(R.string.phase_copy_label, displayName), false, getString(R.string.notif_copy, fileTag))
            postToUi { listener?.onLog(getString(R.string.log_copy_start, displayName, formatBytes(sourceSize))) }
            try {
                contentResolver.openInputStream(uri).use { input ->
                    localInput.outputStream().use { output ->
                        if (input == null) throw java.io.IOException(getString(R.string.error_input_stream_null))
                        val buffer = ByteArray(1 shl 20)
                        var copied = 0L
                        var lastReportMs = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read < 0) break
                            output.write(buffer, 0, read)
                            copied += read
                            val now = System.currentTimeMillis()
                            if (now - lastReportMs > 150 || copied == sourceSize) {
                                lastReportMs = now
                                val total = if (sourceSize > 0) sourceSize else copied
                                postToUi { listener?.onFileProgress(copied, total) }
                                val pct = if (total > 0) ((copied * 1000) / total).toInt() else 0
                                updateNotification(getString(R.string.notif_copy, fileTag), pct, 1000, false)
                            }
                        }
                    }
                }
                val actualSize = localInput.length()
                if (sourceSize > 0 && actualSize != sourceSize) {
                    throw java.io.IOException(
                        getString(R.string.error_copy_incomplete, formatBytes(sourceSize), formatBytes(actualSize))
                    )
                }
                postToUi { listener?.onLog(getString(R.string.log_copy_done, formatBytes(sourceSize))) }
            } catch (e: Exception) {
                postToUi { listener?.onLog(getString(R.string.log_copy_error, displayName, e.message)) }
                failed++
                continue
            }

            // --- Phase 2: native conversion (connectivity check + title lookup + actual write) ---
            phase(
                getString(R.string.phase_convert_label),
                true,
                getString(R.string.notif_convert, fileTag)
            )
            postToUi {
                listener?.onLog(getString(R.string.log_engine_start))
            }

            var sawRealProgress = false
            val callback = object : XgdCallback {
                override fun onLog(line: String) {
                    postToUi { listener?.onLog("   ${line.trimEnd('\n')}") }
                }
                override fun onProgress(processed: Long, total: Long) {
                    if (!sawRealProgress) {
                        sawRealProgress = true
                        postToUi { listener?.onPhase(getString(R.string.phase_convert_writing_label), false) }
                    }
                    postToUi { listener?.onFileProgress(processed, total) }
                    val pct = if (total > 0) ((processed * 1000) / total).toInt() else 0
                    updateNotification(getString(R.string.notif_convert, fileTag), pct, 1000, false)
                }
            }

            val resultCode = XgdNative.convert(
                localInput.absolutePath,
                cacheOut.absolutePath,
                settings.fileType,
                settings.scrubType,
                /* split = */ false,
                settings.offlineMode,
                /* renameXbe = */ !settings.offlineMode,
                /* attachXbe = */ false,
                /* amPatch = */ false,
                callback
            )

            localInput.delete()
            val resultLabel = when (resultCode) {
                XgdNative.RESULT_OK -> getString(R.string.result_ok)
                XgdNative.RESULT_CANCELLED -> getString(R.string.result_cancelled)
                else -> getString(R.string.result_error)
            }
            postToUi { listener?.onLog(getString(R.string.log_engine_done, resultLabel, resultCode)) }

            when (resultCode) {
                XgdNative.RESULT_OK -> {
                    // --- Phase 3: copy converted output(s) to the SAF destination ---
                    phase(getString(R.string.phase_write_label), false, getString(R.string.notif_write, fileTag))
                    val copied = copyOutputToTree(cacheOut, outputTree) { done, total ->
                        postToUi { listener?.onFileProgress(done, total) }
                        val pct = if (total > 0) ((done * 1000) / total).toInt() else 0
                        updateNotification(getString(R.string.notif_write, fileTag), pct, 1000, false)
                    }
                    if (copied) {
                        postToUi { listener?.onLog(getString(R.string.log_write_done)) }
                        succeeded++
                    } else {
                        postToUi { listener?.onLog(getString(R.string.log_write_no_output)) }
                        failed++
                    }
                }
                XgdNative.RESULT_CANCELLED -> {
                    postToUi { listener?.onLog(getString(R.string.log_cancelled_by_user)) }
                }
                else -> {
                    failed++
                }
            }

            cacheOut.listFiles()?.forEach { it.deleteRecursively() }
        }

        val wasCancelled = cancelRequested
        postToUi { listener?.onFinished(succeeded, failed, wasCancelled) }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * Recursively copies every file under [localDir] into the SAF tree
     * [treeUri], preserving relative sub-folders (GOD output is a folder
     * tree; ZAR/CCI/CSO/ISO are single files). Reports byte-accurate
     * progress via [onProgress] so this phase is never a silent black box
     * even for multi-GB ZAR files.
     */
    private fun copyOutputToTree(localDir: File, treeUri: Uri, onProgress: (Long, Long) -> Unit): Boolean {
        val destRoot = DocumentFile.fromTreeUri(this, treeUri) ?: return false
        var anyFile = false
        var anyFailure = false

        val totalBytes = localDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        var copiedBytes = 0L
        var lastReportMs = 0L

        fun copyRecursive(src: File, destDir: DocumentFile) {
            src.listFiles()?.sortedBy { it.name }?.forEach { child ->
                if (child.isDirectory) {
                    val sub = destDir.findFile(child.name) ?: destDir.createDirectory(child.name)
                    if (sub != null) {
                        copyRecursive(child, sub)
                    } else {
                        anyFailure = true
                        postToUi { listener?.onLog(getString(R.string.log_write_error_dir, child.name)) }
                    }
                } else {
                    anyFile = true
                    val mime = "application/octet-stream"
                    val existing = destDir.findFile(child.name)
                    existing?.delete()
                    val destFile = destDir.createFile(mime, child.name)
                    if (destFile == null) {
                        anyFailure = true
                        postToUi { listener?.onLog(getString(R.string.log_write_error_file, child.name)) }
                        return@forEach
                    }
                    val outStream = contentResolver.openOutputStream(destFile.uri)
                    if (outStream == null) {
                        anyFailure = true
                        postToUi { listener?.onLog(getString(R.string.log_write_error_stream, child.name)) }
                        return@forEach
                    }
                    var writtenForFile = 0L
                    outStream.use { out ->
                        child.inputStream().use { input ->
                            val buffer = ByteArray(1 shl 20)
                            while (true) {
                                val read = input.read(buffer)
                                if (read < 0) break
                                out.write(buffer, 0, read)
                                writtenForFile += read
                                copiedBytes += read
                                val now = System.currentTimeMillis()
                                if (now - lastReportMs > 150 || copiedBytes == totalBytes) {
                                    lastReportMs = now
                                    onProgress(copiedBytes, if (totalBytes > 0) totalBytes else copiedBytes)
                                }
                            }
                        }
                    }
                    if (writtenForFile != child.length()) {
                        anyFailure = true
                        postToUi {
                            listener?.onLog(
                                getString(
                                    R.string.log_write_error_partial,
                                    child.name,
                                    formatBytes(writtenForFile),
                                    formatBytes(child.length())
                                )
                            )
                        }
                    }
                }
            }
        }

        copyRecursive(localDir, destRoot)
        return anyFile && !anyFailure
    }

    private fun queryDisplayName(uri: Uri): String? {
        return try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun querySize(uri: Uri): Long {
        return try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (idx >= 0 && cursor.moveToFirst() && !cursor.isNull(idx)) cursor.getLong(idx) else 0L
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return getString(R.string.bytes_unknown)
        val gb = bytes / (1024.0 * 1024.0 * 1024.0)
        return if (gb >= 0.1) String.format("%.2f GB", gb) else String.format("%.0f MB", bytes / (1024.0 * 1024.0))
    }

    // --- Notification plumbing ---

    private fun buildNotification(text: String, progress: Int, max: Int, indeterminate: Boolean): Notification {
        ensureChannel()
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
        builder.setProgress(max, progress, indeterminate)
        return builder.build()
    }

    private fun updateNotification(text: String, progress: Int, max: Int, indeterminate: Boolean) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIF_ID, buildNotification(text, progress, max, indeterminate))
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                nm.createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, getString(R.string.notif_channel_name), NotificationManager.IMPORTANCE_LOW)
                )
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "xgdtool_convert"
        private const val NOTIF_ID = 42
    }
}
