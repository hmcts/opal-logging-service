#!/usr/bin/env python3
"""Send logging-service master build results to the pilot Teams channel.

Jenkins posts a GitHub commit status when a build starts and finishes. The GitHub
Action passes that status to this script. We accept only the regular logging
``master`` job; PR, nightly and environment jobs are ignored.

With REPORT_ALL_BUILDS=true, report each completed build, including successful
ones. With it false, report the first RED build, a new-code build that remains
RED, and the next GREEN recovery.
Pending builds never send a message. Compare build SHAs to distinguish new code
from a repeated build; compare completed results to identify recovery. Neither
comparison proves why a build failed or whether a merge was appropriate.
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


def previous_builds(sha, number):
    """Find the preceding numbered build and preceding completed result.

    Both must have a lower build number than the event. The preceding build
    could still be pending, whereas a result comparison needs a completed one.
    Follow first parents so a new merge commit can see earlier master builds.
    """
    builds = {}
    for _ in range(40):
        for status in api(f"{BASE}/commits/{sha}/statuses?per_page=100"):
            match = MASTER_BUILD.match(status.get("target_url") or "")
            if not match or status.get("context") != "continuous-integration/jenkins/branch":
                continue
            build_number = int(match.group(1))
            if build_number >= number:
                continue
            if build_number not in builds or status["id"] > builds[build_number]["id"]:
                builds[build_number] = {**status, "sha": sha}
        ordered = [builds[n] for n in sorted(builds, reverse=True)]
        previous_numbered = ordered[0] if ordered else None
        previous_completed = next((build for build in ordered
                                   if build["state"] in ("success", "failure", "error")), None)
        if previous_numbered and previous_completed:
            return previous_numbered, previous_completed
        commit = api(f"{BASE}/commits/{sha}")
        if not commit["parents"]:
            break
        sha = commit["parents"][0]["sha"]
    return previous_numbered, previous_completed


def message_for(build, previous_numbered, previous_completed, report_all=False):
    """Describe the result transition and whether the built code changed.

    An unknown earlier result is not called GREEN, and a successful rebuild is
    not described as a confirmed fix to the underlying problem.
    """
    failed = build["state"] in ("failure", "error")
    was_red = previous_completed and previous_completed["state"] in ("failure", "error")
    new_code_since_result = previous_completed and build["sha"] != previous_completed["sha"]
    if not report_all and (failed and was_red and not new_code_since_result or
                           not failed and not was_red):
        return None
    same_code = previous_numbered and build["sha"] == previous_numbered["sha"]
    prior_pending = previous_numbered and previous_numbered["state"] == "pending"
    if failed:
        if was_red:
            if prior_pending:
                result = ("Master remains RED after new code was built; the preceding build "
                          "is still running." if new_code_since_result else
                          "Master remains RED while the preceding build is still running.")
            elif same_code:
                result = "Rebuild remains RED."
            elif previous_numbered:
                result = "Master remains RED after new code was built."
            else:
                result = "Master remains RED; the previous build is unavailable."
        elif previous_completed:
            if prior_pending:
                result = ("Master has turned RED after new code was built; the preceding build "
                          "is still running." if new_code_since_result else
                          "Master has turned RED while the preceding build is still running.")
            elif same_code:
                result = "Master has turned RED on unchanged code."
            elif previous_numbered:
                result = "Master has turned RED after new code was built."
            else:
                result = "Master has turned RED; the previous build is unavailable."
        else:
            result = "Master build is RED; the previous result is unavailable."
        return f"{result} Check master before routine merges and coordinate investigation."
    if was_red:
        if prior_pending:
            result = "Master is GREEN again while the preceding build is still running."
        elif same_code:
            result = "Rebuild is GREEN; master has recovered."
        elif previous_numbered:
            result = "Master is GREEN again after new code was built."
        else:
            result = "Master is GREEN again; the previous build is unavailable."
    elif previous_completed:
        if prior_pending:
            result = "Master is GREEN while the preceding build is still running."
        elif same_code:
            result = "Rebuild remains GREEN."
        elif previous_numbered:
            result = "Master remains GREEN after new code was built."
        else:
            result = "Master remains GREEN; the previous build is unavailable."
    else:
        result = "Master build is GREEN; the previous result is unavailable."
    return f"{result} Check the latest master result before routine merges."


def merged_pr_for_commit(sha):
    """Return a PR only when GitHub confirms this SHA is its master merge commit.

    The association endpoint can also return PRs containing an earlier commit.
    Such associations must not be presented as a merge that triggered this build.
    PR metadata is optional; an API error must not suppress the build alert.
    """
    try:
        associated = api(f"{BASE}/commits/{sha}/pulls?per_page=100")
        merged = [pr for pr in associated if pr.get("merged_at")
                  and pr.get("merge_commit_sha") == sha
                  and pr.get("base", {}).get("ref") == "master"]
        if len(merged) != 1:
            return None
        return api(f'{BASE}/pulls/{merged[0]["number"]}')
    except subprocess.CalledProcessError:
        print("PR metadata unavailable; sending build result without attribution.", file=sys.stderr)
        return None


def card_for(message, build, merged_pr=None, new_code=False):
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
    details = []
    if merged_pr:
        pr_number = merged_pr["number"]
        merged_by = (merged_pr.get("merged_by") or {}).get("login")
        description = f"Merged PR #{pr_number}"
        if merged_by:
            description += f" by @{merged_by}"
        details.append({"type": "TextBlock", "text": description + ".", "wrap": True,
                        "size": "Small", "spacing": "Small"})
        actions.append({"type": "Action.OpenUrl", "title": f"PR #{pr_number}",
                        "url": merged_pr["html_url"]})
    elif new_code:
        short_sha = build["sha"][:7]
        details.append({"type": "TextBlock", "text": f"Commit {short_sha}; no merged PR verified.",
                        "wrap": True, "size": "Small", "spacing": "Small"})
        actions.append({"type": "Action.OpenUrl", "title": f"Commit {short_sha}",
                        "url": f"https://github.com/hmcts/{REPO}/commit/{build['sha']}"})
    return {
        "$schema": "http://adaptivecards.io/schemas/adaptive-card.json",
        "type": "AdaptiveCard",
        "version": "1.2",
        "body": [
            {"type": "Container", "style": "attention" if failed else "good", "items": [
                {"type": "TextBlock", "text": f"{'🔴' if failed else '🟢'} Logging Service master · #{number} · {status}",
                 "size": "Large", "weight": "Bolder", "wrap": True},
            ]},
            {"type": "TextBlock", "text": message, "wrap": True, "separator": True,
             "spacing": "Medium"},
        ] + details,
        "actions": actions,
    }


def send_card(card, webhook):
    """Post an Adaptive Card envelope to the existing Teams workflow webhook."""
    payload = {
        "type": "message",
        "attachments": [{"contentType": "application/vnd.microsoft.card.adaptive", "content": card}],
    }
    subprocess.run(["curl", "--fail-with-body", "--silent", "--show-error",
                    "--retry", "2", "--retry-delay", "2", "-H", "Content-Type: application/json",
                    "--data", json.dumps(payload), "--output", "/dev/null", webhook], check=True)
    print("Webhook accepted the card; check the Teams workflow run for delivery.")


def preview_red_card():
    """Render a conspicuous test card without changing any Jenkins result."""
    sample = {"state": "failure", "target_url": f"{MASTER_JOB}420/display/redirect"}
    message = ("🧪 TEST ONLY — no build has failed. This previews the RED alert layout. "
               "The buttons point to the previously successful build #420.")
    card = card_for(message, sample)
    card["body"][0]["items"][0]["text"] = "🧪 TEST ONLY · Logging Service RED card preview"
    if os.environ.get("SEND_TEST") != "true":
        print(json.dumps(card, indent=2))
        print("Preview only; no Teams message sent.")
        return
    webhook = os.environ.get("TEAMS_WEBHOOK_URL")
    if not webhook:
        sys.exit("OPAL_MASTER_ALERT_TEAMS_WEBHOOK is not configured.")
    send_card(card, webhook)


def main():
    """Select the status, find earlier builds, then send its Adaptive Card."""
    if os.environ.get("PREVIEW_RED_CARD") == "true":
        preview_red_card()
        return
    event_id = int(os.environ["STATUS_ID"])
    event_url = os.environ.get("STATUS_URL", "")
    report_all_setting = (os.environ.get("REPORT_ALL_BUILDS") or "true").lower()
    if report_all_setting not in ("true", "false"):
        sys.exit("OPAL_MASTER_ALERT_REPORT_ALL_BUILDS must be true or false.")
    report_all = report_all_setting == "true"
    if not MASTER_BUILD.match(event_url):
        print("Ignoring a non-master Jenkins status.")
        return
    sha = os.environ["STATUS_SHA"]
    current = status_for_event(sha, event_id, event_url)
    if not current:
        print("No completed master build result to report in this mode.")
        return
    current = {**current, "sha": sha}
    if not report_all:
        builds = completed_builds()
        if not builds or builds[0]["id"] != event_id or builds[0]["target_url"] != event_url:
            print("A newer completed master build superseded this event.")
            return
    number = int(MASTER_BUILD.match(event_url).group(1))
    previous_numbered, previous_completed = previous_builds(sha, number)
    message = message_for(current, previous_numbered, previous_completed, report_all)
    if not message:
        print("No completed master build result to report in this mode.")
        return
    print(message)
    webhook = os.environ.get("TEAMS_WEBHOOK_URL")
    if not webhook:
        sys.exit("OPAL_MASTER_ALERT_TEAMS_WEBHOOK is not configured.")
    new_code = (previous_numbered is not None and previous_numbered["sha"] != sha or
                previous_completed is not None and previous_completed["sha"] != sha)
    merged_pr = merged_pr_for_commit(sha) if new_code else None
    # The webhook accepts a POST before Teams displays the card; inspect its run
    # history if delivery fails after this call succeeds.
    send_card(card_for(message, current, merged_pr, new_code), webhook)


if __name__ == "__main__":
    main()
