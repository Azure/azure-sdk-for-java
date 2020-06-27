# Azure Cognitive Search client library for Java
This is the Java client library for [Azure Cognitive Search](https://docs.microsoft.com/azure/search/). 
Azure Cognitive Search service is a
search-as-a-service cloud solution that gives developers APIs and tools
for adding a rich search experience over private, heterogeneous content
in web, mobile, and enterprise applications.

The Azure Cognitive Search service is well suited for the following
 application scenarios:

* Consolidate varied content types into a single searchable index.
  To populate an index, you can push JSON documents that contain your content,
  or if your data is already in Azure, create an indexer to pull in data
  automatically.
* Attach skillsets to an indexer to create searchable content from images
  and large text documents. A skillset leverages AI from Cognitive Services
  for built-in OCR, entity recognition, key phrase extraction, language
  detection, text translation, and sentiment analysis. You can also add
  custom skills to integrate external processing of your content during
  data ingestion.
* In a search client application, implement query logic and user experiences
  similar to commercial web search engines.

Use the Azure.Search.Documents client library to:

* Submit queries for simple and advanced query forms that include fuzzy
  search, wildcard search, regular expressions.
* Implement filtered queries for faceted navigation, geospatial search,
  or to narrow results based on filter criteria.
* Create and manage search indexes.
* Upload and update documents in the search index.
* Create and manage indexers that pull data from Azure into an index.
* Create and manage skillsets that add AI enrichment to data ingestion.
* Create and manage analyzers for advanced text analysis or multi-lingual content.
* Optimize results through scoring profiles to factor in business logic or freshness.

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_documentation]| [Product documentation][search_docs] | [Samples][samples]

## Getting started

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-search-documents;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-search-documents</artifactId>
    <version>11.0.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Prerequisites

- [Java Development Kit (JDK) with version 8 or above][jdk]
- [Azure subscription][azure_subscription]
- [Azure Cognitive Search service][search]
- To create a new search service, you can use the [Azure portal][create_search_service_docs],
[Azure PowerShell][create_search_service_ps], or the [Azure CLI][create_search_service_cli].
Here's an example using the Azure CLI to create a free instance for getting started:

```Powershell
az search service create --name <mysearch> --resource-group <mysearch-rg> --sku free --location westus
```

- See [choosing a pricing tier](https://docs.microsoft.com/azure/search/search-sku-tier)
 for more information about available options.
 
### Authenticate the client

In order to interact with the Azure Cognitive Search service you'll need to create an instance of the Search Client class. 
To make this possible you will need, 
1. [URL endpoint](https://docs.microsoft.com/en-us/azure/search/search-create-service-portal#get-a-key-and-url-endpoint) and
1. All requests to a search service need an api-key that was generated specifically
for your service. [The api-key is the sole mechanism for authenticating access to
your search service endpoint.](https://docs.microsoft.com/azure/search/search-security-api-keys)
You can obtain your api-key from the
[Azure portal](https://portal.azure.com/) or via the Azure CLI:

```Powershell
az search admin-key show --service-name <mysearch> --resource-group <mysearch-rg>
```
**Note:**
1. The example Azure CLI snippet above retrieves an admin key so it's easier
to get started exploring APIs, but it should be managed carefully.
1. There are two types of keys used to access your search service: **admin**
*(read-write)* and **query** *(read-only)* keys.  Restricting access and
operations in client apps is essential to safeguarding the search assets on your
service.  Always use a query key rather than an admin key for any query
originating from a client app.



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

### Send your first search query

To get running immediately, we're going to connect to a well-known sandbox
Search service provided by Microsoft. This means you do not need an Azure
subscription or Azure Cognitive Search service to try out this query.

<!-- embedme ./src/samples/java/com/azure/search/documents/ReadmeSamples.java#L149-174 -->

## Key concepts

An Azure Cognitive Search service contains one or more indexes that provide
persistent storage of searchable data in the form of JSON documents.  _(If
you're brand new to search, you can make a very rough analogy between
indexes and database tables.)_  The Azure.Search.Documents client library
exposes operations on these resources through two main client types.

* `SearchClient` helps with:
  * [Searching](https://docs.microsoft.com/azure/search/search-lucene-query-architecture)
    your indexed documents using
    [rich queries](https://docs.microsoft.com/azure/search/search-query-overview)
    and [powerful data shaping](https://docs.microsoft.com/azure/search/search-filters)
  * [Autocompleting](https://docs.microsoft.com/rest/api/searchservice/autocomplete)
    partially typed search terms based on documents in the index
  * [Suggesting](https://docs.microsoft.com/rest/api/searchservice/suggestions)
    the most likely matching text in documents as a user types
  * [Adding, Updating or Deleting Documents](https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents)
    documents from an index

* `SearchIndexClient` allows you to:
  * [Create, delete, update, or configure a search index](https://docs.microsoft.com/rest/api/searchservice/index-operations)
  * [Declare custom synonym maps to expand or rewrite queries](https://docs.microsoft.com/rest/api/searchservice/synonym-map-operations)
  * Most of the `SearchServiceClient` functionality is not yet available in our current preview

* `SearchIndexerClient` allows you to:
  * [Start indexers to automatically crawl data sources](https://docs.microsoft.com/rest/api/searchservice/indexer-operations)
  * [Define AI powered Skillsets to transform and enrich your data](https://docs.microsoft.com/rest/api/searchservice/skillset-operations)

## Examples

#### Use `SearchDocument` like a dictionary

`SearchDocument` is the default type returned from queries when you don't
provide your own.  Here we perform the search, enumerate over the results, and
extract data using `SearchDocument`'s dictionary indexer.

<!-- embedme ./src/samples/java/com/azure/search/documents/ReadmeSamples.java#L54-L58 -->
```java 
SearchResults<SearchDocument> response = client.Search<SearchDocument>("luxury");
foreach (SearchResult<SearchDocument> result in response.GetResults())
{
    SearchDocument doc = result.Document;
    string id = (string)doc["hotelId"];
    string name = (string)doc["hotelName"];
    Console.WriteLine("{id}: {name}");
}
```

#### Use Java model class
<!-- embedme ./src/samples/java/com/azure/search/documents/ReadmeSamples.java#L54-L58 -->
```C# Snippet:Azure_Search_Tests_Samples_Readme_StaticType
public class Hotel
{
    [JsonPropertyName("hotelId")]
    public string Id { get; set; }

    [JsonPropertyName("hotelName")]
    public string Name { get; set; }
}
```

And use them in place of `SearchDocument` when querying.
<!-- embedme ./src/samples/java/com/azure/search/documents/ReadmeSamples.java#L54-L58 -->
```C# Snippet:Azure_Search_Tests_Samples_Readme_StaticQuery
SearchResults<Hotel> response = client.Search<Hotel>("luxury");
foreach (SearchResult<Hotel> result in response.GetResults())
{
    Hotel doc = result.Document;
    Console.WriteLine($"{doc.Id}: {doc.Name}");
}
```

If you're working with a search index and know the schema, creating Java model class
is recommended.

#### SearchOptions

The `SearchOptions` provide powerful control over the behavior of our queries.
Let's search for the top 5 luxury hotels with a good rating.

<!-- embedme ./src/samples/java/com/azure/search/documents/ReadmeSamples.java#L54-L58 -->
```C# Snippet:Azure_Search_Tests_Samples_Readme_Options
int stars = 4;
SearchOptions options = new SearchOptions
{
    // Filter to only ratings greater than or equal our preference
    Filter = SearchFilter.Create($"rating ge {stars}"),
    Size = 5, // Take only 5 results
    OrderBy = new[] { "rating desc" } // Sort by rating from high to low
};
SearchResults<Hotel> response = client.Search<Hotel>("luxury", options);
// ...
```

### Creating an index

You can use the [`SearchIndexClient`](#Create-a-SearchIndexClient) to create a search index. Fields can be
defined using convenient `SimpleField`, `SearchableField`, or `ComplexField`
classes. Indexes can also define suggesters, lexical analyzers, and more.

<!-- embedme ./src/samples/java/com/azure/search/documents/ReadmeSamples.java#L116-L121 -->
```C# Snippet:Azure_Search_Tests_Samples_Readme_CreateIndex

// Create the index
SearchIndex index = new SearchIndex("hotels")
{
    Fields =
    {
        new SimpleField("hotelId", SearchFieldDataType.String) { IsKey = true, IsFilterable = true, IsSortable = true },
        new SearchableField("hotelName") { IsFilterable = true, IsSortable = true },
        new SearchableField("description") { AnalyzerName = LexicalAnalyzerName.EnLucene },
        new SearchableField("tags", collection: true) { IsFilterable = true, IsFacetable = true },
        new ComplexField("address")
        {
            Fields =
            {
                new SearchableField("streetAddress"),
                new SearchableField("city") { IsFilterable = true, IsSortable = true, IsFacetable = true },
                new SearchableField("stateProvince") { IsFilterable = true, IsSortable = true, IsFacetable = true },
                new SearchableField("country") { IsFilterable = true, IsSortable = true, IsFacetable = true },
                new SearchableField("postalCode") { IsFilterable = true, IsSortable = true, IsFacetable = true }
            }
        }
    },
    Suggesters =
    {
        // Suggest query terms from both the hotelName and description fields.
        new SearchSuggester("sg", "hotelName", "description")
    }
};

client.CreateIndex(index);
```

### Retrieving a specific document from your index

In addition to querying for documents using keywords and optional filters,
you can retrieve a specific document from your index if you already know the
key. You could get the key from a query, for example, and want to show more
information about it or navigate your customer to that document.

<!-- embedme ./src/samples/java/com/azure/search/documents/ReadmeSamples.java#L116-L121 -->
```C# Snippet:Azure_Search_Tests_Samples_Readme_GetDocument
Hotel doc = client.GetDocument<Hotel>("1");
Console.WriteLine($"{doc.Id}: {doc.Name}");
```

### Adding documents to your index

You can `Upload`, `Merge`, `MergeOrUpload`, and `Delete` multiple documents from
an index in a single batched request.  There are
[a few special rules for merging](https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents#document-actions)
to be aware of.

<!-- embedme ./src/samples/java/com/azure/search/documents/ReadmeSamples.java#L116-L121 -->
```C# Snippet:Azure_Search_Tests_Samples_Readme_Index
IndexDocumentsBatch<Hotel> batch = IndexDocumentsBatch.Create(
IndexDocumentsAction.Upload(new Hotel { Id = "783", Name = "Upload Inn" }),
IndexDocumentsAction.Merge(new Hotel { Id = "12", Name = "Renovated Ranch" }));

IndexDocumentsOptions options = new IndexDocumentsOptions { ThrowOnAnyError = true };
client.IndexDocuments(batch, options);
```

The request will throw `IndexBatchException` by default if any of the individual actions fail, and you can use 
`findFailedActionsToRetry` to retry on failed documents.
There's also a `ThrowOnAnyError` option, and you can set it to `false` to get a successful response
with an `IndexDocumentsResult` for inspection.


### Async APIs

All of the examples so far have been using synchronous APIs, but we provide full
support for async APIs as well. You'll need to use [SearchAsyncClient](#Create-a-SearchClient)

<!-- embedme ./src/samples/java/com/azure/search/documents/ReadmeSamples.java#L134-L144 -->
```C# Snippet:Azure_Search_Tests_Samples_Readme_StaticQueryAsync
SearchResults<Hotel> response = await client.SearchAsync<Hotel>("luxury");
await foreach (SearchResult<Hotel> result in response.GetResultsAsync())
{
    Hotel doc = result.Document;
    Console.WriteLine($"{doc.Id}: {doc.Name}");
}
```

- Samples are explained in detail [here][samples_readme].

## Troubleshooting

### General

When you interact with Azure Cognitive Search using this Java client library, errors returned by the service correspond 
to the same HTTP status codes returned for [REST API][rest_api] requests. For example, if you try to retrieve a document 
that doesn't exist in your index, a `404` error is returned, indicating `Not Found`.

### Handling Search Error Response

Any Search API operation that fails will throw a
[`HttpResponseException`][HttpResponseException] with
helpful [`Status codes`][status_codes].  Many of these errors are recoverable.

<!-- embedme ./src/samples/java/com/azure/search/documents/ReadmeSamples.java#L134-L144 -->
```
try
{
    return client.GetDocument<Hotel>("12345");
}
catch (RequestFailedException ex) when (ex.Status == 404)
{
    Console.WriteLine("We couldn't find the hotel you are looking for!");
    Console.WriteLine("Please try selecting another.");
    return null;
}
```

You can also easily [enable console logging][logging] if you want to dig
deeper into the requests you're making against the service.

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

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fsearch%2Fazure-search-documents%2FREADME.png)

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
[create_search_service_docs]: https://docs.microsoft.com/azure/search/search-create-service-portal
[create_search_service_ps]: https://docs.microsoft.com/azure/search/search-manage-powershell#create-or-delete-a-service
[create_search_service_cli]: https://docs.microsoft.com/cli/azure/search/service?view=azure-cli-latest#az-search-service-create
[HttpResponseException]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/src/main/java/com/azure/core/exception/HttpResponseException.java
[status_codes]: https://docs.microsoft.com/rest/api/searchservice/http-status-codes

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fsearch%2Fazure-search-documents%2FREADME.png)
