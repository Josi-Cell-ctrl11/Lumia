#!/usr/bin/env bash
set -euo pipefail
# Version fixe téléchargée depuis Google, indépendante de la dernière archive SDK.
curl --fail --location --retry 3 https://dl.google.com/android/repository/emulator-linux_x64-11237101.zip -o "$RUNNER_TEMP/luma-emulator.zip"
echo "cfd54ce620ad5f9f786682ecad88b8b9da7ac48827d57406cc2a31bb8f6e73fe  $RUNNER_TEMP/luma-emulator.zip" | sha256sum -c -
unzip -q -o "$RUNNER_TEMP/luma-emulator.zip" -d "$ANDROID_HOME"
"$ANDROID_HOME/emulator/emulator" -version
sdkmanager 'system-images;android-29;google_apis;x86_64'
echo no | avdmanager create avd --force --name luma-test --package 'system-images;android-29;google_apis;x86_64' --device pixel_2
"$ANDROID_HOME/emulator/emulator" -avd luma-test -no-window -gpu swiftshader_indirect -no-snapshot -noaudio -no-boot-anim > emulator.log 2>&1 &
trap 'adb emu kill || true' EXIT
timeout 180 adb wait-for-device
booted=false
for attempt in $(seq 1 90); do
  if [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = 1 ]; then booted=true; break; fi
  sleep 2
done
if [ "$booted" != true ]; then cat emulator.log; exit 1; fi
adb shell input keyevent 82
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
gradle --no-daemon connectedDebugAndroidTest
mkdir -p screenshots
adb pull /sdcard/Android/data/com.josias.luma/files/. screenshots/
