# RevAPI failure report

Baseline command:

```powershell
mvn clean install
```

Result: the build failed in `org.revapi:revapi-maven-plugin:0.15.1:check` while comparing
`com.azure:azure-ai-agents:2.1.0` against `2.2.0-beta.1`. Tests and coverage completed before the
RevAPI check.

## Beta-annotated surface

The 2.1.0 source jar contains no `@Beta` annotations, including on removed classes. The RevAPI
findings below are on types that are `@Beta(warningText = "Preview API. AgentsOptimization=V2Preview")`
in the current source, but the corresponding 2.1.0 API was not beta-annotated.

| Current beta type | RevAPI changes |
| --- | --- |
| `OptimizationCandidate` | Removed `getConfig()`, `getPassRate()`, `getTaskScores()`, and `isParetoOptimal()`. |
| `OptimizationJob` | Public no-arg constructor visibility increased; removed `getDataset()`. |
| `OptimizationJobInputs` | Constructor changed from `(AgentIdentifier, DatasetRef)` to `(OptimizationAgentIdentifier, OptimizationDatasetInput, List<OptimizationEvaluatorRef>)`; `getAgent()` return type changed; `getEvaluators()` list element type changed; removed train/validation dataset reference getters and affected setters. |
| `OptimizationJobProgress` | Removed `getCurrentIteration()`. |
| `OptimizationJobResult` | `getBaseline()` and `getBest()` now return `String` instead of `OptimizationCandidate`; removed `getOptions()`, `getWarnings()`, and `isAllTargetAttributesFailed()`. |
| `OptimizationOptions` | Removed `getMaxIterations()` and `setMaxIterations(Integer)`. |

## Other non-beta API changes

These RevAPI failures are outside the beta-annotated surface:

| Area | RevAPI changes |
| --- | --- |
| `AgentsClient` / `AgentsAsyncClient` | `deleteSession`, `downloadAgentCode`, `getSession`, and `listSessions` signatures changed by removing the `AgentDefinitionOptInKeys` parameter. |
| Removed models | `AgentIdentifier`, `AgentProtocol`, `CandidateDeployConfig`, `CandidateFileInfo`, `CandidateMetadata`, `CandidateResults`, `DatasetInfo`, `DatasetRef`, `OptimizationAgentDefinition`, `OptimizationTaskResult`, `PromoteCandidateInput`, and `PromoteCandidateResult` were removed. None were `@Beta` in 2.1.0. |
| `ProtocolVersionRecord` | Constructor parameter and `getProtocol()` return type changed from `AgentProtocol` to `AgentEndpointProtocol`. |

## Least-verbose exception shape

The concise exception set is four grouped RevAPI rules:

1. `java.method.numberOfParametersChanged` for the affected `AgentsClient` / `AgentsAsyncClient` methods.
2. `java.class.removed` for the removed optimization/protocol model names.
3. A regex covering optimization model method removals, return-type changes, constructor parameter changes, and visibility increase.
4. A regex covering `ProtocolVersionRecord` parameter/return type changes.

Those exceptions are in `revapi.json` and appended from `pom.xml` so the inherited RevAPI config remains active.
