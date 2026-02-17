import QtQuick 2.15
import QtQuick.Controls 2.15
import "../singletons"
import "../components"

Rectangle {
    id: homeScreen
    color: CMTheme.backgroundColor

    Row {
        anchors.fill: parent

        // Sidebar
        Rectangle {
            id: sidebar
            width: CMTheme.sidebarWidth
            height: parent.height
            color: CMTheme.surfaceColor

            Flickable {
                anchors.fill: parent
                anchors.margins: CMTheme.spacingDefault
                contentHeight: sidebarContent.height
                clip: true
                boundsBehavior: Flickable.StopAtBounds

                Column {
                    id: sidebarContent
                    width: parent.width
                    spacing: CMTheme.spacingDefault

                    // Application filter
                    Text {
                        text: "Application"
                        font.pixelSize: CMTheme.fontSizeSmall
                        font.family: CMTheme.fontFamily
                        color: CMTheme.textMutedColor
                    }

                    AppDropdown {
                        width: parent.width
                        model: appController.getAvailableApps()
                        onActivated: appController.filterByApp(currentText)
                    }

                    // Price filter
                    Rectangle {
                        width: parent.width
                        height: 1
                        color: CMTheme.borderColor
                    }

                    Text {
                        text: "Price"
                        font.pixelSize: CMTheme.fontSizeSmall
                        font.family: CMTheme.fontFamily
                        color: CMTheme.textMutedColor
                    }

                    Column {
                        width: parent.width
                        spacing: 2

                        property string selected: "All"

                        Repeater {
                            model: ["All", "Free", "Paid"]

                            Rectangle {
                                width: parent.width
                                height: 32
                                radius: CMTheme.radiusSmall
                                color: {
                                    if (parent.selected === modelData) return CMTheme.accentColor
                                    if (priceArea.containsMouse) return CMTheme.surfaceContainerHighColor
                                    return "transparent"
                                }

                                Text {
                                    anchors.left: parent.left
                                    anchors.leftMargin: CMTheme.spacingMedium
                                    anchors.verticalCenter: parent.verticalCenter
                                    text: modelData
                                    font.pixelSize: CMTheme.fontSizeDefault
                                    font.family: CMTheme.fontFamily
                                    font.bold: parent.parent.selected === modelData
                                    color: parent.parent.selected === modelData ? CMTheme.backgroundColor : CMTheme.textColor
                                }

                                MouseArea {
                                    id: priceArea
                                    anchors.fill: parent
                                    hoverEnabled: true
                                    cursorShape: Qt.PointingHandCursor
                                    onClicked: {
                                        parent.parent.selected = modelData
                                        appController.filterByPrice(modelData)
                                    }
                                }
                            }
                        }
                    }

                    // Categories
                    Rectangle {
                        width: parent.width
                        height: 1
                        color: CMTheme.borderColor
                    }

                    Text {
                        text: "Categories"
                        font.pixelSize: CMTheme.fontSizeSmall
                        font.family: CMTheme.fontFamily
                        color: CMTheme.textMutedColor
                    }

                    ListView {
                        width: parent.width
                        height: contentHeight
                        model: appController.categoryModel
                        interactive: false
                        spacing: 2

                        delegate: SidebarCategory {
                            name: model.name
                            selected: model.isSelected
                            onClicked: appController.filterByCategory(model.name)
                        }
                    }
                }
            }

            // Right border
            Rectangle {
                anchors.right: parent.right
                anchors.top: parent.top
                anchors.bottom: parent.bottom
                width: 1
                color: CMTheme.borderColor
            }
        }

        // Main content area
        Rectangle {
            width: parent.width - sidebar.width
            height: parent.height
            color: CMTheme.backgroundColor

            Column {
                anchors.fill: parent
                anchors.margins: CMTheme.spacingLarge
                spacing: CMTheme.spacingMedium

                // Component grid
                GridView {
                    id: gridView
                    width: parent.width
                    height: parent.height - CMTheme.spacingLarge
                    cellWidth: width / 2
                    cellHeight: CMTheme.cardHeight + CMTheme.spacingMedium
                    model: appController.componentModel
                    clip: true
                    boundsBehavior: Flickable.StopAtBounds

                    ScrollBar.vertical: ScrollBar {
                        policy: ScrollBar.AsNeeded
                    }

                    delegate: Item {
                        width: gridView.cellWidth
                        height: gridView.cellHeight

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
                            onClicked: {
                                appController.navigateTo("details", { componentId: model.componentId })
                            }
                        }
                    }

                    // Empty state
                    Text {
                        anchors.centerIn: parent
                        visible: gridView.count === 0 && !appController.loading
                        text: "No components found"
                        font.pixelSize: CMTheme.fontSizeLarge
                        font.family: CMTheme.fontFamily
                        color: CMTheme.textMutedColor
                    }
                }
            }

            // Loading indicator
            Rectangle {
                anchors.centerIn: parent
                visible: appController.loading
                width: 200
                height: 60
                radius: CMTheme.radiusDefault
                color: CMTheme.surfaceColor

                Text {
                    anchors.centerIn: parent
                    text: "Loading components..."
                    font.pixelSize: CMTheme.fontSizeMedium
                    font.family: CMTheme.fontFamily
                    color: CMTheme.textColor
                }
            }
        }
    }
}
