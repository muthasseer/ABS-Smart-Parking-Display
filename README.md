# ABS Smart Parking Display — GitHub APK Build

This project is designed for **GitHub only**. You do **not** need Android Studio to build the APK.

The Android device / Android TV connects to the same LAN or Wi‑Fi as the Gate Guard Room computer running the ABS Smart Parking Flask web application.

## What the Android app does

- Connect to the Guard Room / server IP over LAN or Wi‑Fi
- Enter the web port (default `5000`)
- Load the available gate list from the web application
- Select `GATE 1`, `GATE 2`, `GATE 3`, etc.
- Select `ENTRY` or `EXIT`
- Open the large fullscreen vehicle display
- Show vehicle number, vehicle image, entry/exit time, parking type, charges and remarks
- Refresh live display data automatically
- Keep the Android screen awake

## Example network

Gate 1 Guard Room PC:

`192.168.1.7`

Web application:

`http://192.168.1.7:5000`

Entry display:

`http://192.168.1.7:5000/display?gate=1&side=entry`

Exit display:

`http://192.168.1.7:5000/display?gate=1&side=exit`

The Android app can load the gate list directly from `/api/display/gates` and stores the selected server, gate and side on the device.

## Build the APK using GitHub

1. Create a new GitHub repository.
2. Upload the contents of this project to the repository. Keep the `.github/workflows/build-apk.yml` file.
3. Open the repository **Actions** tab.
4. Open **Build ABS Smart Parking Display APK**.
5. Click **Run workflow**.
6. Wait for the build to finish.
7. Open the completed workflow run.
8. In **Artifacts**, download **ABS-Smart-Parking-Display-APK**.
9. The downloaded ZIP contains `app-debug.apk`.
10. Copy the APK to the Android display device and install it.

The workflow installs Java 17, Android SDK platform 35 / build tools 35.0.0, and Gradle 8.10.2 on the GitHub runner, then builds and uploads the APK as an artifact. Gradle recommends the Wrapper for standardized builds, but this project intentionally uses GitHub Actions' Gradle setup so the repository can be uploaded and built without Android Studio or a locally installed Gradle. citeturn638513search1turn903824search0

## Important

The display device and Gate Guard Room PC must be on the same reachable LAN/Wi‑Fi network. The Flask server must listen on the PC's LAN interface, not only on `127.0.0.1`.

Example Flask bind:

`0.0.0.0:5000`

Do not expose the parking server directly to the public internet.

## Project files

- `app/` — Android application source
- `.github/workflows/build-apk.yml` — GitHub Actions APK build
- `BUILD_APK.txt` — quick build guide
- `gradle.properties`, `build.gradle`, `settings.gradle` — Gradle project files
