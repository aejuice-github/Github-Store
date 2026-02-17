import QtQuick 2.15
import QtQuick.Controls 2.15
import "singletons"
import "screens"
import "components"

Rectangle {
    id: root
    color: CMTheme.backgroundColor
    width: 1100
    height: 700

    // Bind theme from C++ settings
    Connections {
        target: appController.settings
        function onThemeColorChanged() {
            CMTheme.setTheme(appController.settings.themeColor)
        }
    }

    // Navigation bar
    Rectangle {
        id: navBar
        anchors.left: parent.left
        anchors.right: parent.right
        anchors.top: parent.top
        height: 48
        color: CMTheme.surfaceColor
        z: 10

        Row {
            anchors.fill: parent
            anchors.leftMargin: CMTheme.spacingLarge
            spacing: CMTheme.spacingDefault

            // Back button
            Rectangle {
                width: 32
                height: 32
                anchors.verticalCenter: parent.verticalCenter
                radius: CMTheme.radiusSmall
                color: backArea.containsMouse ? CMTheme.surfaceContainerHighColor : "transparent"
                visible: stackView.depth > 1

                MaterialIcon {
                    anchors.centerIn: parent
                    iconName: "arrow_back"
                    iconSize: 20
                    iconColor: CMTheme.textColor
                }

                MouseArea {
                    id: backArea
                    anchors.fill: parent
                    hoverEnabled: true
                    cursorShape: Qt.PointingHandCursor
                    onClicked: {
                        if (stackView.depth > 1)
                            stackView.pop()
                    }
                }
            }

        }

        // Search field + Action buttons (right side)
        Row {
            anchors.right: parent.right
            anchors.rightMargin: CMTheme.spacingLarge
            anchors.verticalCenter: parent.verticalCenter
            spacing: CMTheme.spacingSmall

            // Inline search field
            Rectangle {
                width: 200
                height: 32
                radius: CMTheme.radiusDefault
                color: CMTheme.surfaceContainerHighColor
                anchors.verticalCenter: parent.verticalCenter

                Row {
                    anchors.fill: parent
                    anchors.leftMargin: CMTheme.spacingDefault
                    anchors.rightMargin: CMTheme.spacingDefault
                    spacing: CMTheme.spacingSmall

                    MaterialIcon {
                        iconName: "search"
                        iconSize: 18
                        iconColor: CMTheme.textMutedColor
                        anchors.verticalCenter: parent.verticalCenter
                    }

                    TextInput {
                        id: navSearchInput
                        width: parent.width - 26
                        anchors.verticalCenter: parent.verticalCenter
                        font.pixelSize: CMTheme.fontSizeDefault
                        font.family: CMTheme.fontFamily
                        color: CMTheme.textColor
                        clip: true
                        selectByMouse: true

                        Text {
                            anchors.verticalCenter: parent.verticalCenter
                            text: "Search..."
                            font.pixelSize: CMTheme.fontSizeDefault
                            font.family: CMTheme.fontFamily
                            color: CMTheme.textMutedColor
                            visible: !navSearchInput.text && !navSearchInput.activeFocus
                        }

                        onTextChanged: appController.search(text)
                    }
                }
            }

            // Favorites
            Rectangle {
                width: 32; height: 32
                radius: CMTheme.radiusSmall
                color: favArea.containsMouse ? CMTheme.surfaceContainerHighColor : "transparent"
                MaterialIcon {
                    anchors.centerIn: parent
                    iconName: "favorite"
                    iconSize: 20
                    iconColor: CMTheme.textColor
                }
                ToolTip.visible: favArea.containsMouse
                ToolTip.text: "Favorites"
                ToolTip.delay: 500
                MouseArea {
                    id: favArea
                    anchors.fill: parent
                    hoverEnabled: true
                    cursorShape: Qt.PointingHandCursor
                    onClicked: { stackView.pop(null); stackView.push(favoritesScreenComp) }
                }
            }

            // Installed Apps
            Rectangle {
                width: 32; height: 32
                radius: CMTheme.radiusSmall
                color: appsArea.containsMouse ? CMTheme.surfaceContainerHighColor : "transparent"
                MaterialIcon {
                    anchors.centerIn: parent
                    iconName: "inventory_2"
                    iconSize: 20
                    iconColor: CMTheme.textColor
                }
                ToolTip.visible: appsArea.containsMouse
                ToolTip.text: "Installed Apps"
                ToolTip.delay: 500
                MouseArea {
                    id: appsArea
                    anchors.fill: parent
                    hoverEnabled: true
                    cursorShape: Qt.PointingHandCursor
                    onClicked: { stackView.pop(null); stackView.push(installedAppsScreenComp) }
                }
            }

            // Manual Install
            Rectangle {
                width: 32; height: 32
                radius: CMTheme.radiusSmall
                color: installArea.containsMouse ? CMTheme.surfaceContainerHighColor : "transparent"
                MaterialIcon {
                    anchors.centerIn: parent
                    iconName: "install_desktop"
                    iconSize: 20
                    iconColor: CMTheme.textColor
                }
                ToolTip.visible: installArea.containsMouse
                ToolTip.text: "Manual Install"
                ToolTip.delay: 500
                MouseArea {
                    id: installArea
                    anchors.fill: parent
                    hoverEnabled: true
                    cursorShape: Qt.PointingHandCursor
                    onClicked: { stackView.pop(null); stackView.push(manualInstallScreenComp) }
                }
            }
        }

        // Bottom border
        Rectangle {
            anchors.left: parent.left
            anchors.right: parent.right
            anchors.bottom: parent.bottom
            height: 1
            color: CMTheme.borderColor
        }
    }

    // Main content
    StackView {
        id: stackView
        anchors.top: navBar.bottom
        anchors.left: parent.left
        anchors.right: parent.right
        anchors.bottom: parent.bottom

        initialItem: HomeScreen {}

        pushEnter: Transition {
            PropertyAnimation { property: "opacity"; from: 0; to: 1; duration: 200 }
        }
        pushExit: Transition {
            PropertyAnimation { property: "opacity"; from: 1; to: 0; duration: 200 }
        }
        popEnter: Transition {
            PropertyAnimation { property: "opacity"; from: 0; to: 1; duration: 200 }
        }
        popExit: Transition {
            PropertyAnimation { property: "opacity"; from: 1; to: 0; duration: 200 }
        }
    }

    // Screen components for navigation
    Component {
        id: searchScreenComp
        SearchScreen {}
    }

    Component {
        id: detailsScreenComp
        DetailsScreen {}
    }

    Component {
        id: settingsScreenComp
        SettingsScreen {}
    }

    Component {
        id: favoritesScreenComp
        FavoritesScreen {}
    }

    Component {
        id: installedAppsScreenComp
        InstalledAppsScreen {}
    }

    Component {
        id: manualInstallScreenComp
        ManualInstallScreen {}
    }

    // Navigation handler from C++
    Connections {
        target: appController
        function onNavigationRequested(screen, params) {
            var comp = null
            switch (screen) {
                case "home": stackView.pop(null); return
                case "search": comp = searchScreenComp; break
                case "details": comp = detailsScreenComp; break
                case "settings": comp = settingsScreenComp; break
                case "favorites": comp = favoritesScreenComp; break
                case "apps": comp = installedAppsScreenComp; break
                case "install": comp = manualInstallScreenComp; break
            }
            if (comp) stackView.push(comp, params || {})
        }
        function onNavigationBackRequested() {
            if (stackView.depth > 1)
                stackView.pop()
        }
        function onToastRequested(message, type) {
            toast.show(message, type)
        }
    }

    // Global drag-drop handler
    DropArea {
        anchors.fill: parent
        keys: ["text/uri-list"]

        onEntered: {
            appController.dragDrop.setDragActive(true)
            drag.accepted = true
        }
        onExited: {
            appController.dragDrop.setDragActive(false)
        }
        onDropped: {
            appController.dragDrop.setDragActive(false)
            var files = []
            for (var i = 0; i < drop.urls.length; i++) {
                var path = drop.urls[i].toString()
                if (path.startsWith("file:///"))
                    path = path.substring(8)
                files.push(path)
            }
            appController.dragDrop.handleDroppedFiles(files)
        }
    }

    // Drop overlay (separate from DropArea to not block popups)
    Rectangle {
        anchors.fill: parent
        color: "#80000000"
        visible: appController.dragDrop.isDragActive
        z: 100

        Rectangle {
            anchors.centerIn: parent
            width: 300
            height: 200
            radius: CMTheme.radiusLarge
            color: CMTheme.surfaceColor
            border.color: CMTheme.accentColor
            border.width: 2

            Column {
                anchors.centerIn: parent
                spacing: CMTheme.spacingMedium

                MaterialIcon {
                    anchors.horizontalCenter: parent.horizontalCenter
                    iconName: "download"
                    iconSize: 48
                    iconColor: CMTheme.accentColor
                }

                Text {
                    anchors.horizontalCenter: parent.horizontalCenter
                    text: "Drop files to install"
                    font.pixelSize: CMTheme.fontSizeLarge
                    font.family: CMTheme.fontFamily
                    color: CMTheme.textColor
                }
            }
        }
    }

    // Toast notification
    Rectangle {
        id: toast
        anchors.horizontalCenter: parent.horizontalCenter
        anchors.bottom: parent.bottom
        anchors.bottomMargin: CMTheme.spacingXLarge
        width: toastText.width + CMTheme.spacingXLarge * 2
        height: 40
        radius: CMTheme.radiusDefault
        color: toastColor
        opacity: 0
        z: 200

        property string toastType: "info"
        property color toastColor: CMTheme.surfaceContainerHighColor

        Text {
            id: toastText
            anchors.centerIn: parent
            text: ""
            font.pixelSize: CMTheme.fontSizeDefault
            font.family: CMTheme.fontFamily
            color: CMTheme.textColor
        }

        function show(message, type) {
            toastText.text = message
            toastType = type || "info"
            switch (toastType) {
                case "error": toastColor = "#93000A"; break
                case "success": toastColor = "#1B5E20"; break
                case "warning": toastColor = "#E65100"; break
                default: toastColor = CMTheme.surfaceContainerHighColor; break
            }
            toastAnimation.restart()
        }

        SequentialAnimation {
            id: toastAnimation
            NumberAnimation { target: toast; property: "opacity"; to: 1; duration: 200 }
            PauseAnimation { duration: 3000 }
            NumberAnimation { target: toast; property: "opacity"; to: 0; duration: 300 }
        }
    }

    // Initial theme sync
    Component.onCompleted: {
        CMTheme.setTheme(appController.settings.themeColor)
        appController.initialize()
    }
}
