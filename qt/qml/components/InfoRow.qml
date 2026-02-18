import QtQuick 2.15
import "../singletons"

Row {
    spacing: CMTheme.spacingDefault

    property string label: ""
    property string value: ""

    Text {
        text: label + ":"
        font.pixelSize: CMTheme.fontSizeDefault
        font.family: CMTheme.fontFamily
        font.bold: true
        color: CMTheme.textMutedColor
        width: 100
    }

    Text {
        text: value
        font.pixelSize: CMTheme.fontSizeDefault
        font.family: CMTheme.fontFamily
        color: CMTheme.textColor
        width: parent.parent ? parent.parent.width - 108 : 200
        wrapMode: Text.WordWrap
    }
}
