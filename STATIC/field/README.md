# OFFLINE WITNESS 003 — Local Human Observation Ledger

**Status:** human-operated tool with synthetic unit tests; no actual device or Mailbox measurements.

This tool lets two consenting people record what they each *see* during [OFFLINE FIELD PROTOCOL 001](../OFFLINE-FIELD-PROTOCOL-001.md), without capturing messages, peer addresses, locations, device identifiers, Tor keys, or absolute timestamps.

It is a **local JSONL ledger**, not a cryptographic receipt. Operators could make mistakes or fabricate entries; the CLI validates structure, role assignment, and claimed ordering. It never connects to Briar or a network.

## Four-step example

Each field observation uses the exact same schema. The command below records B observing their own phone offline:

```sh
python3 STATIC/field/offline_witness.py ./test-ledger.jsonl \
  --add '{"schema":"static-briars.offline-field-witness/v1","experiment":"offline-test-001","observation":"obs-001","phase":"B","observer":"B","event":"device-offline","from":null,"to":null,"marker":null,"elapsed_seconds":10,"result":"observed"}' \
  --confirm-human-observation
```

Next record an **A-side** `send-submitted` observation using a harmless marker such as `TEST-B2`, then a B-side `device-online` observation, then **only when B really sees the test message**, a B-side `recipient-visible` observation. Use elapsed offsets in that order and unique `obs-NNN` IDs.

View the local summary without making changes:

```sh
python3 STATIC/field/offline_witness.py ./test-ledger.jsonl
```

The summary identifies only an **operator-claimed** `offline → send → online → recipient-visible` sequence. Even this is not cryptographic proof of delivery. A sender cannot record a recipient-visible event under their own role.

The Mailbox case (phase C) can record that operator C saw Mailbox status. The summary **never** labels Mailbox custody proven: a recipient might have received the message by another route.

## Privacy boundaries

- Use only disposable test identities and harmless canary phrases. Store only nonsecret `TEST-...` markers.
- Human roles and observations are self-reported, not authenticated. Do not sign on behalf of someone else.
- Do not publish raw ledgers without the operators' consent.
- The script creates local logs with `0600` mode on POSIX, refuses group/world-readable existing logs, and refuses symlinks on supported systems.
- Unknown fields (names, IPs, location, email, URLs, arbitrary notes, contact lists, cryptographic keys, and message text) are rejected.
- No remote API, screen recording, telemetry, background process, or automated Briar message is involved.
- No production Briar identity, private conversation, or contact list should be loaded into the experimental APK.

## Test without devices

```sh
python3 -m unittest discover -v -s STATIC/field -p 'test_*.py'
```

CI uses synthetic observations. Passing tests validate **ledger behavior**, not communication across phones.
