#!/usr/bin/env bash
# Lightweight inherited-source invariant. Not an Android build or security audit.
set -Eeuo pipefail

BASE="d03d6fcd776349ec6c8b7d22035e9bb04aa1787c"
git cat-file -e "$BASE^{commit}" || { echo "Pinned Briar commit missing"; exit 1; }
git merge-base --is-ancestor "$BASE" HEAD || {
  echo "FAIL: Static history does not descend from the pinned Briar release."
  exit 1
}
test -s LICENSE.txt || { echo "FAIL: upstream LICENSE.txt missing"; exit 1; }
test -s .gitmodules || { echo "FAIL: upstream .gitmodules missing"; exit 1; }
test -s STATIC/PROVENANCE.md || { echo "FAIL: source lineage not documented"; exit 1; }

# GPL and submodule manifests are not authorized to be silently changed by
# an unrelated exploratory UX/transport PR.
git diff --exit-code "$BASE" HEAD -- LICENSE.txt .gitmodules || {
  echo "FAIL: changed original license or submodule manifest; requires a separately reviewed change."
  exit 1
}

# Check Gitlink entries themselves, not downloaded contents. Submodules remain
# owned by upstream projects and have not been checked out in this CI job.
git diff --exit-code "$BASE" HEAD -- briar-mailbox || {
  echo "FAIL: changed linked Mailbox revision; requires explicit provenance review."
  exit 1
}
echo "PASS: Static source remains attributable to pinned Briar release."
