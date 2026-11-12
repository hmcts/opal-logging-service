#!/usr/bin/env bash
set -euo pipefail

message="🧪 TEST ONLY — Opal logging master pipeline alert
This is a connectivity test. It does not mean logging master is RED.

A real alert would name the failed build, link to its Jenkins result, and ask the team to pause routine merges until a newer master build is GREEN.
Master pipeline: https://sds-build.hmcts.net/job/HMCTS/job/opal-logging-service/job/master/"

printf '%s\n' "$message"

if [[ "${SEND_TEST:-false}" != "true" ]]; then
  printf '%s\n' 'Preview only; no Teams message sent.'
  exit 0
fi

if [[ -z "${TEAMS_WEBHOOK_URL:-}" ]]; then
  printf '%s\n' 'OPAL_MASTER_ALERT_TEAMS_WEBHOOK is not configured.' >&2
  exit 1
fi

payload="$(jq -n --arg text "$message" '{text: $text}')"
curl --fail-with-body --silent --show-error --retry 2 --retry-delay 2 \
  -H 'Content-Type: application/json' --data "$payload" \
  --output /dev/null "$TEAMS_WEBHOOK_URL"

printf '%s\n' 'Test message sent to Teams.'
