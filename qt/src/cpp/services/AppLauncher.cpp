#include "AppLauncher.h"
#include <QProcess>
#include <QDesktopServices>
#include <QUrl>

AppLauncher::AppLauncher(QObject *parent)
    : QObject(parent) {
}

bool AppLauncher::launch(const QString &command) const {
    return QProcess::startDetached(command, {});
}

bool AppLauncher::openFolder(const QString &path) const {
    return QDesktopServices::openUrl(QUrl::fromLocalFile(path));
}

bool AppLauncher::openUrl(const QString &url) const {
    return QDesktopServices::openUrl(QUrl(url));
}
