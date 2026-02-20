#include "WindowsDnDFix.h"
#include <QtGlobal>

#ifdef Q_OS_WIN
#include <windows.h>
#include <shellapi.h>
#include <stdio.h>
#endif

bool WindowsDnDFix::relaunchIfElevated(const char *argv0)
{
#ifdef Q_OS_WIN
    Q_UNUSED(argv0)

    if (!isElevated())
        return false;

    // Get the full path to our executable
    wchar_t exePath[MAX_PATH];
    GetModuleFileNameW(nullptr, exePath, MAX_PATH);

    // Use explorer.exe to launch the exe directly
    // explorer.exe always runs at medium integrity (non-elevated),
    // so the launched process inherits that
    ShellExecuteW(nullptr, L"open", L"explorer.exe",
                  exePath, nullptr, SW_SHOWNORMAL);

    return true;
#else
    Q_UNUSED(argv0)
    return false;
#endif
}

bool WindowsDnDFix::isElevated()
{
#ifdef Q_OS_WIN
    // Use TokenElevation to check actual process elevation, not just admin group membership
    HANDLE token = nullptr;
    if (!OpenProcessToken(GetCurrentProcess(), TOKEN_QUERY, &token))
        return false;

    TOKEN_ELEVATION elevation;
    DWORD size = sizeof(TOKEN_ELEVATION);
    BOOL result = GetTokenInformation(token, TokenElevation, &elevation, sizeof(elevation), &size);
    CloseHandle(token);

    if (!result)
        return false;

    return elevation.TokenIsElevated != 0;
#else
    return false;
#endif
}
