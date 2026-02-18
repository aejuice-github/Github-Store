#ifndef APPLAUNCHER_H
#define APPLAUNCHER_H

#include <QObject>

class AppLauncher : public QObject {
    Q_OBJECT

public:
    explicit AppLauncher(QObject *parent = nullptr);

    Q_INVOKABLE bool launch(const QString &command) const;
    Q_INVOKABLE bool openFolder(const QString &path) const;
    Q_INVOKABLE bool openUrl(const QString &url) const;
};

#endif // APPLAUNCHER_H
