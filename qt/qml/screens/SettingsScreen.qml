import QtQuick 2.15
import QtQuick.Controls 2.15
import "../singletons"
import "../components"

Rectangle {
    id: settingsScreen
    color: CMTheme.backgroundColor

    Flickable {
        anchors.fill: parent
        anchors.margins: CMTheme.spacingXLarge
        contentHeight: contentColumn.height
        clip: true
        boundsBehavior: Flickable.StopAtBounds

        Column {
            id: contentColumn
            width: parent.width
            spacing: CMTheme.spacingXLarge

            Text {
                text: "Settings"
                font.pixelSize: CMTheme.fontSizeTitle
                font.bold: true
                font.family: CMTheme.fontFamily
                color: CMTheme.textColor
            }

            // Theme section
            Rectangle {
                width: parent.width
                height: themeColumn.height + CMTheme.spacingXLarge * 2
                radius: CMTheme.radiusDefault
                color: CMTheme.surfaceColor

                Column {
                    id: themeColumn
                    anchors.left: parent.left
                    anchors.right: parent.right
                    anchors.top: parent.top
                    anchors.margins: CMTheme.spacingXLarge
                    spacing: CMTheme.spacingMedium

                    Text {
                        text: "Theme Color"
                        font.pixelSize: CMTheme.fontSizeLarge
                        font.bold: true
                        font.family: CMTheme.fontFamily
                        color: CMTheme.textColor
                    }

                    Text {
                        text: "Choose a color scheme for the application"
                        font.pixelSize: CMTheme.fontSizeDefault
                        font.family: CMTheme.fontFamily
                        color: CMTheme.textMutedColor
                    }

                    ThemeColorSelector {
                        selectedTheme: appController.settings.themeColor
                        onThemeSelected: appController.settings.themeColor = themeName
                    }
                }
            }

            // Updates section
            Rectangle {
                width: parent.width
                height: updatesColumn.height + CMTheme.spacingXLarge * 2
                radius: CMTheme.radiusDefault
                color: CMTheme.surfaceColor

                Column {
                    id: updatesColumn
                    anchors.left: parent.left
                    anchors.right: parent.right
                    anchors.top: parent.top
                    anchors.margins: CMTheme.spacingXLarge
                    spacing: CMTheme.spacingMedium

                    Text {
                        text: "Updates"
                        font.pixelSize: CMTheme.fontSizeLarge
                        font.bold: true
                        font.family: CMTheme.fontFamily
                        color: CMTheme.textColor
                    }

                    Row {
                        width: parent.width
                        spacing: CMTheme.spacingMedium

                        Column {
                            width: parent.width - autoUpdateToggle.width - CMTheme.spacingMedium
                            spacing: 4

                            Text {
                                text: "Keep up to date"
                                font.pixelSize: CMTheme.fontSizeDefault
                                font.family: CMTheme.fontFamily
                                color: CMTheme.textColor
                            }

                            Text {
                                text: "Automatically update components when new versions are available"
                                font.pixelSize: CMTheme.fontSizeSmall
                                font.family: CMTheme.fontFamily
                                color: CMTheme.textMutedColor
                                width: parent.width
                                wrapMode: Text.WordWrap
                            }
                        }

                        Rectangle {
                            id: autoUpdateToggle
                            width: 44
                            height: 24
                            radius: 12
                            anchors.verticalCenter: parent.verticalCenter
                            color: appController.settings.autoUpdate ? CMTheme.accentColor : CMTheme.surfaceContainerHighColor

                            Rectangle {
                                width: 18
                                height: 18
                                radius: 9
                                anchors.verticalCenter: parent.verticalCenter
                                x: appController.settings.autoUpdate ? parent.width - width - 3 : 3
                                color: "#FFFFFF"

                                Behavior on x { NumberAnimation { duration: 150 } }
                            }

                            MouseArea {
                                anchors.fill: parent
                                cursorShape: Qt.PointingHandCursor
                                onClicked: appController.settings.autoUpdate = !appController.settings.autoUpdate
                            }
                        }
                    }
                }
            }

            // About section
            Rectangle {
                width: parent.width
                height: aboutColumn.height + CMTheme.spacingXLarge * 2
                radius: CMTheme.radiusDefault
                color: CMTheme.surfaceColor

                Column {
                    id: aboutColumn
                    anchors.left: parent.left
                    anchors.right: parent.right
                    anchors.top: parent.top
                    anchors.margins: CMTheme.spacingXLarge
                    spacing: CMTheme.spacingMedium

                    Text {
                        text: "About"
                        font.pixelSize: CMTheme.fontSizeLarge
                        font.bold: true
                        font.family: CMTheme.fontFamily
                        color: CMTheme.textColor
                    }

                    InfoRow { label: "App"; value: "AEJuice App Store" }
                    InfoRow { label: "Version"; value: "1.0.0" }
                    InfoRow { label: "Developer"; value: "AEJuice" }
                }
            }
        }
    }
}
