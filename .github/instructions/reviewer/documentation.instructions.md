---
applyTo: "sdk/**/README.md,sdk/**/CHANGELOG.md,sdk/**/TROUBLESHOOTING.md,sdk/**/src/samples/**/*.java,sdk/**/src/main/java/**/*.java,sdk/**/customizations/**/*.java"
description: "Review Azure Java SDK JavaDoc, README, CHANGELOG, samples, and generated snippet references for user-visible inaccuracies."
---

# Java SDK documentation and sample review

Report user-visible documentation defects, not wording preferences. For a
public API change, check whether the affected README, JavaDoc, sample, and
release entry still show the correct signature and behavior. A missing
example matters when it blocks use of a new key scenario; do not demand a
sample for every added method.

- Check newly changed public JavaDoc for misleading parameter, return, and
  exception contracts, especially failures that users need to handle.
  Class-level examples should show viable client construction and usage.
- README and JavaDoc snippets should correspond to compilable sample sources
  under `src/samples/java` (or the module's configured snippet source) and
  use the repository's snippet markers, such as `java readme-sample-*` in
  README. Flag a broken reference or stale code example with a concrete
  compile or runtime consequence; do not ask to hard-code JavaDoc examples.
- Check that customer-visible behavior and breaking changes are accurately
  represented in the package's current CHANGELOG entry when applicable.
  Verify the previous GA surface before asserting a breaking change;
  routine internal changes do not need customer-facing release notes.
- Ensure new samples use the module's actual Java/API baseline and do not
  embed real credentials or values that would disclose secrets. Do not flag
  intentionally fake placeholder names as secrets.

Sources: [JavaDoc and sample guidance](https://azure.github.io/azure-sdk/java_introduction.html#java-javadoc-full-docs),
[general documentation](https://azure.github.io/azure-sdk/general_documentation.html#code-snippets),
and [repository snippet workflow](../../../docs/contributor/javadocs.md#3-code-snippet-injection-codesnippet-maven-plugin).
