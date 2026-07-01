#!/usr/bin/env python3
"""Generate sdk/spring/pipeline/spring-cloud-azure-supported-spring.json."""

import argparse
import json
import os
import re
import urllib.request

SPRING_METADATA_URL = "https://spring.io/project_metadata/spring-boot"
SPRING_INITIALIZR_INFO_URL = "https://start.spring.io/actuator/info"
SUPPORT_FILE = "sdk/spring/pipeline/spring-cloud-azure-supported-spring.json"
NONE_SUPPORTED = "NONE_SUPPORTED_SPRING_CLOUD_VERSION"


def fetch_json(url):
    req = urllib.request.Request(url, headers={"User-Agent": "spring-cloud-azure-tools-migration"})
    with urllib.request.urlopen(req, timeout=30) as resp:
        return json.loads(resp.read().decode("utf-8"))


def tokenize_version(version):
    tokens = []
    for part in re.split(r"[.\-]", version):
        if part.isdigit():
            tokens.append((0, int(part)))
        else:
            upper = part.upper()
            if upper.startswith("SNAPSHOT"):
                tokens.append((1, -3))
            elif upper.startswith("M") and upper[1:].isdigit():
                tokens.append((1, -2, int(upper[1:])))
            elif upper.startswith("RC") and upper[2:].isdigit():
                tokens.append((1, -1, int(upper[2:])))
            else:
                tokens.append((1, upper))
    return tokens


def compare_versions(a, b):
    ta = tokenize_version(a)
    tb = tokenize_version(b)
    max_len = max(len(ta), len(tb))
    for i in range(max_len):
        va = ta[i] if i < len(ta) else (0, 0)
        vb = tb[i] if i < len(tb) else (0, 0)
        if va == vb:
            continue
        return -1 if va < vb else 1
    return 0


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
    for cloud_version, expr in spring_cloud_ranges.items():
        if in_range(spring_boot_version, parse_range_expression(expr)):
            return cloud_version
    return NONE_SUPPORTED


def is_snapshot_milestone_or_rc(version, include_rc):
    if version is None:
        return False
    has_snapshot_or_milestone = "SNAPSHOT" in version or "M" in version
    has_rc = "RC" in version
    return has_snapshot_or_milestone or (has_rc and not include_rc)


def is_version_supported(version):
    return compare_versions(version, "3.5.0") >= 0


def load_existing_support_map():
    with open(SUPPORT_FILE, "r", encoding="utf-8") as f:
        existing = json.load(f)
    return {item.get("spring-boot-version"): item for item in existing if item.get("spring-boot-version")}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--include-rc", action="store_true", help="Include RC versions in generated output")
    args = parser.parse_args()

    spring_metadata = fetch_json(SPRING_METADATA_URL)
    releases = spring_metadata.get("projectReleases", [])

    initializr_info = fetch_json(SPRING_INITIALIZR_INFO_URL)
    spring_cloud_ranges = (
        initializr_info.get("bom-ranges", {}).get("spring-cloud", {})
        or initializr_info.get("build", {}).get("bom-ranges", {}).get("spring-cloud", {})
        or initializr_info.get("build", {}).get("versions", {}).get("spring-cloud", {})
        or initializr_info.get("serviceCapabilities", {}).get("bom", {}).get("spring-cloud", {})
        or initializr_info.get("serviceBom", {}).get("spring-cloud", {})
    )
    if not spring_cloud_ranges:
        raise RuntimeError("Cannot locate spring-cloud compatibility map in Spring Initializr response")

    existing_map = load_existing_support_map()
    active_versions = set()
    current_items = []

    for release in releases:
        version = release.get("version")
        if not version:
            continue
        if not is_version_supported(version):
            continue
        if is_snapshot_milestone_or_rc(version, args.include_rc):
            continue

        existing = existing_map.get(version, {})
        support_status = existing.get("supportStatus")
        if support_status is None:
            if release.get("releaseStatus") in ("GENERAL_AVAILABILITY", "PRERELEASE"):
                support_status = "SUPPORTED"
            else:
                support_status = "TODO"

        cloud_version = existing.get("spring-cloud-version")
        if existing.get("supportStatus") != "END_OF_LIFE":
            cloud_version = find_compatible_spring_cloud_version(version, spring_cloud_ranges)
            if cloud_version == NONE_SUPPORTED and support_status == "SUPPORTED":
                # Keep matrix entries non-supported when Initializr does not provide a compatible Spring Cloud BOM.
                support_status = "TODO"

        current_items.append(
            {
                "current": bool(release.get("current", False)),
                "releaseStatus": release.get("releaseStatus"),
                "snapshot": bool(release.get("snapshot", False)),
                "supportStatus": support_status,
                "spring-boot-version": version,
                "spring-cloud-version": cloud_version,
            }
        )
        active_versions.add(version)

    snapshot_items = []
    for version, metadata in existing_map.items():
        if version in active_versions:
            continue
        if is_snapshot_milestone_or_rc(version, args.include_rc):
            continue
        cloned = dict(metadata)
        cloned["current"] = False
        if version != "2.7.18":
            cloned["supportStatus"] = "END_OF_LIFE"
        snapshot_items.append(cloned)

    result = current_items + snapshot_items
    result.sort(key=lambda item: tokenize_version(item.get("spring-boot-version", "0")), reverse=True)

    os.makedirs(os.path.dirname(SUPPORT_FILE), exist_ok=True)
    with open(SUPPORT_FILE, "w", encoding="utf-8") as f:
        json.dump(result, f, indent=2)
        f.write("\n")


if __name__ == "__main__":
    main()
