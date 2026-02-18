QT += quick quickcontrols2 network
CONFIG += c++17

TARGET = ComponentManager
TEMPLATE = app

DEFINES += QT_DEPRECATED_WARNINGS

INCLUDEPATH += \
    src \
    src/cpp

SOURCES += \
    src/main.cpp \
    src/cpp/AppController.cpp \
    src/cpp/models/ComponentModel.cpp \
    src/cpp/models/CategoryModel.cpp \
    src/cpp/managers/ManifestManager.cpp \
    src/cpp/managers/InstallManager.cpp \
    src/cpp/managers/DownloadManager.cpp \
    src/cpp/managers/SettingsManager.cpp \
    src/cpp/managers/DragDropManager.cpp \
    src/cpp/managers/JsonStorage.cpp \
    src/cpp/services/FileLocations.cpp \
    src/cpp/services/ProcessChecker.cpp \
    src/cpp/services/AppLauncher.cpp

HEADERS += \
    src/cpp/AppController.h \
    src/cpp/data/Component.h \
    src/cpp/data/DownloadProgress.h \
    src/cpp/models/ComponentModel.h \
    src/cpp/models/CategoryModel.h \
    src/cpp/managers/ManifestManager.h \
    src/cpp/managers/InstallManager.h \
    src/cpp/managers/DownloadManager.h \
    src/cpp/managers/SettingsManager.h \
    src/cpp/managers/DragDropManager.h \
    src/cpp/managers/JsonStorage.h \
    src/cpp/services/FileLocations.h \
    src/cpp/services/ProcessChecker.h \
    src/cpp/services/AppLauncher.h

RESOURCES += \
    resources.qrc
