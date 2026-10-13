#!/usr/bin/env bash
# Moto G Stylus 2022 OS compatibility smoke: synthetic emulator, no user data.
set -Eeuo pipefail
LAB="org.staticcollective.briars.lab.debug"
APK="$(find briar-android/build/outputs/apk/staticdev/debug -type f -name '*.apk' -print -quit)"
test -s "$APK"
ACTUAL_API="$(adb shell getprop ro.build.version.sdk | tr -d '\r')"
[[ "$ACTUAL_API" == "30" || "$ACTUAL_API" == "31" ]] || {
  echo "::error::Expected Android 11 (API30) or 12 (API31); got $ACTUAL_API"
  exit 1
}
# This test operates on a deliberately offline emulator.
adb shell svc wifi disable
adb shell svc data disable || true
adb install "$APK" >/dev/null
[[ "$(adb shell pm path "$LAB")" == *'package:'* ]] || {
  echo "::error::Distinct lab package failed to install"
  exit 1
}
echo "PASS: package installed on Android API $ACTUAL_API without root"
COMPONENT="$LAB/org.briarproject.briar.android.splash.SplashScreenActivity"
RESULT="$(timeout 70s adb shell am start -W -n "$COMPONENT" 2>&1)"
if [[ "$RESULT" != *"Status: ok"* ]]; then
  echo "::error::Splash activity did not acknowledge launch"
  echo "$RESULT"
  exit 1
fi
sleep 7
if [[ -z "$(adb shell pidof "$LAB" | tr -d '\r')" ]]; then
  echo "::error::Lab process not active after launch"
  exit 1
fi
echo "PASS: Android API $ACTUAL_API app launch and process survival"
echo "No account, contact, message, live Tor circuit or Mailbox was exercised."
