---
page_type: sample
languages:
  - java
products:
  - azure
  - azure-search
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

-   [Using autocomplete to expand a query from index contents](java/com/azure/search/AutoCompleteExample.java)
-   [Creating a new index](java/com/azure/search/CreateIndexExample.java)
-   [Create a new indexer](java/com/azure/search/CreateIndexerExample.java)
-   [Creating, listing and deleting data sources](java/com/azure/search/DataSourceExample.java)
-   [Retrieving a document by key](java/com/azure/search/GetSingleDocumentExample.java)
-   [How to handle HttpResponseException errors](java/com/azure/search/HttpResponseExceptionExample.java)
-   [Using IndexClient configuration options](java/com/azure/search/IndexClientConfigurationExample.java)
-   [Uploading, merging, and deleting documents in indexes](java/com/azure/search/IndexContentManagementExample.java)
-   [Search for documents of unknown type](java/com/azure/search/SearchForDynamicDocumentsExample.java)
-   [Using count, coverage, and facets](java/com/azure/search/SearchOptionsExample.java)
-   [Using suggestions](java/com/azure/search/SearchSuggestionExample.java)
-   [Searching for documents of known type](java/com/azure/search/SearchAsyncWithFullyTypedDocumentsExample.java)
-   [Creating a synonym map for an index](java/com/azure/search/SynonymMapsCreateExample.java)
-   [Creating skillsets](java/com/azure/search/CreateSkillsetExample.java)
-   [Search queries options with async client](java/com/azure/search/SearchOptionsAsyncExample.java)
-   [Search queries options with sync client](java/com/azure/search/SearchOptionsExample.java)
-   [Retrieving Index and Service statistics](java/com/azure/search/IndexAndServiceStatisticsExample.java)
-   [Setup datasource, indexer, index and skillset](java/com/azure/search/LifecycleSetupExample.java)
-   [List indexers](java/com/azure/search/ListIndexersExample.java)
-   [Add Synonym and custom skillset](java/com/azure/search/RefineSearchCapabilitiesExample.java)
-   [Execute a search solution - run indexer and issue search queries](java/com/azure/search/RunningSearchSolutionExample.java)

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
