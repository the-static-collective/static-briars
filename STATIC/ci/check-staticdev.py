#!/usr/bin/env python3
"""Pre-build guard: independent lab identity without altering Briar official."""
from pathlib import Path
import re
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[2]
gradle = (root / "briar-android/build.gradle").read_text()
values = root / "briar-android/src/staticdev/res/values/strings.xml"
manifest = root / "briar-android/src/staticdev/AndroidManifest.xml"
art = root / "briar-android/src/staticdev/res/drawable/staticdev_launcher.xml"
expected = "org.staticcollective.briars.lab.debug"

assert re.search(r'official\s*\{\s*dimension\s+"version"\s*\}', gradle), "official flavor changed"
assert 'applicationId "org.briarproject.briar.android"' in gradle
assert 'applicationId "org.staticcollective.briars.lab"' in gradle
assert 'applicationIdSuffix ".debug"' in gradle
assert 'variant.flavors*.name.contains("staticdev")' in gradle
names = {e.attrib["name"]: e.text for e in ET.parse(values).getroot() if e.tag == "string"}
# Android build-type resources override flavor resources. Briar's original
# src/debug strings say "Briar Debug" and otherwise steal our launcher label
# and the explicit app_package target. The exact variant has higher priority.
variant_values = root / "briar-android/src/staticdevDebug/res/values/strings.xml"
variant_names = {e.attrib["name"]: e.text for e in ET.parse(variant_values).getroot() if e.tag == "string"}
assert variant_names["app_name"] == "STATIC Briars DEV"
assert variant_names["app_package"] == expected
assert names["app_name"] == "STATIC Briars DEV"
assert names["app_package"] == expected
ns = "{http://schemas.android.com/apk/res/android}"
application = ET.parse(manifest).getroot().find("application")
assert application is not None
assert application.get(ns + "label") == "@string/app_name"
assert application.get("{http://schemas.android.com/tools}replace") == "android:icon,android:logo,android:roundIcon"
for attr in ("icon", "logo", "roundIcon"):
    assert application.get(ns + attr) == "@drawable/staticdev_launcher", attr
activity = application.find("activity")
assert activity is not None
assert activity.get(ns + "name") == "org.briarproject.briar.android.navdrawer.NavDrawerActivity"
assert activity.get(ns + "exported") == "false"
assert activity.get("{http://schemas.android.com/tools}replace") == "android:exported"
filters = activity.findall("intent-filter")
assert len(filters) == 1
assert filters[0].get("{http://schemas.android.com/tools}node") == "removeAll"
drawable = ET.parse(art).getroot()
assert drawable.tag == "vector" and len(drawable) >= 2
assert "org.briarproject.briar.android" not in names["app_package"]
print("PASS: independent debug app ID, local intent target, visible identity, release gate")
