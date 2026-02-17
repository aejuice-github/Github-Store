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
    property bool updateAvailable: false
    property string searchQuery: ""

    signal clicked()
    signal installRequested()

    function highlightMatch(text, query, baseColor) {
        if (!query) return text
        var escaped = query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
        var regex = new RegExp("(" + escaped + ")", "ig")
        return text.replace(regex, "<span style='color:#FF9800'>$1</span>")
    }

    // Gradient thumbnail placeholder
    Rectangle {
        id: thumbnail
        anchors.left: parent.left
        anchors.right: parent.right
        anchors.top: parent.top
        height: card.height * 0.55
        radius: CMTheme.radiusLarge

        // Clip bottom corners to square while keeping top rounded
        Rectangle {
            anchors.left: parent.left
            anchors.right: parent.right
            anchors.bottom: parent.bottom
            height: parent.radius
            gradient: Gradient {
                orientation: Gradient.Horizontal
                GradientStop { position: 0.0; color: thumbnail.grad[0] }
                GradientStop { position: 0.5; color: thumbnail.grad[1] }
                GradientStop { position: 1.0; color: thumbnail.grad[2] }
            }
        }

        property int nameHash: {
            var h = 0
            for (var i = 0; i < card.name.length; i++)
                h = ((h << 5) - h + card.name.charCodeAt(i)) | 0
            return Math.abs(h)
        }

        property var gradients: [
            ["#0F2027", "#203A43", "#2C5364"],
            ["#1A1A2E", "#16213E", "#0F3460"],
            ["#0D1B2A", "#1B2838", "#2A4858"],
            ["#1C1C3C", "#2D2D5E", "#3E3E7E"],
            ["#0B0C10", "#1F2833", "#45A29E"],
            ["#1A0533", "#2D1B69", "#5B2C8E"],
            ["#0C1618", "#162A2E", "#264653"],
            ["#1B0A2A", "#2E1A47", "#4A2C6E"],
            ["#0A1628", "#152238", "#2A3F5F"],
            ["#1C0F13", "#3A1F2B", "#5C3D4E"]
        ]

        property var grad: gradients[nameHash % gradients.length]

        gradient: Gradient {
            orientation: Gradient.Horizontal
            GradientStop { position: 0.0; color: thumbnail.grad[0] }
            GradientStop { position: 0.5; color: thumbnail.grad[1] }
            GradientStop { position: 1.0; color: thumbnail.grad[2] }
        }

        // Component initial letter overlay
        Text {
            anchors.centerIn: parent
            text: card.name.charAt(0).toUpperCase()
            font.pixelSize: 48
            font.bold: true
            font.family: CMTheme.fontFamily
            color: "#40FFFFFF"
        }
    }

    Column {
        anchors.left: parent.left
        anchors.right: parent.right
        anchors.top: thumbnail.bottom
        anchors.margins: CMTheme.spacingLarge
        anchors.topMargin: CMTheme.spacingDefault
        spacing: CMTheme.spacingSmall

        // Row 1: Title
        Text {
            width: parent.width
            text: card.highlightMatch(card.name, card.searchQuery, CMTheme.textColor)
            textFormat: card.searchQuery ? Text.RichText : Text.PlainText
            font.pixelSize: CMTheme.fontSizeLarge
            font.bold: true
            font.family: CMTheme.fontFamily
            color: CMTheme.textColor
            elide: Text.ElideRight
            maximumLineCount: 1
        }

        // Row 3: Description
        Text {
            width: parent.width
            text: card.highlightMatch(card.description, card.searchQuery, CMTheme.textMutedColor)
            textFormat: card.searchQuery ? Text.RichText : Text.PlainText
            font.pixelSize: CMTheme.fontSizeDefault
            font.family: CMTheme.fontFamily
            color: CMTheme.textMutedColor
            elide: Text.ElideRight
            maximumLineCount: 2
            wrapMode: Text.WordWrap
            lineHeight: 1.2
        }

    }

    // Row 5: Full-width Install/Update button (anchored to bottom)
    Rectangle {
        id: installButton
        anchors.left: parent.left
        anchors.right: parent.right
        anchors.bottom: parent.bottom
        anchors.margins: CMTheme.spacingLarge
        height: 36
        radius: CMTheme.radiusDefault
        color: {
            if (card.updateAvailable && installBtnArea.containsMouse) return CMTheme.accentColor
            if (card.updateAvailable) return CMTheme.surfaceContainerHighColor
            if (card.installed && installBtnArea.containsMouse) return "#93000A"
            if (card.installed) return CMTheme.surfaceContainerHighColor
            if (installBtnArea.containsMouse) return CMTheme.accentColor
            return CMTheme.surfaceContainerHighColor
        }

        Row {
            anchors.centerIn: parent
            spacing: CMTheme.spacingSmall

            MaterialIcon {
                iconName: {
                    if (card.updateAvailable) return "update"
                    if (card.installed && installBtnArea.containsMouse) return "delete"
                    if (card.installed) return "check_circle"
                    return "download"
                }
                iconSize: 18
                iconColor: {
                    if (card.updateAvailable) return installBtnArea.containsMouse ? CMTheme.backgroundColor : CMTheme.textColor
                    if (card.installed) return installBtnArea.containsMouse ? "#FFFFFF" : CMTheme.textMutedColor
                    return installBtnArea.containsMouse ? CMTheme.backgroundColor : CMTheme.textColor
                }
                visible: true
                anchors.verticalCenter: parent.verticalCenter
            }

            Text {
                text: {
                    if (card.updateAvailable) return "Update"
                    if (card.installed && installBtnArea.containsMouse) return "Uninstall"
                    if (card.installed) return "Installed"
                    return "Install"
                }
                font.pixelSize: CMTheme.fontSizeDefault
                font.bold: true
                font.family: CMTheme.fontFamily
                color: {
                    if (card.updateAvailable) return installBtnArea.containsMouse ? CMTheme.backgroundColor : CMTheme.textColor
                    if (card.installed) return installBtnArea.containsMouse ? "#FFFFFF" : CMTheme.textMutedColor
                    return installBtnArea.containsMouse ? CMTheme.backgroundColor : CMTheme.textColor
                }
                anchors.verticalCenter: parent.verticalCenter
            }
        }

        MouseArea {
            id: installBtnArea
            anchors.fill: parent
            hoverEnabled: true
            cursorShape: Qt.PointingHandCursor
            onClicked: {
                if (card.updateAvailable || !card.installed)
                    appController.installComponent(card.componentId)
                else
                    root.confirmUninstall(card.componentId, card.name)
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
