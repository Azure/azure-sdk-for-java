# November preview / 26.12 deployment feature coverage

The contract baseline is
[Azure/azure-rest-api-specs#47300](https://github.com/Azure/azure-rest-api-specs/pull/47300),
commit `b41ba04c0219f6622b7f5214fc020f90bad339ae`, matching `tsp-location.yaml`.
Deployment work items and one-pagers are feature inventories, not substitutes for the pinned wire contract.

## Coverage matrix

Existing tests are not evidence that all deployed features have passed live validation.
The offline checks exercise Java request/response handling with fake HTTP responses.
The 14 supplemental live-test methods were removed because they had not been exercised against a real service.
Their removal does not close the service-validation gaps below.

| Feature | Retained SDK coverage | Remaining service validation |
| --- | --- | --- |
| GraphRAG Zero L1 / Topic Graph | `PreviewApiTests` round trips discriminators; `FieldBuilderTests` covers `topicGraphProfile`; `PreviewFeatureContractTests` checks query modes, allowed fields and stored KS defaults | Controlled-corpus diversity/interleaving, index/KS configuration and rejection of invalid mode-specific fields |
| EKG KS ingestion, ontology, cache and status | `PreviewFeatureContractTests` checks graph extraction/cache requests and ontology/triple statistics in status responses | Real extraction, cache reuse/reprocessing, grounding, secret omission on GET, ontology lifecycle and failure recovery |
| EKG retrieval | `PreviewApiTests` checks graph headers/options, diagnostics and rejection bodies; `PreviewFeatureContractTests` checks graph overrides and mixed activity types | Graph execution, KB graph retrieval, provenance hydration, authorization and graph-language cases |
| Agentic Direct Search / KR models | `SearchUtilsTests` forwards agentic options; SSE tests deserialize reasoning; `PreviewFeatureContractTests` checks renamed usage fields and Microsoft Retriever requests | Real reasoning/retrieval, model routing, timeout/budget termination and shared accounting |
| File KS LRO | Existing legacy upload/update tests; `FileIngestionPollingTests` checks sync/async polling, errors, operation GET/list and continuation | Real ingestion/replacement, retained-operation lookup, backend failure and expiry behavior |
| Durable Operations framework | Customer-facing file-operation polling coverage above | Internal serverless teardown, durability/restart and expiry belong to backend tests |
| OneLake indexed KS ACLs / OneLake security filtering | `FieldBuilderTests` maps `oneLakeParentFolders`; `PreviewFeatureContractTests` checks security metadata and data-source serialization | Two-identity visibility checks, inherited/moved-folder ACLs and revocation propagation |
| Random scoring | `PreviewApiTests` checks discriminator and classic-scoring compatibility; `PreviewFeatureContractTests` checks profile and seed forwarding | Same-seed repeatability, changed-seed ordering, seed rejection boundaries and statistical quality |
| KB tools | `PreviewFeatureContractTests` checks tool configuration and singular request-time selection | Source isolation, request/default precedence, invalid tool/source rejection and MCP discovery |
| OpenAI-compatible / APIM models | `PreviewFeatureContractTests` checks KB authentication, ingestion models, vectorizers and exact skill URI forwarding | Real Chat Completions/Responses retrieval, ingestion/vectorization and downstream APIM path/query/header behavior |
| GPT-6 via Responses | Model-name handling in compatible-model contract cases | Real deployed-model retrieval and backend Responses routing |
| Query hint filter auto-configuration | `PreviewApiTests` and `PreviewFeatureContractTests` check facet-backed configuration serialization | Facet bootstrap, filtering, TTL refresh, limits and rejection/permission boundaries |

`PreviewFeatureContractTests` adds 23 offline wire cases alongside the existing tests.
`FileIngestionPollingTests` adds seven offline polling/operation cases.
Its fake responses establish SDK behavior only, not backend feature availability or ranking quality.

## Contract details that affect the test design

- The pinned public spelling is `topicGraph`, not the older graph-community naming.
  `none`, `diverse` and `balanced` have different allowed fields.
- The 1,000 candidate-pool change is a server default/documentation change, not a new Java constant or a documented
  maximum. Do not add a test that invents an upper bound of 1,000.
- The pinned Azure Blob graph settings use `azureBlobParameters.graphExtraction`.
  Do not invent an `ingestionParameter.graphParameter` JSON field from the deployment title.
- A secret/write-only TypeSpec annotation describes the service's read/write contract.
  The Java request model must still send a supplied cache connection string; deserializing synthetic request JSON
  is not evidence of a service secret disclosure. Actual server omission still requires a live GET check.
- Query hint auto-configuration is present at this pin despite its private/public history.
  It is stored KS configuration, not a supported request-time `queryHintOverrides` setting.
- `toolName` is singular. MCP knowledge-source tool configuration is a different API.
- `reasoningTokens` replaces the unreleased Direct Search `rankingTokens` name.
- GPT-6 family names at this pin include `gpt-6-astra`, `gpt-6-sol` and `gpt-6-luna`, not a literal `gpt-6`.
  Model-name serialization does not establish deployment or successful retrieval for any model variant.
- LRO tests use the SDK's `SyncPoller` and `PollerFlux`, not a hand-written ingestion poller.

## Running offline checks

From the repository root, with a working JDK/Maven:

```powershell
$env:AZURE_TEST_MODE = 'PLAYBACK'
mvn -B -ntp -f sdk\search\azure-search-documents\pom.xml test `
  '-Dtest=PreviewApiTests,PreviewFeatureContractTests,FileIngestionPollingTests,GetDocumentOptionsTests'
```

These selected tests use fake HTTP responses and do not require live feature fixtures.

### Local validation results

- After removing the supplemental live tests on November 10, a clean build and the offline selection above
  produced 62 passes with no skips, failures or errors; Checkstyle reported zero violations.
- All 23 cases in `PreviewFeatureContractTests` and seven cases in `FileIngestionPollingTests` passed.
- Final Checkstyle, SpotBugs and scoped Spotless checks passed with zero violations/warnings.
  Test sources compiled for both Java 8 and Java 21.
- On November 9, after the operator's backend routing update and completed bootstrap, the existing LIVE retrieval
  suites passed: 99 passes, 14 existing skips and no failures/errors.
- Before removal of the supplemental live tests, the full package RECORD run and the final full PLAYBACK run
  each produced 1,157 passes, 132 skips and no failures/errors.
  Final PLAYBACK used the default test configuration with all `SEARCH_*` environment variables
  removed. Endpoint assertions account for recorded URL sanitization; the two strict pagination fixtures normalize
  environment-specific blob-container names. Configured-secret scans, Checkstyle and SpotBugs passed.
- The 14 removed supplemental live methods were unrun and accounted for 14 of those historical skips.
  The other 118 skips exactly match the earlier DP11 recording run: 92 disabled cases and 26 existing live-only cases.
  Skips are test-execution decisions, not failed HTTP recordings.
- Validated recordings were published as `java/search/azure-search-documents_6950366571`, and `assets.json` was
  updated. Restoring that tag reproduced all 2,000 recording JSON files byte-for-byte against the validated manifest.
  Package recording success does not close the feature-specific validation gaps in the matrix.

## Release gate

Before claiming all features are tested, record per-feature results, prerequisite skips and backend failures.
Collect request/activity/APIM IDs for failures. Verify routes and actual backend behavior before updating recording
assets; fake-response tests, pre-existing CRUD tests and successful compilation are not substitutes.

Testing approach follows the
[Java SDK testing guidance](https://azure.github.io/azure-sdk/java_implementation.html#java-testing)
and the repository's [unit testing](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/unit-testing.md)
and [live testing](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/live-testing.md) documentation.
