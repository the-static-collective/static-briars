# MOTO STYLUS 2022 — STATIC BRIARS First-Flight Preparation

**Status:** compiled / ABI-screened experimental Android app; model-specific Android 11/12 emulator tests tracked separately. No physical Motorola device has been accessed.

## The two 2022 families

| Motorola handset | Common identifiers | Original Android | CPU family |
| --- | --- | --- | --- |
| Moto G Stylus (2022), 4G | XT2211 | Android 11 (API 30) | MediaTek Helio G88, ARM |
| Moto G Stylus 5G (2022) | XT2215 | Android 12 (API 31) | Qualcomm Snapdragon 695, ARM64 |

The source's `briar-android/build.gradle` declares `minSdkVersion 21` and `targetSdkVersion 35`. The **combined STATICDEV + Network Witness** APK contains `lib/arm64-v8a/{libtor.so,liblyrebird.so}` and `lib/armeabi-v7a/{libtor.so,liblyrebird.so}`; an ARM app package should be architecturally compatible with both variants. Android version / ABI compatibility is **not** equivalent to device-specific Bluetooth, Tor or background reliability testing.

Reference devices:
- https://www.ifixit.com/Device/Motorola_Moto_G_Stylus_5G_%282022%29 (XT2215 vs XT2211)
- https://www.phonearena.com/phones/Motorola-Moto-G-Stylus-2022_id11895 (2022 4G Android 11 / Helio G88)
- https://en-us.support.motorola.com/app/answers/detail/a_id/172571/p/0 (2022 5G Android 12 / Snapdragon 695)

## Source and build custody

The correct app is **STATIC Briars DEV** (not official Briar). Its Android application ID is:

```text
org.staticcollective.briars.lab.debug
```

The combined build in [CI run 38434182777](https://github.com/the-static-collective/static-briars/actions/runs/38434182777) passed source-branch composition, Android APK assembly, AAPT package/label checks, and SHA-256 issuance. It is a debug-signed **non-production build** from 2026-10-13, not an audited or independently reproducible release. The signing key is temporary CI-created debug material; future CI APKs may have a different key and cannot be assumed to install as an update.

Recorded SHA-256 for the original combined APK:

```
ccb250fe321c5e7a447dd355bdf63288844579d1530228bd4dc134b5151ff702
```

The app can install *alongside* official Briar because the package IDs differ. No source merge or bootloader unlock is necessary.

## On-phone installation for a consenting, disposable test identity

1. On the Motorola open **Settings → About phone** to verify **model** (XT2211 or XT2215 family) and Android version. Updated firmware versions may differ from the originally shipped OS.
2. Save the **APK file**, not just the ZIP, to your phone's Downloads folder. From the trusted artifact link, if the source downloads a ZIP, extract the APK using Files.
3. In **Files → Downloads**, select the APK. Android may ask to allow this one source to install apps. If so, use the **Install unknown apps / Allow from this source** screen for the file manager used. Review the package prompt and cancel if the app name is anything other than **STATIC Briars DEV**.
4. After installing, immediately turn off the temporary **Allow from this source** setting. Do not globally disable Play Protect, root the phone, unlock its bootloader, or grant any device-owner privilege.
5. Launch STATIC Briars DEV. Use **only a disposable test identity**, not your usual Briar identity or meaningful contact list. Keep official Briar installed separately.
6. Enter **Settings → Connections**; inspect Road Compass (what is configured) and Network Witness (what the app currently reports about its own transport runtime). A green or active status doesn't prove remote reachability or message delivery.
7. For a first test, report only: whether installation succeeded, whether the app launched, the model (XT2211 vs XT2215), Android major version, and whether the Settings screen opened. Do **not** paste contact links, backup keys, IP addresses, nicknames or messages.

For an optional developer PC installation, Android Platform Tools support:

```sh
adb devices
adb install STATIC-Briars-Moto-G-Stylus-2022-TEST.apk
adb shell pm path org.staticcollective.briars.lab.debug
```

No ADB, laptop, USB debugging, or special bootloader steps are required for on-phone file installation.

## Limits and follow-ups

- Real Android 11/12 Moto compatibility is **not** proven by matching API levels and ARM libraries; the CI matrix only emulates Android API 30/31 on x86_64 if it succeeds.
- Two actual phones or a supported Briar Mailbox device remain necessary to prove offline recovery.
- The APK includes networking and messaging technology, but this specific derivative is **experimental**. Do not use it for sensitive or ordinary private communications.
- CI-generated debug signing identities are not stable. If a later test build refuses an in-place update, export **no real data** and use Android's normal app uninstall/reinstall flow only for disposable lab accounts.
- If you use a single everyday phone, complete simple installation/settings tests only; postpone direct test contact exchange until a second isolated test device is available.

Stop if Android identifies the APK as tampered or blocked. Do not disable platform security defenses merely to complete the experiment.
