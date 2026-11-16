#!/usr/bin/env python3
"""Send completed logging-service nightly results to a separate Teams channel.

Jenkins dispatches the nightly result to GitHub after its pipeline finishes.
The Action accepts that event only for this repo's nightly master job. Regular
master statuses use their existing two-channel Action.
"""

import json
import os
import re
import subprocess
import sys


NIGHTLY_JOB = "https://sds-build.hmcts.net/job/HMCTS_Nightly/job/opal-logging-service/job/master/"
NIGHTLY_BUILD = re.compile(rf"^{re.escape(NIGHTLY_JOB)}(\d+)(?:/|$)")
RESULT_MESSAGE = {
    "SUCCESS": "Nightly build succeeded.",
    "FAILURE": "Nightly build failed.",
    "UNSTABLE": "Nightly build was unstable.",
    "ABORTED": "Nightly build was aborted.",
    "NOT_BUILT": "Nightly build did not complete.",
}


def card_for(number, result, sha):
    """Show the nightly result and direct links to that Jenkins build."""
    needs_console = result != "SUCCESS"
    colour = "good" if result == "SUCCESS" else "attention" if result == "FAILURE" else "warning"
    icon = "🟢" if result == "SUCCESS" else "🔴" if result == "FAILURE" else "🟠"
    build_url = f"{NIGHTLY_JOB}{number}/"
    actions = [
        {"type": "Action.OpenUrl", "title": "Latest nightly", "url": NIGHTLY_JOB},
        {"type": "Action.OpenUrl", "title": f"Build #{number}", "url": build_url},
    ]
    if needs_console:
        actions.append({"type": "Action.OpenUrl", "title": "Console output",
                        "url": f"{build_url}console"})
    if sha:
        actions.append({"type": "Action.OpenUrl", "title": f"Commit {sha[:7]}",
                        "url": f"https://github.com/hmcts/opal-logging-service/commit/{sha}"})
    return {
        "$schema": "http://adaptivecards.io/schemas/adaptive-card.json",
        "type": "AdaptiveCard",
        "version": "1.2",
        "body": [
            {"type": "Container", "style": colour, "items": [
                {"type": "TextBlock",
                 "text": f"{icon} Logging Service nightly · #{number} · {result}",
                 "size": "Large", "weight": "Bolder", "wrap": True},
            ]},
            {"type": "TextBlock", "text": RESULT_MESSAGE[result],
             "wrap": True, "separator": True, "spacing": "Medium"},
        ],
        "actions": actions,
    }


def main():
    match = NIGHTLY_BUILD.match(os.environ.get("BUILD_URL", ""))
    if not match:
        sys.exit("Unexpected nightly Jenkins build URL.")
    result = os.environ.get("BUILD_RESULT")
    if result not in RESULT_MESSAGE:
        sys.exit("No completed nightly result to report.")
    webhook = os.environ.get("NIGHTLY_WEBHOOK_URL")
    if not webhook:
        sys.exit("OPAL_NIGHTLY_BUILDS_TEAMS_WEBHOOK is not configured.")
    sha = os.environ.get("BUILD_SHA", "")
    if sha and not re.fullmatch(r"[0-9a-f]{40}", sha):
        sys.exit("Nightly build SHA is invalid.")
    card = card_for(match.group(1), result, sha)
    payload = {"type": "message", "attachments": [
        {"contentType": "application/vnd.microsoft.card.adaptive", "content": card},
    ]}
    subprocess.run(["curl", "--fail-with-body", "--silent", "--show-error",
                    "--retry", "2", "--retry-delay", "2", "-H", "Content-Type: application/json",
                    "--data", json.dumps(payload), "--output", "/dev/null", webhook], check=True)
    print("Nightly card accepted by the Teams webhook; check its workflow run for delivery.")


if __name__ == "__main__":
    main()
