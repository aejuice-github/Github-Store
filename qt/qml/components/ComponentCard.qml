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
    property bool showUninstall: false
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

        property var grad: {
            // Curated bright gradient palettes
            var palettes = [
                ["#FF6B6B", "#FF8E53", "#FFC93C"],  // Coral → Orange → Yellow
                ["#36D1DC", "#5B86E5", "#36D1DC"],  // Cyan → Blue → Cyan
                ["#FF9A9E", "#FAD0C4", "#FFD1FF"],  // Pink → Peach → Lavender
                ["#A8FF78", "#78FFD6", "#78C6FF"],  // Green → Mint → Sky
                ["#FF758C", "#FF7EB3", "#FFBA5C"],  // Rose → Pink → Gold
                ["#43E97B", "#38F9D7", "#43E97B"],  // Emerald → Turquoise → Emerald
                ["#FA709A", "#FEE140", "#FA709A"],  // Hot Pink → Yellow → Hot Pink
                ["#30CFD0", "#330867", "#30CFD0"],  // Teal → Deep → Teal
                ["#FFE985", "#FA742B", "#FFE985"],  // Light Gold → Orange → Light Gold
                ["#A18CD1", "#FBC2EB", "#A18CD1"],  // Lilac → Soft Pink → Lilac
                ["#FF9966", "#FF5E62", "#FF9966"],  // Peach → Red → Peach
                ["#56CCF2", "#2F80ED", "#56CCF2"],  // Light Blue → Blue → Light Blue
                ["#F7971E", "#FFD200", "#F7971E"],  // Orange → Gold → Orange
                ["#00C9FF", "#92FE9D", "#00C9FF"],  // Sky → Mint → Sky
                ["#FC5C7D", "#6A82FB", "#FC5C7D"],  // Pink → Periwinkle → Pink
                ["#FDFC47", "#24FE41", "#FDFC47"],  // Yellow → Green → Yellow
                ["#FF512F", "#F09819", "#FF512F"],  // Red Orange → Amber → Red Orange
                ["#11998E", "#38EF7D", "#11998E"],  // Teal → Green → Teal
                ["#FC466B", "#3F5EFB", "#FC466B"],  // Magenta → Blue → Magenta
                ["#FFE259", "#FFA751", "#FFE259"]   // Sunny → Tangerine → Sunny
            ]

            var h = 0
            for (var i = 0; i < card.name.length; i++)
                h = ((h << 5) - h + card.name.charCodeAt(i)) | 0
            return palettes[Math.abs(h) % palettes.length]
        }

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

    // Bottom buttons
    Row {
        id: installButton
        anchors.left: parent.left
        anchors.right: parent.right
        anchors.bottom: parent.bottom
        anchors.margins: CMTheme.spacingLarge
        spacing: CMTheme.spacingSmall

        // Update button (only when update available)
        Rectangle {
            visible: card.updateAvailable
            width: visible ? (card.showUninstall ? (parent.width - parent.spacing) / 2 : parent.width) : 0
            height: 36
            radius: CMTheme.radiusDefault
            color: updateBtnArea.containsMouse ? CMTheme.accentColor : CMTheme.surfaceContainerHighColor

            Row {
                anchors.centerIn: parent
                spacing: CMTheme.spacingSmall
                MaterialIcon {
                    iconName: "update"
                    iconSize: 18
                    iconColor: updateBtnArea.containsMouse ? "#FFFFFF" : CMTheme.textColor
                    anchors.verticalCenter: parent.verticalCenter
                }
                Text {
                    text: "Update"
                    font.pixelSize: CMTheme.fontSizeDefault
                    font.bold: true
                    font.family: CMTheme.fontFamily
                    color: updateBtnArea.containsMouse ? "#FFFFFF" : CMTheme.textColor
                    anchors.verticalCenter: parent.verticalCenter
                }
            }

            MouseArea {
                id: updateBtnArea
                anchors.fill: parent
                hoverEnabled: true
                cursorShape: Qt.PointingHandCursor
                onClicked: appController.installComponent(card.componentId)
            }
        }

        // Uninstall button (only when update available)
        Rectangle {
            visible: card.updateAvailable && card.showUninstall
            width: visible ? (parent.width - parent.spacing) / 2 : 0
            height: 36
            radius: CMTheme.radiusDefault
            color: uninstallBtnArea.containsMouse ? "#93000A" : CMTheme.surfaceContainerHighColor

            Row {
                anchors.centerIn: parent
                spacing: CMTheme.spacingSmall
                MaterialIcon {
                    iconName: "delete"
                    iconSize: 18
                    iconColor: uninstallBtnArea.containsMouse ? "#FFFFFF" : CMTheme.textMutedColor
                    anchors.verticalCenter: parent.verticalCenter
                }
                Text {
                    text: "Uninstall"
                    font.pixelSize: CMTheme.fontSizeDefault
                    font.bold: true
                    font.family: CMTheme.fontFamily
                    color: uninstallBtnArea.containsMouse ? "#FFFFFF" : CMTheme.textMutedColor
                    anchors.verticalCenter: parent.verticalCenter
                }
            }

            MouseArea {
                id: uninstallBtnArea
                anchors.fill: parent
                hoverEnabled: true
                cursorShape: Qt.PointingHandCursor
                onClicked: root.confirmUninstall(card.componentId, card.name)
            }
        }

        // Uninstall button (installed, no update)
        Rectangle {
            visible: !card.updateAvailable && card.installed && card.showUninstall
            width: visible ? parent.width : 0
            height: 36
            radius: CMTheme.radiusDefault
            color: uninstallOnlyArea.containsMouse ? "#93000A" : CMTheme.surfaceContainerHighColor

            Row {
                anchors.centerIn: parent
                spacing: CMTheme.spacingSmall
                MaterialIcon {
                    iconName: "delete"
                    iconSize: 18
                    iconColor: uninstallOnlyArea.containsMouse ? "#FFFFFF" : CMTheme.textMutedColor
                    anchors.verticalCenter: parent.verticalCenter
                }
                Text {
                    text: "Uninstall"
                    font.pixelSize: CMTheme.fontSizeDefault
                    font.bold: true
                    font.family: CMTheme.fontFamily
                    color: uninstallOnlyArea.containsMouse ? "#FFFFFF" : CMTheme.textMutedColor
                    anchors.verticalCenter: parent.verticalCenter
                }
            }

            MouseArea {
                id: uninstallOnlyArea
                anchors.fill: parent
                hoverEnabled: true
                cursorShape: Qt.PointingHandCursor
                onClicked: root.confirmUninstall(card.componentId, card.name)
            }
        }

        // Installed indicator (home screen - no uninstall)
        Rectangle {
            visible: !card.updateAvailable && card.installed && !card.showUninstall
            width: visible ? parent.width : 0
            height: 36
            radius: CMTheme.radiusDefault
            color: CMTheme.surfaceContainerHighColor

            Row {
                anchors.centerIn: parent
                spacing: CMTheme.spacingSmall
                MaterialIcon {
                    iconName: "check_circle"
                    iconSize: 18
                    iconColor: CMTheme.textMutedColor
                    anchors.verticalCenter: parent.verticalCenter
                }
                Text {
                    text: "Installed"
                    font.pixelSize: CMTheme.fontSizeDefault
                    font.bold: true
                    font.family: CMTheme.fontFamily
                    color: CMTheme.textMutedColor
                    anchors.verticalCenter: parent.verticalCenter
                }
            }
        }

        // Install button (not installed)
        Rectangle {
            visible: !card.updateAvailable && !card.installed
            width: visible ? parent.width : 0
            height: 36
            radius: CMTheme.radiusDefault
            color: installBtnArea.containsMouse ? CMTheme.accentColor : CMTheme.surfaceContainerHighColor

            Row {
                anchors.centerIn: parent
                spacing: CMTheme.spacingSmall
                MaterialIcon {
                    iconName: "download"
                    iconSize: 18
                    iconColor: installBtnArea.containsMouse ? "#FFFFFF" : CMTheme.textColor
                    anchors.verticalCenter: parent.verticalCenter
                }
                Text {
                    text: "Install"
                    font.pixelSize: CMTheme.fontSizeDefault
                    font.bold: true
                    font.family: CMTheme.fontFamily
                    color: installBtnArea.containsMouse ? "#FFFFFF" : CMTheme.textColor
                    anchors.verticalCenter: parent.verticalCenter
                }
            }

            MouseArea {
                id: installBtnArea
                anchors.fill: parent
                hoverEnabled: true
                cursorShape: Qt.PointingHandCursor
                onClicked: appController.installComponent(card.componentId)
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
