import QtQuick 2.15
import QtQuick.Controls 2.15
import "../singletons"
import "../components"

Rectangle {
    id: searchScreen
    color: CMTheme.backgroundColor

    Column {
        anchors.fill: parent
        anchors.margins: CMTheme.spacingLarge
        spacing: CMTheme.spacingMedium

        // Search field
        SearchField {
            id: searchInput
            width: parent.width
            onAccepted: appController.search(text)
            Component.onCompleted: forceActiveFocus()
        }

        // Results count
        Text {
            text: appController.componentModel.count + " results"
            font.pixelSize: CMTheme.fontSizeSmall
            font.family: CMTheme.fontFamily
            color: CMTheme.textMutedColor
            visible: searchInput.text.length > 0
        }

        // Results list
        ListView {
            id: resultsList
            width: parent.width
            height: parent.height - 80
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
                    resultsList.contentY = Math.max(0, Math.min(resultsList.contentHeight - resultsList.height, resultsList.contentY - wheel.angleDelta.y * 1.275))
                }
            }

            ScrollBar.vertical: ScrollBar {
                policy: ScrollBar.AsNeeded
            }

            delegate: Rectangle {
                width: resultsList.width
                height: 72
                radius: CMTheme.radiusDefault
                color: searchItemArea.containsMouse ? CMTheme.surfaceContainerHighColor : CMTheme.surfaceContainerColor

                MouseArea {
                    id: searchItemArea
                    anchors.fill: parent
                    hoverEnabled: true
                    cursorShape: Qt.PointingHandCursor
                    onClicked: appController.openComponentPage(model.componentId)
                }

                Row {
                    anchors.fill: parent
                    anchors.margins: CMTheme.spacingMedium
                    spacing: CMTheme.spacingMedium

                    Rectangle {
                        id: searchIcon
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
                            GradientStop { position: 0.0; color: searchIcon.grad[0] }
                            GradientStop { position: 1.0; color: searchIcon.grad[1] }
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

                    Column {
                        width: parent.width - 48 - searchActionRow.width - CMTheme.spacingMedium * 3
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
                                width: searchPriceLabel.width + CMTheme.spacingDefault
                                height: 16
                                radius: CMTheme.radiusSmall
                                color: CMTheme.accentColor
                                anchors.verticalCenter: parent.verticalCenter

                                Text {
                                    id: searchPriceLabel
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

                    Row {
                        id: searchActionRow
                        z: 1
                        anchors.verticalCenter: parent.verticalCenter
                        spacing: CMTheme.spacingSmall

                        Rectangle {
                            visible: model.isUpdateAvailable
                            width: Math.max(100, searchUpdateLabel.width + CMTheme.spacingXLarge * 2)
                            height: 36
                            radius: CMTheme.radiusDefault
                            color: searchUpdateArea.containsMouse ? CMTheme.accentColor : CMTheme.surfaceContainerHighColor

                            Text {
                                id: searchUpdateLabel
                                anchors.centerIn: parent
                                text: "Update"
                                font.pixelSize: CMTheme.fontSizeDefault
                                font.bold: true
                                font.family: CMTheme.fontFamily
                                color: searchUpdateArea.containsMouse ? "#FFFFFF" : CMTheme.textColor
                            }

                            MouseArea {
                                id: searchUpdateArea
                                anchors.fill: parent
                                hoverEnabled: true
                                cursorShape: Qt.PointingHandCursor
                                onClicked: appController.installComponent(model.componentId)
                            }
                        }

                        Rectangle {
                            visible: !model.isInstalled
                            width: Math.max(100, searchInstallLabel.width + CMTheme.spacingXLarge * 2)
                            height: 36
                            radius: CMTheme.radiusDefault
                            color: searchInstallArea.containsMouse ? CMTheme.accentColor : CMTheme.surfaceContainerHighColor

                            Text {
                                id: searchInstallLabel
                                anchors.centerIn: parent
                                text: "Install"
                                font.pixelSize: CMTheme.fontSizeDefault
                                font.bold: true
                                font.family: CMTheme.fontFamily
                                color: searchInstallArea.containsMouse ? "#FFFFFF" : CMTheme.textColor
                            }

                            MouseArea {
                                id: searchInstallArea
                                anchors.fill: parent
                                hoverEnabled: true
                                cursorShape: Qt.PointingHandCursor
                                onClicked: appController.installComponent(model.componentId)
                            }
                        }

                        Rectangle {
                            visible: model.isInstalled && !model.isUpdateAvailable
                            width: Math.max(100, searchInstalledLabel.width + CMTheme.spacingXLarge * 2)
                            height: 36
                            radius: CMTheme.radiusDefault
                            color: CMTheme.surfaceContainerHighColor

                            Text {
                                id: searchInstalledLabel
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
            Column {
                anchors.centerIn: parent
                visible: resultsList.count === 0
                spacing: CMTheme.spacingDefault

                Text {
                    anchors.horizontalCenter: parent.horizontalCenter
                    text: searchInput.text.length > 0
                          ? "No results for \"" + searchInput.text + "\""
                          : "Type to search components"
                    font.pixelSize: CMTheme.fontSizeLarge
                    font.family: CMTheme.fontFamily
                    color: CMTheme.textMutedColor
                }

                Text {
                    anchors.horizontalCenter: parent.horizontalCenter
                    text: "Search by name, description, author, or tags"
                    font.pixelSize: CMTheme.fontSizeDefault
                    font.family: CMTheme.fontFamily
                    color: CMTheme.textMutedColor
                    visible: searchInput.text.length === 0
                }
            }
        }
    }

    // Clear search when leaving
    Component.onDestruction: {
        appController.search("")
    }
}
