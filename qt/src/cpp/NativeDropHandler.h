#ifndef NATIVEDROPHANDLER_H
#define NATIVEDROPHANDLER_H

#include <QObject>
#include <QWindow>
#include "managers/DragDropManager.h"

#ifdef Q_OS_WIN
#include <windows.h>
#include <oleidl.h>
#include <shlobj.h>
#endif

#ifdef Q_OS_WIN
// OLE IDropTarget implementation for native Windows drag-and-drop
class OleDropTarget : public IDropTarget
{
public:
    OleDropTarget(DragDropManager *dragDrop);
    virtual ~OleDropTarget();

    // IUnknown
    HRESULT STDMETHODCALLTYPE QueryInterface(REFIID riid, void **ppvObject) override;
    ULONG STDMETHODCALLTYPE AddRef() override;
    ULONG STDMETHODCALLTYPE Release() override;

    // IDropTarget
    HRESULT STDMETHODCALLTYPE DragEnter(IDataObject *pDataObj, DWORD grfKeyState, POINTL pt, DWORD *pdwEffect) override;
    HRESULT STDMETHODCALLTYPE DragOver(DWORD grfKeyState, POINTL pt, DWORD *pdwEffect) override;
    HRESULT STDMETHODCALLTYPE DragLeave() override;
    HRESULT STDMETHODCALLTYPE Drop(IDataObject *pDataObj, DWORD grfKeyState, POINTL pt, DWORD *pdwEffect) override;

private:
    QStringList extractFiles(IDataObject *pDataObj);
    LONG m_refCount;
    DragDropManager *m_dragDrop;
};
#endif

class NativeDropHandler : public QObject
{
    Q_OBJECT

public:
    explicit NativeDropHandler(DragDropManager *dragDrop, QObject *parent = nullptr);
    ~NativeDropHandler();

    void registerWindow(QWindow *window);

private:
    DragDropManager *m_dragDrop;
#ifdef Q_OS_WIN
    OleDropTarget *m_dropTarget = nullptr;
    HWND m_hwnd = nullptr;
#endif
};

#endif // NATIVEDROPHANDLER_H
