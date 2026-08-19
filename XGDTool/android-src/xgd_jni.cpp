// JNI bridge between the Kotlin UI (com.xgdtool.android) and the XGDTool
// C++ core (InputHelper / ImageWriter / etc, unmodified from upstream).
//
// Design notes:
//  - convert() is called from a background thread on the Kotlin side
//    (Dispatchers.IO), and runs synchronously to completion on that same
//    native thread. Because we never leave the calling thread, we can use
//    the JNIEnv* handed to us directly for callbacks (no JavaVM::AttachCurrentThread
//    dance needed).
//  - Progress/log callbacks are wired through XGDLog_JNI.cpp's
//    xgd_jni::set_log_callback / set_progress_callback, which XGDLog (the
//    upstream class every writer already logs through) forwards to.
//  - cancel() may be called from a *different* thread (the UI thread, when
//    the user taps "Cancel"). It only touches an InputHelper* guarded by a
//    mutex, so it's safe to call concurrently with convert().
//
// Not part of upstream XGDTool; built only for the Android target.

#include <jni.h>
#include <android/log.h>

#include <memory>
#include <mutex>
#include <string>
#include <vector>

#define XGD_LOG_TAG "XgdJNI"
#define XGD_LOGE(...) __android_log_print(ANDROID_LOG_ERROR, XGD_LOG_TAG, __VA_ARGS__)
#define XGD_LOGI(...) __android_log_print(ANDROID_LOG_INFO, XGD_LOG_TAG, __VA_ARGS__)

#include "InputHelper/InputHelper.h"
#include "InputHelper/Types.h"
#include "XGDException.h"

namespace xgd_jni {
void set_log_callback(std::function<void(const std::string&)> cb);
void set_progress_callback(std::function<void(uint64_t, uint64_t)> cb);
void clear_callbacks();
}

namespace {

std::mutex g_active_mutex;
InputHelper* g_active_helper = nullptr; // non-owning, valid only during convert()

std::string jstring_to_string(JNIEnv* env, jstring js)
{
    if (js == nullptr) return "";
    const char* chars = env->GetStringUTFChars(js, nullptr);
    std::string result(chars);
    env->ReleaseStringUTFChars(js, chars);
    return result;
}

FileType file_type_from_int(jint v)
{
    switch (v)
    {
        case 0: return FileType::ISO;
        case 1: return FileType::GoD;
        case 2: return FileType::CCI;
        case 3: return FileType::CSO;
        case 4: return FileType::ZAR;
        case 5: return FileType::DIR;
        case 6: return FileType::XBE;
        default: return FileType::UNKNOWN;
    }
}

ScrubType scrub_type_from_int(jint v)
{
    switch (v)
    {
        case 1: return ScrubType::PARTIAL;
        case 2: return ScrubType::FULL;
        default: return ScrubType::NONE;
    }
}

} // namespace

extern "C" {

JNIEXPORT jint JNICALL
Java_com_xgdtool_android_XgdNative_convert(
    JNIEnv* env, jobject /* thiz */,
    jstring jInputPath, jstring jOutputDir,
    jint jFileType, jint jScrubType,
    jboolean jSplit, jboolean jOfflineMode,
    jboolean jRenameXbe, jboolean jAttachXbe, jboolean jAmPatch,
    jobject jCallback)
{
    // Always run at Debug log level: this app's whole point is showing the
    // user every step, so there's no reason to hide XGDTool's internal
    // Debug-level trace lines (equivalent to the CLI's --debug flag).
    XGDLog().set_log_level(LogLevel::Debug);

    jclass callback_class = jCallback ? env->GetObjectClass(jCallback) : nullptr;
    jmethodID on_log = callback_class
        ? env->GetMethodID(callback_class, "onLog", "(Ljava/lang/String;)V")
        : nullptr;
    if (env->ExceptionCheck())
    {
        XGD_LOGE("GetMethodID(onLog) threw, clearing");
        env->ExceptionClear();
        on_log = nullptr;
    }
    jmethodID on_progress = callback_class
        ? env->GetMethodID(callback_class, "onProgress", "(JJ)V")
        : nullptr;
    if (env->ExceptionCheck())
    {
        XGD_LOGE("GetMethodID(onProgress) threw, clearing");
        env->ExceptionClear();
        on_progress = nullptr;
    }
    XGD_LOGI("callback wiring: class=%p on_log=%p on_progress=%p",
             (void*)callback_class, (void*)on_log, (void*)on_progress);

    xgd_jni::set_log_callback([env, jCallback, on_log](const std::string& line) {
        if (jCallback && on_log)
        {
            jstring jline = env->NewStringUTF(line.c_str());
            env->CallVoidMethod(jCallback, on_log, jline);
            env->DeleteLocalRef(jline);
        }
    });

    xgd_jni::set_progress_callback([env, jCallback, on_progress](uint64_t processed, uint64_t total) {
        if (jCallback && on_progress)
        {
            env->CallVoidMethod(jCallback, on_progress,
                                 static_cast<jlong>(processed), static_cast<jlong>(total));
        }
    });

    int result_code = 0; // 0 = ok, 1 = error, 2 = cancelled
    try
    {
        std::filesystem::path in_path(jstring_to_string(env, jInputPath));
        std::filesystem::path out_dir(jstring_to_string(env, jOutputDir));

        OutputSettings settings;
        settings.file_type = file_type_from_int(jFileType);
        settings.scrub_type = scrub_type_from_int(jScrubType);
        settings.split = jSplit;
        settings.offline_mode = jOfflineMode;
        settings.rename_xbe = jRenameXbe;
        settings.attach_xbe = jAttachXbe;
        settings.allowed_media_patch = jAmPatch;

        auto helper = std::make_unique<InputHelper>(in_path, out_dir, settings);

        {
            std::lock_guard<std::mutex> lock(g_active_mutex);
            g_active_helper = helper.get();
        }

        helper->process_all();

        {
            std::lock_guard<std::mutex> lock(g_active_mutex);
            g_active_helper = nullptr;
        }

        if (!helper->failed_inputs().empty())
        {
            result_code = 1;
        }
    }
    catch (const XGDException& e)
    {
        if (e.code() == ErrCode::CANCELLED)
        {
            result_code = 2;
        }
        else
        {
            result_code = 1;
            if (jCallback && on_log)
            {
                std::string msg = std::string("Error: ") + e.what();
                jstring jline = env->NewStringUTF(msg.c_str());
                env->CallVoidMethod(jCallback, on_log, jline);
                env->DeleteLocalRef(jline);
            }
        }
    }
    catch (const std::exception& e)
    {
        result_code = 1;
        if (jCallback && on_log)
        {
            std::string msg = std::string("Error: ") + e.what();
            jstring jline = env->NewStringUTF(msg.c_str());
            env->CallVoidMethod(jCallback, on_log, jline);
            env->DeleteLocalRef(jline);
        }
    }

    {
        std::lock_guard<std::mutex> lock(g_active_mutex);
        g_active_helper = nullptr;
    }

    xgd_jni::clear_callbacks();
    return result_code;
}

JNIEXPORT void JNICALL
Java_com_xgdtool_android_XgdNative_cancel(JNIEnv* /* env */, jobject /* thiz */)
{
    std::lock_guard<std::mutex> lock(g_active_mutex);
    if (g_active_helper)
    {
        g_active_helper->cancel_processing();
    }
}

} // extern "C"
