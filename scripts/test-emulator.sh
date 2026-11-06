#!/usr/bin/env bash
set -euo pipefail
# Le runner possède déjà l’émulateur : ne pas télécharger sa dernière version.
test -x "$ANDROID_HOME/emulator/emulator"
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
