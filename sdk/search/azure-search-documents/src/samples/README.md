---
page_type: sample
languages: java
products:
  - azure
  - azure-cognitive-search
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
    <version>11.5.0-beta.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

## How to run
These sample can be run in your IDE with default JDK.

## Examples
The following sections provide several code snippets covering some of the most common service tasks, including:

-   [Using autocomplete to expand a query from index contents](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/AutoCompleteExample.java)
-   [Creating a new index](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/indexes/CreateIndexExample.java)
-   [Create a new indexer](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/indexes/CreateIndexerExample.java)
-   [Creating, listing and deleting data sources](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/indexes/DataSourceExample.java)
-   [Retrieving a document by key](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/GetSingleDocumentExample.java)
-   [How to handle HttpResponseException errors](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/HttpResponseExceptionExample.java)
-   [Using IndexClient configuration options](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/IndexClientConfigurationExample.java)
-   [Uploading, merging, and deleting documents in indexes](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/IndexContentManagementExample.java)
-   [Search for documents of unknown type](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/SearchForDynamicDocumentsExample.java)
-   [Using count, coverage, and facets](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/SearchOptionsExample.java)
-   [Using suggestions](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/SearchSuggestionExample.java)
-   [Searching for documents of known type](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/SearchAsyncWithFullyTypedDocumentsExample.java)
-   [Creating a synonym map for an index](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/SynonymMapsCreateExample.java)
-   [Creating skillsets](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/indexes/CreateSkillsetExample.java)
-   [Search queries options with async client](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/SearchOptionsAsyncExample.java)
-   [Search queries options with sync client](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/SearchOptionsExample.java)
-   [Retrieving Index and Service statistics](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/IndexAndServiceStatisticsExample.java)
-   [Setup datasource, indexer, index and skillset](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/indexes/LifecycleSetupExample.java)
-   [List indexers](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/indexes/ListIndexersExample.java)
-   [Add Synonym and custom skillset](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/RefineSearchCapabilitiesExample.java)
-   [Execute a search solution - run indexer and issue search queries](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/RunningSearchSolutionExample.java)
-   [Setting customer x-ms-client-request-id per API call](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/PerCallRequestIdExample.java)

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
[azsearch_docs]: https://docs.microsoft.com/azure/search

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java/sdk/search/azure-search/samples/README.png)
