import QtQuick 2.15
import QtQuick.Controls 2.15
import "../singletons"
import "../components"

Rectangle {
    id: detailsScreen
    color: CMTheme.backgroundColor

    property string componentId: ""
    property var details: ({})
    property bool isFavorite: false

    Component.onCompleted: {
        if (componentId) {
            details = appController.getComponentDetails(componentId)
            isFavorite = appController.isFavorite(componentId)
        }
    }

    // Favorite button (top right)
    Rectangle {
        anchors.top: parent.top
        anchors.right: parent.right
        anchors.rightMargin: CMTheme.spacingLarge + 25
        anchors.topMargin: CMTheme.spacingLarge
        width: 40
        height: 40
        radius: 20
        z: 10
        color: favBtnArea.containsMouse ? CMTheme.surfaceContainerHighColor : CMTheme.surfaceContainerColor

        MaterialIcon {
            anchors.centerIn: parent
            iconName: "favorite"
            iconSize: 22
            iconColor: detailsScreen.isFavorite ? "#FF4081" : CMTheme.textMutedColor
        }

        MouseArea {
            id: favBtnArea
            anchors.fill: parent
            hoverEnabled: true
            cursorShape: Qt.PointingHandCursor
            onClicked: {
                appController.toggleFavorite(detailsScreen.componentId)
                detailsScreen.isFavorite = !detailsScreen.isFavorite
            }
        }
    }

    Flickable {
        anchors.fill: parent
        anchors.margins: CMTheme.spacingXLarge
        contentHeight: contentColumn.height
        clip: true
        boundsBehavior: Flickable.StopAtBounds

        ScrollBar.vertical: ScrollBar {
            policy: ScrollBar.AsNeeded
        }

        Column {
            id: contentColumn
            width: parent.width
            spacing: CMTheme.spacingLarge

            // Header row
            Row {
                width: parent.width
                spacing: CMTheme.spacingLarge

                // Icon
                IconImage {
                    width: 100
                    height: 100
                    type: details.type || ""
                }

                // Title and meta
                Column {
                    spacing: CMTheme.spacingDefault
                    anchors.verticalCenter: parent.verticalCenter

                    Text {
                        text: details.name || ""
                        font.pixelSize: CMTheme.fontSizeTitle
                        font.bold: true
                        font.family: CMTheme.fontFamily
                        color: CMTheme.textColor
                    }

                    Row {
                        spacing: CMTheme.spacingMedium

                        Text {
                            text: "by " + (details.author || "")
                            font.pixelSize: CMTheme.fontSizeMedium
                            font.family: CMTheme.fontFamily
                            color: CMTheme.textMutedColor
                        }

                        Text {
                            text: "|"
                            font.pixelSize: CMTheme.fontSizeMedium
                            color: CMTheme.textMutedColor
                        }

                        Text {
                            text: "v" + (details.version || "")
                            font.pixelSize: CMTheme.fontSizeMedium
                            font.family: CMTheme.fontFamily
                            color: CMTheme.textMutedColor
                        }

                        PriceBadge {
                            price: details.price || 0
                        }
                    }

                    // Install button
                    Row {
                        spacing: CMTheme.spacingMedium

                        ActionButton {
                            text: {
                                if (details.isInstalled) return "Installed"
                                if (appController.installer.isBusy) return "Installing..."
                                return "Install"
                            }
                            enabled: !details.isInstalled && !appController.installer.isBusy
                            onClicked: appController.installComponent(detailsScreen.componentId)
                        }
                    }
                }
            }

            // Divider
            Rectangle {
                width: parent.width
                height: 1
                color: CMTheme.borderColor
            }

            // Description
            Column {
                width: parent.width
                spacing: CMTheme.spacingDefault

                Text {
                    text: "Description"
                    font.pixelSize: CMTheme.fontSizeLarge
                    font.bold: true
                    font.family: CMTheme.fontFamily
                    color: CMTheme.textColor
                }

                Text {
                    width: parent.width
                    text: details.description || ""
                    font.pixelSize: CMTheme.fontSizeDefault
                    font.family: CMTheme.fontFamily
                    color: CMTheme.textColor
                    wrapMode: Text.WordWrap
                    lineHeight: 1.4
                }
            }

            // Info rows
            Column {
                width: parent.width
                spacing: CMTheme.spacingDefault

                Text {
                    text: "Details"
                    font.pixelSize: CMTheme.fontSizeLarge
                    font.bold: true
                    font.family: CMTheme.fontFamily
                    color: CMTheme.textColor
                }

                InfoRow { label: "Type"; value: details.type || "" }

                Row {
                    spacing: CMTheme.spacingDefault
                    Text {
                        text: "Category:"
                        font.pixelSize: CMTheme.fontSizeDefault
                        font.family: CMTheme.fontFamily
                        font.bold: true
                        color: CMTheme.textMutedColor
                        width: 100
                    }
                    TagChip {
                        text: details.category || ""
                        onClicked: {
                            appController.filterByCategory(details.category)
                            appController.navigateTo("home")
                        }
                    }
                }

                InfoRow { label: "Version"; value: details.version || "" }

                Row {
                    spacing: CMTheme.spacingDefault
                    Text {
                        text: "Author:"
                        font.pixelSize: CMTheme.fontSizeDefault
                        font.family: CMTheme.fontFamily
                        font.bold: true
                        color: CMTheme.textMutedColor
                        width: 100
                    }
                    TagChip {
                        text: details.author || ""
                        onClicked: {
                            appController.filterByAuthor(details.author)
                            appController.navigateTo("home")
                        }
                    }
                }
            }

            // Compatible apps
            Column {
                width: parent.width
                spacing: CMTheme.spacingDefault
                visible: details.compatibleApps && details.compatibleApps.length > 0

                Text {
                    text: "Compatible Apps"
                    font.pixelSize: CMTheme.fontSizeLarge
                    font.bold: true
                    font.family: CMTheme.fontFamily
                    color: CMTheme.textColor
                }

                Flow {
                    width: parent.width
                    spacing: CMTheme.spacingDefault

                    Repeater {
                        model: details.compatibleApps || []
                        TagChip {
                            text: modelData
                            onClicked: {
                                appController.filterByApp(modelData)
                                appController.navigateTo("home")
                            }
                        }
                    }
                }
            }

            // Tags
            Column {
                width: parent.width
                spacing: CMTheme.spacingDefault
                visible: details.tags && details.tags.length > 0

                Text {
                    text: "Tags"
                    font.pixelSize: CMTheme.fontSizeLarge
                    font.bold: true
                    font.family: CMTheme.fontFamily
                    color: CMTheme.textColor
                }

                Flow {
                    width: parent.width
                    spacing: CMTheme.spacingDefault

                    Repeater {
                        model: details.tags || []
                        TagChip {
                            text: modelData
                            onClicked: {
                                appController.search(modelData)
                                appController.navigateTo("home")
                            }
                        }
                    }
                }
            }

            // Changelog
            Column {
                width: parent.width
                spacing: CMTheme.spacingDefault
                visible: (details.changelog || "") !== ""

                Text {
                    text: "Changelog"
                    font.pixelSize: CMTheme.fontSizeLarge
                    font.bold: true
                    font.family: CMTheme.fontFamily
                    color: CMTheme.textColor
                }

                Text {
                    width: parent.width
                    text: details.changelog || ""
                    font.pixelSize: CMTheme.fontSizeDefault
                    font.family: CMTheme.fontFamily
                    color: CMTheme.textMutedColor
                    wrapMode: Text.WordWrap
                }
            }

            // Bottom spacer
            Item { width: 1; height: CMTheme.spacingXLarge }
        }
    }
}
