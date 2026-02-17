#ifndef DRAGDROPMANAGER_H
#define DRAGDROPMANAGER_H

#include <QObject>
#include <QStringList>

class DragDropManager : public QObject
{
    Q_OBJECT
    Q_PROPERTY(bool isDragActive READ isDragActive NOTIFY isDragActiveChanged)
    Q_PROPERTY(QStringList droppedFiles READ droppedFiles NOTIFY droppedFilesChanged)

public:
    explicit DragDropManager(QObject *parent = nullptr);

    bool isDragActive() const { return m_isDragActive; }
    QStringList droppedFiles() const { return m_droppedFiles; }

    Q_INVOKABLE void setDragActive(bool active);
    Q_INVOKABLE void handleDroppedFiles(const QStringList &files);
    Q_INVOKABLE void clearDroppedFiles();

signals:
    void isDragActiveChanged();
    void droppedFilesChanged();
    void filesDropped(const QStringList &files);
    void installResult(const QString &message, const QString &type, const QStringList &installedTypes);

private:
    void installFiles(const QStringList &files);
    QStringList findInstallPaths(const QString &extension);

    bool m_isDragActive = false;
    QStringList m_droppedFiles;
};

#endif // DRAGDROPMANAGER_H
