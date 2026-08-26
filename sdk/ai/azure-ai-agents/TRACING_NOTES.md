# GenAI Tracing — Design Notes, Open Questions & Ambiguities

Status: **proof of concept** (branch `jpalvarezl/genai-tracing-poc`). This document captures the design
decisions, open questions, and ambiguities encountered while re-implementing the tracing feature from
PR [#49434](https://github.com/Azure/azure-sdk-for-java/pull/49434) in a way that follows azure-core and
repository conventions.

Behavioral reference: the current **Python Foundry telemetry implementation**,
which was implemented first and is the baseline generally followed by the other
languages. The historical tracing specification is useful context but is a
living document and may be stale.

Java integration reference: **`azure-ai-inference`**
(`ChatCompletionClientTracer`, `ChatCompletionsClientBuilder.createTracer()`,
`ChatCompletionsClient.complete()`), the in-repo example for integrating GenAI
telemetry with `azure-core`. Cross-language behavior comes from Python; Java
plumbing and conventions come from `azure-ai-inference`.

---

## 1. Should there be an `enableGenAiTracing()` / `disableGenAiTracing()` toggle?

**Answer: No — this PoC removed it, and that matches every traced client we checked.**

- The source PR exposed a global static toggle: `GenAiTracingConfiguration.enableGenAiTracing(options)` /
  `disableGenAiTracing()`, plus a programmatic `setExperimental(true)` opt-in.
- No other traced Azure client exposes such a toggle. In `azure-ai-inference`, tracing activates purely from
  whether an OpenTelemetry implementation is configured — globally (`GlobalOpenTelemetry`) or per client via
  `ClientOptions.getTracingOptions()`. There is no enable/disable method, and no "experimental" runtime flag.
- The toggle is therefore **not indicative of any azure-core or OpenTelemetry spec** — it is a custom construct.
  azure-core's tracing model is entirely configuration-driven; the client's `Tracer` is a no-op unless an OTel
  provider is present. A boolean toggle duplicates (and can contradict) that state.
- **Preview status** should be conveyed the repo way: the package version (`-beta.N`) and/or the internal
  `@Beta` annotation (`com.azure.ai.agents.implementation.utils.Beta`) — not a runtime `setExperimental(true)`.
- The only current PoC knob is **content recording**, and it is a standard shared configuration property
  (`AZURE_TRACING_GEN_AI_CONTENT_RECORDING_ENABLED`), not a programmatic on/off switch. Trace-context
  propagation may require a separate privacy-sensitive opt-in if the approved cross-language contract requires
  it; that decision must remain distinct from whether local GenAI spans are enabled.

> Decision in this PoC: tracing activates automatically from the configured OpenTelemetry; there is no
> enable/disable API. Sample usage becomes noticeably simpler (no opt-in/opt-out calls).

---

## 2. `traced*` methods vs. customizing generated convenience methods

**Question raised:** rather than removing `@Generated` and editing the generated convenience methods, would it
be more guideline-compliant to add `traced*` variants of every traceable method (e.g. `tracedCreateAgentVersion`)?

**What the codebase does today:**
- `azure-ai-inference` **customizes the generated convenience method**: `ChatCompletionsClient.complete(...)`
  still carries the `// Generated convenience method for completeWithResponse` comment but the `@Generated`
  annotation has been **removed**, and the body delegates to `tracer.traceSyncComplete(...)`. This is exactly
  the approach used by the source PR and by this PoC (`AgentsClient.createAgentVersion`,
  `ResponsesClient.createAzureResponse`, ...).
- Both `azure-ai-agents/customizations` and `azure-ai-inference/customization` (the TypeSpec codegen
  customization modules) exist, but **neither weaves tracing** — confirmed by search. So tracing is injected by
  hand-editing the generated file, and the injection survives regeneration via the partial-update codegen
  (non-`@Generated` members are preserved).

**Assessment of the `traced*` approach:**
- Pro: it never touches generated code, so there is no regen fragility.
- Con: it makes tracing **opt-in per call** — the caller must choose `tracedCreateAgentVersion` over
  `createAgentVersion` to get a span. This **breaks tracing transparency**, which is the whole point: a user who
  has configured OpenTelemetry expects the normal methods to be traced. No Azure SDK client ships a parallel
  `traced*` surface, so it would also be a consistency/discoverability regression.

**Recommendation:**
- For the **shipped API**, keep tracing transparent on the real methods (current approach). Do **not** add a
  parallel `traced*` surface.
- The legitimate concern behind the question — fragility of hand-editing generated files — is best addressed by
  weaving the tracer through the **codegen customization module** (`customizations/AgentsCustomizations.java`,
  AST-based) so the generated files stay generated and the injection is reapplied deterministically on every
  regeneration. This is **not** currently done anywhere in-repo (inference lives with the manual edit), so it
  would be new ground; it is the recommended production hardening, out of scope for this PoC draft.
- If a `traced*` scaffold is desired purely to keep the PoC off the generated files during exploration, treat it
  as temporary and fold it into the real methods (via the customization module) before shipping.

**Regeneration verification (2026-08-26):** after temporarily renaming
`tsp-location.yaml.hide` to `tsp-location.yaml`, `tsp-client update` completed
successfully with `tsp-client` 0.31.0 and the tracing fields, constructors, and
customized non-`@Generated` methods were preserved. The update also produced
broad unrelated generated drift from the referenced specification; that drift
was discarded to keep this tracing change focused. This validates the current
fallback approach, but ADR 11 still needs to decide whether deterministic AST
customization is required for long-term ownership.

---

## 3. Async trace-context propagation on the openai-java Responses path

- **azure-core path (`AgentsClient` / `AgentsAsyncClient`):** the span `Context` is threaded via
  `RequestOptions.setContext(span)`, so the pipeline's built-in `InstrumentationPolicy` parents the HTTP span —
  works for both sync and async.
- **openai-java path (`ResponsesClient` / `ResponsesAsyncClient`):** the GenAI span is created and enriched, and
  for **sync** we call `tracer.makeSpanCurrent()` (thread-local) around the blocking call so the underlying HTTP
  span is parented.
- **Confirmed async gap:** making the GenAI span current while invoking the operation supplier was insufficient.
  openai-java prepares the request and invokes its HTTP client from a later `CompletableFuture.thenComposeAsync`
  continuation, after the initiating thread-local scope has closed. Live output showed the GenAI and HTTP spans
  as unrelated roots with different trace IDs.
- **Implemented async bridge:** each Responses client owns an `OpenAITracingContextBridge`. Before invoking an
  asynchronous response, stream, or conversation operation, the tracing wrapper registers the GenAI
  `com.azure.core.util.Context` under an opaque one-time token and places that token in openai-java's additional
  request headers. `HttpClientHelper` consumes the token, removes the internal header before constructing the
  Azure request, and passes the original context to `HttpPipeline.send`. Reactor cleanup also discards
  unconsumed tokens on completion, error, or cancellation.
- The bridge is per-client rather than global, supports concurrent requests, and does not send the opaque token,
  baggage, or customer content to the service. The Azure pipeline still creates and injects the normal HTTP
  child span and W3C trace headers.
- **Live verification (2026-08-26):** synchronous and asynchronous quick suites each passed 142/142 checks
  against a live Foundry project. Every response HTTP span shared the GenAI span's trace ID and had the GenAI
  span ID as its direct `parentSpanId`; content-disabled and content-enabled checks also passed. Foundry and
  Application Insights UI inspection remains separate release work.

---

## 4. `gen_ai.system` vs `gen_ai.provider.name`

- The source PR defined both `GEN_AI_SYSTEM` (`az.ai.agents`) and `GEN_AI_PROVIDER_NAME` (`microsoft.foundry`)
  but set only `gen_ai.provider.name` on spans (the `gen_ai.system` constant was effectively unused on spans).
- The GenAI semantic conventions renamed `gen_ai.system` → `gen_ai.provider.name` across versions. This PoC sets
  **both** for compatibility with backends that read either.
- **Open:** confirm the canonical attribute(s) and values for the targeted schema (currently
  `https://opentelemetry.io/schemas/1.29.0`) and the Foundry backend, then drop whichever is redundant.

---

## 5. `az.namespace` resource-provider value

- Set to `Microsoft.CognitiveServices` (via `LibraryTelemetryOptions.setResourceProviderNamespace(...)`),
  matching `azure-ai-inference`.
- **Open:** confirm the correct resource-provider namespace for Foundry Agents (it may differ from Cognitive
  Services).

---

## 6. Metrics are implemented but not unit-tested

- `gen_ai.client.operation.duration` and `gen_ai.client.token.usage` histograms are created per client from
  `ClientOptions.getMetricsOptions()` and recorded on span close.
- The unit tests assert spans (via an in-memory OpenTelemetry `SdkTracerProvider`) but **do not** assert metrics
  (that needs an OTel `MeterProvider` + in-memory metric reader wired through `MetricsOptions`).
- **Open:** add metric assertions.

---

## 7. Response-path unit-test coverage

- `GenAiAgentTracing` (agent creation) and `GenAiMessageFormatter` are unit-tested against an in-memory OTel SDK.
- `GenAiResponseTracing` now has direct lifecycle tests for synchronous and asynchronous conversation creation,
  error propagation, cancellation, returned conversation identity, and async span activation.
- Chat / invoke-agent response payloads and streaming accumulation are **not** directly unit-tested because
  constructing complete openai-java `Response` / `ResponseStreamEvent` objects remains impractical.
- **Open:** cover the response path via recorded/fixture payloads or a thin seam that lets tests inject a
  synthetic response.

---

## 8. Message/tool-call JSON is built with string concatenation

- The formatters were ported from the PR as-is (manual JSON string building) to preserve exact output shape.
- `azure-ai-inference` instead uses `azure-json` `JsonWriter` (safer escaping, less error-prone).
- **Open:** migrate the formatters to `JsonWriter` if byte-for-byte parity with the PR output is not required.

---

## 9. Content-recording environment variable name

- This PoC uses `AZURE_TRACING_GEN_AI_CONTENT_RECORDING_ENABLED` (the `azure-ai-inference` standard; system
  property `azure.tracing.gen_ai.content_recording_enabled`, default `false`).
- The source PR used `OTEL_INSTRUMENTATION_GENAI_CAPTURE_MESSAGE_CONTENT`.
- **Open:** confirm the intended standard for Foundry Agents and align the docs.

---

## 10. Which operations should be traced?

- Traced in this PoC: all three current `createAgentVersion` convenience overloads (sync + async),
  `createAzureResponse`, `createStreamingAzureResponse` (sync + async), and `createConversation`
  (sync + async).
- Not traced: `getAgent` / `listAgents` / `deleteAgent`, sessions, memory stores, toolboxes, and the other
  `createAgentVersion*` methods with distinct names. Raw-response create and raw-response streaming methods are
  also not traced pending ADR 8 and ADR 9.
- **Open:** decide the intended operation coverage and whether CRUD reads should emit spans at all.

---

## 11. Model-shape drift (hosted agents)

- The source PR (built against an older model) read `HostedAgentDefinition.getContainerProtocolVersions()` and
  `getImage()`. On `main` these are `getProtocolVersions()` (→ `ProtocolVersionRecord`) and
  `getContainerConfiguration().getImage()`. This PoC uses the current getters.
- **Open:** a codegen-customization-based weaving (see §2) would make this less brittle, since the attribute
  extraction would live next to the generated model rather than in hand-maintained tracing code.

---

## 12. Outcomes from the August 25 Java tracing discussion

The design discussion with the original implementation author clarified the
following:

- **Python is the behavioral baseline.** Follow the current Python telemetry
  implementation before older specifications or prototypes, but filter out
  Python-only legacy or deprecated functionality that is not in the approved
  current Agents contract. Other languages remain useful for comparison, not as
  equal sources of truth.
- **Use Azure SDK abstractions first.** Prefer `azure-core` `Tracer`, `Meter`,
  configuration, and pipeline facilities. Direct OpenTelemetry APIs are
  acceptable only for a concrete capability gap, which must be documented and
  included in the architecture review.
- **Content-disabled means no customer-controlled content.** Prompts,
  responses, message text, instructions, function arguments, tool results, and
  equivalent fields must not be collected or emitted without explicit content
  opt-in. Contract-approved identifiers and generic structural metadata may
  remain. This privacy boundary is a release requirement, not optional
  hardening.
- **Live validation is required.** Connect a Foundry project to Application
  Insights and use the existing end-to-end tracing application, or an
  equivalent maintained sample, to verify that client spans appear in Foundry
  and correlate correctly. Keep test agents alive until traces are inspected
  because the current Foundry UI groups traces by agent and deletion can make
  them inaccessible.
- **Architecture review is required before merge.** The review should focus on
  any direct OpenTelemetry usage and on the content-recording privacy boundary.
- **Streaming scope remains open.** The meeting did not resolve whether all
  prompt-agent streaming behavior is ready or which pieces belong in the first
  tracing release.
- **Service-side tracing reduces urgency, not correctness requirements.**
  Client-side tracing is additive because service-side traces already exist,
  but shipped client telemetry must still meet the approved contract and
  privacy requirements.

---

## 13. Python-to-Java contract audit

Audit performed against the current Python implementation under
`sdk/ai/azure-ai-projects/azure/ai/projects/telemetry`. This table records
observed differences; it does not resolve the corresponding ADRs.

| Area | Python behavior | Current Java behavior | Disposition |
| --- | --- | --- | --- |
| Semantic-convention schema | Declares `1.34.0` | Declares `1.29.0` | Resolve through ADR 1 before changing. |
| Provider identity | Emits `gen_ai.provider.name=microsoft.foundry`; conditionally retains `gen_ai.system` | Emits provider and legacy system attributes | Resolve through ADR 3. |
| Experimental gate | Requires `AZURE_EXPERIMENTAL_ENABLE_GENAI_TRACING=true` | Activation follows per-client Azure Core tracing configuration | Resolve through ADR 4; do not restore global mutable APIs. |
| Content gate | `OTEL_INSTRUMENTATION_GENAI_CAPTURE_MESSAGE_CONTENT` | `AZURE_TRACING_GEN_AI_CONTENT_RECORDING_ENABLED` | Resolve through ADR 6 and Architecture Board review. |
| Service propagation | Separate trace-context gate, enabled by default; baggage separately disabled by default | Sync uses the current span; async uses a per-client, request-scoped Azure `Context` bridge. The pipeline injects W3C trace context; baggage is not propagated by this bridge. | HTTP parenting verified locally against a live service; resolve the configurable propagation/privacy contract through ADR 5. |
| Agent creation | Instruments agent version creation | All current Java `createAgentVersion` convenience overloads are instrumented | Implemented; verify regeneration and E2E parity. |
| Responses | Typed sync/async, streaming, and raw-response streaming wrappers | Typed sync/async and streaming are instrumented; raw-response methods are not | ADR 8 and ADR 9 blocker. |
| Conversations | Create and conversation-item listing | Create only | ADR 8 blocker; do not add public APIs solely for tracing. |
| Streaming lifecycle | Explicit cleanup for regular and raw streams | Typed sync supports exhaustion/error/explicit close; async supports completion/error/cancellation | Add raw-stream coverage only after ADR 9. |
| Duration timing | Wall-clock timing | Monotonic `System.nanoTime()` | Intentional Java reliability improvement required by the roadmap. |
| Workflow instrumentation | Present in Python | Excluded from Java instrumentation and the E2E scope | Intentional: workflow agents are retiring and are not a Java tracing requirement. |

Confirmed Java correctness fixes from this audit:

- Conversation spans now surround the network operation and record errors,
  cancellation, duration, and the returned conversation ID.
- Async OpenAI operation suppliers start while the GenAI span is current.
- Async OpenAI response, streaming, and conversation requests carry their
  GenAI parent through a per-client bridge to the Azure HTTP pipeline.
- Duration metrics use monotonic elapsed time.
- Every current sync and async `createAgentVersion` convenience overload uses
  the same tracing wrapper.
- Workflow-specific agent attributes and events are not emitted; workflow
  agents are outside the supported Java tracing scope.

---

## Summary of decisions already taken in this PoC

| Area | Decision |
| --- | --- |
| Enable/disable toggle | Removed — tracing activates from configured OpenTelemetry |
| Configuration | Per-client `Tracer` + `Meter` from `ClientOptions` (`TracingOptions` / `MetricsOptions`) |
| Placement | `com.azure.ai.agents.implementation.telemetry` (non-API) |
| Behavioral reference | Current Python Foundry telemetry implementation |
| Java abstractions | Prefer `azure-core`; use direct OpenTelemetry only for documented gaps |
| Weaving | Customize the generated convenience methods (matches `azure-ai-inference`) |
| Content gating | `AZURE_TRACING_GEN_AI_CONTENT_RECORDING_ENABLED`, off by default; no customer-controlled content when disabled |
| Live validation | Foundry project connected to Application Insights, using the end-to-end tracing scenario |
| Review gate | Azure SDK architecture review before merge |
| Bugs fixed | `end(errorType, throwable)`; monotonic duration; conversation request lifecycle; async request initiation context; all `createAgentVersion` overloads; `formatToolCallOutput` content; histogram start gate; library version from `azure-ai-agents.properties` |
