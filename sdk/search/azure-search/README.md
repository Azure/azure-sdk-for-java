# Azure cognitive search client library for Java

This is the Java client library for [Azure Cognitive Search](https://docs.microsoft.com/en-us/rest/api/searchservice/).
Azure Cognitive Search is a fully managed cloud search service that provides a rich search experience to custom applications.
This library provides an easy (native) way for a Java developer to interact with the service to accomplish tasks like: create and manage indexes, load data, implement search features, execute queries, and handle results.

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_documentation]
| [Product documentation][search_docs] | [Samples][samples]

## Getting started

### Prerequisites

-   Java Development Kit (JDK) with version 8 or above
-   [Azure subscription][azure_subscription]
-   An instance of [Cognitive search service][search]

### Adding the package to your product

[//]: # "{x-version-update-start;com.azure:azure-search;current}"

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-search</artifactId>
    <version>1.0.0-preview.2</version>
</dependency>
```

[//]: # "{x-version-update-end}"

### Default HTTP Client

All client libraries, by default, use Netty HTTP client. Adding the above dependency will automatically configure the Cognitive Search Service client to use Netty HTTP client.

### Alternate HTTP client

If, instead of Netty it is preferable to use OkHTTP, there is an HTTP client available for that too. Exclude the default
Netty and include the OkHTTP client in your pom.xml.

[//]: # "{x-version-update-start;com.azure:azure-search;current}"

```xml
<!-- Add the Cognitive Search dependency without the Netty HTTP client -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-search</artifactId>
    <version>1.0.0-preview.2</version>
    <exclusions>
        <exclusion>
            <groupId>com.azure</groupId>
            <artifactId>azure-core-http-netty</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

[//]: # "{x-version-update-end}"
[//]: # "{x-version-update-start;com.azure:azure-core-http-okhttp;current}"

```xml
<!-- Add the OkHTTP client to use with Cognitive Search -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-core-http-okhttp</artifactId>
    <version>1.1.0</version>
</dependency>
```

[//]: # "{x-version-update-end}"

### Configuring HTTP Clients

When an HTTP client is included on the classpath, as shown above, it is not necessary to specify it in the client library [builders](#create-a-searchserviceclient) unless you want to customize the HTTP client in some fashion. If this is desired, the `httpClient` builder method is often available to achieve just this by allowing users to provide custom (or customized) `com.azure.core.http.HttpClient` instances.

For starters, by having the Netty or OkHTTP dependencies on your classpath, as shown above, you can create new instances of these `HttpClient` types using their builder APIs. For example, here is how you would create a Netty HttpClient instance:

```java
HttpClient client = new NettyAsyncHttpClientBuilder()
        .port(8080)
        .wiretap(true)
        .build();
```

### Default SSL library

All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL operations. The Boring SSL library is an uber jar containing native libraries for Linux / macOS / Windows, and provides better performance compared to the default SSL implementation within the JDK. For more information, including how to reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Using the library

The APIs documented in this section provide access to operations on search data, such as index creation and population, document upload, and queries. When calling APIs, keep the following points in mind:

-   All APIs will be issued over HTTPS (on the default port 443).
-   Your search service is uniquely identified by a fully-qualified domain name (for example: `mysearchservice.search.windows.net`).
-   All requests must include an `api-key` that was generated for the Search service you provisioned. Having a valid key establishes trust, on a per request basis, between the application sending the request and the service that handles it.
-   Requests can optionally specify an `api-version`. The default value matches the version of the current service release: `2019-05-06`.
-   You will need to provide the `cognitive search service url` which is the URL of the Azure Cognitive Search service you provisioned: `https://<yourService>.search.windows.net`.

## Key concepts

Azure Cognitive Search has the concepts of search services and indexes and documents, where a search service contains one or more indexes that provides persistent storage of searchable data, and data is loaded in the form of JSON documents. Data is typically pushed to an index from an external data source, but if you use an indexer, it's possible to crawl a data source to extract and load data into an index.

There are several types of operations that can be executed against the service:

-   [Index management operations](https://docs.microsoft.com/en-us/rest/api/searchservice/index-operations). Create, delete, update, or configure a search index.
-   [Document operations](https://docs.microsoft.com/en-us/rest/api/searchservice/document-operations). Add, update, or delete documents in the index, query the index, or look up specific documents by ID.
-   [Indexer operations](https://docs.microsoft.com/en-us/rest/api/searchservice/indexer-operations). Automate aspects of an indexing operation by configuring a data source and an indexer that you can schedule or run on demand. This feature is supported for a limited number of data source types.
-   [Skillset operations](https://docs.microsoft.com/en-us/rest/api/searchservice/skillset-operations). Part of a cognitive search workload, a skillset defines a series of enrichment processing. A skillset is consumed by an indexer.
-   [Synonym map operations](https://docs.microsoft.com/en-us/rest/api/searchservice/synonym-map-operations). A synonym map is service-level resource that containers user-defined synonyms. This resource is maintained independently from search indexes. Once uploaded, you can point any searchable field to the synonym map (one per field).

A separate REST API is provided for service administration, including provisioning the service or altering capacity. For more information, see [Azure Cognitive Search Management REST](https://docs.microsoft.com/en-us/rest/api/searchmanagement/index).

### Authenticate the client

In order to interact with the Cognitive Search service you'll need to create an instance of the Search Client class. To make this possible you'll need an [api-key of the Cognitive Search service](https://docs.microsoft.com/en-us/azure/search/search-security-api-keys).

The SDK provides two types of clients - SearchIndexClient for all document operations, and SearchServiceClient for all CRUD operations on the service resources.

#### Create a SearchServiceClient

Once you have the values of the Cognitive Search Service [URL endpoint](https://docs.microsoft.com/en-us/azure/search/search-create-service-portal#get-a-key-and-url-endpoint) and admin key you can create the Search Service client:

```Java
SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new SearchApiKeyCredential(ADMIN_KEY))
            .buildClient();
```

or

```Java
SearchServiceAsyncClient client = new SearchServiceClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new SearchApiKeyCredential(ADMIN_KEY))
            .buildAsyncClient();
```

#### Create a SearchIndexClient

To create a SearchIndexClient, you will need an exisitng index name as well as the values of the Cognitive Search Service [URL endpoint](https://docs.microsoft.com/en-us/azure/search/search-create-service-portal#get-a-key-and-url-endpoint) and query key:

```Java
SearchIndexClient client = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new SearchApiKeyCredential(API_KEY))
            .indexName(INDEX_NAME)
            .buildClient();
```

or

```Java
SearchIndexAsyncClient client = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new SearchApiKeyCredential(API_KEY))
            .indexName(INDEX_NAME)
            .buildAsyncClient();
```

### Asynchronous and Synchronous Pagination and Iteration

This SDK is using an [azure-core](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/README.md) concept for paging and async streaming implemented as `PagedFlux<T>`.

Azure Cognitive Search uses server-side paging to prevent queries from retrieving too many documents at once. The default page size is 50, while the maximum page size is 1000. This means that by default Search Documents API returns at most 50 results if you don't specify \$top. If there are more than 50 results, the response includes information to retrieve the next page of at most 50 result.
The SDK handles this by returning PagedIterableBase for sync APIs and PagedFluxBase for async APIs (More reading can be found in the Java SDK [wiki](https://github.com/Azure/azure-sdk-for-java/wiki/Pagination-and-Iteration)).

Both PagedIterable and PagedFlux enable the common case to be quickly and easily achieved: iterating over a paginated response deserialized into a given type T. In the case of PagedIterable, it implements the Iterable interface, and offers API to receive a Stream. In the case of PagedFlux, it is a Flux.

#### Pages

For the Cognitive Search service SDK, each page in the response will contain page-specific additional information, based on the executed API:

**[SearchPagedResponse](/sdk/search/azure-search/src/main/java/com/azure/search/SearchPagedResponse.java)**
Represents an HTTP response from the search API request that contains a list of items deserialized into a page. The additional information is:

-   count - number of total documents returned. Will be returned only if isIncludeTotalResultCount is set to true
-   coverage - coverage value

**[SuggestPagedResponse](/sdk/search/azure-search/src/main/java/com/azure/search/SuggestPagedResponse.java)**
Represents an HTTP response from the suggest API request that contains a list of items deserialized into a page. The additional information is:

-   coverage - coverage value

**[AutocompletePagedResponse](/sdk/search/azure-search/src/main/java/com/azure/search/AutocompletePagedResponse.java)**
Represents an HTTP response from the autocomplete API request that contains a list of items deserialized into a page.

#### Synchronous Pagination and Iteration

[Search queries options with sync client](/sdk/search/azure-search/src/samples/java/com/azure/search/SearchOptionsExample.java)

#### Asynchronous Pagination and Iteration

[Search queries options with async client](/sdk/search/azure-search/src/samples/java/com/azure/search/SearchOptionsAsyncExample.java)

## Examples

-   [Using autocomplete to expand a query from index contents](/sdk/search/azure-search/src/samples/java/com/azure/search/AutoCompleteExample.java)
-   [Creating a new index](/sdk/search/azure-search/src/samples/java/com/azure/search/CreateIndexExample.java)
-   [Create a new indexer](/sdk/search/azure-search/src/samples/java/com/azure/search/CreateIndexerExample.java)
-   [Creating, listing and deleting data sources](/sdk/search/azure-search/src/samples/java/com/azure/search/DataSourceExample.java)
-   [Retrieving a document by key](/sdk/search/azure-search/src/samples/java/com/azure/search/GetSingleDocumentExample.java)
-   [How to handle HttpREsponseException errors](/sdk/search/azure-search/src/samples/java/com/azure/search/HttpResponseExceptionExample.java)
-   [Using IndexClient configuration options](/sdk/search/azure-search/src/samples/java/com/azure/search/IndexClientConfigurationExample.java)
-   [Uploading, merging, and deleting documents in indexes](/sdk/search/azure-search/src/samples/java/com/azure/search/IndexContentManagementExample.java)
-   [Search for documents of unknown type](/sdk/search/azure-search/src/samples/java/com/azure/search/SearchForDynamicDocumentsExample.java)
-   [Using count, coverage, and facets](/sdk/search/azure-search/src/samples/java/com/azure/search/SearchOptionsExample.java)
-   [Using suggestions](/sdk/search/azure-search/src/samples/java/com/azure/search/SearchSuggestionExample.java)
-   [Searching for documents of known type](/sdk/search/azure-search/src/samples/java/com/azure/search/SearchWithFullyTypedDocumentsExample.java)
-   [Creating a synonym map for an index](/sdk/search/azure-search/src/samples/java/com/azure/search/SynonymMapsCreateExample.java)
-   [Creating skillsets](/sdk/search/azure-search/src/samples/java/com/azure/search/CreateSkillsetExample.java)
-   [Creating and managing indexers](/sdk/search/azure-search/src/samples/java/com/azure/search/IndexerManagementExample.java)
-   [Search queries options with async client](/sdk/search/azure-search/src/samples/java/com/azure/search/SearchOptionsAsyncExample.java)
-   [Search queries options with sync client](/sdk/search/azure-search/src/samples/java/com/azure/search/SearchOptionsExample.java)
-   [Retrieving Index and Service statistics](/sdk/search/azure-search/src/samples/java/com/azure/search/IndexAndServiceStatisticsExample.java)
-   [Setup datasource, indexer, index and skillset](/sdk/search/azure-search/src/samples/java/com/azure/search/LifecycleSetupExample.java)
-   [List indexers](/sdk/search/azure-search/src/samples/java/com/azure/search/ListIndexersExample.java)
-   [Add Synonym and custom skillset](/sdk/search/azure-search/src/samples/java/com/azure/search/RefineSearchCapabilitiesExample.java)
-   [Execute a search solution - run indexer and issue search queries](/sdk/search/azure-search/src/samples/java/com/azure/search/RunningSearchSolutionExample.java)

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
