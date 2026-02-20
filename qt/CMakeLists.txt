cmake_minimum_required(VERSION 3.16)
project(ComponentManager VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTORCC ON)

find_package(Qt6 REQUIRED COMPONENTS
    Core
    Quick
    QuickControls2
    Network
)

set(SOURCES
    src/main.cpp
    src/cpp/AppController.h
    src/cpp/AppController.cpp
    src/cpp/data/Component.h
    src/cpp/data/DownloadProgress.h
    src/cpp/models/ComponentModel.h
    src/cpp/models/ComponentModel.cpp
    src/cpp/models/CategoryModel.h
    src/cpp/models/CategoryModel.cpp
    src/cpp/managers/ManifestManager.h
    src/cpp/managers/ManifestManager.cpp
    src/cpp/managers/InstallManager.h
    src/cpp/managers/InstallManager.cpp
    src/cpp/managers/DownloadManager.h
    src/cpp/managers/DownloadManager.cpp
    src/cpp/managers/SettingsManager.h
    src/cpp/managers/SettingsManager.cpp
    src/cpp/managers/DragDropManager.h
    src/cpp/managers/DragDropManager.cpp
    src/cpp/managers/JsonStorage.h
    src/cpp/managers/JsonStorage.cpp
    src/cpp/services/FileLocations.h
    src/cpp/services/FileLocations.cpp
    src/cpp/services/ProcessChecker.h
    src/cpp/services/ProcessChecker.cpp
    src/cpp/services/AppLauncher.h
    src/cpp/services/AppLauncher.cpp
    src/cpp/services/ElevatedCopyHelper.h
    src/cpp/services/ElevatedCopyHelper.cpp
    src/cpp/NativeDropHandler.h
    src/cpp/NativeDropHandler.cpp
    src/cpp/WindowsDnDFix.h
    src/cpp/WindowsDnDFix.cpp
)

set(RESOURCES
    resources.qrc
)

add_executable(${PROJECT_NAME} WIN32 ${SOURCES} ${RESOURCES})

target_include_directories(${PROJECT_NAME} PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/src
    ${CMAKE_CURRENT_SOURCE_DIR}/src/cpp
)

target_link_libraries(${PROJECT_NAME} PRIVATE
    Qt6::Core
    Qt6::Quick
    Qt6::QuickControls2
    Qt6::Network
)

if(WIN32)
    target_link_libraries(${PROJECT_NAME} PRIVATE shell32 advapi32 ole32)
endif()
