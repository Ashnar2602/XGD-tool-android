package com.xgdtool.android

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.xgdtool.android.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), ConvertServiceListener {

    private lateinit var binding: ActivityMainBinding

    private var selectedInputs: List<Uri> = emptyList()
    private var selectedOutputTree: Uri? = null

    private var service: ConvertService? = null
    private var bound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as ConvertService.LocalBinder).getService()
            service?.setListener(this@MainActivity)
            bound = true
            refreshRunningState()
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            bound = false
        }
    }

    private val pickFiles = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            selectedInputs = uris
            binding.inputSummary.text = resources.getQuantityString(R.plurals.input_files_selected, uris.size, uris.size)
        }
    }

    private val pickOutputTree = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            selectedOutputTree = uri
            binding.outputSummary.text = getString(R.string.output_summary_set, uri.lastPathSegment)
        }
    }

    private val requestNotifPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotifPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        binding.btnSelectFiles.setOnClickListener {
            pickFiles.launch(arrayOf("*/*"))
        }

        binding.btnSelectOutput.setOnClickListener {
            pickOutputTree.launch(null)
        }

        binding.btnConvert.setOnClickListener { startConversion() }
        binding.btnCancel.setOnClickListener { service?.requestCancel() }

        val intent = Intent(this, ConvertService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bound) {
            service?.setListener(null)
            unbindService(connection)
        }
    }

    private fun refreshRunningState() {
        val running = service?.isRunning() == true
        binding.btnConvert.isEnabled = !running
        binding.btnCancel.isEnabled = running
    }

    private fun startConversion() {
        val inputs = selectedInputs
        val outputTree = selectedOutputTree
        if (inputs.isEmpty()) {
            binding.logView.append(getString(R.string.error_no_input) + "\n")
            return
        }
        if (outputTree == null) {
            binding.logView.append(getString(R.string.error_no_output) + "\n")
            return
        }

        val fileType = when (binding.formatGroup.checkedChipId) {
            binding.formatGod.id -> XgdNative.FORMAT_GOD
            binding.formatCci.id -> XgdNative.FORMAT_CCI
            binding.formatCso.id -> XgdNative.FORMAT_CSO
            else -> XgdNative.FORMAT_ZAR
        }
        val offline = binding.checkOffline.isChecked

        binding.logView.text = ""
        binding.progressBar.progress = 0
        val settings = ConversionSettings(fileType, XgdNative.SCRUB_NONE, offline)

        val serviceIntent = Intent(this, ConvertService::class.java)
        startService(serviceIntent) // ensure it keeps running independent of binding lifecycle
        service?.startQueue(inputs, outputTree, settings)
        binding.btnConvert.isEnabled = false
        binding.btnCancel.isEnabled = true
    }

    // --- ConvertServiceListener ---

    override fun onQueueProgress(fileIndex: Int, fileCount: Int, currentFileName: String) {
        binding.progressLabel.text = getString(R.string.queue_progress, fileIndex, fileCount, currentFileName)
        binding.progressBar.progress = 0
    }

    override fun onPhase(phaseLabel: String, indeterminate: Boolean) {
        binding.phaseLabel.text = phaseLabel
        binding.progressBar.isIndeterminate = indeterminate
        if (!indeterminate) {
            binding.progressBar.progress = 0
        }
    }

    override fun onFileProgress(processed: Long, total: Long) {
        if (total > 0 && !binding.progressBar.isIndeterminate) {
            val pct = ((processed * 1000) / total).toInt()
            binding.progressBar.progress = pct
            binding.progressLabel.text = "${pct / 10}.${pct % 10}%  (${formatBytesUi(processed)} / ${formatBytesUi(total)})"
        }
    }

    private fun formatBytesUi(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val gb = bytes / (1024.0 * 1024.0 * 1024.0)
        return if (gb >= 0.1) String.format("%.2f GB", gb) else String.format("%.0f MB", bytes / (1024.0 * 1024.0))
    }

    override fun onLog(line: String) {
        binding.logView.append(line + "\n")
    }

    override fun onFinished(succeeded: Int, failed: Int, cancelled: Boolean) {
        val status = when {
            cancelled -> getString(R.string.finished_cancelled)
            failed == 0 -> resources.getQuantityString(R.plurals.finished_success, succeeded, succeeded)
            else -> getString(R.string.finished_partial, succeeded, failed)
        }
        binding.phaseLabel.text = status
        binding.progressBar.isIndeterminate = false
        binding.progressBar.progress = if (failed == 0 && !cancelled) 1000 else 0
        binding.logView.append("$status\n")
        binding.btnConvert.isEnabled = true
        binding.btnCancel.isEnabled = false
    }
}
