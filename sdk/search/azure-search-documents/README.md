# Azure Cognitive Search client library for Java

This is the Java client library for [Azure Cognitive Search](https://docs.microsoft.com/en-us/rest/api/searchservice/).
Azure Cognitive Search is a fully managed cloud search service that provides a rich search experience to custom applications.
This library provides an easy (native) way for a Java developer to interact with the service to accomplish tasks like: 
create and manage indexes, load data, implement search features, execute queries, and handle results.

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_documentation]| [Product documentation][search_docs] | [Samples][samples]

## Getting started

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-search-documents;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-search-documents</artifactId>
    <version>1.0.0-beta.4</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Prerequisites

- [Java Development Kit (JDK) with version 8 or above][jdk]
- [Azure subscription][azure_subscription]
- [Azure Cognitive Search service][search]

### Authenticate the client

In order to interact with the Azure Cognitive Search service you'll need to create an instance of the Search Client class. 
To make this possible you will need, 
1. [URL endpoint](https://docs.microsoft.com/en-us/azure/search/search-create-service-portal#get-a-key-and-url-endpoint) and
1. [Api-key of the Azure Cognitive Search service](https://docs.microsoft.com/en-us/azure/search/search-security-api-keys).

Note that you will need an admin key to authenticate the client (query keys only work for queries).

The SDK provides three clients.

1. SearchIndexClient for all CRUD operations on index and synonym maps.
1. SearchIndexerClient for all CRUD operations on indexer, date source, and skillset.
1. SearchClient for all document operations.

#### Create a SearchIndexClient

To create a `SearchIndexClient/SearchIndexAsyncClient`, you will need the values of the Azure Cognitive Search service 
URL endpoint and admin key.

<!-- embedme ./src/samples/java/com/azure/search/documents/ReadmeSamples.java#L62-L65 -->
```Java
SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
    .endpoint(endpoint)
    .credential(new AzureKeyCredential(apiKey))
    .buildClient();
```

or

<!-- embedme ./src/samples/java/com/azure/search/documents/ReadmeSamples.java#L69-L72 -->
```Java
SearchIndexAsyncClient searchIndexAsyncClient = new SearchIndexClientBuilder()
    .endpoint(endpoint)
    .credential(new AzureKeyCredential(apiKey))
    .buildAsyncClient();
```

#### Create a SearchIndexerClient

To create a `SearchIndexerClient/SearchIndexerAsyncClient`, you will need the values of the Azure Cognitive Search service 
URL endpoint and admin key.

<!-- embedme ./src/samples/java/com/azure/search/documents/ReadmeSamples.java#L76-L79 -->
```Java
SearchIndexerClient searchIndexerClient = new SearchIndexerClientBuilder()
    .endpoint(endpoint)
    .credential(new AzureKeyCredential(apiKey))
    .buildClient();
```

or

<!-- embedme ./src/samples/java/com/azure/search/documents/ReadmeSamples.java#L83-L86 -->
```Java
SearchIndexerAsyncClient searchIndexerAsyncClient = new SearchIndexerClientBuilder()
    .endpoint(endpoint)
    .credential(new AzureKeyCredential(apiKey))
    .buildAsyncClient();
```

#### Create a SearchClient

Once you have the values of the Azure Cognitive Search service URL endpoint and 
admin key, you can create the `SearchClient/SearchAsyncClient` with an existing index name:

<!-- embedme ./src/samples/java/com/azure/search/documents/ReadmeSamples.java#L46-L50 -->
```Java
SearchClient searchClient = new SearchClientBuilder()
    .endpoint(endpoint)
    .credential(new AzureKeyCredential(adminKey))
    .indexName(indexName)
    .buildClient();
```

or

<!-- embedme ./src/samples/java/com/azure/search/documents/ReadmeSamples.java#L54-L58 -->
```Java
SearchAsyncClient searchAsyncClient = new SearchClientBuilder()
    .endpoint(endpoint)
    .credential(new AzureKeyCredential(adminKey))
    .indexName(indexName)
    .buildAsyncClient();
```

## Key concepts

Azure Cognitive Search service has the concepts of search services and indexes and documents, where a search service contains 
one or more indexes that provides persistent storage of searchable data, and data is loaded in the form of JSON documents. 
Data can be pushed to an index from an external data source, but if you use an indexer, it's possible to crawl a data 
source to extract and load data into an index.

There are several types of operations that can be executed against the service:

-   [Index management operations](https://docs.microsoft.com/en-us/rest/api/searchservice/index-operations). Create, delete, update, or configure a search index.
-   [Document operations](https://docs.microsoft.com/en-us/rest/api/searchservice/document-operations). Add, update, or delete documents in the index, query the index, or look up specific documents by ID.
-   [Indexer operations](https://docs.microsoft.com/en-us/rest/api/searchservice/indexer-operations). Automate aspects of an indexing operation by configuring a data source and an indexer that you can schedule or run on demand. This feature is supported for a limited number of data source types.
-   [Skillset operations](https://docs.microsoft.com/en-us/rest/api/searchservice/skillset-operations). Part of AI workload, a skillset defines a series of a series of enrichment processing steps. A skillset is consumed by an indexer.
-   [Synonym map operations](https://docs.microsoft.com/en-us/rest/api/searchservice/synonym-map-operations). A synonym map is a service-level resource that contains user-defined synonyms. This resource is maintained independently from search indexes. Once uploaded, you can point any searchable field to the synonym map (one per field).

## Examples

### Create an index

Create Index using `searchIndexClient` instantiated in [Create a SearchIndexClient](#create-a-searchindexclient)

<!-- embedme ./src/samples/java/com/azure/search/documents/ReadmeSamples.java#L116-L127 -->
```java
SearchIndex newIndex = new SearchIndex()
    .setName("index_name")
    .setFields(
        Arrays.asList(new SearchField()
                .setName("Name")
                .setType(SearchFieldDataType.STRING)
                .setKey(Boolean.TRUE),
            new SearchField()
                .setName("Cuisine")
                .setType(SearchFieldDataType.STRING)));
// Create index.
searchIndexClient.createIndex(newIndex);
```
### Upload a Document

Upload hotel document to Search Index using `searchClient` instantiated [Create a SearchClient](#create-a-searchclient)

<!-- embedme ./src/samples/java/com/azure/search/documents/ReadmeSamples.java#L131-L136 -->
```java
List<Hotel> hotels = new ArrayList<>();
hotels.add(new Hotel().setHotelId("100"));
hotels.add(new Hotel().setHotelId("200"));
hotels.add(new Hotel().setHotelId("300"));
// Upload hotel.
searchClient.uploadDocuments(hotels);
```

### Search on hotel name

Search hotel using keyword using `searchClient` instantiated in [Create a SearchClient](#create-a-searchclient)

<!-- embedme ./src/samples/java/com/azure/search/documents/ReadmeSamples.java#L140-L150 -->
```java
// Perform a text-based search
for (SearchResult result : searchClient.search("luxury hotel",
    new SearchOptions(), new RequestOptions(), Context.NONE)) {

    // Each result is a dynamic Map
    SearchDocument doc = result.getDocument();
    String hotelName = (String) doc.get("HotelName");
    Double rating = (Double) doc.get("Rating");

    System.out.printf("%s: %s%n", hotelName, rating);
}
```

- Samples are explained in detail [here][samples_readme].

## Troubleshooting

### General

When you interact with Azure Cognitive Search using this Java client library, errors returned by the service correspond 
to the same HTTP status codes returned for [REST API][rest_api] requests. For example, if you try to retrieve a document 
that doesn't exist in your index, a `404` error is returned, indicating `Not Found`.

### Enabling Logging

Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite 
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help 
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Default HTTP Client

By default a Netty based HTTP client will be used, for more information on configuring or changing the HTTP client is 
detailed in the [HTTP clients wiki](https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients).

## Next steps

- Samples are explained in detail [here][samples_readme].
- [Watch a demo or deep dive video](https://azure.microsoft.com/resources/videos/index/?services=search)
- [Read more about the Azure Cognitive Search service](https://docs.microsoft.com/azure/search/search-what-is-azure-search)

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a 
[Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights 
to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate 
the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to 
do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] 
or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[jdk]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[api_documentation]: https://aka.ms/java-docs
[search]: https://azure.microsoft.com/services/search/
[search_docs]: https://docs.microsoft.com/azure/search/
[azure_subscription]: https://azure.microsoft.com/free
[maven]: https://maven.apache.org/
[package]: https://search.maven.org/artifact/com.azure/azure-search-documents
[samples]: src/samples/java/com/azure/search
[samples_readme]: src/samples/README.md
[source_code]: src
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[add_headers_from_context_policy]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/src/main/java/com/azure/core/http/policy/AddHeadersFromContextPolicy.java
[rest_api]: https://docs.microsoft.com/rest/api/searchservice/http-status-codes

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fsearch%2Fazure-search-documents%2FREADME.png)
