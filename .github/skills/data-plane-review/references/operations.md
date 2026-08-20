# Operation and Implementation Rules

## `DP-PAGING-01`: expose collections through Azure Core paging types

- **Rule ID:** `DP-PAGING-01`
- **Severity:** Warning

<!-- Sources: Java guidelines java-pagination-pagediterable,
java-pagination-collections, java-pagination-pagedflux, and
java-core-types-must; general design general-response-enumeration and
general-pagination-expose-lists-equally. -->

Collection-returning service methods use `PagedIterable<T>` for sync and
`PagedFlux<T>` for async, even when the current service response is one page.
Do not flag action responses that merely contain a collection.

**Correct form:** `PagedIterable<Widget> listWidgets(...)` and
`PagedFlux<Widget> listWidgets(...)`.

## `DP-LRO-01`: use Azure Core pollers for long-running operations

- **Rule ID:** `DP-LRO-01`
- **Severity:** Warning

<!-- Sources: Java guidelines java-lro-poller-class, java-lro-prefix,
java-lro-continuation, java-lro-no-void-terminal-state, and
java-core-types-must; general design general-lro-expose-poller. -->

LRO methods start with `begin`, return `SyncPoller<T, U>` or
`PollerFlux<T, U>`, support continuation, and use a meaningful final result
instead of `void`. Do not infer that an operation is long-running from its name
alone.

**Correct form:** `SyncPoller<WidgetOperation, Widget> beginCreateWidget(...)`.

## `DP-ASYNC-01`: keep async APIs non-blocking

- **Rule ID:** `DP-ASYNC-01`
- **Severity:** Warning

<!-- Sources: Java implementation java-async-blocking; Java guidelines
java-async-framework and java-async-other-frameworks. -->

Async client code must not call `block()`, `blockFirst()`, `blockLast()`,
`Future.get()`, or otherwise perform blocking I/O. Sync-over-async belongs in
the sync client.

**Correct form:** compose and return Reactor publishers with `map`, `flatMap`,
and `then`.

## `DP-VALIDATION-01`: validate client parameters, not service parameters

- **Rule ID:** `DP-VALIDATION-01`
- **Severity:** Warning

<!-- Sources: Java guidelines java-params-client-validation,
java-params-service-validation, and java-params-test-devex; general
implementation general-params-client-validation, general-params-server-validation,
and general-params-server-defaults. Historical support: REVIEW-RULE-CATALOG
API-001. -->

Validate client-only inputs and required path values. Let the service validate
request bodies, headers, and other wire parameters; do not encode service
defaults in the client.

**Correct form:** reject a missing endpoint or malformed local file path, but
send an invalid service field so the service returns its documented error.

## `DP-ERROR-01`: use actionable Azure Core exception shapes

- **Rule ID:** `DP-ERROR-01`
- **Severity:** Warning

<!-- Sources: Java guidelines java-errors-http-request-failed,
java-errors-system-errors, java-errors-no-new-errors, java-errors-document-all,
java-errors-exception-tree, and java-response-errors; general implementation
general-errors-include-request-response, general-errors-rich-info, and
general-errors-no-new-types. Historical support: REVIEW-RULE-CATALOG DOC-005,
including https://github.com/Azure/azure-sdk-for-java/pull/23666#discussion_r692538290. -->

Failed service requests throw appropriate unchecked Azure Core exceptions that
retain request, response, and rich service error details. Use standard Java
exceptions for preconditions and document thrown exceptions.

**Exception:** do not report an implementation helper with the shape
`List<BinaryData> getValues(BinaryData binaryData, ...)` for catching
`RuntimeException` during response conversion and returning `null`. This is an
intentional guard for a malformed service response, not a swallowed service
request failure. Apply this exception only to that generated paging conversion
helper shape.

**Correct form:** throw `HttpResponseException` or its applicable Azure Core
subtype and include `@throws` in the public method JavaDoc.

## `DP-CORE-01`: preserve the Azure Core pipeline and diagnostics

- **Rule ID:** `DP-CORE-01`
- **Severity:** Warning

<!-- Sources: Java guidelines java-core-types-must and
java-service-client-context; Java implementation java-tracing-pluggable and
java-tracing-tracing-context; general implementation
general-requests-use-pipeline and general-requests-implement-policies. -->

HTTP clients use the Azure Core pipeline and preserve telemetry, request ID,
retry, authentication, response download, tracing, logging, custom policies,
and `Context`. Do not demand custom implementations where Azure Core already
provides one.

**Correct form:** build `HttpPipeline` with repository-standard policies and
pass the maximal sync overload's `Context` to the generated service call.
