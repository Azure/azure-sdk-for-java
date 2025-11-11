// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p><a href="https://learn.microsoft.com/azure/search/">Azure AI Search</a>, formerly known as "Azure AI Search", provides secure information retrieval at scale over
 * user-owned content in traditional and conversational search applications.</p>
 *
 * <p>The Azure AI Search service provides:</p>
 *
 * <ul>
 *     <li>A search engine for vector search, full text, and hybrid search over a search index.</li>
 *     <li>Rich indexing with integrated data chunking and vectorization (preview), lexical analysis for text, and
 *     optional AI enrichment for content extraction and transformation.</li>
 *     <li>Rich query syntax for vector queries, text search, hybrid queries, fuzzy search, autocomplete, geo-search and others.</li>
 *     <li>Azure scale, security, and reach.</li>
 *     <li>Azure integration at the data layer, machine learning layer, Azure AI services and Azure OpenAI</li>
 * </ul>
 *
 * <p>The Azure AI Search service is well suited for the following application scenarios:</p>
 *
 * <ul>
 *     <li>Consolidate varied content types into a single searchable index. To populate an index, you can push JSON
 *     documents that contain your content, or if your data is already in Azure, create an indexer to pull in data
 *     automatically.</li>
 *     <li>Attach skillsets to an indexer to create searchable content from images and large text documents. A skillset
 *     leverages AI from Cognitive Services for built-in OCR, entity recognition, key phrase extraction, language
 *     detection, text translation, and sentiment analysis. You can also add custom skills to integrate external
 *     processing of your content during data ingestion.</li>
 *     <li>In a search client application, implement query logic and user experiences similar to commercial web search engines.</li>
 * </ul>
 *
 * <p>This is the Java client library for Azure AI Search. Azure AI Search service is a search-as-a-service
 * cloud solution that gives developers APIs and tools for adding a rich search experience over private, heterogeneous
 * content in web, mobile, and enterprise applications.</p>
 *
 * <p>The Azure Search Documents client library allows for Java developers to easily interact with the Azure AI Search
 * service from their Java applications. This library provides a set of APIs that abstract the low-level details of working
 * with the Azure AI Search service and allows developers to perform common operations such as:</p>
 *
 * <ul>
 *     <li>Submit queries for simple and advanced query forms that include fuzzy search, wildcard search, regular expressions..</li>
 *     <li>Implement filtered queries for faceted navigation, geospatial search, or to narrow results based on filter criteria.</li>
 *     <li>Create and manage search indexes.</li>
 *     <li>Upload and update documents in the search index.</li>
 *     <li>Create and manage indexers that pull data from Azure into an index.</li>
 *     <li>Create and manage skillsets that add AI enrichment to data ingestion.</li>
 *     <li>Create and manage analyzers for advanced text analysis or multi-lingual content.</li>
 *     <li>Optimize results through scoring profiles to factor in business logic or freshness.</li>
 * </ul>
 *
 * <h2>Getting Started</h2>
 *
 * <h3>Prerequisites</h3>
 *
 * <p>The client library package requires the following:</p>
 *
 * <ul>
 *     <li>Java Development Kit (JDK) 8 or later</li>
 *     <li><a href="https://azure.microsoft.com/free/java/">An Azure subscription</a></li>
 *     <li><a href="https://azure.microsoft.com/products/ai-services/ai-search/">An existing Azure AI Search service</a></li>
 * </ul>
 *
 *<p><em>To create a new Search service, you can use the
 * <a href="https://learn.microsoft.com/azure/search/search-create-service-portal">Azure portal</a>,
 * <a href="https://learn.microsoft.com/azure/search/search-manage-powershell#create-or-delete-a-service">Azure Powershell</a>,
 * or the <a href="https://learn.microsoft.com/cli/azure/search/service?view=azure-cli-latest#az-search-service-create">Azure CLI.</a></em></p>
 *
 *
 * <h3>Authenticate the client</h3>
 *
 * <p>To interact with the Search service, you'll need to create an instance of the appropriate client class:
 * SearchClient for searching indexed documents, SearchIndexClient for managing indexes, or SearchIndexerClient for
 * crawling data sources and loading search documents into an index. To instantiate a client object, you'll need an
 * endpoint and API key. You can refer to the documentation for more information on
 * <a href="https://learn.microsoft.com/azure/search/search-security-overview#authentication">supported authenticating approaches</a>
 * with the Search service.</p>
 *
 * <h4>Get an API Key</h4>
 *
 * <p>You can get the endpoint and an API key from the Search service in the <a href="https://ms.portal.azure.com/">Azure Portal.</a>
 * Please refer <a href="https://learn.microsoft.com/azure/search/search-security-api-keys?tabs=portal-use%2Cportal-find%2Cportal-query">the
 * documentation</a> for instructions on how to get an API key.</p>
 *
 * <p>The SDK provides three clients.</p>
 *
 * <ul>
 *     <li>SearchIndexClient for CRUD operations on indexes and synonym maps.</li>
 *     <li>SearchIndexerClient for CRUD operations on indexers, data sources, and skillsets.</li>
 *     <li>SearchClient for all document operations.</li>
 * </ul>
 *
 * <h3>Create a SearchIndexClient</h3>
 *
 * <p>To create a SearchIndexClient, you will need the values of the Azure AI Search service URL endpoint and
 * admin key. The following snippet shows how to create a SearchIndexClient.</p>
 *
 * The following sample creates a SearchIndexClient using the endpoint and Azure Key Credential (API Key).
 *
 * <!-- src_embed com.azure.search.documents.packageInfo-SearchIndexClient.instantiation -->
 * <pre>
 * SearchIndexClient searchIndexClient = new SearchIndexClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.packageInfo-SearchIndexClient.instantiation -->
 *
 * <h3>Create a SearchIndexerClient</h3>
 *
 * <p>To create a SearchIndexerClient, you will need the values of the Azure AI Search
 * service URL endpoint and admin key. The following snippet shows how to create a SearchIndexerClient.</p>
 *
 * <p>The following sample creates SearchIndexerClient using an endpoint and Azure Key Credential (API Key).</p>
 *
 * <!-- src_embed com.azure.search.documents.packageInfo-SearchIndexerClient.instantiation -->
 * <pre>
 * SearchIndexerClient searchIndexerClient = new SearchIndexerClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.packageInfo-SearchIndexerClient.instantiation -->
 *
 *
 * <h3>Create a SearchClient</h3>
 *
 * <p>To create a SearchClient, you will need the values of the Azure AI Search
 * service URL endpoint, admin key, and an index name. The following snippet shows how to create a SearchIndexerClient.</p>
 *
 * <p>The following sample creates a SearchClient</p>
 *
 * <!-- src_embed com.azure.search.documents.packageInfo-SearchClient.instantiation -->
 * <pre>
 * SearchClient searchClient = new SearchClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .indexName&#40;&quot;&#123;indexName&#125;&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.packageInfo-SearchClient.instantiation -->
 *
 * <h2>Key Concepts</h2>
 *
 * <p>An Azure AI Search service contains one or more indexes that provide persistent storage of searchable data
 * in the form of JSON documents. (If you're new to search, you can make a very rough analogy between indexes and
 * database tables.) The azure-search-documents client library exposes operations on these resources through two main
 * client types.</p>
 *
 * <p>SearchClient helps with:</p>
 * <ul>
 *     <li><a href="https://learn.microsoft.com/azure/search/search-lucene-query-architecture">Searching</a>
 *         your indexed documents using <a href="https://learn.microsoft.com/azure/search/search-query-overview">rich queries</a>
 *         and <a href="https://learn.microsoft.com/azure/search/search-filters">powerful data shaping.</a></li>
 *     <li><a href="https://learn.microsoft.com/rest/api/searchservice/autocomplete">Autocompleting</a> partially typed search terms based on documents in the index.</li>
 *     <li><a href="https://learn.microsoft.com/rest/api/searchservice/suggestions">Suggesting</a> the most likely matching text in documents as a user types.</li>
 *     <li><a href="https://learn.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Adding, Updating or Deleting</a> documents from an index.</li>
 * </ul>
 * <p>SearchIndexClient allows you to:</p>
 * <ul>
 *      <li>Create, delete, update, or configure a search index</li>
 *      <li>Declare custom synonym maps to expand or rewrite queries</li>
 *      <li>Most of the SearchServiceClient functionality is not yet available in our current preview</li>
 * </ul>
 * <p>SearchIndexerClient allows you to:</p>
 * <ul>
 *      <li>Start indexers to automatically crawl data sources</li>
 *      <li>Define AI powered Skillsets to transform and enrich your data</li>
 * </ul>
 *
 * <p>Azure AI Search provides two powerful features:</p>
 *
 * <h3>Semantic Search</h3>
 *
 * <p>Semantic search enhances the quality of search results for text-based queries. By enabling Semantic Search on
 * your search service, you can improve the relevance of search results in two ways:</p>
 *
 * <ul>
 *     <li>It applies secondary ranking to the initial result set, promoting the most semantically relevant results to the top.</li>
 *     <li>It extracts and returns captions and answers in the response, which can be displayed on a search page to enhance the user's search experience.</li>
 * </ul>
 *
 * <p>To learn more about Semantic Search, you can refer to the documentation.</p>
 *
 * <h3>Vector Search</h3>
 *
 * <p>Vector Search is an information retrieval technique that overcomes the limitations of traditional keyword-based
 * search. Instead of relying solely on lexical analysis and matching individual query terms, Vector Search utilizes
 * machine learning models to capture the contextual meaning of words and phrases. It represents documents and queries
 * as vectors in a high-dimensional space called an embedding. By understanding the intent behind the query,
 * Vector Search can deliver more relevant results that align with the user's requirements, even if the exact terms are
 * not present in the document. Moreover, Vector Search can be applied to various types of content, including images
 * and videos, not just text.</p>
 *
 * <p>To learn how to index vector fields and perform vector search, you can refer to the <a href="https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/VectorSearchExample.java">sample</a>.
 * This sample provides detailed guidance on indexing vector fields and demonstrates how to perform vector search.</p>
 *
 * <p>Additionally, for more comprehensive information about Vector Search, including its concepts and usage, you can
 * refer to the <a href="https://learn.microsoft.com/azure/search/vector-search-overview">documentation</a>. The documentation provides in-depth explanations and guidance on leveraging the power of
 * Vector Search in Azure AI Search.</p>
 *
 * <h3>Examples</h3>
 *
 * <p>The following examples all use a sample<a href="https://github.com/Azure-Samples/azure-search-sample-data"> Hotel data set</a> that you can <a href="https://learn.microsoft.com/azure/search/search-get-started-portal#step-1---start-the-import-data-wizard-and-create-a-data-source">import into your own index from the Azure
 * portal</a>. These are just a few of the basics - please <a href="https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/README.md">check out our Samples</a> for much more.</p>
 *
 * <h4>Querying</h4>
 *
 * <p>There are two ways to interact with the data returned from a search query.</p>
 *
 * <h5>Use SearchDocument like a dictionary for search results</h5>
 *
 * <p>SearchDocument is the default type returned from queries when you don't provide your own. The following sample performs the
 * search, enumerates over the results, and extracts data using SearchDocument's dictionary indexer.</p>
 *
 * <!-- src_embed com.azure.search.documents.packageInfo-SearchClient.search#String -->
 * <pre>
 * for &#40;SearchResult result : searchClient.search&#40;&quot;luxury&quot;&#41;&#41; &#123;
 *     SearchDocument document = result.getDocument&#40;SearchDocument.class&#41;;
 *     System.out.printf&#40;&quot;Hotel ID: %s%n&quot;, document.get&#40;&quot;hotelId&quot;&#41;&#41;;
 *     System.out.printf&#40;&quot;Hotel Name: %s%n&quot;, document.get&#40;&quot;hotelName&quot;&#41;&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.search.documents.packageInfo-SearchClient.search#String -->
 *
 * <h5>Use Java model class for search results</h5>
 *
 * <p>Define a `Hotel` class.</p>
 *
 * <!-- src_embed hotelExampleClass -->
 * <pre>
 * public static class Hotel &#123;
 *     private String hotelId;
 *     private String hotelName;
 *
 *     &#64;SimpleField&#40;isKey = true&#41;
 *     public String getHotelId&#40;&#41; &#123;
 *         return this.hotelId;
 *     &#125;
 *
 *     public String getHotelName&#40;&#41; &#123;
 *         return this.hotelName;
 *     &#125;
 *
 *     public Hotel setHotelId&#40;String number&#41; &#123;
 *         this.hotelId = number;
 *         return this;
 *     &#125;
 *
 *     public Hotel setHotelName&#40;String secretPointMotel&#41; &#123;
 *         this.hotelName = secretPointMotel;
 *         return this;
 *     &#125;
 * &#125;
 * </pre>
 * <!-- end hotelExampleClass -->
 *
 * <p>Use it in place of SearchDocument when querying.</p>
 *
 * <!-- src_embed com.azure.search.documents.packageInfo-SearchClient.search#String-Object-Class-Method -->
 * <pre>
 * for &#40;SearchResult result : searchClient.search&#40;&quot;luxury&quot;&#41;&#41; &#123;
 *     Hotel hotel = result.getDocument&#40;Hotel.class&#41;;
 *     System.out.printf&#40;&quot;Hotel ID: %s%n&quot;, hotel.getHotelId&#40;&#41;&#41;;
 *     System.out.printf&#40;&quot;Hotel Name: %s%n&quot;, hotel.getHotelName&#40;&#41;&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.search.documents.packageInfo-SearchClient.search#String-Object-Class-Method -->
 *
 * <h5>Search Options</h5>
 *
 * <p>The SearchOptions provide powerful control over the behavior of our queries.</p>
 *
 * <p>The following sample uses SearchOptions to search for the top 5 luxury hotel with a good rating (4 or above).</p>
 *
 * <!-- src_embed com.azure.search.documents.packageInfo-SearchClient.search#SearchOptions -->
 * <pre>
 * SearchOptions options = new SearchOptions&#40;&#41;
 *     .setFilter&#40;&quot;rating gt 4&quot;&#41;
 *     .setOrderBy&#40;&quot;rating desc&quot;&#41;
 *     .setTop&#40;5&#41;;
 * SearchPagedIterable searchResultsIterable = searchClient.search&#40;&quot;luxury&quot;, options, Context.NONE&#41;;
 * searchResultsIterable.forEach&#40;result -&gt; &#123;
 *     System.out.printf&#40;&quot;Hotel ID: %s%n&quot;, result.getDocument&#40;Hotel.class&#41;.getHotelId&#40;&#41;&#41;;
 *     System.out.printf&#40;&quot;Hotel Name: %s%n&quot;, result.getDocument&#40;Hotel.class&#41;.getHotelName&#40;&#41;&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.packageInfo-SearchClient.search#SearchOptions -->
 *
 * <h4>Creating an index</h4>
 *
 * <p>You can use the SearchIndexClient to create a search index. Indexes can also define suggesters, lexical analyzers,
 * and more.</p>
 *
 * <p>There are multiple ways of preparing search fields for a search index. For basic needs, there is a static helper
 * method buildSearchFields in SearchIndexClient and SearchIndexAsyncClient. There are three annotations
 * SimpleFieldProperty, SearchFieldProperty and FieldBuilderIgnore to configure the field of model class.</p>
 *
 * <!-- src_embed com.azure.search.documents.packageInfo-SearchIndexClient.createIndex#SearchIndex -->
 * <pre>
 * &#47;&#47; Create a new search index structure that matches the properties of the Hotel class.
 * List&lt;SearchField&gt; searchFields = SearchIndexClient.buildSearchFields&#40;Hotel.class, null&#41;;
 * searchIndexClient.createIndex&#40;new SearchIndex&#40;&quot;hotels&quot;, searchFields&#41;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.packageInfo-SearchIndexClient.createIndex#SearchIndex -->
 *
 * <p>For advanced scenarios, you can build search fields using SearchField directly. The following sample shows how to
 * build search fields with SearchField.</p>
 *
 * <!-- src_embed com.azure.search.documents.packageInfo-SearchIndexClient.createIndex#String-List-boolean -->
 * <pre>
 * &#47;&#47; Create a new search index structure that matches the properties of the Hotel class.
 * List&lt;SearchField&gt; searchFieldList = new ArrayList&lt;&gt;&#40;&#41;;
 * searchFieldList.add&#40;new SearchField&#40;&quot;hotelId&quot;, SearchFieldDataType.STRING&#41;
 *         .setKey&#40;true&#41;
 *         .setFilterable&#40;true&#41;
 *         .setSortable&#40;true&#41;&#41;;
 *
 * searchFieldList.add&#40;new SearchField&#40;&quot;hotelName&quot;, SearchFieldDataType.STRING&#41;
 *         .setSearchable&#40;true&#41;
 *         .setFilterable&#40;true&#41;
 *         .setSortable&#40;true&#41;&#41;;
 * searchFieldList.add&#40;new SearchField&#40;&quot;description&quot;, SearchFieldDataType.STRING&#41;
 *     .setSearchable&#40;true&#41;
 *     .setAnalyzerName&#40;LexicalAnalyzerName.EU_LUCENE&#41;&#41;;
 * searchFieldList.add&#40;new SearchField&#40;&quot;tags&quot;, SearchFieldDataType.collection&#40;SearchFieldDataType.STRING&#41;&#41;
 *     .setSearchable&#40;true&#41;
 *     .setFilterable&#40;true&#41;
 *     .setFacetable&#40;true&#41;&#41;;
 * searchFieldList.add&#40;new SearchField&#40;&quot;address&quot;, SearchFieldDataType.COMPLEX&#41;
 *     .setFields&#40;new SearchField&#40;&quot;streetAddress&quot;, SearchFieldDataType.STRING&#41;.setSearchable&#40;true&#41;,
 *         new SearchField&#40;&quot;city&quot;, SearchFieldDataType.STRING&#41;
 *             .setSearchable&#40;true&#41;
 *             .setFilterable&#40;true&#41;
 *             .setFacetable&#40;true&#41;
 *             .setSortable&#40;true&#41;,
 *         new SearchField&#40;&quot;stateProvince&quot;, SearchFieldDataType.STRING&#41;
 *             .setSearchable&#40;true&#41;
 *             .setFilterable&#40;true&#41;
 *             .setFacetable&#40;true&#41;
 *             .setSortable&#40;true&#41;,
 *         new SearchField&#40;&quot;country&quot;, SearchFieldDataType.STRING&#41;
 *             .setSearchable&#40;true&#41;
 *             .setFilterable&#40;true&#41;
 *             .setFacetable&#40;true&#41;
 *             .setSortable&#40;true&#41;,
 *         new SearchField&#40;&quot;postalCode&quot;, SearchFieldDataType.STRING&#41;
 *             .setSearchable&#40;true&#41;
 *             .setFilterable&#40;true&#41;
 *             .setFacetable&#40;true&#41;
 *             .setSortable&#40;true&#41;
 *     &#41;&#41;;
 *
 * &#47;&#47; Prepare suggester.
 * SearchSuggester suggester = new SearchSuggester&#40;&quot;sg&quot;, Collections.singletonList&#40;&quot;hotelName&quot;&#41;&#41;;
 * &#47;&#47; Prepare SearchIndex with index name and search fields.
 * SearchIndex index = new SearchIndex&#40;&quot;hotels&quot;&#41;.setFields&#40;searchFieldList&#41;.setSuggesters&#40;suggester&#41;;
 * &#47;&#47; Create an index
 * searchIndexClient.createIndex&#40;index&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.packageInfo-SearchIndexClient.createIndex#String-List-boolean -->
 *
 * <h4>Retrieving a specific document from your index</h4>
 *
 *<p>In addition to querying for documents using keywords and optional filters, you can retrieve a specific document from your index if you already know the key.</p>
 *
 * <p>The following example retrieves a document using the document's key.</p>
 *
 * <!-- src_embed com.azure.search.documents.packageInfo-SearchClient.getDocument#String-String -->
 * <pre>
 * Hotel hotel = searchClient.getDocument&#40;&quot;1&quot;, Hotel.class&#41;;
 * System.out.printf&#40;&quot;Hotel ID: %s%n&quot;, hotel.getHotelId&#40;&#41;&#41;;
 * System.out.printf&#40;&quot;Hotel Name: %s%n&quot;, hotel.getHotelName&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.packageInfo-SearchClient.getDocument#String-String -->
 *
 * <h4>Adding documents to your index</h4>
 *
 * <p>You can Upload, Merge, MergeOrUpload, and Delete multiple documents from an index in a single batched request.
 * There are <a href="https://learn.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents#document-actions">a few special rules for merging</a> to be aware of.</p>
 *
 * <p>The following sample shows using a single batch request to perform a document upload and merge in a single request.</p>
 *
 * <!-- src_embed com.azure.search.documents.packageInfo-SearchClient.uploadDocuments#Iterable-boolean-boolean -->
 * <pre>
 * IndexDocumentsBatch&lt;Hotel&gt; batch = new IndexDocumentsBatch&lt;Hotel&gt;&#40;&#41;;
 * batch.addUploadActions&#40;Collections.singletonList&#40;
 *         new Hotel&#40;&#41;.setHotelId&#40;&quot;783&quot;&#41;.setHotelName&#40;&quot;Upload Inn&quot;&#41;&#41;&#41;;
 * batch.addMergeActions&#40;Collections.singletonList&#40;
 *         new Hotel&#40;&#41;.setHotelId&#40;&quot;12&quot;&#41;.setHotelName&#40;&quot;Renovated Ranch&quot;&#41;&#41;&#41;;
 * searchClient.indexDocuments&#40;batch&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.packageInfo-SearchClient.uploadDocuments#Iterable-boolean-boolean -->
 *
 * <h4>Async APIs</h4>
 *
 * <p>The examples so far have been using synchronous APIs. For asynchronous support and examples, please see our asynchronous clients:</p>
 *
 * <ul>
 *     <li>SearchIndexAsyncClient</li>
 *     <li>SearchIndexerAsyncClient</li>
 *     <li>SearchAsyncClient</li>
 * </ul>
 *
 * <h3>Authenticate in a National Cloud</h3>
 *
 * <p>To authenticate a <a href="https://learn.microsoft.com/azure/active-directory/develop/authentication-national-cloud">National Cloud</a>, you will need to make the following additions to your client configuration:</p>
 *
 * <ul>
 *     <li>Set `AuthorityHost` in the credential potions or via the `AZURE_AUTHORITY_HOST` environment variable</li>
 *     <li>Set the `audience` in SearchClientBuilder, SearchIndexClientBuilder, SearchIndexerClientBuilder</li>
 * </ul>
 *
 * <!-- src_embed com.azure.search.documents.packageInfo-SearchClient.instantiation.nationalCloud -->
 * <pre>
 * SearchClient searchClient = new SearchClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;
 *         .authorityHost&#40;&quot;&#123;national cloud endpoint&#125;&quot;&#41;
 *         .build&#40;&#41;&#41;
 *     .audience&#40;SearchAudience.AZURE_PUBLIC_CLOUD&#41; &#47;&#47;set the audience of your cloud
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.packageInfo-SearchClient.instantiation.nationalCloud -->
 *
 * <h3>Troubleshooting</h3>
 *
 * <p>See our <a href="https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/TROUBLESHOOTING.md">troubleshooting guide</a> for details on how to diagnose various failure scenarios.</p>
 *
 * <h4>General</h4>
 *
 * <p>When you interact with Azure AI Search using this Java client library, errors returned by the service
 * correspond to the <a href="https://learn.microsoft.com/rest/api/searchservice/http-status-codes">same HTTP status codes returned for REST API requests.</a> For example, the service will return a 404
 * error if you try to retrieve a document that doesn't exist in your index.</p>
 *
 * <h4>Handling Search Error Response</h4>
 *
 * <p>Any Search API operation that fails will throw an HttpResponseException with helpful <a href="https://learn.microsoft.com/rest/api/searchservice/http-status-codes">Status codes</a>. Many of these errors are recoverable.</p>
 *
 * <!-- src_embed com.azure.search.documents.packageInfo-SearchClient.search#String-Object-Class-Error -->
 * <pre>
 * try &#123;
 *     Iterable&lt;SearchResult&gt; results = searchClient.search&#40;&quot;hotel&quot;&#41;;
 *     results.forEach&#40;result -&gt; &#123;
 *         System.out.println&#40;result.getDocument&#40;Hotel.class&#41;.getHotelName&#40;&#41;&#41;;
 *     &#125;&#41;;
 * &#125; catch &#40;HttpResponseException ex&#41; &#123;
 *     &#47;&#47; The exception contains the HTTP status code and the detailed message
 *     &#47;&#47; returned from the search service
 *     HttpResponse response = ex.getResponse&#40;&#41;;
 *     System.out.println&#40;&quot;Status Code: &quot; + response.getStatusCode&#40;&#41;&#41;;
 *     System.out.println&#40;&quot;Message: &quot; + ex.getMessage&#40;&#41;&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.search.documents.packageInfo-SearchClient.search#String-Object-Class-Error -->
 *
 *
 * @see com.azure.search.documents.SearchClient
 * @see com.azure.search.documents.SearchAsyncClient
 * @see com.azure.search.documents.SearchClientBuilder
 * @see com.azure.search.documents.indexes.SearchIndexClient
 * @see com.azure.search.documents.indexes.SearchIndexAsyncClient
 * @see com.azure.search.documents.indexes.SearchIndexClientBuilder
 * @see com.azure.search.documents.indexes.SearchIndexerClient
 * @see com.azure.search.documents.indexes.SearchIndexerAsyncClient
 * @see com.azure.search.documents.indexes.SearchIndexerClientBuilder
 * @see com.azure.search.documents.models.SearchOptions
 * @see com.azure.search.documents.indexes.models.SearchField
 *
 */
package com.azure.search.documents;
