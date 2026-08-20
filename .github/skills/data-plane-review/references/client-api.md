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
java-client-construction, and java-service-client-builder-validity. Historical
support: REVIEW-RULE-CATALOG API-001, including
https://github.com/Azure/azure-sdk-for-java/pull/29580#discussion_r925248169. -->

Service-client constructors must not be public or protected. Use
`<Service>ClientBuilder`, `@ServiceClientBuilder`, `buildClient()`, and
`buildAsyncClient()`. The builder must reject incomplete or mutually exclusive
configuration with `IllegalStateException`.

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
java-params-complex-naming, and java-params-complex. Historical support:
REVIEW-RULE-CATALOG NAME-001, including
https://github.com/Azure/azure-sdk-for-java/pull/23666#discussion_r691930389. -->

Use standard CRUD verbs, no `Async` method suffix, `Context` only on maximal
sync overloads, `WithResponse` for complete responses, and
`<Operation>Options` for complex inputs. Group related naming inconsistencies
into one finding.

**Correct form:** `createWidget(CreateWidgetOptions options)` and
`createWidgetWithResponse(CreateWidgetOptions options, Context context)`.

