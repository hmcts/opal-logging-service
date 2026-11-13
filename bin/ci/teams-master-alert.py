#!/usr/bin/env python3
"""Send logging-service master build results to the pilot Teams channel.

Jenkins posts a GitHub commit status when a build starts and finishes. The GitHub
Action passes that status to this script. We accept only the regular logging
``master`` job; PR, nightly and environment jobs are ignored.

With REPORT_ALL_BUILDS=true, report each completed build, including successful
ones. With it false, report the first RED build and the next GREEN recovery.
Pending builds never send a message. The status tells us the build result, not
which Jenkins stage failed or the underlying cause.

To change the Teams wording, edit the four MESSAGE templates below. Card layout
and Jenkins links are built separately, so status URLs from Jenkins can point
to a stage page without sending people there from Teams.
"""

import json
import os
import re
import subprocess
import sys


REPO = "opal-logging-service"
BASE = f"repos/hmcts/{REPO}"
MASTER_JOB = f"https://sds-build.hmcts.net/job/HMCTS/job/{REPO}/job/master/"
MASTER_BUILD = re.compile(
    rf"^{re.escape(MASTER_JOB)}(\d+)(?:/|$)"
)

# Message wording lives here so the alert policy below can stay unchanged.
# These are build results; an older overlapping build can finish after a newer one.
EVERY_BUILD_GREEN_MESSAGE = (
    "Build finished successfully. Check the latest master result before routine merges."
)
EVERY_BUILD_RED_MESSAGE = (
    "Build finished {state}. Check the latest master result before routine merges "
    "and coordinate a fix."
)
FIRST_RED_MESSAGE = (
    "Master is RED. Please check the latest master result before routine merges "
    "and coordinate a fix."
)
RECOVERY_MESSAGE = (
    "Master recovered. Check the latest master result before routine merges."
)


def api(path):
    """Read GitHub's JSON API using the token supplied by the Action."""
    result = subprocess.run(["gh", "api", path], check=True, capture_output=True, text=True)
    return json.loads(result.stdout)


def completed_builds():
    """Find the latest two completed master builds, including earlier commits.

    A new commit can have only a pending build, so we follow master's first
    parents to retain the previous completed result. Build number determines
    order: an older build may finish after a newer one. Forty commits bounds the
    API work; if history ends sooner, we use the results found so far.
    """
    commit = api(f"{BASE}/commits/master")
    builds = {}
    for _ in range(40):
        sha = commit["sha"]
        for status in api(f"{BASE}/commits/{sha}/statuses?per_page=100"):
            url = status.get("target_url") or ""
            match = MASTER_BUILD.match(url)
            if match and status.get("context") == "continuous-integration/jenkins/branch":
                number = int(match.group(1))
                if number not in builds or status["id"] > builds[number]["id"]:
                    builds[number] = status
        completed = [builds[n] for n in sorted(builds, reverse=True)
                     if builds[n]["state"] in ("success", "failure", "error")]
        if len(completed) >= 2 or not commit["parents"]:
            return completed[:2]
        commit = api(f'{BASE}/commits/{commit["parents"][0]["sha"]}')
    return completed[:2]


def status_for_event(sha, event_id, event_url):
    """In every-build mode, confirm this is the final status for that build.

    Jenkins can post multiple pending statuses before a final result. The event
    ID must match the latest status for its build, so pending or superseded
    events do not produce duplicate cards.
    """
    statuses = [status for status in api(f"{BASE}/commits/{sha}/statuses?per_page=100")
                if status.get("target_url") == event_url
                and status.get("context") == "continuous-integration/jenkins/branch"]
    latest = max(statuses, key=lambda status: status["id"], default=None)
    if latest and latest["id"] == event_id and latest["state"] in ("success", "failure", "error"):
        return latest
    return None


def message_for(event_id, event_url, builds, report_all=False):
    """Choose a message template from the build result and reporting mode.

    Return None when this status should be silent. In transition mode, two
    completed builds tell us whether this is a new RED incident or a recovery.
    """
    if not MASTER_BUILD.match(event_url) or not builds:
        return None
    latest = builds[0]
    if latest["id"] != event_id or latest["target_url"] != event_url:
        return None
    if latest["state"] not in ("success", "failure", "error"):
        return None
    values = {"state": latest["state"].upper()}
    if report_all:
        template = EVERY_BUILD_GREEN_MESSAGE if latest["state"] == "success" else EVERY_BUILD_RED_MESSAGE
        return template.format(**values)
    previous = builds[1] if len(builds) > 1 else None
    was_red = previous is not None and previous["state"] in ("failure", "error")
    if latest["state"] in ("failure", "error") and not was_red:
        return FIRST_RED_MESSAGE.format(**values)
    if latest["state"] == "success" and was_red:
        return RECOVERY_MESSAGE.format(**values)
    return None


def card_for(message, build):
    """Build a Teams card with stable Jenkins destinations and status styling.

    Teams chooses the actual shades for Good and Attention in each theme. The
    source status URL may point to a stage view, so links use canonical pages.
    """
    number = MASTER_BUILD.match(build["target_url"]).group(1)
    failed = build["state"] in ("failure", "error")
    status = build["state"].upper()
    build_url = f"{MASTER_JOB}{number}/"
    actions = [
        {"type": "Action.OpenUrl", "title": "Latest master", "url": MASTER_JOB},
        {"type": "Action.OpenUrl", "title": f"Build #{number}", "url": build_url},
    ]
    if failed:
        actions.append({"type": "Action.OpenUrl", "title": "Console output",
                        "url": f"{build_url}console"})
    return {
        "$schema": "http://adaptivecards.io/schemas/adaptive-card.json",
        "type": "AdaptiveCard",
        "version": "1.2",
        "body": [
            {"type": "Container", "style": "attention" if failed else "good", "items": [
                {"type": "TextBlock", "text": f"{'🔴' if failed else '🟢'} Logging master · #{number} · {status}",
                 "size": "Large", "weight": "Bolder", "wrap": True},
            ]},
            {"type": "TextBlock", "text": message, "wrap": True, "separator": True,
             "spacing": "Medium"},
        ],
        "actions": actions,
    }


def main():
    """Select the status, print the chosen text, then send its Adaptive Card."""
    event_id = int(os.environ["STATUS_ID"])
    event_url = os.environ.get("STATUS_URL", "")
    report_all_setting = (os.environ.get("REPORT_ALL_BUILDS") or "true").lower()
    if report_all_setting not in ("true", "false"):
        sys.exit("OPAL_MASTER_ALERT_REPORT_ALL_BUILDS must be true or false.")
    report_all = report_all_setting == "true"
    if not MASTER_BUILD.match(event_url):
        print("Ignoring a non-master Jenkins status.")
        return
    if report_all:
        current = status_for_event(os.environ["STATUS_SHA"], event_id, event_url)
        builds = [current] if current else []
    else:
        builds = completed_builds()
    message = message_for(event_id, event_url, builds, report_all)
    if not message:
        print("No completed master build result to report in this mode.")
        return
    print(message)
    webhook = os.environ.get("TEAMS_WEBHOOK_URL")
    if not webhook:
        sys.exit("OPAL_MASTER_ALERT_TEAMS_WEBHOOK is not configured.")
    # Teams Workflows requires an Adaptive Card envelope. The workflow accepts
    # the POST before Teams displays the card, so check its run if delivery fails.
    payload = {
        "type": "message",
        "attachments": [{
            "contentType": "application/vnd.microsoft.card.adaptive",
            "content": card_for(message, builds[0]),
        }],
    }
    subprocess.run(["curl", "--fail-with-body", "--silent", "--show-error",
                    "--retry", "2", "--retry-delay", "2", "-H", "Content-Type: application/json",
                    "--data", json.dumps(payload), "--output", "/dev/null", webhook], check=True)
    print("Webhook accepted the card; check the Teams workflow run for delivery.")


if __name__ == "__main__":
    main()
