import QtQuick 2.15
import "../singletons"

Rectangle {
    id: chip
    width: chipText.width + CMTheme.spacingMedium * 2
    height: 24
    radius: CMTheme.radiusMedium
    color: chipArea.containsMouse ? CMTheme.accentColor : CMTheme.surfaceContainerHighColor

    property string text: ""
    signal clicked()

    Text {
        id: chipText
        anchors.centerIn: parent
        text: chip.text
        font.pixelSize: CMTheme.fontSizeSmall
        font.family: CMTheme.fontFamily
        color: chipArea.containsMouse ? "#FFFFFF" : CMTheme.textMutedColor
    }

    MouseArea {
        id: chipArea
        anchors.fill: parent
        hoverEnabled: true
        cursorShape: Qt.PointingHandCursor
        onClicked: chip.clicked()
    }
}
