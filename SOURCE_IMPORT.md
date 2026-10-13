# Source import and ownership

## Why this repository has two histories

The default `main` branch is a small control repository. **It is not Briar's app source.** A one-time import pushes the upstream Git objects into a separate source branch, preserving its original commit authors and ancestry without overwriting `main`.

Target branches:
- `upstream/release-1.5.22` — exact, unmodified upstream Git commit.
- `static/continuation-001` — a descendant commit that adds only `STATIC/PROVENANCE.md`. This is the starting point for independent Briar source changes.

## Pinned upstream

- Canonical Git remote: `https://code.briarproject.org/briar/briar.git`
- Release tag: `release-1.5.22`
- Annotated tag object: `9153cb672bcc258443ff31c1b2d7bba6999a13a2`
- Underlying commit: `d03d6fcd776349ec6c8b7d22035e9bb04aa1787c`
- Checked against Briar's public GitHub mirror on October 13, 2026, with a `verified` annotated tag.

The importer checks both Git object identities before any push. GitHub mirror verification of the tag proves a tag cryptographic signature validated against the mirror's key information; it does **not** by itself establish the upstream author's real-world identity independently.

## Import steps and verification

The `.github/workflows/import-briar.yml` workflow triggers on its initial commit to `main` and also supports manual reruns. It executes `scripts/import-briar.sh` in a bounded GitHub Actions job with `contents: write`, narrowly restricted to this repository.

This workflow:
1. Clones canonical upstream GitLab history for the pinned tag without executing Briar Gradle or submodule build code.
2. Verifies the annotated tag Git object SHA and the target commit SHA, and checks the original source license and submodule declarations.
3. Non-force-pushes the unmodified upstream commit to a named branch, refusing to replace unexpected existing branches.
4. Creates a new derivative branch from the same upstream commit and adds one provenance file.
5. Exits without changing existing developer work on rerun.

If organization or repository Actions permissions prohibit write, the import may fail without mutating any source branch. Inspect logs and permissions; do not claim an import before GitHub exposes the expected refs.

**After import:**

```sh
git clone https://github.com/the-static-collective/static-briars.git
cd static-briars
git fetch origin
git switch --track origin/static/continuation-001
git merge-base --is-ancestor d03d6fcd776349ec6c8b7d22035e9bb04aa1787c HEAD
git submodule sync --recursive
git submodule update --init --recursive
```

Run the exact upstream build prerequisites from [Briar's build documentation](https://briarproject.org/building/). Only then attempt an Android debug compile, and separately use the [official reproducibility project](https://code.briarproject.org/briar/briar-reproducer) for official-release binary comparisons.

## Boundaries

Source import != successfully built Android binary.

Android debug build != reproducible published APK.

Briar transport != guaranteed universal anonymity.

A signed reLATTE crossing invitation != a message delivered to a peer.

Retain GPL-3.0-or-later notices, inspect submodule licenses and the separate Briar Mailbox / desktop licensing, and distinguish downstream branding from official Briar software. Never commit private keys, local Headless REST tokens, or actual contact records.
