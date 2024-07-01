// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.indexes;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchServiceVersion;
import com.azure.search.documents.implementation.converters.AnalyzeRequestConverter;
import com.azure.search.documents.implementation.util.MappingUtils;
import com.azure.search.documents.implementation.util.Utility;
import com.azure.search.documents.indexes.implementation.SearchServiceClientImpl;
import com.azure.search.documents.indexes.implementation.models.ListSynonymMapsResult;
import com.azure.search.documents.indexes.models.AnalyzeTextOptions;
import com.azure.search.documents.indexes.models.AnalyzedTokenInfo;
import com.azure.search.documents.indexes.models.FieldBuilderOptions;
import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.LexicalTokenizerName;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexStatistics;
import com.azure.search.documents.indexes.models.SearchServiceStatistics;
import com.azure.search.documents.indexes.models.SynonymMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import static com.azure.search.documents.indexes.SearchIndexAsyncClient.getSearchClientBuilder;

/**
 * This class provides a client that contains the operations for creating, getting, listing, updating, or deleting
 * indexes or synonym map and analyzing text in an Azure AI Search service.
 *
 * <h2>
 *     Overview
 * </h2>
 *
 * <p>
 *     An index is stored on your search service and populated with JSON documents that are indexed and tokenized for
 *     information retrieval. The fields collection of an index defines the structure of the search document. Fields
 *     have a name, data types, and attributes that determine how it's used. For example, searchable fields are used in
 *     full text search, and thus tokenized during indexing. An index also defines other constructs, such as scoring
 *     profiles for relevance tuning, suggesters, semantic configurations, and custom analyzers.
 * </p>
 *
 * <p>
 *     A synonym map is service-level object that contains user-defined synonyms. This object is maintained
 *     independently from search indexes. Once uploaded, you can point any searchable field to the synonym map (one per field).
 * </p>
 *
 * <p>
 *     This client provides a synchronous API for accessing indexes. This client allows you to create, delete, update,
 *     and configure search indexes. The client also allows you to declare custom synonym maps to expand or rewrite
 *     queries.
 * </p>
 *
 * <h2>
 *     Getting Started
 * </h2>
 *
 * <p>
 *     Authenticating and building instances of this client are handled by {@link SearchIndexClientBuilder}. This
 *     sample shows you how to create an instance of the client:
 * </p>
 *
 * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.instantiation -->
 * <pre>
 * SearchIndexClient searchIndexClient = new SearchIndexClientBuilder&#40;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.instantiation -->
 * <p>
 *     For more information on authentication and building, see the documentation for {@link SearchIndexClientBuilder}.
 * </p>
 *
 * <hr/>
 *
 * <h2>
 *     Examples
 * </h2>
 *
 * <p>
 *     The following examples all use <a href="https://github.com/Azure-Samples/azure-search-sample-data">a simple Hotel
 *     data set</a> that you can <a href="https://learn.microsoft.com/azure/search/search-get-started-portal#step-1---start-the-import-data-wizard-and-create-a-data-source">
 *         import into your own index from the Azure portal.</a>
 *     These are just a few of the basics - please check out <a href="https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/README.md">our Samples </a>for much more.
 * </p>
 *
 * <h3>
 *     Create an Index
 * </h3>
 *
 * <p>
 *     The following sample creates an index.
 * </p>
 *
 * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.createIndex#SearchIndex -->
 * <pre>
 * SearchIndex searchIndex = new SearchIndex&#40;&quot;indexName&quot;, Arrays.asList&#40;
 *     new SearchField&#40;&quot;hotelId&quot;, SearchFieldDataType.STRING&#41;
 *         .setKey&#40;true&#41;
 *         .setFilterable&#40;true&#41;
 *         .setSortable&#40;true&#41;,
 *     new SearchField&#40;&quot;hotelName&quot;, SearchFieldDataType.STRING&#41;
 *         .setSearchable&#40;true&#41;
 *         .setFilterable&#40;true&#41;
 *         .setSortable&#40;true&#41;,
 *     new SearchField&#40;&quot;description&quot;, SearchFieldDataType.STRING&#41;
 *         .setSearchable&#40;true&#41;
 *         .setAnalyzerName&#40;LexicalAnalyzerName.EN_LUCENE&#41;,
 *     new SearchField&#40;&quot;descriptionFr&quot;, SearchFieldDataType.STRING&#41;
 *         .setSearchable&#40;true&#41;
 *         .setAnalyzerName&#40;LexicalAnalyzerName.FR_LUCENE&#41;,
 *     new SearchField&#40;&quot;tags&quot;, SearchFieldDataType.collection&#40;SearchFieldDataType.STRING&#41;&#41;
 *         .setSearchable&#40;true&#41;
 *         .setFilterable&#40;true&#41;
 *         .setFacetable&#40;true&#41;,
 *     new SearchField&#40;&quot;address&quot;, SearchFieldDataType.COMPLEX&#41;
 *         .setFields&#40;
 *             new SearchField&#40;&quot;streetAddress&quot;, SearchFieldDataType.STRING&#41;
 *                 .setSearchable&#40;true&#41;,
 *             new SearchField&#40;&quot;city&quot;, SearchFieldDataType.STRING&#41;
 *                 .setFilterable&#40;true&#41;
 *                 .setSortable&#40;true&#41;
 *                 .setFacetable&#40;true&#41;,
 *             new SearchField&#40;&quot;stateProvince&quot;, SearchFieldDataType.STRING&#41;
 *                 .setSearchable&#40;true&#41;
 *                 .setFilterable&#40;true&#41;
 *                 .setSortable&#40;true&#41;
 *                 .setFacetable&#40;true&#41;,
 *             new SearchField&#40;&quot;country&quot;, SearchFieldDataType.STRING&#41;
 *                 .setSearchable&#40;true&#41;
 *                 .setSynonymMapNames&#40;&quot;synonymMapName&quot;&#41;
 *                 .setFilterable&#40;true&#41;
 *                 .setSortable&#40;true&#41;
 *                 .setFacetable&#40;true&#41;,
 *             new SearchField&#40;&quot;postalCode&quot;, SearchFieldDataType.STRING&#41;
 *                 .setSearchable&#40;true&#41;
 *                 .setFilterable&#40;true&#41;
 *                 .setSortable&#40;true&#41;
 *                 .setFacetable&#40;true&#41;&#41;
 * &#41;&#41;;
 *
 * searchIndexClient.createIndex&#40;searchIndex&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.createIndex#SearchIndex -->
 * <em>
 *     For an asynchronous sample see {@link SearchIndexAsyncClient#createIndex(SearchIndex)}.
 * </em>
 *
 * <h3>
 *     List indexes
 * </h3>
 *
 * <p>
 *     The following sample lists all indexes.
 * </p>
 *
 * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.listIndexes -->
 * <pre>
 * searchIndexClient.listIndexes&#40;&#41;.forEach&#40;index -&gt; System.out.println&#40;index.getName&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.listIndexes -->
 * <em>
 *     For an asynchronous sample see {@link SearchIndexAsyncClient#listIndexes()}.
 * </em>
 *
 * <h3>
 *     Retrieve an Index
 * </h3>
 *
 * <p>
 *     The following sample retrieves an index.
 * </p>
 *
 * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.getIndex#String -->
 * <pre>
 * SearchIndex searchIndex = searchIndexClient.getIndex&#40;&quot;indexName&quot;&#41;;
 * if &#40;searchIndex != null&#41; &#123;
 *     System.out.println&#40;&quot;The ETag of the index is &quot; + searchIndex.getETag&#40;&#41;&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.getIndex#String -->
 *
 * <em>
 *     For an asynchronous sample see {@link SearchIndexAsyncClient#getIndex(String)}.
 * </em>
 *
 *
 *
 * <h3>
 *     Update an Index
 * </h3>
 *
 * <p>
 *     The following sample updates an index.
 * </p>
 *
 * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.updateIndex#SearchIndex -->
 * <pre>
 * SearchIndex searchIndex = searchIndexClient.getIndex&#40;&quot;indexName&quot;&#41;;
 * if &#40;searchIndex != null&#41; &#123;
 *     searchIndex.setFields&#40;new SearchField&#40;&quot;newField&quot;, SearchFieldDataType.STRING&#41;&#41;;
 *     searchIndexClient.createOrUpdateIndex&#40;searchIndex&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.updateIndex#SearchIndex -->
 *
 * <em>
 *     For an asynchronous sample see {@link SearchIndexAsyncClient#createOrUpdateIndex(SearchIndex)}.
 * </em>
 *
 *
 *
 * <h3>
 *     Delete an Index
 * </h3>
 *
 * <p>
 *     The following sample deletes an index.
 * </p>
 * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.deleteIndex#String -->
 * <pre>
 * String indexName = &quot;indexName&quot;;
 * searchIndexClient.deleteIndex&#40;indexName&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.deleteIndex#String -->
 * <em>
 *     For an asynchronous sample see {@link SearchIndexAsyncClient#deleteIndex(String)}.
 * </em>
 *
 *
 *
 * <h3>
 *     Create a Synonym Map
 * </h3>
 *
 * <p>
 *     The following sample creates a synonym map.
 * </p>
 *
 * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.createSynonymMap#SynonymMap -->
 * <pre>
 * SynonymMap synonymMap = new SynonymMap&#40;&quot;synonymMapName&quot;, &quot;hotel, motel, &#92;&quot;motor inn&#92;&quot;&quot;&#41;;
 * searchIndexClient.createSynonymMap&#40;synonymMap&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.createSynonymMap#SynonymMap -->
 *
 * <em>
 *     For an asynchronous sample see {@link SearchIndexAsyncClient#createSynonymMap(SynonymMap)}.
 * </em>
 *
 *
 *
 * <h3>
 *     List Synonym Maps
 * </h3>
 *
 * <p>
 *     The following sample lists all synonym maps.
 * </p>
 *
 * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.listSynonymMaps -->
 * <pre>
 * searchIndexClient.listSynonymMaps&#40;&#41;.forEach&#40;synonymMap -&gt; System.out.println&#40;synonymMap.getName&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.listSynonymMaps -->
 *
 * <em>
 *     For an asynchronous sample see {@link SearchIndexAsyncClient#listSynonymMaps()}.
 * </em>
 *
 *
 *
 * <h3>
 *     Retrieve a Synonym Map
 * </h3>
 *
 * <p>
 *     The following sample retrieves a synonym map.
 * </p>
 * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.getSynonymMap#String -->
 * <pre>
 * SynonymMap synonymMap = searchIndexClient.getSynonymMap&#40;&quot;synonymMapName&quot;&#41;;
 * if &#40;synonymMap != null&#41; &#123;
 *     System.out.println&#40;&quot;The ETag of the synonymMap is &quot; + synonymMap.getETag&#40;&#41;&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.getSynonymMap#String -->
 * <em>
 *     For an asynchronous sample see {@link SearchIndexAsyncClient#getSynonymMap(String)}.
 * </em>
 *
 *
 *
 * <h3>
 *     Update a Synonym Map
 * </h3>
 *
 * <p>
 *     The following sample updates a synonym map.
 * </p>
 * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.updateSynonymMap#SynonymMap -->
 * <pre>
 * SynonymMap synonymMap = searchIndexClient.getSynonymMap&#40;&quot;synonymMapName&quot;&#41;;
 * if &#40;synonymMap != null&#41; &#123;
 *     synonymMap.setSynonyms&#40;&quot;inn,hotel,motel&quot;&#41;;
 *     searchIndexClient.createOrUpdateSynonymMap&#40;synonymMap&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.updateSynonymMap#SynonymMap -->
 * <em>
 *     For an asynchronous sample see {@link SearchIndexAsyncClient#createOrUpdateSynonymMap(SynonymMap)}.
 * </em>
 *
 *
 *
 * <h3>
 *     Delete a Synonym Map
 * </h3>
 *
 * <p>
 *     The following sample deletes a synonym map.
 * </p>
 *
 * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.deleteSynonymMap#String -->
 * <pre>
 * String synonymMapName = &quot;synonymMapName&quot;;
 * searchIndexClient.deleteSynonymMap&#40;synonymMapName&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.deleteSynonymMap#String -->
 *
 * <em>
 *     For an asynchronous sample see {@link SearchIndexAsyncClient#deleteSynonymMap(String)}.
 * </em>
 *
 *
 * @see SearchIndexAsyncClient
 * @see SearchIndexClientBuilder
 * @see com.azure.search.documents.indexes
 */
@ServiceClient(builder = SearchIndexClientBuilder.class)
public final class SearchIndexClient {
    private static final ClientLogger LOGGER = new ClientLogger(SearchIndexClient.class);

    /**
     * Search REST API Version
     */
    private final SearchServiceVersion serviceVersion;

    /**
     * The endpoint for the Azure AI Search service.
     */
    private final String endpoint;

    /**
     * The underlying AutoRest client used to interact with the Search service
     */
    private final SearchServiceClientImpl restClient;

    private final JsonSerializer serializer;

    /**
     * The pipeline that powers this client.
     */
    private final HttpPipeline httpPipeline;

    SearchIndexClient(String endpoint, SearchServiceVersion serviceVersion, HttpPipeline httpPipeline,
        JsonSerializer serializer) {
        this.endpoint = endpoint;
        this.serviceVersion = serviceVersion;
        this.httpPipeline = httpPipeline;
        this.serializer = serializer;
        this.restClient = new SearchServiceClientImpl(httpPipeline, endpoint, serviceVersion.getVersion());
    }
    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return the pipeline.
     */
    HttpPipeline getHttpPipeline() {
        return this.httpPipeline;
    }

    /**
     * Gets the endpoint for the Azure AI Search service.
     *
     * @return the endpoint value.
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /**
     * Initializes a new {@link SearchClient} using the given Index name and the same configuration as the
     * SearchServiceClient.
     *
     * @param indexName the name of the Index for the client
     * @return a {@link SearchClient} created from the service client configuration
     */
    public SearchClient getSearchClient(String indexName) {
        return getSearchClientBuilder(indexName, endpoint, serviceVersion, httpPipeline, serializer).buildClient();
    }

    /**
     * Creates a new Azure AI Search index
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create search index named "searchIndex". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.createIndex#SearchIndex -->
     * <pre>
     * List&lt;SearchField&gt; searchFields = Arrays.asList&#40;
     *     new SearchField&#40;&quot;hotelId&quot;, SearchFieldDataType.STRING&#41;.setKey&#40;true&#41;,
     *     new SearchField&#40;&quot;hotelName&quot;, SearchFieldDataType.STRING&#41;.setSearchable&#40;true&#41;
     * &#41;;
     * SearchIndex searchIndex = new SearchIndex&#40;&quot;searchIndex&quot;, searchFields&#41;;
     * SearchIndex indexFromService = SEARCH_INDEX_CLIENT.createIndex&#40;searchIndex&#41;;
     * System.out.printf&#40;&quot;The index name is %s. The ETag of index is %s.%n&quot;, indexFromService.getName&#40;&#41;,
     *     indexFromService.getETag&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.createIndex#SearchIndex -->
     *
     * @param index definition of the index to create
     * @return the created Index.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchIndex createIndex(SearchIndex index) {
        return createIndexWithResponse(index, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure AI Search index
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create search index named "searchIndex". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.createIndexWithResponse#SearchIndex-Context -->
     * <pre>
     * List&lt;SearchField&gt; searchFields = Arrays.asList&#40;
     *     new SearchField&#40;&quot;hotelId&quot;, SearchFieldDataType.STRING&#41;.setKey&#40;true&#41;,
     *     new SearchField&#40;&quot;hotelName&quot;, SearchFieldDataType.STRING&#41;.setSearchable&#40;true&#41;
     * &#41;;
     * SearchIndex searchIndex = new SearchIndex&#40;&quot;searchIndex&quot;, searchFields&#41;;
     *
     * Response&lt;SearchIndex&gt; indexFromServiceResponse =
     *     SEARCH_INDEX_CLIENT.createIndexWithResponse&#40;searchIndex, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.printf&#40;&quot;The status code of the response is %s. The index name is %s.%n&quot;,
     *     indexFromServiceResponse.getStatusCode&#40;&#41;, indexFromServiceResponse.getValue&#40;&#41;.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.createIndexWithResponse#SearchIndex-Context -->
     *
     * @param index definition of the index to create
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the created Index.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchIndex> createIndexWithResponse(SearchIndex index, Context context) {
        return Utility.executeRestCallWithExceptionHandling(() -> {
            Objects.requireNonNull(index, "'Index' cannot be null");
            return restClient.getIndexes().createWithResponse(index, null, context);
        }, LOGGER);
    }

    /**
     * Retrieves an index definition from the Azure AI Search.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search index with name "searchIndex". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.getIndex#String -->
     * <pre>
     * SearchIndex indexFromService =
     *     SEARCH_INDEX_CLIENT.getIndex&#40;&quot;searchIndex&quot;&#41;;
     * System.out.printf&#40;&quot;The index name is %s. The ETag of index is %s.%n&quot;, indexFromService.getName&#40;&#41;,
     *     indexFromService.getETag&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.getIndex#String -->
     *
     * @param indexName the name of the index to retrieve
     * @return the Index.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchIndex getIndex(String indexName) {
        return getIndexWithResponse(indexName, Context.NONE).getValue();
    }

    /**
     * Retrieves an index definition from the Azure AI Search.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search index with "searchIndex. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.getIndexWithResponse#String-Context -->
     * <pre>
     * Response&lt;SearchIndex&gt; indexFromServiceResponse =
     *     SEARCH_INDEX_CLIENT.getIndexWithResponse&#40;&quot;searchIndex&quot;, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     *
     * System.out.printf&#40;&quot;The status code of the response is %s. The index name is %s.%n&quot;,
     *     indexFromServiceResponse.getStatusCode&#40;&#41;, indexFromServiceResponse.getValue&#40;&#41;.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.getIndexWithResponse#String-Context -->
     *
     * @param indexName the name of the index to retrieve
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the Index.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchIndex> getIndexWithResponse(String indexName, Context context) {
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getIndexes()
            .getWithResponse(indexName, null, context), LOGGER);
    }



    /**
     * Returns statistics for the given index, including a document count and storage usage.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search index "searchIndex" statistics. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.getIndexStatistics#String -->
     * <pre>
     * SearchIndexStatistics statistics = SEARCH_INDEX_CLIENT.getIndexStatistics&#40;&quot;searchIndex&quot;&#41;;
     * System.out.printf&#40;&quot;There are %d documents and storage size of %d available in 'searchIndex'.%n&quot;,
     *     statistics.getDocumentCount&#40;&#41;, statistics.getStorageSize&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.getIndexStatistics#String -->
     *
     * @param indexName the name of the index for which to retrieve statistics
     * @return the index statistics result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchIndexStatistics getIndexStatistics(String indexName) {
        return getIndexStatisticsWithResponse(indexName, Context.NONE).getValue();
    }

    /**
     * Returns statistics for the given index, including a document count and storage usage.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get search index "searchIndex" statistics. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.getIndexStatisticsWithResponse#String-Context -->
     * <pre>
     * Response&lt;SearchIndexStatistics&gt; statistics = SEARCH_INDEX_CLIENT.getIndexStatisticsWithResponse&#40;&quot;searchIndex&quot;,
     *     new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.printf&#40;&quot;The status code of the response is %s.%n&quot;
     *         + &quot;There are %d documents and storage size of %d available in 'searchIndex'.%n&quot;,
     *     statistics.getStatusCode&#40;&#41;, statistics.getValue&#40;&#41;.getDocumentCount&#40;&#41;,
     *     statistics.getValue&#40;&#41;.getStorageSize&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.getIndexStatisticsWithResponse#String-Context -->
     *
     * @param indexName the name of the index for which to retrieve statistics
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the index statistics result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchIndexStatistics> getIndexStatisticsWithResponse(String indexName, Context context) {
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getIndexes()
            .getStatisticsWithResponse(indexName, null, context), LOGGER);
    }

    /**
     * Lists all indexes available for an Azure AI Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexes. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.listIndexes -->
     * <pre>
     * PagedIterable&lt;SearchIndex&gt; indexes = SEARCH_INDEX_CLIENT.listIndexes&#40;&#41;;
     * for &#40;SearchIndex index: indexes&#41; &#123;
     *     System.out.printf&#40;&quot;The index name is %s. The ETag of index is %s.%n&quot;, index.getName&#40;&#41;,
     *         index.getETag&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.listIndexes -->
     *
     * @return the list of indexes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SearchIndex> listIndexes() {
        return listIndexes(Context.NONE);
    }

    /**
     * Lists all indexes available for an Azure AI Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexes. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.listIndexesWithResponse#Context -->
     * <pre>
     * PagedIterable&lt;SearchIndex&gt; indexes = SEARCH_INDEX_CLIENT.listIndexes&#40;new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.println&#40;&quot;The status code of the response is&quot;
     *     + indexes.iterableByPage&#40;&#41;.iterator&#40;&#41;.next&#40;&#41;.getStatusCode&#40;&#41;&#41;;
     * for &#40;SearchIndex index: indexes&#41; &#123;
     *     System.out.printf&#40;&quot;The index name is %s. The ETag of index is %s.%n&quot;, index.getName&#40;&#41;, index.getETag&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.listIndexesWithResponse#Context -->
     *
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return the list of indexes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SearchIndex> listIndexes(Context context) {
        try {
            return new PagedIterable<>(() -> this.listIndexesWithResponse(null, context));
        } catch (RuntimeException ex) {
            throw LOGGER.logExceptionAsError(ex);
        }
    }

    private PagedResponse<SearchIndex> listIndexesWithResponse(String select, Context context) {
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getIndexes()
            .listSinglePage(select, null, context), LOGGER);
    }

    /**
     * Lists all index names for an Azure AI Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexes names. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.listIndexNames -->
     * <pre>
     * PagedIterable&lt;String&gt; indexes = SEARCH_INDEX_CLIENT.listIndexNames&#40;&#41;;
     * for &#40;String indexName: indexes&#41; &#123;
     *     System.out.printf&#40;&quot;The index name is %s.%n&quot;, indexName&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.listIndexNames -->
     *
     * @return the list of index names.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listIndexNames() {
        return listIndexNames(Context.NONE);
    }

    /**
     * Lists all indexes names for an Azure AI Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all search indexes names. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.listIndexNames#Context -->
     * <pre>
     * PagedIterable&lt;String&gt; indexes = SEARCH_INDEX_CLIENT.listIndexNames&#40;new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.println&#40;&quot;The status code of the response is&quot;
     *     + indexes.iterableByPage&#40;&#41;.iterator&#40;&#41;.next&#40;&#41;.getStatusCode&#40;&#41;&#41;;
     * for &#40;String indexName: indexes&#41; &#123;
     *     System.out.printf&#40;&quot;The index name is %s.%n&quot;, indexName&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.listIndexNames#Context -->
     *
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return the list of index names.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listIndexNames(Context context) {
        try {
            return new PagedIterable<>(() ->
                MappingUtils.mappingPagingSearchIndexNames(this.listIndexesWithResponse("name", context)));
        } catch (RuntimeException ex) {
            throw LOGGER.logExceptionAsError(ex);
        }
    }

    /**
     * Creates a new Azure AI Search index or updates an index if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update search index named "searchIndex". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateIndex#SearchIndex -->
     * <pre>
     * SearchIndex indexFromService = SEARCH_INDEX_CLIENT.getIndex&#40;&quot;searchIndex&quot;&#41;;
     * indexFromService.setSuggesters&#40;Collections.singletonList&#40;new SearchSuggester&#40;&quot;sg&quot;,
     *     Collections.singletonList&#40;&quot;hotelName&quot;&#41;&#41;&#41;&#41;;
     * SearchIndex updatedIndex = SEARCH_INDEX_CLIENT.createOrUpdateIndex&#40;indexFromService&#41;;
     * System.out.printf&#40;&quot;The index name is %s. The suggester name of index is %s.%n&quot;, updatedIndex.getName&#40;&#41;,
     *     updatedIndex.getSuggesters&#40;&#41;.get&#40;0&#41;.getName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateIndex#SearchIndex -->
     *
     * @param index the definition of the index to create or update
     * @return the index that was created or updated.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchIndex createOrUpdateIndex(SearchIndex index) {
        return createOrUpdateIndexWithResponse(index, false, false, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure AI Search index or updates an index if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update search index named "searchIndex". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateIndexWithResponse#SearchIndex-boolean-boolean-Context -->
     * <pre>
     * SearchIndex indexFromService = SEARCH_INDEX_CLIENT.getIndex&#40;&quot;searchIndex&quot;&#41;;
     * indexFromService.setSuggesters&#40;Collections.singletonList&#40;new SearchSuggester&#40;&quot;sg&quot;,
     *     Collections.singletonList&#40;&quot;hotelName&quot;&#41;&#41;&#41;&#41;;
     * Response&lt;SearchIndex&gt; updatedIndexResponse = SEARCH_INDEX_CLIENT.createOrUpdateIndexWithResponse&#40;indexFromService, true,
     *     false, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.printf&#40;&quot;The status code of the normal response is %s.%n&quot;
     *         + &quot;The index name is %s. The ETag of index is %s.%n&quot;, updatedIndexResponse.getStatusCode&#40;&#41;,
     *     updatedIndexResponse.getValue&#40;&#41;.getName&#40;&#41;, updatedIndexResponse.getValue&#40;&#41;.getETag&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateIndexWithResponse#SearchIndex-boolean-boolean-Context -->
     *
     * @param index the {@link SearchIndex} to create or update
     * @param allowIndexDowntime allows new analyzers, tokenizers, token filters, or char filters to be added to an
     * index by taking the index offline for at least a few seconds. This temporarily causes indexing and query requests
     * to fail. Performance and write availability of the index can be impaired for several minutes after the index is
     * updated, or longer for very large indexes.
     * @param onlyIfUnchanged {@code true} to update if the {@code index} is the same as the current service value.
     * {@code false} to always update existing value.
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the Index that was created or updated.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchIndex> createOrUpdateIndexWithResponse(SearchIndex index, boolean allowIndexDowntime,
        boolean onlyIfUnchanged, Context context) {
        return Utility.executeRestCallWithExceptionHandling(() -> {
            Objects.requireNonNull(index, "'Index' cannot null.");
            String ifMatch = onlyIfUnchanged ? index.getETag() : null;
            return restClient.getIndexes().createOrUpdateWithResponse(index.getName(), index, allowIndexDowntime,
                ifMatch, null, null, context);
        }, LOGGER);
    }

    /**
     * Deletes an Azure AI Search index and all the documents it contains.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete search index with name "searchIndex". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.deleteIndex#String -->
     * <pre>
     * SEARCH_INDEX_CLIENT.deleteIndex&#40;&quot;searchIndex&quot;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.deleteIndex#String -->
     *
     * @param indexName the name of the index to delete
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteIndex(String indexName) {
        Utility.executeRestCallWithExceptionHandling(() -> restClient.getIndexes()
            .deleteWithResponse(indexName, null, null, null, Context.NONE), LOGGER);
    }

    /**
     * Deletes an Azure AI Search index and all the documents it contains.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete search index with name "searchIndex". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.deleteIndexWithResponse#SearchIndex-boolean-Context -->
     * <pre>
     * SearchIndex indexFromService = SEARCH_INDEX_CLIENT.getIndex&#40;&quot;searchIndex&quot;&#41;;
     * Response&lt;Void&gt; deleteResponse = SEARCH_INDEX_CLIENT.deleteIndexWithResponse&#40;indexFromService, true,
     *     new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.printf&#40;&quot;The status code of the response is %d.%n&quot;, deleteResponse.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.deleteIndexWithResponse#SearchIndex-boolean-Context -->
     *
     * @param index the Search {@link SearchIndex} to delete.
     * @param onlyIfUnchanged {@code true} to delete if the {@code index} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return a response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteIndexWithResponse(SearchIndex index, boolean onlyIfUnchanged, Context context) {
        return Utility.executeRestCallWithExceptionHandling(() -> {
            String etag = onlyIfUnchanged ? index.getETag() : null;
            return restClient.getIndexes()
                .deleteWithResponse(index.getName(), etag, null, null, context);
        }, LOGGER);
    }

    /**
     * Shows how an analyzer breaks text into tokens.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Analyzer text with LexicalTokenizerName "Classic" in search index "searchIndex". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.analyzeText#String-AnalyzeTextOptions -->
     * <pre>
     * PagedIterable&lt;AnalyzedTokenInfo&gt; tokenInfos = SEARCH_INDEX_CLIENT.analyzeText&#40;&quot;searchIndex&quot;,
     *     new AnalyzeTextOptions&#40;&quot;The quick brown fox&quot;, LexicalTokenizerName.CLASSIC&#41;&#41;;
     * for &#40;AnalyzedTokenInfo tokenInfo : tokenInfos&#41; &#123;
     *     System.out.printf&#40;&quot;The token emitted by the analyzer is %s.%n&quot;, tokenInfo.getToken&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.analyzeText#String-AnalyzeTextOptions -->
     *
     * @param indexName the name of the index for which to test an analyzer
     * @param analyzeTextOptions the text and analyzer or analysis components to test. Requires to provide either {@link
     * LexicalTokenizerName} or {@link LexicalAnalyzerName}.
     * @return analyze result.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AnalyzedTokenInfo> analyzeText(String indexName, AnalyzeTextOptions analyzeTextOptions) {
        return analyzeText(indexName, analyzeTextOptions, Context.NONE);
    }

    /**
     * Shows how an analyzer breaks text into tokens.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Analyzer text response with LexicalTokenizerName "Classic" in search index "searchIndex". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.analyzeText#String-AnalyzeTextOptions-Context -->
     * <pre>
     * PagedIterable&lt;AnalyzedTokenInfo&gt; tokenInfos = SEARCH_INDEX_CLIENT.analyzeText&#40;&quot;searchIndex&quot;,
     *     new AnalyzeTextOptions&#40;&quot;The quick brown fox&quot;, LexicalTokenizerName.CLASSIC&#41;, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.println&#40;&quot;The status code of the response is &quot;
     *     + tokenInfos.iterableByPage&#40;&#41;.iterator&#40;&#41;.next&#40;&#41;.getStatusCode&#40;&#41;&#41;;
     * for &#40;AnalyzedTokenInfo tokenInfo : tokenInfos&#41; &#123;
     *     System.out.printf&#40;&quot;The token emitted by the analyzer is %s.%n&quot;, tokenInfo.getToken&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.analyzeText#String-AnalyzeTextOptions-Context -->
     *
     * @param indexName the name of the index for which to test an analyzer
     * @param analyzeTextOptions the text and analyzer or analysis components to test. Requires to provide either {@link
     * LexicalTokenizerName} or {@link LexicalAnalyzerName}.
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return analyze result.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AnalyzedTokenInfo> analyzeText(String indexName, AnalyzeTextOptions analyzeTextOptions,
        Context context) {
        try {
            return new PagedIterable<>(() -> analyzeTextWithResponse(indexName, analyzeTextOptions, context));
        } catch (RuntimeException ex) {
            throw LOGGER.logExceptionAsError(ex);
        }
    }

    private PagedResponse<AnalyzedTokenInfo> analyzeTextWithResponse(String indexName,
        AnalyzeTextOptions analyzeTextOptions, Context context) {
        return Utility.executeRestCallWithExceptionHandling(() -> MappingUtils.mappingTokenInfo(restClient.getIndexes()
            .analyzeWithResponse(indexName, AnalyzeRequestConverter.map(analyzeTextOptions), null,
                context)), LOGGER);
    }

    /**
     * Creates a new Azure AI Search synonym map.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create synonym map named "synonymMap". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.createSynonymMap#SynonymMap -->
     * <pre>
     * SynonymMap synonymMap = new SynonymMap&#40;&quot;synonymMap&quot;,
     *     &quot;United States, United States of America, USA&#92;nWashington, Wash. =&gt; WA&quot;&#41;;
     * SynonymMap synonymMapFromService = SEARCH_INDEX_CLIENT.createSynonymMap&#40;synonymMap&#41;;
     * System.out.printf&#40;&quot;The synonym map name is %s. The ETag of synonym map is %s.%n&quot;,
     *     synonymMapFromService.getName&#40;&#41;, synonymMapFromService.getETag&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.createSynonymMap#SynonymMap -->
     *
     * @param synonymMap the definition of the synonym map to create
     * @return the created {@link SynonymMap}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SynonymMap createSynonymMap(SynonymMap synonymMap) {
        return createSynonymMapWithResponse(synonymMap, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure AI Search synonym map.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create synonym map named "synonymMap". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.createSynonymMapWithResponse#SynonymMap-Context -->
     * <pre>
     * SynonymMap synonymMap = new SynonymMap&#40;&quot;synonymMap&quot;,
     *     &quot;United States, United States of America, USA&#92;nWashington, Wash. =&gt; WA&quot;&#41;;
     * Response&lt;SynonymMap&gt; synonymMapFromService = SEARCH_INDEX_CLIENT.createSynonymMapWithResponse&#40;synonymMap,
     *     new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.printf&#40;&quot;The status code of the response is %d.%n&quot;
     *         + &quot;The synonym map name is %s. The ETag of synonym map is %s.%n&quot;, synonymMapFromService.getStatusCode&#40;&#41;,
     *     synonymMapFromService.getValue&#40;&#41;.getName&#40;&#41;, synonymMapFromService.getValue&#40;&#41;.getETag&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.createSynonymMapWithResponse#SynonymMap-Context -->
     *
     * @param synonymMap the definition of the synonym map to create
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the created SynonymMap.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SynonymMap> createSynonymMapWithResponse(SynonymMap synonymMap, Context context) {
        return Utility.executeRestCallWithExceptionHandling(() -> {
            Objects.requireNonNull(synonymMap, "'synonymMap' cannot be null.");
            return restClient.getSynonymMaps()
                .createWithResponse(synonymMap, null, context);
        }, LOGGER);
    }

    /**
     * Retrieves a synonym map definition.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get synonym map with name "synonymMap". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.getSynonymMap#String -->
     * <pre>
     * SynonymMap synonymMapFromService =
     *     SEARCH_INDEX_CLIENT.getSynonymMap&#40;&quot;synonymMap&quot;&#41;;
     * System.out.printf&#40;&quot;The synonym map is %s. The ETag of synonym map is %s.%n&quot;, synonymMapFromService.getName&#40;&#41;,
     *     synonymMapFromService.getETag&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.getSynonymMap#String -->
     *
     * @param synonymMapName name of the synonym map to retrieve
     * @return the {@link SynonymMap} definition
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SynonymMap getSynonymMap(String synonymMapName) {
        return getSynonymMapWithResponse(synonymMapName, Context.NONE).getValue();
    }

    /**
     * Retrieves a synonym map definition.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get synonym map with name "synonymMap". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.getSynonymMapWithResponse#String-Context -->
     * <pre>
     * Response&lt;SynonymMap&gt; synonymMapFromService =
     *     SEARCH_INDEX_CLIENT.getSynonymMapWithResponse&#40;&quot;synonymMap&quot;, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.printf&#40;&quot;The status code of the response is %d.%n&quot;
     *         + &quot;The synonym map name is %s. The ETag of synonym map is %s.%n&quot;, synonymMapFromService.getStatusCode&#40;&#41;,
     *     synonymMapFromService.getValue&#40;&#41;.getName&#40;&#41;, synonymMapFromService.getValue&#40;&#41;.getETag&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.getSynonymMapWithResponse#String-Context -->
     *
     * @param synonymMapName name of the synonym map to retrieve
     * @param context a context that is passed through the HTTP pipeline during the service call
     * @return a response containing the SynonymMap.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SynonymMap> getSynonymMapWithResponse(String synonymMapName, Context context) {
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getSynonymMaps()
            .getWithResponse(synonymMapName, null, context), LOGGER);
    }

    /**
     * Lists all synonym maps available for an Azure AI Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all synonym maps. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.listSynonymMaps -->
     * <pre>
     * PagedIterable&lt;SynonymMap&gt; synonymMaps = SEARCH_INDEX_CLIENT.listSynonymMaps&#40;&#41;;
     * for &#40;SynonymMap synonymMap: synonymMaps&#41; &#123;
     *     System.out.printf&#40;&quot;The synonymMap name is %s. The ETag of synonymMap is %s.%n&quot;, synonymMap.getName&#40;&#41;,
     *         synonymMap.getETag&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.listSynonymMaps -->
     *
     * @return the list of synonym maps.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SynonymMap> listSynonymMaps() {
        return listSynonymMaps(Context.NONE);
    }

    /**
     * Lists all synonym maps available for an Azure AI Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all synonym maps. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.listSynonymMapsWithResponse#Context -->
     * <pre>
     * PagedIterable&lt;SynonymMap&gt; synonymMaps = SEARCH_INDEX_CLIENT.listSynonymMaps&#40;new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.println&#40;&quot;The status code of the response is&quot;
     *     + synonymMaps.iterableByPage&#40;&#41;.iterator&#40;&#41;.next&#40;&#41;.getStatusCode&#40;&#41;&#41;;
     * for &#40;SynonymMap index: synonymMaps&#41; &#123;
     *     System.out.printf&#40;&quot;The index name is %s. The ETag of index is %s.%n&quot;, index.getName&#40;&#41;, index.getETag&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.listSynonymMapsWithResponse#Context -->
     *
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return the list of synonym map names.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SynonymMap> listSynonymMaps(Context context) {
        try {
            return new PagedIterable<>(() ->
                MappingUtils.mappingPagingSynonymMap(listSynonymMapsWithResponse(null, context)));
        } catch (RuntimeException ex) {
            throw LOGGER.logExceptionAsError(ex);
        }
    }

    private Response<ListSynonymMapsResult> listSynonymMapsWithResponse(String select, Context context) {
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getSynonymMaps()
            .listWithResponse(select, null, context), LOGGER);
    }

    /**
     * Lists all synonym maps names for an Azure AI Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all synonym map names. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.listSynonymMapNames -->
     * <pre>
     * PagedIterable&lt;String&gt; synonymMaps = SEARCH_INDEX_CLIENT.listSynonymMapNames&#40;&#41;;
     * for &#40;String synonymMap: synonymMaps&#41; &#123;
     *     System.out.printf&#40;&quot;The synonymMap name is %s.%n&quot;, synonymMap&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.listSynonymMapNames -->
     *
     * @return the list of synonym maps.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listSynonymMapNames() {
        return listSynonymMapNames(Context.NONE);
    }

    /**
     * Lists all synonym maps names for an Azure AI Search service.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> List all synonym map names. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.listSynonymMapNamesWithResponse#Context -->
     * <pre>
     * PagedIterable&lt;String&gt; synonymMaps = SEARCH_INDEX_CLIENT.listIndexNames&#40;new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.println&#40;&quot;The status code of the response is&quot;
     *     + synonymMaps.iterableByPage&#40;&#41;.iterator&#40;&#41;.next&#40;&#41;.getStatusCode&#40;&#41;&#41;;
     * for &#40;String synonymMapNames: synonymMaps&#41; &#123;
     *     System.out.printf&#40;&quot;The synonymMap name is %s.%n&quot;, synonymMapNames&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.listSynonymMapNamesWithResponse#Context -->
     *
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return the list of synonym map names.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listSynonymMapNames(Context context) {
        try {
            return new PagedIterable<>(() ->
                MappingUtils.mappingPagingSynonymMapNames(listSynonymMapsWithResponse("name", context)));
        } catch (RuntimeException ex) {
            throw LOGGER.logExceptionAsError(ex);
        }
    }

    /**
     * Creates a new Azure AI Search synonym map or updates a synonym map if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update synonym map named "synonymMap". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateSynonymMap#SynonymMap -->
     * <pre>
     * SynonymMap synonymMap = SEARCH_INDEX_CLIENT.getSynonymMap&#40;&quot;synonymMapName&quot;&#41;;
     * synonymMap.setSynonyms&#40;&quot;United States, United States of America, USA, America&#92;nWashington, Wash. =&gt; WA&quot;&#41;;
     * SynonymMap updatedSynonymMap = SEARCH_INDEX_CLIENT.createOrUpdateSynonymMap&#40;synonymMap&#41;;
     * System.out.printf&#40;&quot;The synonym map name is %s. The synonyms are %s.%n&quot;, updatedSynonymMap.getName&#40;&#41;,
     *     updatedSynonymMap.getSynonyms&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateSynonymMap#SynonymMap -->
     *
     * @param synonymMap the definition of the synonym map to create or update
     * @return the synonym map that was created or updated.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SynonymMap createOrUpdateSynonymMap(SynonymMap synonymMap) {
        return createOrUpdateSynonymMapWithResponse(synonymMap, false, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure AI Search synonym map or updates a synonym map if it already exists.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Create or update synonym map named "synonymMap". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateSynonymMapWithResponse#SynonymMap-boolean-Context -->
     * <pre>
     * SynonymMap synonymMap = SEARCH_INDEX_CLIENT.getSynonymMap&#40;&quot;synonymMap&quot;&#41;;
     * synonymMap.setSynonyms&#40;&quot;United States, United States of America, USA, America&#92;nWashington, Wash. =&gt; WA&quot;&#41;;
     * Response&lt;SynonymMap&gt; updatedSynonymMap =
     *     SEARCH_INDEX_CLIENT.createOrUpdateSynonymMapWithResponse&#40;synonymMap, true,
     *         new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.printf&#40;&quot;The status code of the normal response is %s.%n&quot;
     *         + &quot;The synonym map name is %s. The synonyms are %s.%n&quot;, updatedSynonymMap.getStatusCode&#40;&#41;,
     *     updatedSynonymMap.getValue&#40;&#41;.getName&#40;&#41;, updatedSynonymMap.getValue&#40;&#41;.getSynonyms&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateSynonymMapWithResponse#SynonymMap-boolean-Context -->
     *
     * @param synonymMap the definition of the synonym map to create or update
     * @param onlyIfUnchanged {@code true} to update if the {@code synonymMap} is the same as the current service value.
     * {@code false} to always update existing value.
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the synonym map that was created or updated.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SynonymMap> createOrUpdateSynonymMapWithResponse(SynonymMap synonymMap,
        boolean onlyIfUnchanged, Context context) {
        return Utility.executeRestCallWithExceptionHandling(() -> {
            Objects.requireNonNull(synonymMap, "'synonymMap' cannot be null.");
            String ifMatch = onlyIfUnchanged ? synonymMap.getETag() : null;
            return restClient.getSynonymMaps().createOrUpdateWithResponse(synonymMap.getName(), synonymMap, ifMatch,
                null, null, context);
        }, LOGGER);
    }

    /**
     * Deletes an Azure AI Search synonym map.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete synonym map with name "synonymMap". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.deleteSynonymMap#String -->
     * <pre>
     * SEARCH_INDEX_CLIENT.deleteSynonymMap&#40;&quot;synonymMap&quot;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.deleteSynonymMap#String -->
     *
     * @param synonymMapName the name of the synonym map to delete
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteSynonymMap(String synonymMapName) {
        Utility.executeRestCallWithExceptionHandling(() -> restClient.getSynonymMaps()
            .deleteWithResponse(synonymMapName, null, null, null, Context.NONE), LOGGER);
    }

    /**
     * Deletes an Azure AI Search synonym map.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete synonym map with name "synonymMap". </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.deleteSynonymMapWithResponse#SynonymMap-boolean-Context -->
     * <pre>
     * SynonymMap synonymMap = SEARCH_INDEX_CLIENT.getSynonymMap&#40;&quot;synonymMap&quot;&#41;;
     * Response&lt;Void&gt; response = SEARCH_INDEX_CLIENT.deleteSynonymMapWithResponse&#40;synonymMap, true,
     *     new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.println&#40;&quot;The status code of the response is&quot; + response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.deleteSynonymMapWithResponse#SynonymMap-boolean-Context -->
     *
     * @param synonymMap the {@link SynonymMap} to delete.
     * @param onlyIfUnchanged {@code true} to delete if the {@code synonymMap} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return a response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteSynonymMapWithResponse(SynonymMap synonymMap, boolean onlyIfUnchanged,
        Context context) {
        String etag = onlyIfUnchanged ? synonymMap.getETag() : null;
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getSynonymMaps()
            .deleteWithResponse(synonymMap.getName(), etag, null, null, context), LOGGER);
    }

    /**
     * Returns service level statistics for a search service, including service counters and limits.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get service statistics. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.getServiceStatistics -->
     * <pre>
     * SearchServiceStatistics serviceStatistics = SEARCH_INDEX_CLIENT.getServiceStatistics&#40;&#41;;
     * System.out.printf&#40;&quot;There are %s search indexes in your service.%n&quot;,
     *     serviceStatistics.getCounters&#40;&#41;.getIndexCounter&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.getServiceStatistics -->
     *
     * @return the search service statistics result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchServiceStatistics getServiceStatistics() {
        return getServiceStatisticsWithResponse(Context.NONE).getValue();
    }

    /**
     * Returns service level statistics for a search service, including service counters and limits.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get service statistics. </p>
     *
     * <!-- src_embed com.azure.search.documents.indexes.SearchIndexClient.getServiceStatisticsWithResponse#Context -->
     * <pre>
     * Response&lt;SearchServiceStatistics&gt; serviceStatistics =
     *     SEARCH_INDEX_CLIENT.getServiceStatisticsWithResponse&#40;new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.printf&#40;&quot;The status code of the response is %s.%nThere are %s search indexes in your service.%n&quot;,
     *     serviceStatistics.getStatusCode&#40;&#41;,
     *     serviceStatistics.getValue&#40;&#41;.getCounters&#40;&#41;.getIndexCounter&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.indexes.SearchIndexClient.getServiceStatisticsWithResponse#Context -->
     *
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return the search service statistics result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchServiceStatistics> getServiceStatisticsWithResponse(Context context) {
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getServiceStatisticsWithResponse(null,
            context), LOGGER);
    }

    /**
     * Convenience method to convert a {@link Class Class's} {@link Field Fields} and {@link Method Methods} into {@link
     * SearchField SearchFields} to help aid the creation of a {@link SearchField} which represents the {@link Class}.
     *
     * @param model The model {@link Class} that will have {@link SearchField SearchFields} generated from its
     * structure.
     * @param options Configuration used to determine generation of the {@link SearchField SearchFields}.
     * @return A list {@link SearchField SearchFields} which represent the model {@link Class}.
     */
    public static List<SearchField> buildSearchFields(Class<?> model, FieldBuilderOptions options) {
        return SearchIndexAsyncClient.buildSearchFields(model, options);
    }
}
