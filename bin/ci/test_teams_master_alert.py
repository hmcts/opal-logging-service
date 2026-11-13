"""Focused transition checks for logging master Teams alerts."""

import importlib.util
from pathlib import Path
import unittest
from unittest.mock import patch


spec = importlib.util.spec_from_file_location(
    "teams_master_alert", Path(__file__).with_name("teams-master-alert.py"))
alert = importlib.util.module_from_spec(spec)
spec.loader.exec_module(alert)


def status(number, state, identifier):
    return {
        "id": identifier,
        "state": state,
        "context": "continuous-integration/jenkins/branch",
        "target_url": ("https://sds-build.hmcts.net/job/HMCTS/job/opal-logging-service/"
                       f"job/master/{number}/display/redirect"),
    }


class MasterAlertTest(unittest.TestCase):
    def test_first_red_and_recovery(self):
        red = status(417, "failure", 17)
        green = status(416, "success", 16)
        message = alert.message_for(17, red["target_url"], [red, green])
        self.assertIn("Master is RED", message)
        self.assertIn("Master is RED", alert.message_for(17, red["target_url"], [red]))

        recovered = status(418, "success", 18)
        message = alert.message_for(18, recovered["target_url"], [recovered, red])
        self.assertIn("Master recovered", message)

    def test_continuing_red_and_unrelated_status_are_silent(self):
        red = status(418, "error", 18)
        previous = status(417, "failure", 17)
        self.assertIsNone(alert.message_for(18, red["target_url"], [red, previous]))
        self.assertIsNone(alert.message_for(18, red["target_url"].replace("master", "demo"),
                                             [red, previous]))

    def test_pending_and_superseded_events_are_silent(self):
        pending = status(418, "pending", 18)
        previous = status(417, "failure", 17)
        self.assertIsNone(alert.message_for(18, pending["target_url"], [previous]))
        self.assertIsNone(alert.message_for(17, previous["target_url"], [pending, previous]))

    def test_report_all_sends_each_completed_build(self):
        green = status(418, "success", 18)
        red = status(417, "failure", 17)
        self.assertIn("Build finished successfully",
                      alert.message_for(18, green["target_url"], [green], report_all=True))
        self.assertIn("Build finished FAILURE",
                      alert.message_for(17, red["target_url"], [red], report_all=True))

    def test_card_uses_build_links_instead_of_jenkins_stage_url(self):
        green = status(419, "success", 19)
        red = status(420, "failure", 20)
        base = alert.MASTER_JOB

        green_card = alert.card_for("Build finished successfully.", green)
        self.assertEqual("good", green_card["body"][0]["style"])
        self.assertEqual("Large", green_card["body"][0]["items"][0]["size"])
        self.assertEqual([base, f"{base}419/"],
                         [action["url"] for action in green_card["actions"]])

        red_card = alert.card_for("Master is RED.", red)
        self.assertEqual("attention", red_card["body"][0]["style"])
        self.assertEqual([base, f"{base}420/", f"{base}420/console"],
                         [action["url"] for action in red_card["actions"]])

    def test_report_all_ignores_pending_and_superseded_statuses(self):
        pending = status(418, "pending", 18)
        complete = status(418, "success", 19)
        with patch.object(alert, "api", return_value=[complete, pending]):
            self.assertIsNone(alert.status_for_event("sha", 18, pending["target_url"]))
            self.assertEqual(complete, alert.status_for_event("sha", 19, complete["target_url"]))

    def test_build_number_wins_over_late_older_completion(self):
        newer = status(418, "failure", 18)
        older = status(417, "success", 19)
        commits = {
            "master": {"sha": "head", "parents": [{"sha": "parent"}]},
            "head": {"sha": "head", "parents": [{"sha": "parent"}]},
            "parent": {"sha": "parent", "parents": []},
        }

        def fake_api(path):
            if "/statuses?" in path:
                return [newer] if "/head/" in path else [older]
            return commits[path.rsplit("/", 1)[1]]

        with patch.object(alert, "api", side_effect=fake_api):
            self.assertEqual([18, 19], [s["id"] for s in alert.completed_builds()])


if __name__ == "__main__":
    unittest.main()
