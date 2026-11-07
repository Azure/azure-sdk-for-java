# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.
# cspell:ignore ffeb

import copy
import hashlib
import io
import json
import subprocess
import unittest
import urllib.error
import warnings
import zipfile
from unittest.mock import patch

import issue_investigation_handoff as module


REPOSITORY = "example/java-sdk"
SHA = "a" * 40
START = "2026-10-16T10:00:00Z"
END = "2026-10-16T10:05:00Z"
NOW = "2026-10-16T10:01:00Z"


def zipped(name, content):
    output = io.BytesIO()
    with zipfile.ZipFile(output, "w", zipfile.ZIP_DEFLATED) as archive:
        archive.writestr(name, content)
    return output.getvalue()


class FakeAPI:
    def __init__(self):
        self.writes = []
        self.reads = []
        self.repository = {"id": 100, "full_name": REPOSITORY, "default_branch": "test/branch"}
        self.workflow = {"id": 200, "path": module.TRIAGE_PATH}
        self.run = {
            "id": 300, "workflow_id": 200, "path": module.TRIAGE_PATH, "status": "completed",
            "conclusion": "success", "run_attempt": 1, "event": "issues", "head_branch": "test/branch",
            "head_sha": SHA, "repository": {"id": 100}, "head_repository": {"id": 100},
        }
        self.event = {"action": "completed", "workflow_run": copy.deepcopy(self.run)}
        self.jobs = [
            {"name": name, "run_id": 300, "run_attempt": 1, "status": "completed", "conclusion": "success",
             "started_at": START, "completed_at": END}
            for name in ("activation", "agent", "detection", "safe_outputs", "conclusion", "mention_owners")
        ]
        self.info = {
            "run_id": 300, "run_attempt": "1", "repository": REPOSITORY,
            "ref": "refs/heads/test/branch", "sha": SHA, "event_name": "issues",
        }
        snapshot = {
            "title": "Example", "body_hash": hashlib.sha256(b"Report").hexdigest(),
            "state": "open", "labels": ["KeyVault", "Mgmt", "customer-reported"], "assignees": ["owner1"],
        }
        self.records = [
            {"type": "add_labels", "number": 17, "repo": REPOSITORY, "labelsAdded": snapshot["labels"].copy(),
             "before_state": {**snapshot, "labels": []}, "after_state": snapshot},
            {"type": "add_comment", "number": 17, "repo": REPOSITORY,
             "url": f"https://github.com/{REPOSITORY}/issues/17#issuecomment-123"},
        ]
        self.issue = {
            "number": 17, "html_url": f"https://github.com/{REPOSITORY}/issues/17",
            "state": "open", "title": "Example", "body": "Report", "assignees": [{"login": "owner1"}],
        }
        self.labels = [
            {"name": "KeyVault", "color": "E99695"}, {"name": "Mgmt", "color": "#ffeb77"},
            {"name": "customer-reported", "color": "eeeeee"},
        ]
        self.artifacts = [
            {"id": index, "name": name, "expired": False, "size_in_bytes": 4096,
             "workflow_run": {"id": 300, "head_sha": SHA}, "created_at": NOW}
            for index, name in ((1, "activation"), (2, "safe-outputs-items"), (3, "detection"))
        ]
        self.latest = None
        self.override_archives = {}
        self.run_reads = 0

    def get(self, path):
        self.reads.append(path)
        if path == "":
            return self.repository
        if path == "actions/workflows/issue-triage.lock.yml":
            return self.workflow
        if path == "actions/runs/300":
            self.run_reads += 1
            return self.latest if self.latest is not None and self.run_reads == 2 else self.run
        if path == "issues/17":
            return self.issue
        raise AssertionError(f"Unexpected read: {path}")

    def collection(self, path, key):
        self.reads.append(path)
        if path == "actions/runs/300/attempts/1/jobs":
            return self.jobs
        if path == "actions/runs/300/artifacts":
            return self.artifacts
        if path == "issues/17/labels":
            return self.labels
        raise AssertionError(f"Unexpected collection: {path}")

    def archive(self, artifact_id):
        if artifact_id in self.override_archives:
            return self.override_archives[artifact_id]
        if artifact_id == 1:
            return zipped("aw_info.json", json.dumps(self.info))
        if artifact_id == 2:
            return zipped("safe-output-items.jsonl", "\n".join(json.dumps(item) for item in self.records))
        if artifact_id == 3:
            return zipped("detection.log", "THREAT_DETECTION_RESULT:" + json.dumps({
                "prompt_injection": False, "secret_leak": False, "malicious_patch": False, "reasons": []
            }))
        raise AssertionError("Unexpected artifact")

    def dispatch(self, branch, issue_number):
        self.writes.append((branch, issue_number))


def run(api, attempt="1"):
    return module.handoff(api, api.event, REPOSITORY, 100, attempt, lambda log: True)


class HandoffTests(unittest.TestCase):
    def denied(self, mutate, exception=(module.SkipHandoff, module.EvidenceError), attempt="1"):
        api = FakeAPI()
        mutate(api)
        with self.assertRaises(exception):
            run(api, attempt)
        self.assertEqual([], api.writes)

    def test_success_is_one_dispatch_with_no_issue_writes(self):
        api = FakeAPI()
        before = copy.deepcopy((api.issue, api.labels, api.records))
        self.assertEqual(17, run(api))
        self.assertEqual([("test/branch", 17)], api.writes)
        self.assertEqual(before, (api.issue, api.labels, api.records))
        self.assertEqual(2, api.run_reads)

    def test_both_supported_triggers_and_skipped_owner(self):
        for trigger in ("issues", "workflow_dispatch"):
            for result in ("success", "skipped"):
                with self.subTest(trigger=trigger, owner=result):
                    api = FakeAPI()
                    api.run["event"] = api.event["workflow_run"]["event"] = api.info["event_name"] = trigger
                    api.jobs[-1]["conclusion"] = result
                    self.assertEqual(17, run(api))

    def test_all_run_identity_and_completion_gates(self):
        variants = {
            "status": "in_progress", "conclusion": "failure", "run_attempt": 2, "event": "pull_request",
            "head_branch": "untrusted", "workflow_id": 999, "path": "other.yml", "head_sha": "bad",
            "repository": {"id": 101}, "head_repository": {"id": 101},
        }
        for field, value in variants.items():
            with self.subTest(field=field):
                self.denied(lambda api: api.run.update({field: value}))
        self.denied(lambda api: api.event.update(action="requested"))
        self.denied(lambda api: api.repository.update(id=101))
        self.denied(lambda api: api.repository.update(full_name="another/repo"))
        self.denied(lambda api: api.workflow.update(path="wrong.yml"))
        self.denied(lambda api: api.event["workflow_run"].update(head_sha="b" * 40))
        self.denied(lambda api: None, attempt="2")

    def test_failures_cancellations_and_missing_jobs_never_dispatch(self):
        for index in range(6):
            for result in ("failure", "cancelled", "timed_out"):
                with self.subTest(job=index, result=result):
                    self.denied(lambda api: api.jobs[index].update(conclusion=result))
            self.denied(lambda api: api.jobs.pop(index))
        self.denied(lambda api: api.jobs.append(api.jobs[0]))
        self.denied(lambda api: api.jobs[0].update(run_attempt=2))
        self.denied(lambda api: api.jobs[0].update(run_id=999))

    def test_activation_metadata_is_bound_to_exact_run_and_attempt(self):
        for field, value in {
            "run_id": 301, "run_attempt": "2", "repository": "other/repo", "ref": "refs/heads/main",
            "sha": "b" * 40, "event_name": "pull_request",
        }.items():
            with self.subTest(field=field):
                self.denied(lambda api: api.info.update({field: value}))

    def test_artifacts_require_exact_producer_and_upload_window(self):
        variants = {
            "expired": True, "size_in_bytes": module.MAX_ARCHIVE_BYTES + 1, "created_at": "2026-10-15T00:00:00Z",
            "workflow_run": {"id": 999, "head_sha": SHA},
        }
        for index in (0, 1, 2):
            for field, value in variants.items():
                with self.subTest(artifact=index, field=field):
                    self.denied(lambda api: api.artifacts[index].update({field: value}))
            self.denied(lambda api: api.artifacts.pop(index))
        self.denied(lambda api: api.artifacts.append(copy.deepcopy(api.artifacts[0])))
        self.denied(lambda api: api.artifacts[0].update(workflow_run={"id": 300, "head_sha": "b" * 40}))

    def test_detection_warning_does_not_dispatch_even_with_successful_jobs(self):
        api = FakeAPI()
        with self.assertRaises(module.SkipHandoff):
            module.handoff(api, api.event, REPOSITORY, 100, "1", lambda log: False)
        self.assertEqual([], api.writes)

    def test_noop_fallback_close_and_ambiguous_output_paths(self):
        self.denied(lambda api: api.records.clear(), module.SkipHandoff)
        self.denied(lambda api: api.records.pop(0))
        self.denied(lambda api: api.records.pop(1))
        self.denied(lambda api: api.records.append(copy.deepcopy(api.records[0])))
        for output in ("close_issue", "noop", "report_incomplete", "create_issue"):
            self.denied(lambda api: api.records.append({"type": output}))
        for number in (0, -1, True, "17", "17oops", 1.5, 9007199254740992):
            self.denied(lambda api: api.records[0].update(number=number))
        self.denied(lambda api: api.records[1].update(number=18))
        self.denied(lambda api: api.records[0].update(repo="other/repo"))
        self.denied(lambda api: api.records[1].update(url=f"https://github.com/{REPOSITORY}/pull/17#issuecomment-123"))
        self.denied(lambda api: api.records[1].update(url="https://example.invalid/"))
        self.denied(lambda api: api.records[0]["before_state"].update(labels=["already-triaged"]))
        self.denied(lambda api: api.records[0]["before_state"].update(state="closed"))
        self.denied(lambda api: api.records[0].update(labelsSuggested=["Mgmt"]))
        self.denied(lambda api: api.records[0].update(labelsAdded=["Mgmt"]))
        self.denied(lambda api: api.records[0]["after_state"]["labels"].append("needs-team-triage"))
        self.denied(lambda api: api.records[0].update(before_state=[]), module.EvidenceError)
        self.denied(lambda api: api.records[0].update(after_state=[]), module.EvidenceError)

    def test_malformed_json_and_oversized_record_sets_fail_closed(self):
        self.denied(lambda api: api.override_archives.update({1: zipped("aw_info.json", "{")}), ValueError)
        self.denied(lambda api: api.override_archives.update({2: zipped("safe-output-items.jsonl", "null")}),
                    module.EvidenceError)
        self.denied(lambda api: api.records.extend([{"type": "mention_owners"}] * 33), module.EvidenceError)

    def test_fresh_issue_guards_and_changed_evidence(self):
        for mutate in (
            lambda api: api.issue.update(state="closed"),
            lambda api: api.issue.update(pull_request={}),
            lambda api: api.issue.update(number=18),
            lambda api: api.issue.update(html_url="https://github.com/other/repo/issues/17"),
            lambda api: api.issue.update(body="Changed"),
            lambda api: api.issue.update(title="Changed"),
            lambda api: api.labels.pop(0),
            lambda api: api.labels.pop(1),
            lambda api: api.labels.pop(2),
            lambda api: api.labels.append({"name": "Second service", "color": "e99695"}),
            lambda api: api.labels.append({"name": "Client", "color": "ffeb77"}),
            lambda api: api.labels.append(api.labels[0]),
            lambda api: api.labels[0].pop("color"),
        ):
            self.denied(mutate)
        for label in module.BLOCKERS:
            self.denied(lambda api: api.labels.append({"name": label, "color": "eeeeee"}))

    def test_new_attempt_during_handoff_and_network_failure(self):
        self.denied(lambda api: setattr(api, "latest", {**api.run, "run_attempt": 2}))
        api = FakeAPI()
        with patch.object(api, "archive", side_effect=OSError("Unavailable")):
            with self.assertRaises(OSError):
                run(api)
        self.assertEqual([], api.writes)
        with patch.object(api, "dispatch", side_effect=OSError("Response lost")) as dispatch:
            with self.assertRaises(OSError):
                run(api)
            self.assertEqual(1, dispatch.call_count)

    def test_all_existing_category_names_and_human_assignments_remain_supported(self):
        for category in ("Client", "Mgmt", "Provisioning", "Service", "Central-EngSys"):
            with self.subTest(category=category):
                api = FakeAPI()
                api.labels[1]["name"] = category
                api.records[0]["after_state"]["labels"][1] = category
                api.records[0]["labelsAdded"][1] = category
                api.records.insert(1, {"type": "assign_to_user", "timestamp": NOW})
                api.records.append({"type": "mention_owners"})
                self.assertEqual(17, run(api))
                self.assertEqual([{"login": "owner1"}], api.issue["assignees"])

    def test_assignment_records_are_not_used_as_target_evidence(self):
        self.denied(lambda api: api.records.append({"type": "assign_to_user", "number": 18}))
        self.denied(lambda api: api.records.append({"type": "assign_to_user", "repo": "other/repo"}))
        self.denied(lambda api: api.records[0].pop("repo"))
        self.denied(lambda api: api.records[1].pop("repo"))
        self.denied(lambda api: api.records[0].update(type="assign_to_user"))

    def test_body_hash_uses_native_runtime_whitespace_normalization(self):
        api = FakeAPI()
        api.issue["body"] = " \r\nReport \t\r\n"
        self.assertEqual(17, run(api))

    def test_unicode_body_hash_matches_javascript_not_python_trimming(self):
        for body, normalized in (
            ("\ufeff\u00a0Report\u3000\ufeff", "Report"),
            ("\u0085Report\u0085", "\u0085Report\u0085"),
            ("\u180eReport\u180e", "\u180eReport\u180e"),
        ):
            with self.subTest(body=repr(body)):
                api = FakeAPI()
                api.issue["body"] = body
                api.records[0]["after_state"]["body_hash"] = hashlib.sha256(normalized.encode("utf-8")).hexdigest()
                self.assertEqual(17, run(api))


class ArtifactTests(unittest.TestCase):
    def test_only_selected_json_is_read_without_extracting_neighbor_files(self):
        output = io.BytesIO()
        with zipfile.ZipFile(output, "w") as archive:
            archive.writestr("../../outside.py", "raise Exception('never execute')")
            archive.writestr("aw_info.json", '{"run_id": 3}')
        self.assertEqual('{"run_id": 3}', module.read_entry(output.getvalue(), "aw_info.json"))

    def test_missing_duplicate_oversized_and_corrupt_entries(self):
        with self.assertRaises(module.EvidenceError):
            module.read_entry(zipped("wrong.json", "{}"), "aw_info.json")
        output = io.BytesIO()
        with warnings.catch_warnings():
            warnings.simplefilter("ignore", UserWarning)
            with zipfile.ZipFile(output, "w") as archive:
                archive.writestr("aw_info.json", "{}")
                archive.writestr("aw_info.json", "{}")
        with self.assertRaises(module.EvidenceError):
            module.read_entry(output.getvalue(), "aw_info.json")
        with self.assertRaises(module.EvidenceError):
            module.read_entry(zipped("aw_info.json", "a" * (module.MAX_ENTRY_BYTES + 1)), "aw_info.json")
        with self.assertRaises(module.EvidenceError):
            module.read_entry(b"a" * (module.MAX_ARCHIVE_BYTES + 1), "aw_info.json")
        with self.assertRaises(zipfile.BadZipFile):
            module.read_entry(b"not an archive", "aw_info.json")

    def test_duplicate_json_properties_rejected(self):
        with self.assertRaises(module.EvidenceError):
            module.parse_json('{"run_id":300,"run_id":301}')
        with self.assertRaises(module.EvidenceError):
            module.parse_json('{"nested":{"id":1,"id":2}}')

    def test_nonfinite_json_values_rejected(self):
        for constant in ("NaN", "Infinity", "-Infinity"):
            with self.subTest(constant=constant):
                with self.assertRaises(module.EvidenceError):
                    module.parse_json('{"number":' + constant + '}')

    def test_byte_limit_and_utf8_handling(self):
        content = "a" * module.MAX_ENTRY_BYTES
        self.assertEqual(content, module.read_entry(zipped("aw_info.json", content), "aw_info.json"))
        with self.assertRaises(UnicodeDecodeError):
            module.read_entry(zipped("aw_info.json", b"\xff"), "aw_info.json")


class TransportTests(unittest.TestCase):
    def test_authenticated_json_request_uses_exact_repository_url(self):
        api = module.GitHub(REPOSITORY, "test-token", "https://api.github.com")
        with patch.object(api.opener, "open") as opened:
            opened.return_value.__enter__.return_value.read.return_value = b"{}"
            api.get("")
            request = opened.call_args.args[0]
            self.assertEqual(f"https://api.github.com/repos/{REPOSITORY}", request.full_url)
            self.assertEqual("Bearer test-token", request.get_header("Authorization"))
            self.assertEqual("application/json", request.get_header("Content-type"))

    def test_fixed_api_destination_and_write_payload(self):
        api = module.GitHub(REPOSITORY, "test-token", "https://api.github.com")
        with patch.object(api, "request", return_value=b"") as request:
            api.dispatch("test/branch", 17)
            request.assert_called_once_with("actions/workflows/issue-investigation.lock.yml/dispatches", "POST",
                                            {"ref": "test/branch", "inputs": {"issue_number": "17"}})
        with self.assertRaises(module.EvidenceError):
            module.GitHub(REPOSITORY, "token", "https://example.invalid")
        with self.assertRaises(module.EvidenceError):
            module.GitHub("../other/repo", "token", "https://api.github.com")

    def test_download_redirect_never_forwards_github_token(self):
        api = module.GitHub(REPOSITORY, "secret-test-token", "https://api.github.com")
        redirect = urllib.error.HTTPError("https://api.github.com/example", 302, "Found",
                                          {"Location": "https://artifact.example.invalid/signed"}, None)
        with patch.object(api, "request", side_effect=redirect), patch.object(api.opener, "open") as opened:
            opened.return_value.__enter__.return_value.read.return_value = b"archive"
            self.assertEqual(b"archive", api.archive(123))
            request = opened.call_args.args[0]
            self.assertIsNone(request.get_header("Authorization"))
            self.assertNotIn("secret-test-token", request.full_url)
        for location in ("http://artifact.example.invalid", "https://user:password@example.invalid"):
            with self.subTest(location=location):
                redirect = urllib.error.HTTPError("https://api.github.com/example", 302, "Found",
                                                  {"Location": location}, None)
                with patch.object(api, "request", side_effect=redirect):
                    with self.assertRaises(module.EvidenceError):
                        api.archive(123)

    def test_api_collection_pagination_and_bound(self):
        api = module.GitHub(REPOSITORY, "token", "https://api.github.com")
        with patch.object(api, "get", side_effect=[{"jobs": list(range(100))}, {"jobs": [100]}]) as get:
            self.assertEqual(list(range(101)), api.collection("actions/runs/3/jobs", "jobs"))
            self.assertEqual(2, get.call_count)
        with patch.object(api, "get", return_value={"jobs": [0] * 100}):
            with self.assertRaises(module.EvidenceError):
                api.collection("actions/runs/3/jobs", "jobs")


class PinnedDetectionTests(unittest.TestCase):
    def test_body_hashes_match_the_actual_pinned_producer(self):
        bodies = ["Report", " \r\nReport \t\r\n", "\ufeffReport\ufeff", "\u0085Report\u0085",
                  "\u180eReport\u180e", "\u00a0Report\u3000", "Report \t\u2028", "a \t\u2028b"]
        script = """
const fs = require('node:fs'), path = require('node:path');
const { hashNormalizedBody } = require(path.join(process.env.GH_AW_ACTIONS_DIR, 'safe_output_execution_metadata.cjs'));
process.stdout.write(JSON.stringify(JSON.parse(fs.readFileSync(0, 'utf8')).map(hashNormalizedBody)));
"""
        hashes = json.loads(subprocess.run(["node", "-e", script], input=json.dumps(bodies), text=True,
                                           capture_output=True, check=True, timeout=15).stdout)
        for body, expected in zip(bodies, hashes):
            with self.subTest(body=repr(body)):
                api = FakeAPI()
                api.issue["body"] = body
                api.records[0]["after_state"]["body_hash"] = expected
                self.assertEqual(17, run(api))

    def test_handoff_uses_native_clear_verdict_end_to_end(self):
        api = FakeAPI()
        self.assertEqual(17, module.handoff(api, api.event, REPOSITORY, 100, "1"))
        self.assertEqual([("test/branch", 17)], api.writes)

    def test_handoff_suppresses_native_threat_verdict_end_to_end(self):
        api = FakeAPI()
        api.override_archives[3] = zipped("detection.log",
            'THREAT_DETECTION_RESULT:{"prompt_injection":true,"secret_leak":false,"malicious_patch":false}')
        with self.assertRaises(module.SkipHandoff):
            module.handoff(api, api.event, REPOSITORY, 100, "1")
        self.assertEqual([], api.writes)

    def test_native_clear_and_threat_verdicts(self):
        for field in ("prompt_injection", "secret_leak", "malicious_patch"):
            for present in (False, True):
                verdict = {"prompt_injection": False, "secret_leak": False, "malicious_patch": False, "reasons": []}
                verdict[field] = present
                line = "THREAT_DETECTION_RESULT:" + json.dumps(verdict)
                for log in (line, "**" + line + "**", json.dumps({"type": "result", "result": line})):
                    with self.subTest(field=field, present=present, format=log[:20]):
                        self.assertEqual(not present, module.detection_clear(log))

    def test_native_missing_malformed_and_conflicting_verdicts(self):
        clear = 'THREAT_DETECTION_RESULT:{"prompt_injection":false,"secret_leak":false,"malicious_patch":false}'
        for log in ("", "No verdict", 'THREAT_DETECTION_RESULT:{"prompt_injection":"false"}',
                    clear + "\n" + clear.replace('"prompt_injection":false', '"prompt_injection":true')):
            with self.subTest(log=log):
                with self.assertRaises(module.EvidenceError):
                    module.detection_clear(log)


if __name__ == "__main__":
    unittest.main()
