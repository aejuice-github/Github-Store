import QtQuick 2.15
import "../singletons"

Rectangle {
    id: button
    width: buttonLabel.width + CMTheme.spacingXLarge * 2
    height: 36
    radius: CMTheme.radiusDefault
    color: {
        if (!button.enabled) return CMTheme.surfaceContainerHighColor
        if (mouseArea.pressed) return Qt.darker(buttonColor, 1.3)
        if (mouseArea.containsMouse) return Qt.lighter(buttonColor, 1.2)
        return buttonColor
    }

    property string text: ""
    property color buttonColor: CMTheme.accentColor
    property color textColor: "#FFFFFF"
    property bool primary: true

    signal clicked()

    Text {
        id: buttonLabel
        anchors.centerIn: parent
        text: button.text
        font.pixelSize: CMTheme.fontSizeDefault
        font.bold: true
        font.family: CMTheme.fontFamily
        color: button.enabled ? button.textColor : CMTheme.textMutedColor
    }

    MouseArea {
        id: mouseArea
        anchors.fill: parent
        hoverEnabled: true
        cursorShape: button.enabled ? Qt.PointingHandCursor : Qt.ArrowCursor
        onClicked: {
            if (button.enabled)
                button.clicked()
        }
    }
}
