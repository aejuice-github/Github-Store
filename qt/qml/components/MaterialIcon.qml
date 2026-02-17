import QtQuick 2.15
import "../singletons"

Text {
    id: icon
    property string iconName: ""
    property int iconSize: 24
    property color iconColor: CMTheme.textColor

    font.family: materialFont.name
    font.pixelSize: iconSize
    color: iconColor

    text: {
        var codepoints = {
            "extension": "\ue87b",
            "palette": "\ue40a",
            "brush": "\ue3ae",
            "auto_fix_high": "\ue663",
            "smart_toy": "\uf06c",
            "movie": "\ue404",
            "audiotrack": "\ue405",
            "download": "\uf090",
            "flash_on": "\ue3e7",
            "build": "\uf8cd",
            "settings": "\ue8b8",
            "code": "\ue86f",
            "computer": "\ue31e",
            "tune": "\ue429",
            "bolt": "\uea0b",
            "waves": "\ue176",
            "auto_awesome": "\ue65f",
            "memory": "\ue322",
            "terminal": "\ueb8e",
            "psychology": "\uea4a",
            "check_circle": "\uf0be",
            "deployed_code": "\uf720",
            "widgets": "\ue75e",
            "apps": "\ue5c3",
            "category": "\ue574",
            "package_2": "\uf8ff",
            "search": "\ue8b6",
            "favorite": "\ue87e",
            "arrow_back": "\ue5c4",
            "inventory_2": "\ue1a1",
            "install_desktop": "\ueb71",
            "add_circle": "\ue3ba",
            "grid_view": "\ue9b0",
            "update": "\ue923",
            "system_update": "\ue62a",
            "delete": "\ue872",
            "close": "\ue5cd"
        }
        return codepoints[iconName] || "\ue5c3"
    }

    FontLoader {
        id: materialFont
        source: "qrc:/resources/fonts/MaterialSymbolsRounded.ttf"
    }
}
