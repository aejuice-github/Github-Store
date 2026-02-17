import QtQuick 2.15
import QtQuick.Controls 2.15
import "../singletons"
import "../components"

Rectangle {
    id: installedScreen
    objectName: "installedAppsScreen"
    color: CMTheme.backgroundColor

    property var installedApps: []
    property var filteredApps: []
    property string searchQuery: ""

    Component.onCompleted: {
        installedApps = appController.getInstalledComponents()
        filteredApps = installedApps
    }

    function filterList() {
        if (!searchQuery) {
            filteredApps = installedApps
            return
        }
        var result = []
        var query = searchQuery.toLowerCase()
        for (var i = 0; i < installedApps.length; i++) {
            var item = installedApps[i]
            if (item.name.toLowerCase().indexOf(query) >= 0
                || item.description.toLowerCase().indexOf(query) >= 0
                || item.author.toLowerCase().indexOf(query) >= 0) {
                result.push(item)
            }
        }
        filteredApps = result
    }

    Column {
        anchors.fill: parent
        anchors.margins: CMTheme.spacingLarge
        spacing: CMTheme.spacingMedium

        // Header row with title and search
        Row {
            width: parent.width
            spacing: CMTheme.spacingMedium

            Text {
                text: "Installed Apps"
                font.pixelSize: CMTheme.fontSizeXLarge
                font.bold: true
                font.family: CMTheme.fontFamily
                color: CMTheme.textColor
                anchors.verticalCenter: parent.verticalCenter
            }

            Item { width: parent.width - 350; height: 1 }

            Rectangle {
                width: 200
                height: 32
                radius: CMTheme.radiusDefault
                color: CMTheme.surfaceContainerHighColor
                anchors.verticalCenter: parent.verticalCenter

                Row {
                    anchors.fill: parent
                    anchors.leftMargin: CMTheme.spacingDefault
                    anchors.rightMargin: CMTheme.spacingDefault
                    spacing: CMTheme.spacingSmall

                    MaterialIcon {
                        iconName: "search"
                        iconSize: 18
                        iconColor: CMTheme.textMutedColor
                        anchors.verticalCenter: parent.verticalCenter
                    }

                    TextInput {
                        width: parent.width - 26
                        anchors.verticalCenter: parent.verticalCenter
                        font.pixelSize: CMTheme.fontSizeDefault
                        font.family: CMTheme.fontFamily
                        color: CMTheme.textColor
                        clip: true
                        selectByMouse: true

                        Text {
                            anchors.verticalCenter: parent.verticalCenter
                            text: "Search installed..."
                            font.pixelSize: CMTheme.fontSizeDefault
                            font.family: CMTheme.fontFamily
                            color: CMTheme.textMutedColor
                            visible: !parent.text && !parent.activeFocus
                        }

                        onTextChanged: {
                            installedScreen.searchQuery = text
                            installedScreen.filterList()
                        }
                    }
                }
            }
        }

        GridView {
            id: installedGrid
            width: parent.width
            height: parent.height - 50
            cellWidth: width / 2
            cellHeight: CMTheme.cardHeight + CMTheme.spacingMedium
            model: installedScreen.filteredApps
            clip: true
            boundsBehavior: Flickable.StopAtBounds

            ScrollBar.vertical: ScrollBar {
                policy: ScrollBar.AsNeeded
            }

            delegate: Item {
                width: installedGrid.cellWidth
                height: installedGrid.cellHeight

                ComponentCard {
                    anchors.fill: parent
                    anchors.margins: CMTheme.spacingSmall
                    componentId: modelData.id
                    name: modelData.name
                    description: modelData.description
                    author: modelData.author
                    category: modelData.category
                    icon: modelData.icon || ""
                    type: modelData.type
                    version: modelData.version
                    price: modelData.price
                    installed: modelData.isInstalled
                    updateAvailable: modelData.isUpdateAvailable || false
                    onClicked: {
                        appController.navigateTo("details", { componentId: modelData.id })
                    }
                }
            }

            // Empty state
            Text {
                anchors.centerIn: parent
                visible: installedGrid.count === 0
                text: installedScreen.searchQuery ? "No matches" : "No installed apps yet"
                font.pixelSize: CMTheme.fontSizeLarge
                font.family: CMTheme.fontFamily
                color: CMTheme.textMutedColor
            }
        }
    }
}
