# Component Manager

Qt/QML desktop app for browsing, installing, and managing AEJuice components (plugins, scripts, extensions).

## CLI Flags

| Flag | Description |
|------|-------------|
| `--install-required` | Headless mode. Reads manifest, installs all components with `"required": true` that aren't already installed, exits |
| `--install id1,id2` | Headless mode. Installs specific components by ID, exits |
| *(no flags)* | Normal GUI mode |

Headless modes log to `%TEMP%/cm_install.log` and exit with code 0 on success, 1 on failure.

## Key Features

### Install & Update
- Download and install plugins/scripts/extensions from a remote manifest
- Automatic version comparison — shows update badge when newer version available
- "Update All" bulk action for outdated components
- Uninstall tracking (local record removal)
- Drag-and-drop install for `.jsx`, `.jsxbin`, `.aex`, `.ofx`, `.zxp` files

### UAC Elevation (Windows)
- Single UAC prompt per session via an elevated helper process
- Main app launches `ComponentManager.exe --pipe-helper <pipe>` elevated on first admin install
- Subsequent installs reuse the same helper over a named pipe — no repeated UAC prompts
- Helper auto-relaunches if it dies

### Rollout System
- Each component can have `"rollout_percentage": 50` to show it to only 50% of users
- Random roll (1–100) assigned per component on first encounter, persisted in `rollout.json`
- Re-rolls only when the percentage value changes in the manifest

### Self-Update
- Manifest declares `appVersion` and `appUpdateUrl`
- Update banner shown when a newer version is detected
- Downloads new exe, runs a batch script to replace itself, relaunches

### Drag-and-Drop
- Native Windows OLE `IDropTarget` integration
- Auto de-elevation: if app is launched as admin, it relaunches as normal user so UIPI doesn't block drops

### Search & Filter
- Live search by name, description, author, tags
- Filter by category, compatible app, author, price (All/Free/Paid)
- Grid and list view toggle

### Favorites & Installed
- Favorite any component, persisted locally
- Installed Apps screen with update indicators
- Bulk uninstall

### Themes
5 dark themes: **ocean**, **purple**, **forest**, **slate**, **amber**

## Manifest Format

Fetched from `https://install.aejuice.com/Products.json` with bundled fallback.

### Top-Level

```json
{
  "version": "1.0",
  "appVersion": "1.1.0",
  "appUpdateUrl": "https://install.aejuice.com/ComponentManager.exe",
  "categories": ["AI", "Animation", "Effects"],
  "components": [...]
}
```

### Component

```json
{
  "id": "ascii",
  "name": "ASCII",
  "type": "plugin",
  "description": "...",
  "tooltip": "...",
  "version": "2.0.0",
  "author": "AEJuice",
  "category": "Stylize",
  "tags": ["ascii", "stylize"],
  "icon": "",
  "screenshots": [],
  "price": 0,
  "required": false,
  "rollout_percentage": 100,
  "runnable": false,
  "runCommand": "",
  "changelog": "### 2.0.0\n- New feature",
  "compatibleApps": ["After Effects", "Premiere Pro"],
  "dependencies": [],
  "platforms": {
    "windows": {
      "url": "https://install.aejuice.com/Plugins/Win/Adobe/ASCII.aex",
      "sha256": "",
      "size": 3500000,
      "installPath": "%PROGRAMFILES%/Adobe/Common/Plug-ins/7.0/MediaCore/",
      "requiresAdmin": true,
      "fileName": "AEJuice ASCII.aex"
    }
  }
}
```

### Component Fields

| Field | Type | Description |
|-------|------|-------------|
| `id` | string | Unique identifier |
| `name` | string | Display name |
| `type` | string | `plugin`, `script`, `extension`, `software` |
| `version` | string | Semantic version |
| `author` | string | Author name |
| `category` | string | Category from top-level list |
| `price` | int | Price in cents (0 = free) |
| `required` | bool | Auto-install via `--install-required` |
| `rollout_percentage` | int | 1–100, controls visibility to users |
| `runnable` | bool | Can be launched directly |
| `compatibleApps` | array | Compatible host apps |
| `platforms` | object | Per-platform download assets |

### Platform Asset Fields

| Field | Type | Description |
|-------|------|-------------|
| `url` | string | Download URL |
| `sha256` | string | SHA-256 checksum (optional) |
| `size` | int | File size in bytes |
| `installPath` | string | Target directory. Supports `%PROGRAMFILES%` and `%COMMONFILES%` placeholders (resolved at runtime via system API). Use `scriptui` or `scripts` for AE-relative paths |
| `requiresAdmin` | bool | Needs elevated permissions to install |
| `fileName` | string | Name of the installed file |
| `wait_for_finish` | array | Process names that must be closed before overwriting (e.g. `["AfterFX.exe"]`) |
| `silentArgs` | array | Arguments for silent/hidden install of exe/msi installers. Examples: `["/S"]` (NSIS), `["/quiet", "/norestart"]` (MSI), `["/VERYSILENT", "/NORESTART"]` (Inno Setup). The app runs the installer with these args and waits for exit code 0 |

## Local Storage

All persisted to `%APPDATA%/AEJuice/AEJuice App Store/`:

| File | Contents |
|------|----------|
| `installed.json` | Map of component ID → installed version |
| `favorites.json` | Array of favorited component IDs |
| `rollout.json` | Map of component ID → `{roll, percentage}` |
| `settings.ini` | Theme color, auto-update preference |

## Build

```bash
cd qt
cmake -B build
cmake --build build --config Debug
./build/ComponentManager.exe
```

Requires Qt 6 (Core, Quick, QuickControls2, Network). Windows links `shell32`, `advapi32`, `ole32`.
