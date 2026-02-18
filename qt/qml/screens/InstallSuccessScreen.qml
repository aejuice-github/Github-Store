import QtQuick 2.15
import QtQuick.Controls 2.15
import "../singletons"
import "../components"

Rectangle {
    id: successScreen
    color: CMTheme.backgroundColor

    property string message: ""
    property var installedTypes: []

    function buildInstructions() {
        var lines = []
        var types = installedTypes || []

        var hasScript = types.indexOf("jsx") >= 0 || types.indexOf("jsxbin") >= 0
        var hasPlugin = types.indexOf("aex") >= 0
        var hasOfx = types.indexOf("ofx") >= 0
        var hasExtension = types.indexOf("zxp") >= 0

        if (hasScript) {
            lines.push("1. Restart After Effects")
            lines.push("2. Go to Window menu at the top")
            lines.push("3. Find the script at the bottom of the list")
        }
        if (hasPlugin) {
            lines.push(hasScript ? "" : "1. Restart After Effects")
            lines.push("Find the plugin in Effect menu")
        }
        if (hasOfx) {
            lines.push("Restart your host application to load the OFX plugin")
        }
        if (hasExtension) {
            lines.push("Restart your host application to load the extension")
        }

        if (lines.length === 0) {
            lines.push("Restart the host application to use the installed files")
        }

        return lines.filter(function(l) { return l !== "" }).join("\n")
    }

    Column {
        anchors.centerIn: parent
        spacing: CMTheme.spacingLarge
        width: 400

        // Checkmark circle
        Rectangle {
            anchors.horizontalCenter: parent.horizontalCenter
            width: 100
            height: 100
            radius: 50
            color: Qt.rgba(CMTheme.successColor.r, CMTheme.successColor.g, CMTheme.successColor.b, 0.15)

            MaterialIcon {
                anchors.centerIn: parent
                iconName: "check_circle"
                iconSize: 64
                iconColor: CMTheme.successColor
            }

            // Scale animation
            scale: 0
            Component.onCompleted: scaleAnim.start()

            NumberAnimation on scale {
                id: scaleAnim
                from: 0
                to: 1
                duration: 400
                easing.type: Easing.OutBack
            }
        }

        // Title
        Text {
            anchors.horizontalCenter: parent.horizontalCenter
            text: successScreen.message || "Installation Complete"
            font.pixelSize: CMTheme.fontSizeTitle
            font.bold: true
            font.family: CMTheme.fontFamily
            color: CMTheme.textColor
        }

        // Instructions
        Rectangle {
            anchors.horizontalCenter: parent.horizontalCenter
            width: parent.width
            height: instructionsColumn.height + CMTheme.spacingLarge * 2
            radius: CMTheme.radiusDefault
            color: CMTheme.surfaceContainerColor

            Column {
                id: instructionsColumn
                anchors.centerIn: parent
                width: parent.width - CMTheme.spacingLarge * 2
                spacing: CMTheme.spacingDefault

                Text {
                    text: "How to use"
                    font.pixelSize: CMTheme.fontSizeMedium
                    font.bold: true
                    font.family: CMTheme.fontFamily
                    color: CMTheme.textColor
                }

                Text {
                    width: parent.width
                    text: buildInstructions()
                    font.pixelSize: CMTheme.fontSizeDefault
                    font.family: CMTheme.fontFamily
                    color: CMTheme.textMutedColor
                    wrapMode: Text.WordWrap
                    lineHeight: 1.6
                }
            }
        }

        // Install More button
        Rectangle {
            anchors.horizontalCenter: parent.horizontalCenter
            width: 200
            height: 44
            radius: CMTheme.radiusDefault
            color: installMoreArea.containsMouse ? CMTheme.accentColor : CMTheme.surfaceContainerHighColor

            Row {
                anchors.centerIn: parent
                spacing: CMTheme.spacingSmall

                MaterialIcon {
                    anchors.verticalCenter: parent.verticalCenter
                    iconName: "add_circle"
                    iconSize: 20
                    iconColor: installMoreArea.containsMouse ? "#FFFFFF" : CMTheme.textColor
                }

                Text {
                    anchors.verticalCenter: parent.verticalCenter
                    text: "Install More"
                    font.pixelSize: CMTheme.fontSizeMedium
                    font.family: CMTheme.fontFamily
                    font.bold: true
                    color: installMoreArea.containsMouse ? "#FFFFFF" : CMTheme.textColor
                }
            }

            MouseArea {
                id: installMoreArea
                anchors.fill: parent
                hoverEnabled: true
                cursorShape: Qt.PointingHandCursor
                onClicked: appController.navigateBack()
            }
        }
    }
}
