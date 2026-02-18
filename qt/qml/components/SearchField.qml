import QtQuick 2.15
import QtQuick.Controls 2.15
import "../singletons"

Rectangle {
    id: searchField
    height: 36
    radius: CMTheme.radiusDefault
    color: CMTheme.surfaceContainerColor
    border.color: input.activeFocus ? CMTheme.accentColor : CMTheme.borderColor
    border.width: 1

    property alias text: input.text
    property string placeholder: "Search components..."

    signal accepted()

    Row {
        anchors.fill: parent
        anchors.leftMargin: CMTheme.spacingMedium
        anchors.rightMargin: CMTheme.spacingMedium
        spacing: CMTheme.spacingDefault

        // Search icon
        Text {
            anchors.verticalCenter: parent.verticalCenter
            text: "\uD83D\uDD0D"
            font.pixelSize: CMTheme.fontSizeMedium
            color: CMTheme.textMutedColor
        }

        TextField {
            id: input
            width: parent.width - 40
            anchors.verticalCenter: parent.verticalCenter
            background: Item {}
            color: CMTheme.textColor
            font.pixelSize: CMTheme.fontSizeDefault
            font.family: CMTheme.fontFamily
            placeholderText: searchField.placeholder
            placeholderTextColor: CMTheme.textMutedColor
            selectByMouse: true
            onAccepted: searchField.accepted()
            onTextChanged: searchField.accepted()
        }
    }
}
