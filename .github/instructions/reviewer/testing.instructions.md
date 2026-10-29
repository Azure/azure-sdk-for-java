---
applyTo: "sdk/**/src/test/**,sdk/**/src/main/java/**/*.java"
description: "Review SDK test changes and the coverage implications of changed Java behavior."
---

# Java SDK test review

Inspect existing tests before saying a changed behavior is untested. Report
only a consequential gap or regression, such as a removed assertion that
guarded the changed behavior, an untested failure path with a plausible
customer impact, or a test that can pass while the new behavior is broken.

- Check applicable success, failure, and boundary cases for new behavior.
  For service clients that test multiple HTTP transports or service versions,
  preserve relevant parameterized coverage; do not require every matrix
  combination in every PR.
- Check tests that use live Azure services for correct playback/record/live
  mode handling. Default playback tests should not unexpectedly require
  credentials, network access, or live resources. Do not suggest re-recording
  tests as a fix for a product or test failure.
- Flag leaked secrets in fixtures or recordings, shared mutable test state
  that causes parallel-test races, or cleanup that deletes resources the test
  did not create when the changed code makes the risk concrete.
- Test behavior, not implementation trivia; do not request tests for
  generated boilerplate or documentation-only changes without an actual
  regression risk.

Sources: [Java implementation testing](https://azure.github.io/azure-sdk/java_implementation.html#java-testing-params),
[repository unit testing](../../../docs/contributor/unit-testing.md),
and [record/playback modes](../../../docs/contributor/live-testing.md#test-recording--playback).
