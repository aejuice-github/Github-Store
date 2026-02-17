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

                    InfoRow { label: "App"; value: "Component Manager" }
                    InfoRow { label: "Version"; value: "1.0.0" }
                    InfoRow { label: "Developer"; value: "AEJuice" }
                }
            }
        }
    }
}
