# GenAI Telemetry Roadmap

Status: **Planning**

Last updated: **2026-08-25**

This document defines the work required to complete GenAI telemetry for
`azure-ai-agents` and its use from `azure-ai-projects`.

The intended implementation branch is
[PR #49706](https://github.com/Azure/azure-sdk-for-java/pull/49706). The original
[PR #49434](https://github.com/Azure/azure-sdk-for-java/pull/49434) remains a
requirements and prototype reference, but its implementation should not be merged
directly.

See [TRACING_NOTES.md](TRACING_NOTES.md) for detailed design investigations and
open technical questions discovered while building the proof of concept.

## Goals

- Emit Foundry GenAI spans and metrics that conform to an explicitly versioned
  cross-language telemetry contract.
- Follow Azure SDK for Java conventions and reuse `azure-core` tracing, metrics,
  configuration, and HTTP pipeline infrastructure.
- Keep telemetry configuration and state isolated per client.
- Cover synchronous, asynchronous, regular, raw-response, and streaming entry
  points consistently.
- Preserve trace context between GenAI operation spans and HTTP spans.
- Keep prompt, response, tool, workflow, and agent content disabled by default.
- Make generated-client instrumentation deterministic and resilient to SDK
  regeneration.
- Provide tests that assert emitted telemetry rather than only verifying that
  instrumented operations execute.

## Non-goals

- Adding a parallel `traced*` public API.
- Replacing `azure-core` tracing or metrics providers.
- Adding process-global mutable tracing state.
- Tracing unrelated Projects operations unless they are added to the approved
  Foundry telemetry contract.
- Adding new public conversation APIs solely to make telemetry easier to
  implement.

## Architecture Direction

The following proof-of-concept decisions should be retained:

| Area | Direction |
| --- | --- |
| Configuration | Build a per-client `Tracer` and `Meter` from `ClientOptions`. |
| State | Inject immutable telemetry dependencies into each client. |
| Placement | Keep helpers under `com.azure.ai.agents.implementation.telemetry`. |
| Activation | Use configured tracing and metrics providers; do not add a public global toggle API. |
| HTTP tracing | Use `azure-core` pipeline instrumentation and propagate the GenAI span context. |
| Generated methods | Instrument the existing convenience methods transparently. |
| Content | Disable content recording by default and require explicit opt-in. |
| Library metadata | Read client name and version from `azure-ai-agents.properties`. |

If the cross-language contract requires
`AZURE_EXPERIMENTAL_ENABLE_GENAI_TRACING`, implement the gate internally without
restoring the public `GenAiTracingConfiguration` and `GenAiTracingOptions` API
from PR #49434. Confirm that the environment variable has the required Azure SDK
Architecture Board approval.

## Phase 0: Freeze the Telemetry Contract

**Owners:** Foundry telemetry owners, Java SDK owner, cross-language SDK
representatives.

Before further implementation, approve and record a contract matrix containing:

- Semantic-convention version or exact source commit.
- OpenTelemetry schema URL.
- Span names, kinds, and operation names.
- Required, conditional, recommended, and opt-in attributes.
- Attribute value types.
- Foundry-specific attributes and events.
- Provider name and resource-provider namespace.
- Metrics, units, attributes, and expected boundaries.
- Content-recording behavior and configuration variable.
- Experimental feature gate.
- Error and cancellation behavior.
- Operation coverage for typed, raw, and streaming APIs.

The matrix must distinguish official OpenTelemetry fields from Foundry
extensions. In particular, resolve:

- `gen_ai.system` versus `gen_ai.provider.name`.
- `microsoft.foundry` as the provider value.
- `create_conversation` as a custom operation.
- `gen_ai.agent.type` and hosted-agent attributes.
- `gen_ai.workflow.action` versus `gen_ai.conversation.item`.
- `gen_ai.request.max_input_tokens` and
  `gen_ai.request.max_output_tokens` versus `gen_ai.request.max_tokens`.
- `gen_ai.request.reasoning.effort` versus
  `gen_ai.request.reasoning.level`.
- Whether content-disabled mode omits message attributes or emits structural
  redactions.
- `OTEL_INSTRUMENTATION_GENAI_CAPTURE_MESSAGE_CONTENT` versus
  `AZURE_TRACING_GEN_AI_CONTENT_RECORDING_ENABLED`.

Current cross-language implementations are useful inputs but are not mutually
consistent:

- [Python telemetry](https://github.com/Azure/azure-sdk-for-python/tree/main/sdk/ai/azure-ai-projects/azure/ai/projects/telemetry)
- [JavaScript tracing constants](https://github.com/Azure/azure-sdk-for-js/blob/main/sdk/ai/ai-projects/src/tracing/constants.ts)
- [.NET telemetry constants](https://github.com/Azure/azure-sdk-for-net/blob/main/sdk/ai/Azure.AI.Projects.Agents/src/Custom/Telemetry/OpenTelemetryConstants.cs)
- [OpenTelemetry GenAI conventions](https://github.com/open-telemetry/semantic-conventions-genai)

### Exit criteria

- The contract matrix is approved by the relevant Foundry and SDK owners.
- Every emitted Java field has a defined name, type, requirement level, and
  source.
- All custom Foundry fields are explicitly identified.

## Phase 1: Complete Operation Coverage

### Agent creation

- Instrument every supported `createAgentVersion` overload.
- Cover synchronous and asynchronous clients.
- Cover prompt, workflow, hosted, and unknown agent definitions.
- Record response-assigned agent identity and version.
- Put sampling-relevant values such as agent name and request model into
  `StartSpanOptions` when available.

### Responses

- Cover typed non-streaming responses.
- Cover typed streaming responses.
- Decide and implement coverage for raw-response methods.
- Cover both synchronous and asynchronous clients.
- Record request model, response model, response ID, finish reasons, token
  usage, conversation ID, tools, reasoning options, and approved content fields.

### Conversations

- Start the `create_conversation` span before the network call.
- End the span after success, error, or cancellation.
- Record the returned conversation ID.
- Decide through API review whether conversation methods belong on
  `ResponsesClient` and `ResponsesAsyncClient`.
- Do not retain new public conversation methods if they exist only as telemetry
  scaffolding.

### Exit criteria

- The approved operation matrix has no unexplained sync/async or
  typed/raw/streaming gaps.
- Equivalent overloads emit equivalent telemetry.

## Phase 2: Correct Context and Lifecycle Handling

- Continue passing the GenAI span context through `RequestOptions` for
  `AgentsClient` and `AgentsAsyncClient`.
- Propagate context through Reactor for OpenAI Java asynchronous response calls.
- Verify that the HTTP span is a child of the GenAI operation span.
- End asynchronous spans on completion, error, and cancellation.
- End synchronous streaming spans on exhaustion, error, and early close.
- Provide a viable close path for partially consumed streams.
- Preserve the original exception and record it on the span.
- Use monotonic elapsed time for duration metrics.
- Ensure span closure remains idempotent.

### Exit criteria

- Parent-child relationships are asserted for sync and async requests.
- No tested error, cancellation, or stream-consumption path leaks a span.
- Exceptions are propagated unchanged.

## Phase 3: Align Attributes, Content, and Metrics

- Use the exact attribute names and value types from the approved contract.
- Emit numeric attributes as numbers where required.
- Emit collection attributes, including finish reasons, as collections.
- Replace hand-built JSON with `azure-json` and `JsonWriter`.
- Validate message, tool-call, tool-response, workflow, and system-instruction
  payloads against the selected schemas.
- Avoid accumulating content when content recording is disabled unless
  structural data is explicitly required.
- Add request and response model attributes to metrics when available.
- Record `gen_ai.client.operation.duration`.
- Record `gen_ai.client.token.usage` separately for input and output tokens.
- Include required provider, operation, server, model, and error metric
  attributes.

### Exit criteria

- Attribute names, values, and types match the approved golden data.
- Content is absent or redacted according to the approved privacy behavior.
- Span and metric tests cover success and failure.

## Phase 4: Make Instrumentation Regeneration-safe

Preferred approach:

- Weave telemetry fields, constructors, and method bodies through
  `customizations/AgentsCustomizations.java`.
- Apply customization to every approved generated overload.
- Keep generated members marked consistently with repository conventions.

Fallback approach:

- Retain manual convenience-method customization only if regeneration
  validation proves that non-generated members are preserved reliably.

Do not add separate `traced*` methods. Tracing must remain transparent to users
who configure telemetry on the normal client.

### Exit criteria

- Running `tsp-client update` does not remove or duplicate instrumentation.
- A regeneration diff contains no unexpected telemetry changes.

## Phase 5: Verification

Use an in-memory OpenTelemetry SDK and assert telemetry data directly.

Required coverage:

- All supported agent types and overloads.
- Sync and async clients.
- Typed, raw, and streaming responses.
- Conversation success and failure.
- Stream completion, error, cancellation, and early close.
- Content enabled and disabled.
- Escaping and schema-valid structured content.
- Span names, kinds, status, attributes, events, and exact value types.
- Parent-child relationships between GenAI and HTTP spans.
- Duration and token metrics.
- Metrics-only, tracing-only, and fully disabled clients.
- Isolation between clients with different `ClientOptions`.
- Invalid or absent endpoint metadata.
- Java 8 and supported Java LTS versions.

Where practical, derive golden expected telemetry from the current Python
Foundry tests so Java remains cross-language compatible.

Repository validation must include:

- Package build and unit tests.
- Checkstyle and SpotBugs.
- API compatibility checks.
- Sample and README snippet compilation.
- TypeSpec regeneration validation.

## Phase 6: Documentation and Release

### `azure-ai-agents`

- Replace proof-of-concept wording with the approved experimental contract.
- Document configuration through `ClientOptions`.
- Document the experimental feature gate if retained.
- Document content privacy behavior and its opt-in variable.
- Provide console and Azure Monitor samples.
- Document sync, async, and streaming span lifetime behavior.
- Update the changelog only after the implementation contract is final.

### `azure-ai-projects`

`azure-ai-projects` depends on `azure-ai-agents`, but its production builder
does not construct agents clients. Its README and samples instantiate
`AgentsClientBuilder` directly.

Therefore:

- Keep the telemetry implementation in `azure-ai-agents`.
- Add Projects documentation that shows telemetry configuration on
  `AgentsClientBuilder`.
- Verify that the transitive dependency and Projects samples compile.
- Add production telemetry to `azure-ai-projects` only if Projects-native
  operations are included in the approved contract.

## Pull Request Strategy

1. Capture the approved contract and remaining requirements from PR #49434.
2. Continue implementation in PR #49706.
3. Rebase PR #49706 on the current `main` branch.
4. Redirect or close PR #49434 after its requirements are represented here.
5. Keep contract, implementation, tests, and documentation changes reviewable
   as separate commits where practical.
6. Request Java SDK, Foundry telemetry, and API reviewers before removing draft
   status.

## Completion Criteria

Telemetry is ready to ship when:

- The Foundry telemetry contract is approved and versioned.
- All approved operations and overloads are instrumented.
- Per-client configuration is preserved without public global mutable state.
- HTTP span parenting works for sync and async clients.
- Streaming spans close on all supported terminal paths.
- Content remains disabled by default.
- Attributes and metrics match golden cross-language expectations.
- Regeneration preserves the implementation.
- Package tests, quality checks, API checks, and samples pass.
- `azure-ai-agents` and `azure-ai-projects` documentation is consistent.
