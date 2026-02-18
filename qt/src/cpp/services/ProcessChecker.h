#ifndef PROCESSCHECKER_H
#define PROCESSCHECKER_H

#include <QObject>

class ProcessChecker : public QObject {
    Q_OBJECT

public:
    explicit ProcessChecker(QObject *parent = nullptr);

    Q_INVOKABLE bool isProcessRunning(const QString &processName) const;
    Q_INVOKABLE bool isAfterEffectsRunning() const;
    Q_INVOKABLE bool isPremiereProRunning() const;
};

#endif // PROCESSCHECKER_H
