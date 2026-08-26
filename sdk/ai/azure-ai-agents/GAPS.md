# GenAI Telemetry Gaps

Status: **Implementation gap tracker**

Last updated: **2026-08-26**

This document tracks the concrete work remaining to bring Java GenAI telemetry
into parity with the current Python Foundry telemetry implementation.

Python is the behavioral baseline. Java should follow Azure SDK for Java and
`azure-core` conventions where language plumbing differs. Deprecated workflow
agents and legacy Projects functionality are intentionally excluded and are
not parity gaps.

## Current alignment

The Java implementation already:

- Instruments all current synchronous and asynchronous
  `createAgentVersion` convenience overloads.
- Instruments typed responses, typed streaming responses, and conversation
  creation for synchronous and asynchronous clients.
- Uses per-client `Tracer` and `Meter` instances created from `ClientOptions`.
- Keeps telemetry internal and transparent on the normal client methods.
- Disables customer-content recording by default.
- Uses Azure Core HTTP instrumentation and directly parents HTTP spans to the
  corresponding GenAI span.
- Propagates asynchronous response context through a per-client, request-scoped
  bridge without sending its internal token, baggage, or customer content to
  the service.
- Handles success, error, cancellation, stream completion, and explicit stream
  close for the typed paths.
- Uses monotonic duration measurement.
- Survives `tsp-client update`.
- Passes the maintained live quick suite in synchronous and asynchronous modes,
  with 142/142 checks in both content-disabled and content-enabled runs.

## Python parity gaps

| Priority | Area | Python behavior | Current Java behavior | Required work |
| --- | --- | --- | --- | --- |
| P0 | Semantic-convention schema | Declares `1.34.0` | Declares `1.29.0` | Align Java to `1.34.0`, or document a concrete compatibility reason not to. Verify affected attribute names and types at the same time. |
| P0 | Experimental gate | Requires `AZURE_EXPERIMENTAL_ENABLE_GENAI_TRACING=true` | Tracing activates when an OpenTelemetry provider is configured through `ClientOptions` | Confirm whether Java must honor the same internal environment gate. Do not restore the process-global mutable API from the prototype. |
| P0 | Content opt-in | Uses `OTEL_INSTRUMENTATION_GENAI_CAPTURE_MESSAGE_CONTENT` | Uses `AZURE_TRACING_GEN_AI_CONTENT_RECORDING_ENABLED` | Select one supported key and update implementation, tests, and documentation. Preserve the default-off privacy boundary. |
| P1 | Provider attributes | Emits `gen_ai.provider.name=microsoft.foundry` and conditionally retains legacy `gen_ai.system` | Emits both provider and legacy system attributes | Match Python's compatibility behavior and remove any redundant legacy attribute when safe. |
| P1 | Propagation controls | Has separate trace-context and baggage controls; trace context is enabled by default and baggage is disabled by default | Propagates trace context through Azure Core; the Java bridge never propagates baggage | Decide whether Java needs the same configuration controls. Keep baggage excluded unless explicitly approved. |
| P1 | Raw responses and raw streams | Wraps typed responses, typed streams, and raw-response streaming | Instruments typed responses and typed streams only | Confirm that current Java consumers require raw paths, then instrument and validate those paths without adding telemetry-only public APIs. |
| P1 | Conversation operations | Instruments conversation creation and conversation-item listing | Instruments conversation creation only | Add listing only if the operation exists in the approved current Agents API. Do not recreate legacy Projects APIs for parity. |
| P1 | Metrics verification | Emits operation-duration and token-usage metrics under the Python contract | Emits both metrics, but unit tests do not assert values and dimensions | Add in-memory metric assertions for names, units, values, dimensions, errors, cancellation, and streaming. |
| P2 | Response fixture coverage | Python tests can exercise response and stream payload formatting | Java lifecycle tests do not directly construct full openai-java response payloads | Add recorded fixtures or a narrow test seam for response attributes, usage, tool calls, and content gating. |
| P2 | Structured event serialization | Uses structured serialization | Some Java event payloads are built with string concatenation | Migrate to `azure-json` `JsonWriter` and add escaping/schema tests. |

## Release validation gaps

These are acceptance tasks rather than Python behavior differences:

1. Inspect the live client spans in Foundry and Application Insights and confirm
   their hierarchy and visibility. Local exporters already verify the exact
   GenAI-to-HTTP parent relationship.
2. Complete Azure SDK architecture review, focused on per-client configuration,
   environment-variable selection, propagation privacy, and the content
   boundary.
3. Confirm whether raw-response streaming is required for the first release and
   validate the same entry point used by downstream consumers if it is.
4. Record the release owner and target milestone.

## Intentional non-gaps

The following differences should not be implemented merely to imitate older
Python or prototype code:

- Workflow-agent telemetry and workflow-specific events.
- Legacy Projects operations that are not part of the current Agents API.
- Process-global mutable tracing configuration.
- Public `traced*` method variants.
- Public conversation methods added only as telemetry scaffolding.
- Wall-clock duration measurement when monotonic timing is available.
- Older hosted-agent model accessors that no longer match the generated Java
  models.

## Definition of done

Python parity is complete when:

- Every P0 item is resolved in code and documentation.
- Required P1 operation paths and metrics have direct automated coverage.
- Content-disabled tests prove that no customer-controlled content is emitted.
- Sync and async HTTP spans remain direct children of their GenAI spans.
- The implementation survives TypeSpec regeneration.
- Live traces are inspected in Foundry or Application Insights.
- Architecture and release owners approve the remaining intentional Java
  differences.
