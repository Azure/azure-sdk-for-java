# Documentation and Testing Rules

## `DP-DOC-01`: provide a complete package-specific README

- **Rule ID:** `DP-DOC-01`
- **Severity:** Suggestion

<!-- Sources: General documentation deliverables and
general-docs-to-silence; repository docs/STRUCTURE.md library documentation. -->

The package README must accurately cover installation, authentication or
client creation, key concepts, examples, troubleshooting, and current links.
Do not flag wording preference alone.

**Correct form:** follow the
[Azure SDK README template](https://github.com/Azure/azure-sdk/blob/main/docs/policies/README-TEMPLATE.md)
to describe this package and its champion scenarios rather than retaining
generator placeholders or text copied from another service. See the Java
repository's
[documentation guidance](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/typespec-quickstart.md#5-improve-documentation)
and
[code-snippet guide](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/building.md#code-snippets-in-readme-files)
for package-specific details.

## `DP-DOC-02`: keep examples atomic, runnable, and source-backed

- **Rule ID:** `DP-DOC-02`
- **Severity:** Suggestion

<!-- Sources: General documentation general-docs-include-snippets,
general-docs-build-snippets, general-docs-snippets-in-docstrings, and
general-docs-operation-combinations; repository docs/contributor/building.md
"Code Snippets in README Files". -->

Examples demonstrate one customer task, include required setup, compile in CI,
and are injected from maintained Java sample sources.

Files under `src/samples/**/generated/` are emitter-generated examples and do
not count as maintained customer samples.

**Correct form:** follow the
[code-snippet guide](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/building.md#code-snippets-in-readme-files):
place the sample under `src/samples/java`, use
`readme-sample-<descriptiveName>`, and rebuild the package to inject it.

## `DP-DOC-03`: document public behavior and failures in JavaDoc

- **Rule ID:** `DP-DOC-03`
- **Severity:** Suggestion

<!-- Sources: General documentation general-docs-to-silence; Java guidelines
java-errors-document-all; repository docs/contributor/javadocs.md. -->

Public JavaDoc explains behavior, parameters, return values, and non-obvious
failure contracts. Group repeated omissions. Do not report generated
boilerplate merely for style.

**Correct form:** follow the repository's
[JavaDoc guide](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/javadocs.md#2-what-to-document)
to include meaningful `@param`, `@return`, and applicable `@throws` entries,
with source-backed snippets for key operations.

## `DP-TEST-01`: cover supported transports and service versions

- **Rule ID:** `DP-TEST-01`
- **Severity:** Suggestion

<!-- Sources: Java implementation java-testing-params; repository
CONTRIBUTING.md "Building and Unit Testing"; repository
eng/automation/generate_utils.py remove_generated_source_code. -->

New behavior should be exercised across applicable HTTP clients and service
versions. Do not demand live coverage for behavior that repository playback or
unit tests can verify.

Before reporting missing coverage, inspect the package's complete `src/test`
tree at the pinned PR head, including unchanged tests. Do not infer missing
coverage merely because the PR does not add or modify a test file. Report only
when the changed behavior lacks applicable existing coverage.

Exclude files under `src/test/**/generated/`; they are emitter-generated
examples and do not count as tests.

For a `new-module`, emit `DP-TEST-01` when no test files remain after excluding
`src/test/**/generated/`. This trigger is mandatory and does not depend on
whether test files appear in the PR diff.

**Correct form:** follow the repository's
[live testing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/live-testing.md)
to add applicable tests outside `generated`, parameterize them for supported
HTTP clients and service versions, and use the normal PR subset plus the full
transport/version matrix. See also the
[Test Proxy onboarding guide](https://github.com/Azure/azure-sdk-for-java/blob/main/eng/common/testproxy/onboarding/README.md)
and
[TypeSpec test-mode commands](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/typespec-quickstart.md#6-tests).
