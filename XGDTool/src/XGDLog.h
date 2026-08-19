#ifndef _XGDLOG_H_
#define _XGDLOG_H_

#include <iostream>
#include <sstream>

enum LogLevel {
    None = 0,
    Error,
    Normal,
    Debug
};

class XGDLog 
{
public:
    enum Manip { Endl = 176 };

    XGDLog(LogLevel level = Normal) 
        : log_level(level) {}

    ~XGDLog()
    {
        // Route through the same flush path as an explicit "<< Endl" (CLI:
        // stdout/stderr, desktop GUI: MainFrame, Android: JNI callback).
        // Without this, any line built without an explicit Endl (there are
        // several in this codebase, e.g. InputHelper's catch blocks using
        // "\n" instead of Endl) would fall back to raw std::cerr, which is
        // invisible on Android (no attached console) and would silently
        // swallow error messages and success confirmations alike.
        if (should_log() && !oss.str().empty())
        {
            *this << Endl;
        }
    }

    template <typename T>
    XGDLog& operator<<(const T& value) 
    {
        if (should_log()) 
        {
            oss << value;
        }
        return *this;
    }

    XGDLog& operator<<(Manip manip);

    void set_log_level(LogLevel level) { current_level = level; }
    bool should_log() const { return current_level >= log_level; }
    LogLevel get_log_level() const { return current_level; }

    void print_progress(uint64_t processed, uint64_t total);

private:
    static LogLevel current_level;
    std::ostringstream oss;
    LogLevel log_level;    
};

#endif // _XGDLOG_H_
