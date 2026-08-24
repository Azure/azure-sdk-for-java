# Azure Agent Server - End-User Guide

This guide shows how to run and use the Java agent server as a library consumer.

## What you get

- A server adapter that implements OpenAI Responses-style endpoints.
- Health endpoints for container readiness/liveness probes.
- Sample apps for echo, LangChain4j, Spring, and financial multi-agent scenarios.

## Quick start with samples

All sample projects are under `azure-agentserver-samples`.

### Echo sample

```bash
cd azure-agentserver-samples/azure-agentserver-echo-sample
./run-sample.sh
```

### LangChain4j sample (Jersey)

1. For **local testing only**, set env vars in `sdk/agentserver/.env`:
    - `AZURE_DEPLOYMENT_NAME`
    - `AZURE_ENDPOINT`
    - `AZURE_API_KEY`
2. Run:

```bash
cd azure-agentserver-samples/azure-agentserver-langchain4j-sample
./run-sample.sh
```

### LangChain4j sample (Spring)

```bash
cd azure-agentserver-samples/azure-agentserver-langchain4j-spring-sample
./run-sample.sh
```

> [!IMPORTANT]
> `AZURE_API_KEY` is intended for local/dev sample execution. In Azure AI Foundry hosted deployments, you typically
> should **not** require end users to set API keys and should use **Managed Identity** (for example via
> `DefaultAzureCredential`) for authentication.

## Foundry hosted deployment configuration

For Azure AI Foundry hosted deployments, users typically do **not** need to define:

- `AZURE_API_KEY`
- `AZURE_ENDPOINT`
- `AZURE_DEPLOYMENT_NAME`

The expected production pattern is Managed Identity authentication.

### Typically required (platform-provided)

- `FOUNDRY_HOSTING_ENVIRONMENT`
- `FOUNDRY_PROJECT_ENDPOINT`

### Common platform context variables

- `FOUNDRY_AGENT_NAME`
- `FOUNDRY_AGENT_VERSION`
- `FOUNDRY_AGENT_SESSION_ID`

### Optional (scenario-dependent)

- `MODEL_DEPLOYMENT_NAME` (override default model deployment name)
- `AZURE_CLIENT_ID` (when using a user-assigned managed identity)

## Calling the service

### Non-streaming request

```bash
curl -X POST http://localhost:8088/responses \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
    "input": "Echo this back to me.",
    "model": "gpt-4o"
  }'
```

### Streaming request (SSE)

```bash
curl -X POST http://localhost:8088/responses \
  -H "Accept: text/event-stream" \
  -H "Content-Type: application/json" \
  -d '{
    "input": "Echo this back to me.",
    "model": "gpt-4o",
    "stream": true
  }' --no-buffer
```

## Using `azure-agentserver-api` as a library

Typical bootstrap patterns from samples:

- **Jersey runtime**
    - Build `ResponsesApi` with your `ResponseHandler`.
    - Start with `JerseyAgentServerAdaptorService.buildAgent(...)`.
- **Spring runtime**
    - Build a handler (for example LangChain4j-backed).
    - Start with `SpringAgentServerAdaptorService.run(...)`.

Minimal pattern:

```java
ResponsesApi api = ResponsesApi.builder()
    .responseHandler(new MyHandler())
    .build();
```

Your handler can support:

- `createResponse(...)` for non-streaming requests.
- `createAsync(...)` for streaming requests using `ResponseEventStream`.

In handler code, use `ResponseContext` to access:

- resolved input items (`getInputItemsAsync`)
- conversation history (`getHistoryAsync`)
- cancellation signals (`isCancelled`)
- platform metadata (`getIsolation`, `getRequestId`, `getSessionId`)

## Notes for consumers

- Default port in provided adapters/samples is `8088`.
- `CA_LOG_REQUESTS=true` enables request/response logging in adapters.
- Doc-focused examples are in `azure-agentserver-doc-samples` (sample1..sample10 patterns).
- Example framework integrations are in
  `azure-agentserver-samples/azure-agentserver-example-framework-integrations/azure-agentserver-agentframeworks`.

## Framework integration support boundary

`azure-agentserver-samples/azure-agentserver-example-framework-integrations/azure-agentserver-agentframeworks` (for
example LangChain4j integration) is provided as **sample/example integration code** and is **not an officially supported
API contract**. Build production code against the core API (`azure-agentserver-api`) and web adapters.
