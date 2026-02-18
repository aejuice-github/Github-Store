#include "ElevatedCopyHelper.h"
#include <QCoreApplication>
#include <QDebug>
#include <QDir>
#include <QFileInfo>

#ifdef Q_OS_WIN
#include <shellapi.h>
#include <sddl.h>
#endif

ElevatedCopyHelper::ElevatedCopyHelper(QObject *parent)
    : QObject(parent)
{
#ifdef Q_OS_WIN
    m_pipeName = "\\\\.\\pipe\\ComponentManagerHelper_"
                 + QString::number(GetCurrentProcessId());
#endif
}

ElevatedCopyHelper::~ElevatedCopyHelper()
{
    shutdown();
}

bool ElevatedCopyHelper::copyFiles(const QString &source, const QStringList &destinations)
{
#ifdef Q_OS_WIN
    // Try direct copy first — no UAC needed if we already have permission
    bool directSuccess = true;
    for (const QString &dest : destinations) {
        QDir().mkpath(QFileInfo(dest).absolutePath());
        if (QFile::exists(dest))
            QFile::remove(dest);
        if (!QFile::copy(source, dest)) {
            directSuccess = false;
            break;
        }
    }
    if (directSuccess)
        return true;

    // Clean up partial direct copies before elevating
    for (const QString &dest : destinations) {
        if (QFile::exists(dest))
            QFile::remove(dest);
    }

    if (m_running && !isHelperAlive()) {
        qDebug() << "Elevated helper died, will relaunch";
        CloseHandle(m_pipe);
        m_pipe = INVALID_HANDLE_VALUE;
        CloseHandle(m_helperProcess);
        m_helperProcess = nullptr;
        m_running = false;
    }

    if (!m_running) {
        if (!launchHelper())
            return false;
    }

    // Protocol: COPY|source|dest1|dest2|...
    QStringList parts;
    parts << "COPY" << source;
    parts.append(destinations);
    QString command = parts.join("|");

    if (!sendCommand(command)) {
        qDebug() << "Failed to send command to helper";
        m_running = false;
        return false;
    }

    QString response = readResponse();
    qDebug() << "Helper response:" << response;
    return response.startsWith("OK");
#else
    bool success = true;
    for (const QString &dest : destinations) {
        QDir().mkpath(QFileInfo(dest).absolutePath());
        if (QFile::exists(dest))
            QFile::remove(dest);
        if (!QFile::copy(source, dest))
            success = false;
    }
    return success;
#endif
}

bool ElevatedCopyHelper::launchHelper()
{
#ifdef Q_OS_WIN
    // Create named pipe with permissive security so elevated process can connect
    SECURITY_ATTRIBUTES sa = {};
    sa.nLength = sizeof(sa);
    sa.bInheritHandle = FALSE;

    PSECURITY_DESCRIPTOR pSD = nullptr;
    ConvertStringSecurityDescriptorToSecurityDescriptorW(
        L"D:(A;;GA;;;WD)", SDDL_REVISION_1, &pSD, nullptr);
    sa.lpSecurityDescriptor = pSD;

    m_pipe = CreateNamedPipeW(
        reinterpret_cast<LPCWSTR>(m_pipeName.utf16()),
        PIPE_ACCESS_DUPLEX,
        PIPE_TYPE_MESSAGE | PIPE_READMODE_MESSAGE | PIPE_WAIT,
        1, 4096, 4096, 5000, &sa);

    if (pSD)
        LocalFree(pSD);

    if (m_pipe == INVALID_HANDLE_VALUE) {
        qDebug() << "Failed to create named pipe:" << GetLastError();
        return false;
    }

    // Launch self as elevated helper
    QString exePath = QCoreApplication::applicationFilePath();
    QString args = "--pipe-helper \"" + m_pipeName + "\"";

    SHELLEXECUTEINFOW sei = {};
    sei.cbSize = sizeof(sei);
    sei.fMask = SEE_MASK_NOCLOSEPROCESS;
    sei.lpVerb = L"runas";
    sei.lpFile = reinterpret_cast<LPCWSTR>(exePath.utf16());
    sei.lpParameters = reinterpret_cast<LPCWSTR>(args.utf16());
    sei.nShow = SW_HIDE;

    if (!ShellExecuteExW(&sei)) {
        qDebug() << "UAC elevation denied or failed";
        CloseHandle(m_pipe);
        m_pipe = INVALID_HANDLE_VALUE;
        return false;
    }

    m_helperProcess = sei.hProcess;

    // Wait for helper to connect (timeout handled by pipe creation)
    if (!ConnectNamedPipe(m_pipe, nullptr)) {
        DWORD error = GetLastError();
        if (error != ERROR_PIPE_CONNECTED) {
            qDebug() << "Helper failed to connect:" << error;
            CloseHandle(m_pipe);
            m_pipe = INVALID_HANDLE_VALUE;
            TerminateProcess(m_helperProcess, 1);
            CloseHandle(m_helperProcess);
            m_helperProcess = nullptr;
            return false;
        }
    }

    qDebug() << "Elevated helper connected";
    m_running = true;
    return true;
#else
    return false;
#endif
}

bool ElevatedCopyHelper::sendCommand(const QString &command)
{
#ifdef Q_OS_WIN
    QByteArray data = command.toUtf8();
    DWORD written = 0;
    return WriteFile(m_pipe, data.constData(), data.size(), &written, nullptr);
#else
    Q_UNUSED(command);
    return false;
#endif
}

QString ElevatedCopyHelper::readResponse()
{
#ifdef Q_OS_WIN
    char buffer[4096];
    DWORD bytesRead = 0;
    if (ReadFile(m_pipe, buffer, sizeof(buffer) - 1, &bytesRead, nullptr)) {
        buffer[bytesRead] = '\0';
        return QString::fromUtf8(buffer, bytesRead);
    }
    return "ERROR read failed";
#else
    return "ERROR not supported";
#endif
}

bool ElevatedCopyHelper::isHelperAlive() const
{
#ifdef Q_OS_WIN
    if (!m_helperProcess)
        return false;
    DWORD exitCode = 0;
    if (GetExitCodeProcess(m_helperProcess, &exitCode))
        return exitCode == STILL_ACTIVE;
    return false;
#else
    return false;
#endif
}

void ElevatedCopyHelper::shutdown()
{
#ifdef Q_OS_WIN
    if (m_running && m_pipe != INVALID_HANDLE_VALUE)
        sendCommand("EXIT");
    m_running = false;

    if (m_pipe != INVALID_HANDLE_VALUE) {
        CloseHandle(m_pipe);
        m_pipe = INVALID_HANDLE_VALUE;
    }
    if (m_helperProcess) {
        WaitForSingleObject(m_helperProcess, 3000);
        CloseHandle(m_helperProcess);
        m_helperProcess = nullptr;
    }
#endif
}

// --- Static helper entry point (runs in the elevated child process) ---

int ElevatedCopyHelper::runHelper(const QString &pipeName)
{
#ifdef Q_OS_WIN
    HANDLE pipe = CreateFileW(
        reinterpret_cast<LPCWSTR>(pipeName.utf16()),
        GENERIC_READ | GENERIC_WRITE,
        0, nullptr, OPEN_EXISTING, 0, nullptr);

    if (pipe == INVALID_HANDLE_VALUE)
        return 1;

    DWORD mode = PIPE_READMODE_MESSAGE;
    SetNamedPipeHandleState(pipe, &mode, nullptr, nullptr);

    char buffer[4096];
    while (true) {
        DWORD bytesRead = 0;
        if (!ReadFile(pipe, buffer, sizeof(buffer) - 1, &bytesRead, nullptr))
            break;

        buffer[bytesRead] = '\0';
        QString command = QString::fromUtf8(buffer, bytesRead);

        if (command == "EXIT")
            break;

        if (command.startsWith("COPY|")) {
            QStringList parts = command.split("|");
            if (parts.size() >= 3) {
                QString source = parts[1];
                bool success = true;
                for (int i = 2; i < parts.size(); i++) {
                    QString destination = parts[i];
                    QDir().mkpath(QFileInfo(destination).absolutePath());
                    if (QFile::exists(destination))
                        QFile::remove(destination);
                    if (!QFile::copy(source, destination))
                        success = false;
                }
                QByteArray response = success ? "OK" : "ERROR copy failed";
                DWORD written = 0;
                WriteFile(pipe, response.constData(), response.size(), &written, nullptr);
            }
        }
    }

    CloseHandle(pipe);
    return 0;
#else
    Q_UNUSED(pipeName);
    return 1;
#endif
}
