#ifndef FILELOCATIONS_H
#define FILELOCATIONS_H

#include <QObject>
#include <QStringList>

class FileLocations : public QObject {
    Q_OBJECT

public:
    explicit FileLocations(QObject *parent = nullptr);

    Q_INVOKABLE QString mediaCorePath() const;
    Q_INVOKABLE QStringList scriptUIPanelsPaths() const;
    Q_INVOKABLE QString tempDownloadPath() const;
    Q_INVOKABLE QString appDataPath() const;

    static QString platformKey();
    static QString programFilesPath();
    static QString commonFilesPath();
};

#endif // FILELOCATIONS_H
