import QtQuick 2.15
import "../singletons"

// Placeholder icon display when no image URL is available
Rectangle {
    id: iconImage
    width: CMTheme.iconSizeLarge
    height: CMTheme.iconSizeLarge
    radius: CMTheme.radiusDefault
    color: CMTheme.surfaceContainerColor

    property string type: ""

    Text {
        anchors.centerIn: parent
        text: {
            switch (iconImage.type) {
                case "plugin": return "\u2699"
                case "script": return "\u2328"
                case "extension": return "\u2B62"
                case "software": return "\u2B1A"
                default: return "\u25A0"
            }
        }
        font.pixelSize: parent.width * 0.4
        color: CMTheme.accentColor
    }
}
