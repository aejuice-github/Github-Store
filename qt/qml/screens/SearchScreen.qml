import QtQuick 2.15
import QtQuick.Controls 2.15
import "../singletons"
import "../components"

Rectangle {
    id: searchScreen
    color: CMTheme.backgroundColor

    Column {
        anchors.fill: parent
        anchors.margins: CMTheme.spacingLarge
        spacing: CMTheme.spacingMedium

        // Search field
        SearchField {
            id: searchInput
            width: parent.width
            onAccepted: appController.search(text)
            Component.onCompleted: forceActiveFocus()
        }

        // Results count
        Text {
            text: appController.componentModel.count + " results"
            font.pixelSize: CMTheme.fontSizeSmall
            font.family: CMTheme.fontFamily
            color: CMTheme.textMutedColor
            visible: searchInput.text.length > 0
        }

        // Results grid
        GridView {
            id: resultsGrid
            width: parent.width
            height: parent.height - 80
            cellWidth: width / 2
            cellHeight: CMTheme.cardHeight + CMTheme.spacingMedium
            model: appController.componentModel
            clip: true

            ScrollBar.vertical: ScrollBar {
                policy: ScrollBar.AsNeeded
            }

            delegate: Item {
                width: resultsGrid.cellWidth
                height: resultsGrid.cellHeight

                ComponentCard {
                    anchors.fill: parent
                    anchors.margins: CMTheme.spacingSmall
                    componentId: model.componentId
                    name: model.name
                    description: model.description
                    author: model.author
                    category: model.category
                    icon: model.icon
                    type: model.type
                    version: model.version
                    price: model.price
                    installed: model.isInstalled
                    updateAvailable: model.isUpdateAvailable
                    onClicked: {
                        appController.navigateTo("details", { componentId: model.componentId })
                    }
                }
            }

            // Empty state
            Column {
                anchors.centerIn: parent
                visible: resultsGrid.count === 0
                spacing: CMTheme.spacingDefault

                Text {
                    anchors.horizontalCenter: parent.horizontalCenter
                    text: searchInput.text.length > 0
                          ? "No results for \"" + searchInput.text + "\""
                          : "Type to search components"
                    font.pixelSize: CMTheme.fontSizeLarge
                    font.family: CMTheme.fontFamily
                    color: CMTheme.textMutedColor
                }

                Text {
                    anchors.horizontalCenter: parent.horizontalCenter
                    text: "Search by name, description, author, or tags"
                    font.pixelSize: CMTheme.fontSizeDefault
                    font.family: CMTheme.fontFamily
                    color: CMTheme.textMutedColor
                    visible: searchInput.text.length === 0
                }
            }
        }
    }

    // Clear search when leaving
    Component.onDestruction: {
        appController.search("")
    }
}
