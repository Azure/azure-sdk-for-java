# Java documentation change classifier

This classifier runs in **report-only mode**: it reports whether Java
documentation, ordinary-comment, and formatting changes could qualify for
skipping runtime tests, but never changes which tests run. The existing path
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
do not qualify for exclusion from runtime test matrices. Other changed paths must
already have a non-runtime validation route in `Classify-PRChanges.ps1`.
Comparison is all-or-nothing, not package-level pruning. A POM change in the PR
still prevents test exclusion; POM changes already in the target branch do not
require updating this classifier.

There is no candidate file-count limit. The 2 MiB limit per source blob and
comparison timeout remain in place. Oversized sources do not qualify; timeouts,
a missing or failed parser, incomplete output, invalid history, or invalid source
produce an explicit inconclusive result.

Java source is read one before/after pair at a time and sent to one Java process.
The observer waits for that comparison's result before fetching another pair.
As soon as a file prevents test exclusion, remaining source files are neither
fetched nor parsed. Library detection also stops at the first unsupported module.
The existing changed-path checks can rule out a PR before any Java comparison.
The parser must return a result for every candidate before the PR can be eligible.
Larger PRs use the same streaming comparison without additional pipeline jobs.

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

The result contains exact revision IDs, the `track2-data-plane` library scope,
Maven coordinates, per-file reasons, elapsed time, and `SuppressionApplied=false`.
Decisions are:

| Decision | Meaning |
| --- | --- |
| `Eligible` | All changed paths satisfy the criteria for potential runtime-test exclusion. |
| `NotEligible` | A supported check ruled out skipping runtime tests, or the scope is unsupported. |
| `Inconclusive` | Required setup, source parsing, snapshot data, or output was invalid. |

Accepted per-file reasons distinguish `javadoc-only`, `ordinary-comment-only`,
`whitespace-only`, and `non-code-only` (a combination of these changes).
An unchanged source is not counted as an eligible edit. Every candidate must
have an accepted result for the PR to qualify.

`DecisionFile` identifies the file that prevented exclusion or could not be checked.
`ComparedFileCount` records how many pairs received a well-formed parser response, and
each file's `Compared` flag distinguishes compared sources from unexamined ones.
Remaining candidates are marked `not-evaluated`, never assumed to be harmless.
A failed parser exit, unexpected output, or missing response is inconclusive,
even if earlier files received positive results. The parser is stopped and
cleaned up on failures and timeouts.

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
`JavaDocReportDecision`, `JavaDocReportReason`, and `JavaDocReportMilliseconds`.
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
POM metadata, mixed per-file change categories, PRs above the former file limit,
first/middle/last rejection,
unread later source blobs, process cleanup, limits, force override, context
guards, and incomplete-result handling.
Parser setup is mandatory; missing dependencies are not reported as skipped or
passing tests.

The existing engineering script CI builds/tests this tool before Pester on its
Windows and Linux legs. No shared `eng/common` files are changed.
