#include "NativeDropHandler.h"
#include <QDebug>

NativeDropHandler::NativeDropHandler(DragDropManager *dragDrop, QObject *parent)
    : QObject(parent)
    , m_dragDrop(dragDrop)
{
#ifdef Q_OS_WIN
    OleInitialize(nullptr);
#endif
}

NativeDropHandler::~NativeDropHandler()
{
#ifdef Q_OS_WIN
    if (m_hwnd && m_dropTarget) {
        RevokeDragDrop(m_hwnd);
    }
    if (m_dropTarget) {
        m_dropTarget->Release();
    }
    OleUninitialize();
#endif
}

void NativeDropHandler::registerWindow(QWindow *window)
{
#ifdef Q_OS_WIN
    if (!window) {
        qDebug() << "NativeDropHandler: window is null";
        return;
    }

    m_hwnd = reinterpret_cast<HWND>(window->winId());
    m_dropTarget = new OleDropTarget(m_dragDrop);

    // Revoke any existing drop target Qt may have registered
    RevokeDragDrop(m_hwnd);

    HRESULT hr = RegisterDragDrop(m_hwnd, m_dropTarget);
    if (SUCCEEDED(hr)) {
        qDebug() << "NativeDropHandler: OLE drop target registered on HWND" << m_hwnd;
    } else {
        qDebug() << "NativeDropHandler: RegisterDragDrop failed with HRESULT" << hr;
    }
#else
    Q_UNUSED(window)
#endif
}

#ifdef Q_OS_WIN

// --- OleDropTarget implementation ---

OleDropTarget::OleDropTarget(DragDropManager *dragDrop)
    : m_refCount(1)
    , m_dragDrop(dragDrop)
{
}

OleDropTarget::~OleDropTarget()
{
}

HRESULT STDMETHODCALLTYPE OleDropTarget::QueryInterface(REFIID riid, void **ppvObject)
{
    if (riid == IID_IUnknown || riid == IID_IDropTarget) {
        *ppvObject = static_cast<IDropTarget *>(this);
        AddRef();
        return S_OK;
    }
    *ppvObject = nullptr;
    return E_NOINTERFACE;
}

ULONG STDMETHODCALLTYPE OleDropTarget::AddRef()
{
    return InterlockedIncrement(&m_refCount);
}

ULONG STDMETHODCALLTYPE OleDropTarget::Release()
{
    LONG count = InterlockedDecrement(&m_refCount);
    if (count == 0) {
        delete this;
    }
    return count;
}

HRESULT STDMETHODCALLTYPE OleDropTarget::DragEnter(IDataObject *pDataObj, DWORD grfKeyState, POINTL pt, DWORD *pdwEffect)
{
    Q_UNUSED(grfKeyState)
    Q_UNUSED(pt)

    qDebug() << "OleDropTarget: DragEnter";
    m_dragDrop->setDragActive(true);
    *pdwEffect = DROPEFFECT_COPY;
    return S_OK;
}

HRESULT STDMETHODCALLTYPE OleDropTarget::DragOver(DWORD grfKeyState, POINTL pt, DWORD *pdwEffect)
{
    Q_UNUSED(grfKeyState)
    Q_UNUSED(pt)

    *pdwEffect = DROPEFFECT_COPY;
    return S_OK;
}

HRESULT STDMETHODCALLTYPE OleDropTarget::DragLeave()
{
    qDebug() << "OleDropTarget: DragLeave";
    m_dragDrop->setDragActive(false);
    return S_OK;
}

HRESULT STDMETHODCALLTYPE OleDropTarget::Drop(IDataObject *pDataObj, DWORD grfKeyState, POINTL pt, DWORD *pdwEffect)
{
    Q_UNUSED(grfKeyState)
    Q_UNUSED(pt)

    qDebug() << "OleDropTarget: Drop";
    m_dragDrop->setDragActive(false);

    QStringList files = extractFiles(pDataObj);
    qDebug() << "OleDropTarget: files:" << files;

    if (!files.isEmpty()) {
        m_dragDrop->handleDroppedFiles(files);
    }

    *pdwEffect = DROPEFFECT_COPY;
    return S_OK;
}

QStringList OleDropTarget::extractFiles(IDataObject *pDataObj)
{
    QStringList files;

    FORMATETC fmt;
    fmt.cfFormat = CF_HDROP;
    fmt.ptd = nullptr;
    fmt.dwAspect = DVASPECT_CONTENT;
    fmt.lindex = -1;
    fmt.tymed = TYMED_HGLOBAL;

    STGMEDIUM stg;
    if (SUCCEEDED(pDataObj->GetData(&fmt, &stg))) {
        HDROP hDrop = static_cast<HDROP>(stg.hGlobal);
        UINT fileCount = DragQueryFileW(hDrop, 0xFFFFFFFF, nullptr, 0);

        for (UINT i = 0; i < fileCount; i++) {
            UINT pathLen = DragQueryFileW(hDrop, i, nullptr, 0) + 1;
            QVector<wchar_t> buffer(pathLen);
            DragQueryFileW(hDrop, i, buffer.data(), pathLen);
            files.append(QString::fromWCharArray(buffer.data()));
        }

        ReleaseStgMedium(&stg);
    }

    return files;
}

#endif
