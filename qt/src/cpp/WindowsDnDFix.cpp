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

    // Get temp directory
    wchar_t tempDir[MAX_PATH];
    GetTempPathW(MAX_PATH, tempDir);

    // Convert exe path to UTF-8 for batch file
    char exePathUtf8[MAX_PATH * 3];
    WideCharToMultiByte(CP_UTF8, 0, exePath, -1, exePathUtf8, sizeof(exePathUtf8), nullptr, nullptr);

    // Write a temp batch file that launches our exe
    char batPathUtf8[MAX_PATH * 3];
    char tempDirUtf8[MAX_PATH * 3];
    WideCharToMultiByte(CP_UTF8, 0, tempDir, -1, tempDirUtf8, sizeof(tempDirUtf8), nullptr, nullptr);
    snprintf(batPathUtf8, sizeof(batPathUtf8), "%saejuice-relaunch.bat", tempDirUtf8);

    FILE *batFile = fopen(batPathUtf8, "w");
    if (batFile) {
        fprintf(batFile, "@echo off\r\n");
        fprintf(batFile, "\"%s\"\r\n", exePathUtf8);
        fclose(batFile);
    }

    // Convert bat path back to wide for ShellExecuteW
    wchar_t batPathW[MAX_PATH];
    MultiByteToWideChar(CP_UTF8, 0, batPathUtf8, -1, batPathW, MAX_PATH);

    // Use explorer.exe to launch the batch file
    // explorer.exe always runs at medium integrity (non-elevated),
    // so the launched process inherits that
    ShellExecuteW(nullptr, L"open", L"explorer.exe",
                  batPathW, nullptr, SW_HIDE);

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
