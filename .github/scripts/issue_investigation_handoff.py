# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.
# cspell:ignore ffeb

"""Dispatch investigation from applied triage results, without modifying triage."""

import hashlib
import io
import json
import os
import re
import subprocess
import urllib.error
import urllib.parse
import urllib.request
import zipfile
from datetime import datetime


TRIAGE_PATH = ".github/workflows/issue-triage.lock.yml"
INVESTIGATION_FILE = "issue-investigation.lock.yml"
MAX_ARCHIVE_BYTES = 16 * 1024 * 1024
MAX_JSON_BYTES = 4 * 1024 * 1024
MAX_ENTRY_BYTES = 128 * 1024
BLOCKERS = {"needs-triage", "needs-team-triage", "issue-addressed", "needs-author-feedback"}
# Match JavaScript String.trim in the pinned producer, not Python's different Unicode whitespace set.
JS_WHITESPACE = "\u0009\u000a\u000b\u000c\u000d\u0020\u00a0\u1680\u2000\u2001\u2002\u2003\u2004\u2005" \
                "\u2006\u2007\u2008\u2009\u200a\u2028\u2029\u202f\u205f\u3000\ufeff"


class SkipHandoff(Exception):
    """Valid evidence that does not permit an automatic handoff."""


class EvidenceError(Exception):
    """Missing, malformed, or contradictory evidence; never dispatch."""


def require(condition, message):
    if not condition:
        raise EvidenceError(message)


def positive_integer(value):
    return type(value) is int and 0 < value <= 9007199254740991


def timestamp(value):
    require(isinstance(value, str), "Missing evidence timestamp.")
    parsed = datetime.fromisoformat(value.replace("Z", "+00:00"))
    require(parsed.tzinfo is not None, "Evidence timestamp has no time zone.")
    return parsed


def unique_object(pairs):
    result = {}
    for key, value in pairs:
        require(key not in result, "Duplicate JSON property.")
        result[key] = value
    return result


def parse_json(data):
    def invalid_constant(value):
        raise EvidenceError("Non-finite JSON value.")
    return json.loads(data, object_pairs_hook=unique_object, parse_constant=invalid_constant)


def read_entry(archive, name):
    require(isinstance(archive, bytes) and len(archive) <= MAX_ARCHIVE_BYTES, "Artifact archive is oversized.")
    with zipfile.ZipFile(io.BytesIO(archive)) as bundle:
        entries = bundle.infolist()
        require(len(entries) <= 4096, "Too many artifact entries.")
        matches = [entry for entry in entries if entry.filename == name]
        require(len(matches) == 1, "Required artifact entry is missing or ambiguous.")
        entry = matches[0]
        require(not entry.is_dir() and not entry.flag_bits & 1, "Unsupported artifact entry.")
        require(entry.compress_type in (zipfile.ZIP_STORED, zipfile.ZIP_DEFLATED), "Unsupported artifact compression.")
        require(entry.file_size <= MAX_ENTRY_BYTES, "Artifact entry is oversized.")
        # Read data only, never extract or execute files from the completed run.
        with bundle.open(entry) as stream:
            content = stream.read(MAX_ENTRY_BYTES + 1)
        require(len(content) <= MAX_ENTRY_BYTES, "Expanded artifact entry is oversized.")
        return content.decode("utf-8")


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, request, response, code, message, headers, new_url):
        return None


class GitHub:
    def __init__(self, repository, token, api_url):
        require(re.fullmatch(r"[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+", repository), "Invalid repository.")
        require(api_url == "https://api.github.com", "This handoff supports GitHub.com only.")
        self.prefix = f"{api_url}/repos/{repository}/"
        self.token = token
        self.opener = urllib.request.build_opener(NoRedirect)

    def request(self, path, method="GET", payload=None, limit=MAX_JSON_BYTES):
        require(not path.startswith("/") and "://" not in path, "Invalid API path.")
        headers = {
            "Authorization": f"Bearer {self.token}",
            "Accept": "application/vnd.github+json",
            "Content-Type": "application/json",
            "X-GitHub-Api-Version": "2022-11-28",
            "User-Agent": "azure-sdk-java-investigation-handoff",
        }
        data = None if payload is None else json.dumps(payload).encode("utf-8")
        url = self.prefix + path if path else self.prefix.rstrip("/")
        request = urllib.request.Request(url, data=data, headers=headers, method=method)
        with self.opener.open(request, timeout=30) as response:
            content = response.read(limit + 1)
        require(len(content) <= limit, "API response is oversized.")
        return content

    def get(self, path):
        return parse_json(self.request(path))

    def collection(self, path, key):
        values = []
        for page in range(1, 11):
            result = self.get(f"{path}?per_page=100&page={page}")
            batch = result[key] if key else result
            require(isinstance(batch, list), "Invalid API collection.")
            values.extend(batch)
            if len(batch) < 100:
                return values
        raise EvidenceError("API collection exceeds the pagination bound.")

    def archive(self, artifact_id):
        require(positive_integer(artifact_id), "Invalid artifact identifier.")
        try:
            self.request(f"actions/artifacts/{artifact_id}/zip", limit=MAX_ARCHIVE_BYTES)
        except urllib.error.HTTPError as error:
            if error.code != 302:
                raise
            location = error.headers.get("Location", "")
            error.close()
        else:
            raise EvidenceError("Expected the artifact API's signed download redirect.")
        url = urllib.parse.urlsplit(location)
        require(url.scheme == "https" and url.hostname and not url.username and not url.password,
                "Invalid signed artifact URL.")
        # The signed destination receives no GitHub Authorization header.
        request = urllib.request.Request(location, headers={"User-Agent": "azure-sdk-java-investigation-handoff"})
        with self.opener.open(request, timeout=30) as response:
            content = response.read(MAX_ARCHIVE_BYTES + 1)
        require(len(content) <= MAX_ARCHIVE_BYTES, "Downloaded artifact is oversized.")
        return content

    def dispatch(self, branch, issue_number):
        # Do not automatically retry this write: a lost response may follow a successful dispatch.
        self.request(f"actions/workflows/{INVESTIGATION_FILE}/dispatches", "POST", {
            "ref": branch,
            "inputs": {"issue_number": str(issue_number)},
        })


def validate_run(run, repository, workflow_id):
    require(isinstance(run, dict) and positive_integer(run.get("id")), "Invalid workflow run.")
    if run.get("status") != "completed" or run.get("conclusion") != "success":
        raise SkipHandoff("Triage did not complete successfully.")
    if run.get("run_attempt") != 1:
        raise SkipHandoff("Triage reruns require manual investigation; artifact attempts can be ambiguous.")
    if run.get("event") not in ("issues", "workflow_dispatch"):
        raise SkipHandoff("Not an issue-triage trigger.")
    if run.get("head_branch") != repository["default_branch"]:
        raise SkipHandoff("Triage did not run on the repository default branch.")
    require(run.get("repository", {}).get("id") == repository["id"], "Run repository mismatch.")
    require(run.get("head_repository", {}).get("id") == repository["id"], "Run head repository mismatch.")
    require(run.get("workflow_id") == workflow_id and run.get("path") == TRIAGE_PATH, "Wrong triage producer.")
    require(re.fullmatch(r"[0-9a-f]{40}", run.get("head_sha", "")), "Invalid triage source SHA.")


def validate_jobs(jobs, run_id):
    indexed = {}
    for job in jobs:
        require(job.get("run_id") == run_id and job.get("run_attempt") == 1, "Job provenance mismatch.")
        name = job.get("name")
        require(name not in indexed, "Ambiguous triage jobs.")
        indexed[name] = job
    for name in ("activation", "agent", "detection", "safe_outputs", "conclusion"):
        require(name in indexed, "Missing required triage job.")
        if indexed[name].get("status") != "completed" or indexed[name].get("conclusion") != "success":
            raise SkipHandoff("A required triage job did not succeed.")
    require("mention_owners" in indexed, "Missing owner-notification job.")
    if indexed["mention_owners"].get("conclusion") not in ("success", "skipped"):
        raise SkipHandoff("Owner notification did not complete safely.")
    return indexed


def artifact_entry(api, artifacts, name, entry, run, job):
    matches = [item for item in artifacts if item.get("name") == name]
    require(len(matches) == 1, "Missing or ambiguous triage artifact.")
    artifact = matches[0]
    require(not artifact.get("expired"), "Triage artifact has expired.")
    require(positive_integer(artifact.get("size_in_bytes")) and artifact["size_in_bytes"] <= MAX_ARCHIVE_BYTES,
            "Invalid triage artifact size.")
    provenance = artifact.get("workflow_run", {})
    require(provenance.get("id") == run["id"] and provenance.get("head_sha") == run["head_sha"],
            "Artifact run provenance mismatch.")
    require(timestamp(job["started_at"]) <= timestamp(artifact["created_at"]) <= timestamp(job["completed_at"]),
            "Artifact was not created during the expected producer job.")
    return read_entry(api.archive(artifact["id"]), entry)


def validate_activation(info, run, repository):
    require(isinstance(info, dict), "Invalid activation metadata.")
    expected = {
        "run_id": run["id"],
        "run_attempt": "1",
        "repository": repository["full_name"],
        "ref": "refs/heads/" + repository["default_branch"],
        "sha": run["head_sha"],
        "event_name": run["event"],
    }
    require(all(info.get(key) == value for key, value in expected.items()), "Activation provenance mismatch.")


def detection_clear(log):
    # Reuse the exact pinned parser rather than reinterpret its supported verdict formats.
    script = """
const fs = require('node:fs');
const path = require('node:path');
const { parseDetectionLog } = require(path.join(process.env.GH_AW_ACTIONS_DIR, 'parse_threat_detection_results.cjs'));
const result = parseDetectionLog(fs.readFileSync(0, 'utf8'));
process.stdout.write(JSON.stringify({
    valid: !result.error && !!result.verdict,
    clear: !!result.verdict && ['prompt_injection', 'secret_leak', 'malicious_patch']
        .every(field => result.verdict[field] === false)
}));
"""
    result = subprocess.run(["node", "-e", script], input=log, text=True, encoding="utf-8",
                            capture_output=True, check=True, timeout=15)
    verdict = parse_json(result.stdout)
    require(verdict.get("valid") is True, "Threat detection did not produce a valid verdict.")
    return verdict.get("clear") is True


def applied_issue(records, repository):
    require(isinstance(records, list) and len(records) <= 32 and all(isinstance(item, dict) for item in records),
            "Invalid applied-output records.")
    if not records:
        raise SkipHandoff("Triage made no recorded changes.")
    allowed = {"add_labels", "add_comment", "assign_to_user", "mention_owners"}
    if any(item.get("type") not in allowed for item in records):
        raise SkipHandoff("Triage produced a closure or another non-handoff outcome.")
    label_records = [item for item in records if item["type"] == "add_labels"]
    if len(label_records) != 1:
        raise SkipHandoff("No single applied final label decision.")
    applied = label_records[0]
    issue_number = applied.get("number")
    require(positive_integer(issue_number), "Invalid applied issue number.")
    for item in records:
        if item["type"] == "mention_owners":
            continue  # This record represents intent, not completion; the separate job was checked.
        if item["type"] == "assign_to_user":
            # v0.87.1 drops issueNumber from this result's manifest. Do not use it
            # to select or attest the target, and preserve the existing assignee.
            require(item.get("number", issue_number) == issue_number and item.get("repo", repository) == repository,
                    "Assignment result names a different target.")
        else:
            require(item.get("number") == issue_number and item.get("repo") == repository,
                    "Applied outputs target different issues or repositories.")
    before = applied.get("before_state", {})
    after = applied.get("after_state", {})
    require(isinstance(before, dict) and isinstance(after, dict), "Invalid applied issue snapshots.")
    if before.get("state") != "open" or before.get("labels") != []:
        raise SkipHandoff("The issue was not newly triaged.")
    labels = after.get("labels")
    added = applied.get("labelsAdded")
    require(isinstance(labels, list) and all(isinstance(label, str) for label in labels), "Invalid applied labels.")
    require(isinstance(added, list) and all(isinstance(label, str) for label in added), "Invalid added labels.")
    require(after.get("state") == "open" and len(labels) == len(set(labels)) and set(added).issubset(labels),
            "Inconsistent applied label state.")
    if "customer-reported" not in added or "customer-reported" not in labels or BLOCKERS.intersection(labels):
        raise SkipHandoff("Applied triage labels are not eligible.")
    if applied.get("labelsSuggested"):
        raise SkipHandoff("The label decision was not fully applied.")
    comments = [item for item in records if item["type"] == "add_comment"]
    if not 1 <= len(comments) <= 2:
        raise SkipHandoff("No completed triage analysis comment.")
    expected_url = f"https://github.com/{repository}/issues/{issue_number}#issuecomment-"
    require(all(isinstance(item.get("url"), str) and
                re.fullmatch(re.escape(expected_url) + r"[1-9][0-9]*", item["url"]) for item in comments),
            "Applied comment target mismatch.")
    return issue_number, after


def eligible_issue(issue, labels, issue_number, snapshot, repository):
    require(issue.get("number") == issue_number and
            issue.get("html_url") == f"https://github.com/{repository}/issues/{issue_number}", "Issue target mismatch.")
    if "pull_request" in issue or issue.get("state") != "open":
        raise SkipHandoff("Target is not an open issue.")
    require(isinstance(labels, list) and all(isinstance(label, dict) and
            isinstance(label.get("name"), str) and isinstance(label.get("color"), str) for label in labels),
            "Label names and colors are required.")
    names = [label["name"] for label in labels]
    if (len(names) != len(set(names)) or "customer-reported" not in names or BLOCKERS.intersection(names)
            or sum(label["color"].lower().lstrip("#") == "e99695" for label in labels) != 1
            or sum(label["color"].lower().lstrip("#") == "ffeb77" for label in labels) != 1):
        raise SkipHandoff("Fresh issue labels do not pass the investigation handoff.")
    body = issue.get("body") or ""
    require(isinstance(body, str), "Invalid issue body.")
    normalized = "\n".join(re.sub(r"[ \t]+$", "", line)
                           for line in body.replace("\r\n", "\n").split("\n")).strip(JS_WHITESPACE)
    body_hash = hashlib.sha256(normalized.encode("utf-8")).hexdigest()
    if (issue.get("title") != snapshot.get("title") or body_hash != snapshot.get("body_hash")
            or not set(snapshot["labels"]).issubset(names)):
        raise SkipHandoff("Issue evidence changed after triage.")


def handoff(api, event, repository_name, repository_id, attempt, verify_detection=None):
    if attempt != "1":
        raise SkipHandoff("Do not replay an automatic handoff; dispatch may already have succeeded.")
    require(isinstance(event, dict) and event.get("action") == "completed", "Unexpected workflow-run action.")
    repository = api.get("")
    require(repository.get("full_name") == repository_name and repository.get("id") == repository_id,
            "Repository identity mismatch.")
    workflow = api.get("actions/workflows/issue-triage.lock.yml")
    require(workflow.get("path") == TRIAGE_PATH and positive_integer(workflow.get("id")), "Invalid triage workflow.")
    trigger = event.get("workflow_run", {})
    validate_run(trigger, repository, workflow["id"])
    run = api.get(f"actions/runs/{trigger['id']}")
    validate_run(run, repository, workflow["id"])
    require(run["id"] == trigger["id"] and run["head_sha"] == trigger["head_sha"], "Trigger and API run mismatch.")
    jobs = validate_jobs(api.collection(f"actions/runs/{run['id']}/attempts/1/jobs", "jobs"), run["id"])
    artifacts = api.collection(f"actions/runs/{run['id']}/artifacts", "artifacts")
    info = parse_json(artifact_entry(api, artifacts, "activation", "aw_info.json", run, jobs["activation"]))
    validate_activation(info, run, repository)
    detection = artifact_entry(api, artifacts, "detection", "detection.log", run, jobs["detection"])
    if not (verify_detection or detection_clear)(detection):
        raise SkipHandoff("Threat detection did not clear the triage outputs.")
    manifest = artifact_entry(api, artifacts, "safe-outputs-items", "safe-output-items.jsonl", run, jobs["safe_outputs"])
    records = [parse_json(line) for line in manifest.splitlines() if line.strip()]
    # Baseline activation has no originating issue field. Use only the unique issue
    # positively labeled and commented on by this completed triage run.
    number, snapshot = applied_issue(records, repository_name)
    issue = api.get(f"issues/{number}")
    labels = api.collection(f"issues/{number}/labels", None)
    eligible_issue(issue, labels, number, snapshot, repository_name)
    latest = api.get(f"actions/runs/{run['id']}")
    validate_run(latest, repository, workflow["id"])
    require(latest["head_sha"] == run["head_sha"], "Triage run changed during handoff.")
    api.dispatch(repository["default_branch"], number)
    return number


def main():
    try:
        with open(os.environ["GITHUB_EVENT_PATH"], "rb") as stream:
            data = stream.read(MAX_JSON_BYTES + 1)
        require(len(data) <= MAX_JSON_BYTES, "Workflow event is oversized.")
        api = GitHub(os.environ["GITHUB_REPOSITORY"], os.environ["GH_TOKEN"], os.environ["GITHUB_API_URL"])
        number = handoff(api, parse_json(data), os.environ["GITHUB_REPOSITORY"],
                         int(os.environ["GITHUB_REPOSITORY_ID"]), os.environ["GITHUB_RUN_ATTEMPT"])
        print(f"Dispatched investigation for the applied triage issue #{number}.")
    except SkipHandoff as error:
        print(f"Investigation not dispatched: {error}")
    except (EvidenceError, ValueError, KeyError, TypeError, OSError, zipfile.BadZipFile,
            subprocess.SubprocessError) as error:
        # Do not print API bodies, signed URLs, tokens, or issue-sourced content.
        detail = str(error) if isinstance(error, EvidenceError) else type(error).__name__
        print(f"::error::Investigation handoff failed closed: {detail} Existing triage is unaffected.")
        raise SystemExit(1) from None


if __name__ == "__main__":
    main()
