import QtQuick 2.15
import QtQuick.Controls 2.15
import "../singletons"
import "../components"

Rectangle {
    id: favoritesScreen
    objectName: "favoritesScreen"
    color: CMTheme.backgroundColor

    property var favorites: []
    property var filteredFavorites: []
    property string searchQuery: ""

    Component.onCompleted: {
        favorites = appController.getFavoriteComponents()
        filteredFavorites = favorites
    }

    function filterList() {
        if (!searchQuery) {
            filteredFavorites = favorites
            return
        }
        var result = []
        var query = searchQuery.toLowerCase()
        for (var i = 0; i < favorites.length; i++) {
            var item = favorites[i]
            if (item.name.toLowerCase().indexOf(query) >= 0
                || item.description.toLowerCase().indexOf(query) >= 0
                || item.author.toLowerCase().indexOf(query) >= 0) {
                result.push(item)
            }
        }
        filteredFavorites = result
    }

    Column {
        anchors.fill: parent
        anchors.margins: CMTheme.spacingLarge
        spacing: CMTheme.spacingMedium

        // Header row with title and search
        Row {
            width: parent.width
            spacing: CMTheme.spacingMedium

            Text {
                text: "Favorites"
                font.pixelSize: CMTheme.fontSizeXLarge
                font.bold: true
                font.family: CMTheme.fontFamily
                color: CMTheme.textColor
                anchors.verticalCenter: parent.verticalCenter
            }

            Item { width: parent.width - 300; height: 1 }

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
                        width: parent.width - 26
                        anchors.verticalCenter: parent.verticalCenter
                        font.pixelSize: CMTheme.fontSizeDefault
                        font.family: CMTheme.fontFamily
                        color: CMTheme.textColor
                        clip: true
                        selectByMouse: true

                        Text {
                            anchors.verticalCenter: parent.verticalCenter
                            text: "Search favorites..."
                            font.pixelSize: CMTheme.fontSizeDefault
                            font.family: CMTheme.fontFamily
                            color: CMTheme.textMutedColor
                            visible: !parent.text && !parent.activeFocus
                        }

                        onTextChanged: {
                            favoritesScreen.searchQuery = text
                            favoritesScreen.filterList()
                        }
                    }
                }
            }
        }

        ListView {
            id: favList
            width: parent.width
            height: parent.height - 50
            model: favoritesScreen.filteredFavorites
            clip: true
            spacing: CMTheme.spacingSmall
            boundsBehavior: Flickable.StopAtBounds
            flickDeceleration: 3000
            maximumFlickVelocity: 4000

            MouseArea {
                anchors.fill: parent
                acceptedButtons: Qt.NoButton
                onWheel: {
                    favList.contentY = Math.max(0, Math.min(favList.contentHeight - favList.height, favList.contentY - wheel.angleDelta.y * 1.275))
                }
            }

            ScrollBar.vertical: ScrollBar {
                policy: ScrollBar.AsNeeded
            }

            delegate: Rectangle {
                width: favList.width
                height: 72
                radius: CMTheme.radiusDefault
                color: favItemArea.containsMouse ? CMTheme.surfaceContainerHighColor : CMTheme.surfaceContainerColor

                MouseArea {
                    id: favItemArea
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
                        id: favIcon
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
                            GradientStop { position: 0.0; color: favIcon.grad[0] }
                            GradientStop { position: 1.0; color: favIcon.grad[1] }
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
                        width: parent.width - 48 - favActionRow.width - CMTheme.spacingMedium * 3
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

                            Text {
                                text: "\u00B7"
                                font.pixelSize: CMTheme.fontSizeSmall
                                color: CMTheme.textMutedColor
                            }

                            Text {
                                text: modelData.category
                                font.pixelSize: CMTheme.fontSizeSmall
                                font.family: CMTheme.fontFamily
                                color: CMTheme.textMutedColor
                            }

                            Rectangle {
                                visible: modelData.price > 0
                                width: favPriceLabel.width + CMTheme.spacingDefault
                                height: 16
                                radius: CMTheme.radiusSmall
                                color: CMTheme.accentColor
                                anchors.verticalCenter: parent.verticalCenter

                                Text {
                                    id: favPriceLabel
                                    anchors.centerIn: parent
                                    text: "$" + modelData.price
                                    font.pixelSize: 9
                                    font.bold: true
                                    font.family: CMTheme.fontFamily
                                    color: CMTheme.backgroundColor
                                }
                            }
                        }
                    }

                    Row {
                        id: favActionRow
                        z: 1
                        anchors.verticalCenter: parent.verticalCenter
                        spacing: CMTheme.spacingSmall

                        Rectangle {
                            visible: modelData.isUpdateAvailable || false
                            width: Math.max(100, favUpdateLabel.width + CMTheme.spacingXLarge * 2)
                            height: 36
                            radius: CMTheme.radiusDefault
                            color: favUpdateArea.containsMouse ? CMTheme.accentColor : CMTheme.surfaceContainerHighColor

                            Text {
                                id: favUpdateLabel
                                anchors.centerIn: parent
                                text: "Update"
                                font.pixelSize: CMTheme.fontSizeDefault
                                font.bold: true
                                font.family: CMTheme.fontFamily
                                color: favUpdateArea.containsMouse ? "#FFFFFF" : CMTheme.textColor
                            }

                            MouseArea {
                                id: favUpdateArea
                                anchors.fill: parent
                                hoverEnabled: true
                                cursorShape: Qt.PointingHandCursor
                                onClicked: appController.installComponent(modelData.id)
                            }
                        }

                        Rectangle {
                            visible: !modelData.isInstalled
                            width: Math.max(100, favInstallLabel.width + CMTheme.spacingXLarge * 2)
                            height: 36
                            radius: CMTheme.radiusDefault
                            color: favInstallArea.containsMouse ? CMTheme.accentColor : CMTheme.surfaceContainerHighColor

                            Text {
                                id: favInstallLabel
                                anchors.centerIn: parent
                                text: "Install"
                                font.pixelSize: CMTheme.fontSizeDefault
                                font.bold: true
                                font.family: CMTheme.fontFamily
                                color: favInstallArea.containsMouse ? "#FFFFFF" : CMTheme.textColor
                            }

                            MouseArea {
                                id: favInstallArea
                                anchors.fill: parent
                                hoverEnabled: true
                                cursorShape: Qt.PointingHandCursor
                                onClicked: appController.installComponent(modelData.id)
                            }
                        }

                        Rectangle {
                            visible: modelData.isInstalled && !(modelData.isUpdateAvailable || false)
                            width: Math.max(100, favInstalledLabel.width + CMTheme.spacingXLarge * 2)
                            height: 36
                            radius: CMTheme.radiusDefault
                            color: CMTheme.surfaceContainerHighColor

                            Text {
                                id: favInstalledLabel
                                anchors.centerIn: parent
                                text: "Installed"
                                font.pixelSize: CMTheme.fontSizeDefault
                                font.family: CMTheme.fontFamily
                                color: CMTheme.textMutedColor
                            }
                        }
                    }
                }
            }

            // Empty state
            Text {
                anchors.centerIn: parent
                visible: favList.count === 0
                text: favoritesScreen.searchQuery ? "No matches" : "No favorites yet"
                font.pixelSize: CMTheme.fontSizeLarge
                font.family: CMTheme.fontFamily
                color: CMTheme.textMutedColor
            }
        }
    }
}
