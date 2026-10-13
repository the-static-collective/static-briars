# STATIC BRIARS

**Independent experimental continuation of Briar.** This project is not affiliated with or endorsed by the Briar Project.

## Source continuity

The canonical upstream is [Briar on GitLab](https://code.briarproject.org/briar/briar), with a [read-only GitHub mirror](https://github.com/briar/briar). We preserve its source history, upstream copyright notices, Git submodule pointers and GPL obligations.

**Pinned initial baseline:** upstream signed tag `release-1.5.22`, resolving to commit `d03d6fcd776349ec6c8b7d22035e9bb04aa1787c` (checked 2026-10-13). **The source is NOT in this main branch until a distinct import branch is confirmed.**

The import workflow targets:
- `upstream/release-1.5.22`: original Git history ending exactly at the pinned upstream commit, with no Static changes;
- `static/continuation-001`: descendant branch containing provenance and future experiments.

The default `main` branch holds the import machinery and project-level documentation; it is intentionally **not** advertised as an Android source build or merged fork. Source work should branch from `static/continuation-001` and be reviewed against that lineage. Imports never rewrite main or force-push the source branches.

## What we are building

A locally owned, offline-first human communication platform that explores Briar's existing Tor, Bluetooth, Wi-Fi, Mailbox and headless infrastructure. Source experiments may eventually explore ergonomic contact onboarding, battery diagnostics, continuity and restricted reLATTE invitations.

Related reLATTE prototypes:
- [ROADWORKS 001](https://github.com/the-static-collective/reLATTE/pull/111): consented transport selection and idempotence.
- [Briar Continuation 001](https://github.com/the-static-collective/reLATTE/pull/112): consent-gated human invite via documented Briar Headless localhost API.
- [Briar Continuation 002](https://github.com/the-static-collective/reLATTE/pull/114): earlier source bootstrap (pinned to 1.5.21); superseded here by 1.5.22 for the import baseline.

## Development laws

```
UPSTREAM != DOWNSTREAM
SOURCE CHECKOUT != REPRODUCIBLE APK
TRANSPORT != TRUST
HUMAN INVITATION != AUTOMATED DELIVERY
MESSAGE SUBMISSION != REMOTE RECEPTION
SIGNED != TRUE
```

## Security, copyright and attribution

- Briar's original source license is **GPL-3.0-or-later**. Keep its original license and notices; review other components and Git submodules for their own licenses.
- Modified binaries must be clearly distinguished from official Briar releases and accompanied by applicable corresponding source and notices.
- Never publish Briar local REST bearer tokens, private keys, address books, or messages.
- No public relay, unattended network exposure, production anonymity claims, or automatic chat messages are part of this bootstrap.
- Independent upstream release reproducibility uses Briar's [reproducer](https://code.briarproject.org/briar/briar-reproducer); a successful Gradle build alone does not establish reproducibility or security.

See [SOURCE_IMPORT.md](SOURCE_IMPORT.md) for import procedure, branches, constraints, and verification.
