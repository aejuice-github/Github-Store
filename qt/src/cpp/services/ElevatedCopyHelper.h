#ifndef ELEVATEDCOPYHELPER_H
#define ELEVATEDCOPYHELPER_H

#include <QObject>
#include <QStringList>

#ifdef Q_OS_WIN
#include <windows.h>
#endif

// Launches a single elevated helper process on first use, keeps it alive
// via a named pipe, and sends copy commands without additional UAC prompts
class ElevatedCopyHelper : public QObject
{
    Q_OBJECT

public:
    explicit ElevatedCopyHelper(QObject *parent = nullptr);
    ~ElevatedCopyHelper();

    bool copyFiles(const QString &source, const QStringList &destinations);

    // Pipe helper entry point (runs elevated, no GUI)
    static int runHelper(const QString &pipeName);

private:
    bool launchHelper();
    bool sendCommand(const QString &command);
    QString readResponse();
    void shutdown();
    bool isHelperAlive() const;

#ifdef Q_OS_WIN
    HANDLE m_pipe = INVALID_HANDLE_VALUE;
    HANDLE m_helperProcess = nullptr;
#endif
    bool m_running = false;
    QString m_pipeName;
};

#endif // ELEVATEDCOPYHELPER_H
