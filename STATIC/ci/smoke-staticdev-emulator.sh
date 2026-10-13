#!/usr/bin/env bash
# Offline, disposable emulator only. No accounts, contacts, or conversations.
set -Eeuo pipefail
LAB="org.staticcollective.briars.lab.debug"
OFFICIAL_DEBUG="org.briarproject.briar.android.debug"
ROOT="briar-android/build/outputs/apk"
LAB_APK="$(find "$ROOT/staticdev/debug" -maxdepth 1 -name '*.apk' -type f -print -quit)"
OFFICIAL_APK="$(find "$ROOT/official/debug" -maxdepth 1 -name '*.apk' -type f -print -quit)"
test -s "$LAB_APK" && test -s "$OFFICIAL_APK"
AAPT="${ANDROID_HOME:-/usr/local/lib/android/sdk}/build-tools/35.0.0/aapt"
test -x "$AAPT"
# Never pipe verbose AAPT into grep -q with pipefail: the consumer exits
# early and can SIGPIPE AAPT (exit 141) despite a correct identity.
LAB_BADGING="$("$AAPT" dump badging "$LAB_APK")"
OFFICIAL_BADGING="$("$AAPT" dump badging "$OFFICIAL_APK")"
[[ "$LAB_BADGING" == *"package: name='$LAB'"* ]] || {
    echo "::error::Unexpected lab APK application ID"
    exit 1
}
[[ "$OFFICIAL_BADGING" == *"package: name='$OFFICIAL_DEBUG'"* ]] || {
    echo "::error::Unexpected upstream debug application ID"
    exit 1
}
echo "PASS: distinct lab and upstream-debug identifiers in assembled APK manifests"

# Keep the application itself offline even though the CI host needed Internet
# for the Android SDK and dependencies. No login or personal data is created.
adb shell svc wifi disable
adb shell svc data disable || true
adb install -r "$OFFICIAL_APK" >/dev/null
adb install -r "$LAB_APK" >/dev/null
[[ "$(adb shell pm path "$OFFICIAL_DEBUG")" == *'package:'* ]]
[[ "$(adb shell pm path "$LAB")" == *'package:'* ]]
echo "PASS: both APKs installed side-by-side in a clean emulator"

launch() {
    local pkg="$1"
    local component="$pkg/org.briarproject.briar.android.splash.SplashScreenActivity"
    adb shell am force-stop "$pkg"
    local result
    result="$(timeout 65s adb shell am start -W -n "$component" 2>&1)"
    if ! grep -Fq 'Status: ok' <<<"$result"; then
        echo "::error::Startup was not acknowledged for $pkg"
        echo "$result"
        return 1
    fi
    sleep 5
    if [[ -z "$(adb shell pidof "$pkg" | tr -d '\r')" ]]; then
        echo "::error::Application process missing after launch: $pkg"
        return 1
    fi
    echo "PASS: $pkg launched and process stayed alive"
}
launch "$OFFICIAL_DEBUG"
launch "$LAB"
echo "PASS: offline startup smoke completed with zero test identities"
