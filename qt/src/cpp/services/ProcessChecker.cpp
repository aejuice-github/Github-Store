#include "ProcessChecker.h"
#include <QProcess>

ProcessChecker::ProcessChecker(QObject *parent)
    : QObject(parent) {
}

bool ProcessChecker::isProcessRunning(const QString &processName) const {
    QProcess process;
#ifdef Q_OS_WIN
    process.start("tasklist", {"/FI", "IMAGENAME eq " + processName, "/NH"});
#else
    process.start("pgrep", {"-x", processName});
#endif
    process.waitForFinished(3000);
    QString output = process.readAllStandardOutput();
    return output.contains(processName, Qt::CaseInsensitive);
}

bool ProcessChecker::isAfterEffectsRunning() const {
#ifdef Q_OS_WIN
    return isProcessRunning("AfterFX.exe");
#else
    return isProcessRunning("After Effects");
#endif
}

bool ProcessChecker::isPremiereProRunning() const {
#ifdef Q_OS_WIN
    return isProcessRunning("Adobe Premiere Pro.exe");
#else
    return isProcessRunning("Adobe Premiere Pro");
#endif
}
