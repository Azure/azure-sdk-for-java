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
- Serializes GenAI message attributes with `azure-json`, including structured
  tool results and content-safe escaping.
- Has SDK-local fixture tests for text and function-call response payloads,
  content gating, structured tool results, malformed tool results, and special
  characters.
- Survives `tsp-client update`.
- Passes the maintained live quick suite in synchronous and asynchronous modes,
  with 200/200 checks across typed, raw-response, raw-streaming, and tool-call
  scenarios with content recording both disabled and enabled.

## Python parity gaps

No implementation gaps are currently identified for the supported Java
surface.

### Trace propagation terminology and language difference

Trace context consists of the `traceparent` and optional `tracestate` headers.
They carry trace and span identifiers so client and service spans can be joined.

OpenTelemetry baggage is different: it is an application-defined collection of
key/value pairs that may contain tenant IDs, experiment labels, or other
customer-controlled data. It is not needed to join spans and is therefore more
sensitive.

Python exposes separate controls because it installs an HTTP hook on an OpenAI
client obtained from the Projects client:

- Trace-context propagation defaults to enabled.
- Baggage propagation defaults to disabled.

Java sends trace context through the normal Azure Core HTTP pipeline. Azure
Core's OpenTelemetry propagator emits only `traceparent` and `tracestate`; the
private async bridge also carries only the in-process Azure `Context` and strips
its opaque token before transmission. Java does not send baggage.

The default wire behavior is therefore aligned. Java does not copy Python's
extra propagation switches because the Python switches configure a separately
acquired OpenAI client, while the Java SDK owns and configures its Azure Core
pipeline. Adding Java-only pipeline interception solely to reproduce those
Python plumbing switches would go beyond the required behavioral parity.

## Resolved parity gaps

| Area | Resolution |
| --- | --- |
| Semantic-convention schema | Java now declares the Python baseline schema, `1.34.0`. |
| Experimental gate | Java honors `AZURE_EXPERIMENTAL_ENABLE_GENAI_TRACING=true` internally while retaining per-client Azure Core configuration and no mutable global API. |
| Content opt-in | Java uses `OTEL_INSTRUMENTATION_GENAI_CAPTURE_MESSAGE_CONTENT`, defaulting to `false`; the former Azure setting is a compatibility fallback. |
| Provider attributes | Java emits `gen_ai.provider.name=microsoft.foundry` and no longer unconditionally emits the legacy `gen_ai.system` attribute, matching the current Python paths in scope. |
| Metrics verification | In-memory tests assert duration and token metric names, units, values, provider, operation, server, model, token type, and error dimensions. Response operations use Python's `responses` metric operation and `input` / `completion` token types. |
| Raw responses and raw streams | Existing sync and async protocol methods are instrumented. Raw streaming spans remain open through stream exhaustion, failure, cancellation, or explicit close and use the same async context bridge as typed calls. |
| Response fixture coverage | SDK-local tests exercise text and function-call response fixtures, content gating, structured and malformed tool results, and special-character escaping. |
| Structured event serialization | GenAI message attributes use `azure-json` structured serialization instead of manual JSON string concatenation. |

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
- Supported operation paths and metrics have direct automated coverage.
- Content-disabled tests prove that no customer-controlled content is emitted.
- Sync and async HTTP spans remain direct children of their GenAI spans.
- The implementation survives TypeSpec regeneration.
