# AGENTS.md

Automated setup and safety instructions for AI coding agents (**Antigravity, Codex, Claude Code, Gemini CLI, Cursor, Copilot**) running this repository on a user's Android TV / Google TV.

When the user clones this repository and asks you to "run this repo", "install this launcher", or "set up my TV", **follow every step below in exact order**. Do not skip the backup step, and do not build or install before asking the user's configuration choices.

---

## Step 1 — Environment & ADB Connection

1. **Create the Python environment** in `tools/venv` (if not already present):
   - macOS/Linux:
     ```bash
     python3 -m venv tools/venv
     tools/venv/bin/pip install -r tools/requirements.txt
     ```
   - Windows (PowerShell):
     ```powershell
     python -m venv tools\venv
     tools\venv\Scripts\pip install -r tools\requirements.txt
     ```
2. **Connect to the TV over ADB**:
   - Ask the user for their TV's IP address (or find it in `arp -a`) and confirm Developer Options → **USB debugging** (and **Network/ADB debugging** if present) is enabled on the TV.
   - Remind the user to accept the **"Allow USB debugging?"** prompt on the TV screen.
   - Verify connection and check SDK level (`minSdk` is `26`, Android 8.0+):
     ```bash
     TV_HOST=<tv-ip> tools/venv/bin/python tools/tv.py --probe
     TV_HOST=<tv-ip> tools/venv/bin/python tools/tv.py "getprop ro.build.version.sdk"
     ```
     *(On Windows PowerShell use `$env:TV_HOST="<tv-ip>"; tools\venv\Scripts\python.exe tools\tv.py ...`)*
   - If the TV is below SDK 26, not Android TV/Google TV (e.g. Tizen, webOS, Roku, VIDAA, SmartCast), or refuses sideloading (`INSTALL_FAILED_VERIFICATION_FAILURE`), stop immediately and inform the user.
   - If a sandbox blocks LAN access, have the user run `tools/relay.py <tv-ip>` locally and connect using `TV_HOST=127.0.0.1 TV_PORT=15555`.

---

## Step 2 — Mandatory Backup of Stock UI & Packages (BEFORE Any Changes)

Never modify or disable anything until the TV's current stock launcher and package state are backed up into `backup/`:

1. Detect the current stock HOME launcher and all HOME claimants:
   ```bash
   tools/tv.py "cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME" > backup/stock_home.txt
   tools/tv.py "cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.HOME" >> backup/stock_home.txt
   ```
2. Save complete device snapshots into `backup/` (`backup/` is gitignored and must stay local):
   - `backup/packages_all.txt` (`pm list packages -u`)
   - `backup/packages_enabled.txt` (`pm list packages -e --user 0`)
   - `backup/packages_disabled.txt` (`pm list packages -d --user 0`)
   - `backup/getprop.txt` (`getprop`)
   - `backup/settings_system.txt`, `backup/settings_secure.txt`, `backup/settings_global.txt`
3. Create TV-specific restore scripts in `backup/` (`backup/restore-stock.ps1` and `backup/restore-stock.sh`) that:
   - Re-enable the backed-up stock launcher package (`pm enable --user 0 <stock_pkg>`).
   - Restore the backed-up stock Home activity (`cmd package set-home-activity --user 0 <stock_component>`).
   - Re-enable any packages disabled in this session via `tools/restore.sh` / `tools/restore.ps1`.

---

## Step 3 — Detect Installed Apps & Ask User Before Building

Query the apps actually installed on the user's TV:
```bash
tools/tv.py "cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.LEANBACK_LAUNCHER"
tools/tv.py "pm list packages -3"
```

Then ask the user for their exact configuration **before editing `config.xml`, building, or installing**:

1. **Columns & Dock Size**: Ask how many columns / apps they want in the dock (`4`, `5`, `6`, or `7`).
2. **Exact Dock Apps**: Show the detected installed apps from their TV and ask them to pick **exact `N` apps** (matching the number of columns they chose) in exact left-to-right order.
   - Verify the count matches their chosen number of columns **1-to-1** (for example: 4 columns → exactly 4 dock apps; 5 columns → exactly 5 dock apps).
3. **Screensaver Photo**: Ask the user to provide a photo (local file path or image placed into this folder) for their TV screensaver.
   - **NEVER commit or push the user's screensaver photo to GitHub** (`screensaver.*` is gitignored in `.gitignore`).
   - If the user declines or skips providing a separate screensaver photo, the launcher automatically falls back to displaying the wallpaper on the screensaver.
4. **Optional Debloat**: Ask if they also want to safely disable bloatware/recommendations (`pm disable-user`, never `pm uninstall`), or keep stock packages untouched except replacing Home.

---

## Step 4 — Apply Configuration & Prepare Media

1. Update **only** `launcher/app/src/main/res/values/config.xml` for the user's TV:
   - Set `<integer name="columns">N</integer>` to the user's chosen column count `N`.
   - Set `<string-array name="dock_apps">` to hold the user's **exact `N` package names** in exact order, keeping `<bool name="fill_dock">true</bool>`.
   - Auto-detect the TV's Live TV / tuner package (if present) and configure `live_tv_package` and `hidden_apps` cleanly if appropriate.
   - **Keep the existing visual layout & styling defaults intact**:
     - `dock_glass = true`, `dock_border = true`, `dock_opacity = 45`, `dock_title = false`, `rows_below_dock = 0`, `glass_style = none`, `wallpaper_true_color = true`, `tile_menu = true`.
2. **Keep `wallpaper.png` as-is**: Do not overwrite or delete the repository's `wallpaper.png`.
3. **Prepare local screensaver image** (if provided by user): Copy it locally as `screensaver.png` (or `screensaver.jpg`) at the repo root (gitignored).

---

## Step 5 — Build, Install, Push Wallpaper & Screensaver

Run the safe build-and-install script (requires JDK 17+ and Android SDK platform 34):
- **macOS / Linux**:
  ```bash
  TV_HOST=<tv-ip> launcher/build-and-install.sh
  ```
  *(Pass `INPUT_KEY_SERVICE=1` / `HOME_KEY_SERVICE=1` only if needed for Google TV or Xiaomi remote button remapping.)*
- **Windows (PowerShell)**:
  ```powershell
  launcher\build-and-install.ps1 -TvHost <tv-ip>
  ```

What `build-and-install` handles automatically:
- Builds `app-release.apk` (verify size stays under `589,824 B`).
- Pushes and installs the APK safely with automatic stock Home verification and rollback guard.
- Disables the stock launcher only **after** `com.ashwithkumar.tvhome/.MainActivity` starts and is confirmed as Home.
- Pushes `wallpaper.png` from the repo root and the user's `screensaver.png` / `screensaver.jpg` (if provided) through `ThemeProvider` (`content://com.ashwithkumar.tvhome.theme/...`) and restarts Home cleanly so the media loads immediately.

If the user has a **CVTE board** (`com.cvte.*` packages): pass `STOCK_LAUNCHER=none` / `-StockLauncher none`, set Home via `cmd package set-home-activity`, reboot once, and verify Home and ADB return before doing anything else.

---

## Step 6 — Optional Debloat, Live Verification & Close ADB

1. **Debloat (only if requested)**:
   - Use `tools/disable.sh` / `tools/disable.ps1` in small batches.
   - **Never uninstall** (`pm uninstall` is forbidden; use `pm disable-user --user 0` only).
   - **Never disable** Android core packages (`com.android.systemui`, `com.android.permissioncontroller`, `com.android.packageinstaller`, `android.ext.*`, `com.android.tv.settings`, `com.android.settings`, etc.), TV middleware/input services, Bluetooth/remote pairing, Play Store, or `setupwraith` if it owns the active Wi-Fi network (`dumpsys wifi | grep -o 'cname=[^ ]*'`).
   - **Never launch** factory/test packages (`*fac*`, `*factory*`).
2. **Verify visually on the real TV**:
   ```bash
   tools/tv.py --screenshot shots/01-home.png
   ```
   Confirm in the screenshot:
   - The wallpaper (`wallpaper.png`) renders crisply.
   - Exact `N` dock columns and user-selected apps appear in order inside the thin-bordered frosted dock band.
   - Top-right container shows Inputs, Wi-Fi, and Settings icons cleanly.
   - Pressing **Inputs** opens the HDMI/input popup aligned cleanly above the dock.
   - Pressing **Back** on Home launches the screensaver (`ClockActivity`) with the user's screensaver image and top-right clock & date.
3. **Close ADB when done** (unless the user explicitly asks to keep ADB enabled):
   - Have the user turn off Network/Wireless/USB debugging in TV Developer Options and verify port closure:
     ```bash
     tools/adb-closed.py <tv-ip>
     ```

---

## Non-Negotiable Safety Rules

- **Disable, never uninstall**: Always reversible via `backup/restore-stock.*` and `tools/restore.*`.
- **Keep local files local**: Never commit `backup/`, `shots/`, `work/`, `tools/adbkey/`, or `screensaver.*`.
- **Launcher constraints**: Plain Java, no `INTERNET` permission, no telemetry/analytics, release APK under `589,824 B` (576 KB), `minSdk 26`.
