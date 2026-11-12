# Setup Prompt

Paste this into **Antigravity**, **Codex**, **Claude Code**, **Gemini CLI**, or **Cursor** inside the cloned repository folder (on a computer on the same Wi-Fi as your TV):

---

Read `AGENTS.md` and run the complete setup workflow on my Android TV / Google TV:

1. Set up `tools/venv` and connect to my TV over ADB (`tools/tv.py`).
2. Back up my current stock TV launcher, installed packages, properties, and settings into `backup/` first, and generate my restore scripts (`backup/restore-stock.sh` / `backup/restore-stock.ps1`).
3. Inspect the installed launchable apps on my TV and ask me:
   - How many columns / dock apps I want (4, 5, 6, or 7).
   - Which **exact N apps** (matching my column count) I want in the dock and in what order.
   - What photo I want to use for my TV screensaver (kept locally only — never committed to git).
   - Whether I want any bloatware disabled safely (`pm disable-user` only, never `pm uninstall`).
4. Update `launcher/app/src/main/res/values/config.xml` with my exact columns and dock apps while keeping the launcher styling and `wallpaper.png` as-is.
5. Build, install, verify Home handover (`launcher/build-and-install.sh` or `.ps1`), push `wallpaper.png` and my screensaver image, verify live on TV via screenshots (`tools/tv.py --screenshot`), and walk me through closing ADB when done.
