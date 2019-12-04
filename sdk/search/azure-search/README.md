# Azure cognitive search client library for Java

This is the Java client library for [Azure Cognitive Search](https://docs.microsoft.com/en-us/rest/api/searchservice/).
Azure Cognitive Search is a fully managed cloud search service that provides a rich search experience to custom applications.
This library provides an easy (native) way for a Java developer to interact with the service to accomplish tasks like: create and manage indexes, load data, implement search features, execute queries, and handle results.

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_documentation]
| [Product documentation][search_docs] | [Samples][samples]

## Getting started

### Prerequisites

- Java Development Kit (JDK) with version 8 or above
- [Azure subscription][azure_subscription]
- An instance of [Cognitive search service][search]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-search;current})

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-search</artifactId>
    <version>1.0.0-preview.2</version>
</dependency>
```

[//]: # ({x-version-update-end})

## Key concepts

Azure Cognitive Search has the concepts of search services and indexes and documents, where a search service contains one or more indexes that provides persistent storage of searchable data, and data is loaded in the form of JSON documents. Data is typically pushed to an index from an external data source, but if you use an indexer, it's possible to crawl a data source to extract and load data into an index.

There are several types of operations that can be executed against the service:

* [Index management operations](https://docs.microsoft.com/en-us/rest/api/searchservice/index-operations). Create, delete, update, or configure a search index.
* [Document operations](https://docs.microsoft.com/en-us/rest/api/searchservice/document-operations). Add, update, or delete documents in the index, query the index, or look up specific documents by ID.
* [Indexer operations](https://docs.microsoft.com/en-us/rest/api/searchservice/indexer-operations). Automate aspects of an indexing operation by configuring a data source and an indexer that you can schedule or run on demand. This feature is supported for a limited number of data source types.
* [Skillset operations](https://docs.microsoft.com/en-us/rest/api/searchservice/skillset-operations). Part of a cognitive search workload, a skillset defines a series of enrichment processing. A skillset is consumed by an indexer.
* [Synonym map operations](https://docs.microsoft.com/en-us/rest/api/searchservice/synonym-map-operations). A synonym map is service-level resource that containers user-defined synonyms. This resource is maintained independently from search indexes. Once uploaded, you can point any searchable field to the synonym map (one per field).

A separate REST API is provided for service administration, including provisioning the service or altering capacity. For more information, see [Azure Cognitive Search Management REST](https://docs.microsoft.com/en-us/rest/api/searchmanagement/index).

## Using the library

The APIs documented in this section provide access to operations on search data, such as index creation and population, document upload, and queries. When calling APIs, keep the following points in mind:

* All APIs will be issued over HTTPS (on the default port 443).
* Your search service is uniquely identified by a fully-qualified domain name (for example: `mysearchservice.search.windows.net`).
* All requests must include an `api-key` that was generated for the Search service you provisioned. Having a valid key establishes trust, on a per request basis, between the application sending the request and the service that handles it.
* Requests can optionally specify an `api-version`. The default value matches the version of the current service release: `2019-05-06`.
* You will need to provide `index name`and the `cognitive search service name` which is the URL of the Azure Cognitive Search service you provisioned: `https://<yourService>.search.windows.net`.

## Examples

* [Using autocomplete to expand a query from index contents](/sdk/search/azure-search/src/samples/java/com/azure/search/AutoCompleteExample.java)
* [Creating a new index](/sdk/search/azure-search/src/samples/java/com/azure/search/CreateIndexExample.java)
* [Creating, listind, and deleting data sources](/sdk/search/azure-search/src/samples/java/com/azure/search/DataSourceExample.java)
* [Retrieving a document by key](/sdk/search/azure-search/src/samples/java/com/azure/search/GetSingleDocumentExample.java)
* [How to handle HttpREsponseException errors](/sdk/search/azure-search/src/samples/java/com/azure/search/HttpResponseExceptionExample.java)
* [Using IndexClient configuration options](/sdk/search/azure-search/src/samples/java/com/azure/search/IndexClientConfigurationExample.java)
* [Uploading, merging, and deleting documents in indexes](/sdk/search/azure-search/src/samples/java/com/azure/search/IndexContentManagementExample.java)
* [Search for documents of unknown type](/sdk/search/azure-search/src/samples/java/com/azure/search/SearchForDynamicDocumentsExample.java)
* [Using count, coverage, and facets](/sdk/search/azure-search/src/samples/java/com/azure/search/SearchOptionsExample.java)
* [Using suggestions](/sdk/search/azure-search/src/samples/java/com/azure/search/SearchSuggestionExample.java)
* [Searching for documents of known type](/sdk/search/azure-search/src/samples/java/com/azure/search/SearchWithFullyTypedDocumentsExample.java)
* [Creating a synonym map for an index](/sdk/search/azure-search/src/samples/java/com/azure/search/SynonymMapsCreateExample.java)
* [Creating skillsets](/sdk/search/azure-search/src/samples/java/com/azure/search/CreateSkillsetExample.java)
* [Creating and managing indexers](/sdk/search/azure-search/src/samples/java/com/azure/search/IndexerManagementExample.java)
* [Async Search](/sdk/search/azure-search/src/samples/java/com/azure/search/SearchOptionsAsyncExample.java)
* [Sync Search](/sdk/search/azure-search/src/samples/java/com/azure/search/SearchOptionsExample.java)

## Troubleshooting

### General

When you interact with Azure Cognitive Search using this Java client library, errors returned by the service correspond to the same HTTP status codes returned for [REST API][rest_api] requests. For example, if you try to retrieve a document that doesn't exist in your index, a `404` error is returned, indicating `Not Found`.

## Next steps

TODO: Add a java quickstart

## Contributing

If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft Azure Projects Contribution Guidelines](http://azure.github.io/guidelines.html).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

<!-- LINKS -->
[api_documentation]: https://aka.ms/java-docs
[search]: https://azure.microsoft.com/en-us/services/search/
[search_docs]: https://docs.microsoft.com/en-us/azure/search/
[azure_subscription]: https://azure.microsoft.com/free
[maven]: https://maven.apache.org/
[package]: https://search.maven.org/artifact/com.azure/azure-search
[samples]: src/samples/java/com/azure/search
[source_code]: src

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java/sdk/search/azure-search/README.png)
