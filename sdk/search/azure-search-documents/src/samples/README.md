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
    <version>12.1.0</version>
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

### Knowledge bases and knowledge sources

These samples target the `2026-10-01` GA service API. Knowledge bases, retrieval, and the earlier knowledge source
types already existed in `12.0.2`; the table below identifies the additional `12.1.0` scenarios.

- [Knowledge base CRUD](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeBaseCrudExample.java)
- [Knowledge source CRUD and source-specific configuration](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeSourceCrudExample.java)
- [Knowledge base configuration and request overrides](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeBaseConfigurationExample.java)
- [Retrieval, conversation history, and source-specific parameters](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeRetrievalExample.java)
- [Response activity and citation URLs](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeRetrievalResponseExample.java)
- [Typed synchronous and asynchronous streaming](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeBaseRetrievalStreamExample.java)
- [File upload, update, capacity, listing, and deletion](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeSourceFileExample.java)

### New GA feature coverage

| Feature added since `12.0.2` | Sample and intended flow |
| --- | --- |
| Similar-document queries (`moreLikeThis`) | [SearchOptionsExample](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/SearchOptionsExample.java) queries a document key without search text and limits search fields to top-level text fields. |
| Permission-aware queries and document lookup | [DocumentPermissionsExample](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/DocumentPermissionsExample.java) passes the end user's query-source token to search, lookup, and retrieval with elevated reads disabled. |
| Permission schema, SharePoint registration, ACL ingestion, federated identity, and ACL refresh/resynchronization | [DocumentPermissionsExample](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/DocumentPermissionsExample.java) includes provisioning models and an explicit creation method; supply deployment-specific mappings and roles. Pass `resync` with `SEARCH_INDEXER_NAME` to resynchronize permissions. Created resources are not automatically deleted. |
| Resource pagination, name-prefix filtering, selected properties, aliases, and opaque continuation tokens | [ListIndexesExample](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/indexes/ListIndexesExample.java), [ListIndexersExample](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/indexes/ListIndexersExample.java), and [SynonymMapsCreateExample](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/SynonymMapsCreateExample.java). Knowledge resource CRUD samples also use pageable listings. |
| Index statistics summaries and service/indexer runtime | [IndexAndServiceStatisticsExample](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/IndexAndServiceStatisticsExample.java) and [ListIndexersExample](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/indexes/ListIndexersExample.java) report usage, remaining runtime, and reporting periods. |
| File knowledge sources, file metadata and prefixes, multipart upload/update, deletion, and capacity | [KnowledgeSourceFileExample](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeSourceFileExample.java) checks capacity before uploading and deletes the files and source afterward. |
| Indexed SQL content/embedding mappings and indexed SharePoint container selection | Run [KnowledgeSourceCrudExample](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeSourceCrudExample.java) with `sql` or `sharepoint`; prerequisites are listed in the class JavaDoc. |
| Knowledge ingestion permissions and public/private network configuration | [KnowledgeSourceCrudExample](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeSourceCrudExample.java) configures ingestion, inspects generated Blob resources, and demonstrates permission ingestion for SharePoint. Private connectivity must already be configured. |
| Web language, market, result count, and freshness | Run [KnowledgeSourceCrudExample](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeSourceCrudExample.java) with `web`. |
| Low/medium reasoning, answer/extractive output modes, instructions, CORS, and request precedence | [KnowledgeBaseConfigurationExample](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeBaseConfigurationExample.java) configures defaults and overrides them per request. |
| Typed asynchronous knowledge resource updates | Run [KnowledgeBaseCrudExample](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeBaseCrudExample.java) with `async`; the source-specific [KnowledgeSourceCrudExample](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeSourceCrudExample.java) flows inspect typed asynchronous update responses. |
| Conversation messages, role/content constructors, query planning, and per-source result/error controls | [KnowledgeRetrievalExample](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeRetrievalExample.java) uses an existing populated knowledge base. Optionally set `SEARCH_IMAGE_URL` for an image-capable model. Pass `file`, `sql`, or `sharepoint` with `KNOWLEDGE_SOURCE_NAME` to use the corresponding retrieval parameters. |
| Typed source/model activities, timestamps, warnings, model/deployment metadata, and citation URLs | [KnowledgeRetrievalResponseExample](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeRetrievalResponseExample.java) reads responses without modifying existing resources. Citation URLs are Search-owned and require appropriate Search authorization. |
| Retrieval, activity, answer, reference, error, and completion stream events | [KnowledgeBaseRetrievalStreamExample](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/KnowledgeBaseRetrievalStreamExample.java) handles typed events and disposes synchronous streams. Pass `sync` for the synchronous flow. |
| Content understanding model/deployment and fixed-size/semantic chunking, including tokens | Run [CreateSkillsetExample](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/indexes/CreateSkillsetExample.java) with `content-understanding`; its factory also demonstrates fixed-size character chunks. |
| Additional Azure OpenAI model names | Knowledge base configuration accepts `SEARCH_OPENAI_MODEL_NAME`, including `gpt-5.4`, `gpt-5.5`, `gpt-5.6-sol`, `gpt-5.6-terra`, and `gpt-5.6-luna`. The matching supported deployment must exist. |

Knowledge source ingestion is asynchronous. The source/file CRUD samples demonstrate resource management and clean
up their resources; they do not assume uploaded content is immediately searchable. Run retrieval examples against an
existing source whose ingestion has completed. The caller needs the appropriate Search management/data roles;
the Search service identity separately needs permission to read source data and invoke configured model deployments.

The parameterless `SearchResourceEncryptionKey` constructor is also new, but the Java model does not expose the
service-level-key selector referenced by that constructor's JavaDoc, or setters for its key name and vault URL.
It is therefore not demonstrated as a working service-level encryption flow. For customer-managed keys, continue
to use the constructor accepting a key name and Key Vault URL with an authorized identity.

The sample configurations and response-handling helpers are covered by offline tests in
[SearchGaSamplesTests](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/test/java/com/azure/search/documents/SearchGaSamplesTests.java). All samples are compiled
by the package build; provisioning and ingestion require a live, suitably configured Azure service.

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
