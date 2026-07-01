#!/usr/bin/env python3
"""Generate spring-versions.txt and pr-descriptions.txt for Spring dependency update workflows."""

import argparse
import html
import json
import re
import urllib.error
import urllib.request

SPRING_METADATA_URL = "https://spring.io/project_metadata/spring-boot"
SPRING_INITIALIZR_INFO_URL = "https://start.spring.io/actuator/info"
SPRING_BOOT_RELEASE_TAG_URL = "https://github.com/spring-projects/spring-boot/releases/tag/v{}"
SPRING_BOOT_RELEASE_API_URL = "https://api.github.com/repos/spring-projects/spring-boot/releases/tags/v{}"
EXTERNAL_DEPENDENCIES_FILE = "eng/versioning/external_dependencies.txt"
SUPPORT_MATRIX_FILE = "sdk/spring/pipeline/spring-cloud-azure-supported-spring.json"
SPRING_VERSIONS_OUTPUT = "spring-versions.txt"
PR_DESCRIPTIONS_OUTPUT = "pr-descriptions.txt"


def version_key(version):
    match = re.match(r"^(\d+)(?:\.(\d+))?(?:\.(\d+))?(?:[-.]?([A-Za-z]+)(\d*)?)?$", version)
    if not match:
        return (0, 0, 0, 0, 0, version)

    major = int(match.group(1) or 0)
    minor = int(match.group(2) or 0)
    patch = int(match.group(3) or 0)
    qualifier = (match.group(4) or "").upper()
    qualifier_num = int(match.group(5) or 0)

    # Higher rank means newer release status for the same base version.
    if not qualifier:
        qualifier_rank = 3  # GA
    elif qualifier.startswith("RC"):
        qualifier_rank = 2
    elif qualifier.startswith("M"):
        qualifier_rank = 1
    elif qualifier.startswith("SNAPSHOT"):
        qualifier_rank = 0
    else:
        qualifier_rank = 0

    return (major, minor, patch, qualifier_rank, qualifier_num, qualifier)


def fetch_json(url):
    req = urllib.request.Request(url, headers={"User-Agent": "spring-cloud-azure-tools-migration"})
    with urllib.request.urlopen(req, timeout=30) as resp:
        return json.loads(resp.read().decode("utf-8"))


def compare_versions(a, b):
    ka = version_key(a)
    kb = version_key(b)
    if ka == kb:
        return 0
    if ka < kb:
        return -1
    return 1


def sort_versions_desc(versions):
    return sorted(versions, key=version_key, reverse=True)


def read_current_supported_versions():
    # Prefer the support matrix because it reflects the current Spring compatibility target.
    boot = None
    cloud = None
    try:
        with open(SUPPORT_MATRIX_FILE, "r", encoding="utf-8") as f:
            entries = json.load(f)
        current_entries = [
            e for e in entries
            if e.get("current") and e.get("supportStatus") == "SUPPORTED"
        ]
        if current_entries:
            current = current_entries[0]
            boot = current.get("spring-boot-version")
            cloud = current.get("spring-cloud-version")
    except (FileNotFoundError, json.JSONDecodeError, TypeError):
        pass

    if boot and cloud:
        return boot, cloud

    # Fallback to external dependencies for compatibility with older layouts.
    boot_candidates = {
        "org.springframework.boot:spring-boot-dependencies",
        "org.springframework.boot:spring-boot-starter-parent",
        "org.springframework.boot:spring-boot-starter",
        "org.springframework.boot:spring-boot-maven-plugin",
    }
    with open(EXTERNAL_DEPENDENCIES_FILE, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#") or ";" not in line:
                continue
            artifact, version = line.split(";", 1)
            if artifact in boot_candidates and not boot:
                boot = version
            elif artifact == "org.springframework.cloud:spring-cloud-dependencies" and not cloud:
                cloud = version
    if not boot or not cloud:
        raise RuntimeError(
            "Failed to read current Spring Boot/Cloud versions from support matrix and external_dependencies.txt"
        )
    return boot, cloud


def parse_range_expression(expr):
    rules = []
    for token in expr.split():
        m = re.match(r"^([><])(=*)(.*)$", token)
        if not m:
            continue
        op = m.group(1)
        inclusive = m.group(2) == "="
        version = m.group(3)
        rules.append((op, inclusive, version))
    if not rules:
        raise RuntimeError("Cannot parse Spring Initializr range: {}".format(expr))
    return rules


def in_range(version, rules):
    for op, inclusive, bound in rules:
        cmp_result = compare_versions(version, bound)
        if op == ">":
            if cmp_result < 0 or (cmp_result == 0 and not inclusive):
                return False
        elif op == "<":
            if cmp_result > 0 or (cmp_result == 0 and not inclusive):
                return False
    return True


def find_compatible_spring_cloud_version(spring_boot_version, spring_cloud_ranges):
    for cloud_version in sort_versions_desc(list(spring_cloud_ranges.keys())):
        expr = spring_cloud_ranges[cloud_version]
        if in_range(spring_boot_version, parse_range_expression(expr)):
            return cloud_version
    raise RuntimeError(
        "No compatible spring-cloud version found for spring-boot {}".format(spring_boot_version)
    )


def release_notes_html(version):
    release_url = SPRING_BOOT_RELEASE_TAG_URL.format(version)
    try:
        body = fetch_json(SPRING_BOOT_RELEASE_API_URL.format(version)).get("body", "")
    except (urllib.error.HTTPError, urllib.error.URLError):
        body = ""

    if body:
        body = body.replace("\r\n", "\n")
        body = re.split(r"\n##\s+.*Contributors.*", body, maxsplit=1, flags=re.IGNORECASE)[0]
        body = html.escape(body).replace("\n", "<br/>")
    else:
        body = "Release notes unavailable from GitHub API."

    return (
        "<details><summary>Release notes</summary>"
        "<p><em>Sourced from <a href='{}'>spring-boot releases</a>.</em></p>"
        "{}"
        "</details>".format(release_url, body)
    )


def write_outputs(target_boot, target_cloud, current_boot, current_cloud, notes):
    with open(SPRING_VERSIONS_OUTPUT, "w", encoding="utf-8") as f:
        f.write(target_boot + "\n")
        f.write(target_cloud + "\n")
        f.write(current_boot + "\n")
        f.write(current_cloud + "\n")

    with open(PR_DESCRIPTIONS_OUTPUT, "w", encoding="utf-8") as f:
        f.write(notes)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--target-major", default="4", help="Target Spring Boot major version.")
    args = parser.parse_args()

    spring_metadata = fetch_json(SPRING_METADATA_URL)
    releases = spring_metadata.get("projectReleases", [])
    ga_versions = [
        r.get("version") for r in releases
        if r.get("releaseStatus") == "GENERAL_AVAILABILITY" and r.get("version", "").startswith(args.target_major)
    ]
    rc_versions = [
        r.get("version") for r in releases
        if r.get("releaseStatus") == "PRERELEASE" and r.get("version", "").startswith(args.target_major)
    ]
    ga_versions = [v for v in ga_versions if v]
    rc_versions = [v for v in rc_versions if v]

    if not ga_versions and not rc_versions:
        return

    latest_ga = sort_versions_desc(ga_versions)[0] if ga_versions else None
    latest_rc = sort_versions_desc(rc_versions)[0] if rc_versions else None

    current_boot, current_cloud = read_current_supported_versions()
    is_new_ga = latest_ga is not None and compare_versions(latest_ga, current_boot) > 0
    is_new_rc = latest_rc is not None and compare_versions(latest_rc, current_boot) > 0

    if not is_new_ga and not is_new_rc:
        return

    initializr_info = fetch_json(SPRING_INITIALIZR_INFO_URL)
    spring_cloud_ranges = (
        initializr_info
        .get("bom-ranges", {})
        .get("spring-cloud", {})
    )
    if not spring_cloud_ranges:
        spring_cloud_ranges = (
            initializr_info
            .get("build", {})
            .get("bom-ranges", {})
            .get("spring-cloud", {})
        )
    if not spring_cloud_ranges:
        spring_cloud_ranges = (
            initializr_info
            .get("build", {})
            .get("versions", {})
            .get("spring-cloud", {})
        )
    if not spring_cloud_ranges:
        spring_cloud_ranges = (
            initializr_info
            .get("serviceCapabilities", {})
            .get("bom", {})
            .get("spring-cloud", {})
        )
    if not spring_cloud_ranges:
        spring_cloud_ranges = (
            initializr_info
            .get("serviceBom", {})
            .get("spring-cloud", {})
        )

    if not spring_cloud_ranges:
        raise RuntimeError("Cannot locate spring-cloud compatibility map in Spring Initializr response")

    target_boot = latest_ga if is_new_ga else latest_rc
    if is_new_ga:
        target_cloud = find_compatible_spring_cloud_version(target_boot, spring_cloud_ranges)
    else:
        try:
            target_cloud = find_compatible_spring_cloud_version(target_boot, spring_cloud_ranges)
        except RuntimeError:
            if latest_ga is None:
                raise
            target_cloud = find_compatible_spring_cloud_version(latest_ga, spring_cloud_ranges)

    write_outputs(target_boot, target_cloud, current_boot, current_cloud, release_notes_html(target_boot))


if __name__ == "__main__":
    main()
