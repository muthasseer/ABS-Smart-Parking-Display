# ABS Smart Parking Display — GitHub APK Build

This APK is a lightweight fullscreen WebView display for the ABS Smart Parking web application.

## Display
- Connect to the Guard Room computer / server over the same LAN or Wi-Fi.
- Load active gates from `/api/display/gates`.
- Select GATE 1, GATE 2, GATE 3, etc. and ENTRY or EXIT.
- Opens `/display?gate=...&side=...` in fullscreen.
- The web display automatically refreshes live vehicle information every second.
- The updated web display (V19) includes the World Trade Center Colombo logo, vehicle image, entry/exit time, date/time, Season/Free/Chargeable, charges, remarks, and AB SECURITAS company footer.

## GitHub
The repository is designed to build without Android Studio by using GitHub Actions.

Workflow: `.github/workflows/build-apk.yml`
Artifact: `ABS-Smart-Parking-Display-APK`

Build after uploading this project to GitHub, then open **Actions → Build ABS Smart Parking Display APK → Run workflow**.
