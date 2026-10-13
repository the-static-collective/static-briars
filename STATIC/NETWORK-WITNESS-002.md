# NETWORK WITNESS 002 — Runtime Is Not Delivery

**Status:** native Android prototype; compile/runtime CI and field trials assessed separately.

## Existing Briar evidence sources

Briar 1.5.22 already implements:
- `PluginManager.getPlugin(TransportId).getState()`: real local runtime states `STARTING_STOPPING`, `DISABLED`, `ENABLING`, `ACTIVE`, `INACTIVE`;
- `ConnectionRegistry.getConnectedContacts(TransportId)`: the **current** set of locally connected contacts for a given transport;
- `TransportStateEvent`, `ConnectionOpenedEvent`, `ConnectionClosedEvent`: local events emitted on state and link changes.

All are built-in. **No ping, location, packet inspection, contact name, address, API token, or remote service** enters this feature.

## User-facing effect

On **Settings → Connections**, Road Compass shows unchanged *preferences*. Its new **Network Witness** section independently shows, for Bluetooth, Wi-Fi and Tor:
- plugin absent, disabled, enabling, active, inactive, or starting/stopping;
- integer count of locally connected contacts over each transport;
- explicit text that even an active transport cannot prove a specific message was delivered.

The view model re-samples Briar's registry when the page opens, on `TransportStateEvent`, and on connection open/close; sampling happens through the existing database executor, not blocking rendering. Snapshots are local and ephemeral, never persisted/exported.

An ACTIVE transport is not necessarily connected to anyone; a connection to a contact is not evidence of any given message delivery. Multiple transport counts **can refer to the same contact**: don't sum them into a unique participant count.

## Boundaries

- A plugin state is only what the local plugin reports at that moment.
- Connection counts are derived from the runtime registry, not independently network-attested.
- No contact list enumeration appears in Network Witness; `Collection.size()` alone is surfaced.
- Sampling and events can race; this is **an observation**, not an authoritative chronological transcript.
- This code does not claim status of Mailbox delivery, messages, attachments, or Tor anonymity.
- Briar may be offline without forfeiting stored conversation continuity.

## Executable scope

- `NetworkWitnessState.java`: plain Java mode/count model.
- `ConnectionsManager.java`: actual plugin/registry snapshot.
- `SettingsViewModel.java`: refresh via existing `EventBus` and database executor.
- `ConnectionsFragment.java`: three read-only runtime rows.
- `NetworkWitnessStateTest.java` and `STATIC/ci/NetworkWitnessSmoke.java`: state vs peers tests.
- `STATIC/ci/check-road-compass.py`: validates Android text resource, settings and event bindings.
- Existing Android compile CI additionally evaluates the actual full Briar modules.

## Human two-device experiment

See [OFFLINE-FIELD-PROTOCOL-001.md](OFFLINE-FIELD-PROTOCOL-001.md). No simulated CI test can replace a real two-device disconnect/reconnect and optional third-device Briar Mailbox witness.

```
CONFIGURED != ACTIVE
ACTIVE != PEER LINK
PEER LINK != MESSAGE DELIVERED
MAILBOX ONLINE != RECIPIENT ONLINE
OBSERVATION != IDENTITY
```
