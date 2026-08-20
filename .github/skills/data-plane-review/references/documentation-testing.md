# Documentation and Testing Rules

## `DP-DOC-01`: provide a complete package-specific README

- **Rule ID:** `DP-DOC-01`
- **Severity:** Suggestion

<!-- Sources: General documentation deliverables and
general-docs-to-silence; repository docs/STRUCTURE.md library documentation.
Historical support: REVIEW-RULE-CATALOG DOC-001 and DOC-002, including
https://github.com/Azure/azure-sdk-for-java/pull/23666#discussion_r691815034. -->

The package README must accurately cover installation, authentication or
client creation, key concepts, examples, troubleshooting, and current links.
Do not flag wording preference alone.

**Correct form:** describe this package and its champion scenarios rather than
retaining generator placeholders or text copied from another service.

## `DP-DOC-02`: keep examples atomic, runnable, and source-backed

- **Rule ID:** `DP-DOC-02`
- **Severity:** Suggestion

<!-- Sources: General documentation general-docs-include-snippets,
general-docs-build-snippets, general-docs-snippets-in-docstrings, and
general-docs-operation-combinations; repository docs/contributor/building.md
"Code Snippets in README Files". Historical support: REVIEW-RULE-CATALOG
DOC-003 and DOC-004, including
https://github.com/Azure/azure-sdk-for-java/pull/33719#discussion_r1123609134. -->

Examples demonstrate one customer task, include required setup, compile in CI,
and are injected from maintained Java sample sources.

**Correct form:** place the sample under `src/samples/java`, use
`readme-sample-<descriptiveName>`, and rebuild the package to inject it.

## `DP-DOC-03`: document public behavior and failures in JavaDoc

- **Rule ID:** `DP-DOC-03`
- **Severity:** Suggestion

<!-- Sources: General documentation general-docs-to-silence; Java guidelines
java-errors-document-all; repository docs/contributor/javadocs.md. Historical
support: REVIEW-RULE-CATALOG DOC-005. -->

Public JavaDoc explains behavior, parameters, return values, and non-obvious
failure contracts. Group repeated omissions. Do not report generated
boilerplate merely for style.

**Correct form:** include meaningful `@param`, `@return`, and applicable
`@throws` entries, with source-backed snippets for key operations.

## `DP-TEST-01`: cover supported transports and service versions

- **Rule ID:** `DP-TEST-01`
- **Severity:** Suggestion

<!-- Sources: Java implementation java-testing-params; repository
CONTRIBUTING.md "Building and Unit Testing". Historical support:
REVIEW-RULE-CATALOG TEST-001. -->

New behavior should be exercised across applicable HTTP clients and service
versions. Do not demand live coverage for behavior that repository playback or
unit tests can verify.

**Correct form:** parameterize applicable tests; use the repository's normal
PR subset and the full transport/version matrix for live validation.
