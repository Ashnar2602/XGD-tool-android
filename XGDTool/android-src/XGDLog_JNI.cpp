// Android/JNI replacement for src/XGDLog.cpp.
// Same public interface as the original XGDLog class, but instead of writing
// to std::cerr/std::cout (CLI) or a wxWidgets MainFrame (desktop GUI), it
// forwards log lines and progress updates to callbacks that the JNI bridge
// (xgd_jni.cpp) sets, which in turn post them into the Kotlin/Java UI thread.
//
// NOT compiled as part of the upstream project; only included in the
// Android CMake target in place of src/XGDLog.cpp.

#include <atomic>
#include <chrono>
#include <cstdint>
#include <functional>
#include <mutex>
#include <string>

#include <android/log.h>

#include "XGDLog.h"

#define XGD_LOG_TAG "XgdCore"

LogLevel XGDLog::current_level = Normal;

namespace xgd_jni {

// Guarded by log_mutex; set once from JNI before a conversion job starts.
std::function<void(const std::string&)> g_log_callback;
std::function<void(uint64_t, uint64_t)> g_progress_callback;
std::mutex g_callback_mutex;

void set_log_callback(std::function<void(const std::string&)> cb) {
    std::lock_guard<std::mutex> lock(g_callback_mutex);
    g_log_callback = std::move(cb);
}

void set_progress_callback(std::function<void(uint64_t, uint64_t)> cb) {
    std::lock_guard<std::mutex> lock(g_callback_mutex);
    g_progress_callback = std::move(cb);
}

void clear_callbacks() {
    std::lock_guard<std::mutex> lock(g_callback_mutex);
    g_log_callback = nullptr;
    g_progress_callback = nullptr;
}

} // namespace xgd_jni

XGDLog& XGDLog::operator<<(Manip manip)
{
    if (manip == Manip::Endl && should_log())
    {
        __android_log_print(ANDROID_LOG_INFO, XGD_LOG_TAG, "%s", oss.str().c_str());

        std::lock_guard<std::mutex> lock(xgd_jni::g_callback_mutex);
        if (xgd_jni::g_log_callback)
        {
            xgd_jni::g_log_callback(oss.str());
        }
        oss.str("");
        oss.clear();
    }
    return *this;
}

void XGDLog::print_progress(uint64_t processed, uint64_t total)
{
    static auto last_update_time = std::chrono::steady_clock::now();

    auto now = std::chrono::steady_clock::now();
    auto duration_since_last_update = std::chrono::duration_cast<std::chrono::milliseconds>(now - last_update_time);

    if (duration_since_last_update.count() < 100 && processed < total)
    {
        return;
    }

    last_update_time = now;

    __android_log_print(ANDROID_LOG_INFO, XGD_LOG_TAG, "progress %llu/%llu",
                         (unsigned long long)processed, (unsigned long long)total);

    std::lock_guard<std::mutex> lock(xgd_jni::g_callback_mutex);
    if (xgd_jni::g_progress_callback)
    {
        xgd_jni::g_progress_callback(processed, total);
    }
}
