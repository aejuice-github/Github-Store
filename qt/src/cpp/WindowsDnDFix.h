#ifndef WINDOWSDNDFIX_H
#define WINDOWSDNDFIX_H

#ifdef Q_OS_WIN
#include <windows.h>
#endif

class WindowsDnDFix
{
public:
    // Returns true if the app was relaunched as non-elevated (caller should exit)
    static bool relaunchIfElevated(const char *argv0);

private:
    static bool isElevated();
};

#endif // WINDOWSDNDFIX_H
