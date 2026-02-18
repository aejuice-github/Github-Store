import QtQuick 2.15
import QtQuick.Controls 2.15
import "../singletons"

ComboBox {
    id: dropdown
    height: 32
    width: 160

    property string label: ""

    MouseArea {
        anchors.fill: parent
        cursorShape: Qt.PointingHandCursor
        acceptedButtons: Qt.NoButton
    }

    delegate: ItemDelegate {
        width: dropdown.width
        contentItem: Text {
            text: modelData
            font.pixelSize: CMTheme.fontSizeDefault
            font.family: CMTheme.fontFamily
            color: CMTheme.textColor
        }
        background: Rectangle {
            color: highlighted ? CMTheme.surfaceContainerHighColor : CMTheme.surfaceColor
        }
        highlighted: dropdown.highlightedIndex === index
    }

    contentItem: Text {
        leftPadding: CMTheme.spacingDefault
        text: dropdown.displayText
        font.pixelSize: CMTheme.fontSizeDefault
        font.family: CMTheme.fontFamily
        color: CMTheme.textColor
        verticalAlignment: Text.AlignVCenter
        elide: Text.ElideRight
    }

    background: Rectangle {
        radius: CMTheme.radiusSmall
        color: CMTheme.surfaceContainerColor
        border.color: CMTheme.borderColor
        border.width: 1
    }

    popup: Popup {
        y: dropdown.height
        width: dropdown.width
        implicitHeight: contentItem.implicitHeight + 2
        padding: 1

        contentItem: ListView {
            clip: true
            implicitHeight: contentHeight
            model: dropdown.popup.visible ? dropdown.delegateModel : null
            ScrollIndicator.vertical: ScrollIndicator {}
        }

        background: Rectangle {
            radius: CMTheme.radiusSmall
            color: CMTheme.surfaceColor
            border.color: CMTheme.borderColor
            border.width: 1
        }
    }
}
