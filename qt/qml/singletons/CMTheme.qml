pragma Singleton
import QtQuick 2.15

QtObject {
    id: theme

    // Active theme color name
    property string activeTheme: "ocean"

    // Semantic colors (dark only)
    readonly property color accentColor: schemes[activeTheme].accent
    readonly property color backgroundColor: schemes[activeTheme].background
    readonly property color surfaceColor: schemes[activeTheme].surface
    readonly property color surfaceContainerColor: schemes[activeTheme].surfaceContainer
    readonly property color surfaceContainerHighColor: schemes[activeTheme].surfaceContainerHigh
    readonly property color textColor: schemes[activeTheme].text
    readonly property color textMutedColor: schemes[activeTheme].textMuted
    readonly property color borderColor: schemes[activeTheme].border
    readonly property color errorColor: "#FFB4AB"
    readonly property color successColor: "#4CAF50"
    readonly property color warningColor: "#FFB74D"
    readonly property color infoColor: "#64B5F6"

    // Font sizes
    readonly property int fontSizeSmall: 10
    readonly property int fontSizeDefault: 12
    readonly property int fontSizeMedium: 14
    readonly property int fontSizeLarge: 16
    readonly property int fontSizeXLarge: 20
    readonly property int fontSizeTitle: 24

    // Font family
    readonly property string fontFamily: "Segoe UI"

    // Spacing
    readonly property int spacingSmall: 4
    readonly property int spacingDefault: 8
    readonly property int spacingMedium: 12
    readonly property int spacingLarge: 16
    readonly property int spacingXLarge: 24

    // Radii
    readonly property int radiusSmall: 4
    readonly property int radiusDefault: 8
    readonly property int radiusMedium: 12
    readonly property int radiusLarge: 16

    // Card
    readonly property int cardHeight: 300

    // Sidebar
    readonly property int sidebarWidth: 200

    // Icon sizes
    readonly property int iconSizeSmall: 16
    readonly property int iconSizeMedium: 24
    readonly property int iconSizeLarge: 32

    // Theme color preview (for selector)
    readonly property var themePreviewColors: ({
        "ocean": "#35AEFF",
        "purple": "#CFBCFF",
        "forest": "#9CD1BD",
        "slate": "#B4C7D9",
        "amber": "#FFB870"
    })

    // 5 dark color schemes ported from Kotlin Color.kt
    readonly property var schemes: ({
        "ocean": {
            accent: "#35AEFF",
            accentDark: "#034B71",
            background: "#101417",
            surface: "#1C2024",
            surfaceContainer: "#1C2024",
            surfaceContainerHigh: "#272A2E",
            text: "#E0E3E8",
            textMuted: "#8C9198",
            border: "#42474D",
            primary: "#35AEFF",
            onPrimary: "#003350",
            primaryContainer: "#034B71",
            secondary: "#B8C8D9",
            outline: "#8C9198"
        },
        "purple": {
            accent: "#CFBCFF",
            accentDark: "#4F378A",
            background: "#141316",
            surface: "#201F22",
            surfaceContainer: "#201F22",
            surfaceContainerHigh: "#2B292D",
            text: "#E6E1E6",
            textMuted: "#948F99",
            border: "#49454E",
            primary: "#CFBCFF",
            onPrimary: "#381E72",
            primaryContainer: "#4F378A",
            secondary: "#CBC2DB",
            outline: "#948F99"
        },
        "forest": {
            accent: "#9CD1BD",
            accentDark: "#1C4F41",
            background: "#0F1512",
            surface: "#1B211E",
            surfaceContainer: "#1B211E",
            surfaceContainerHigh: "#262C28",
            text: "#DFE4E0",
            textMuted: "#89938C",
            border: "#404943",
            primary: "#9CD1BD",
            onPrimary: "#00382B",
            primaryContainer: "#1C4F41",
            secondary: "#B2CDBF",
            outline: "#89938C"
        },
        "slate": {
            accent: "#B4C7D9",
            accentDark: "#394654",
            background: "#111416",
            surface: "#1D2022",
            surfaceContainer: "#1D2022",
            surfaceContainerHigh: "#282A2D",
            text: "#E2E2E5",
            textMuted: "#8D9199",
            border: "#43474E",
            primary: "#B4C7D9",
            onPrimary: "#1F2F3D",
            primaryContainer: "#394654",
            secondary: "#BEC6D5",
            outline: "#8D9199"
        },
        "amber": {
            accent: "#FFB870",
            accentDark: "#6A3C00",
            background: "#18130E",
            surface: "#241F1A",
            surfaceContainer: "#241F1A",
            surfaceContainerHigh: "#2F2A24",
            text: "#ECE1DA",
            textMuted: "#9D8E82",
            border: "#51443A",
            primary: "#FFB870",
            onPrimary: "#4B2800",
            primaryContainer: "#6A3C00",
            secondary: "#E2C1A3",
            outline: "#9D8E82"
        }
    })

    function setTheme(themeName) {
        if (schemes.hasOwnProperty(themeName)) {
            activeTheme = themeName
        }
    }
}
