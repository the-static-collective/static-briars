# STATICDEV 001 — independently identifiable Android laboratory build

Status: SOURCE EXPERIMENT. A passing Android Java compile is **not** an assembled APK or a device/security test.

## Goal

Create a separately installable `staticdevDebug` flavor for reviewable laboratory testing, without changing Briar's original `official` Android flavor or trusted-source history.

## Specific differences

- Android application ID: `org.staticcollective.briars.lab.debug` (not the Briar Project's package).
- Distinct launcher name and icon: **STATIC Briars DEV**.
- Existing `@string/app_package` used by in-app explicit settings Intents points to the lab app rather than official Briar.
- Lab variant removes NavDrawerActivity's exported `briar://` and incoming share Intent filters and marks it unexported, to avoid intercepting contact invitations meant for official Briar. These removals must be verified in the merged manifest and on a test device; no claim of complete Intent isolation is made.
- `staticdevRelease` is disabled, and only `assembleStaticdevDebug` is used.
- The independently maintained source lineage and original licensing remain intact.

`applicationId` controls Android app data separation. It does **not** prove that every link handler, interaction with third-party apps, device-specific service or background API is isolated. Investigate all package-name assumptions and externally exported intent filters before actual personal-device use.

## CI and safety boundary

The `staticdev-apk` workflow verifies the distinct manifest package and label with `aapt`, computes SHA-256, and uploads only a 2-day lab artifact if assembly succeeds. Artifacts are unsigned as a *release* but carry standard ephemeral Android debug signing; CI signing identity is not stable or independently trustworthy. **No GitHub Release is published.**

An APK obtained from an unrelated workflow, mirrored host, private message, or third-party source is not trusted by these checks.

### NOT ESTABLISHED

- Device smoke tests, official APK reproduction, independent security review, compatible stable signing, Briar protocol interoperability, or end-to-end privacy claims.
- Whether app links, exported activities and package-specific integrations need additional isolation before safe side-by-side use.
- Whether an Android debug APK will install on all Android API 21+ devices.

Never load real identities, real conversations, or important contacts into this experiment.

## Future gates

1. CI builds the exact lab APK, archives a build hash, and verifies independent manifest label/ID.
2. Clean Android emulator: launch, account creation, settings navigation, Tor plugin activation, Mailbox status, app link behavior.
3. Two sacrificial devices and test identities only: Wi-Fi / Bluetooth / Tor path transitions; battery and reconnect tests.
4. Confirm all original GPL notices, branding, signing custody and independent release authority before any wide distribution.
