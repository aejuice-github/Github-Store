#ifndef DOWNLOADPROGRESS_H
#define DOWNLOADPROGRESS_H

#include <QString>

struct DownloadProgress {
    QString componentId;
    qint64 bytesReceived = 0;
    qint64 bytesTotal = 0;
    double percentage = 0.0;
    QString status; // downloading, verifying, installing, completed, error
    QString errorMessage;
};

#endif // DOWNLOADPROGRESS_H
