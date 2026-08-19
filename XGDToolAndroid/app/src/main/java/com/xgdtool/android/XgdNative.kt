package com.xgdtool.android

/**
 * Thin JNI wrapper around libxgdtool.so, which is the XGDTool C++ core
 * (https://github.com/wiredopposite/XGDTool, GPL-3.0) cross-compiled for
 * arm64-v8a with its wxWidgets GUI stripped out. See android-src/xgd_jni.cpp
 * in the XGDTool checkout for the native side of this bridge.
 */
object XgdNative {

    // Must match FileType mapping in android-src/xgd_jni.cpp (file_type_from_int)
    const val FORMAT_ISO = 0
    const val FORMAT_GOD = 1
    const val FORMAT_CCI = 2
    const val FORMAT_CSO = 3
    const val FORMAT_ZAR = 4
    const val FORMAT_DIR = 5
    const val FORMAT_XBE = 6

    // Must match ScrubType mapping in android-src/xgd_jni.cpp (scrub_type_from_int)
    const val SCRUB_NONE = 0
    const val SCRUB_PARTIAL = 1
    const val SCRUB_FULL = 2

    // Result codes returned by convert()
    const val RESULT_OK = 0
    const val RESULT_ERROR = 1
    const val RESULT_CANCELLED = 2

    init {
        System.loadLibrary("xgdtool")
    }

    /**
     * Runs synchronously on the calling thread - always invoke from a
     * background thread. inputPath and outputDir must be real filesystem
     * paths (not content:// Uris); the caller is responsible for staging
     * SAF-selected files into app-local storage first and copying results
     * back out afterwards.
     */
    external fun convert(
        inputPath: String,
        outputDir: String,
        fileType: Int,
        scrubType: Int,
        split: Boolean,
        offlineMode: Boolean,
        renameXbe: Boolean,
        attachXbe: Boolean,
        amPatch: Boolean,
        callback: XgdCallback
    ): Int

    /** Safe to call from any thread while a convert() call is in flight. */
    external fun cancel()
}

interface XgdCallback {
    fun onLog(line: String)
    fun onProgress(processed: Long, total: Long)
}
