# GenAI Tracing — Design Notes, Open Questions & Ambiguities

Status: **proof of concept** (branch `jpalvarezl/genai-tracing-poc`). This document captures the design
decisions, open questions, and ambiguities encountered while re-implementing the tracing feature from
PR [#49434](https://github.com/Azure/azure-sdk-for-java/pull/49434) in a way that follows azure-core and
repository conventions.

Reference implementation consulted throughout: **`azure-ai-inference`** (`ChatCompletionClientTracer`,
`ChatCompletionsClientBuilder.createTracer()`, `ChatCompletionsClient.complete()`), the in-repo canonical
GenAI-traced client.

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
- The only remaining knob is **content recording**, and it is a standard shared configuration property
  (`AZURE_TRACING_GEN_AI_CONTENT_RECORDING_ENABLED`), not a programmatic on/off switch.

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

---

## 3. Async trace-context propagation on the openai-java Responses path

- **azure-core path (`AgentsClient` / `AgentsAsyncClient`):** the span `Context` is threaded via
  `RequestOptions.setContext(span)`, so the pipeline's built-in `InstrumentationPolicy` parents the HTTP span —
  works for both sync and async.
- **openai-java path (`ResponsesClient` / `ResponsesAsyncClient`):** the GenAI span is created and enriched, and
  for **sync** we call `tracer.makeSpanCurrent()` (thread-local) around the blocking call so the underlying HTTP
  span is parented. For **async** (reactive), thread-local context does not span the Reactor chain, so the GenAI
  span may not become the parent of the underlying HTTP span. The GenAI span and its attributes are still emitted
  correctly.
- **Open:** wire Reactor context propagation (Micrometer context-propagation, which azure-core supports) so the
  async openai-java HTTP span is parented under the GenAI span.

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

## 7. Response-path unit-test coverage gap

- `GenAiAgentTracing` (agent creation) and `GenAiMessageFormatter` are unit-tested against an in-memory OTel SDK.
- `GenAiResponseTracing` (chat / invoke_agent, streaming) is **not** directly unit-tested because constructing
  openai-java `Response` / `ResponseStreamEvent` objects in tests is impractical.
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

- Traced in this PoC: `createAgentVersion` (sync + async), `createAzureResponse` and
  `createStreamingAzureResponse` (sync; async variants added on `GenAiResponseTracing`), and
  `createConversation`.
- Not traced: `getAgent` / `listAgents` / `deleteAgent`, sessions, memory stores, toolboxes, and the other
  `createAgentVersion*` overloads.
- **Open:** decide the intended operation coverage and whether CRUD reads should emit spans at all.

---

## 11. Model-shape drift (hosted agents)

- The source PR (built against an older model) read `HostedAgentDefinition.getContainerProtocolVersions()` and
  `getImage()`. On `main` these are `getProtocolVersions()` (→ `ProtocolVersionRecord`) and
  `getContainerConfiguration().getImage()`. This PoC uses the current getters.
- **Open:** a codegen-customization-based weaving (see §2) would make this less brittle, since the attribute
  extraction would live next to the generated model rather than in hand-maintained tracing code.

---

## Summary of decisions already taken in this PoC

| Area | Decision |
| --- | --- |
| Enable/disable toggle | Removed — tracing activates from configured OpenTelemetry |
| Configuration | Per-client `Tracer` + `Meter` from `ClientOptions` (`TracingOptions` / `MetricsOptions`) |
| Placement | `com.azure.ai.agents.implementation.telemetry` (non-API) |
| Weaving | Customize the generated convenience methods (matches `azure-ai-inference`) |
| Content gating | `AZURE_TRACING_GEN_AI_CONTENT_RECORDING_ENABLED`, off by default |
| Bugs fixed | `end(errorType, throwable)`; wall-clock duration; `formatToolCallOutput` content; histogram start gate; library version from `azure-ai-agents.properties` |
