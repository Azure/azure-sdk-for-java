---
page_type: sample
languages: java
products:
  - azure
  - azure-cognitive-search
urlFragment: search-samples
---

# Azure AI Search Samples client library for Java
This document explains samples and how to use them.

## Key concepts
Key concepts are explained in detail [here][SDK_README_KEY_CONCEPTS].

# Samples Azure AI Search APIs
This document describes how to use samples and what is done in each sample.

## Getting started
Getting started explained in detail [here][SDK_README_GETTING_STARTED].

### Include the package

#### Include the BOM file

Please include the azure-sdk-bom to your project to take dependency on the General Availability (GA) version of the library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number.
To learn more about the BOM, see the [AZURE SDK BOM README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md).

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>azure-sdk-bom</artifactId>
            <version>{bom_version_to_target}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
and then include the direct dependency in the dependencies section without the version tag.

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-search-documents</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency

If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.


[//]: # ({x-version-update-start;com.azure:azure-search-documents;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-search-documents</artifactId>
    <version>12.3.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## How to run
These sample can be run in your IDE with default JDK.

## Examples
The following sections provide several code snippets covering some of the most common service tasks, including:

- [Using autocomplete to expand a query from index contents](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/AutoCompleteExample.java)
- [Creating a new index](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/indexes/CreateIndexExample.java)
- [Creating a new index using field builder](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/indexes/CreateIndexWithFieldBuilderExample.java)
- [Create a new indexer](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/indexes/CreateIndexerExample.java)
- [Creating, listing and deleting data sources](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/indexes/DataSourceExample.java)
- [Retrieving a document by key](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/GetSingleDocumentExample.java)
- [How to handle HttpResponseException errors](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/HttpResponseExceptionExample.java)
- [Using IndexClient configuration options](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/IndexClientConfigurationExample.java)
- [Uploading, merging, and deleting documents in indexes](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/IndexContentManagementExample.java)
- [Search for documents of unknown type](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/SearchForDynamicDocumentsExample.java)
- [Using count, coverage, and facets](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/SearchOptionsExample.java)
- [Using suggestions](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/SearchSuggestionExample.java)
- [Searching for documents of known type](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/SearchAsyncWithFullyTypedDocumentsExample.java)
- [Creating a synonym map for an index](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/SynonymMapsCreateExample.java)
- [Creating skillsets](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/indexes/CreateSkillsetExample.java)
- [Search queries options with async client](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/SearchOptionsAsyncExample.java)
- [Search queries options with sync client](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/SearchOptionsExample.java)
- [Retrieving Index and Service statistics](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/IndexAndServiceStatisticsExample.java)
- [Setup datasource, indexer, index and skillset](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/indexes/LifecycleSetupExample.java)
- [List indexes using server-driven pagination](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/indexes/ListIndexesExample.java)
- [List indexers](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/indexes/ListIndexersExample.java)
- [Add Synonym and custom skillset](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/RefineSearchCapabilitiesExample.java)
- [Execute a search solution - run indexer and issue search queries](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/RunningSearchSolutionExample.java)
- [Setting customer x-ms-client-request-id per API call](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/PerCallRequestIdExample.java)
- [Index vector fields and perform vector search](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/VectorSearchExample.java).
- [Rewrite Request URL to replace OData URL syntax with standard syntax](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/SearchRequestUrlRewriterPolicy.java)
- [Vector search using reduced embeddings](https://github.com/Azure/azure-sdk-for-java/blob/40261403b3a75aa56a3eeaf18c2ba0fd071c87a6/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/VectorSearchReducedEmbeddings.java)

### Knowledge Base & Knowledge Source Preview Samples
- [Knowledge Base configuration (preview)](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeBasePreviewConfigurationExample.java)
- [Streaming knowledge base retrieval with server-sent events (preview)](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeBaseRetrievalStreamExample.java)
- [Knowledge Base retrieval response parsing (preview)](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeRetrievalPreviewResponseExample.java)
- [Knowledge service statistics (preview)](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeServiceStatsPreviewExample.java)
- [Knowledge Source freshness and defaults (preview)](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeSourceFreshnessPreviewExample.java)
- [File Knowledge Source (preview)](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeSourceFilePreviewExample.java)
- [MCP Server Knowledge Source (preview)](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeSourceMcpServerPreviewExample.java)
- [Work IQ Knowledge Source (preview)](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeSourceWorkIqPreviewExample.java)
- [Fabric Ontology Knowledge Source (preview)](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeSourceFabricOntologyPreviewExample.java)
- [Fabric Data Agent Knowledge Source (preview)](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeSourceFabricDataAgentPreviewExample.java)

The preview retrieval response parsing sample uses an existing knowledge base configured through
`SEARCH_ENDPOINT`, `SEARCH_API_KEY`, and `KNOWLEDGE_BASE_NAME`. The knowledge base must have a model configured
for answer synthesis. This sample does not create, update, or delete knowledge bases or knowledge sources.

### November preview feature walkthroughs

The following classes have runnable `main` methods. Use this checkout's `12.3.0-beta.1` SDK, not a GA BOM version,
and set `SEARCH_ENDPOINT` and `SEARCH_API_KEY` for a service supporting `2026-11-01-preview`.
Run individual classes in your IDE with the sample/test dependency classpath. Each creates unique resources using
create-only operations and deletes only successfully created resources, even when the main operation fails.
Cleanup runs in reverse order and preserves the original exception. No sample enables paid features, changes
permissions, deploys models, or configures companion services.
The contract is the TypeSpec revision pinned by [`tsp-location.yaml`](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/tsp-location.yaml);
design drafts or HTTP walkthroughs are not authoritative when they differ from that revision.

| Class in `com.azure.search.documents` | Scenario | Additional prerequisites |
| --- | --- | --- |
| `FileIngestionLroPreviewExample` | Minimal text-file ingestion and in-place replacement using native `SyncPoller`, terminal results, operation GET/list, metadata and deletion. This is separate from the older synchronous upload/update file sample. | FileKS ingestion support. No model or storage deployment is needed for this text-only flow; it does not demonstrate KB retrieval. |
| `RandomScoringPreviewExample` | A real scoring profile, repeatable signed 64-bit seeds and scores, changed/unseeded ordering, paging, filtering and counts over 20 documents. | One free index slot; no model deployment. This is an API example, not a statistical-quality benchmark. |
| `KnowledgeBaseToolsPreviewExample` | Two sources, saved tools, filtered/source-restricted retrieval with singular `toolName`, then clearing tools to restore default retrieval. | Semantic search and two free index slots. Minimal/extractive retrieval needs no chat deployment. MCP discovery/call is a separate protocol, not the KB-tool selection shown here. |
| `QueryHintsAutoConfigurationPreviewExample` | Populate country facets, save automatic filter hints without manual field values, retrieve and inspect generated filters/references. | Native chat settings below, semantic search, one index slot. Optional `SEARCH_HINT_REFRESH=true` adds a new country and waits beyond the 60-second TTL. This demonstrates new-value availability, not cache-hit telemetry. |
| `TopicGraphPreviewExample` | Create/populate corpus and topic sidecar, link their profiles, compare none/diverse/balanced vector and text search, then use stored/runtime KS settings in KB retrieval. | Native embedding settings below and two index slots. Nine synthetic hotels and three representative vectors exercise the API, not production topic clustering or relevance quality. No private dataset or chat model is required. |
| `GraphIndexPreviewExample` | Create an edge-list graph and provenance index, upload assertions, inspect a physical plan, execute typed GQL and verify evidence IDs. | Graph preview support and two index slots. Optional `SEARCH_GRAPH_HYDRATE=true` requires existing hydration support/authorization. |
| `GraphExtractionCachePreviewExample` | Approved Blob fixture, inline ontology, extraction/cache/status, actual graph relationships and KB graph retrieval. | Approved synthetic Blob folder, native embedding/chat models, supported GPv2 cache and required storage/model permissions; see class JavaDoc. No source blobs are uploaded, changed or deleted. |
| `OpenAICompatibleRetrievalPreviewExample` | Compatible-provider/APIM KB retrieval with actual scoped document evidence; supported GPT-6 profiles use Responses. | Complete compatible chat POST URI, model profile and authentication below. GPT-6 requires a real Responses provider implementing stateless tool continuation; confirm the provider exchange in redacted gateway traces. |
| `OpenAICompatibleEmbeddingsPreviewExample` | Generate matching document vectors, upload them and compare precomputed-vector and provider-vectorized text queries. | Compatible embeddings POST URI, model, dimensions and authentication below. Optional `blob` mode requires a nonempty `AZURE_SEARCH_COMPATIBLE_BLOB_FOLDER` and `AZURE_SEARCH_COMPATIBLE_BLOB_SOURCE_APPROVED=true`, plus the connection/container settings in JavaDoc; it tests managed ingestion rather than just model serialization. |
| `GenAiPromptSkillPreviewExample` | Execute a GenAI prompt skill through Blob indexing and verify enriched hotel facts, rather than only serialize a skillset. | Approved compatible-Blob fixture settings, `SEARCH_SKILL_CHAT_URI` and `SEARCH_SKILL_CHAT_API_KEY`. The skill uses supported Azure/Foundry/APIM Chat Completions endpoints, not Responses or arbitrary compatible-provider header maps/audiences. |
| `NativeGpt6RetrievalPreviewExample` | Native Azure OpenAI GPT-6 KB retrieval over an owned corpus with actual evidence checks and optional reasoning telemetry. | Existing `SEARCH_OPENAI_ENDPOINT`/`SEARCH_OPENAI_API_KEY`, explicit `SEARCH_GPT6_DEPLOYMENT_NAME`, and supported `SEARCH_GPT6_MODEL_NAME` (default `gpt-6-sol`). No deployment is inferred or created; use provider traces to verify downstream Responses transport. |
| `OneLakeSecurityFilteringPreviewExample` | Create an indexed OneLake source, wait for synchronization and demonstrate opposite document-access results for two source users. | Dedicated populated fixture, two separately authenticated source users with different permissions, expected document URLs, and explicit approval; see below. No ACL, role, or source-data mutations are performed. |
| `AgenticSearchPreviewExample` | Direct agentic search with reasoning-token diagnostics and a minimal Microsoft Retriever 1 KB. Pass `direct` or `retriever` to run one scenario. | **Paid standard Knowledge Retrieval** must already be enabled: Microsoft Retriever 1 has no free tier (otherwise HTTP 402). No customer chat deployment is configured. |

Knowledge Retrieval Standard is a **feature billing plan**, not the Standard S1 service SKU: Basic or higher
is sufficient. An authorized operator can select **Settings > Premium features > Knowledge retrieval > Standard**
on the Search resource. Current API versions control this consent separately from semantic ranker billing;
changing local environment variables does not enable it. See
[billing plans and enabling billing](https://learn.microsoft.com/azure/search/agentic-retrieval-how-to-enable-disable#enable-agentic-retrieval-billing).

Native model settings used by the new walkthroughs are `AZURE_OPENAI_ENDPOINT` and, for key authentication,
`AZURE_OPENAI_API_KEY`; otherwise the Search service needs the model's existing managed-identity permissions.
Chat uses `AZURE_OPENAI_CHAT_DEPLOYMENT` and `AZURE_OPENAI_CHAT_MODEL`.
Embeddings use `AZURE_OPENAI_EMBEDDING_DEPLOYMENT` and `AZURE_OPENAI_EMBEDDING_MODEL`;
Topic Graph supports `text-embedding-3-small` or `text-embedding-3-large`, with optional matching
`AZURE_OPENAI_EMBEDDING_DIMENSIONS`. Topic Graph also requires `AZURE_OPENAI_API_KEY` for the caller's requests
to produce matching synthetic document vectors.

Compatible-provider settings use prefixes `AZURE_SEARCH_COMPATIBLE_CHAT_` or
`AZURE_SEARCH_COMPATIBLE_EMBEDDING_`: set `URI`, `MODEL`, and exactly one of `HEADERS_JSON`
(a nonempty secret JSON header map) or `MI_AUDIENCE` (the third-party Entra application's bare GUID).
Embedding configuration also requires `DIMENSIONS`. These URIs are **complete HTTPS POST URLs**, including
path and query, not native Azure OpenAI resource roots. They are forwarded without appending paths or API versions.
Do not derive a gateway authentication scheme or model routing from its hostname. A GPT-6 profile such as
`gpt-6-sol` requires `/responses`, not `/chat/completions`; enum/configuration presence is not live protocol proof.
Keep credentials local and never print provider configuration or credential-bearing URLs.

For extraction/cache, upload the class's synthetic hotel text into a new approved folder before running.
Set `AZURE_STORAGE_CONNECTION_STRING`, `AZURE_STORAGE_CONTAINER`, `AZURE_STORAGE_FOLDER`,
`AZURE_SEARCH_GRAPH_CACHE_CONNECTION_STRING` and `SEARCH_GRAPH_SOURCE_APPROVED=true` only after confirming the
folder contains no unrelated data. Native Blob soft delete and GPv2 Blob/Table cache access must already work.
Cache reuse needs execution telemetry; configuring a cache does not prove a cache hit.
The sample prints generated Search resource names; verify service-managed artifacts/cache retention separately.
Graph runtime parameters do not force planner routing. The extraction walkthrough uses graph-specific KB retrieval
instructions and a hotel/operator relationship question, and requires graph references plus generated GQL;
ordinary Blob-search references do not establish graph retrieval.

The existing Fabric/MCP/freshness examples also use unique create-only KB/KS resources and owned cleanup.
Fabric requires real `FABRIC_WORKSPACE_ID` plus `FABRIC_ONTOLOGY_ID` or `FABRIC_DATA_AGENT_ID` and authorization.
MCP requires `SEARCH_MCP_SERVER_URL` exposing the sample's `search-tool`/`lookup-tool`; a placeholder endpoint
is not runnable. Freshness uses an existing `SEARCH_INDEX_NAME`, `SEARCH_SEMANTIC_CONFIGURATION` (default
`default`), and Java-native `SEARCH_OPENAI_*` model settings. None of these prerequisites is provisioned by a sample.

For OneLake, set `SEARCH_ONELAKE_FIXTURE_APPROVED=true` only for an approved synthetic fixture. Other settings
share the `SEARCH_ONELAKE_` prefix: `WORKSPACE_ID`, `LAKEHOUSE_ID`, `TARGET_PATH`, `DOCUMENT_URL_PREFIX`,
`USER_A_DOCUMENT_URL`, `USER_B_DOCUMENT_URL`, `FIXTURE_QUERY`, `USER_A_TOKEN`, and `USER_B_TOKEN`.
The query must match both fixture documents. Each user must see their expected document and must not see the
other user's document. Supply raw short-lived source tokens using separate approved authentication sessions;
do not change a shared CLI login or substitute the Search admin key.

OneLake ACL enforcement, GenAI prompt execution, real cache hits and provider-side Responses continuation need
actual service evidence with their stated prerequisites. Compilation and mocked sample execution do not
establish that these flows run successfully on a particular Search service or provider.

## Troubleshooting
Troubleshooting steps can be found [here][SDK_README_TROUBLESHOOTING].

## Next steps
Start using Cognitive Search Java SDK in your solutions. Our SDK documentation could be found at [SDK Documentation][azsearch_docs]. 

## Contributing
This project welcomes contributions and suggestions. Find [more contributing][SDK_README_CONTRIBUTING] details here.

<!-- LINKS -->
[KEYS_SDK_README]: ../../README.md
[SDK_README_CONTRIBUTING]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/README.md#contributing
[SDK_README_GETTING_STARTED]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/README.md#getting-started
[SDK_README_TROUBLESHOOTING]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/README.md#troubleshooting
[SDK_README_KEY_CONCEPTS]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/README.md#key-concepts
[SDK_README_DEPENDENCY]: ../../README.md#adding-the-package-to-your-product
[azsearch_docs]: https://learn.microsoft.com/azure/search

