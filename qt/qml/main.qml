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

        // Search field (centered)
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

        // Action buttons (right)
        Row {
            anchors.right: parent.right
            anchors.rightMargin: CMTheme.spacingLarge
            anchors.verticalCenter: parent.verticalCenter
            spacing: CMTheme.spacingSmall

            // Update All
            Rectangle {
                visible: appController.updatesAvailableCount > 0
                width: updateAllRow.width + CMTheme.spacingLarge
                height: 28
                radius: CMTheme.radiusDefault
                color: updateAllArea.containsMouse ? "#C68400" : "#F9A825"
                anchors.verticalCenter: parent.verticalCenter

                Row {
                    id: updateAllRow
                    anchors.centerIn: parent
                    spacing: CMTheme.spacingSmall

                    MaterialIcon {
                        iconName: "update"
                        iconSize: 16
                        iconColor: "#000000"
                        anchors.verticalCenter: parent.verticalCenter
                    }

                    Text {
                        text: "Update All (" + appController.updatesAvailableCount + ")"
                        font.pixelSize: CMTheme.fontSizeSmall
                        font.bold: true
                        font.family: CMTheme.fontFamily
                        color: "#000000"
                        anchors.verticalCenter: parent.verticalCenter
                    }
                }

                MouseArea {
                    id: updateAllArea
                    anchors.fill: parent
                    hoverEnabled: true
                    cursorShape: Qt.PointingHandCursor
                    onClicked: appController.updateAllComponents()
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

    // App update banner
    Rectangle {
        id: updateBanner
        anchors.left: parent.left
        anchors.right: parent.right
        anchors.top: navBar.bottom
        height: appController.appUpdateAvailable ? 36 : 0
        visible: appController.appUpdateAvailable
        color: "#E65100"
        z: 9

        Row {
            anchors.centerIn: parent
            spacing: CMTheme.spacingDefault

            MaterialIcon {
                iconName: "system_update"
                iconSize: 18
                iconColor: "#FFFFFF"
                anchors.verticalCenter: parent.verticalCenter
            }

            Text {
                text: "Version " + appController.appUpdateVersion + " is available"
                font.pixelSize: CMTheme.fontSizeDefault
                font.family: CMTheme.fontFamily
                color: "#FFFFFF"
                anchors.verticalCenter: parent.verticalCenter
            }

            Rectangle {
                width: updateBtnText.width + CMTheme.spacingLarge
                height: 24
                radius: CMTheme.radiusSmall
                color: updateBtnArea.containsMouse ? "#FFFFFF" : "transparent"
                border.color: "#FFFFFF"
                border.width: 1
                anchors.verticalCenter: parent.verticalCenter

                Text {
                    id: updateBtnText
                    anchors.centerIn: parent
                    text: "Update"
                    font.pixelSize: CMTheme.fontSizeSmall
                    font.bold: true
                    font.family: CMTheme.fontFamily
                    color: updateBtnArea.containsMouse ? "#E65100" : "#FFFFFF"
                }

                MouseArea {
                    id: updateBtnArea
                    anchors.fill: parent
                    hoverEnabled: true
                    cursorShape: Qt.PointingHandCursor
                    onClicked: appController.updateApp()
                }
            }
        }
    }

    // Main content
    StackView {
        id: stackView
        anchors.top: updateBanner.bottom
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

    // Initial theme sync
    Component.onCompleted: {
        CMTheme.setTheme(appController.settings.themeColor)
        appController.initialize()
    }
}
