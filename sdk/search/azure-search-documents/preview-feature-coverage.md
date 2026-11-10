# November preview / 26.12 deployment feature coverage

The contract baseline is
[Azure/azure-rest-api-specs#47300](https://github.com/Azure/azure-rest-api-specs/pull/47300),
commit `b41ba04c0219f6622b7f5214fc020f90bad339ae`, matching `tsp-location.yaml`.
Deployment work items and one-pagers are feature inventories, not substitutes for the pinned wire contract.

## Coverage matrix

Existing tests are not evidence that all deployed features have passed live validation.
The offline checks exercise Java request/response handling with fake HTTP responses.
The new live checks are opt-in and have not yet been run against the corrected backend.

| Feature | Existing coverage before this addition | Added service-behavior check in `PreviewFeatureLiveTests` | Remaining validation |
| --- | --- | --- | --- |
| GraphRAG Zero L1 / Topic Graph | `PreviewApiTests.topicGraphDiscriminatorsRoundTrip`; `FieldBuilderTests` covers `topicGraphProfile` | `topicGraphDiversifiesAndInterleavesWithPureVector`: diverse/balanced modes, single-topic pure-vector control, cross-topic results, mixed queries without duplicate IDs | Provision the controlled topic fixture; validate index/KS creation and server-side rejection of invalid mode-specific fields separately |
| EKG KS ingestion, ontology, cache and status | Basic cache round trip only; Fabric Ontology KS tests are a different feature | `graphIngestionReportsOntologyAndTriplesWithoutReturningCacheSecret`: completed extraction, ontology statistics, positive triple count, GET omits cache secret | Fresh ingestion, cache reuse/reprocessing, grounding correctness, ontology attach/update/detach and failure recovery still require provisioned blob/ontology/cache fixtures |
| EKG retrieval | `PreviewApiTests` covers graph request headers/options, response diagnostics and rejection body | `graphPlansAndExecutesAgainstExtractedEdges`; `graphKnowledgeBaseRetrievalUsesGraphSearchActivity` | Validate provenance hydration, authorization and additional graph-language cases with dedicated fixtures |
| Agentic Direct Search / KR models | `SearchUtilsTests` forwards agentic options; SSE tests deserialize agentic reasoning; Microsoft Retriever model round trip | `agenticSearchReturnsReasoningUsageAndDocuments`; `minimalAndMicrosoftRetrieverKnowledgeBasesRetrieve` | Backend deployment/routing must support both models; check timeout/budget termination and shared accounting separately |
| File KS LRO | Existing legacy upload/update tests require embedding fixtures; `PreviewApiTests` checks operation kind | `fileIngestionSupportsUploadReplacementAndRetainedOperationLookup`: sync upload, async replacement, same file ID, operation GET/list with page size 1, missing-operation 404 | Backend failure/retention scenarios require service-side fault/expiry fixtures; offline polling tests do not prove actual ingestion |
| Durable Operations framework | No separate customer-facing framework API | Covered through File KS operation lifecycle above | No SDK test for internal serverless teardown; framework durability/restart/expiry belongs to backend tests |
| OneLake indexed KS ACLs / OneLake security filtering | `FieldBuilderTests` maps `oneLakeParentFolders`; old OneLake citation deserialization is not ACL coverage | `oneLakePermissionsAllowOneIdentityAndExcludeAnother`: indexed OneLake source, permission schema, two valid identities, restricted sentinel included/excluded with elevated read disabled | Requires a real indexed OneLake fixture and fresh source-authorization tokens; inherited/moved-folder ACLs and revocation propagation remain backend scenarios |
| Random scoring | `PreviewApiTests` checks discriminator, omission of field/boost and classic-scoring compatibility | `randomScoringIsRepeatableForOneSeedAndRotatesForAnother`: identical seed ordering, changed signed 64-bit seed ordering, same document set | Seed validation/error boundaries are contract checks; statistical sampling quality belongs to backend tests |
| KB tools | MCP knowledge-source tests are not KB tool tests | `toolSelectionLimitsSourcesAndRequestDefaultsTakePrecedence`: persisted singular tool selection, source filter isolation, request overrides tool/KB limits, unknown-tool 400 | Multiple tools, invalid source membership, MCP catalog and schema discovery still need additional integration coverage |
| OpenAI-compatible / APIM models | No focused compatible-model test found in the previous test inventory | `compatibleKnowledgeBaseModelsRetrieveThroughBothProtocols`; `compatibleIngestionSkillsAndVectorizersProcessDocuments` | Provision chat and Responses gateways, KBs, embedding/vectorizer, skillset/indexer and ingested KS; APIM traces must verify exact downstream path/query/header behavior |
| GPT-6 via Responses | General extensible model-name handling is not proof of GPT-6 operation | `gpt6KnowledgeBaseProducesAResponse`: persisted GPT-6 model and nonempty retrieval evidence | Requires deployed GPT-6; confirm Responses selection in backend traces, not merely a successful model-name serialization |
| Query hint filter auto-configuration | `PreviewApiTests.automaticFilterHintsSerializeFacetConfiguration`; existing filter/boost retrieval tests do not exercise facet bootstrap | `automaticFilterHintsGroundRetrievalAndRejectExplicitValues`: stored auto-configuration, facet-backed retrieval restricted to a known category, explicit values + auto-configuration rejected | TTL refresh, max facet-count behavior, nonfacetable fields, permission/Purview restrictions and request-time override rejection remain additional cases |

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
  is not evidence of a service secret disclosure. The live GET check tests actual server omission.
- Query hint auto-configuration is present at this pin despite its private/public history.
  It is stored KS configuration, not a supported request-time `queryHintOverrides` setting.
- `toolName` is singular. MCP knowledge-source tool configuration is a different API.
- `reasoningTokens` replaces the unreleased Direct Search `rankingTokens` name.
- GPT-6 family names at this pin include `gpt-6-astra`, `gpt-6-sol` and `gpt-6-luna`, not a literal `gpt-6`.
  The live fixture checks one configured family member; it does not establish deployment of every model variant.
- LRO tests use the SDK's `SyncPoller` and `PollerFlux`, not a hand-written ingestion poller.

## Running offline checks

From the repository root, with a working JDK/Maven:

```powershell
$env:AZURE_TEST_MODE = 'PLAYBACK'
mvn -B -ntp -f sdk\search\azure-search-documents\pom.xml test `
  '-Dtest=PreviewApiTests,PreviewFeatureContractTests,FileIngestionPollingTests,PreviewFeatureLiveTests,GetDocumentOptionsTests'
```

The live class must be skipped without live credentials, even if
`SEARCH_RUN_PREVIEW_FEATURE_TESTS=true` is set while test mode remains PLAYBACK.
Check the test reports; a skipped live test is not a passed feature check.

### Local validation results

- The broader selection (`PreviewApiTests`, `PreviewFeatureContractTests`, `FileIngestionPollingTests`,
  `PreviewFeatureLiveTests`, `FieldBuilderTests`, `SearchUtilsTests`) produced 65 passes and 16 skips:
  14 live methods and two existing FieldBuilder skips. All 30 new offline cases passed.
- After the final assertion changes, `PreviewFeatureContractTests,PreviewFeatureLiveTests` produced
  23 passes and 14 live skips with PLAYBACK mode and the live opt-in flag explicitly enabled.
- Final Checkstyle, SpotBugs and scoped Spotless checks passed with zero violations/warnings.
  Test sources compiled for both Java 8 and Java 21.
- On November 9, after the operator's backend routing update and completed bootstrap, the existing LIVE retrieval
  suites passed: 99 passes, 14 existing skips and no failures/errors.
- The full package RECORD run and the final full PLAYBACK run each produced 1,157 passes, 132 skips and no
  failures/errors. Final PLAYBACK used the default test configuration with all `SEARCH_*` environment variables
  removed. Endpoint assertions account for recorded URL sanitization; the two strict pagination fixtures normalize
  environment-specific blob-container names. Configured-secret scans, Checkstyle and SpotBugs passed.
- The 14 new opt-in feature methods remain unrun and are included in those 132 RECORD/PLAYBACK skips.
  The other 118 skips exactly match the earlier DP11 recording run: 92 disabled cases and 26 existing live-only cases.
  Skips are test-execution decisions, not failed HTTP recordings.
- Validated recordings were published as `java/search/azure-search-documents_6950366571`, and `assets.json` was
  updated. Restoring that tag reproduced all 2,000 recording JSON files byte-for-byte against the validated manifest.
  Package recording success does not close the feature-specific validation gaps in the matrix.

## Running live checks safely

Do not execute until the target service is explicitly reserved, the deployment/routing is verified, and the
operator approves the run. These tests create resources and can incur indexing/model costs.
Keep the recording/test host awake. Run this class alone, not alongside older suites whose cleanup deletes
all service knowledge sources/bases.

```powershell
$env:AZURE_TEST_MODE = 'LIVE'
$env:SEARCH_RUN_PREVIEW_FEATURE_TESTS = 'true'
mvn -B -ntp -f sdk\search\azure-search-documents\pom.xml test '-Dtest=PreviewFeatureLiveTests'
```

The class uses `@LiveOnly`: it intentionally does not create new recording assets in RECORD mode.
Existing record/playback tests use a separate RECORD/PLAYBACK workflow; they do not execute this live-only class.

Random scoring, minimal/Microsoft Retriever KBs, KB tools and query hints create fresh small indexes/sources/bases.
Only successfully created resources are tracked for cleanup; no list-all/delete-all cleanup is used.
The file test creates its own KS and files and requires the configured embedding deployment.
External fixtures below are read/queried, not updated or deleted.

Missing fixture settings cause explicit assumption skips. Once settings are supplied, HTTP/model/assertion failures
are failures, not reasons to skip. A run with prerequisite skips is not complete deployment validation.

### Shared prerequisites

Use the existing `SEARCH_SERVICE_ENDPOINT`, Azure test credentials and model/identity settings from `SearchTestBase`.
File ingestion additionally requires `SEARCH_OPENAI_EMBEDDING_DEPLOYMENT_NAME`.
Query-hint planning requires a working `SEARCH_OPENAI_DEPLOYMENT_NAME` / `SEARCH_OPENAI_MODEL_NAME`, and the configured
user-assigned identity must have the required model access.

### External fixture settings

| Scenario | Required settings | Fixture contract |
| --- | --- | --- |
| Topic Graph | `SEARCH_PREVIEW_TOPIC_INDEX`, `SEARCH_PREVIEW_TOPIC_QUERY_JSON`, `SEARCH_PREVIEW_TOPIC_FIELD` | JSON is a vector query with `kind`, `vector`, `fields`, `k`; index has Topic Graph configured, retrievable `id` and scalar topic label. Top 10 pure-vector neighbors share one topic; diverse/balanced retrieval spans at least two. The vectors/data must deliberately exercise these contrasts. |
| Graph execution | `SEARCH_PREVIEW_GRAPH_INDEX`, `SEARCH_PREVIEW_GRAPH_GQL`, `SEARCH_PREVIEW_GRAPH_RESULT_FIELD`, `SEARCH_PREVIEW_GRAPH_RESULT_VALUE` | Extracted edge-list graph; GQL supports plan and execution and returns a known string sentinel in the named column. |
| Graph KB | `SEARCH_PREVIEW_GRAPH_KB`, `SEARCH_PREVIEW_GRAPH_KB_QUERY` | Graph-grounded KB with a deterministic question that retrieves evidence and produces `graphSearch` activity. |
| Graph ingestion/status | `SEARCH_PREVIEW_GRAPH_SOURCE` | Azure Blob KS with a named ontology and cache; completed synchronization has extracted triples. Do not place connection strings in this document. |
| Agentic search | `SEARCH_PREVIEW_AGENTIC_INDEX`, `SEARCH_PREVIEW_AGENTIC_QUERY` | Populated index and enabled agentic backend; known question exercises both planning and semantic reasoning. |
| OneLake ACLs | `SEARCH_PREVIEW_ONELAKE_INDEX`, `SEARCH_PREVIEW_ONELAKE_SOURCE`, `SEARCH_PREVIEW_ONELAKE_RESTRICTED_ID`, `SEARCH_PREVIEW_ONELAKE_ALLOWED_TOKEN`, `SEARCH_PREVIEW_ONELAKE_DENIED_TOKEN` | Index uses `id` as key and `oneLakeParentFolders`. The same indexed sentinel is visible to one valid source identity and invisible to another. Supply fresh tokens privately; never commit tokens or put them in screenshots/logs. |
| Compatible KBs | `SEARCH_PREVIEW_COMPATIBLE_CHAT_KB`, `SEARCH_PREVIEW_COMPATIBLE_RESPONSES_KB`, `SEARCH_PREVIEW_COMPATIBLE_QUERY` | Separate preconfigured KBs with `openAICompatible` models whose URI paths end in `/chat/completions` and `/responses`, respectively; known question retrieves evidence. |
| Compatible ingestion/vectorization | `SEARCH_PREVIEW_COMPATIBLE_SOURCE`, `SEARCH_PREVIEW_COMPATIBLE_INDEX`, `SEARCH_PREVIEW_COMPATIBLE_VECTOR_FIELD`, `SEARCH_PREVIEW_COMPATIBLE_SKILLSET`, `SEARCH_PREVIEW_COMPATIBLE_INDEXER` | Blob KS using compatible chat, successfully ingested documents, index with compatible query vectorizer, a URL-based ChatCompletionSkill, successful indexer output and searchable vectors. |
| GPT-6 | `SEARCH_PREVIEW_GPT6_KB`, `SEARCH_PREVIEW_GPT6_QUERY` | Preconfigured Azure OpenAI KB using `gpt-6-astra`, `gpt-6-sol` or `gpt-6-luna`, with known question and supporting documents; routing/deployment must support Responses. |

## Release gate

Before claiming all features are tested, record per-feature results, prerequisite skips and backend failures.
Collect request/activity/APIM IDs for failures. Verify routes and actual backend behavior before updating recording
assets; fake-response tests, pre-existing CRUD tests and successful compilation are not substitutes.

Testing approach follows the
[Java SDK testing guidance](https://azure.github.io/azure-sdk/java_implementation.html#java-testing)
and the repository's [unit testing](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/unit-testing.md)
and [live testing](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/live-testing.md) documentation.
