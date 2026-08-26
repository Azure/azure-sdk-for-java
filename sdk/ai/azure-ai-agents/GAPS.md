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
  with 200/200 checks across typed, raw-response, raw-streaming, and tool-call
  scenarios with content recording both disabled and enabled.

## Python parity gaps

| Priority | Area | Python behavior | Current Java behavior | Required work |
| --- | --- | --- | --- | --- |
| P1 | Propagation controls | Has separate trace-context and baggage controls; trace context is enabled by default and baggage is disabled by default | Propagates trace context through Azure Core; the Java bridge never propagates baggage | Decide whether Java needs the same configuration controls. Keep baggage excluded unless explicitly approved. |
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
3. Record the release owner and target milestone.

## Resolved parity gaps

| Area | Resolution |
| --- | --- |
| Semantic-convention schema | Java now declares the Python baseline schema, `1.34.0`. |
| Experimental gate | Java honors `AZURE_EXPERIMENTAL_ENABLE_GENAI_TRACING=true` internally while retaining per-client Azure Core configuration and no mutable global API. |
| Content opt-in | Java uses `OTEL_INSTRUMENTATION_GENAI_CAPTURE_MESSAGE_CONTENT`, defaulting to `false`; the former Azure setting is a compatibility fallback. |
| Provider attributes | Java emits `gen_ai.provider.name=microsoft.foundry` and no longer unconditionally emits the legacy `gen_ai.system` attribute, matching the current Python paths in scope. |
| Metrics verification | In-memory tests assert duration and token metric names, units, values, provider, operation, server, model, token type, and error dimensions. Response operations use Python's `responses` metric operation and `input` / `completion` token types. |
| Raw responses and raw streams | Existing sync and async protocol methods are instrumented. Raw streaming spans remain open through stream exhaustion, failure, cancellation, or explicit close and use the same async context bridge as typed calls. |

## Intentional non-gaps

The following differences should not be implemented merely to imitate older
Python or prototype code:

- Workflow-agent telemetry and workflow-specific events.
- Legacy Projects operations that are not part of the current Agents API.
- Conversation-item listing, which is not exposed by the current Java Agents API.
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
