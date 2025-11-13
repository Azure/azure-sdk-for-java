---
# Fill in the fields below to create a basic custom agent for your repository.
# The Copilot CLI can be used for local testing: https://gh.io/customagents/cli
# To make this agent available, merge this file into the default repository branch.
# For format details, see: https://gh.io/customagents/config

name: Update Spring versions
description: Create a task to update Spring Boot and Spring Cloud versions.
---

# My Agent

Task: Create a PR to upgrade Spring dependencies to the target version by running our repository Python scripts. Confirm Spring Cloud ↔ Spring Boot compatibility using official references and include the conclusion and links in the PR.

Requirements:
- Run the repository’s Python scripts(use `python3`).
- In the PR, state the compatibility conclusion (compatible / not compatible / conditionally compatible) and include official reference links to the Spring Cloud release train ↔ Spring Boot compatibility.
- If execution is not possible, fall back to directly editing files to produce the expected outputs, and note this in the PR.

Milestone/Snapshot rule:
- If either target version is a Milestone (contains `-M` or `-RC`) or a Snapshot (contains `-SNAPSHOT`), add Spring milestone (and optionally snapshot) repositories to the build files (Maven) for dependency resolution.
  - Spring Milestone: https://repo.spring.io/milestone
  - Spring Snapshot: https://repo.spring.io/snapshot (only if snapshot artifacts are needed)

Steps (existing flow):
1) Compatibility check:
   - Verify whether Spring Cloud <SPRING_CLOUD_VERSION> is compatible with Spring Boot <SPRING_BOOT_VERSION>, citing official docs/compatibility table.
   - Add the conclusion and links in the PR. If incompatible, propose the nearest compatible Spring Cloud version, and mark the PR as "needs review".

2) Generate managed external dependencies:
   - Run:
     pip install termcolor
     python ./sdk/spring/scripts/get_spring_boot_managed_external_dependencies.py -b ${SPRING_BOOT_VERSION} -c ${SPRING_CLOUD_VERSION}
   - Output file:
     ./sdk/spring/scripts/spring_boot_${SPRING_BOOT_VERSION}_managed_external_dependencies.txt

3) Sync external dependencies:
   - Run:
     pip install termcolor
     pip install in_place
     python ./sdk/spring/scripts/sync_external_dependencies.py -b ${SPRING_BOOT_VERSION} -sbmvn 3
   - Target file:
     ./eng/versioning/external_dependencies.txt

4) Update version aggregation:
   - Run:
     python ./eng/versioning/update_versions.py --sr
   - Target file: all pom.xml under ./sdk/spring

5) Update Spring changelog:
   - Run:
     python ./sdk/spring/scripts/update_changelog.py -b ${SPRING_BOOT_VERSION} -c ${SPRING_CLOUD_VERSION}
   - Target file:
     ./sdk/spring/CHANGELOG.md (add a new entry: date, versions, brief notes)

6) Cleanup old files:
   - Remove previous ./sdk/spring/scripts/spring_boot_*_managed_external_dependencies.txt files, keep only the current ${SPRING_BOOT_VERSION}.
   - List removed files in the PR.

7) Run command from repo root to validate the Spring SDK module build after the upgrade:
   - Run:
     mvn clean install -Dcheckstyle.skip=true -Dcodesnippet.skip -Denforcer.skip -Djacoco.skip=true -Dmaven.javadoc.skip=true -Drevapi.skip=true -DskipTests -Dspotbugs.skip=true -Pdev -T 4 -ntp -f sdk/spring/pom.xml
   - If the build FAILS:
     - Do NOT modify repository files yet. Do NOT commit any changes.
     - Capture and paste the root cause from the logs (first failing module, key exception, dependency conflict).
     - Provide at least two remediation options. For each option include:
      - scope of changes (files, dependencies, BOM/exclusions, or code migrations)
      - pros/cons and risk level
      - a preview patch (unapplied unified diff) if file edits are involved
    - Post the analysis as a PR comment under the section “Build Failure Analysis”.
    - WAIT for explicit approval before proceeding. Only act after a reviewer comments:
      - APPROVE OPTION 1
      - APPROVE OPTION 2
      - APPROVE OPTION <N> WITH NOTES: <free text>
    - After approval:
      - Implement ONLY the approved option.
      - Commit and push changes.
      - Update the PR description: add “Applied Remediation” with the chosen option and rationale.
      - Re-run the build command and post the result.

Build file alignment (if present):
- For pom.xml:
  - Align to official BOMs (Spring Boot BOM and Spring Cloud BOM if applicable), remove redundant explicit versions.
  - Apply the Milestone/Snapshot rule: if target versions are Milestone/RC/Snapshot, add the Spring milestone/snapshot repositories (Maven) required for resolution.
  - Keep changes minimal and focused.

Branch & commit:
- Branch: chore/spring-upgrade-${SPRING_BOOT_VERSION}-${SPRING_CLOUD_VERSION}
- Commit message: chore(spring): upgrade Spring Boot to ${SPRING_BOOT_VERSION} and Spring Cloud to ${SPRING_CLOUD_VERSION}

Open a PR with this description structure (Markdown):
1. Summary: purpose and scope
2. Target Versions: Boot/Cloud targets
3. Compatibility: conclusion + official links (mandatory)
4. Files Changed: key changes (managed_external_dependencies, external_dependencies, CHANGELOG, build file alignment if any)
5. Breaking Changes & Notes: e.g., javax.* -> jakarta.*, Security DSL changes, Actuator exposure differences (brief)
6. Checklist:
   - [ ] Generated & synced spring_boot_${SPRING_BOOT_VERSION}_managed_external_dependencies.txt
   - [ ] Updated eng/versioning/external_dependencies.txt
   - [ ] Updated all versions under ./sdk/spring
   - [ ] Updated sdk/spring/CHANGELOG.md
   - [ ] Removed old spring_boot_*_managed_external_dependencies.txt
   - [ ] Run command from repo root to validate the Spring SDK module build after the upgrade
   - [ ] Milestone/Snapshot repositories added (if applicable)

Please create the branch, run the commands above (or produce equivalent edits if execution is not possible), and open the PR with the specified description and labels.
