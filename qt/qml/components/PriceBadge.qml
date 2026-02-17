import QtQuick 2.15
import "../singletons"

Rectangle {
    id: badge
    property int price: 0

    width: priceText.implicitWidth + CMTheme.spacingMedium * 2
    height: priceText.implicitHeight + CMTheme.spacingSmall * 2
    radius: height / 2
    color: price === 0 ? Qt.rgba(76/255, 175/255, 80/255, 0.15) : Qt.rgba(CMTheme.accentColor.r, CMTheme.accentColor.g, CMTheme.accentColor.b, 0.15)

    Text {
        id: priceText
        anchors.centerIn: parent
        text: badge.price === 0 ? "Free" : "$" + badge.price
        font.pixelSize: CMTheme.fontSizeSmall
        font.bold: true
        font.family: CMTheme.fontFamily
        color: badge.price === 0 ? CMTheme.successColor : CMTheme.accentColor
    }
}
