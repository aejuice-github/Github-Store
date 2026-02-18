import QtQuick 2.15
import QtQuick.Controls 2.15
import "../singletons"
import "../components"

Rectangle {
    id: homeScreen
    color: CMTheme.backgroundColor

    Row {
        anchors.fill: parent

        // Sidebar
        Rectangle {
            id: sidebar
            width: CMTheme.sidebarWidth
            height: parent.height
            color: CMTheme.surfaceColor

            Flickable {
                anchors.fill: parent
                anchors.margins: CMTheme.spacingDefault
                contentHeight: sidebarContent.height
                clip: true
                boundsBehavior: Flickable.StopAtBounds

                Column {
                    id: sidebarContent
                    width: parent.width
                    spacing: CMTheme.spacingDefault

                    // Application filter
                    Text {
                        text: "Application"
                        font.pixelSize: CMTheme.fontSizeSmall
                        font.family: CMTheme.fontFamily
                        color: CMTheme.textMutedColor
                    }

                    AppDropdown {
                        id: appDropdown
                        width: parent.width
                        model: appController.getAvailableApps()
                        onActivated: appController.filterByApp(currentText)

                        Connections {
                            target: appController
                            function onSelectedAppChanged() {
                                var apps = appController.getAvailableApps()
                                for (var i = 0; i < apps.length; i++) {
                                    if (apps[i] === appController.selectedApp) {
                                        appDropdown.currentIndex = i
                                        return
                                    }
                                }
                            }
                        }
                    }

                    // Price filter
                    Rectangle {
                        width: parent.width
                        height: 1
                        color: CMTheme.borderColor
                    }

                    Text {
                        text: "Price"
                        font.pixelSize: CMTheme.fontSizeSmall
                        font.family: CMTheme.fontFamily
                        color: CMTheme.textMutedColor
                    }

                    Column {
                        width: parent.width
                        spacing: 2

                        property string selected: "All"

                        Repeater {
                            model: ["All", "Free", "Paid"]

                            Rectangle {
                                width: parent.width
                                height: 32
                                radius: CMTheme.radiusSmall
                                color: {
                                    if (parent.selected === modelData) return CMTheme.accentColor
                                    if (priceArea.containsMouse) return CMTheme.surfaceContainerHighColor
                                    return "transparent"
                                }

                                Text {
                                    anchors.left: parent.left
                                    anchors.leftMargin: CMTheme.spacingMedium
                                    anchors.verticalCenter: parent.verticalCenter
                                    text: modelData
                                    font.pixelSize: CMTheme.fontSizeDefault
                                    font.family: CMTheme.fontFamily
                                    font.bold: parent.parent.selected === modelData
                                    color: parent.parent.selected === modelData ? "#FFFFFF" : CMTheme.textColor
                                }

                                MouseArea {
                                    id: priceArea
                                    anchors.fill: parent
                                    hoverEnabled: true
                                    cursorShape: Qt.PointingHandCursor
                                    onClicked: {
                                        parent.parent.selected = modelData
                                        appController.filterByPrice(modelData)
                                    }
                                }
                            }
                        }
                    }

                    // Categories
                    Rectangle {
                        width: parent.width
                        height: 1
                        color: CMTheme.borderColor
                    }

                    Text {
                        text: "Categories"
                        font.pixelSize: CMTheme.fontSizeSmall
                        font.family: CMTheme.fontFamily
                        color: CMTheme.textMutedColor
                    }

                    ListView {
                        width: parent.width
                        height: contentHeight
                        model: appController.categoryModel
                        interactive: false
                        spacing: 2

                        delegate: SidebarCategory {
                            name: model.name
                            selected: model.isSelected
                            onClicked: appController.filterByCategory(model.name)
                        }
                    }

                    // Author filter
                    Rectangle {
                        width: parent.width
                        height: 1
                        color: CMTheme.borderColor
                    }

                    Text {
                        text: "Author"
                        font.pixelSize: CMTheme.fontSizeSmall
                        font.family: CMTheme.fontFamily
                        color: CMTheme.textMutedColor
                    }

                    ListView {
                        id: authorList
                        width: parent.width
                        height: contentHeight
                        model: appController.authors
                        interactive: false
                        spacing: 2

                        property string selectedAuthor: "All"

                        delegate: SidebarCategory {
                            name: modelData
                            selected: authorList.selectedAuthor === modelData
                            onClicked: {
                                authorList.selectedAuthor = modelData
                                appController.filterByAuthor(modelData)
                            }
                        }
                    }
                }
            }

            // Right border
            Rectangle {
                anchors.right: parent.right
                anchors.top: parent.top
                anchors.bottom: parent.bottom
                width: 1
                color: CMTheme.borderColor
            }
        }

        // Main content area
        Rectangle {
            width: parent.width - sidebar.width
            height: parent.height
            color: CMTheme.backgroundColor

            Item {
                id: contentArea
                anchors.fill: parent
                anchors.margins: CMTheme.spacingMedium

                // Component grid
                GridView {
                    id: gridView
                    visible: false  // Hidden until card images are ready
                    anchors.fill: parent
                    cellWidth: width / 2
                    cellHeight: CMTheme.cardHeight + CMTheme.spacingMedium
                    model: appController.componentModel
                    clip: true
                    boundsBehavior: Flickable.StopAtBounds
                    flickDeceleration: 3000
                    maximumFlickVelocity: 4000

                    MouseArea {
                        anchors.fill: parent
                        acceptedButtons: Qt.NoButton
                        onWheel: {
                            gridView.contentY = Math.max(0, Math.min(gridView.contentHeight - gridView.height, gridView.contentY - wheel.angleDelta.y * 1.275))
                        }
                    }

                    ScrollBar.vertical: ScrollBar {
                        policy: ScrollBar.AsNeeded
                    }

                    delegate: Item {
                        width: gridView.cellWidth
                        height: gridView.cellHeight

                        ComponentCard {
                            anchors.fill: parent
                            anchors.margins: CMTheme.spacingSmall
                            componentId: model.componentId
                            name: model.name
                            description: model.description
                            author: model.author
                            category: model.category
                            icon: model.icon
                            type: model.type
                            version: model.version
                            price: model.price
                            installed: model.isInstalled
                            updateAvailable: model.isUpdateAvailable
                            searchQuery: root.searchQuery
                            onClicked: {
                                appController.openComponentPage(model.componentId)
                            }
                        }
                    }
                }

                // Component list
                ListView {
                    id: listViewComp
                    visible: true
                    anchors.fill: parent
                    model: appController.componentModel
                    clip: true
                    spacing: CMTheme.spacingSmall
                    boundsBehavior: Flickable.StopAtBounds
                    flickDeceleration: 3000
                    maximumFlickVelocity: 4000

                    MouseArea {
                        anchors.fill: parent
                        acceptedButtons: Qt.NoButton
                        onWheel: {
                            listViewComp.contentY = Math.max(0, Math.min(listViewComp.contentHeight - listViewComp.height, listViewComp.contentY - wheel.angleDelta.y * 1.275))
                        }
                    }

                    ScrollBar.vertical: ScrollBar {
                        policy: ScrollBar.AsNeeded
                    }

                    delegate: Rectangle {
                        width: listViewComp.width
                        height: 72
                        radius: CMTheme.radiusDefault
                        color: listItemArea.containsMouse ? CMTheme.surfaceContainerHighColor : CMTheme.surfaceContainerColor

                        MouseArea {
                            id: listItemArea
                            anchors.fill: parent
                            hoverEnabled: true
                            cursorShape: Qt.PointingHandCursor
                            onClicked: appController.openComponentPage(model.componentId)
                        }

                        Row {
                            anchors.fill: parent
                            anchors.margins: CMTheme.spacingMedium
                            spacing: CMTheme.spacingMedium

                            // Gradient icon
                            Rectangle {
                                id: listIcon
                                width: 48
                                height: 48
                                radius: CMTheme.radiusDefault
                                anchors.verticalCenter: parent.verticalCenter

                                property var grad: {
                                    var h = 0
                                    for (var i = 0; i < model.name.length; i++)
                                        h = ((h << 5) - h + model.name.charCodeAt(i)) | 0
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
                                    GradientStop { position: 0.0; color: listIcon.grad[0] }
                                    GradientStop { position: 1.0; color: listIcon.grad[1] }
                                }

                                Text {
                                    anchors.centerIn: parent
                                    text: model.name.charAt(0).toUpperCase()
                                    font.pixelSize: 20
                                    font.bold: true
                                    font.family: CMTheme.fontFamily
                                    color: "#60FFFFFF"
                                }
                            }

                            // Details column
                            Column {
                                width: parent.width - 48 - actionRow.width - CMTheme.spacingMedium * 3
                                anchors.verticalCenter: parent.verticalCenter
                                spacing: 2

                                Text {
                                    width: parent.width
                                    text: model.name
                                    font.pixelSize: CMTheme.fontSizeMedium
                                    font.bold: true
                                    font.family: CMTheme.fontFamily
                                    color: CMTheme.textColor
                                    elide: Text.ElideRight
                                }

                                Text {
                                    width: parent.width
                                    text: model.description
                                    font.pixelSize: CMTheme.fontSizeDefault
                                    font.family: CMTheme.fontFamily
                                    color: CMTheme.textMutedColor
                                    elide: Text.ElideRight
                                    maximumLineCount: 1
                                }

                                Row {
                                    spacing: CMTheme.spacingDefault

                                    Text {
                                        text: model.author
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
                                        text: "v" + model.version
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
                                        text: model.category
                                        font.pixelSize: CMTheme.fontSizeSmall
                                        font.family: CMTheme.fontFamily
                                        color: CMTheme.textMutedColor
                                    }

                                    Rectangle {
                                        visible: model.price > 0
                                        width: priceLabel.width + CMTheme.spacingDefault
                                        height: 16
                                        radius: CMTheme.radiusSmall
                                        color: CMTheme.accentColor
                                        anchors.verticalCenter: parent.verticalCenter

                                        Text {
                                            id: priceLabel
                                            anchors.centerIn: parent
                                            text: "$" + model.price
                                            font.pixelSize: 9
                                            font.bold: true
                                            font.family: CMTheme.fontFamily
                                            color: CMTheme.backgroundColor
                                        }
                                    }
                                }
                            }

                            // Action buttons
                            Row {
                                id: actionRow
                                z: 1
                                anchors.verticalCenter: parent.verticalCenter
                                spacing: CMTheme.spacingSmall

                                Rectangle {
                                    visible: model.isUpdateAvailable
                                    width: Math.max(100, listUpdateLabel.width + CMTheme.spacingXLarge * 2)
                                    height: 36
                                    radius: CMTheme.radiusDefault
                                    color: listUpdateArea.containsMouse ? CMTheme.accentColor : CMTheme.surfaceContainerHighColor

                                    Text {
                                        id: listUpdateLabel
                                        anchors.centerIn: parent
                                        text: "Update"
                                        font.pixelSize: CMTheme.fontSizeDefault
                                        font.bold: true
                                        font.family: CMTheme.fontFamily
                                        color: listUpdateArea.containsMouse ? "#FFFFFF" : CMTheme.textColor
                                    }

                                    MouseArea {
                                        id: listUpdateArea
                                        anchors.fill: parent
                                        hoverEnabled: true
                                        cursorShape: Qt.PointingHandCursor
                                        onClicked: appController.installComponent(model.componentId)
                                    }
                                }

                                Rectangle {
                                    visible: !model.isInstalled
                                    width: Math.max(100, listInstallLabel.width + CMTheme.spacingXLarge * 2)
                                    height: 36
                                    radius: CMTheme.radiusDefault
                                    color: listInstallArea.containsMouse ? CMTheme.accentColor : CMTheme.surfaceContainerHighColor

                                    Text {
                                        id: listInstallLabel
                                        anchors.centerIn: parent
                                        text: "Install"
                                        font.pixelSize: CMTheme.fontSizeDefault
                                        font.bold: true
                                        font.family: CMTheme.fontFamily
                                        color: listInstallArea.containsMouse ? "#FFFFFF" : CMTheme.textColor
                                    }

                                    MouseArea {
                                        id: listInstallArea
                                        anchors.fill: parent
                                        hoverEnabled: true
                                        cursorShape: Qt.PointingHandCursor
                                        onClicked: appController.installComponent(model.componentId)
                                    }
                                }

                                Rectangle {
                                    visible: model.isInstalled && !model.isUpdateAvailable
                                    width: Math.max(100, installedLabel.width + CMTheme.spacingXLarge * 2)
                                    height: 36
                                    radius: CMTheme.radiusDefault
                                    color: CMTheme.surfaceContainerHighColor

                                    Text {
                                        id: installedLabel
                                        anchors.centerIn: parent
                                        text: "Installed"
                                        font.pixelSize: CMTheme.fontSizeDefault
                                        font.family: CMTheme.fontFamily
                                        color: CMTheme.textMutedColor
                                    }
                                }

                                // Three-dot menu
                                Rectangle {
                                    id: menuBtn
                                    width: 36
                                    height: 36
                                    radius: CMTheme.radiusDefault
                                    color: menuBtnArea.containsMouse ? CMTheme.surfaceContainerHighColor : "transparent"

                                    MaterialIcon {
                                        anchors.centerIn: parent
                                        iconName: "more_vert"
                                        iconSize: 20
                                        iconColor: CMTheme.textMutedColor
                                    }

                                    MouseArea {
                                        id: menuBtnArea
                                        anchors.fill: parent
                                        hoverEnabled: true
                                        cursorShape: Qt.PointingHandCursor
                                        onClicked: {
                                            var pos = menuBtn.mapToItem(contentArea, menuBtn.width, menuBtn.height + 4)
                                            contextPopup.targetComponentId = model.componentId
                                            contextPopup.targetName = model.name
                                            contextPopup.targetInstalled = model.isInstalled
                                            contextPopup.x = pos.x - contextPopup.width
                                            contextPopup.y = pos.y
                                            if (contextPopup.y + contextPopup.implicitHeight > contentArea.height)
                                                contextPopup.y = pos.y - menuBtn.height - 4 - contextPopup.implicitHeight
                                            contextPopup.open()
                                        }
                                    }
                                }
                            }
                        }

                    }
                }

                Popup {
                    id: contextPopup
                    width: 160
                    padding: CMTheme.spacingSmall
                    closePolicy: Popup.CloseOnPressOutside | Popup.CloseOnEscape

                    property string targetComponentId: ""
                    property string targetName: ""
                    property bool targetInstalled: false

                    background: Rectangle {
                        radius: CMTheme.radiusDefault
                        color: CMTheme.surfaceColor
                        border.color: CMTheme.borderColor
                        border.width: 1
                    }

                    Column {
                        width: parent.width

                        Rectangle {
                            visible: contextPopup.targetInstalled
                            width: parent.width
                            height: 32
                            radius: CMTheme.radiusSmall
                            color: popupUninstallArea.containsMouse ? CMTheme.surfaceContainerHighColor : "transparent"

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
                                id: popupUninstallArea
                                anchors.fill: parent
                                hoverEnabled: true
                                cursorShape: Qt.PointingHandCursor
                                onClicked: {
                                    contextPopup.close()
                                    root.confirmUninstall(contextPopup.targetComponentId, contextPopup.targetName)
                                }
                            }
                        }

                        Rectangle {
                            visible: !contextPopup.targetInstalled
                            width: parent.width
                            height: 32
                            radius: CMTheme.radiusSmall
                            color: popupInstallArea.containsMouse ? CMTheme.surfaceContainerHighColor : "transparent"

                            Text {
                                anchors.verticalCenter: parent.verticalCenter
                                anchors.left: parent.left
                                anchors.leftMargin: CMTheme.spacingDefault
                                text: "Install"
                                font.pixelSize: CMTheme.fontSizeDefault
                                font.family: CMTheme.fontFamily
                                color: CMTheme.textColor
                            }

                            MouseArea {
                                id: popupInstallArea
                                anchors.fill: parent
                                hoverEnabled: true
                                cursorShape: Qt.PointingHandCursor
                                onClicked: {
                                    contextPopup.close()
                                    appController.installComponent(contextPopup.targetComponentId)
                                }
                            }
                        }
                    }
                }

                // Empty state (shared)
                Column {
                    anchors.centerIn: parent
                    visible: gridView.count === 0 && !appController.loading
                    spacing: CMTheme.spacingMedium

                    Text {
                        anchors.horizontalCenter: parent.horizontalCenter
                        text: "No apps found"
                        font.pixelSize: CMTheme.fontSizeLarge
                        font.family: CMTheme.fontFamily
                        color: CMTheme.textMutedColor
                    }

                    Rectangle {
                        anchors.horizontalCenter: parent.horizontalCenter
                        width: showAllLabel.width + CMTheme.spacingXLarge * 2
                        height: 36
                        radius: CMTheme.radiusDefault
                        color: showAllArea.containsMouse ? Qt.lighter(CMTheme.accentColor, 1.2) : CMTheme.accentColor

                        Text {
                            id: showAllLabel
                            anchors.centerIn: parent
                            text: "Show All"
                            font.pixelSize: CMTheme.fontSizeDefault
                            font.bold: true
                            font.family: CMTheme.fontFamily
                            color: CMTheme.backgroundColor
                        }

                        MouseArea {
                            id: showAllArea
                            anchors.fill: parent
                            hoverEnabled: true
                            cursorShape: Qt.PointingHandCursor
                            onClicked: {
                                root.clearSearch()
                                appController.filterByCategory("All")
                                appController.filterByApp("All")
                                appController.filterByPrice("All")
                                appController.filterByAuthor("All")
                            }
                        }
                    }
                }
            }

            // Loading indicator
            Rectangle {
                anchors.centerIn: parent
                visible: appController.loading
                width: 200
                height: 60
                radius: CMTheme.radiusDefault
                color: CMTheme.surfaceColor

                Text {
                    anchors.centerIn: parent
                    text: "Loading components..."
                    font.pixelSize: CMTheme.fontSizeMedium
                    font.family: CMTheme.fontFamily
                    color: CMTheme.textColor
                }
            }
        }
    }
}
