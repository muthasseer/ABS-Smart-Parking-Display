@echo off
setlocal
if not exist gradlew.bat (
  echo Gradle wrapper is not included in this source package.
  echo Open this folder in Android Studio and use Build ^> Build APK(s).
  echo If Gradle is installed globally, you can run: gradle :app:assembleDebug
  exit /b 0
)
call gradlew.bat :app:assembleDebug
