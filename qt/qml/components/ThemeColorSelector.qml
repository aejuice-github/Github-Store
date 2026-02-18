import QtQuick 2.15
import "../singletons"

Row {
    id: selector
    spacing: CMTheme.spacingMedium

    property string selectedTheme: CMTheme.activeTheme

    signal themeSelected(string themeName)

    Repeater {
        model: ["ocean", "purple", "forest", "slate", "amber"]

        Rectangle {
            width: 40
            height: 40
            radius: 20
            color: CMTheme.themePreviewColors[modelData]
            border.color: selector.selectedTheme === modelData ? CMTheme.textColor : "transparent"
            border.width: 3

            Rectangle {
                anchors.centerIn: parent
                width: 30
                height: 30
                radius: 15
                color: CMTheme.themePreviewColors[modelData]
            }

            Text {
                anchors.centerIn: parent
                text: selector.selectedTheme === modelData ? "\u2713" : ""
                font.pixelSize: 16
                font.bold: true
                color: CMTheme.backgroundColor
            }

            MouseArea {
                anchors.fill: parent
                cursorShape: Qt.PointingHandCursor
                onClicked: {
                    selector.selectedTheme = modelData
                    selector.themeSelected(modelData)
                }
            }
        }
    }
}
