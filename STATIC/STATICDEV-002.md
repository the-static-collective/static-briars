# STATICDEV 002 — offline emulator installation witness

This separate branch stacks on STATICDEV 001. It is an experimental disposable Android emulator workflow, not an endorsement to install an experimental cryptographic messenger on a real device.

### The gate

1. Build upstream `officialDebug` and `staticdevDebug` from their shared attributed source.
2. Assert that the assembled APKs have *different* application IDs.
3. Boot a clean, temporary Android API 35 emulator. Turn off Wi-Fi and mobile data before launching either app.
4. Install both apps into that emulator and ensure their packages coexist.
5. Launch each app's own splash activity and check that its process stays alive for a short interval.
6. Exit without creating a Briar account, contacts, private messages, Tor use, or exporting logs/screenshots.

A successful run would establish installation/launch coexistence on that specific emulator, **not** security, reproducibility, remote interoperability, data persistence, physical radio operation, Mailbox delivery, or proper end-to-end identity separation. Some native dependencies may be architecture-specific; emulator failures must not be generalized to physical Android devices.

The third-party emulator action, platform and dependencies are used only in an ephemeral CI runner. Explicit release and key custody review are still necessary before any public release.

This experiment intentionally contains no `upload-artifact` step and never publishes an APK.
