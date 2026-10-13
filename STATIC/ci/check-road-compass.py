#!/usr/bin/env python3
"""Static Android Road Compass: resource and wiring contract (no SDK needed)."""
import pathlib
import re
import xml.etree.ElementTree as ET

ROOT = pathlib.Path(__file__).resolve().parents[2]
RES = ROOT / "briar-android/src/main/res"
ANDROID = "{http://schemas.android.com/apk/res/android}"
app = ROOT / "briar-android/src/main/java/org/briarproject/briar/android/settings/ConnectionsFragment.java"
res = RES / "values/strings_static_compass.xml"
prefs = RES / "xml/settings_connections.xml"

strings_root = ET.parse(res).getroot()
resources = [e.attrib["name"] for e in strings_root if e.tag == "string"]
assert len(resources) == len(set(resources)), "Duplicate resource names"
assert len(resources) >= 12, "Companion resources missing"
preference_root = ET.parse(prefs).getroot()
preference_ids = [
    e.get(ANDROID + "key") for e in preference_root.iter()
    if e.get(ANDROID + "key") is not None
]
assert len(preference_ids) == len(set(preference_ids)), "Duplicate preference keys"
expected = {
    "pref_key_static_compass_explain",
    "pref_key_static_compass_bluetooth",
    "pref_key_static_compass_wifi",
    "pref_key_static_compass_tor",
    "pref_key_static_compass_mailbox",
}
assert expected.issubset(preference_ids), "Missing visible Android preference"
for el in preference_root.iter():
    for value in el.attrib.values():
        if value.startswith("@string/static_compass_"):
            assert value.split("/", 1)[1] in resources, "Missing resource " + value

source = app.read_text()
for event in ("btEnabled()", "wifiEnabled()", "torEnabled()", "torMobile()", "torCharging()"):
    assert re.search(r"connectionsManager\." + re.escape(event) + r"\.observe", source), event
assert source.count("updateRoadCompass();") == 5, "A settings change no longer refreshes the guide"
assert "new Intent(requireContext(), MailboxActivity.class)" in source
assert "RoadCompassState.torMode" in source
assert "setPositiveButton(android.R.string.ok, null)" in source

notices = res.read_text()
assert "not live connection tests" in notices
assert "Connection not confirmed" in notices
print("PASS: Android Road Compass resource/wiring and privacy copy contracts")
