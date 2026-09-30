---
applyTo: "**/pom.xml,eng/versioning/*.txt"
description: "Review Maven dependency, BOM, and version catalog changes for release and compatibility regressions."
---

# Maven dependencies and versioning review

For SDK package and parent/BOM changes, verify the effective dependency
versions and the resulting build or release behavior before flagging a
manifest difference. Normal synchronized version churn is not a finding.

- Check version-update tags against `eng/versioning/version_client.txt` for
  Azure libraries and `eng/versioning/external_dependencies.txt` for third
  parties. Within the same release group use the current library version;
  outside it prefer the released dependency version unless an explicitly
  tracked unreleased additive dependency is required. Flag a mismatch only
  when it builds against the wrong API or breaks versioning automation.
- New dependencies should be necessary, approved where required, and
  compatible with the package's parent/BOM. Avoid snapshot dependencies in
  shipping libraries. Do not add `azure-identity` as a compile-scope client
  dependency when azure-core credential abstractions suffice.
- For a new shipping module, verify its service aggregator/CI registration
  and version catalog entry so it can actually build and release. For
  Spring modules, respect their BOM and supported Spring dependency versions;
  the [Spring instructions](spring.instructions.md) take precedence.
- Compare new public APIs and package versions with the last GA release
  before calling a change incompatible. A patch release should not add
  public features; a preview package can evolve differently. Do not treat
  every dependency or version bump as a compatibility defect.

Sources: [Java versioning and dependencies](https://azure.github.io/azure-sdk/java_introduction.html#java-version-semver),
[Spring dependencies](https://azure.github.io/azure-sdk/java_spring.html#java-spring-dependency-approval),
and [repository versioning rules](../../../CONTRIBUTING.md#versions-and-versioning).
