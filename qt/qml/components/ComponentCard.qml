import QtQuick 2.15
import QtQuick.Controls 2.15
import "../singletons"

Rectangle {
    id: card
    height: CMTheme.cardHeight
    radius: CMTheme.radiusLarge
    color: mouseArea.containsMouse ? CMTheme.surfaceContainerHighColor : CMTheme.surfaceContainerColor

    property string componentId: ""
    property string name: ""
    property string description: ""
    property string author: ""
    property string category: ""
    property string icon: ""
    property string type: ""
    property string version: ""
    property int price: 0
    property bool installed: false

    signal clicked()
    signal installRequested()

    Column {
        anchors.left: parent.left
        anchors.right: parent.right
        anchors.top: parent.top
        anchors.margins: CMTheme.spacingLarge
        spacing: CMTheme.spacingSmall

        // Row 1: Author + Type
        Text {
            width: parent.width
            text: card.author + (card.type ? " \u2022 " + card.type.charAt(0).toUpperCase() + card.type.slice(1) : "")
            font.pixelSize: CMTheme.fontSizeSmall
            font.family: CMTheme.fontFamily
            color: CMTheme.textMutedColor
            elide: Text.ElideRight
            maximumLineCount: 1
        }

        // Row 2: Title + Free pill
        Row {
            width: parent.width
            spacing: CMTheme.spacingDefault

            Text {
                id: titleText
                width: parent.width - priceBadge.width - CMTheme.spacingDefault
                text: card.name
                font.pixelSize: CMTheme.fontSizeLarge
                font.bold: true
                font.family: CMTheme.fontFamily
                color: CMTheme.textColor
                elide: Text.ElideRight
                maximumLineCount: 1
                anchors.verticalCenter: parent.verticalCenter
            }

            PriceBadge {
                id: priceBadge
                price: card.price
                anchors.verticalCenter: parent.verticalCenter
            }
        }

        // Row 3: Description
        Text {
            width: parent.width
            text: card.description
            font.pixelSize: CMTheme.fontSizeDefault
            font.family: CMTheme.fontFamily
            color: CMTheme.textMutedColor
            elide: Text.ElideRight
            maximumLineCount: 2
            wrapMode: Text.WordWrap
            lineHeight: 1.2
        }

        // Row 4: Version + Category
        Text {
            width: parent.width
            text: (card.version ? "v" + card.version : "") + (card.version && card.category ? " \u2022 " : "") + card.category
            font.pixelSize: CMTheme.fontSizeSmall
            font.family: CMTheme.fontFamily
            color: CMTheme.textMutedColor
            elide: Text.ElideRight
            maximumLineCount: 1
        }
    }

    // Row 5: Full-width Install button (anchored to bottom)
    Rectangle {
        id: installButton
        anchors.left: parent.left
        anchors.right: parent.right
        anchors.bottom: parent.bottom
        anchors.margins: CMTheme.spacingLarge
        height: 36
        radius: CMTheme.radiusDefault
        color: {
            if (card.installed) return CMTheme.surfaceContainerHighColor
            if (installBtnArea.containsMouse) return Qt.darker(CMTheme.accentColor, 1.2)
            return CMTheme.accentColor
        }

        Row {
            anchors.centerIn: parent
            spacing: CMTheme.spacingSmall

            Text {
                text: "\u2193"
                font.pixelSize: CMTheme.fontSizeMedium
                font.bold: true
                font.family: CMTheme.fontFamily
                color: CMTheme.backgroundColor
                visible: !card.installed
                anchors.verticalCenter: parent.verticalCenter
            }

            Text {
                text: card.installed ? "\u2713  Installed" : "Install"
                font.pixelSize: CMTheme.fontSizeDefault
                font.bold: true
                font.family: CMTheme.fontFamily
                color: card.installed ? CMTheme.textMutedColor : CMTheme.backgroundColor
                anchors.verticalCenter: parent.verticalCenter
            }
        }

        MouseArea {
            id: installBtnArea
            anchors.fill: parent
            hoverEnabled: true
            cursorShape: card.installed ? Qt.ArrowCursor : Qt.PointingHandCursor
            onClicked: {
                if (!card.installed)
                    appController.installComponent(card.componentId)
            }
        }
    }

    MouseArea {
        id: mouseArea
        anchors.fill: parent
        anchors.bottomMargin: installButton.height + CMTheme.spacingLarge + CMTheme.spacingSmall
        hoverEnabled: true
        cursorShape: Qt.PointingHandCursor
        onClicked: card.clicked()
    }
}
