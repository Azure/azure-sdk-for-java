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

[//]: # ({x-version-update-start;com.azure:azure-search;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-search</artifactId>
    <version>1.0.0-beta.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

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

In order to interact with the Cognitive Search service you'll need to create an instance of the Search Client class. To make this possible you will need an [api-key of the Cognitive Search service](https://docs.microsoft.com/en-us/azure/search/search-security-api-keys).

The SDK provides two types of clients - SearchIndexClient for all document operations, and SearchServiceClient for all CRUD operations on the service resources.

#### Create a SearchServiceClient

Once you have the values of the Cognitive Search Service [URL endpoint](https://docs.microsoft.com/en-us/azure/search/search-create-service-portal#get-a-key-and-url-endpoint) and admin key you can create the Search Service client:

<!-- embedme ./src/samples/java/com/azure/search/ReadmeSamples.java#L27-L32 -->
```Java
SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new SearchApiKeyCredential(ADMIN_KEY))
            .buildClient();
```

or

<!-- embedme ./src/samples/java/com/azure/search/ReadmeSamples.java#L34-L39 -->
```Java
SearchServiceAsyncClient client = new SearchServiceClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new SearchApiKeyCredential(ADMIN_KEY))
            .buildAsyncClient();
```

#### Create a SearchIndexClient

To create a SearchIndexClient, you will need an exisitng index name as well as the values of the Cognitive Search Service [URL endpoint](https://docs.microsoft.com/en-us/azure/search/search-create-service-portal#get-a-key-and-url-endpoint) and query key:

<!-- embedme ./src/samples/java/com/azure/search/ReadmeSamples.java#L41-L47 -->
```Java
SearchIndexClient client = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new SearchApiKeyCredential(API_KEY))
            .indexName(INDEX_NAME)
            .buildClient();
```

or

<!-- embedme ./src/samples/java/com/azure/search/ReadmeSamples.java#L49-L55 -->
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

Samples are explained in detail [here][samples_readme].

## Troubleshooting

### General

When you interact with Azure Cognitive Search using this Java client library, errors returned by the service correspond to the same HTTP status codes returned for [REST API][rest_api] requests. For example, if you try to retrieve a document that doesn't exist in your index, a `404` error is returned, indicating `Not Found`.

App Configuration provides a way to define customized headers through `Context` object in the public API. 

<!-- embedme ./src/samples/java/com/azure/search/ReadmeSamples.java#L57-L69 -->
```java
HttpHeaders headers = new HttpHeaders();
headers.put("my-header1", "my-header1-value");
headers.put("my-header2", "my-header2-value");
headers.put("my-header3", "my-header3-value");
// Call API by passing headers in Context.
Index index = new Index().setName(INDEX_NAME);
searchClient.createIndexWithResponse(
    index,
    new RequestOptions(),
    new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers));
// Above three HttpHeader will be added in outgoing HttpRequest.
```
For more detail information, check out the [AddHeadersFromContextPolicy][add_headers_from_context_policy]

### Default HTTP Client
All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure 
the client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the
[HTTP clients wiki](https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients).

### Default SSL library
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL 
operations. The Boring SSL library is an uber jar containing native libraries for Linux / macOS / Windows, and provides 
better performance compared to the default SSL implementation within the JDK. For more information, including how to 
reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Next steps

- Samples are explained in detail [here][samples_readme].

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->

[api_documentation]: https://aka.ms/java-docs
[search]: https://azure.microsoft.com/en-us/services/search/
[search_docs]: https://docs.microsoft.com/en-us/azure/search/
[azure_subscription]: https://azure.microsoft.com/free
[maven]: https://maven.apache.org/
[package]: https://search.maven.org/artifact/com.azure/azure-search
[samples]: src/samples/java/com/azure/search
[samples_readme]: src/samples/README.md
[source_code]: src
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[add_headers_from_context_policy]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/src/main/java/com/azure/core/http/policy/AddHeadersFromContextPolicy.java
[rest_api]: https://docs.microsoft.com/en-us/rest/api/searchservice/http-status-codes

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%search%2Fazure-search%2FREADME.png)
