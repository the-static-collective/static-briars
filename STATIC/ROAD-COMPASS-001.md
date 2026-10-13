# ROAD COMPASS 001 — First Native Android Interface

**Status:** Android UI source implementation + state/resource CI proof; physical-device UX and complete Android APK build unverified.

## Purpose

Briar's settings already expose separate Bluetooth, Wi-Fi, and Tor preferences through `ConnectionsManager` LiveData. A person looking at a switch can wrongly infer that a usable network route exists, or that a message was delivered.

This experiment makes local configuration legible while leaving Briar's cryptographic and synchronization subsystems unchanged.

## UI

Navigate to **Settings → Connections → Road Compass**.

- A read-only Bluetooth row reflects Briar's local enabled preference.
- A read-only Wi-Fi row reflects Briar's local enabled preference.
- A Tor row reflects local Tor enabled, mobile data availability, and charging-only constraints; combinations appear distinctly.
- A human-facing connection guide explains that setting enabled is **not** a successfully established route, nearby peer, or delivered message.
- A Mailbox shortcut opens **Briar's existing MailboxActivity**. The UI never creates or sends a message.

Values update through existing settings LiveData, not via new service probes, DNS queries, telemetry, or contact enumeration.

## Scope boundaries

- **Tor configured != Tor circuit live**
- **Wi-Fi enabled != nearby contact**
- **Bluetooth enabled != sufficient Android permissions**
- **Mailbox configured != mailbox delivery**
- **Connected contact != any particular message delivered**
- No new permissions, network sockets, background jobs, databases, analytics or encryption code.
- This is a downstream Static experiment, not an official Briar app release; `applicationId` is unchanged in this source slice and MUST be rebranded before distributing an independent APK.

## Implementation

`RoadCompassState.java`: plain-Java classification of enabled/disabled/unknown local settings, including the combined Tor restriction. Its pure logic has an Android JUnit test and an Android-free executable smoke harness.

`ConnectionsFragment.java`: view-only preference summaries and explicit user-initiated Mailbox and guide actions.

`settings_connections.xml`, `strings_static_compass.xml`: Android Preference UI and text resources; additions deliberately avoid editing existing translations and provide English fallback until translation work.

`.github/workflows/static-road-compass.yml`: tests 27 Tor combinations, three local states, Android XML wellformedness and resource references, and checks observers and original Briar ancestry.

## Verify

Source checks (no SDK):

```sh
python3 STATIC/ci/check-road-compass.py
OUT=$(mktemp -d)
javac -d "$OUT" \
  briar-android/src/main/java/org/briarproject/briar/android/settings/RoadCompassState.java \
  STATIC/ci/RoadCompassSmoke.java
java -cp "$OUT" RoadCompassSmoke
rm -rf "$OUT"
```

Full Android build is distinct and requires original Briar's Android build dependencies and SDK. Candidate command after setting up its supported toolchain:

```sh
./gradlew --no-daemon :briar-android:compileDebugJavaWithJavac
```

That check does not imply release reproducibility, threat-model coverage, or tested devices.

## Next experiments

- **002:** real *transport* availability displayed as verified separate signals from Briar's own existing connection registry, with careful handling of permission status and no IP disclosures.
- **003:** comprehensible Mailbox connection and offline-delivery state, tested with two consented Android devices.
- **004:** contact onboarding navigation improvement without modifying handshake identity or trust UX.
- **005:** identify, replace or retain source branding and verify safe side-by-side install; independent APK builds must never masquerade as official Briar binaries.
