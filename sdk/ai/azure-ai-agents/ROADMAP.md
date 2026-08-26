# GenAI Telemetry Roadmap

Status: **Planning**

Last updated: **2026-08-26**

This document defines the work required to complete GenAI telemetry for
`azure-ai-agents` and its use from `azure-ai-projects`.

The intended implementation branch is
[PR #49706](https://github.com/Azure/azure-sdk-for-java/pull/49706). The original
[PR #49434](https://github.com/Azure/azure-sdk-for-java/pull/49434) remains a
requirements and prototype reference, but its implementation should not be merged
directly.

See [TRACING_NOTES.md](TRACING_NOTES.md) for detailed design investigations and
open technical questions discovered while building the proof of concept. See
[ADR.md](ADR.md) for the unresolved decisions, required participants, and final
resolutions.

## Decision Provenance

Use public, durable sources for normative requirements and final decisions:

- [PR #49706](https://github.com/Azure/azure-sdk-for-java/pull/49706) is the
  Java implementation and review record.
- [PR #49434](https://github.com/Azure/azure-sdk-for-java/pull/49434) is the
  original prototype and requirements reference.
- The
  [OpenTelemetry GenAI semantic conventions](https://github.com/open-telemetry/semantic-conventions-genai)
  define standard telemetry fields and behavior.
- The
  [Azure SDK implementation guidelines](https://azure.github.io/azure-sdk/general_implementation.html)
  define client configuration requirements and require Architecture Board
  approval for new environment variables.

Team discussions also identified requirements around SDK/service span naming,
trace-context propagation privacy, raw-stream compatibility, end-to-end
validation, and release ownership. Before Phase 0 exits, resolve the
corresponding records in [ADR.md](ADR.md) and link each resolution to PR #49706
or a public issue. Do not rely on chat or meeting history as the only
specification for shipped behavior.

The August 25, 2026 Java tracing design discussion established the current
working approach: use the current Python implementation as the behavioral
baseline, use Azure SDK telemetry abstractions before direct OpenTelemetry APIs,
validate end to end through a Foundry project connected to Application Insights,
and require Azure SDK architecture review before merge. The historical tracing
specification remains useful context, but it is a living document and must not
override current implementations or the approved contract.

## Goals

- Emit Foundry GenAI spans and metrics that conform to an explicitly versioned
  cross-language telemetry contract.
- Follow Azure SDK for Java conventions and reuse `azure-core` tracing, metrics,
  configuration, and HTTP pipeline infrastructure.
- Keep telemetry configuration and state isolated per client.
- Cover synchronous, asynchronous, regular, raw-response, and streaming entry
  points consistently.
- Cover raw-response streaming explicitly, including compatibility with
  consumers that currently require tracing workarounds.
- Preserve trace context between GenAI operation spans and HTTP spans.
- Keep prompt, response, tool, workflow, and agent content disabled by default.
- Make generated-client instrumentation deterministic and resilient to SDK
  regeneration.
- Provide tests that assert emitted telemetry rather than only verifying that
  instrumented operations execute.
- Validate that emitted client spans are visible and correctly correlated in
  Foundry and Application Insights.

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
| Reference behavior | Treat the current Python implementation as the primary cross-language baseline; use other languages and the historical spec as corroborating inputs. |
| Abstraction | Prefer `azure-core` tracing and metrics abstractions; use direct OpenTelemetry APIs only for documented gaps that cannot be addressed in Azure SDK abstractions. |
| HTTP tracing | Use `azure-core` pipeline instrumentation and propagate the GenAI span context. |
| Generated methods | Instrument the existing convenience methods transparently. |
| Content | Disable content recording by default and require explicit opt-in; without opt-in, emit no customer-controlled prompts, responses, messages, instructions, or function arguments. |
| Library metadata | Read client name and version from `azure-ai-agents.properties`. |

If the cross-language contract requires
`AZURE_EXPERIMENTAL_ENABLE_GENAI_TRACING`, implement the gate internally without
restoring the public `GenAiTracingConfiguration` and `GenAiTracingOptions` API
from PR #49434. Confirm that the environment variable has the required Azure SDK
Architecture Board approval.

The per-client configuration and internal-gate direction intentionally differs
from PR #49434, which used process-global configuration and a public
`setExperimental(true)` option. Phase 0 must explicitly approve this difference;
it must not be treated as an implementation-only refactoring. The same decision
must specify whether any remaining public API requires `@Beta`, or record that
preview status is conveyed only through package versioning and the internal
feature gate.

## Working Ownership and Milestone

| Area | Working owner | Required confirmation |
| --- | --- | --- |
| Java implementation | Author and reviewers of [PR #49706](https://github.com/Azure/azure-sdk-for-java/pull/49706) | Confirm the Java SDK owner responsible for closing each phase. |
| Prototype and E2E reference | Author of [PR #49434](https://github.com/Azure/azure-sdk-for-java/pull/49434) | Confirm the location and supported use of the E2E harness and reference output. |
| Telemetry contract | Foundry telemetry owners and cross-language representatives | Name the approver for the contract matrix and Foundry extensions. |
| Integration and release | Foundry Java integration/release owner | Name the final reviewer and release decision maker. |

An end-of-month release was discussed as a target, not established as a firm
commitment. Record the actual milestone and release owner after Phase 0 contract
approval. A separate hotfix is not currently required for the raw-stream
scenario; revisit that decision only if consumer impact changes.

## Phase 0: Freeze the Telemetry Contract

**Owners:** Foundry telemetry owners, Java SDK owner, cross-language SDK
representatives.

Before further implementation, approve and record a contract matrix containing:

- Source link, rationale, approver, and approval date for every decision.
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
- Public API preview treatment, including the disposition of `@Beta`.
- Trace-context propagation gate, propagated headers, baggage treatment, and
  privacy behavior.
- Error and cancellation behavior.
- Operation coverage for typed, raw, and streaming APIs.
- Consumer compatibility requirements for raw-response streaming.

Use [ADR.md](ADR.md) to record the decision, participants, rationale, approval,
and public evidence for each unresolved area. The contract matrix may be stored
separately, but its approved version must be linked from ADR 1 and PR #49706.

The matrix must distinguish official OpenTelemetry fields from Foundry
extensions. In particular, resolve:

- The prior `request_*` SDK span-name proposal, such as
  `request_invoke_agent`, versus using an `invoke_agent` SDK parent span over
  service `chat`, `execute_tool`, and related spans.
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
- Whether service trace-context propagation remains separately opt-in through
  `AZURE_TRACING_GEN_AI_ENABLE_TRACE_CONTEXT_PROPAGATION`, and whether baggage
  is excluded because it may contain sensitive data.
- Per-client Java configuration versus the global experimental configuration
  used by PR #49434.

The current Python implementation is the primary behavioral reference because
it was implemented first and is the implementation other languages generally
follow. Do not copy Python-only legacy or deprecated paths without confirming
that they remain part of the current Agents contract. Other language
implementations are useful corroborating inputs but are not mutually consistent:

- [Python telemetry](https://github.com/Azure/azure-sdk-for-python/tree/main/sdk/ai/azure-ai-projects/azure/ai/projects/telemetry)
- [JavaScript tracing constants](https://github.com/Azure/azure-sdk-for-js/blob/main/sdk/ai/ai-projects/src/tracing/constants.ts)
- [.NET telemetry constants](https://github.com/Azure/azure-sdk-for-net/blob/main/sdk/ai/Azure.AI.Projects.Agents/src/Custom/Telemetry/OpenTelemetryConstants.cs)
- [OpenTelemetry GenAI conventions](https://github.com/open-telemetry/semantic-conventions-genai)

### Exit criteria

- The contract matrix is approved by the relevant Foundry and SDK owners.
- ADRs 1 through 10 are resolved, and ADRs 11 and 12 have named owners and an
  approved execution plan.
- The approved matrix or linked public decision record is referenced from
  PR #49706.
- Every emitted Java field has a defined name, type, requirement level, and
  source.
- All custom Foundry fields are explicitly identified.
- SDK/service span naming, propagation privacy, experimental gating, and
  `@Beta` treatment have explicit decisions.
- Each cross-language input has a Java mapping or a documented reason for
  intentional divergence.

## Phase 1: Complete Operation Coverage

Implementation status as of 2026-08-26:

- All current synchronous and asynchronous `createAgentVersion` convenience
  overloads use the tracing wrapper.
- Typed synchronous and asynchronous response and streaming entry points are
  instrumented.
- Conversation creation now wraps the service call and handles success, error,
  and cancellation.
- Workflow-specific instrumentation is intentionally excluded because workflow
  agents are retiring; unsupported definitions receive only generic agent
  identity attributes.
- Raw-response methods, conversation-item listing, and broader CRUD coverage
  remain blocked on ADR 8 and ADR 9 rather than being inferred from legacy
  Python or Projects APIs.

### Agent creation

- Instrument every supported `createAgentVersion` overload.
- Cover synchronous and asynchronous clients.
- Cover every agent definition in the approved current contract, including
  prompt, hosted, and unknown definitions.
- Explicitly confirm whether workflow-agent telemetry remains in scope; do not
  retain it solely because it exists in an older Python or prototype
  implementation.
- Record response-assigned agent identity and version.
- Put sampling-relevant values such as agent name and request model into
  `StartSpanOptions` when available.

### Responses

- Cover typed non-streaming responses.
- Cover typed streaming responses.
- Decide and implement coverage for raw-response methods.
- Cover raw-response streaming as its own required scenario; do not assume that
  typed streaming coverage exercises the same path.
- Cover both synchronous and asynchronous clients.
- Record request model, response model, response ID, finish reasons, token
  usage, conversation ID, tools, reasoning options, and approved content fields.
- Verify compatibility with consumers that currently use a workaround to obtain
  traces from raw streams, and document whether the workaround can be removed.

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

Implementation status as of 2026-08-26:

- Sync operations make the GenAI span current around request initiation.
- Async OpenAI response, streaming, and conversation operations use a
  per-client, request-scoped bridge to carry the original Azure `Context`
  across openai-java's deferred `CompletableFuture` execution. The opaque
  bridge token is consumed before the Azure request is built and is never sent
  to the service.
- Reactor owns span and bridge cleanup through success, error, and
  cancellation.
- Duration metrics use monotonic elapsed time.
- The maintained E2E verifier now requires each response HTTP span to share the
  GenAI operation's trace ID and use its span ID as the direct parent.
- Live synchronous and asynchronous quick runs each passed 142/142 checks with
  content recording both disabled and enabled. This verifies local exported
  spans against a live Foundry service; Foundry and Application Insights UI
  inspection remains outstanding.

- Continue passing the GenAI span context through `RequestOptions` for
  `AgentsClient` and `AgentsAsyncClient`.
- Propagate context through Reactor for OpenAI Java asynchronous response calls.
- Apply the Phase 0 propagation decision consistently to sync, async, typed,
  raw-response, and streaming calls.
- If service propagation is opt-in, inject only the approved W3C headers and
  verify that baggage or other potentially sensitive context is not forwarded
  unless the contract explicitly requires it.
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
- When content recording is disabled, do not collect or emit
  customer-controlled prompts, responses, messages, instructions, function
  arguments, tool results, or equivalent agent content.
- Allow only contract-approved generic metadata such as identifiers and
  structural fields in content-disabled mode, and test every content-bearing
  attribute and event against this rule.
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

Verification status as of 2026-08-26:

- `tsp-client update` completed successfully after temporarily restoring the
  expected `tsp-location.yaml` filename.
- Existing tracing constructors, fields, and customized non-`@Generated`
  methods were preserved without duplication, including all instrumented
  `createAgentVersion` overloads and conversation wrappers.
- The generator produced unrelated broad specification drift, which is not part
  of this tracing change and was removed after verifying preservation.
- ADR 11 remains open because one successful fallback verification does not
  replace an ownership decision about AST-based customization.

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
- Raw-response streaming through the same entry point used by downstream
  framework consumers.
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

In addition to unit-level golden data:

- Run the E2E tracing harness associated with PR #49434, or migrate its required
  scenarios into a maintained equivalent.
- Compare emitted output with `trace-output-reference.txt` after updating that
  reference to the approved Phase 0 contract.
- Record the reviewer and outcome for the E2E application and every supported
  configuration combination.
- Verify all approved operation types, metric names, units, attributes, and
  boundaries in the emitted output.
- Add a regression scenario for raw-stream tracing and confirm whether the
  downstream consumer workaround is still necessary.

Repository validation must include:

- Package build and unit tests.
- Checkstyle and SpotBugs.
- API compatibility checks.
- Sample and README snippet compilation.
- TypeSpec regeneration validation.

Live validation must also include:

- A dedicated Foundry project connected to an Application Insights resource.
- Marko's end-to-end tracing application, or an equivalent checked-in sample,
  as the initial scenario driver.
- Confirmation that client spans appear in the Foundry tracing UI and
  Application Insights with the expected parent-child relationships.
- Content-enabled and content-disabled runs, with explicit inspection that the
  disabled run contains no customer-controlled content.
- A validation run that keeps the agent available until traces are inspected.
  The current Foundry UI groups traces by agent, so deleting the agent during
  sample cleanup can make the traces difficult or impossible to access.

## Phase 6: Documentation and Release

### `azure-ai-agents`

- Replace proof-of-concept wording with the approved experimental contract.
- Document configuration through `ClientOptions`.
- Document the experimental feature gate if retained.
- Document the approved `@Beta` or package-version preview treatment.
- Document content privacy behavior and its opt-in variable.
- Document trace-context propagation separately from content recording,
  including whether baggage is propagated.
- Provide console and Azure Monitor samples.
- Document that Foundry UI validation requires the project to be connected to
  Application Insights.
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
2. Convert decisions that currently exist only in team discussions into
   PR #49706 comments, a linked public issue, or the approved contract artifact.
3. Continue implementation in PR #49706.
4. Rebase PR #49706 on the current `main` branch.
5. Redirect or close PR #49434 after its requirements are represented here.
6. Keep contract, implementation, tests, and documentation changes reviewable
   as separate commits where practical.
7. Request Java SDK, Foundry telemetry, and API reviewers before removing draft
   status.
8. Obtain Azure SDK architecture review before merge, with particular attention
   to any direct OpenTelemetry usage and the content-recording privacy boundary.

## Completion Criteria

Telemetry is ready to ship when:

- The Foundry telemetry contract is approved and versioned.
- All approved operations and overloads are instrumented.
- Raw-response streaming works for direct SDK and downstream framework use.
- Per-client configuration is preserved without public global mutable state.
- The intentional configuration differences from PR #49434 are approved.
- HTTP span parenting works for sync and async clients.
- Service trace-context and baggage propagation match the approved privacy
  policy.
- Streaming spans close on all supported terminal paths.
- Content remains disabled by default.
- Content-disabled telemetry contains no customer-controlled prompts,
  responses, messages, instructions, function arguments, or tool results.
- Attributes and metrics match golden cross-language expectations.
- Regeneration preserves the implementation.
- Package tests, quality checks, API checks, and samples pass.
- The maintained E2E harness and approved reference output pass review.
- Live client spans are visible and correlated in a Foundry project connected
  to Application Insights.
- Azure SDK architecture review is complete.
- `azure-ai-agents` and `azure-ai-projects` documentation is consistent.
- Final ownership and the release milestone are recorded.
