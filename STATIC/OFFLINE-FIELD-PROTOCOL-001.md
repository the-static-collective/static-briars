# OFFLINE FIELD PROTOCOL 001 — Two Humans, One Interrupted Road

**Status:** runnable human test protocol; no field observations or proof of delivered messages yet.

## Experimental question

Can two independently controlled Briar Android devices keep their local histories coherent while one contact drops offline, returns, and synchronizes — without inferring successful message delivery from an enabled network preference? Can an optional Briar Mailbox (third device) accept custody and later facilitate message delivery when the receiving phone is offline?

**The humans, not a scheduler, control all sends and disconnections.**

## Equipment and privacy

- Phone A and phone B, Android versions compatible with **the same agreed Briar build**. For our modified app, first produce an audited debug APK, use a testing profile, and confirm its package identity and data isolation. Never overwrite a live Briar installation with a test build.
- Optional Phone C with an **official, supported Briar Mailbox** installation. Do not claim a generic Linux headless peer is interchangeable with Mailbox.
- Two consenting test operators, throwaway test nicknames, no personal contacts, no production conversation data, and no real network identifiers in public reports.
- Agree on a local experiment ID not reused for other tests. Both operators record observations separately, using relative time offsets rather than identifying timestamps where possible.

## Boundaries

- **Actual delivery:** can only be reported based on the recipient visibly finding the exact unique test message in their own Briar application. Sender-side queue/ack symbols are not sufficient witness of recipient display.
- **Message confidentiality:** do not capture screenshots of contact lists, credentials, private messages, IP addresses, Tor keys, or QR links. Use pre-agreed harmless test markers.
- **No coercive experimentation:** if either operator opts out, stop immediately.
- **No risky anonymity test:** this measures application behavior on permitted roads, not whether traffic-correlation adversaries can identify participants.
- Disable/break only equipment and accounts under your control. Never ask third-party nodes to act as VPN exits without consent.

## Protocol

### Phase A — Initial contact

1. On A and B, confirm the desired transport setting and record plugin state separately: `ENABLED` in Road Compass means preference; `ACTIVE` in Network Witness means the plugin claims it can connect.
2. Use Briar's intended contact exchange method. Verify each other's actual identity through an independent trusted channel if the trial asserts *verified contacts*.
3. With both phones online, intentionally send a harmless uniquely labeled test message A→B, then B→A.
4. Each recipient records **observed on recipient** only when the matching marker appears on their own screen.
5. Record per-transport connected counts as local observations, **not** as participant identities.

### Phase B — Deliberate disappearance

1. Explicitly take B offline (disable all network transports at the device level; verify by local device settings). A remains online.
2. A sends a new harmless marker while B is disconnected.
3. Record A's sending interface exactly, without upgrading that state to delivery. Expectation: B cannot display a new message while fully disconnected, but this is tested, not assumed.
4. Restore B's permitted transport(s).
5. Record whether/when B displays the exact marker and whether conversation order and previous messages remain intact.
6. Restart B's app; inspect whether the earlier messages are still present.

### Phase C — Optional official Mailbox

1. Confirm **official Briar Mailbox** compatibility and complete its standard pairing process with B, while B controls its own identity.
2. With B offline and Mailbox C online, A intentionally sends marker C1 to B.
3. Record Mailbox's own documented status (if shown) **separately** from recipient status; don't infer C1 reached B.
4. Bring B online and observe whether B displays C1. Restart B and inspect continuity.
5. Repeat once with C offline to distinguish Mailbox custody from successful recipient access. Mark failure/unknown if the sender/recipient UI provides insufficient evidence.

### Phase D — Road change, no identity replacement

1. If both devices support it and both operators consent, change A/B's permitted transport from internet/Tor to nearby Wi-Fi/Bluetooth.
2. Record which *actual* runtime and peer-link observations change, whether the same existing verified contact is preserved, and whether the message markers remain.
3. Never label cross-road transfer as anonymity or universal network resilience.

## Field record — safe compact template

```json
{
  "schema": "static-briars.offline-field-witness/v0",
  "experiment": "local-disconnected-message-001",
  "actors": ["A", "B"],
  "phase": "B",
  "step": "B5",
  "observation_type": "recipient-message-visible",
  "marker": "TEST-B2",
  "observer": "B",
  "result": "observed",
  "transport_claim": "unknown",
  "receipt_claim": "none",
  "notes": "No identity, message content, IPs or tokens"
}
```

The marker is an arbitrary *non-secret* test label, not the private message body. A machine-generated record must **not** mark `recipient-message-visible` without a human B observation.

## Graduation criteria

The experiment graduates from source/UI proof to **two-device field observation** only after receiving at least one independently logged recipient-visible message on both sides, an interrupted-transport recovery case, an app-restart continuity check, and an explicit list of unknowns. Mailbox success requires a *separate* receiver-visible result following supported Mailbox custody; don't relabel simple reconnect as proof of Mailbox function.

No physical-device field trial has been performed by this branch.
