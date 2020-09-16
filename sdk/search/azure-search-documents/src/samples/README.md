---
page_type: sample
languages: java
products:
  - azure
  - azure-search-documents
urlFragment: search-samples
---

# Azure Cognitive Search Samples client library for Java
This document explains samples and how to use them.

## Key concepts
Key concepts are explained in detail [here][SDK_README_KEY_CONCEPTS].

# Samples Azure Cognitive Search APIs
This document describes how to use samples and what is done in each sample.

## Getting started
Getting started explained in detail [here][SDK_README_GETTING_STARTED].
 
### Adding the package to your project

Maven dependency for Azure Cognitive Search Client library. Add it to your project's pom file.

[//]: # {x-version-update-start;com.azure:azure-search;current}

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-search</artifactId>
    <version>1.0.0-preview.2</version>
</dependency>
```

[//]: # {x-version-update-end}

## How to run
These sample can be run in your IDE with default JDK.

## Examples
The following sections provide several code snippets covering some of the most common service tasks, including:

-   [Using autocomplete to expand a query from index contents](java/com/azure/search/documents/AutoCompleteExample.java)
-   [Creating a new index](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/indexes/CreateIndexExample.java)
-   [Create a new indexer](jhttps://github.com/Azure/azure-sdk-for-java/blob/master/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/indexes/CreateIndexerExample.java)
-   [Creating, listing and deleting data sources](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/indexes/DataSourceExample.java)
-   [Retrieving a document by key](java/com/azure/search/documents/GetSingleDocumentExample.java)
-   [How to handle HttpResponseException errors](java/com/azure/search/documents/HttpResponseExceptionExample.java)
-   [Using IndexClient configuration options](java/com/azure/search/documents/IndexClientConfigurationExample.java)
-   [Uploading, merging, and deleting documents in indexes](java/com/azure/search/documents/IndexContentManagementExample.java)
-   [Search for documents of unknown type](java/com/azure/search/documents/SearchForDynamicDocumentsExample.java)
-   [Using count, coverage, and facets](java/com/azure/search/documents/SearchOptionsExample.java)
-   [Using suggestions](java/com/azure/search/documents/SearchSuggestionExample.java)
-   [Searching for documents of known type](java/com/azure/search/documents/SearchAsyncWithFullyTypedDocumentsExample.java)
-   [Creating a synonym map for an index](java/com/azure/search/documents/SynonymMapsCreateExample.java)
-   [Creating skillsets](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/indexes/CreateSkillsetExample.java)
-   [Search queries options with async client](java/com/azure/search/documents/SearchOptionsAsyncExample.java)
-   [Search queries options with sync client](java/com/azure/search/documents/SearchOptionsExample.java)
-   [Retrieving Index and Service statistics](java/com/azure/search/documents/IndexAndServiceStatisticsExample.java)
-   [Setup datasource, indexer, index and skillset](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/indexes/LifecycleSetupExample.java)
-   [List indexers](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/indexes/ListIndexersExample.java)
-   [Add Synonym and custom skillset](java/com/azure/search/documents/RefineSearchCapabilitiesExample.java)
-   [Execute a search solution - run indexer and issue search queries](java/com/azure/search/documents/RunningSearchSolutionExample.java)
-   [Setting customer x-ms-client-request-id per API call](java/com/azure/search/documents/PerCallRequestIdExample.java)

## Troubleshooting
Troubleshooting steps can be found [here][SDK_README_TROUBLESHOOTING].

## Next steps
Start using Cognitive Search Java SDK in your solutions. Our SDK documentation could be found at [SDK Documentation][azsearch_docs]. 

## Contributing
This project welcomes contributions and suggestions. Find [more contributing][SDK_README_CONTRIBUTING] details here.

<!-- LINKS -->
[KEYS_SDK_README]: ../../README.md
[SDK_README_CONTRIBUTING]: ../../README.md#contributing
[SDK_README_GETTING_STARTED]: ../../README.md#getting-started
[SDK_README_TROUBLESHOOTING]: ../../README.md#troubleshooting
[SDK_README_KEY_CONCEPTS]: ../../README.md#key-concepts
[SDK_README_DEPENDENCY]: ../../README.md#adding-the-package-to-your-product
[azsearch_docs]: https://docs.microsoft.com/en-us/azure/search

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java/sdk/search/azure-search/samples/README.png)
