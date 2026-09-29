# Java documentation change classifier

This classifier runs in **report-only mode**: it evaluates documentation,
ordinary-comment, and formatting changes separately for each Maven library,
but never changes which tests run. The existing path
classifier, PackageInfo files, Build, Analyze, and CHANGELOG-only routing stay
unchanged.

## Eligibility policy

The observer requires an exact, checked-out, two-parent PR merge commit and
compares its complete tree against the first parent. In pipeline mode it also
checks the source commit and the public canonical repository/main PR context.
Source is read from Git objects, not the working tree.

All Track 2 data-plane libraries are considered automatically. The observer reads
each changed module's `pom.xml` from the PR commit and recognizes the repository's
Azure client Maven groups (`com.azure` and `com.azure.v2`), the
`azure-client-sdk-parent` family in those groups, and JAR packaging.
An omitted group inherits from the declared parent; omitted packaging means JAR.
No per-library JSON allowlist, parent-file list, or recorded POM hashes are needed.

Control-plane/management libraries and older Track 1 data-plane libraries are
excluded. Spring, standalone tools, performance projects, and POM-only modules
also do not match this client-library scope. This follows the repository's
[client and historical release documentation](../../README.md#available-packages)
and the client-group/parent conventions in
[`Language-Settings.ps1`](../scripts/Language-Settings.ps1) and
[`generate_aggregate_pom.py`](../scripts/generate_aggregate_pom.py).

Only modified regular, non-executable Java files under a matching module's
`src/main/java` directory are candidates. Library detection does not bypass the
source comparison or prove that tests can safely be skipped. We have not tried
the classifier on real PRs in CI.

JavaParser parses both versions of the modified files as Java 8. Whitespace
between code tokens and ordinary prose comments may change, even without a
Javadoc edit. Identifiers, operators, literals, annotations, and other code tokens
must remain identical. Whitespace or comment-like text inside a string or
character literal is not ignored. Adding a comment that hides executable code
does not qualify.

Existing Javadoc must remain attached to the same declaration, using positions
among code tokens rather than positions that count whitespace/comments.
Adding, removing, or moving Javadoc remains unsupported, as do edits involving
`@deprecated` or unsupported documentation tags.

Tool directives must keep their text and position relative to code. This includes
snippet boundaries (`BEGIN:`/`END:`), generated-code markers, formatter and
inspection directives, and unrecognized directive-shaped comments such as
`@generate`, `#if`, or `custom-generator: enabled`. These are not treated as ordinary prose.
Unchanged directives can coexist with formatting changes elsewhere. Line-ending
conversion is supported, including inside otherwise unchanged comments.

Unicode escape sequences, byte-order marks, non-UTF-8 source, and unsupported Java
syntax remain excluded. Build and Analyze still check formatting, generated
documentation, and snippet synchronization.

Additions, deletions, renames, mode changes, package-info, module-info, test/sample
Java, libraries outside the Track 2 data-plane scope, and mixed functional changes
do not qualify for exclusion from runtime test matrices. These changes stop
evaluation of their owning library, not unrelated libraries. Known consumer
documents use the existing non-runtime path policy in `Classify-PRChanges.ps1`.
A library's POM change still prevents its exclusion; POM changes already in the
target branch do not require updating this classifier.

## Library-level decisions

A library is a Maven module under `sdk/<service>/<library>` with a regular
`pom.xml` in the before or after snapshot. Different libraries within the same
service are evaluated separately.

| Changes | Library results |
| --- | --- |
| Key Vault Secrets Javadoc and App Configuration Javadoc | Both eligible |
| Key Vault Secrets code and App Configuration Javadoc | Secrets not eligible; App Configuration still evaluated |
| Key Vault Secrets test resources and App Configuration Javadoc | Secrets not eligible; App Configuration still evaluated |

The first triggering or unsupported source change leaves the rest of that
library's source candidates `not-evaluated`. A source parse/read failure is
inconclusive for that library; evaluation continues for the other libraries.
Every candidate in a library must pass before that library is eligible.

Shared service inputs, such as `sdk/keyvault/ci.yml` or unowned test resources
under that service, block exclusions for that service. Repository-level files,
engineering inputs, and shared parents block exclusions for all changed
libraries. Missing ownership is never interpreted as a harmless change.

**A library result describes its own changes, not dependency impact.** A library
with only Javadoc edits can still need tests when another changed library is one
of its dependencies. The report sets `DependencyImpactEvaluated=false`; it does
not remove packages from the existing test matrices or the From Source dependent
test set.

There is no candidate file-count limit. The 2 MiB limit per source blob and
comparison timeout remain in place. Oversized sources do not qualify; timeouts,
a missing or failed parser, incomplete output, invalid history, or invalid source
produce an explicit inconclusive result.

Java source is read one before/after pair at a time and sent to one Java process
reused across libraries. The observer waits for each result before fetching the
next pair. Once a file prevents a library's exclusion, no later source candidates
from that library are fetched or parsed, but other libraries continue.
Larger PRs use the same streaming comparison without additional pipeline jobs.
An unusable parser process or exhausted comparison timeout is a shared failure:
results from that comparison session remain inconclusive, not eligible.

Build and Analyze retain compilation, Javadoc, API, sample, snippet, and generator
checks. Changes to executable samples are a separate future policy. Documentation
changes can still affect generated documentation, source positions, or
comment-consuming tools; eligibility does not prove equivalent behavior.

## Local use

PowerShell 7, Git, Maven, and a JDK are required. The engineering CI uses JDK 17;
the Java tool targets Java 8. JavaParser and build plugin versions use the existing
`eng/versioning/external_dependencies.txt` pins.

```powershell
mvn -B --no-transfer-progress -f eng\java-doc-classifier\pom.xml package

.\eng\scripts\Measure-JavaDocChanges.ps1 `
    -RepositoryRoot (Get-Location).Path `
    -ExpectedHeadSha (git rev-parse HEAD) `
    -ExpectedSourceSha (git rev-parse HEAD^2) `
    -ParserJar eng\java-doc-classifier\target\java-doc-classifier.jar `
    -OutputPath java-doc-report.json `
    -PassThru
```

HEAD must be the PR's synthetic merge commit, which combines the working branch
with the target branch for validation, not the working branch tip. A missing
explicit `ParserJar` is an error. If the argument is omitted, the observer builds
the small tool on demand, only after library-scope and changed-path checks find
candidates. It never builds the SDK or the baseline revision.

Use `-ForceFullValidation` or `FORCE_FULL_VALIDATION=true` to disable evaluation of
whether runtime tests can be omitted.

Report schema version 2 contains exact revision IDs, the `track2-data-plane`
library scope, per-library decisions in `Libraries`, shared inputs in
`SharedChanges`, per-file reasons, and `SuppressionApplied=false`.
Library decisions are `Eligible`, `NotEligible`, or `Inconclusive`. The PR summary
also supports `PartiallyEligible`:

| Decision | Meaning |
| --- | --- |
| `Eligible` | All changed libraries are eligible and no shared input blocks the PR. |
| `PartiallyEligible` | Some libraries are eligible, while others or shared inputs still require validation. |
| `NotEligible` | No library qualifies, or repository-wide input changes block exclusions. |
| `Inconclusive` | Required setup, source parsing, snapshot data, or output was invalid. |

Accepted per-file reasons distinguish `javadoc-only`, `ordinary-comment-only`,
`whitespace-only`, and `non-code-only` (a combination of these changes).
An unchanged source is not counted as an eligible edit. A library with only known
consumer-documentation changes uses `existing-non-runtime-validation` without
starting the Java parser.

Each library has its own `DecisionFile`, candidate/compared counts, Maven
coordinates, and files. The top-level `EligibleLibraryCount` reports how many
libraries passed; the whole-PR `WouldSuppressTests` remains false for a mixed PR.
`ComparedFileCount` counts well-formed parser responses, and each file's
`Compared` flag distinguishes compared sources from unexamined ones.
A failed parser exit, unexpected output, or missing response invalidates that
comparison session even if earlier files received positive results. The parser
is stopped and cleaned up on failures and timeouts.

## Pipeline reporting

The Java-owned `ci.yml` template adds an opt-in step to the existing matrix job,
before the unchanged path classifier. Set `JavaDocClassifierReportOnly=true` to
enable an approved reporting run. The default is off. No extra job or
Build/Analyze dependency is introduced.

The step is limited to public canonical auto/client PR validation; the observer
further restricts the target to main and verifies the pipeline's exact merge and
source SHAs. Service, private, manual, scheduled, and release validation retain
their existing behavior.

The diagnostic output variables are `JavaDocReportEligible`,
`JavaDocReportDecision`, `JavaDocReportReason`, `JavaDocReportMilliseconds`, and
`JavaDocReportEligibleLibraryCount`.
The JSON report is uploaded as a task attachment. No output controls a matrix,
and the observer never emits `JavaTestsSuppressed`.

Count worker-backed test agent-minutes for eligible attempts and subtract
classifier/tool-bootstrap overhead. Measure elapsed pipeline time separately.
The first Maven invocation on an agent may require dependency downloads.

Skipping tests requires a separate change, verification of build/generator
behavior, hosted results, and approval. Do not connect the report to the
future CHANGELOG-only Build/Analyze bypass.

## Validation

Run the focused suites after building the parser:

```powershell
Import-Module Pester -RequiredVersion 5.7.1
Invoke-Pester -Path @(
    'eng/scripts/tests/JavaDoc-Changes.tests.ps1',
    'eng/scripts/tests/Classify-PRChanges.tests.ps1',
    'eng/scripts/tests/PullRequest-Trigger.tests.ps1'
) -Tag UnitTest -Output Detailed
```

Java unit tests cover token preservation, formatting, ordinary-comment edits,
stable Javadoc attachment, deprecation, tool directives, Unicode, comments versus
literals, malformed source, exact 2 MiB UTF-8 byte boundaries, per-pair flushing,
more than 100 source pairs, and early termination.
Pester uses disposable local Git repositories and the real parser to cover
complete PR snapshots, target advancement, shallow history, sparse/dirty
worktrees, automatic library detection, management/Track 1 exclusions, invalid
POM metadata, mixed libraries and per-file change categories, service/repository
shared inputs, PRs above the former file limit, first/middle/last rejection,
unread later source blobs, process cleanup, limits, force override, context
guards, and incomplete-result handling.
Parser setup is mandatory; missing dependencies are not reported as skipped or
passing tests.

The existing engineering script CI builds/tests this tool before Pester on its
Windows and Linux legs. No shared `eng/common` files are changed.
