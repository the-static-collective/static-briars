"""Offline witness ledger contract tests; no real Briar contacts or networks."""
import importlib.util
import json
import os
import stat
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

MODULE = Path(__file__).resolve().with_name("offline_witness.py")
spec = importlib.util.spec_from_file_location("offline_witness", MODULE)
w = importlib.util.module_from_spec(spec)
spec.loader.exec_module(w)


def record(idx, phase, observer, event, frm=None, to=None,
           marker=None, at=0, result="observed"):
    return {
        "schema": w.SCHEMA,
        "experiment": "offline-test-001",
        "observation": f"obs-{idx:03d}",
        "phase": phase,
        "observer": observer,
        "event": event,
        "from": frm,
        "to": to,
        "marker": marker,
        "elapsed_seconds": at,
        "result": result,
    }


class WitnessTests(unittest.TestCase):

    def test_exact_schema_and_recipient_ownership(self):
        msg = record(1, "B", "A", "send-submitted", "A", "B", "TEST-B2")
        w.check(msg)
        w.check(record(2, "B", "B", "recipient-visible", "A", "B", "TEST-B2"))
        with self.assertRaisesRegex(ValueError, "recipient"):
            w.check(record(3, "B", "A", "recipient-visible", "A", "B", "TEST-B2"))
        with self.assertRaisesRegex(ValueError, "sender"):
            w.check(record(4, "B", "B", "send-submitted", "A", "B", "TEST-B2"))
        for bad in ({**msg, "contact-name": "secret"}, {**msg, "marker": "Private message body"},
                    {**msg, "elapsed_seconds": True}):
            with self.assertRaises(ValueError):
                w.check(bad)

    def test_recovery_requires_observed_order_and_same_marker(self):
        rows = [
            record(1, "B", "B", "device-offline", at=10),
            record(2, "B", "A", "send-submitted", "A", "B", "TEST-B2", 20),
            record(3, "B", "B", "device-online", at=30),
            record(4, "B", "B", "recipient-visible", "A", "B", "TEST-B2", 40),
        ]
        self.assertTrue(w.summarize(rows)["phase-b-disconnection-recovery-claimed"])
        self.assertFalse(w.summarize(rows[:3])["phase-b-disconnection-recovery-claimed"])
        wrong_marker = [{**rows[3], "marker": "TEST-DIFFERENT"}]
        self.assertFalse(w.summarize(rows[:3] + wrong_marker)["phase-b-disconnection-recovery-claimed"])
        wrong_order = [{**rows[2], "elapsed_seconds": 15}]
        self.assertFalse(w.summarize(rows[:2] + wrong_order + rows[3:])["phase-b-disconnection-recovery-claimed"])
        self.assertFalse(w.summarize(rows[:1] + rows[2:])["phase-b-disconnection-recovery-claimed"])

    def test_mailbox_claim_never_promotes_to_custody_proof(self):
        r = [record(1, "C", "C", "mailbox-status-visible", at=17)]
        s = w.summarize(r)
        self.assertTrue(s["mailbox-ui-status-claimed"])
        self.assertFalse(s["mailbox-custody-proven"])
        self.assertFalse(s["cryptographic-delivery-proof"])

    def test_ledger_rejects_duplicate_ids_and_experiments(self):
        with tempfile.TemporaryDirectory() as td:
            p = Path(td) / "test.jsonl"
            r = record(1, "A", "A", "device-online")
            p.write_text(json.dumps(r) + "\n" + json.dumps(r) + "\n")
            with self.assertRaisesRegex(ValueError, "Duplicate"):
                w.read_ledger(p)
            p.write_text(json.dumps(r) + "\n" + json.dumps(
                {**r, "observation": "obs-002", "experiment": "other-test-001"}) + "\n")
            with self.assertRaisesRegex(ValueError, "exactly one"):
                w.read_ledger(p)

    def test_cli_requires_confirmed_human_entry_and_private_file(self):
        with tempfile.TemporaryDirectory() as td:
            p = Path(td) / "witness.jsonl"
            r = record(1, "A", "A", "device-online")
            cmd = [sys.executable, str(MODULE), str(p), "--add", json.dumps(r)]
            denied = subprocess.run(cmd, capture_output=True, text=True)
            self.assertNotEqual(denied.returncode, 0)
            self.assertFalse(p.exists())
            allowed = subprocess.run(cmd + ["--confirm-human-observation"],
                                     capture_output=True, text=True)
            self.assertEqual(allowed.returncode, 0, allowed.stderr)
            self.assertEqual(len(w.read_ledger(p)), 1)
            if os.name == "posix":
                self.assertEqual(stat.S_IMODE(p.stat().st_mode), 0o600)
            duplicate = subprocess.run(cmd + ["--confirm-human-observation"],
                                       capture_output=True, text=True)
            self.assertNotEqual(duplicate.returncode, 0)

    def test_no_network_import(self):
        code = MODULE.read_text()
        self.assertNotIn("requests", code)
        self.assertNotIn("urllib.request", code)
        self.assertNotIn("socket.socket", code)


if __name__ == "__main__":
    unittest.main()
