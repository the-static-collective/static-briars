#!/usr/bin/env python3
"""STATIC BRIARS OFFLINE WITNESS 003: minimal local human observation ledger.

No Briar access, no transport probing, no external I/O, no claims of signed
identity. Entries are test-operator assertions and may be mistaken.
"""
import argparse
import json
import os
import re
from pathlib import Path

SCHEMA = "static-briars.offline-field-witness/v1"
ALLOWED_EVENTS = {
    "send-submitted", "recipient-visible", "device-offline", "device-online",
    "mailbox-status-visible", "app-restarted", "runtime-state-observed",
}
ALLOWED_PHASES = {"A", "B", "C", "D"}
ALLOWED_ACTORS = {"A", "B", "C"}
ALLOWED_KEYS = {
    "schema", "experiment", "observation", "phase", "observer",
    "event", "from", "to", "marker", "elapsed_seconds", "result",
}
ID = re.compile(r"^[a-z][a-z0-9-]{2,63}$")
MARKER = re.compile(r"^TEST-[A-Z0-9-]{2,30}$")
OBS = re.compile(r"^[a-z0-9][a-z0-9-]{2,40}$")


def check(entry):
    if not isinstance(entry, dict) or set(entry) != ALLOWED_KEYS:
        raise ValueError("Only non-identifying fields in the v1 schema are allowed")
    if entry["schema"] != SCHEMA:
        raise ValueError("Unsupported schema")
    if not isinstance(entry["experiment"], str) or not ID.fullmatch(entry["experiment"]):
        raise ValueError("Invalid experiment label")
    if not isinstance(entry["observation"], str) or not OBS.fullmatch(entry["observation"]):
        raise ValueError("Invalid observation label")
    if entry["phase"] not in ALLOWED_PHASES:
        raise ValueError("Unknown phase")
    if entry["observer"] not in ALLOWED_ACTORS:
        raise ValueError("Unknown observer")
    if entry["event"] not in ALLOWED_EVENTS:
        raise ValueError("Unknown observation type")
    for field in ("from", "to"):
        if entry[field] not in ("A", "B", None):
            raise ValueError("Unknown sender/recipient")
    if type(entry["elapsed_seconds"]) is not int or not 0 <= entry["elapsed_seconds"] <= 604800:
        raise ValueError("Invalid relative observation offset")
    if entry["result"] not in ("observed", "not-observed", "unknown"):
        raise ValueError("Unsupported observation result")
    is_message = entry["event"] in ("send-submitted", "recipient-visible")
    if is_message:
        if entry["from"] == entry["to"] or entry["from"] is None or entry["to"] is None:
            raise ValueError("A test message requires distinct A/B participants")
        if not isinstance(entry["marker"], str) or not MARKER.fullmatch(entry["marker"]):
            raise ValueError("Use a non-secret TEST- marker; no message body")
        if entry["event"] == "send-submitted" and entry["observer"] != entry["from"]:
            raise ValueError("Only sender can record own UI submission")
        if entry["event"] == "recipient-visible" and entry["observer"] != entry["to"]:
            raise ValueError("Only recipient can report their own screen")
    elif any(entry[k] is not None for k in ("from", "to", "marker")):
        raise ValueError("No free-text or message fields in device/runtime observations")
    if entry["event"] == "mailbox-status-visible" and entry["phase"] != "C":
        raise ValueError("Mailbox observations belong in phase C")
    if entry["event"] == "device-offline" and entry["observer"] == "C" and entry["phase"] != "C":
        raise ValueError("Only phase C tests mailbox offline state")


def read_ledger(path):
    if not path.exists():
        return []
    records = []
    for n, line in enumerate(path.read_text(encoding="utf-8").splitlines(), start=1):
        try:
            item = json.loads(line)
            check(item)
        except (ValueError, KeyError, TypeError) as error:
            raise ValueError(f"Invalid record on line {n}: {error}") from error
        records.append(item)
    if len(records) > 10000:
        raise ValueError("Ledger is over the bounded test size")
    seen = set()
    experiments = {r["experiment"] for r in records}
    if len(experiments) > 1:
        raise ValueError("A ledger must contain exactly one experiment")
    for record in records:
        key = record["observation"]
        if key in seen:
            raise ValueError("Duplicate observation ID")
        seen.add(key)
    return records


def summarize(records):
    """Independent *claims* only. There is no authentication or proof of delivery."""
    submitted = {
        (r["phase"], r["from"], r["to"], r["marker"])
        for r in records if r["event"] == "send-submitted" and r["result"] == "observed"
    }
    visible = {
        (r["phase"], r["from"], r["to"], r["marker"])
        for r in records if r["event"] == "recipient-visible" and r["result"] == "observed"
    }
    confirmed_pairs = sorted(submitted & visible)
    any_b_offline = any(
        r["phase"] == "B" and r["event"] == "device-offline"
        and r["observer"] == "B" and r["result"] == "observed"
        for r in records
    )
    any_b_online = any(
        r["phase"] == "B" and r["event"] == "device-online"
        and r["observer"] == "B" and r["result"] == "observed"
        for r in records
    )
    b_recovered = any_b_offline and any_b_online and any(
        x[0] == "B" and x[2] == "B" for x in confirmed_pairs
    )
    mailbox_observed = any(
        r["phase"] == "C" and r["observer"] == "C"
        and r["event"] == "mailbox-status-visible"
        and r["result"] == "observed"
        for r in records
    )
    return {
        "schema": "static-briars.field-summary/v1",
        "evidence_type": "UNAUTHENTICATED_HUMAN_CLAIMS",
        "validated_records": len(records),
        "submission-and-recipient-visibility-claims": len(confirmed_pairs),
        "phase-b-disconnection-recovery-claimed": b_recovered,
        "mailbox-ui-status-claimed": mailbox_observed,
        "mailbox-custody-proven": False,
        "cryptographic-delivery-proof": False,
        "notes": "Local observations, not sender cryptographic receipts or proof of Mailbox custody.",
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("ledger", type=Path, help="Private local JSONL file (not committed to Git)")
    parser.add_argument("--add", metavar="JSON", help="Add one exact-schema observation")
    parser.add_argument("--confirm-human-observation", action="store_true",
                        help="Required to append a record of what you yourself observed")
    args = parser.parse_args()
    records = read_ledger(args.ledger)
    if args.add is not None:
        if not args.confirm_human_observation:
            parser.error("Explicit --confirm-human-observation is required")
        item = json.loads(args.add)
        check(item)
        if any(x["observation"] == item["observation"] for x in records):
            parser.error("Observation ID exists; do not overwrite field history")
        if records and item["experiment"] != records[0]["experiment"]:
            parser.error("Experiment mismatch")
        args.ledger.parent.mkdir(parents=True, exist_ok=True)
        fd = os.open(str(args.ledger), os.O_WRONLY | os.O_APPEND | os.O_CREAT, 0o600)
        try:
            if os.fstat(fd).st_mode & 0o077:
                raise ValueError("Existing ledger permissions expose private observations")
            with os.fdopen(fd, "a", encoding="utf-8") as stream:
                stream.write(json.dumps(item, sort_keys=True, ensure_ascii=True) + "\n")
        except BaseException:
            # os.fdopen owns descriptor after success; on early failure close it.
            try: os.close(fd)
            except OSError: pass
            raise
        records.append(item)
    print(json.dumps(summarize(records), sort_keys=True, indent=2))


if __name__ == "__main__":
    main()
