# Contributing

Contributions to **Apple TV Launcher** are welcome under the [MIT License](LICENSE).

## Core Principles

- **Lightweight & Offline-First**: The launcher has no `INTERNET` permission, no analytics, no background polling, and targets low-memory Android TV devices (`minSdk 26`).
- **APK Size Budget**: Keep the release APK under **576 KB (`589,824 B`)**. Check after building:
  ```bash
  ./gradlew --no-daemon testReleaseUnitTest lintRelease assembleRelease
  ```
- **Reversible Device Changes**: Any TV tooling or debloat script must use `pm disable-user --user 0` (never `pm uninstall`), preserve core Android & TV input packages, and remain completely reversible.
- **Privacy First**: Never commit screenshots showing Wi-Fi SSIDs, personal names, emails, IPs, or device serials, and never commit personal `screensaver.*`, `backup/`, or `tools/adbkey/` files.

## Submitting Changes

1. Fork the repository and create a topic branch (`git checkout -b fix/my-change`).
2. Make focused, clean changes matching the existing Java and Python code style.
3. Verify `./gradlew --no-daemon testReleaseUnitTest lintRelease assembleRelease` passes with zero lint errors.
4. Test visually on a real Android TV / Google TV over ADB (`tools/tv.py --screenshot`) and attach before/after screenshots to your Pull Request.
