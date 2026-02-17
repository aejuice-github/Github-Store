import QtQuick 2.15
import QtQuick.Controls 2.15
import "../singletons"

Rectangle {
    id: installScreen
    objectName: "manualInstallScreen"
    color: CMTheme.backgroundColor

    // Drop zone centered
    Column {
        anchors.centerIn: parent
        spacing: CMTheme.spacingXLarge

        // Dashed border drop area
        Rectangle {
            width: 480
            height: 320
            radius: CMTheme.radiusLarge
            color: dropZoneArea.containsMouse ? CMTheme.surfaceContainerHighColor : "transparent"
            border.color: CMTheme.borderColor
            border.width: 0

            Canvas {
                id: dashedBorder
                anchors.fill: parent

                onPaint: {
                    var ctx = getContext("2d")
                    ctx.clearRect(0, 0, width, height)
                    ctx.strokeStyle = CMTheme.borderColor
                    ctx.lineWidth = 2
                    ctx.setLineDash([12, 8])
                    ctx.beginPath()

                    var r = CMTheme.radiusLarge
                    ctx.moveTo(r, 0)
                    ctx.lineTo(width - r, 0)
                    ctx.arcTo(width, 0, width, r, r)
                    ctx.lineTo(width, height - r)
                    ctx.arcTo(width, height, width - r, height, r)
                    ctx.lineTo(r, height)
                    ctx.arcTo(0, height, 0, height - r, r)
                    ctx.lineTo(0, r)
                    ctx.arcTo(0, 0, r, 0, r)

                    ctx.stroke()
                }
            }

            Column {
                anchors.centerIn: parent
                spacing: CMTheme.spacingLarge

                // Download icon
                Text {
                    anchors.horizontalCenter: parent.horizontalCenter
                    text: "\u2913"
                    font.pixelSize: 64
                    color: CMTheme.accentColor
                }

                // Title
                Text {
                    anchors.horizontalCenter: parent.horizontalCenter
                    text: "Drop files here to install"
                    font.pixelSize: CMTheme.fontSizeXLarge
                    font.bold: true
                    font.family: CMTheme.fontFamily
                    color: CMTheme.textColor
                }

                // Supported formats
                Text {
                    anchors.horizontalCenter: parent.horizontalCenter
                    text: ".jsx  .jsxbin  .aex  .ofx  .zxp"
                    font.pixelSize: CMTheme.fontSizeDefault
                    font.family: CMTheme.fontFamily
                    color: CMTheme.textMutedColor
                }
            }

            MouseArea {
                id: dropZoneArea
                anchors.fill: parent
                hoverEnabled: true
                cursorShape: Qt.PointingHandCursor
                onClicked: appController.dragDrop.browseFiles()
            }

            DropArea {
                anchors.fill: parent
                keys: ["text/uri-list"]

                onEntered: {
                    appController.dragDrop.setDragActive(true)
                    drag.accepted = true
                }
                onExited: {
                    appController.dragDrop.setDragActive(false)
                }
                onDropped: {
                    appController.dragDrop.setDragActive(false)
                    var files = []
                    for (var i = 0; i < drop.urls.length; i++) {
                        var path = drop.urls[i].toString()
                        if (path.startsWith("file:///"))
                            path = path.substring(8)
                        files.push(path)
                    }
                    appController.dragDrop.handleDroppedFiles(files)
                }
            }
        }

        // Browse Store button
        Rectangle {
            anchors.horizontalCenter: parent.horizontalCenter
            width: browseRow.width + CMTheme.spacingXLarge * 2
            height: 40
            radius: CMTheme.radiusDefault
            color: browseArea.containsMouse ? CMTheme.surfaceContainerHighColor : CMTheme.surfaceContainerColor

            Row {
                id: browseRow
                anchors.centerIn: parent
                spacing: CMTheme.spacingDefault

                Text {
                    text: "\u2302"
                    font.pixelSize: 16
                    color: CMTheme.textColor
                    anchors.verticalCenter: parent.verticalCenter
                }

                Text {
                    text: "Browse Store"
                    font.pixelSize: CMTheme.fontSizeDefault
                    font.family: CMTheme.fontFamily
                    color: CMTheme.textColor
                    anchors.verticalCenter: parent.verticalCenter
                }
            }

            MouseArea {
                id: browseArea
                anchors.fill: parent
                hoverEnabled: true
                cursorShape: Qt.PointingHandCursor
                onClicked: {
                    if (stackView)
                        stackView.pop()
                    else
                        appController.navigateBack()
                }
            }
        }
    }
}
