# GenAI Telemetry Architecture Decision Records

Status: **Open for review**

Last updated: **2026-08-25**

These records capture decisions that must be made before the GenAI telemetry
implementation in
[PR #49706](https://github.com/Azure/azure-sdk-for-java/pull/49706) is ready to
ship. They are intentionally unresolved: the Java implementation owner should
not decide cross-language telemetry contracts, privacy behavior, public API
shape, or release policy without the relevant owners.

Each resolution must include:

- The selected option and rationale.
- Approvers and approval date.
- A public source link, such as a PR comment, issue, specification commit, or
  approved contract document.
- Any follow-up implementation, test, documentation, or compatibility work.

The primary inputs are:

- [GenAI telemetry roadmap](ROADMAP.md)
- [Java proof-of-concept notes](TRACING_NOTES.md)
- [Java implementation PR #49706](https://github.com/Azure/azure-sdk-for-java/pull/49706)
- [Original prototype PR #49434](https://github.com/Azure/azure-sdk-for-java/pull/49434)
- [OpenTelemetry GenAI semantic conventions](https://github.com/open-telemetry/semantic-conventions-genai)
- [Azure SDK implementation guidelines](https://azure.github.io/azure-sdk/general_implementation.html)
- [Python Foundry telemetry](https://github.com/Azure/azure-sdk-for-python/tree/main/sdk/ai/azure-ai-projects/azure/ai/projects/telemetry)
- [JavaScript Foundry tracing constants](https://github.com/Azure/azure-sdk-for-js/blob/main/sdk/ai/ai-projects/src/tracing/constants.ts)
- [.NET Foundry telemetry constants](https://github.com/Azure/azure-sdk-for-net/blob/main/sdk/ai/Azure.AI.Projects.Agents/src/Custom/Telemetry/OpenTelemetryConstants.cs)

## ADR 1: Telemetry contract version

### Description

Java needs a versioned contract that defines every emitted span, event, metric,
attribute, value type, and schema URL. The current cross-language
implementations and evolving OpenTelemetry GenAI conventions are useful inputs,
but they are not mutually consistent.

The decision must select an exact OpenTelemetry GenAI semantic-conventions
release or source commit and identify the versioned Foundry extension contract.
The contract must distinguish required, conditional, recommended, opt-in, and
custom fields.

### Required participants

- Foundry telemetry contract owner
- OpenTelemetry or observability representative
- Java, Python, JavaScript, and .NET SDK representatives

### Resolution

TBD

## ADR 2: SDK and service span model

### Description

SDK and service spans need distinguishable names and a defined parent-child
model. One prior proposal used an SDK `request_*` prefix, such as
`request_invoke_agent`, while the service emitted `invoke_agent`. An alternative
uses one SDK `invoke_agent` parent span with service `chat`, `execute_tool`, and
related spans beneath it.

The decision must define span names, kinds, operation names, and expected
hierarchy for agent creation, responses, conversations, tools, and workflows.
It must also state whether OpenTelemetry events remain part of the contract.

### Required participants

- Foundry telemetry contract owner
- Foundry service tracing owner
- Cross-language SDK representatives
- Portal or trace-visualization owner

### Resolution

TBD

## ADR 3: Provider and Azure resource identity

### Description

The implementation needs canonical values for provider and Azure resource
identity. Open questions include:

- Whether to emit only `gen_ai.provider.name` or also the legacy
  `gen_ai.system`.
- Whether `microsoft.foundry` is the canonical provider value.
- Whether `az.namespace` should be `Microsoft.CognitiveServices` for Foundry
  Agents or use another resource-provider namespace.

The selected values must work with the Foundry backend and the chosen
OpenTelemetry schema.

### Required participants

- Foundry telemetry contract owner
- Foundry resource-provider owner
- Azure Monitor or observability representative
- Cross-language SDK representatives

### Resolution

TBD

## ADR 4: Configuration and experimental feature model

### Description

[PR #49434](https://github.com/Azure/azure-sdk-for-java/pull/49434) used
process-global mutable configuration, public tracing options, and
`setExperimental(true)`. The Java proof of concept instead constructs a
per-client `Tracer` and `Meter` from `ClientOptions`, with no public global
toggle.

The decision must determine:

- Whether per-client Java configuration is approved as an intentional
  cross-language difference.
- Whether `AZURE_EXPERIMENTAL_ENABLE_GENAI_TRACING` is required as an internal
  feature gate.
- Whether any public API requires `@Beta`, or preview status is represented by
  package versioning and an internal gate.
- How global tracing enablement changes interact with client-specific options.

Any new Azure environment variable requires Architecture Board approval under
the
[Azure SDK implementation guidelines](https://azure.github.io/azure-sdk/general_implementation.html).

### Required participants

- Java SDK architect or API reviewer
- Azure SDK Architecture Board representative
- Foundry telemetry owner
- Cross-language SDK representatives

### Resolution

TBD

## ADR 5: Trace-context propagation and privacy

### Description

GenAI spans need to parent HTTP and service spans consistently across sync,
async, typed, raw-response, and streaming calls. Service propagation may need a
separate opt-in because OpenTelemetry baggage can contain sensitive data.

The decision must determine:

- Whether service propagation is enabled automatically with tracing or through
  a separate gate.
- Whether `AZURE_TRACING_GEN_AI_ENABLE_TRACE_CONTEXT_PROPAGATION` is the
  approved configuration variable.
- Which W3C headers are propagated.
- Whether baggage is always excluded, explicitly opt-in, or required.
- How propagation works through Reactor and OpenAI Java asynchronous calls.

### Required participants

- Foundry service tracing owner
- Azure SDK security or privacy reviewer
- Azure Core tracing owner
- Java and cross-language SDK representatives

### Resolution

TBD

## ADR 6: Content recording and redaction

### Description

Prompt, response, tool, workflow, system-instruction, and agent content must be
disabled by default. The contract still needs to define the opt-in mechanism and
the telemetry shape when content is disabled.

The decision must determine:

- Whether to use `OTEL_INSTRUMENTATION_GENAI_CAPTURE_MESSAGE_CONTENT`,
  `AZURE_TRACING_GEN_AI_CONTENT_RECORDING_ENABLED`, or another approved key.
- Whether disabled content is omitted entirely or represented using structural
  redactions.
- Which message, tool-call, tool-output, workflow, and instruction schemas are
  approved.
- Whether implementations may accumulate or serialize content while recording
  is disabled.

Any Azure-specific environment variable requires Architecture Board approval.

### Required participants

- Foundry telemetry contract owner
- Azure SDK security or privacy reviewer
- Azure SDK Architecture Board representative
- Cross-language SDK representatives

### Resolution

TBD

## ADR 7: Attribute and event contract

### Description

Several attribute names, types, and Foundry extensions differ across current
implementations. The decision must resolve at least:

- `gen_ai.request.max_input_tokens` and
  `gen_ai.request.max_output_tokens` versus `gen_ai.request.max_tokens`.
- `gen_ai.request.reasoning.effort` versus
  `gen_ai.request.reasoning.level`.
- `gen_ai.workflow.action` versus `gen_ai.conversation.item`.
- `create_conversation` as a custom operation.
- `gen_ai.agent.type`, hosted-agent fields, and other Foundry extensions.
- Numeric attributes as numbers and collection attributes as collections.
- Required event names, payload schemas, and content gating.

Every selected field needs a name, value type, requirement level, source, and
Java mapping. Intentional cross-language differences need a rationale.

### Required participants

- Foundry telemetry contract owner
- Cross-language SDK representatives
- OpenTelemetry or observability representative
- Foundry agent and workflow model owners

### Resolution

TBD

## ADR 8: Operation and public API coverage

### Description

The approved instrumentation surface is not yet final. The decision must define
coverage for:

- Every `createAgentVersion` overload and supported agent definition.
- Typed and raw-response methods.
- Non-streaming and streaming methods.
- Synchronous and asynchronous clients.
- Agent get, list, delete, sessions, memory stores, toolboxes, and other CRUD
  operations.
- Conversation creation.
- Projects-native operations.

Conversation methods must undergo API review before remaining on
`ResponsesClient` or `ResponsesAsyncClient`. Public methods must not be added
solely as telemetry scaffolding. Projects-native telemetry should be added only
if the approved Foundry contract includes those operations.

### Required participants

- Foundry API owner
- Java SDK API reviewer
- Foundry telemetry contract owner
- `azure-ai-projects` and `azure-ai-agents` maintainers
- Cross-language SDK representatives

### Resolution

TBD

## ADR 9: Streaming lifecycle and raw-stream compatibility

### Description

Streaming spans must close without leaks on exhaustion, error, cancellation,
and early termination. Raw-response streaming also needs to work for downstream
framework consumers that currently use tracing workarounds.

The decision must define:

- When sync and async streaming spans start and end.
- How a partially consumed synchronous stream is explicitly closed.
- Cancellation semantics.
- Whether response attributes and token usage are available on each terminal
  path.
- The supported raw-stream entry point.
- Whether downstream framework workarounds can be removed.

The maintained E2E harness must include the same raw-stream path used by
downstream consumers.

### Required participants

- Java SDK implementation owner
- OpenAI Java integration owner
- Downstream framework representative
- Foundry telemetry contract owner

### Resolution

TBD

## ADR 10: Metrics contract

### Description

The implementation currently targets `gen_ai.client.operation.duration` and
`gen_ai.client.token.usage`, but the complete metric contract is not approved.

The decision must define:

- Metric names, descriptions, units, and histogram boundaries.
- Input and output token recording.
- Required provider, operation, server, model, and error dimensions.
- Whether request and response model dimensions are required.
- Behavior for metrics-only, tracing-only, and disabled clients.
- Error, cancellation, and streaming duration behavior.

Golden metric data must assert emitted values and dimensions rather than only
confirming that instrumented operations execute.

### Required participants

- Foundry telemetry contract owner
- Azure Monitor or observability representative
- Cross-language SDK representatives
- Java SDK metrics owner

### Resolution

TBD

## ADR 11: Regeneration-safe instrumentation

### Description

Tracing must remain transparent on normal client methods and survive TypeSpec
regeneration. The preferred approach is to weave telemetry fields,
constructors, and method bodies through
`customizations/AgentsCustomizations.java`. The fallback is to retain manually
customized convenience methods only if regeneration reliably preserves them.

The decision must select the supported approach and define how all approved
sync, async, typed, raw-response, and streaming overloads are instrumented
without duplication or drift. A separate public `traced*` API is not an option.

### Required participants

- Java SDK code-generation owner
- `azure-ai-agents` maintainer
- Java SDK implementation reviewer

### Resolution

TBD

## ADR 12: Verification, ownership, and release readiness

### Description

The project needs named owners and an acceptance process. The decision must
identify:

- The approver for the telemetry contract.
- The Java owner responsible for closing each roadmap phase.
- The owner and maintained location of the E2E harness and
  `trace-output-reference.txt`.
- The integration reviewer and release decision maker.
- The target release milestone.

Release acceptance must include in-memory telemetry assertions, approved golden
data, the maintained E2E harness, raw-stream compatibility, TypeSpec
regeneration, API checks, samples, and `azure-ai-projects` documentation.

An end-of-month release has been discussed as a target but is not treated as a
commitment until the release owner records it here. A separate raw-stream
hotfix is not currently planned; consumer impact may cause that decision to be
revisited.

### Required participants

- Foundry Java integration or release owner
- Java SDK implementation owner
- Foundry telemetry contract owner
- Prototype and E2E reference owner
- Downstream framework representative

### Resolution

TBD
