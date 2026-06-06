# azure-ai-agents — Architecture Reference

## Source Layout

```
sdk/ai/azure-ai-agents/
├── tsp-location.yaml                          # TypeSpec spec reference
├── customizations/src/main/java/
│   └── AgentsCustomizations.java              # Post-gen AST customizations
├── src/main/java/com/azure/ai/agents/
│   ├── AgentsClient.java                      # Generated — main client
│   ├── AgentsAsyncClient.java                 # Generated — async main client
│   ├── AgentSessionFilesClient.java           # Generated
│   ├── MemoryStoresClient.java                # Generated
│   ├── ResponsesClient.java                   # Generated
│   ├── ToolboxesClient.java                   # Generated
│   ├── AgentsClientBuilder.java               # Generated — multi-service builder
│   ├── AgentsServiceVersion.java              # Generated — service version enum
│   ├── models/                                # ~185 generated model classes
│   └── implementation/
│       ├── AgentsClientImpl.java              # Generated — HTTP operations
│       ├── OpenAIJsonHelper.java              # HAND-WRITTEN — openai-java bridge
│       ├── AgentsServicePollUtils.java        # HAND-WRITTEN — polling helpers
│       ├── StreamingUtils.java                # HAND-WRITTEN — reactive streaming
│       ├── TokenUtils.java                    # HAND-WRITTEN — token auth bridge
│       ├── HttpClientHelper.java              # HAND-WRITTEN — HTTP pipeline adapter
│       ├── AzureHttpResponseAdapter.java      # HAND-WRITTEN — response adapter
│       └── JsonMergePatchHelper.java          # HAND-WRITTEN — JSON patch utils
├── src/samples/java/                          # ~85 sample files
└── src/test/java/                             # ~25 test files
```

## Client Inventory

| Sync Client | Async Client | Purpose |
|---|---|---|
| `AgentsClient` | `AgentsAsyncClient` | Agent CRUD, runs, threads, messages, sessions, conversations |
| `AgentSessionFilesClient` | `AgentSessionFilesAsyncClient` | File upload/download within sessions |
| `MemoryStoresClient` | `MemoryStoresAsyncClient` | Memory store CRUD and search |
| `ResponsesClient` | `ResponsesAsyncClient` | Response creation and management |
| `ToolboxesClient` | `ToolboxesAsyncClient` | Toolbox version CRUD |

All clients are generated. The builder `AgentsClientBuilder` constructs all of them.

## Bridge Classes (Hand-Written)

These do NOT have the generated header. They survive codegen but may need updates if generated types change.

| Class | Purpose | Update trigger |
|---|---|---|
| `OpenAIJsonHelper` | Serialization bridge between openai-java (Jackson/Kotlin) and Azure SDK (azure-json). Provides `toBinaryData()` / `fromBinaryData()` for safe interop. | New openai-java types used in dedup |
| `AgentsServicePollUtils` | Adds `Foundry-Features` header to polling requests. Remaps terminal poll states (`completed`, `superseded`). | Polling behavior changes or new Foundry features |
| `StreamingUtils` | Reactive streaming utilities for server-sent events | Streaming format changes |
| `TokenUtils` | Bridges `TokenCredential` to openai-java token supplier | Auth mechanism changes |
| `HttpClientHelper` | Adapts Azure `HttpPipeline` to openai-java `HttpClient` | openai-java HTTP contract changes |
| `AzureHttpResponseAdapter` | Exposes Azure `HttpResponse` as openai-java `HttpResponse` | openai-java response contract changes |
| `JsonMergePatchHelper` | JSON merge-patch utilities for PATCH operations | New PATCH-able models |

## Module Descriptor (module-info.java)

Generated. Key transitive exports:

```java
requires transitive com.azure.core;
requires transitive openai.java.client.okhttp;
requires transitive openai.java.core;

exports com.azure.ai.agents;
exports com.azure.ai.agents.models;
opens com.azure.ai.agents.models to com.azure.core;
opens com.azure.ai.agents.implementation.models to com.azure.core;
```

## Key Dependencies

| Dependency | Version | Notes |
|---|---|---|
| `com.openai:openai-java` | 4.14.0 | External; enforcer bans other versions |
| `com.azure:azure-core` | 1.58.0-beta.1 | Core library |
| `com.azure:azure-core-http-netty` | 1.16.3 | Default HTTP client |

## Shared Skills Pipeline

After regeneration, apply these shared skills from `sdk/ai/.github/skills/` in order:

1. **`tsp-naming-collision`** — Fix `*Request1` parameter suffixes
2. **`api-diff`** — Identify new API additions, bucket by feature area
3. **`dup-classes`** — Check new models against openai-java for duplicates
4. **`dedup-openai`** — Suppress actionable duplicates via `@@alternateType`
5. **`union-type-wrappers`** — Add typed getters/setters for `BinaryData` union properties
6. **`codegen-survival-rules`** — Ensure manual edits survive next regen
7. **`tsp-type-override`** — Override TypeSpec types with Java-native types if needed
