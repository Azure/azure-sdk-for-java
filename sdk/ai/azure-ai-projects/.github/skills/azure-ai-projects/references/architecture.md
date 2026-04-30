# azure-ai-projects — Architecture Reference

## Source Layout

```
sdk/ai/azure-ai-projects/
├── tsp-location.yaml                          # TypeSpec spec reference
├── customizations/src/main/java/
│   └── ProjectsCustomizations.java            # Post-gen AST customizations (mostly inactive)
├── src/main/java/com/azure/ai/projects/
│   ├── AIProjectClientBuilder.java            # Generated — multi-service builder
│   ├── AIProjectsServiceVersion.java          # Generated — service version enum
│   ├── ConnectionsClient.java                 # Generated
│   ├── DatasetsClient.java                    # Generated
│   ├── DeploymentsClient.java                 # Generated
│   ├── EvaluationRulesClient.java             # Generated
│   ├── EvaluationTaxonomiesClient.java        # Generated
│   ├── EvaluatorsClient.java                  # Generated
│   ├── IndexesClient.java                     # Generated
│   ├── InsightsClient.java                    # Generated
│   ├── RedTeamsClient.java                    # Generated
│   ├── SchedulesClient.java                   # Generated
│   ├── SkillsClient.java                      # Generated
│   ├── models/                                # ~113 generated model classes
│   └── implementation/
│       ├── AIProjectClientImpl.java           # Generated — HTTP operations
│       ├── TokenUtils.java                    # HAND-WRITTEN — token auth bridge
│       └── http/
│           ├── HttpClientHelper.java          # HAND-WRITTEN — HTTP pipeline adapter
│           └── AzureHttpResponseAdapter.java  # HAND-WRITTEN — response adapter
├── src/samples/java/                          # ~14 sample files
└── src/test/java/                             # ~13 test files
```

## Client Inventory

| Sync Client | Async Client | Purpose |
|---|---|---|
| `ConnectionsClient` | `ConnectionsAsyncClient` | Workspace connection management |
| `DatasetsClient` | `DatasetsAsyncClient` | Dataset CRUD |
| `DeploymentsClient` | `DeploymentsAsyncClient` | Model deployment management |
| `EvaluationRulesClient` | `EvaluationRulesAsyncClient` | Evaluation rule configuration |
| `EvaluationTaxonomiesClient` | `EvaluationTaxonomiesAsyncClient` | Evaluation taxonomy management |
| `EvaluatorsClient` | `EvaluatorsAsyncClient` | Evaluator CRUD and runs |
| `IndexesClient` | `IndexesAsyncClient` | Index management |
| `InsightsClient` | `InsightsAsyncClient` | Insights and metrics |
| `RedTeamsClient` | `RedTeamsAsyncClient` | Red team evaluation |
| `SchedulesClient` | `SchedulesAsyncClient` | Schedule management |
| `SkillsClient` | `SkillsAsyncClient` | Skill CRUD |

All clients are fully generated. No hand-written convenience wrappers mixed in.

## Bridge Classes (Hand-Written)

These do NOT have the generated header. They survive codegen but may need updates if SDK contracts change.

| Class | Purpose | Update trigger |
|---|---|---|
| `TokenUtils` | Creates a lazy `BearerTokenSupplier` from Azure `TokenCredential` for openai-java auth | Auth mechanism changes in either SDK |
| `HttpClientHelper` | Adapts Azure `HttpPipeline` to openai-java `HttpClient` interface. Handles sync/async request/response translation, maps Azure exceptions to openai-java exceptions. | HTTP contract changes in openai-java or azure-core |
| `AzureHttpResponseAdapter` | Exposes Azure `HttpResponse` as openai-java `HttpResponse`. Converts header formats. | Response contract changes in openai-java |

**Key risk:** These bridge classes implement openai-java interfaces, so changes to openai-java's HTTP contracts (e.g., new methods on `HttpClient` or `HttpResponse`) will cause compile failures.

## Module Descriptor (module-info.java)

Generated. Notable transitive dependencies:

```java
requires transitive com.azure.core;
requires com.azure.storage.blob;
requires transitive openai.java.core;
requires transitive openai.java.client.okhttp;
requires com.azure.ai.agents;

exports com.azure.ai.projects;
exports com.azure.ai.projects.models;
opens com.azure.ai.projects.models to com.azure.core;
opens com.azure.ai.projects.implementation.models to com.azure.core;
```

**Cross-package dependency:** This module requires `com.azure.ai.agents`. Changes to the agents module's exports can affect this package.

## Key Dependencies

| Dependency | Version | Notes |
|---|---|---|
| `com.openai:openai-java` | 4.14.0 | External; enforcer bans other versions |
| `com.azure:azure-core` | 1.58.0-beta.1 | Core library |
| `com.azure:azure-ai-agents` | 2.1.0 | Sibling package dependency |
| `com.azure:azure-storage-blob` | 12.33.3 | File storage operations |

## Relationship to azure-ai-agents

This package depends on `azure-ai-agents` at both the Maven and JPMS level. The implications:

- **Build order matters:** Always build agents before projects
- **Model sharing:** Some shared types come from the agents package
- **Bridge class reuse:** `OpenAIJsonHelper` lives in agents — projects uses it transitively
- **Version lockstep:** Both packages share the same version (2.1.0) and should be released together
