# Azure Agent Server - Developer Guide

This guide describes how `sdk/agentserver` is structured, what `azure-agentserver-api` provides, and how consumers wire
it into runnable services.

## Architecture overview

`sdk/agentserver` is a multi-module Maven project with three layers:

1. **Core API (`azure-agentserver-api`)**
    - Protocol-facing types (`AgentServerCreateResponse`, `CreateResponse`, event streams).
    - Extension points (`ResponseHandler`, `ResponsesProvider`).
    - Default orchestration (`AgentServerResponsesApi`) for sync + streaming + background flows.
2. **Web adapters**
    - Spring (`azure-agentserver-spring`)
    - JAX-RS/Jersey (`azure-agentserver-api-jaxrs`, `azure-agentserver-jersey`)
3. **Samples and example framework integrations**
    - Runnable end-user samples under `azure-agentserver-samples`.
    - Example framework integrations under:
      `azure-agentserver-samples/azure-agentserver-example-framework-integrations/azure-agentserver-agentframeworks`.

## Core runtime flow

1. HTTP adapter receives request and builds `RequestMetadata` (headers, isolation, client headers, query params).
2. Adapter delegates to `ResponsesApi`.
3. `AgentServerResponsesApi` resolves response/session IDs, builds `ResponseContext`, and invokes `ResponseHandler`.
4. Results are normalized/persisted and returned as JSON or SSE.
5. Platform headers are emitted (`x-request-id`, `x-platform-server`, optional `x-agent-session-id`).

## Key extension points

- **Implement a custom agent:** implement `ResponseHandler`.
    - `createResponse(...)` for sync responses.
    - `createAsync(...)` for SSE responses.
- **Customize persistence:** implement `ResponsesProvider`.
- **Swap web stack:** use either Spring adapter or Jersey adapter around the same `ResponsesApi`.

## `azure-agentserver-api` deep dive

`azure-agentserver-api` is the core contract layer your code should target.

### Main responsibilities

- Defines protocol contracts (`ResponsesApi`, `ResponseHandler`, `ResponseEventStream`, `ResponseContext`).
- Provides default orchestration (`AgentServerResponsesApi`) for:
    - sync and streaming response creation
    - response persistence
    - background lifecycle and cancellation
    - stream replay for stored streaming responses
- Handles platform-aware request metadata:
    - isolation context
    - client headers (`x-client-*`)
    - request IDs
    - session IDs (`agent_session_id`)
- Provides ID/session normalization helpers (`IdGenerator`, `SessionIdResolver`, `ResponseBuilder`).

### How handlers are expected to use it

Inside a `ResponseHandler`, `ResponseContext` is the primary runtime surface:

- `getResponseId()` for correlation and deterministic child IDs.
- `getInputItemsAsync()` for normalized request input (including resolved references).
- `getHistoryAsync()` for conversation history stitching.
- `isCancelled()`/`isShutdownRequested()` for cooperative cancellation.
- `getIsolation()`, `getClientHeaders()`, `getQueryParameters()`, `getRequestId()`, `getSessionId()` for platform
  context.

### Minimal integration pattern

```java
ResponsesApi api = ResponsesApi.builder()
    .responseHandler(new MyHandler())
    .build();
```

Then host `api` with either adapter:

- Jersey: `JerseyAgentServerAdaptorService.buildAgent(api);`
- Spring: `SpringAgentServerAdaptorService.run(api);`

### Streaming model

- Handler returns `ResponseEventStream` from `createAsync(...)`.
- Emit lifecycle and output events via fluent builders (`emitCreated()`, `emitInProgress()`, `addOutputMessage(...)`,
  `emitCompleted()`).
- Adapters translate stream events into SSE frames on `/responses/streaming`.
- Stored background streams can be replayed via `GET /responses/{id}/stream`.

### Persistence model

- If hosted environment variables are present, provider auto-resolves to `FoundryStorageProvider`.
- Otherwise defaults to `InMemoryResponseProvider`.
- You can override with a custom `ResponsesProvider` via `ResponsesApi.builder().provider(...)`.

## Streaming and background behavior

- Streaming endpoint is `/responses/streaming`.
- Replay endpoint is `GET /responses/{id}/stream` and requires a stored streaming response.
- `background=true` requires `store=true`.
- Cancellation is supported through `/responses/{id}/cancel`.
- Client disconnect signaling is coordinated so cancelled state wins over late terminal writes.

## Module map

| Module                                                                                                                                       | What to change here                                                   |
|----------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------|
| `azure-agentserver-api`                                                                                                                      | Protocol/domain behavior, IDs, storage logic, event stream semantics. |
| `azure-agentserver-spring`                                                                                                                   | Spring filters/controllers/exception mapping and bootstrapping.       |
| `azure-agentserver-api-jaxrs`                                                                                                                | JAX-RS resources/filters/exception mapping.                           |
| `azure-agentserver-jersey`                                                                                                                   | Jersey runtime and server host wiring.                                |
| `azure-agentserver-samples/azure-agentserver-example-framework-integrations/azure-agentserver-agentframeworks/azure-agentserver-langchain4j` | Example LangChain4j request/message mapping and invocation plumbing.  |
| `azure-agentserver-samples`                                                                                                                  | Reference implementations and runnable usage patterns.                |

## Build and test

From `sdk/agentserver`:

```bash
./mvnw clean package -DskipTests
./mvnw clean verify
```

## Important support boundary

`azure-agentserver-samples/azure-agentserver-example-framework-integrations/azure-agentserver-agentframeworks` is
intentionally shipped as **integration sample/example code** and is **not an officially supported API surface**. Keep
framework-specific behavior isolated there and avoid coupling core API contracts to framework internals.
