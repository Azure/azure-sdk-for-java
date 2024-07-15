# Azure AI Search client library for Java

This is the Java client library for [Azure AI Search](https://docs.microsoft.com/azure/search/) (formerly known as "Azure Cognitive Search"). Azure AI Search service is an AI-powered information retrieval platform that helps developers build rich search experiences and generative AI apps that combine large language models with enterprise data.

Azure AI Search is well suited for the following application scenarios:

* Consolidate varied content types into a single searchable index.
  To populate an index, you can push JSON documents that contain your content,
  or if your data is already in Azure, create an indexer to pull in data
  automatically.
* Attach skillsets to an indexer to create searchable content from images
  and unstructured documents. A skillset leverages APIs from Azure AI Services
  for built-in OCR, entity recognition, key phrase extraction, language
  detection, text translation, and sentiment analysis. You can also add
  custom skills to integrate external processing of your content during
  data ingestion.
* In a search client application, implement query logic and user experiences
  similar to commercial web search engines and chat-style apps.

Use the Azure AI Search client library to:

* Submit queries using vector, keyword, and hybrid query forms.
* Implement filtered queries for metadata, geospatial search, faceted navigation, 
  or to narrow results based on filter criteria.
* Create and manage search indexes.
* Upload and update documents in the search index.
* Create and manage indexers that pull data from Azure into an index.
* Create and manage skillsets that add AI enrichment to data ingestion.
* Create and manage analyzers for advanced text analysis or multi-lingual content.
* Optimize results through semantic ranking and scoring profiles to factor in business logic or freshness.

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_documentation]| [Product documentation][search_docs] | [Samples][samples]

## Getting started

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
    <version>11.7.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Prerequisites

* [Java Development Kit (JDK) with version 8 or above][jdk]
  * Here are details about [Java 8 client compatibility with Azure Certificate Authority](https://learn.microsoft.com/azure/security/fundamentals/azure-ca-details?tabs=root-and-subordinate-cas-list#client-compatibility-for-public-pkis).
* [Azure subscription][azure_subscription]
* [Azure AI Search service][search]
* To create a new search service, you can use the [Azure portal][create_search_service_docs],
[Azure PowerShell][create_search_service_ps], or the [Azure CLI][create_search_service_cli].
Here's an example using the Azure CLI to create a free instance for getting started:

```bash
az search service create --name <mysearch> --resource-group <mysearch-rg> --sku free --location westus
```

See [choosing a pricing tier](https://docs.microsoft.com/azure/search/search-sku-tier) for more information about available options.

### Authenticate the client

To interact with the Search service, you'll need to create an instance of the appropriate client class: `SearchClient` 
for searching indexed documents, `SearchIndexClient` for managing indexes, or `SearchIndexerClient` for crawling data 
sources and loading search documents into an index. To instantiate a client object, you'll need an **endpoint** and **Azure roles** or an **API key**. You can refer to the documentation for more information on [supported authenticating approaches](https://learn.microsoft.com/azure/search/search-security-overview#authentication) 
with the Search service.

#### Get an API Key

An API key can be an easier approach to start with because it doesn't require pre-existing role assignments.

You can get the **endpoint** and an **API key** from the search service in the [Azure Portal](https://portal.azure.com/). 
Please refer the [documentation](https://docs.microsoft.com/azure/search/search-security-api-keys) for instructions on 
how to get an API key.

Alternatively, you can use the following [Azure CLI](https://learn.microsoft.com/cli/azure/) command to retrieve the 
API key from the search service:

```bash
az search admin-key show --service-name <mysearch> --resource-group <mysearch-rg>
```

**Note:**

* The example Azure CLI snippet above retrieves an admin key. This allows for easier access when exploring APIs,
but it should be managed carefully.
* There are two types of keys used to access your search service: **admin** *(read-write)* and **query** *(read-only)*
keys. Restricting access and operations in client apps is essential to safeguarding the search assets on your service.
Always use a query key rather than an admin key for any query originating from a client app.

The SDK provides three clients.

* `SearchIndexClient` for CRUD operations on indexes and synonym maps.
* `SearchIndexerClient` for CRUD operations on indexers, data sources, and skillsets.
* `SearchClient` for all document operations.

#### Create a SearchIndexClient

To create a `SearchIndexClient/SearchIndexAsyncClient`, you will need the values of the Azure AI Search service
URL endpoint and admin key.

```java readme-sample-createIndexClient
SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
    .endpoint(ENDPOINT)
    .credential(new AzureKeyCredential(API_KEY))
    .buildClient();
```

or

```java readme-sample-createIndexAsyncClient
SearchIndexAsyncClient searchIndexAsyncClient = new SearchIndexClientBuilder()
    .endpoint(ENDPOINT)
    .credential(new AzureKeyCredential(API_KEY))
    .buildAsyncClient();
```

#### Create a SearchIndexerClient

To create a `SearchIndexerClient/SearchIndexerAsyncClient`, you will need the values of the Azure AI Search service
URL endpoint and admin key.

```java readme-sample-createIndexerClient
SearchIndexerClient searchIndexerClient = new SearchIndexerClientBuilder()
    .endpoint(ENDPOINT)
    .credential(new AzureKeyCredential(API_KEY))
    .buildClient();
```

or

```java readme-sample-createIndexerAsyncClient
SearchIndexerAsyncClient searchIndexerAsyncClient = new SearchIndexerClientBuilder()
    .endpoint(ENDPOINT)
    .credential(new AzureKeyCredential(API_KEY))
    .buildAsyncClient();
```

#### Create a SearchClient

Once you have the values of the Azure AI Search service URL endpoint and
admin key, you can create the `SearchClient/SearchAsyncClient` with an existing index name:

```java readme-sample-createSearchClient
SearchClient searchClient = new SearchClientBuilder()
    .endpoint(ENDPOINT)
    .credential(new AzureKeyCredential(ADMIN_KEY))
    .indexName(INDEX_NAME)
    .buildClient();
```

or

```java readme-sample-createAsyncSearchClient
SearchAsyncClient searchAsyncClient = new SearchClientBuilder()
    .endpoint(ENDPOINT)
    .credential(new AzureKeyCredential(ADMIN_KEY))
    .indexName(INDEX_NAME)
    .buildAsyncClient();
```

#### Create a client using Microsoft Entra ID authentication

You can also create a `SearchClient`, `SearchIndexClient`, or `SearchIndexerClient` using Microsoft Entra ID authentication. Your user or service principal must be assigned the "Search Index Data Reader" role.
Using the [DefaultAzureCredential](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#defaultazurecredential) 
you can authenticate a service using Managed Identity or a service principal, authenticate as a developer working on an
application, and more all without changing code. Please refer the [documentation](https://learn.microsoft.com/azure/search/search-security-rbac?tabs=config-svc-portal%2Croles-portal%2Ctest-portal%2Ccustom-role-portal%2Cdisable-keys-portal) 
for instructions on how to connect to Azure AI Search using Azure role-based access control (Azure RBAC).

Before you can use the `DefaultAzureCredential`, or any credential type from [Azure.Identity](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md), 
you'll first need to [install the Azure.Identity package](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#include-the-package).

To use `DefaultAzureCredential` with a client ID and secret, you'll need to set the `AZURE_TENANT_ID`, 
`AZURE_CLIENT_ID`, and `AZURE_CLIENT_SECRET` environment variables; alternatively, you can pass those values
to the `ClientSecretCredential` also in `azure-identity`.

Make sure you use the right namespace for `DefaultAzureCredential` at the top of your source file:

```java
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
```

Then you can create an instance of `DefaultAzureCredential` and pass it to a new instance of your client:

```java readme-sample-searchClientWithTokenCredential
String indexName = "nycjobs";

// Get the service endpoint from the environment
String endpoint = Configuration.getGlobalConfiguration().get("SEARCH_ENDPOINT");
DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

// Create a client
SearchClient client = new SearchClientBuilder()
    .endpoint(endpoint)
    .indexName(indexName)
    .credential(credential)
    .buildClient();
```

### Send your first search query

To get running with Azure AI Search first create an index following this [guide][search-get-started-portal].
With an index created you can use the following samples to begin using the SDK.

## Key concepts

An Azure AI Search service contains one or more indexes that provide persistent storage of searchable data in
the form of JSON documents. _(If you're new to search, you can make a very rough analogy between indexes and database
tables.)_ The `azure-search-documents` client library exposes operations on these resources through two main client types.

* `SearchClient` helps with:
  * [Searching](https://docs.microsoft.com/azure/search/search-lucene-query-architecture)
    your indexed documents using [vector queries](https://learn.microsoft.com/azure/search/vector-search-how-to-query),
    [keyword queries](https://learn.microsoft.com/azure/search/search-query-create)
    and [hybrid queries](https://learn.microsoft.com/azure/search/hybrid-search-how-to-query)
  * [Vector query filters](https://learn.microsoft.com/azure/search/vector-search-filters) and [Text query filters](https://learn.microsoft.com/azure/search/search-filters)
  * [Semantic ranking](https://learn.microsoft.com/azure/search/semantic-how-to-query-request) and [scoring profiles](https://learn.microsoft.com/azure/search/index-add-scoring-profiles) for boosting relevance
  * [Autocompleting](https://docs.microsoft.com/rest/api/searchservice/autocomplete)
    partially typed search terms based on documents in the index
  * [Suggesting](https://docs.microsoft.com/rest/api/searchservice/suggestions)
    the most likely matching text in documents as a user types
  * [Adding, Updating or Deleting Documents](https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents)
    documents from an index

* `SearchIndexClient` allows you to:
  * [Create, delete, update, or configure a search index](https://docs.microsoft.com/rest/api/searchservice/index-operations)
  * [Declare custom synonym maps to expand or rewrite queries](https://docs.microsoft.com/rest/api/searchservice/synonym-map-operations)
  <!-- * Most of the `SearchServiceClient` functionality is not yet available in our current preview -->

* `SearchIndexerClient` allows you to:
  * [Start indexers to automatically crawl data sources](https://docs.microsoft.com/rest/api/searchservice/indexer-operations)
  * [Define AI powered Skillsets to transform and enrich your data](https://docs.microsoft.com/rest/api/searchservice/skillset-operations)

Azure AI Search provides two powerful features:

### Semantic ranking

Semantic ranking enhances the quality of search results for text-based queries. By enabling semantic ranking on your search service, you can improve the relevance of search results in two ways:
- It applies secondary ranking to the initial result set, promoting the most semantically relevant results to the top.
- It extracts and returns captions and answers in the response, which can be displayed on a search page to enhance the user's search experience.

To learn more about semantic ranking, you can refer to the [documentation](https://learn.microsoft.com/azure/search/vector-search-overview).

### Vector Search

**Vector search** is an information retrieval technique that uses numeric representations of searchable documents and query strings. By searching for numeric representations of content that are most similar to the numeric query, vector search can find relevant matches, even if the exact terms of the query are not present in the index. Moreover, vector search can be applied to various types of content, including images and videos and translated text, not just same-language text.

To learn how to index vector fields and perform vector search, you can refer to the [sample](https://github.com/Azure/azure-sdk-for-python/blob/main/sdk/search/azure-search-documents/samples/sample_vector_search.py). This sample provides detailed guidance on indexing vector fields and demonstrates how to perform vector search.

Additionally, for more comprehensive information about vector search, including its concepts and usage, you can refer to the [documentation](https://learn.microsoft.com/azure/search/vector-search-overview). The documentation provides in-depth explanations and guidance on leveraging the power of vector search in Azure AI Search.

## Examples

The following examples all use a simple [Hotel data set](https://github.com/Azure-Samples/azure-search-sample-data)
that you can [import into your own index from the Azure portal.](https://docs.microsoft.com/azure/search/search-get-started-portal#step-1---start-the-import-data-wizard-and-create-a-data-source)
These are just a few of the basics - please [check out our Samples][samples_readme] for much more.

* [Querying](#querying)
  * [Use `SearchDocument` like a dictionary for search results](#use-searchdocument-like-a-dictionary-for-search-results)
  * [Use Java model for search results](#use-java-model-class-for-search-results)
  * [Search Options](#search-options)
* [Creating an index](#creating-an-index)
* [Adding documents to your index](#adding-documents-to-your-index)
* [Retrieving a specific document from your index](#retrieving-a-specific-document-from-your-index)
* [Async APIs](#async-apis)
* [Create a client that can authenticate in a national cloud](#authenticate-in-a-national-cloud)

### Querying

There are two ways to interact with the data returned from a search query.

Let's explore them with a search for a "luxury" hotel.

#### Use `SearchDocument` like a dictionary for search results

`SearchDocument` is the default type returned from queries when you don't provide your own.  Here we perform the search,
enumerate over the results, and extract data using `SearchDocument`'s dictionary indexer.

```java readme-sample-searchWithDynamicType
for (SearchResult searchResult : SEARCH_CLIENT.search("luxury")) {
    SearchDocument doc = searchResult.getDocument(SearchDocument.class);
    String id = (String) doc.get("hotelId");
    String name = (String) doc.get("hotelName");
    System.out.printf("This is hotelId %s, and this is hotel name %s.%n", id, name);
}
```

#### Use Java model class for search results

Define a `Hotel` class.

```java readme-sample-hotelclass
public static class Hotel {
    @SimpleField(isKey = true, isFilterable = true, isSortable = true)
    private String id;
    @SearchableField(isFilterable = true, isSortable = true)
    private String name;

    public String getId() {
        return id;
    }

    public Hotel setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Hotel setName(String name) {
        this.name = name;
        return this;
    }
}
```

Use it in place of `SearchDocument` when querying.

```java readme-sample-searchWithStronglyType
for (SearchResult searchResult : SEARCH_CLIENT.search("luxury")) {
    Hotel doc = searchResult.getDocument(Hotel.class);
    String id = doc.getId();
    String name = doc.getName();
    System.out.printf("This is hotelId %s, and this is hotel name %s.%n", id, name);
}
```

It is recommended, when you know the schema of the search index, to create a Java model class.

#### Search Options

The `SearchOptions` provide powerful control over the behavior of our queries.

Let's search for the top 5 luxury hotels with a good rating.

```java readme-sample-searchWithSearchOptions
SearchOptions options = new SearchOptions()
    .setFilter("rating ge 4")
    .setOrderBy("rating desc")
    .setTop(5);
SearchPagedIterable searchResultsIterable = SEARCH_CLIENT.search("luxury", options, Context.NONE);
// ...
```

### Creating an index

You can use the [`SearchIndexClient`](#create-a-searchindexclient) to create a search index. Indexes can also define
suggesters, lexical analyzers, and more.

There are multiple ways of preparing search fields for a search index. For basic needs, we provide a static helper method
`buildSearchFields` in `SearchIndexClient` and `SearchIndexAsyncClient`, which can convert Java POJO class into
`List<SearchField>`. There are three annotations `SimpleFieldProperty`, `SearchFieldProperty` and `FieldBuilderIgnore`
to configure the field of model class.

```java readme-sample-createIndexUseFieldBuilder
List<SearchField> searchFields = SearchIndexClient.buildSearchFields(Hotel.class, null);
SEARCH_INDEX_CLIENT.createIndex(new SearchIndex("index", searchFields));
```

For advanced scenarios, we can build search fields using `SearchField` directly.

```java readme-sample-createIndex
List<SearchField> searchFieldList = new ArrayList<>();
searchFieldList.add(new SearchField("hotelId", SearchFieldDataType.STRING)
    .setKey(true)
    .setFilterable(true)
    .setSortable(true));

searchFieldList.add(new SearchField("hotelName", SearchFieldDataType.STRING)
    .setSearchable(true)
    .setFilterable(true)
    .setSortable(true));
searchFieldList.add(new SearchField("description", SearchFieldDataType.STRING)
    .setSearchable(true)
    .setAnalyzerName(LexicalAnalyzerName.EU_LUCENE));
searchFieldList.add(new SearchField("tags", SearchFieldDataType.collection(SearchFieldDataType.STRING))
    .setSearchable(true)
    .setFilterable(true)
    .setFacetable(true));
searchFieldList.add(new SearchField("address", SearchFieldDataType.COMPLEX)
    .setFields(new SearchField("streetAddress", SearchFieldDataType.STRING).setSearchable(true),
        new SearchField("city", SearchFieldDataType.STRING)
            .setSearchable(true)
            .setFilterable(true)
            .setFacetable(true)
            .setSortable(true),
        new SearchField("stateProvince", SearchFieldDataType.STRING)
            .setSearchable(true)
            .setFilterable(true)
            .setFacetable(true)
            .setSortable(true),
        new SearchField("country", SearchFieldDataType.STRING)
            .setSearchable(true)
            .setFilterable(true)
            .setFacetable(true)
            .setSortable(true),
        new SearchField("postalCode", SearchFieldDataType.STRING)
            .setSearchable(true)
            .setFilterable(true)
            .setFacetable(true)
            .setSortable(true)
    ));

// Prepare suggester.
SearchSuggester suggester = new SearchSuggester("sg", Collections.singletonList("hotelName"));
// Prepare SearchIndex with index name and search fields.
SearchIndex index = new SearchIndex("hotels").setFields(searchFieldList).setSuggesters(suggester);
// Create an index
SEARCH_INDEX_CLIENT.createIndex(index);
```

### Retrieving a specific document from your index

In addition to querying for documents using keywords and optional filters, you can retrieve a specific document from
your index if you already know the key. You could get the key from a query, for example, and want to show more
information about it or navigate your customer to that document.

```java readme-sample-retrieveDocuments
Hotel hotel = SEARCH_CLIENT.getDocument("1", Hotel.class);
System.out.printf("This is hotelId %s, and this is hotel name %s.%n", hotel.getId(), hotel.getName());
```

### Adding documents to your index

You can `Upload`, `Merge`, `MergeOrUpload`, and `Delete` multiple documents from an index in a single batched request.
There are [a few special rules for merging](https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents#document-actions)
to be aware of.

```java readme-sample-batchDocumentsOperations
IndexDocumentsBatch<Hotel> batch = new IndexDocumentsBatch<>();
batch.addUploadActions(Collections.singletonList(new Hotel().setId("783").setName("Upload Inn")));
batch.addMergeActions(Collections.singletonList(new Hotel().setId("12").setName("Renovated Ranch")));
SEARCH_CLIENT.indexDocuments(batch);
```

The request will throw `IndexBatchException` by default if any of the individual actions fail, and you can use
`findFailedActionsToRetry` to retry on failed documents. There's also a `throwOnAnyError` option, and you can set it
to `false` to get a successful response with an `IndexDocumentsResult` for inspection.

### Async APIs

The examples so far have been using synchronous APIs, but we provide full support for async APIs as well. You'll need
to use [SearchAsyncClient](#create-a-searchclient).

```java readme-sample-searchWithAsyncClient
SEARCH_ASYNC_CLIENT.search("luxury")
    .subscribe(result -> {
        Hotel hotel = result.getDocument(Hotel.class);
        System.out.printf("This is hotelId %s, and this is hotel name %s.%n", hotel.getId(), hotel.getName());
    });
```

### Authenticate in a National Cloud

To authenticate in a [National Cloud](https://docs.microsoft.com/azure/active-directory/develop/authentication-national-cloud), you will need to make the following additions to your client configuration:

- Set the `AuthorityHost` in the credential options or via the `AZURE_AUTHORITY_HOST` environment variable
- Set the `audience` in `SearchClientBuilder`, `SearchIndexClientBuilder`, or `SearchIndexerClientBuilder`

```java readme-sample-nationalCloud
// Create a SearchClient that will authenticate through AAD in the China national cloud.
SearchClient searchClient = new SearchClientBuilder()
    .endpoint(ENDPOINT)
    .indexName(INDEX_NAME)
    .credential(new DefaultAzureCredentialBuilder()
        .authorityHost(AzureAuthorityHosts.AZURE_CHINA)
        .build())
    .audience(SearchAudience.AZURE_CHINA)
    .buildClient();
```

## Troubleshooting

See our [troubleshooting guide](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/TROUBLESHOOTING.md) 
for details on how to diagnose various failure scenarios.

### General

When you interact with Azure AI Search using this Java client library, errors returned by the service correspond
to the same HTTP status codes returned for [REST API][rest_api] requests. For example, the service will return a `404`
error if you try to retrieve a document that doesn't exist in your index.

### Handling Search Error Response

Any Search API operation that fails will throw an [`HttpResponseException`][HttpResponseException] with helpful
[`Status codes`][status_codes]. Many of these errors are recoverable.

```java readme-sample-handleErrorsWithSyncClient
try {
    Iterable<SearchResult> results = SEARCH_CLIENT.search("hotel");
} catch (HttpResponseException ex) {
    // The exception contains the HTTP status code and the detailed message
    // returned from the search service
    HttpResponse response = ex.getResponse();
    System.out.println("Status Code: " + response.getStatusCode());
    System.out.println("Message: " + ex.getMessage());
}
```

You can also easily [enable console logging][logging] if you want to dig deeper into the requests you're making against
the service.

### Enabling Logging

Azure SDKs for Java provide a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Default HTTP Client

By default, a Netty based HTTP client will be used. The [HTTP clients wiki](https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients)
provides more information on configuring or changing the HTTP client.

## Next steps

* Samples are explained in detail [here][samples_readme].
* Read more about the [Azure AI Search service](https://docs.microsoft.com/azure/search/search-what-is-azure-search)

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a
[Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights
to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate
the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to
do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the
[Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[jdk]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[api_documentation]: https://azure.github.io/azure-sdk-for-java/search.html
[search]: https://azure.microsoft.com/services/search/
[search_docs]: https://docs.microsoft.com/azure/search/
[azure_subscription]: https://azure.microsoft.com/free/java
[maven]: https://maven.apache.org/
[package]: https://central.sonatype.com/artifact/com.azure/azure-search-documents
[samples]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/search/azure-search-documents/src/samples/
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/README.md
[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/search/azure-search-documents/src
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[add_headers_from_context_policy]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/http/policy/AddHeadersFromContextPolicy.java
[rest_api]: https://docs.microsoft.com/rest/api/searchservice/http-status-codes
[create_search_service_docs]: https://docs.microsoft.com/azure/search/search-create-service-portal
[create_search_service_ps]: https://docs.microsoft.com/azure/search/search-manage-powershell#create-or-delete-a-service
[create_search_service_cli]: https://docs.microsoft.com/cli/azure/search/service?view=azure-cli-latest#az-search-service-create
[HttpResponseException]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/exception/HttpResponseException.java
[status_codes]: https://docs.microsoft.com/rest/api/searchservice/http-status-codes
[search-get-started-portal]: https://docs.microsoft.com/azure/search/search-get-started-portal

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fsearch%2Fazure-search-documents%2FREADME.png)
