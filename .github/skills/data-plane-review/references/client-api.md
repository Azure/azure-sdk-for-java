# Client and Public API Rules

## `DP-CLIENT-01`: expose idiomatic sync and async clients

- **Rule ID:** `DP-CLIENT-01`
- **Severity:** Warning

<!-- Sources: Java guidelines java-service-client-name, java-network-separate-packages,
java-sync-client-name, java-async-client-name, and java-async-framework; general
design general-network-support-sync-and-async. -->

Require `<Service>Client` and `<Service>AsyncClient` for network APIs, with
`@ServiceClient` and Reactor types on the async surface. Do not flag a
non-service helper or a protocol that has an approved exception.

**Correct form:** `WidgetClient` and `WidgetAsyncClient`; async methods return
`Mono<T>`, `Flux<T>`, `PagedFlux<T>`, or `PollerFlux<T, U>`.

## `DP-CLIENT-02`: construct clients through one valid fluent builder

- **Rule ID:** `DP-CLIENT-02`
- **Severity:** Warning

<!-- Sources: Java guidelines java-service-client-constructors,
java-service-client-fluent-builder, java-service-client-builder-annotation,
java-service-client-builder-consistency, java-service-client-builder-state,
java-client-construction, and java-service-client-builder-validity. -->

Service-client constructors must not be public or protected. Use
`<Service>ClientBuilder`, `@ServiceClientBuilder`, `buildClient()`, and
`buildAsyncClient()`. The builder must reject incomplete or mutually exclusive
configuration with `IllegalStateException`.

Do not treat a caller-supplied `HttpPipeline` taking precedence over
individually configured HTTP clients, credentials, retry options, logging
options, or policies as an invalid builder state. This is the standard
generated builder precedence and is not evidence that configuration is
silently ignored.

**Correct form:** `new WidgetClientBuilder().endpoint(endpoint)
.credential(credential).buildClient()`.

## `DP-API-01`: keep implementation types out of public API

- **Rule ID:** `DP-API-01`
- **Severity:** Warning

<!-- Sources: Java implementation java-namespaces-implementation; Java
guidelines java-models-interface and java-module-exports; general design
general-network-no-leakage. -->

Public signatures and exported modules must not expose `implementation`
packages, generated protocol types, transport internals, or package-private
exceptions.

**Correct form:** return a public `models.Widget` interface or model while the
implementation remains package-private or under `.implementation`.

## `DP-METHOD-01`: use Java service-method conventions

- **Rule ID:** `DP-METHOD-01`
- **Severity:** Suggestion

<!-- Sources: Java guidelines java-async-suffix, java-service-client-verbs,
java-service-client-context, java-service-client-context-async,
java-response-logical-entity, java-response-async-logical-entity,
java-params-complex-naming, and java-params-complex. -->

Use standard CRUD verbs, no `Async` method suffix, `Context`/`RequestOptions`
only on maximal sync overloads, and `WithResponse` for complete responses.
Group related naming inconsistencies into one finding.

**Correct form:** `createWidget(...)` and
`createWidgetWithResponse(..., Context context)`.

## `DP-METHOD-02`: use an options bag for more than six parameters

- **Rule ID:** `DP-METHOD-02`
- **Severity:** Warning

<!-- Sources: Java guidelines "Service Method Parameters", which defines simple
methods as having up to six parameters, plus java-params-complex-naming,
java-params-complex, java-params-complex-overloads,
java-params-complex-withResponse, java-params-options-package, and
java-params-options-design. -->

A newly added user-facing service method with more than six service parameters
must replace the service parameters with an `<Operation>Options` bag. Do not
count `Context`, `RequestOptions`, timeout, or other client-only parameters
toward the threshold. Do not report generated low-level protocol methods.
Methods with six or fewer parameters may still use an options bag when growth
is expected.

The options type requires mandatory values through its constructor and exposes
optional values through fluent setters. The corresponding `WithResponse`
method uses the same options type.

**Correct form:** `createWidget(CreateWidgetOptions options)` and
`createWidgetWithResponse(CreateWidgetOptions options, Context context)`.
