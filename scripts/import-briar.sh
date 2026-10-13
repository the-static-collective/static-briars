#!/usr/bin/env bash
# Imports Briar's pinned, original Git lineage into separate source branches.
# Never force-pushes, modifies main, or runs upstream build code.
set -Eeuo pipefail
IFS=$'\n\t'
export GIT_TERMINAL_PROMPT=0

UPSTREAM="https://code.briarproject.org/briar/briar.git"
TAG="release-1.5.22"
TAG_SHA="9153cb672bcc258443ff31c1b2d7bba6999a13a2"
COMMIT="d03d6fcd776349ec6c8b7d22035e9bb04aa1787c"
SOURCE_BRANCH="upstream/release-1.5.22"
DERIVATIVE_BRANCH="static/continuation-001"
EXPECTED_REPO="the-static-collective/static-briars"

: "${GITHUB_REPOSITORY:?Expected Actions repository environment}"
: "${GITHUB_TOKEN:?Expected scoped Actions token}"
if [[ "$GITHUB_REPOSITORY" != "$EXPECTED_REPO" ]]; then
  echo "Refusing import into unexpected repository." >&2
  exit 2
fi

DEST="https://github.com/$EXPECTED_REPO.git"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT
git clone --no-checkout --single-branch --branch "$TAG" "$UPSTREAM" "$WORK/briar"
cd "$WORK/briar"

if [[ "$(git rev-parse "refs/tags/$TAG")" != "$TAG_SHA" ]]; then
  echo "STOP: signed release tag object does not match pinned Git object." >&2
  exit 1
fi
if [[ "$(git rev-parse "refs/tags/$TAG^{commit}")" != "$COMMIT" ]]; then
  echo "STOP: release tag points to an unexpected Git commit." >&2
  exit 1
fi
git checkout --detach "$COMMIT"
test -f LICENSE.txt || { echo "STOP: missing original GPL license" >&2; exit 1; }
test -f .gitmodules || { echo "STOP: expected submodules declaration absent" >&2; exit 1; }
# Git submodule references are committed as gitlinks and therefore preserved
# by the Git transport without executing or fetching submodule code.

# Authorization only for GitHub pushes, never sent to the upstream GitLab host.
AUTH="$(printf 'x-access-token:%s' "$GITHUB_TOKEN" | base64 -w0)"
push_static() {
  git -c "http.https://github.com/.extraheader=AUTHORIZATION: basic $AUTH" \
    push "$DEST" "$1:refs/heads/$2"
}
remote_head() {
  git ls-remote --heads "$DEST" "$1" | awk '{print $1}'
}

BASE_REMOTE="$(remote_head "$SOURCE_BRANCH")"
if [[ -n "$BASE_REMOTE" && "$BASE_REMOTE" != "$COMMIT" ]]; then
  echo "STOP: source branch exists but does not match pinned upstream history." >&2
  exit 1
fi
push_static "$COMMIT" "$SOURCE_BRANCH"
echo "SOURCE IMPORTED: $SOURCE_BRANCH -> $COMMIT"

if [[ -n "$(remote_head "$DERIVATIVE_BRANCH")" ]]; then
  echo "Derivative branch already exists; leaving untouched."
  exit 0
fi

git switch --create "$DERIVATIVE_BRANCH" "$COMMIT"
mkdir -p STATIC
cat > STATIC/PROVENANCE.md <<'EOF'
# STATIC BRIARS — Source lineage record

This source branch descends directly from the Briar project's original
`release-1.5.22` Git history, rather than a file-copy snapshot.

- Canonical upstream: https://code.briarproject.org/briar/briar
- Public upstream mirror: https://github.com/briar/briar
- Upstream tag: `release-1.5.22`
- Annotated tag object: `9153cb672bcc258443ff31c1b2d7bba6999a13a2`
- Release commit: `d03d6fcd776349ec6c8b7d22035e9bb04aa1787c`
- Original copyright notices, Git history, submodule gitlinks, and
  `LICENSE.txt` remain in place.

This is an independently maintained research derivative, **not** an
official Briar release. Submodules and other separately licensed components
must be audited before any binary distribution. A source snapshot or debug
build is not a reproducible-release proof.

Next stages: Android toolchain audit, unmodified build smoke test,
independent binary reproduction, then narrowly reviewed UX modifications.
No private data or secrets belong in commits.
EOF
git add STATIC/PROVENANCE.md
git -c user.name="Static Collective Source Bootstrap" \
  -c user.email="source-bootstrap@users.noreply.github.com" \
  commit -m "docs(static): establish attributed Briar continuation lineage"
push_static "HEAD" "$DERIVATIVE_BRANCH"
echo "CONTINUATION READY: $DERIVATIVE_BRANCH"
