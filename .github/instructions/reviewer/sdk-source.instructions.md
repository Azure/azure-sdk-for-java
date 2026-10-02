---
applyTo: "sdk/**/src/main/java/**/*.java,sdk/**/customizations/**/*.java"
description: "Review Java SDK source and public API changes for material design, compatibility, and implementation regressions."
---

# Java SDK source review

Review the changed behavior, not every difference from a design example.
Apply client-library API guidance only to relevant SDK APIs; consult the
package's existing patterns. For Spring packages, the
[Spring instructions](spring.instructions.md) override conflicting rules.

## Public API and compatibility

- Establish the last GA API before calling an API change breaking; preview
  packages can evolve differently. Flag an actual loss of source or binary
  compatibility for a released public API, not an internal rename or normal
  generated churn. Do not request a breaking rename solely to match a naming
  guideline.
- For new service clients, check that the public entry point is an immutable
  `<Service>Client` or `<Service>AsyncClient`, constructed via a fluent
  `ClientBuilder` with valid build-time configuration. Preserve the package's
  sync/async coverage where applicable. For complex operations, prefer
  `<Operation>Options` to a growing list of overloads.
- For service methods returning item collections, use `PagedIterable<T>` for
  sync or `PagedFlux<T>` for async rather than exposing a page's raw `List<T>`
  as the whole result. For long-running operations, use `begin*` with
  azure-core `SyncPoller`/`PollerFlux` rather than hand-written polling.
  Confirm the operation really is a list or LRO before flagging a shape.
- Keep client-side validation (for example local paths) while leaving
  service-bound parameter validation to the service. Preserve actionable
  failures and the appropriate azure-core exception behavior.

## Implementation

- Never block inside an async client path (`block()`, synchronous I/O, or
  equivalent); sync-over-async inside a **sync** client is permitted. Forward
  subscriber context in async flows and `Context` in sync service methods.
- Preserve the azure-core HTTP pipeline's retry, authentication, logging, and
  tracing policies. Flag lost tracing context, unsafe credential caching,
  or newly exposed secrets in logs, URLs, headers, or errors. In non-Spring
  Azure Core-based client libraries, use `ClientLogger`; redact headers and
  query values unless they are explicitly allow-listed.
- Do not expose implementation-only types as new public API. Check the
  affected module's configured Java baseline before flagging language or JDK
  APIs: the usual Java 8 baseline does not apply to every module.
- Generated code is reviewable for material defects, but trace the fix to
  TypeSpec, generation settings, or customizations. For eligible management
  AutoPRs, the specialist skill's `generated` exclusion takes precedence.

Sources: [Java service clients and builders](https://azure.github.io/azure-sdk/java_introduction.html#service-client),
[paging](https://azure.github.io/azure-sdk/java_introduction.html#java-pagination-pagediterable),
[LROs](https://azure.github.io/azure-sdk/java_introduction.html#java-lro-poller-class),
[compatibility](https://azure.github.io/azure-sdk/java_introduction.html#java-versioning-backwards-compatibility),
[async implementation](https://azure.github.io/azure-sdk/java_implementation.html#java-async-blocking),
[logging](https://azure.github.io/azure-sdk/java_implementation.html#java-logging-no-sensitive-info),
and [tracing](https://azure.github.io/azure-sdk/java_implementation.html#java-tracing-tracing-context).
