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

    signal clicked()
    signal installRequested()

    Column {
        anchors.left: parent.left
        anchors.right: parent.right
        anchors.top: parent.top
        anchors.margins: CMTheme.spacingLarge
        spacing: CMTheme.spacingSmall

        // Row 1: Title
        Text {
            width: parent.width
            text: card.name
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
            text: card.description
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
            if (card.updateAvailable && installBtnArea.containsMouse) return "#F9A825"
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
                    if (card.updateAvailable) return installBtnArea.containsMouse ? CMTheme.backgroundColor : "#F9A825"
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
                    if (card.updateAvailable) return installBtnArea.containsMouse ? CMTheme.backgroundColor : "#F9A825"
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
