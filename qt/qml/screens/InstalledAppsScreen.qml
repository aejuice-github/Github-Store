import QtQuick 2.15
import QtQuick.Controls 2.15
import "../singletons"
import "../components"

Rectangle {
    id: installedScreen
    objectName: "installedAppsScreen"
    color: CMTheme.backgroundColor
    focus: true

    Keys.onPressed: function(event) {
        var maxY = Math.max(0, installedList.contentHeight - installedList.height)
        switch (event.key) {
            case Qt.Key_Down:
                installedList.contentY = Math.min(maxY, installedList.contentY + 72)
                event.accepted = true; break
            case Qt.Key_Up:
                installedList.contentY = Math.max(0, installedList.contentY - 72)
                event.accepted = true; break
            case Qt.Key_PageDown:
                installedList.contentY = Math.min(maxY, installedList.contentY + installedList.height)
                event.accepted = true; break
            case Qt.Key_PageUp:
                installedList.contentY = Math.max(0, installedList.contentY - installedList.height)
                event.accepted = true; break
            case Qt.Key_Home:
                installedList.contentY = 0
                event.accepted = true; break
            case Qt.Key_End:
                installedList.contentY = maxY
                event.accepted = true; break
        }
    }

    property var installedApps: []
    property var filteredApps: []
    property string searchQuery: ""

    Component.onCompleted: {
        installedApps = appController.getInstalledComponents()
        filteredApps = installedApps
    }

    function filterList() {
        if (!searchQuery) {
            filteredApps = installedApps
            return
        }
        var result = []
        var query = searchQuery.toLowerCase()
        for (var i = 0; i < installedApps.length; i++) {
            var item = installedApps[i]
            if (item.name.toLowerCase().indexOf(query) >= 0
                || item.description.toLowerCase().indexOf(query) >= 0
                || item.author.toLowerCase().indexOf(query) >= 0) {
                result.push(item)
            }
        }
        filteredApps = result
    }

    Column {
        anchors.fill: parent
        anchors.margins: CMTheme.spacingLarge
        spacing: CMTheme.spacingMedium

        // Header row with title and search
        Item {
            width: parent.width
            height: 32

            Text {
                text: "Installed Apps"
                font.pixelSize: CMTheme.fontSizeXLarge
                font.bold: true
                font.family: CMTheme.fontFamily
                color: CMTheme.textColor
                anchors.verticalCenter: parent.verticalCenter
            }

            Rectangle {
                id: installedSearchField
                width: 200
                height: 32
                radius: CMTheme.radiusDefault
                color: CMTheme.surfaceContainerHighColor
                anchors.right: parent.right
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
                        width: parent.width - 26
                        anchors.verticalCenter: parent.verticalCenter
                        font.pixelSize: CMTheme.fontSizeDefault
                        font.family: CMTheme.fontFamily
                        color: CMTheme.textColor
                        clip: true
                        selectByMouse: true

                        Text {
                            anchors.verticalCenter: parent.verticalCenter
                            text: "Search installed..."
                            font.pixelSize: CMTheme.fontSizeDefault
                            font.family: CMTheme.fontFamily
                            color: CMTheme.textMutedColor
                            visible: !parent.text && !parent.activeFocus
                        }

                        onTextChanged: {
                            installedScreen.searchQuery = text
                            installedScreen.filterList()
                        }
                    }
                }
            }
        }

        ListView {
            id: installedList
            width: parent.width
            height: parent.height - 50 - keepUpToDateRow.height - CMTheme.spacingMedium
            model: installedScreen.filteredApps
            clip: true
            spacing: CMTheme.spacingSmall
            boundsBehavior: Flickable.StopAtBounds
            flickDeceleration: 3000
            maximumFlickVelocity: 4000

            MouseArea {
                anchors.fill: parent
                acceptedButtons: Qt.NoButton
                onWheel: {
                    installedList.contentY = Math.max(0, Math.min(installedList.contentHeight - installedList.height, installedList.contentY - wheel.angleDelta.y * 1.275))
                }
            }

            ScrollBar.vertical: ScrollBar {
                policy: ScrollBar.AsNeeded
            }

            delegate: Rectangle {
                width: installedList.width
                height: 72
                radius: CMTheme.radiusDefault
                color: installedItemArea.containsMouse ? CMTheme.surfaceContainerHighColor : CMTheme.surfaceContainerColor

                MouseArea {
                    id: installedItemArea
                    anchors.fill: parent
                    hoverEnabled: true
                    cursorShape: Qt.PointingHandCursor
                    onClicked: appController.openComponentPage(modelData.id)
                }

                Row {
                    anchors.fill: parent
                    anchors.margins: CMTheme.spacingMedium
                    spacing: CMTheme.spacingMedium

                    Rectangle {
                        id: installedIcon
                        width: 48
                        height: 48
                        radius: CMTheme.radiusDefault
                        anchors.verticalCenter: parent.verticalCenter

                        property var grad: {
                            var h = 0
                            for (var i = 0; i < modelData.name.length; i++)
                                h = ((h << 5) - h + modelData.name.charCodeAt(i)) | 0
                            h = Math.abs(h)
                            var hue = h % 360
                            var hueShift = 20 + (h >> 8) % 30
                            function hslToHex(hh, s, l) {
                                hh /= 360; s /= 100; l /= 100
                                var r, g, b
                                if (s === 0) { r = g = b = l }
                                else {
                                    function hue2rgb(p, q, t) {
                                        if (t < 0) t += 1; if (t > 1) t -= 1
                                        if (t < 1/6) return p + (q - p) * 6 * t
                                        if (t < 1/2) return q
                                        if (t < 2/3) return p + (q - p) * (2/3 - t) * 6
                                        return p
                                    }
                                    var q = l < 0.5 ? l * (1 + s) : l + s - l * s
                                    var p = 2 * l - q
                                    r = hue2rgb(p, q, hh + 1/3)
                                    g = hue2rgb(p, q, hh)
                                    b = hue2rgb(p, q, hh - 1/3)
                                }
                                var toHex = function(v) { var hex = Math.round(v * 255).toString(16); return hex.length === 1 ? "0" + hex : hex }
                                return "#" + toHex(r) + toHex(g) + toHex(b)
                            }
                            return [hslToHex(hue, 60, 15), hslToHex((hue + hueShift * 2) % 360, 50, 30)]
                        }

                        gradient: Gradient {
                            orientation: Gradient.Horizontal
                            GradientStop { position: 0.0; color: installedIcon.grad[0] }
                            GradientStop { position: 1.0; color: installedIcon.grad[1] }
                        }

                        Text {
                            anchors.centerIn: parent
                            text: modelData.name.charAt(0).toUpperCase()
                            font.pixelSize: 20
                            font.bold: true
                            font.family: CMTheme.fontFamily
                            color: "#60FFFFFF"
                        }
                    }

                    Column {
                        width: parent.width - 48 - installedActionRow.width - CMTheme.spacingMedium * 3
                        anchors.verticalCenter: parent.verticalCenter
                        spacing: 2

                        Text {
                            width: parent.width
                            text: modelData.name
                            font.pixelSize: CMTheme.fontSizeMedium
                            font.bold: true
                            font.family: CMTheme.fontFamily
                            color: CMTheme.textColor
                            elide: Text.ElideRight
                        }

                        Text {
                            width: parent.width
                            text: modelData.description
                            font.pixelSize: CMTheme.fontSizeDefault
                            font.family: CMTheme.fontFamily
                            color: CMTheme.textMutedColor
                            elide: Text.ElideRight
                            maximumLineCount: 1
                        }

                        Row {
                            spacing: CMTheme.spacingDefault

                            Text {
                                text: modelData.author
                                font.pixelSize: CMTheme.fontSizeSmall
                                font.family: CMTheme.fontFamily
                                color: CMTheme.textMutedColor
                            }

                            Text {
                                text: "\u00B7"
                                font.pixelSize: CMTheme.fontSizeSmall
                                color: CMTheme.textMutedColor
                            }

                            Text {
                                text: "v" + modelData.version
                                font.pixelSize: CMTheme.fontSizeSmall
                                font.family: CMTheme.fontFamily
                                color: CMTheme.textMutedColor
                            }
                        }
                    }

                    Row {
                        id: installedActionRow
                        z: 1
                        anchors.verticalCenter: parent.verticalCenter
                        spacing: CMTheme.spacingSmall

                        Rectangle {
                            visible: modelData.isUpdateAvailable || false
                            width: Math.max(100, installedUpdateLabel.width + CMTheme.spacingXLarge * 2)
                            height: 36
                            radius: CMTheme.radiusDefault
                            color: installedUpdateArea.containsMouse ? CMTheme.accentColor : CMTheme.surfaceContainerHighColor

                            Text {
                                id: installedUpdateLabel
                                anchors.centerIn: parent
                                text: "Update"
                                font.pixelSize: CMTheme.fontSizeDefault
                                font.bold: true
                                font.family: CMTheme.fontFamily
                                color: installedUpdateArea.containsMouse ? "#FFFFFF" : CMTheme.textColor
                            }

                            MouseArea {
                                id: installedUpdateArea
                                anchors.fill: parent
                                hoverEnabled: true
                                cursorShape: Qt.PointingHandCursor
                                onClicked: appController.installComponent(modelData.id)
                            }
                        }

                        Rectangle {
                            id: installedMenuBtn
                            width: 36
                            height: 36
                            radius: CMTheme.radiusDefault
                            color: installedMenuBtnArea.containsMouse ? CMTheme.surfaceContainerHighColor : "transparent"

                            MaterialIcon {
                                anchors.centerIn: parent
                                iconName: "more_vert"
                                iconSize: 20
                                iconColor: CMTheme.textMutedColor
                            }

                            MouseArea {
                                id: installedMenuBtnArea
                                anchors.fill: parent
                                hoverEnabled: true
                                cursorShape: Qt.PointingHandCursor
                                onClicked: {
                                    var pos = installedMenuBtn.mapToItem(installedScreen, installedMenuBtn.width, installedMenuBtn.height + 4)
                                    installedContextPopup.targetComponentId = modelData.id
                                    installedContextPopup.targetName = modelData.name
                                    installedContextPopup.x = pos.x - installedContextPopup.width
                                    installedContextPopup.y = pos.y
                                    if (installedContextPopup.y + installedContextPopup.implicitHeight > installedScreen.height)
                                        installedContextPopup.y = pos.y - installedMenuBtn.height - 4 - installedContextPopup.implicitHeight
                                    installedContextPopup.open()
                                }
                            }
                        }
                    }
                }
            }

            // Empty state
            Text {
                anchors.centerIn: parent
                visible: installedList.count === 0
                text: installedScreen.searchQuery ? "No matches" : "No installed apps yet"
                font.pixelSize: CMTheme.fontSizeLarge
                font.family: CMTheme.fontFamily
                color: CMTheme.textMutedColor
            }
        }

        // Keep up to date checkbox
        CheckBox {
            id: keepUpToDateRow
            checked: appController.settings.keepUpToDate
            onCheckedChanged: appController.settings.keepUpToDate = checked
            text: "Keep up to date"
            font.pixelSize: CMTheme.fontSizeDefault
            font.family: CMTheme.fontFamily

            indicator: Rectangle {
                width: 16
                height: 16
                x: keepUpToDateRow.leftPadding
                y: parent.height / 2 - height / 2
                radius: 3
                color: keepUpToDateRow.checked ? CMTheme.accentColor : "transparent"
                border.color: keepUpToDateRow.checked ? CMTheme.accentColor : CMTheme.textMutedColor
                border.width: 1

                MaterialIcon {
                    anchors.centerIn: parent
                    visible: keepUpToDateRow.checked
                    iconName: "check"
                    iconSize: 12
                    iconColor: "#FFFFFF"
                }
            }

            contentItem: Text {
                text: keepUpToDateRow.text
                font: keepUpToDateRow.font
                color: CMTheme.textColor
                verticalAlignment: Text.AlignVCenter
                leftPadding: keepUpToDateRow.indicator.width + keepUpToDateRow.spacing
            }
        }
    }

    // Uninstall All button
    Rectangle {
        visible: installedScreen.installedApps.length > 0
        width: uninstallAllRow.width + CMTheme.spacingLarge
        height: 28
        radius: CMTheme.radiusDefault
        color: uninstallAllArea.containsMouse ? "#93000A" : CMTheme.surfaceContainerHighColor
        anchors.right: parent.right
        anchors.bottom: parent.bottom
        anchors.rightMargin: CMTheme.spacingLarge
        anchors.bottomMargin: CMTheme.spacingLarge

        Row {
            id: uninstallAllRow
            anchors.centerIn: parent
            spacing: CMTheme.spacingSmall

            MaterialIcon {
                iconName: "delete"
                iconSize: 16
                iconColor: uninstallAllArea.containsMouse ? "#FFFFFF" : CMTheme.textMutedColor
                anchors.verticalCenter: parent.verticalCenter
            }

            Text {
                text: "Uninstall All"
                font.pixelSize: CMTheme.fontSizeSmall
                font.bold: true
                font.family: CMTheme.fontFamily
                color: uninstallAllArea.containsMouse ? "#FFFFFF" : CMTheme.textMutedColor
                anchors.verticalCenter: parent.verticalCenter
            }
        }

        MouseArea {
            id: uninstallAllArea
            anchors.fill: parent
            hoverEnabled: true
            cursorShape: Qt.PointingHandCursor
            onClicked: uninstallAllDialog.open()
        }
    }

    // Uninstall All confirmation
    Rectangle {
        anchors.fill: parent
        color: "#80000000"
        visible: uninstallAllDialog.opened
        z: 299
    }

    Popup {
        id: uninstallAllDialog
        anchors.centerIn: parent
        width: 360
        height: 160
        modal: true
        closePolicy: Popup.CloseOnEscape | Popup.CloseOnPressOutside
        z: 300

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
                text: "Uninstall all " + installedScreen.installedApps.length + " apps?"
                font.pixelSize: CMTheme.fontSizeLarge
                font.bold: true
                font.family: CMTheme.fontFamily
                color: CMTheme.textColor
                width: parent.width
                wrapMode: Text.WordWrap
            }

            Text {
                text: "This will remove all installed components."
                font.pixelSize: CMTheme.fontSizeDefault
                font.family: CMTheme.fontFamily
                color: CMTheme.textMutedColor
            }

            Row {
                anchors.right: parent.right
                spacing: CMTheme.spacingDefault

                Rectangle {
                    width: cancelAllText.width + CMTheme.spacingLarge * 2
                    height: 32
                    radius: CMTheme.radiusDefault
                    color: cancelAllArea.containsMouse ? CMTheme.surfaceContainerHighColor : "transparent"

                    Text {
                        id: cancelAllText
                        anchors.centerIn: parent
                        text: "Cancel"
                        font.pixelSize: CMTheme.fontSizeDefault
                        font.family: CMTheme.fontFamily
                        color: CMTheme.textColor
                    }

                    MouseArea {
                        id: cancelAllArea
                        anchors.fill: parent
                        hoverEnabled: true
                        cursorShape: Qt.PointingHandCursor
                        onClicked: uninstallAllDialog.close()
                    }
                }

                Rectangle {
                    width: confirmAllText.width + CMTheme.spacingLarge * 2
                    height: 32
                    radius: CMTheme.radiusDefault
                    color: confirmAllArea.containsMouse ? "#B71C1C" : "#93000A"

                    Text {
                        id: confirmAllText
                        anchors.centerIn: parent
                        text: "Uninstall All"
                        font.pixelSize: CMTheme.fontSizeDefault
                        font.bold: true
                        font.family: CMTheme.fontFamily
                        color: "#FFFFFF"
                    }

                    MouseArea {
                        id: confirmAllArea
                        anchors.fill: parent
                        hoverEnabled: true
                        cursorShape: Qt.PointingHandCursor
                        onClicked: {
                            for (var i = 0; i < installedScreen.installedApps.length; i++)
                                appController.uninstallComponent(installedScreen.installedApps[i].id)
                            uninstallAllDialog.close()
                            installedApps = appController.getInstalledComponents()
                            filteredApps = installedApps
                        }
                    }
                }
            }
        }
    }

    Popup {
        id: installedContextPopup
        width: 160
        padding: CMTheme.spacingSmall
        closePolicy: Popup.CloseOnPressOutside | Popup.CloseOnEscape

        property string targetComponentId: ""
        property string targetName: ""

        background: Rectangle {
            radius: CMTheme.radiusDefault
            color: CMTheme.surfaceColor
            border.color: CMTheme.borderColor
            border.width: 1
        }

        Column {
            width: parent.width

            Rectangle {
                width: parent.width
                height: 32
                radius: CMTheme.radiusSmall
                color: popupReinstallArea2.containsMouse ? CMTheme.surfaceContainerHighColor : "transparent"

                Text {
                    anchors.verticalCenter: parent.verticalCenter
                    anchors.left: parent.left
                    anchors.leftMargin: CMTheme.spacingDefault
                    text: "Reinstall"
                    font.pixelSize: CMTheme.fontSizeDefault
                    font.family: CMTheme.fontFamily
                    color: CMTheme.textColor
                }

                MouseArea {
                    id: popupReinstallArea2
                    anchors.fill: parent
                    hoverEnabled: true
                    cursorShape: Qt.PointingHandCursor
                    onClicked: {
                        installedContextPopup.close()
                        appController.installComponent(installedContextPopup.targetComponentId)
                    }
                }
            }

            Rectangle {
                width: parent.width
                height: 32
                radius: CMTheme.radiusSmall
                color: popupUninstallArea2.containsMouse ? CMTheme.surfaceContainerHighColor : "transparent"

                Text {
                    anchors.verticalCenter: parent.verticalCenter
                    anchors.left: parent.left
                    anchors.leftMargin: CMTheme.spacingDefault
                    text: "Uninstall"
                    font.pixelSize: CMTheme.fontSizeDefault
                    font.family: CMTheme.fontFamily
                    color: "#F44336"
                }

                MouseArea {
                    id: popupUninstallArea2
                    anchors.fill: parent
                    hoverEnabled: true
                    cursorShape: Qt.PointingHandCursor
                    onClicked: {
                        installedContextPopup.close()
                        appController.uninstallComponent(installedContextPopup.targetComponentId)
                        installedApps = appController.getInstalledComponents()
                        filteredApps = installedApps
                    }
                }
            }
        }
    }
}
