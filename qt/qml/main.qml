import QtQuick 2.15
import QtQuick.Controls 2.15
import QtQuick.Window 2.15
import "singletons"
import "screens"
import "components"

ApplicationWindow {
    id: root
    visible: true
    width: 1100
    height: 700
    minimumWidth: 800
    minimumHeight: 500
    title: "AEJuice App Store"
    color: CMTheme.backgroundColor

    property string searchQuery: navSearchInput.text

    function clearSearch() {
        navSearchInput.text = ""
        navSearchInput.focus = false
    }

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

        // Back button (left)
        Rectangle {
            width: 32
            height: 32
            anchors.left: parent.left
            anchors.leftMargin: CMTheme.spacingLarge
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

        // Search field (left-aligned)
        Rectangle {
            width: Math.min(500, parent.width - 250)
            height: 32
            radius: CMTheme.radiusDefault
            color: CMTheme.surfaceContainerHighColor
            anchors.centerIn: parent

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
                    width: parent.width - 26 - (clearBtn.visible ? clearBtn.width + CMTheme.spacingSmall : 0)
                    anchors.verticalCenter: parent.verticalCenter
                    font.pixelSize: CMTheme.fontSizeDefault
                    font.family: CMTheme.fontFamily
                    color: CMTheme.textColor
                    clip: true
                    focus: false
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

                MaterialIcon {
                    id: clearBtn
                    visible: navSearchInput.text.length > 0
                    iconName: "close"
                    iconSize: 16
                    iconColor: clearArea.containsMouse ? CMTheme.textColor : CMTheme.textMutedColor
                    anchors.verticalCenter: parent.verticalCenter

                    MouseArea {
                        id: clearArea
                        anchors.fill: parent
                        anchors.margins: -4
                        hoverEnabled: true
                        cursorShape: Qt.PointingHandCursor
                        onClicked: { navSearchInput.text = ""; navSearchInput.focus = false }
                    }
                }
            }
        }

        // Action buttons (right)
        Row {
            anchors.right: parent.right
            anchors.rightMargin: CMTheme.spacingLarge
            anchors.verticalCenter: parent.verticalCenter
            spacing: CMTheme.spacingSmall

            // Favorites
            Rectangle {
                property bool active: stackView.currentItem && stackView.currentItem.objectName === "favoritesScreen"
                width: 32; height: 32
                radius: CMTheme.radiusSmall
                color: active ? CMTheme.surfaceContainerHighColor : favArea.containsMouse ? CMTheme.surfaceContainerHighColor : "transparent"
                MaterialIcon {
                    anchors.centerIn: parent
                    iconName: "favorite"
                    iconSize: 20
                    iconColor: parent.active ? CMTheme.accentColor : CMTheme.textColor
                }
                ToolTip.visible: favArea.containsMouse
                ToolTip.text: "Favorites"
                ToolTip.delay: 500
                MouseArea {
                    id: favArea
                    anchors.fill: parent
                    hoverEnabled: true
                    cursorShape: Qt.PointingHandCursor
                    onClicked: {
                        if (stackView.currentItem && stackView.currentItem.objectName === "favoritesScreen")
                            stackView.pop(null)
                        else
                            { stackView.pop(null, StackView.Immediate); stackView.push(favoritesScreenComp) }
                    }
                }
            }

            // Installed Apps
            Rectangle {
                property bool active: stackView.currentItem && stackView.currentItem.objectName === "installedAppsScreen"
                width: 32; height: 32
                radius: CMTheme.radiusSmall
                color: active ? CMTheme.surfaceContainerHighColor : appsArea.containsMouse ? CMTheme.surfaceContainerHighColor : "transparent"
                MaterialIcon {
                    anchors.centerIn: parent
                    iconName: "download"
                    iconSize: 20
                    iconColor: parent.active ? CMTheme.accentColor : CMTheme.textColor
                }
                ToolTip.visible: appsArea.containsMouse
                ToolTip.text: "Installed Apps"
                ToolTip.delay: 500
                MouseArea {
                    id: appsArea
                    anchors.fill: parent
                    hoverEnabled: true
                    cursorShape: Qt.PointingHandCursor
                    onClicked: {
                        if (stackView.currentItem && stackView.currentItem.objectName === "installedAppsScreen")
                            stackView.pop(null)
                        else
                            { stackView.pop(null, StackView.Immediate); stackView.push(installedAppsScreenComp) }
                    }
                }
            }

            // Manual Install
            Rectangle {
                property bool active: stackView.currentItem && stackView.currentItem.objectName === "manualInstallScreen"
                width: 32; height: 32
                radius: CMTheme.radiusSmall
                color: active ? CMTheme.surfaceContainerHighColor : installArea.containsMouse ? CMTheme.surfaceContainerHighColor : "transparent"
                MaterialIcon {
                    anchors.centerIn: parent
                    iconName: "install_desktop"
                    iconSize: 20
                    iconColor: parent.active ? CMTheme.accentColor : CMTheme.textColor
                }
                ToolTip.visible: installArea.containsMouse
                ToolTip.text: "Manual Install"
                ToolTip.delay: 500
                MouseArea {
                    id: installArea
                    anchors.fill: parent
                    hoverEnabled: true
                    cursorShape: Qt.PointingHandCursor
                    onClicked: {
                        if (stackView.currentItem && stackView.currentItem.objectName === "manualInstallScreen")
                            stackView.pop(null)
                        else
                            { stackView.pop(null, StackView.Immediate); stackView.push(manualInstallScreenComp) }
                    }
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
            PropertyAnimation { property: "opacity"; from: 0; to: 1; duration: 100 }
        }
        pushExit: Transition {
            PropertyAnimation { property: "opacity"; from: 1; to: 0; duration: 100 }
        }
        popEnter: Transition {
            PropertyAnimation { property: "opacity"; from: 0; to: 1; duration: 100 }
        }
        popExit: Transition {
            PropertyAnimation { property: "opacity"; from: 1; to: 0; duration: 100 }
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

    Component {
        id: installSuccessScreenComp
        InstallSuccessScreen {}
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
                case "installSuccess": comp = installSuccessScreenComp; break
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

    // Drop overlay (native OLE drop target handles the actual drag-drop)
    Rectangle {
        anchors.fill: parent
        color: "#80000000"
        visible: appController.dragDrop.isDragActive
        z: 200

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
        width: Math.min(root.width - CMTheme.spacingXLarge * 2, toastText.contentWidth + CMTheme.spacingXLarge * 2)
        height: 40
        radius: CMTheme.radiusDefault
        color: toastColor
        opacity: 0
        z: 200

        property string toastType: "info"
        property color toastColor: CMTheme.surfaceContainerHighColor

        TextEdit {
            id: toastText
            anchors.centerIn: parent
            text: ""
            font.pixelSize: CMTheme.fontSizeDefault
            font.family: CMTheme.fontFamily
            color: CMTheme.textColor
            readOnly: true
            selectByMouse: true
            selectedTextColor: CMTheme.backgroundColor
            selectionColor: CMTheme.accentColor
            cursorVisible: false
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
            PauseAnimation { duration: 7000 }
            NumberAnimation { target: toast; property: "opacity"; to: 0; duration: 300 }
        }
    }

    // Uninstall confirmation dialog
    function confirmUninstall(componentId, componentName) {
        uninstallDialog.componentId = componentId
        uninstallDialog.componentName = componentName
        uninstallDialog.open()
    }

    Popup {
        id: uninstallDialog
        anchors.centerIn: parent
        width: 360
        height: 160
        modal: true
        closePolicy: Popup.CloseOnEscape | Popup.CloseOnPressOutside
        z: 300

        property string componentId: ""
        property string componentName: ""

        background: Rectangle {
            radius: CMTheme.radiusLarge
            color: CMTheme.surfaceColor
            border.color: CMTheme.borderColor
            border.width: 1
        }

        Column {
            anchors.fill: parent
            anchors.margins: CMTheme.spacingLarge
            spacing: CMTheme.spacingMedium

            Text {
                text: "Uninstall " + uninstallDialog.componentName + "?"
                font.pixelSize: CMTheme.fontSizeLarge
                font.bold: true
                font.family: CMTheme.fontFamily
                color: CMTheme.textColor
                width: parent.width
                wrapMode: Text.WordWrap
            }

            Text {
                text: "This will remove it from your installed list."
                font.pixelSize: CMTheme.fontSizeDefault
                font.family: CMTheme.fontFamily
                color: CMTheme.textMutedColor
            }

            Row {
                anchors.right: parent.right
                spacing: CMTheme.spacingDefault

                Rectangle {
                    width: cancelText.width + CMTheme.spacingLarge * 2
                    height: 32
                    radius: CMTheme.radiusDefault
                    color: cancelArea.containsMouse ? CMTheme.surfaceContainerHighColor : "transparent"

                    Text {
                        id: cancelText
                        anchors.centerIn: parent
                        text: "Cancel"
                        font.pixelSize: CMTheme.fontSizeDefault
                        font.family: CMTheme.fontFamily
                        color: CMTheme.textColor
                    }

                    MouseArea {
                        id: cancelArea
                        anchors.fill: parent
                        hoverEnabled: true
                        cursorShape: Qt.PointingHandCursor
                        onClicked: uninstallDialog.close()
                    }
                }

                Rectangle {
                    width: confirmText.width + CMTheme.spacingLarge * 2
                    height: 32
                    radius: CMTheme.radiusDefault
                    color: confirmArea.containsMouse ? "#B71C1C" : "#93000A"

                    Text {
                        id: confirmText
                        anchors.centerIn: parent
                        text: "Uninstall"
                        font.pixelSize: CMTheme.fontSizeDefault
                        font.bold: true
                        font.family: CMTheme.fontFamily
                        color: "#FFFFFF"
                    }

                    MouseArea {
                        id: confirmArea
                        anchors.fill: parent
                        hoverEnabled: true
                        cursorShape: Qt.PointingHandCursor
                        onClicked: {
                            appController.uninstallComponent(uninstallDialog.componentId)
                            uninstallDialog.close()
                        }
                    }
                }
            }
        }
    }

    // Dim overlay when dialog is open
    Rectangle {
        anchors.fill: parent
        color: "#80000000"
        visible: uninstallDialog.opened
        z: 299
    }

    // Initial theme sync and window sizing
    Timer {
        id: positionTimer
        interval: 50
        onTriggered: {
            var availW = Screen.desktopAvailableWidth
            var availH = Screen.desktopAvailableHeight
            var topMargin = 60   // Reserve space for title bar
            var bottomMargin = 10 // Gap above taskbar for resize handle
            var totalMargin = topMargin + bottomMargin
            if (root.width > availW) root.width = availW
            if (root.height > availH - totalMargin) root.height = availH - totalMargin
            root.x = Math.max(0, (availW - root.width) / 2)
            root.y = Math.max(0, (availH - root.height - totalMargin) / 2 + topMargin)
        }
    }

    Component.onCompleted: {
        CMTheme.setTheme(appController.settings.themeColor)
        positionTimer.start()
        appController.initialize()
    }
}
