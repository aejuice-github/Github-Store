import QtQuick 2.15
import "../singletons"

Rectangle {
    id: item
    width: parent ? parent.width : 200
    height: 36
    radius: CMTheme.radiusSmall
    color: {
        if (item.selected) return CMTheme.surfaceContainerHighColor
        if (mouseArea.containsMouse) return CMTheme.surfaceContainerHighColor
        return "transparent"
    }

    property string name: ""
    property bool selected: false

    signal clicked()

    Text {
        anchors.left: parent.left
        anchors.leftMargin: CMTheme.spacingMedium
        anchors.verticalCenter: parent.verticalCenter
        text: item.name
        font.pixelSize: CMTheme.fontSizeDefault
        font.family: CMTheme.fontFamily
        font.bold: item.selected
        color: item.selected ? CMTheme.accentColor : CMTheme.textColor
    }

    MouseArea {
        id: mouseArea
        anchors.fill: parent
        hoverEnabled: true
        cursorShape: Qt.PointingHandCursor
        onClicked: item.clicked()
    }
}
