// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.ServiceVersion;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.search.documents.implementation.SearchIndexClientImpl;
import com.azure.search.documents.implementation.converters.IndexActionConverter;
import com.azure.search.documents.implementation.converters.SearchResultConverter;
import com.azure.search.documents.implementation.converters.SuggestResultConverter;
import com.azure.search.documents.implementation.models.AutocompleteRequest;
import com.azure.search.documents.implementation.models.SearchContinuationToken;
import com.azure.search.documents.implementation.models.SearchDocumentsResult;
import com.azure.search.documents.implementation.models.SearchFirstPageResponseWrapper;
import com.azure.search.documents.implementation.models.SearchRequest;
import com.azure.search.documents.implementation.models.SuggestDocumentsResult;
import com.azure.search.documents.implementation.models.SuggestRequest;
import com.azure.search.documents.implementation.util.MappingUtils;
import com.azure.search.documents.implementation.util.Utility;
import com.azure.search.documents.indexes.models.IndexDocumentsBatch;
import com.azure.search.documents.models.AutocompleteOptions;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexBatchException;
import com.azure.search.documents.models.IndexDocumentsOptions;
import com.azure.search.documents.models.IndexDocumentsResult;
import com.azure.search.documents.models.QueryAnswer;
import com.azure.search.documents.models.QueryAnswerType;
import com.azure.search.documents.models.QueryCaption;
import com.azure.search.documents.models.QueryCaptionType;
import com.azure.search.documents.models.ScoringParameter;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.models.SemanticSearchOptions;
import com.azure.search.documents.models.SuggestOptions;
import com.azure.search.documents.models.SuggestResult;
import com.azure.search.documents.models.VectorSearchOptions;
import com.azure.search.documents.util.AutocompletePagedFlux;
import com.azure.search.documents.util.AutocompletePagedResponse;
import com.azure.search.documents.util.SearchPagedFlux;
import com.azure.search.documents.util.SearchPagedResponse;
import com.azure.search.documents.util.SuggestPagedFlux;
import com.azure.search.documents.util.SuggestPagedResponse;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.serializer.TypeReference.createInstance;

/**
 * This class provides a client that contains the operations for querying an index and uploading, merging, or deleting
 * documents in an Azure AI Search service.
 *
 * <h2>
 *     Overview
 * </h2>
 *
 * <p>
 *     Conceptually, a document is an entity in your index. Mapping this concept to more familiar database equivalents:
 *     a search index equates to a table, and documents are roughly equivalent to rows in a table. Documents exist only
 *     in an index, and are retrieved only through queries that target the documents collection (/docs) of an index. All
 *     operations performed on the collection such as uploading, merging, deleting, or querying documents take place in
 *     the context of a single index, so the URL format document operations will always include /indexes/[index name]/docs
 *     for a given index name.
 * </p>
 *
 * <p>
 *     This client provides an asynchronous API for accessing and performing operations on indexed documents. This client
 *     assists with searching your indexed documents, autocompleting partially typed search terms based on documents within the index,
 *     suggesting the most likely matching text in documents as a user types. The client provides operations for adding, updating, and deleting
 *     documents from an index.
 * </p>
 *
 * <h2>
 *     Getting Started
 * </h2>
 *
 * <p>
 *     Authenticating and building instances of this client are handled by {@link SearchClientBuilder}. This sample shows
 *     you how to authenticate and create an instance of the client:
 * </p>
 *
 * <!-- src_embed com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.instantiationWithSearchClientBuilder -->
 * <pre>
 * SearchAsyncClient searchAsyncClient = new SearchClientBuilder&#40;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .indexName&#40;&quot;&#123;indexName&#125;&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.instantiationWithSearchClientBuilder -->
 *
 * <p>
 *     For more information on authentication and building, see the {@link SearchClientBuilder} documentation.
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
 *     Upload a Document
 * </h3>
 *
 * <p>
 *     The following sample uploads a new document to an index.
 * </p>
 *
 * <!-- src_embed com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.uploadDocument#Map-boolean -->
 * <pre>
 * List&lt;Hotel&gt; hotels = new ArrayList&lt;&gt;&#40;&#41;;
 * hotels.add&#40;new Hotel&#40;&#41;.setHotelId&#40;&quot;100&quot;&#41;&#41;;
 * hotels.add&#40;new Hotel&#40;&#41;.setHotelId&#40;&quot;200&quot;&#41;&#41;;
 * hotels.add&#40;new Hotel&#40;&#41;.setHotelId&#40;&quot;300&quot;&#41;&#41;;
 * searchAsyncClient.uploadDocuments&#40;hotels&#41;.block&#40;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.uploadDocument#Map-boolean -->
 *
 * <em>
 *     For a synchronous sample see {@link SearchClient#uploadDocuments(Iterable)}.
 * </em>
 *
 * <h3>
 *     Merge a Document
 * </h3>
 *
 * <p>
 *     The following sample merges documents in an index.
 * </p>
 *
 * <!-- src_embed com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.mergeDocument#Map -->
 * <pre>
 * List&lt;Hotel&gt; hotels = new ArrayList&lt;&gt;&#40;&#41;;
 * hotels.add&#40;new Hotel&#40;&#41;.setHotelId&#40;&quot;100&quot;&#41;&#41;;
 * hotels.add&#40;new Hotel&#40;&#41;.setHotelId&#40;&quot;200&quot;&#41;&#41;;
 * searchAsyncClient.mergeDocuments&#40;hotels&#41;.block&#40;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.mergeDocument#Map -->
 *
 * <em>
 *     For a synchronous sample see {@link SearchClient#mergeDocuments(Iterable)}.
 * </em>
 *
 * <h3>
 *     Delete a Document
 * </h3>
 *
 * <p>
 *     The following sample deletes a document from an index.
 * </p>
 *
 * <!-- src_embed com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.deleteDocument#String -->
 * <pre>
 * SearchDocument documentId = new SearchDocument&#40;&#41;;
 * documentId.put&#40;&quot;hotelId&quot;, &quot;100&quot;&#41;;
 * searchAsyncClient.deleteDocuments&#40;Collections.singletonList&#40;documentId&#41;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.deleteDocument#String -->
 *
 * <em>
 *     For a synchronous sample see {@link SearchClient#deleteDocuments(Iterable)}.
 * </em>
 *
 * <h3>
 *     Get a Document
 * </h3>
 *
 * <p>
 *     The following sample gets a document from an index.
 * </p>
 *
 * <!-- src_embed com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.getDocument#String-Class -->
 * <pre>
 * Hotel hotel = searchAsyncClient.getDocument&#40;&quot;100&quot;, Hotel.class&#41;.block&#40;&#41;;
 * if &#40;hotel != null&#41; &#123;
 *     System.out.printf&#40;&quot;Retrieved Hotel %s%n&quot;, hotel.getHotelId&#40;&#41;&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.getDocument#String-Class -->
 *
 * <em>
 *     For a synchronous sample see {@link SearchClient#getDocument(String, Class)}.
 * </em>
 *
 * <h3>
 *     Search Documents
 * </h3>
 *
 * <p>
 *     The following sample searches for documents within an index.
 * </p>
 *
 * <!-- src_embed com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.searchDocuments#String -->
 * <pre>
 * SearchDocument searchDocument = new SearchDocument&#40;&#41;;
 * searchDocument.put&#40;&quot;hotelId&quot;, &quot;8&quot;&#41;;
 * searchDocument.put&#40;&quot;description&quot;, &quot;budget&quot;&#41;;
 * searchDocument.put&#40;&quot;descriptionFr&quot;, &quot;motel&quot;&#41;;
 *
 * SearchDocument searchDocument1 = new SearchDocument&#40;&#41;;
 * searchDocument1.put&#40;&quot;hotelId&quot;, &quot;9&quot;&#41;;
 * searchDocument1.put&#40;&quot;description&quot;, &quot;budget&quot;&#41;;
 * searchDocument1.put&#40;&quot;descriptionFr&quot;, &quot;motel&quot;&#41;;
 *
 * List&lt;SearchDocument&gt; searchDocuments = new ArrayList&lt;&gt;&#40;&#41;;
 * searchDocuments.add&#40;searchDocument&#41;;
 * searchDocuments.add&#40;searchDocument1&#41;;
 * searchAsyncClient.uploadDocuments&#40;searchDocuments&#41;;
 *
 * SearchPagedFlux results = searchAsyncClient.search&#40;&quot;SearchText&quot;&#41;;
 * results.getTotalCount&#40;&#41;.subscribe&#40;total -&gt; System.out.printf&#40;&quot;There are %s results&quot;, total&#41;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.searchDocuments#String -->
 *
 * <em>
 *     For a synchronous sample see {@link SearchClient#search(String)}.
 * </em>
 *
 * <h3>
 *     Make a Suggestion
 * </h3>
 *
 * <p>
 *     The following sample suggests the most likely matching text in documents.
 * </p>
 *
 * <!-- src_embed com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.suggestDocuments#String-String -->
 * <pre>
 * SuggestPagedFlux results = searchAsyncClient.suggest&#40;&quot;searchText&quot;, &quot;sg&quot;&#41;;
 * results.subscribe&#40;item -&gt; &#123;
 *     System.out.printf&#40;&quot;The text '%s' was found.%n&quot;, item.getText&#40;&#41;&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.suggestDocuments#String-String -->
 *
 * <em>
 *     For a synchronous sample see {@link SearchClient#suggest(String, String)}.
 * </em>
 *
 * <h3>
 *     Provide an Autocompletion
 * </h3>
 *
 * <p>
 *     The following sample provides autocompletion for a partially typed query.
 * </p>
 *
 * <!-- src_embed com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.autocomplete#String-String -->
 * <pre>
 * AutocompletePagedFlux results = searchAsyncClient.autocomplete&#40;&quot;searchText&quot;, &quot;sg&quot;&#41;;
 * results.subscribe&#40;item -&gt; &#123;
 *     System.out.printf&#40;&quot;The text '%s' was found.%n&quot;, item.getText&#40;&#41;&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.SearchAsyncClient-classLevelJavaDoc.autocomplete#String-String -->
 *
 * <em>
 *     For a synchronous sample see {@link SearchClient#autocomplete(String, String)}.
 * </em>
 *
 * @see SearchClient
 * @see SearchClientBuilder
 * @see com.azure.search.documents
 */
@ServiceClient(builder = SearchClientBuilder.class, isAsync = true)
public final class SearchAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(SearchAsyncClient.class);

    /**
     * Search REST API Version
     */
    private final SearchServiceVersion serviceVersion;

    /**
     * The endpoint for the Azure AI Search service.
     */
    private final String endpoint;

    /**
     * The name of the Azure AI Search index.
     */
    private final String indexName;

    /**
     * The underlying AutoRest client used to interact with the Azure AI Search service
     */
    private final SearchIndexClientImpl restClient;

    /**
     * The pipeline that powers this client.
     */
    private final HttpPipeline httpPipeline;

    final JsonSerializer serializer;

    /**
     * Package private constructor to be used by {@link SearchClientBuilder}
     */
    SearchAsyncClient(String endpoint, String indexName, SearchServiceVersion serviceVersion,
        HttpPipeline httpPipeline, JsonSerializer serializer, SearchIndexClientImpl restClient) {
        this.endpoint = endpoint;
        this.indexName = indexName;
        this.serviceVersion = serviceVersion;
        this.httpPipeline = httpPipeline;
        this.serializer = serializer;
        this.restClient = restClient;
    }

    /**
     * Gets the name of the Azure AI Search index.
     *
     * @return the indexName value.
     */
    public String getIndexName() {
        return this.indexName;
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
     * Uploads a collection of documents to the target index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Upload dynamic SearchDocument. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchAsyncClient.uploadDocuments#Iterable -->
     * <pre>
     * SearchDocument searchDocument = new SearchDocument&#40;&#41;;
     * searchDocument.put&#40;&quot;hotelId&quot;, &quot;1&quot;&#41;;
     * searchDocument.put&#40;&quot;hotelName&quot;, &quot;test&quot;&#41;;
     * SEARCH_ASYNC_CLIENT.uploadDocuments&#40;Collections.singletonList&#40;searchDocument&#41;&#41;
     *     .subscribe&#40;result -&gt; &#123;
     *         for &#40;IndexingResult indexingResult : result.getResults&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;Does document with key %s upload successfully? %b%n&quot;,
     *                 indexingResult.getKey&#40;&#41;, indexingResult.isSucceeded&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.SearchAsyncClient.uploadDocuments#Iterable -->
     *
     * @param documents collection of documents to upload to the target Index.
     * @return The result of the document indexing actions.
     * @throws IndexBatchException If an indexing action fails but other actions succeed and modify the state of the
     * index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly catch
     * this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing result
     * reports the status of each indexing action in the batch, making it possible to determine the state of the index
     * after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<IndexDocumentsResult> uploadDocuments(Iterable<?> documents) {
        return uploadDocumentsWithResponse(documents, null).map(Response::getValue);
    }

    /**
     * Uploads a collection of documents to the target index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Upload dynamic SearchDocument. </p>
     *
     * <!-- src_embed
     * com.azure.search.documents.SearchAsyncClient.uploadDocumentsWithResponse#Iterable-IndexDocumentsOptions -->
     * <pre>
     * SearchDocument searchDocument = new SearchDocument&#40;&#41;;
     * searchDocument.put&#40;&quot;hotelId&quot;, &quot;1&quot;&#41;;
     * searchDocument.put&#40;&quot;hotelName&quot;, &quot;test&quot;&#41;;
     * searchAsyncClient.uploadDocumentsWithResponse&#40;Collections.singletonList&#40;searchDocument&#41;, null&#41;
     *     .subscribe&#40;resultResponse -&gt; &#123;
     *         System.out.println&#40;&quot;The status code of the response is &quot; + resultResponse.getStatusCode&#40;&#41;&#41;;
     *         for &#40;IndexingResult indexingResult : resultResponse.getValue&#40;&#41;.getResults&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;Does document with key %s upload successfully? %b%n&quot;, indexingResult.getKey&#40;&#41;,
     *                 indexingResult.isSucceeded&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.SearchAsyncClient.uploadDocumentsWithResponse#Iterable-IndexDocumentsOptions
     * -->
     *
     * @param documents collection of documents to upload to the target Index.
     * @param options Options that allow specifying document indexing behavior.
     * @return A response containing the result of the document indexing actions.
     * @throws IndexBatchException If an indexing action fails but other actions succeed and modify the state of the
     * index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly catch
     * this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing result
     * reports the status of each indexing action in the batch, making it possible to determine the state of the index
     * after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<IndexDocumentsResult>> uploadDocumentsWithResponse(Iterable<?> documents,
        IndexDocumentsOptions options) {
        return withContext(context -> uploadDocumentsWithResponse(documents, options, context));
    }

    Mono<Response<IndexDocumentsResult>> uploadDocumentsWithResponse(Iterable<?> documents,
        IndexDocumentsOptions options, Context context) {
        return indexDocumentsWithResponse(buildIndexBatch(documents, IndexActionType.UPLOAD), options, context);
    }

    /**
     * Merges a collection of documents with existing documents in the target index.
     * <p>
     * If the type of the document contains non-nullable primitive-typed properties, these properties may not merge
     * correctly. If you do not set such a property, it will automatically take its default value (for example,
     * {@code 0} for {@code int} or false for {@code boolean}), which will override the value of the property currently
     * stored in the index, even if this was not your intent. For this reason, it is strongly recommended that you
     * always declare primitive-typed properties with their class equivalents (for example, an integer property should
     * be of type {@code Integer} instead of {@code int}).
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Merge dynamic SearchDocument. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchAsyncClient.mergeDocuments#Iterable -->
     * <pre>
     * SearchDocument searchDocument = new SearchDocument&#40;&#41;;
     * searchDocument.put&#40;&quot;hotelName&quot;, &quot;merge&quot;&#41;;
     * SEARCH_ASYNC_CLIENT.mergeDocuments&#40;Collections.singletonList&#40;searchDocument&#41;&#41;
     *     .subscribe&#40;result -&gt; &#123;
     *         for &#40;IndexingResult indexingResult : result.getResults&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;Does document with key %s merge successfully? %b%n&quot;, indexingResult.getKey&#40;&#41;,
     *                 indexingResult.isSucceeded&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.SearchAsyncClient.mergeDocuments#Iterable -->
     *
     * @param documents collection of documents to be merged
     * @return document index result
     * @throws IndexBatchException If an indexing action fails but other actions succeed and modify the state of the
     * index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly catch
     * this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing result
     * reports the status of each indexing action in the batch, making it possible to determine the state of the index
     * after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<IndexDocumentsResult> mergeDocuments(Iterable<?> documents) {
        return mergeDocumentsWithResponse(documents, null).map(Response::getValue);
    }

    /**
     * Merges a collection of documents with existing documents in the target index.
     * <p>
     * If the type of the document contains non-nullable primitive-typed properties, these properties may not merge
     * correctly. If you do not set such a property, it will automatically take its default value (for example,
     * {@code 0} for {@code int} or false for {@code boolean}), which will override the value of the property currently
     * stored in the index, even if this was not your intent. For this reason, it is strongly recommended that you
     * always declare primitive-typed properties with their class equivalents (for example, an integer property should
     * be of type {@code Integer} instead of {@code int}).
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Merge dynamic SearchDocument. </p>
     *
     * <!-- src_embed
     * com.azure.search.documents.SearchAsyncClient.mergeDocumentsWithResponse#Iterable-IndexDocumentsOptions -->
     * <pre>
     * SearchDocument searchDocument = new SearchDocument&#40;&#41;;
     * searchDocument.put&#40;&quot;hotelName&quot;, &quot;test&quot;&#41;;
     * searchAsyncClient.mergeDocumentsWithResponse&#40;Collections.singletonList&#40;searchDocument&#41;, null&#41;
     *     .subscribe&#40;resultResponse -&gt; &#123;
     *         System.out.println&#40;&quot;The status code of the response is &quot; + resultResponse.getStatusCode&#40;&#41;&#41;;
     *         for &#40;IndexingResult indexingResult : resultResponse.getValue&#40;&#41;.getResults&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;Does document with key %s merge successfully? %b%n&quot;, indexingResult.getKey&#40;&#41;,
     *                 indexingResult.isSucceeded&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.SearchAsyncClient.mergeDocumentsWithResponse#Iterable-IndexDocumentsOptions
     * -->
     *
     * @param documents collection of documents to be merged
     * @param options Options that allow specifying document indexing behavior.
     * @return response containing the document index result.
     * @throws IndexBatchException If an indexing action fails but other actions succeed and modify the state of the
     * index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly catch
     * this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing result
     * reports the status of each indexing action in the batch, making it possible to determine the state of the index
     * after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<IndexDocumentsResult>> mergeDocumentsWithResponse(Iterable<?> documents,
        IndexDocumentsOptions options) {
        return withContext(context -> mergeDocumentsWithResponse(documents, options, context));
    }

    Mono<Response<IndexDocumentsResult>> mergeDocumentsWithResponse(Iterable<?> documents,
        IndexDocumentsOptions options, Context context) {
        return indexDocumentsWithResponse(buildIndexBatch(documents, IndexActionType.MERGE), options, context);
    }

    /**
     * This action behaves like merge if a document with the given key already exists in the index. If the document does
     * not exist, it behaves like upload with a new document.
     * <p>
     * If the type of the document contains non-nullable primitive-typed properties, these properties may not merge
     * correctly. If you do not set such a property, it will automatically take its default value (for example,
     * {@code 0} for {@code int} or false for {@code boolean}), which will override the value of the property currently
     * stored in the index, even if this was not your intent. For this reason, it is strongly recommended that you
     * always declare primitive-typed properties with their class equivalents (for example, an integer property should
     * be of type {@code Integer} instead of {@code int}).
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Merge or upload dynamic SearchDocument. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchAsyncClient.mergeOrUploadDocuments#Iterable -->
     * <pre>
     * SearchDocument searchDocument = new SearchDocument&#40;&#41;;
     * searchDocument.put&#40;&quot;hotelId&quot;, &quot;1&quot;&#41;;
     * searchDocument.put&#40;&quot;hotelName&quot;, &quot;test&quot;&#41;;
     * SEARCH_ASYNC_CLIENT.mergeOrUploadDocuments&#40;Collections.singletonList&#40;searchDocument&#41;&#41;
     *     .subscribe&#40;result -&gt; &#123;
     *         for &#40;IndexingResult indexingResult : result.getResults&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;Does document with key %s mergeOrUpload successfully? %b%n&quot;,
     *                 indexingResult.getKey&#40;&#41;, indexingResult.isSucceeded&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.SearchAsyncClient.mergeOrUploadDocuments#Iterable -->
     *
     * @param documents collection of documents to be merged, if exists, otherwise uploaded
     * @return document index result
     * @throws IndexBatchException If an indexing action fails but other actions succeed and modify the state of the
     * index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly catch
     * this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing result
     * reports the status of each indexing action in the batch, making it possible to determine the state of the index
     * after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<IndexDocumentsResult> mergeOrUploadDocuments(Iterable<?> documents) {
        return mergeOrUploadDocumentsWithResponse(documents, null).map(Response::getValue);
    }

    /**
     * This action behaves like merge if a document with the given key already exists in the index. If the document does
     * not exist, it behaves like upload with a new document.
     * <p>
     * If the type of the document contains non-nullable primitive-typed properties, these properties may not merge
     * correctly. If you do not set such a property, it will automatically take its default value (for example,
     * {@code 0} for {@code int} or false for {@code boolean}), which will override the value of the property currently
     * stored in the index, even if this was not your intent. For this reason, it is strongly recommended that you
     * always declare primitive-typed properties with their class equivalents (for example, an integer property should
     * be of type {@code Integer} instead of {@code int}).
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Merge or upload dynamic SearchDocument. </p>
     *
     * <!-- src_embed
     * com.azure.search.documents.SearchAsyncClient.mergeOrUploadDocumentsWithResponse#Iterable-IndexDocumentsOptions
     * -->
     * <pre>
     * SearchDocument searchDocument = new SearchDocument&#40;&#41;;
     * searchDocument.put&#40;&quot;hotelId&quot;, &quot;1&quot;&#41;;
     * searchDocument.put&#40;&quot;hotelName&quot;, &quot;test&quot;&#41;;
     * searchAsyncClient.mergeOrUploadDocumentsWithResponse&#40;Collections.singletonList&#40;searchDocument&#41;, null&#41;
     *     .subscribe&#40;resultResponse -&gt; &#123;
     *         System.out.println&#40;&quot;The status code of the response is &quot; + resultResponse.getStatusCode&#40;&#41;&#41;;
     *         for &#40;IndexingResult indexingResult : resultResponse.getValue&#40;&#41;.getResults&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;Does document with key %s mergeOrUpload successfully? %b%n&quot;,
     *                 indexingResult.getKey&#40;&#41;, indexingResult.isSucceeded&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end
     * com.azure.search.documents.SearchAsyncClient.mergeOrUploadDocumentsWithResponse#Iterable-IndexDocumentsOptions
     * -->
     *
     * @param documents collection of documents to be merged, if exists, otherwise uploaded
     * @param options Options that allow specifying document indexing behavior.
     * @return document index result
     * @throws IndexBatchException If an indexing action fails but other actions succeed and modify the state of the
     * index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly catch
     * this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing result
     * reports the status of each indexing action in the batch, making it possible to determine the state of the index
     * after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<IndexDocumentsResult>> mergeOrUploadDocumentsWithResponse(Iterable<?> documents,
        IndexDocumentsOptions options) {
        return withContext(context -> mergeOrUploadDocumentsWithResponse(documents, options, context));
    }

    Mono<Response<IndexDocumentsResult>> mergeOrUploadDocumentsWithResponse(Iterable<?> documents,
        IndexDocumentsOptions options, Context context) {
        return indexDocumentsWithResponse(buildIndexBatch(documents, IndexActionType.MERGE_OR_UPLOAD), options,
            context);
    }

    /**
     * Deletes a collection of documents from the target index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete dynamic SearchDocument. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchAsyncClient.deleteDocuments#Iterable -->
     * <pre>
     * SearchDocument searchDocument = new SearchDocument&#40;&#41;;
     * searchDocument.put&#40;&quot;hotelId&quot;, &quot;1&quot;&#41;;
     * searchDocument.put&#40;&quot;hotelName&quot;, &quot;test&quot;&#41;;
     * SEARCH_ASYNC_CLIENT.deleteDocuments&#40;Collections.singletonList&#40;searchDocument&#41;&#41;
     *     .subscribe&#40;result -&gt; &#123;
     *         for &#40;IndexingResult indexingResult : result.getResults&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;Does document with key %s delete successfully? %b%n&quot;, indexingResult.getKey&#40;&#41;,
     *                 indexingResult.isSucceeded&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.SearchAsyncClient.deleteDocuments#Iterable -->
     *
     * @param documents collection of documents to delete from the target Index. Fields other than the key are ignored.
     * @return document index result.
     * @throws IndexBatchException If an indexing action fails but other actions succeed and modify the state of the
     * index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly catch
     * this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing result
     * reports the status of each indexing action in the batch, making it possible to determine the state of the index
     * after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<IndexDocumentsResult> deleteDocuments(Iterable<?> documents) {
        return deleteDocumentsWithResponse(documents, null).map(Response::getValue);
    }

    /**
     * Deletes a collection of documents from the target index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete dynamic SearchDocument. </p>
     *
     * <!-- src_embed
     * com.azure.search.documents.SearchAsyncClient.deleteDocumentsWithResponse#Iterable-IndexDocumentsOptions -->
     * <pre>
     * SearchDocument searchDocument = new SearchDocument&#40;&#41;;
     * searchDocument.put&#40;&quot;hotelId&quot;, &quot;1&quot;&#41;;
     * searchDocument.put&#40;&quot;hotelName&quot;, &quot;test&quot;&#41;;
     * searchAsyncClient.deleteDocumentsWithResponse&#40;Collections.singletonList&#40;searchDocument&#41;, null&#41;
     *     .subscribe&#40;resultResponse -&gt; &#123;
     *         System.out.println&#40;&quot;The status code of the response is &quot; + resultResponse.getStatusCode&#40;&#41;&#41;;
     *         for &#40;IndexingResult indexingResult : resultResponse.getValue&#40;&#41;.getResults&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;Does document with key %s delete successfully? %b%n&quot;, indexingResult.getKey&#40;&#41;,
     *                 indexingResult.isSucceeded&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.SearchAsyncClient.deleteDocumentsWithResponse#Iterable-IndexDocumentsOptions
     * -->
     *
     * @param documents collection of documents to delete from the target Index. Fields other than the key are ignored.
     * @param options Options that allow specifying document indexing behavior.
     * @return response containing the document index result.
     * @throws IndexBatchException If an indexing action fails but other actions succeed and modify the state of the
     * index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly catch
     * this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing result
     * reports the status of each indexing action in the batch, making it possible to determine the state of the index
     * after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<IndexDocumentsResult>> deleteDocumentsWithResponse(Iterable<?> documents,
        IndexDocumentsOptions options) {
        return withContext(context -> deleteDocumentsWithResponse(documents, options, context));
    }

    Mono<Response<IndexDocumentsResult>> deleteDocumentsWithResponse(Iterable<?> documents,
        IndexDocumentsOptions options, Context context) {
        return indexDocumentsWithResponse(buildIndexBatch(documents, IndexActionType.DELETE), options, context);
    }

    /**
     * Sends a batch of upload, merge, and/or delete actions to the search index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Index batch operation on dynamic SearchDocument. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchAsyncClient.indexDocuments#IndexDocumentsBatch -->
     * <pre>
     * SearchDocument searchDocument1 = new SearchDocument&#40;&#41;;
     * searchDocument1.put&#40;&quot;hotelId&quot;, &quot;1&quot;&#41;;
     * searchDocument1.put&#40;&quot;hotelName&quot;, &quot;test1&quot;&#41;;
     * SearchDocument searchDocument2 = new SearchDocument&#40;&#41;;
     * searchDocument2.put&#40;&quot;hotelId&quot;, &quot;2&quot;&#41;;
     * searchDocument2.put&#40;&quot;hotelName&quot;, &quot;test2&quot;&#41;;
     * IndexDocumentsBatch&lt;SearchDocument&gt; indexDocumentsBatch = new IndexDocumentsBatch&lt;&gt;&#40;&#41;;
     * indexDocumentsBatch.addUploadActions&#40;Collections.singletonList&#40;searchDocument1&#41;&#41;;
     * indexDocumentsBatch.addDeleteActions&#40;Collections.singletonList&#40;searchDocument2&#41;&#41;;
     * SEARCH_ASYNC_CLIENT.indexDocuments&#40;indexDocumentsBatch&#41;
     *     .subscribe&#40;result -&gt; &#123;
     *         for &#40;IndexingResult indexingResult : result.getResults&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;Does document with key %s finish successfully? %b%n&quot;, indexingResult.getKey&#40;&#41;,
     *                 indexingResult.isSucceeded&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.SearchAsyncClient.indexDocuments#IndexDocumentsBatch -->
     *
     * @param batch The batch of index actions
     * @return Response containing the status of operations for all actions in the batch.
     * @throws IndexBatchException If an indexing action fails but other actions succeed and modify the state of the
     * index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly catch
     * this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing result
     * reports the status of each indexing action in the batch, making it possible to determine the state of the index
     * after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<IndexDocumentsResult> indexDocuments(IndexDocumentsBatch<?> batch) {
        return indexDocumentsWithResponse(batch, null).map(Response::getValue);
    }

    /**
     * Sends a batch of upload, merge, and/or delete actions to the search index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Index batch operation on dynamic SearchDocument. </p>
     *
     * <!-- src_embed
     * com.azure.search.documents.SearchAsyncClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions
     * -->
     * <pre>
     * SearchDocument searchDocument1 = new SearchDocument&#40;&#41;;
     * searchDocument1.put&#40;&quot;hotelId&quot;, &quot;1&quot;&#41;;
     * searchDocument1.put&#40;&quot;hotelName&quot;, &quot;test1&quot;&#41;;
     * SearchDocument searchDocument2 = new SearchDocument&#40;&#41;;
     * searchDocument2.put&#40;&quot;hotelId&quot;, &quot;2&quot;&#41;;
     * searchDocument2.put&#40;&quot;hotelName&quot;, &quot;test2&quot;&#41;;
     * IndexDocumentsBatch&lt;SearchDocument&gt; indexDocumentsBatch = new IndexDocumentsBatch&lt;&gt;&#40;&#41;;
     * indexDocumentsBatch.addUploadActions&#40;Collections.singletonList&#40;searchDocument1&#41;&#41;;
     * indexDocumentsBatch.addDeleteActions&#40;Collections.singletonList&#40;searchDocument2&#41;&#41;;
     * searchAsyncClient.indexDocumentsWithResponse&#40;indexDocumentsBatch, null&#41;
     *     .subscribe&#40;resultResponse -&gt; &#123;
     *         System.out.println&#40;&quot;The status code of the response is &quot; + resultResponse.getStatusCode&#40;&#41;&#41;;
     *         for &#40;IndexingResult indexingResult : resultResponse.getValue&#40;&#41;.getResults&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;Does document with key %s finish successfully? %b%n&quot;, indexingResult.getKey&#40;&#41;,
     *                 indexingResult.isSucceeded&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end
     * com.azure.search.documents.SearchAsyncClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions
     * -->
     *
     * @param batch The batch of index actions
     * @param options Options that allow specifying document indexing behavior.
     * @return Response containing the status of operations for all actions in the batch
     * @throws IndexBatchException If an indexing action fails but other actions succeed and modify the state of the
     * index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly catch
     * this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing result
     * reports the status of each indexing action in the batch, making it possible to determine the state of the index
     * after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<IndexDocumentsResult>> indexDocumentsWithResponse(IndexDocumentsBatch<?> batch,
        IndexDocumentsOptions options) {
        return withContext(context -> indexDocumentsWithResponse(batch, options, context));
    }

    Mono<Response<IndexDocumentsResult>> indexDocumentsWithResponse(IndexDocumentsBatch<?> batch,
        IndexDocumentsOptions options, Context context) {
        List<com.azure.search.documents.implementation.models.IndexAction> indexActions = batch.getActions()
            .stream()
            .map(document -> IndexActionConverter.map(document, serializer))
            .collect(Collectors.toList());

        boolean throwOnAnyError = options == null || options.throwOnAnyError();
        return Utility.indexDocumentsWithResponseAsync(restClient, indexActions, throwOnAnyError, context, LOGGER);
    }

    /**
     * Retrieves a document from the Azure AI Search index.
     * <p>
     * View <a href="https://docs.microsoft.com/rest/api/searchservice/Naming-rules">naming rules</a> for guidelines on
     * constructing valid document keys.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get dynamic SearchDocument. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchAsyncClient.getDocuments#String-Class -->
     * <pre>
     * SEARCH_ASYNC_CLIENT.getDocument&#40;&quot;hotelId&quot;, SearchDocument.class&#41;
     *     .subscribe&#40;result -&gt; &#123;
     *         for &#40;Map.Entry&lt;String, Object&gt; keyValuePair : result.entrySet&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;Document key %s, Document value %s&quot;, keyValuePair.getKey&#40;&#41;,
     *                 keyValuePair.getValue&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.SearchAsyncClient.getDocuments#String-Class -->
     *
     * @param key The key of the document to retrieve.
     * @param modelClass The model class converts to.
     * @param <T> Convert document to the generic type.
     * @return the document object
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/Lookup-Document">Lookup document</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<T> getDocument(String key, Class<T> modelClass) {
        return getDocumentWithResponse(key, modelClass, null).map(Response::getValue);
    }

    /**
     * Retrieves a document from the Azure AI Search index.
     * <p>
     * View <a href="https://docs.microsoft.com/rest/api/searchservice/Naming-rules">naming rules</a> for guidelines on
     * constructing valid document keys.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get dynamic SearchDocument. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchAsyncClient.getDocumentWithResponse#String-Class-List -->
     * <pre>
     * SEARCH_ASYNC_CLIENT.getDocumentWithResponse&#40;&quot;hotelId&quot;, SearchDocument.class, null&#41;
     *     .subscribe&#40;resultResponse -&gt; &#123;
     *         System.out.println&#40;&quot;The status code of the response is &quot; + resultResponse.getStatusCode&#40;&#41;&#41;;
     *         for &#40;Map.Entry&lt;String, Object&gt; keyValuePair : resultResponse.getValue&#40;&#41;.entrySet&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;Document key %s, Document value %s&quot;, keyValuePair.getKey&#40;&#41;,
     *                 keyValuePair.getValue&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.SearchAsyncClient.getDocumentWithResponse#String-Class-List -->
     *
     * @param <T> Convert document to the generic type.
     * @param key The key of the document to retrieve.
     * @param modelClass The model class converts to.
     * @param selectedFields List of field names to retrieve for the document; Any field not retrieved will have null or
     * default as its corresponding property value in the returned object.
     * @return a response containing the document object
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/Lookup-Document">Lookup document</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<Response<T>> getDocumentWithResponse(String key, Class<T> modelClass, List<String> selectedFields) {
        return withContext(context -> getDocumentWithResponse(key, modelClass, selectedFields, context));
    }

    <T> Mono<Response<T>> getDocumentWithResponse(String key, Class<T> modelClass, List<String> selectedFields,
        Context context) {
        try {
            return restClient.getDocuments()
                .getWithResponseAsync(key, selectedFields, null, context)
                .onErrorMap(Utility::exceptionMapper)
                .map(res -> new SimpleResponse<>(res, serializer.deserializeFromBytes(
                    serializer.serializeToBytes(res.getValue()), createInstance(modelClass))));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Queries the number of documents in the search index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get document count. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchAsyncClient.getDocumentCount -->
     * <pre>
     * SEARCH_ASYNC_CLIENT.getDocumentCount&#40;&#41;
     *     .subscribe&#40;count -&gt; System.out.printf&#40;&quot;There are %d documents in service.&quot;, count&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.SearchAsyncClient.getDocumentCount -->
     *
     * @return the number of documents.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Long> getDocumentCount() {
        return this.getDocumentCountWithResponse().map(Response::getValue);
    }

    /**
     * Queries the number of documents in the search index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get document count. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchAsyncClient.getDocumentCountWithResponse -->
     * <pre>
     * SEARCH_ASYNC_CLIENT.getDocumentCountWithResponse&#40;&#41;
     *     .subscribe&#40;countResponse -&gt; &#123;
     *         System.out.println&#40;&quot;The status code of the response is &quot; + countResponse.getStatusCode&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;There are %d documents in service.&quot;, countResponse.getValue&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.SearchAsyncClient.getDocumentCountWithResponse -->
     *
     * @return response containing the number of documents.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Long>> getDocumentCountWithResponse() {
        return withContext(this::getDocumentCountWithResponse);
    }

    Mono<Response<Long>> getDocumentCountWithResponse(Context context) {
        try {
            return restClient.getDocuments()
                .countWithResponseAsync(null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Searches for documents in the Azure AI Search index.
     * <p>
     * If {@code searchText} is set to null or {@code "*"} all documents will be matched, see
     * <a href="https://docs.microsoft.com/rest/api/searchservice/Simple-query-syntax-in-Azure-Search">simple query
     * syntax in Azure AI Search</a> for more information about search query syntax.
     * <p>
     * The {@link SearchPagedFlux} will iterate through search result pages until all search results are returned.
     * Each page is determined by the {@code $skip} and {@code $top} values and the Search service has a limit on the
     * number of documents that can be skipped, more information about the {@code $skip} limit can be found at
     * <a href="https://learn.microsoft.com/rest/api/searchservice/search-documents">Search Documents REST API</a> and
     * reading the {@code $skip} description. If the total number of results exceeds the {@code $skip} limit the
     * {@link SearchPagedFlux} won't prevent you from exceeding the {@code $skip} limit. To prevent exceeding the limit
     * you can track the number of documents returned and stop requesting new pages when the limit is reached.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Search text from documents in service. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchAsyncClient.search#String -->
     * <pre>
     * SearchPagedFlux searchPagedFlux = SEARCH_ASYNC_CLIENT.search&#40;&quot;searchText&quot;&#41;;
     * searchPagedFlux.getTotalCount&#40;&#41;.subscribe&#40;
     *     count -&gt; System.out.printf&#40;&quot;There are around %d results.&quot;, count&#41;&#41;;
     *
     * AtomicLong numberOfDocumentsReturned = new AtomicLong&#40;&#41;;
     * searchPagedFlux.byPage&#40;&#41;
     *     .takeUntil&#40;page -&gt; &#123;
     *         if &#40;numberOfDocumentsReturned.addAndGet&#40;page.getValue&#40;&#41;.size&#40;&#41;&#41; &gt;= SEARCH_SKIP_LIMIT&#41; &#123;
     *             &#47;&#47; Reached the $skip limit, stop requesting more documents.
     *             return true;
     *         &#125;
     *
     *         return false;
     *     &#125;&#41;
     *     .subscribe&#40;resultResponse -&gt; &#123;
     *         for &#40;SearchResult result: resultResponse.getValue&#40;&#41;&#41; &#123;
     *             SearchDocument searchDocument = result.getDocument&#40;SearchDocument.class&#41;;
     *             for &#40;Map.Entry&lt;String, Object&gt; keyValuePair: searchDocument.entrySet&#40;&#41;&#41; &#123;
     *                 System.out.printf&#40;&quot;Document key %s, document value %s&quot;, keyValuePair.getKey&#40;&#41;, keyValuePair.getValue&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.SearchAsyncClient.search#String -->
     *
     * @param searchText A full-text search query expression.
     * @return A {@link SearchPagedFlux} that iterates over {@link SearchResult} objects and provides access to the
     * {@link SearchPagedResponse} object for each page containing HTTP response and count, facet, and coverage
     * information.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/Search-Documents">Search documents</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SearchPagedFlux search(String searchText) {
        return this.search(searchText, null);
    }

    /**
     * Searches for documents in the Azure AI Search index.
     * <p>
     * If {@code searchText} is set to null or {@code "*"} all documents will be matched, see
     * <a href="https://docs.microsoft.com/rest/api/searchservice/Simple-query-syntax-in-Azure-Search">simple query
     * syntax in Azure AI Search</a> for more information about search query syntax.
     * <p>
     * The {@link SearchPagedFlux} will iterate through search result pages until all search results are returned.
     * Each page is determined by the {@code $skip} and {@code $top} values and the Search service has a limit on the
     * number of documents that can be skipped, more information about the {@code $skip} limit can be found at
     * <a href="https://learn.microsoft.com/rest/api/searchservice/search-documents">Search Documents REST API</a> and
     * reading the {@code $skip} description. If the total number of results exceeds the {@code $skip} limit the
     * {@link SearchPagedFlux} won't prevent you from exceeding the {@code $skip} limit. To prevent exceeding the limit
     * you can track the number of documents returned and stop requesting new pages when the limit is reached.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Search text from documents in service with option. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchAsyncClient.search#String-SearchOptions -->
     * <pre>
     * SearchPagedFlux pagedFlux = SEARCH_ASYNC_CLIENT.search&#40;&quot;searchText&quot;,
     *     new SearchOptions&#40;&#41;.setOrderBy&#40;&quot;hotelId desc&quot;&#41;&#41;;
     *
     * pagedFlux.getTotalCount&#40;&#41;.subscribe&#40;count -&gt; System.out.printf&#40;&quot;There are around %d results.&quot;, count&#41;&#41;;
     *
     * AtomicLong numberOfDocumentsReturned = new AtomicLong&#40;&#41;;
     * pagedFlux.byPage&#40;&#41;
     *     .takeUntil&#40;page -&gt; &#123;
     *         if &#40;numberOfDocumentsReturned.addAndGet&#40;page.getValue&#40;&#41;.size&#40;&#41;&#41; &gt;= SEARCH_SKIP_LIMIT&#41; &#123;
     *             &#47;&#47; Reached the $skip limit, stop requesting more documents.
     *             return true;
     *         &#125;
     *
     *         return false;
     *     &#125;&#41;
     *     .subscribe&#40;searchResultResponse -&gt; searchResultResponse.getValue&#40;&#41;.forEach&#40;searchDocument -&gt; &#123;
     *         for &#40;Map.Entry&lt;String, Object&gt; keyValuePair
     *             : searchDocument.getDocument&#40;SearchDocument.class&#41;.entrySet&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;Document key %s, document value %s&quot;, keyValuePair.getKey&#40;&#41;,
     *                 keyValuePair.getValue&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.SearchAsyncClient.search#String-SearchOptions -->
     *
     * @param searchText A full-text search query expression.
     * @param searchOptions Parameters to further refine the search query
     * @return A {@link SearchPagedFlux} that iterates over {@link SearchResult} objects and provides access to the
     * {@link SearchPagedResponse} object for each page containing HTTP response and count, facet, and coverage
     * information.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/Search-Documents">Search documents</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SearchPagedFlux search(String searchText, SearchOptions searchOptions) {
        SearchRequest request = createSearchRequest(searchText, searchOptions);
        // The firstPageResponse shared among all functional calls below.
        // Do not initial new instance directly in func call.
        final SearchFirstPageResponseWrapper firstPageResponse = new SearchFirstPageResponseWrapper();
        Function<String, Mono<SearchPagedResponse>> func = continuationToken -> withContext(context ->
            search(request, continuationToken, firstPageResponse, context));
        return new SearchPagedFlux(() -> func.apply(null), func);
    }

    SearchPagedFlux search(String searchText, SearchOptions searchOptions, Context context) {
        SearchRequest request = createSearchRequest(searchText, searchOptions);
        // The firstPageResponse shared among all functional calls below.
        // Do not initial new instance directly in func call.
        final SearchFirstPageResponseWrapper firstPageResponseWrapper = new SearchFirstPageResponseWrapper();
        Function<String, Mono<SearchPagedResponse>> func = continuationToken ->
            search(request, continuationToken, firstPageResponseWrapper, context);
        return new SearchPagedFlux(() -> func.apply(null), func);
    }

    private Mono<SearchPagedResponse> search(SearchRequest request, String continuationToken,
        SearchFirstPageResponseWrapper firstPageResponseWrapper, Context context) {
        if (continuationToken == null && firstPageResponseWrapper.getFirstPageResponse() != null) {
            return Mono.just(firstPageResponseWrapper.getFirstPageResponse());
        }
        SearchRequest requestToUse = (continuationToken == null)
            ? request
            : SearchContinuationToken.deserializeToken(serviceVersion.getVersion(), continuationToken);

        return restClient.getDocuments().searchPostWithResponseAsync(requestToUse, null, context)
            .onErrorMap(MappingUtils::exceptionMapper)
            .map(response -> {
                SearchDocumentsResult result = response.getValue();

                SearchPagedResponse page = new SearchPagedResponse(
                    new SimpleResponse<>(response, getSearchResults(result, serializer)),
                    createContinuationToken(result, serviceVersion), result.getFacets(), result.getCount(),
                    result.getCoverage(), result.getAnswers(), result.getSemanticPartialResponseReason(),
                    result.getSemanticPartialResponseType());
                if (continuationToken == null) {
                    firstPageResponseWrapper.setFirstPageResponse(page);
                }
                return page;
            });
    }

    static List<SearchResult> getSearchResults(SearchDocumentsResult result, JsonSerializer jsonSerializer) {
        return result.getResults().stream()
            .map(searchResult -> SearchResultConverter.map(searchResult, jsonSerializer))
            .collect(Collectors.toList());
    }

    static String createContinuationToken(SearchDocumentsResult result, ServiceVersion serviceVersion) {
        return SearchContinuationToken.serializeToken(serviceVersion.getVersion(), result.getNextLink(),
            result.getNextPageParameters());
    }

    /**
     * Suggests documents in the index that match the given partial query.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Suggest text from documents in service. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchAsyncClient.suggest#String-String -->
     * <pre>
     * SEARCH_ASYNC_CLIENT.suggest&#40;&quot;searchText&quot;, &quot;sg&quot;&#41;
     *     .subscribe&#40;results -&gt; &#123;
     *         for &#40;Map.Entry&lt;String, Object&gt; keyValuePair: results.getDocument&#40;SearchDocument.class&#41;.entrySet&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;Document key %s, document value %s&quot;, keyValuePair.getKey&#40;&#41;,
     *                 keyValuePair.getValue&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.SearchAsyncClient.suggest#String-String -->
     *
     * @param searchText The search text.
     * @param suggesterName The name of the suggester.
     * @return A {@link SuggestPagedFlux} that iterates over {@link SuggestResult} objects and provides access to the
     * {@link SuggestPagedResponse} object for each page containing HTTP response and coverage information.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/Suggestions">Suggestions</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SuggestPagedFlux suggest(String searchText, String suggesterName) {
        return suggest(searchText, suggesterName, null);
    }

    /**
     * Suggests documents in the index that match the given partial query.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Suggest text from documents in service with option. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchAsyncClient.suggest#String-String-SuggestOptions -->
     * <pre>
     * SEARCH_ASYNC_CLIENT.suggest&#40;&quot;searchText&quot;, &quot;sg&quot;,
     *     new SuggestOptions&#40;&#41;.setOrderBy&#40;&quot;hotelId desc&quot;&#41;&#41;
     *     .subscribe&#40;results -&gt; &#123;
     *         for &#40;Map.Entry&lt;String, Object&gt; keyValuePair: results.getDocument&#40;SearchDocument.class&#41;.entrySet&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;Document key %s, document value %s&quot;, keyValuePair.getKey&#40;&#41;,
     *                 keyValuePair.getValue&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.SearchAsyncClient.suggest#String-String-SuggestOptions -->
     *
     * @param searchText The search text.
     * @param suggesterName The name of the suggester.
     * @param suggestOptions Parameters to further refine the suggestion query.
     * @return A {@link SuggestPagedFlux} that iterates over {@link SuggestResult} objects and provides access to the
     * {@link SuggestPagedResponse} object for each page containing HTTP response and coverage information.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/Suggestions">Suggestions</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SuggestPagedFlux suggest(String searchText, String suggesterName, SuggestOptions suggestOptions) {
        SuggestRequest suggestRequest = createSuggestRequest(searchText, suggesterName,
            Utility.ensureSuggestOptions(suggestOptions));

        return new SuggestPagedFlux(() -> withContext(context -> suggest(suggestRequest, context)));
    }

    private Mono<SuggestPagedResponse> suggest(SuggestRequest suggestRequest, Context context) {
        return restClient.getDocuments().suggestPostWithResponseAsync(suggestRequest, null, context)
            .onErrorMap(MappingUtils::exceptionMapper)
            .map(response -> {
                SuggestDocumentsResult result = response.getValue();

                return new SuggestPagedResponse(new SimpleResponse<>(response, getSuggestResults(result, serializer)),
                    result.getCoverage());
            });
    }

    static List<SuggestResult> getSuggestResults(SuggestDocumentsResult result, JsonSerializer serializer) {
        return result.getResults().stream()
            .map(suggestResult -> SuggestResultConverter.map(suggestResult, serializer))
            .collect(Collectors.toList());
    }

    /**
     * Autocompletes incomplete query terms based on input text and matching terms in the index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Autocomplete text from documents in service. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchAsyncClient.autocomplete#String-String -->
     * <pre>
     * SEARCH_ASYNC_CLIENT.autocomplete&#40;&quot;searchText&quot;, &quot;sg&quot;&#41;
     *     .subscribe&#40;result -&gt; System.out.printf&#40;&quot;The complete term is %s&quot;, result.getText&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.SearchAsyncClient.autocomplete#String-String -->
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @return auto complete result.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public AutocompletePagedFlux autocomplete(String searchText, String suggesterName) {
        return autocomplete(searchText, suggesterName, null);
    }

    /**
     * Autocompletes incomplete query terms based on input text and matching terms in the index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Autocomplete text from documents in service with option. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchAsyncClient.autocomplete#String-String-AutocompleteOptions -->
     * <pre>
     * SEARCH_ASYNC_CLIENT.autocomplete&#40;&quot;searchText&quot;, &quot;sg&quot;,
     *     new AutocompleteOptions&#40;&#41;.setAutocompleteMode&#40;AutocompleteMode.ONE_TERM_WITH_CONTEXT&#41;&#41;
     *     .subscribe&#40;result -&gt;
     *         System.out.printf&#40;&quot;The complete term is %s&quot;, result.getText&#40;&#41;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.search.documents.SearchAsyncClient.autocomplete#String-String-AutocompleteOptions -->
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @param autocompleteOptions autocomplete options
     * @return auto complete result.
     */
    public AutocompletePagedFlux autocomplete(String searchText, String suggesterName,
        AutocompleteOptions autocompleteOptions) {
        AutocompleteRequest request = createAutoCompleteRequest(searchText, suggesterName, autocompleteOptions);

        return new AutocompletePagedFlux(() -> withContext(context -> autocomplete(request, context)));
    }

    AutocompletePagedFlux autocomplete(String searchText, String suggesterName, AutocompleteOptions autocompleteOptions,
        Context context) {
        AutocompleteRequest request = createAutoCompleteRequest(searchText, suggesterName, autocompleteOptions);

        return new AutocompletePagedFlux(() -> autocomplete(request, context));
    }

    private Mono<AutocompletePagedResponse> autocomplete(AutocompleteRequest request, Context context) {
        return restClient.getDocuments().autocompletePostWithResponseAsync(request, null, context)
            .onErrorMap(MappingUtils::exceptionMapper)
            .map(response -> new AutocompletePagedResponse(new SimpleResponse<>(response, response.getValue())));
    }

    /**
     * Create search request from search text and parameters
     *
     * @param searchText search text
     * @param options search options
     * @return SearchRequest
     */
    static SearchRequest createSearchRequest(String searchText, SearchOptions options) {
        SearchRequest request = new SearchRequest().setSearchText(searchText);

        if (options == null) {
            return request;
        }

        List<String> scoringParameters = options.getScoringParameters() == null
            ? null
            : options.getScoringParameters().stream().map(ScoringParameter::toString).collect(Collectors.toList());

        request.setQueryType(options.getQueryType())
            .setIncludeTotalResultCount(options.isTotalCountIncluded())
            .setFacets(options.getFacets())
            .setFilter(options.getFilter())
            .setHighlightFields(nullSafeStringJoin(options.getHighlightFields()))
            .setHighlightPostTag(options.getHighlightPostTag())
            .setHighlightPreTag(options.getHighlightPreTag())
            .setMinimumCoverage(options.getMinimumCoverage())
            .setOrderBy(nullSafeStringJoin(options.getOrderBy()))
            .setScoringParameters(scoringParameters)
            .setScoringProfile(options.getScoringProfile())
            .setSearchFields(nullSafeStringJoin(options.getSearchFields()))
            .setSearchMode(options.getSearchMode())
            .setScoringStatistics(options.getScoringStatistics())
            .setSessionId(options.getSessionId())
            .setSelect(nullSafeStringJoin(options.getSelect()))
            .setSkip(options.getSkip())
            .setTop(options.getTop());

        SemanticSearchOptions semanticSearchOptions = options.getSemanticSearchOptions();
        if (semanticSearchOptions != null) {
            Integer waitInMillis = semanticSearchOptions.getMaxWaitDuration() == null ? null
                : (int) semanticSearchOptions.getMaxWaitDuration().toMillis();
            request.setSemanticConfiguration(semanticSearchOptions.getSemanticConfigurationName())
                .setSemanticErrorHandling(semanticSearchOptions.getErrorMode())
                .setSemanticMaxWaitInMilliseconds(waitInMillis)
                .setAnswers(createSearchRequestAnswers(semanticSearchOptions.getQueryAnswer()))
                .setCaptions(createSearchRequestCaptions(semanticSearchOptions.getQueryCaption()))
                .setSemanticQuery(semanticSearchOptions.getSemanticQuery());
        }

        VectorSearchOptions vectorSearchOptions = options.getVectorSearchOptions();
        if (vectorSearchOptions != null) {
            request.setVectorFilterMode(vectorSearchOptions.getFilterMode())
                .setVectorQueries(vectorSearchOptions.getQueries());
        }


        return request;
    }

    static String createSearchRequestAnswers(QueryAnswer queryAnswer) {
        if (queryAnswer == null) {
            return null;
        }

        QueryAnswerType queryAnswerType = queryAnswer.getAnswerType();
        Integer answersCount = queryAnswer.getCount();
        Double answerThreshold = queryAnswer.getThreshold();

        // No answer has been defined.
        if (queryAnswerType == null) {
            return null;
        }

        String answerString = queryAnswerType.toString();

        if (answersCount != null && answerThreshold != null) {
            return answerString + "|count-" + answersCount + ",threshold-" + answerThreshold;
        } else if (answersCount != null) {
            return answerString + "|count-" + answersCount;
        } else if (answerThreshold != null) {
            return answerString + "|threshold-" + answerThreshold;
        } else {
            return answerString;
        }
    }

    static String createSearchRequestCaptions(QueryCaption queryCaption) {
        if (queryCaption == null) {
            return null;
        }

        QueryCaptionType queryCaptionType = queryCaption.getCaptionType();
        Boolean highlightEnabled = queryCaption.isHighlightEnabled();

        // No caption has been defined.
        if (queryCaptionType == null) {
            return null;
        }

        return highlightEnabled == null
            ? queryCaptionType.toString()
            : queryCaptionType + "|highlight-" + highlightEnabled;
    }

    /**
     * Create suggest request from search text, suggester name, and parameters
     *
     * @param searchText search text
     * @param suggesterName search text
     * @param options suggest options
     * @return SuggestRequest
     */
    static SuggestRequest createSuggestRequest(String searchText, String suggesterName,
        SuggestOptions options) {
        SuggestRequest request = new SuggestRequest(searchText, suggesterName);

        if (options == null) {
            return request;
        }

        return request.setFilter(options.getFilter())
            .setUseFuzzyMatching(options.useFuzzyMatching())
            .setHighlightPostTag(options.getHighlightPostTag())
            .setHighlightPreTag(options.getHighlightPreTag())
            .setMinimumCoverage(options.getMinimumCoverage())
            .setOrderBy(nullSafeStringJoin(options.getOrderBy()))
            .setSearchFields(nullSafeStringJoin(options.getSearchFields()))
            .setSelect(nullSafeStringJoin(options.getSelect()))
            .setTop(options.getTop());
    }

    /**
     * Create Autocomplete request from search text, suggester name, and parameters
     *
     * @param searchText search text
     * @param suggesterName search text
     * @param options autocomplete options
     * @return AutocompleteRequest
     */
    static AutocompleteRequest createAutoCompleteRequest(String searchText, String suggesterName,
        AutocompleteOptions options) {
        AutocompleteRequest request = new AutocompleteRequest(searchText, suggesterName);

        if (options == null) {
            return request;
        }

        return request.setAutocompleteMode(options.getAutocompleteMode())
            .setFilter(options.getFilter())
            .setUseFuzzyMatching(options.useFuzzyMatching())
            .setHighlightPostTag(options.getHighlightPostTag())
            .setHighlightPreTag(options.getHighlightPreTag())
            .setMinimumCoverage(options.getMinimumCoverage())
            .setSearchFields(nullSafeStringJoin(options.getSearchFields()))
            .setTop(options.getTop());
    }

    private static String nullSafeStringJoin(Iterable<? extends CharSequence> elements) {
        if (elements == null) {
            return null;
        }

        return String.join(",", elements);
    }

    static <T> IndexDocumentsBatch<T> buildIndexBatch(Iterable<T> documents, IndexActionType actionType) {
        List<IndexAction<T>> actions = new ArrayList<>();
        documents.forEach(d -> actions.add(new IndexAction<T>()
            .setActionType(actionType)
            .setDocument(d)));

        return new IndexDocumentsBatch<T>().addActions(actions);
    }

}
