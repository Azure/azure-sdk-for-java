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
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.search.documents.implementation.SearchIndexClientImpl;
import com.azure.search.documents.implementation.converters.IndexActionConverter;
import com.azure.search.documents.implementation.models.AutocompleteRequest;
import com.azure.search.documents.implementation.models.ErrorResponseException;
import com.azure.search.documents.implementation.models.SearchContinuationToken;
import com.azure.search.documents.implementation.models.SearchDocumentsResult;
import com.azure.search.documents.implementation.models.SearchFirstPageResponseWrapper;
import com.azure.search.documents.implementation.models.SearchRequest;
import com.azure.search.documents.implementation.models.SuggestDocumentsResult;
import com.azure.search.documents.implementation.models.SuggestRequest;
import com.azure.search.documents.implementation.util.Utility;
import com.azure.search.documents.indexes.models.IndexDocumentsBatch;
import com.azure.search.documents.models.AutocompleteOptions;
import com.azure.search.documents.models.AutocompleteResult;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexBatchException;
import com.azure.search.documents.models.IndexDocumentsOptions;
import com.azure.search.documents.models.IndexDocumentsResult;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.models.SuggestOptions;
import com.azure.search.documents.models.SuggestResult;
import com.azure.search.documents.util.AutocompletePagedIterable;
import com.azure.search.documents.util.AutocompletePagedResponse;
import com.azure.search.documents.util.SearchPagedIterable;
import com.azure.search.documents.util.SearchPagedResponse;
import com.azure.search.documents.util.SuggestPagedIterable;
import com.azure.search.documents.util.SuggestPagedResponse;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.core.util.serializer.TypeReference.createInstance;
import static com.azure.search.documents.SearchAsyncClient.buildIndexBatch;
import static com.azure.search.documents.SearchAsyncClient.createAutoCompleteRequest;
import static com.azure.search.documents.SearchAsyncClient.createContinuationToken;
import static com.azure.search.documents.SearchAsyncClient.createSearchRequest;
import static com.azure.search.documents.SearchAsyncClient.createSuggestRequest;
import static com.azure.search.documents.SearchAsyncClient.getSearchResults;
import static com.azure.search.documents.SearchAsyncClient.getSuggestResults;

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
 *     This client provides a synchronous API for accessing and performing operations on indexed documents. This client
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
 * <!-- src_embed com.azure.search.documents.SearchClient-classLevelJavaDoc.instantiationWithSearchClientBuilder -->
 * <pre>
 * SearchClient searchClient = new SearchClientBuilder&#40;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .indexName&#40;&quot;&#123;indexName&#125;&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.SearchClient-classLevelJavaDoc.instantiationWithSearchClientBuilder -->
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
 * <!-- src_embed com.azure.search.documents.SearchClient-classLevelJavaDoc.uploadDocument#Map-boolean -->
 * <pre>
 * List&lt;Hotel&gt; hotels = new ArrayList&lt;&gt;&#40;&#41;;
 * hotels.add&#40;new Hotel&#40;&#41;.setHotelId&#40;&quot;100&quot;&#41;&#41;;
 * hotels.add&#40;new Hotel&#40;&#41;.setHotelId&#40;&quot;200&quot;&#41;&#41;;
 * hotels.add&#40;new Hotel&#40;&#41;.setHotelId&#40;&quot;300&quot;&#41;&#41;;
 * searchClient.uploadDocuments&#40;hotels&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.SearchClient-classLevelJavaDoc.uploadDocument#Map-boolean -->
 *
 * <em>
 *     For an asynchronous sample see {@link SearchAsyncClient#uploadDocuments(Iterable)}.
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
 * <!-- src_embed com.azure.search.documents.SearchClient-classLevelJavaDoc.mergeDocument#Map -->
 * <pre>
 * List&lt;Hotel&gt; hotels = new ArrayList&lt;&gt;&#40;&#41;;
 * hotels.add&#40;new Hotel&#40;&#41;.setHotelId&#40;&quot;100&quot;&#41;&#41;;
 * hotels.add&#40;new Hotel&#40;&#41;.setHotelId&#40;&quot;200&quot;&#41;&#41;;
 * searchClient.mergeDocuments&#40;hotels&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.SearchClient-classLevelJavaDoc.mergeDocument#Map -->
 *
 * <em>
 *     For an asynchronous sample see {@link SearchAsyncClient#mergeDocuments(Iterable)}.
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
 * <!-- src_embed com.azure.search.documents.SearchClient-classLevelJavaDoc.deleteDocument#String -->
 * <pre>
 * SearchDocument documentId = new SearchDocument&#40;&#41;;
 * documentId.put&#40;&quot;hotelId&quot;, &quot;100&quot;&#41;;
 * searchClient.deleteDocuments&#40;Collections.singletonList&#40;documentId&#41;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.SearchClient-classLevelJavaDoc.deleteDocument#String -->
 *
 * <em>
 *     For an asynchronous sample see {@link SearchAsyncClient#deleteDocuments(Iterable)}.
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
 * <!-- src_embed com.azure.search.documents.SearchClient-classLevelJavaDoc.getDocument#String-Class -->
 * <pre>
 * Hotel hotel = searchClient.getDocument&#40;&quot;100&quot;, Hotel.class&#41;;
 * System.out.printf&#40;&quot;Retrieved Hotel %s%n&quot;, hotel.getHotelId&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.SearchClient-classLevelJavaDoc.getDocument#String-Class -->
 *
 * <em>
 *     For an asynchronous sample see {@link SearchAsyncClient#getDocument(String, Class)}.
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
 * <!-- src_embed com.azure.search.documents.SearchClient-classLevelJavaDoc.searchDocuments#String -->
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
 * searchClient.uploadDocuments&#40;searchDocuments&#41;;
 *
 * SearchPagedIterable results = searchClient.search&#40;&quot;SearchText&quot;&#41;;
 * System.out.printf&#40;&quot;There are %s results.%n&quot;, results.getTotalCount&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.SearchClient-classLevelJavaDoc.searchDocuments#String -->
 * <em>
 *     For an asynchronous sample see {@link SearchAsyncClient#search(String)}.
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
 * <!-- src_embed com.azure.search.documents.SearchClient-classLevelJavaDoc.suggestDocuments#String-String -->
 * <pre>
 * SuggestPagedIterable suggestPagedIterable = searchClient.suggest&#40;&quot;searchText&quot;, &quot;sg&quot;&#41;;
 * for &#40;SuggestResult result: suggestPagedIterable&#41; &#123;
 *     System.out.printf&#40;&quot;The suggested text is %s&quot;, result.getText&#40;&#41;&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.search.documents.SearchClient-classLevelJavaDoc.suggestDocuments#String-String -->
 *
 * <em>
 *     For an asynchronous sample see {@link SearchAsyncClient#suggest(String, String)}.
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
 * <!-- src_embed com.azure.search.documents.SearchClient-classLevelJavaDoc.autocomplete#String-String -->
 * <pre>
 * AutocompletePagedIterable autocompletePagedIterable = searchClient.autocomplete&#40;&quot;searchText&quot;, &quot;sg&quot;&#41;;
 * for &#40;AutocompleteItem result: autocompletePagedIterable&#41; &#123;
 *     System.out.printf&#40;&quot;The complete term is %s&quot;, result.getText&#40;&#41;&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.search.documents.SearchClient-classLevelJavaDoc.autocomplete#String-String -->
 *
 * <em>
 *     For an asynchronous sample see {@link SearchAsyncClient#autocomplete(String, String)}.
 * </em>
 *
 * @see SearchAsyncClient
 * @see SearchClientBuilder
 * @see com.azure.search.documents
 */
@ServiceClient(builder = SearchClientBuilder.class)
public final class SearchClient {
    private static final ClientLogger LOGGER = new ClientLogger(SearchClient.class);

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
    SearchClient(String endpoint, String indexName, SearchServiceVersion serviceVersion,
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
     * <!-- src_embed com.azure.search.documents.SearchClient.uploadDocuments#Iterable -->
     * <pre>
     * SearchDocument searchDocument = new SearchDocument&#40;&#41;;
     * searchDocument.put&#40;&quot;hotelId&quot;, &quot;1&quot;&#41;;
     * searchDocument.put&#40;&quot;hotelName&quot;, &quot;test&quot;&#41;;
     * IndexDocumentsResult result = SEARCH_CLIENT.uploadDocuments&#40;Collections.singletonList&#40;searchDocument&#41;&#41;;
     * for &#40;IndexingResult indexingResult : result.getResults&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Does document with key %s upload successfully? %b%n&quot;, indexingResult.getKey&#40;&#41;,
     *         indexingResult.isSucceeded&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.SearchClient.uploadDocuments#Iterable -->
     *
     * @param documents collection of documents to upload to the target Index.
     * @return document index result.
     * @throws IndexBatchException If some of the indexing actions fail but other actions succeed and modify the state
     * of the index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly
     * catch this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing
     * result reports the status of each indexing action in the batch, making it possible to determine the state of the
     * index after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public IndexDocumentsResult uploadDocuments(Iterable<?> documents) {
        return uploadDocumentsWithResponse(documents, null, Context.NONE).getValue();
    }

    /**
     * Uploads a collection of documents to the target index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Upload dynamic SearchDocument. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchClient.uploadDocumentsWithResponse#Iterable-IndexDocumentsOptions-Context -->
     * <pre>
     * SearchDocument searchDocument = new SearchDocument&#40;&#41;;
     * searchDocument.put&#40;&quot;hotelId&quot;, &quot;1&quot;&#41;;
     * searchDocument.put&#40;&quot;hotelName&quot;, &quot;test&quot;&#41;;
     * Response&lt;IndexDocumentsResult&gt; resultResponse = SEARCH_CLIENT.uploadDocumentsWithResponse&#40;
     *     Collections.singletonList&#40;searchDocument&#41;, null, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.println&#40;&quot;The status code of the response is &quot; + resultResponse.getStatusCode&#40;&#41;&#41;;
     * for &#40;IndexingResult indexingResult : resultResponse.getValue&#40;&#41;.getResults&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Does document with key %s upload successfully? %b%n&quot;, indexingResult.getKey&#40;&#41;,
     *         indexingResult.isSucceeded&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.SearchClient.uploadDocumentsWithResponse#Iterable-IndexDocumentsOptions-Context -->
     *
     * @param documents collection of documents to upload to the target Index.
     * @param options Options that allow specifying document indexing behavior.
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return response containing the document index result.
     * @throws IndexBatchException If some of the indexing actions fail but other actions succeed and modify the state
     * of the index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly
     * catch this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing
     * result reports the status of each indexing action in the batch, making it possible to determine the state of the
     * index after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<IndexDocumentsResult> uploadDocumentsWithResponse(Iterable<?> documents,
        IndexDocumentsOptions options, Context context) {
        return indexDocumentsWithResponse(buildIndexBatch(documents, IndexActionType.UPLOAD), options, context);
    }

    /**
     * Merges a collection of documents with existing documents in the target index.
     * <p>
     * If the type of the document contains non-nullable primitive-typed properties, these properties may not merge
     * correctly. If you do not set such a property, it will automatically take its default value (for example, {@code
     * 0} for {@code int} or false for {@code boolean}), which will override the value of the property currently stored
     * in the index, even if this was not your intent. For this reason, it is strongly recommended that you always
     * declare primitive-typed properties with their class equivalents (for example, an integer property should be of
     * type {@code Integer} instead of {@code int}).
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Merge dynamic SearchDocument. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchClient.mergeDocuments#Iterable -->
     * <pre>
     * SearchDocument searchDocument = new SearchDocument&#40;&#41;;
     * searchDocument.put&#40;&quot;hotelName&quot;, &quot;merge&quot;&#41;;
     * IndexDocumentsResult result = SEARCH_CLIENT.mergeDocuments&#40;Collections.singletonList&#40;searchDocument&#41;&#41;;
     * for &#40;IndexingResult indexingResult : result.getResults&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Does document with key %s merge successfully? %b%n&quot;, indexingResult.getKey&#40;&#41;,
     *         indexingResult.isSucceeded&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.SearchClient.mergeDocuments#Iterable -->
     *
     * @param documents collection of documents to be merged
     * @return document index result
     * @throws IndexBatchException If some of the indexing actions fail but other actions succeed and modify the state
     * of the index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly
     * catch this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing
     * result reports the status of each indexing action in the batch, making it possible to determine the state of the
     * index after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public IndexDocumentsResult mergeDocuments(Iterable<?> documents) {
        return mergeDocumentsWithResponse(documents, null, Context.NONE).getValue();
    }

    /**
     * Merges a collection of documents with existing documents in the target index.
     * <p>
     * If the type of the document contains non-nullable primitive-typed properties, these properties may not merge
     * correctly. If you do not set such a property, it will automatically take its default value (for example, {@code
     * 0} for {@code int} or false for {@code boolean}), which will override the value of the property currently stored
     * in the index, even if this was not your intent. For this reason, it is strongly recommended that you always
     * declare primitive-typed properties with their class equivalents (for example, an integer property should be of
     * type {@code Integer} instead of {@code int}).
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Merge dynamic SearchDocument. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchClient.mergeDocumentsWithResponse#Iterable-IndexDocumentsOptions-Context -->
     * <pre>
     * SearchDocument searchDocument = new SearchDocument&#40;&#41;;
     * searchDocument.put&#40;&quot;hotelName&quot;, &quot;test&quot;&#41;;
     * Response&lt;IndexDocumentsResult&gt; resultResponse = SEARCH_CLIENT.mergeDocumentsWithResponse&#40;
     *     Collections.singletonList&#40;searchDocument&#41;, null, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.println&#40;&quot;The status code of the response is &quot; + resultResponse.getStatusCode&#40;&#41;&#41;;
     * for &#40;IndexingResult indexingResult : resultResponse.getValue&#40;&#41;.getResults&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Does document with key %s merge successfully? %b%n&quot;, indexingResult.getKey&#40;&#41;,
     *         indexingResult.isSucceeded&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.SearchClient.mergeDocumentsWithResponse#Iterable-IndexDocumentsOptions-Context -->
     *
     * @param documents collection of documents to be merged.
     * @param options Options that allow specifying document indexing behavior.
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return response containing the document index result.
     * @throws IndexBatchException If some of the indexing actions fail but other actions succeed and modify the state
     * of the index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly
     * catch this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing
     * result reports the status of each indexing action in the batch, making it possible to determine the state of the
     * index after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<IndexDocumentsResult> mergeDocumentsWithResponse(Iterable<?> documents,
        IndexDocumentsOptions options, Context context) {
        return indexDocumentsWithResponse(buildIndexBatch(documents, IndexActionType.MERGE), options, context);
    }

    /**
     * This action behaves like merge if a document with the given key already exists in the index. If the document does
     * not exist, it behaves like upload with a new document.
     * <p>
     * If the type of the document contains non-nullable primitive-typed properties, these properties may not merge
     * correctly. If you do not set such a property, it will automatically take its default value (for example, {@code
     * 0} for {@code int} or false for {@code boolean}), which will override the value of the property currently stored
     * in the index, even if this was not your intent. For this reason, it is strongly recommended that you always
     * declare primitive-typed properties with their class equivalents (for example, an integer property should be of
     * type {@code Integer} instead of {@code int}).
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Merge or upload dynamic SearchDocument. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchClient.mergeOrUploadDocuments#Iterable -->
     * <pre>
     * SearchDocument searchDocument = new SearchDocument&#40;&#41;;
     * searchDocument.put&#40;&quot;hotelId&quot;, &quot;1&quot;&#41;;
     * searchDocument.put&#40;&quot;hotelName&quot;, &quot;test&quot;&#41;;
     * IndexDocumentsResult result = SEARCH_CLIENT.mergeOrUploadDocuments&#40;Collections.singletonList&#40;searchDocument&#41;&#41;;
     * for &#40;IndexingResult indexingResult : result.getResults&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Does document with key %s mergeOrUpload successfully? %b%n&quot;, indexingResult.getKey&#40;&#41;,
     *         indexingResult.isSucceeded&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.SearchClient.mergeOrUploadDocuments#Iterable -->
     *
     * @param documents collection of documents to be merged, if exists, otherwise uploaded
     * @return document index result
     * @throws IndexBatchException If some of the indexing actions fail but other actions succeed and modify the state
     * of the index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly
     * catch this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing
     * result reports the status of each indexing action in the batch, making it possible to determine the state of the
     * index after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public IndexDocumentsResult mergeOrUploadDocuments(Iterable<?> documents) {
        return mergeOrUploadDocumentsWithResponse(documents, null, Context.NONE).getValue();
    }

    /**
     * This action behaves like merge if a document with the given key already exists in the index. If the document does
     * not exist, it behaves like upload with a new document.
     * <p>
     * If the type of the document contains non-nullable primitive-typed properties, these properties may not merge
     * correctly. If you do not set such a property, it will automatically take its default value (for example, {@code
     * 0} for {@code int} or false for {@code boolean}), which will override the value of the property currently stored
     * in the index, even if this was not your intent. For this reason, it is strongly recommended that you always
     * declare primitive-typed properties with their class equivalents (for example, an integer property should be of
     * type {@code Integer} instead of {@code int}).
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Merge or upload dynamic SearchDocument. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchClient.mergeOrUploadDocumentsWithResponse#Iterable-IndexDocumentsOptions-Context -->
     * <pre>
     * SearchDocument searchDocument = new SearchDocument&#40;&#41;;
     * searchDocument.put&#40;&quot;hotelId&quot;, &quot;1&quot;&#41;;
     * searchDocument.put&#40;&quot;hotelName&quot;, &quot;test&quot;&#41;;
     * Response&lt;IndexDocumentsResult&gt; resultResponse = SEARCH_CLIENT.mergeOrUploadDocumentsWithResponse&#40;
     *     Collections.singletonList&#40;searchDocument&#41;, null, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.println&#40;&quot;The status code of the response is &quot; + resultResponse.getStatusCode&#40;&#41;&#41;;
     * for &#40;IndexingResult indexingResult : resultResponse.getValue&#40;&#41;.getResults&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Does document with key %s mergeOrUpload successfully? %b%n&quot;, indexingResult.getKey&#40;&#41;,
     *         indexingResult.isSucceeded&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.SearchClient.mergeOrUploadDocumentsWithResponse#Iterable-IndexDocumentsOptions-Context -->
     *
     * @param documents collection of documents to be merged, if exists, otherwise uploaded
     * @param options Options that allow specifying document indexing behavior.
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return response containing a document index result
     * @throws IndexBatchException If some of the indexing actions fail but other actions succeed and modify the state
     * of the index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly
     * catch this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing
     * result reports the status of each indexing action in the batch, making it possible to determine the state of the
     * index after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<IndexDocumentsResult> mergeOrUploadDocumentsWithResponse(Iterable<?> documents,
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
     * <!-- src_embed com.azure.search.documents.SearchClient.deleteDocuments#Iterable -->
     * <pre>
     * SearchDocument searchDocument = new SearchDocument&#40;&#41;;
     * searchDocument.put&#40;&quot;hotelId&quot;, &quot;1&quot;&#41;;
     * searchDocument.put&#40;&quot;hotelName&quot;, &quot;test&quot;&#41;;
     * IndexDocumentsResult result = SEARCH_CLIENT.deleteDocuments&#40;Collections.singletonList&#40;searchDocument&#41;&#41;;
     * for &#40;IndexingResult indexingResult : result.getResults&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Does document with key %s delete successfully? %b%n&quot;, indexingResult.getKey&#40;&#41;,
     *         indexingResult.isSucceeded&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.SearchClient.deleteDocuments#Iterable -->
     *
     * @param documents collection of documents to delete from the target Index. Fields other than the key are ignored.
     * @return document index result.
     * @throws IndexBatchException If some of the indexing actions fail but other actions succeed and modify the state
     * of the index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly
     * catch this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing
     * result reports the status of each indexing action in the batch, making it possible to determine the state of the
     * index after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public IndexDocumentsResult deleteDocuments(Iterable<?> documents) {
        return deleteDocumentsWithResponse(documents, null, Context.NONE).getValue();
    }

    /**
     * Deletes a collection of documents from the target index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete dynamic SearchDocument. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchClient.deleteDocumentsWithResponse#Iterable-IndexDocumentsOptions-Context -->
     * <pre>
     * SearchDocument searchDocument = new SearchDocument&#40;&#41;;
     * searchDocument.put&#40;&quot;hotelId&quot;, &quot;1&quot;&#41;;
     * searchDocument.put&#40;&quot;hotelName&quot;, &quot;test&quot;&#41;;
     * Response&lt;IndexDocumentsResult&gt; resultResponse = SEARCH_CLIENT.deleteDocumentsWithResponse&#40;
     *     Collections.singletonList&#40;searchDocument&#41;, null, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.println&#40;&quot;The status code of the response is &quot; + resultResponse.getStatusCode&#40;&#41;&#41;;
     * for &#40;IndexingResult indexingResult : resultResponse.getValue&#40;&#41;.getResults&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Does document with key %s delete successfully? %b%n&quot;, indexingResult.getKey&#40;&#41;,
     *         indexingResult.isSucceeded&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.SearchClient.deleteDocumentsWithResponse#Iterable-IndexDocumentsOptions-Context -->
     *
     * @param documents collection of documents to delete from the target Index. Fields other than the key are ignored.
     * @param options Options that allow specifying document indexing behavior.
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return response containing a document index result.
     * @throws IndexBatchException If some of the indexing actions fail but other actions succeed and modify the state
     * of the index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly
     * catch this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing
     * result reports the status of each indexing action in the batch, making it possible to determine the state of the
     * index after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<IndexDocumentsResult> deleteDocumentsWithResponse(Iterable<?> documents,
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
     * <!-- src_embed com.azure.search.documents.SearchClient.indexDocuments#IndexDocumentsBatch -->
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
     * IndexDocumentsResult result = SEARCH_CLIENT.indexDocuments&#40;indexDocumentsBatch&#41;;
     * for &#40;IndexingResult indexingResult : result.getResults&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Does document with key %s finish successfully? %b%n&quot;, indexingResult.getKey&#40;&#41;,
     *         indexingResult.isSucceeded&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.SearchClient.indexDocuments#IndexDocumentsBatch -->
     *
     * @param batch The batch of index actions
     * @return Response containing the status of operations for all actions in the batch
     * @throws IndexBatchException If some of the indexing actions fail but other actions succeed and modify the state
     * of the index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly
     * catch this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing
     * result reports the status of each indexing action in the batch, making it possible to determine the state of the
     * index after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public IndexDocumentsResult indexDocuments(IndexDocumentsBatch<?> batch) {
        return indexDocumentsWithResponse(batch, null, Context.NONE).getValue();
    }

    /**
     * Sends a batch of upload, merge, and/or delete actions to the search index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Index batch operation on dynamic SearchDocument. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-Context -->
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
     * Response&lt;IndexDocumentsResult&gt; resultResponse = SEARCH_CLIENT.indexDocumentsWithResponse&#40;indexDocumentsBatch,
     *     null, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.println&#40;&quot;The status code of the response is &quot; + resultResponse.getStatusCode&#40;&#41;&#41;;
     * for &#40;IndexingResult indexingResult : resultResponse.getValue&#40;&#41;.getResults&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Does document with key %s finish successfully? %b%n&quot;, indexingResult.getKey&#40;&#41;,
     *         indexingResult.isSucceeded&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.SearchClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-Context -->
     *
     * @param batch The batch of index actions
     * @param options Options that allow specifying document indexing behavior.
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return Response containing the status of operations for all actions in the batch
     * @throws IndexBatchException If some of the indexing actions fail but other actions succeed and modify the state
     * of the index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly
     * catch this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing
     * result reports the status of each indexing action in the batch, making it possible to determine the state of the
     * index after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<IndexDocumentsResult> indexDocumentsWithResponse(IndexDocumentsBatch<?> batch,
        IndexDocumentsOptions options, Context context) {
        List<com.azure.search.documents.implementation.models.IndexAction> indexActions = batch.getActions()
            .stream()
            .map(document -> IndexActionConverter.map(document, serializer))
            .collect(Collectors.toList());

        boolean throwOnAnyError = options == null || options.throwOnAnyError();
        return Utility.indexDocumentsWithResponse(restClient, indexActions, throwOnAnyError, context, LOGGER);
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
     * <!-- src_embed com.azure.search.documents.SearchClient.getDocuments#String-Class -->
     * <pre>
     * SearchDocument result = SEARCH_CLIENT.getDocument&#40;&quot;hotelId&quot;, SearchDocument.class&#41;;
     * for &#40;Map.Entry&lt;String, Object&gt; keyValuePair : result.entrySet&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Document key %s, Document value %s&quot;, keyValuePair.getKey&#40;&#41;, keyValuePair.getValue&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.SearchClient.getDocuments#String-Class -->
     *
     * @param key The key of the document to retrieve.
     * @param modelClass The model class converts to.
     * @param <T> Convert document to the generic type.
     * @return document object
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/Lookup-Document">Lookup document</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> T getDocument(String key, Class<T> modelClass) {
        return getDocumentWithResponse(key, modelClass, null, Context.NONE).getValue();
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
     * <!-- src_embed com.azure.search.documents.SearchClient.getDocumentWithResponse#String-Class-List-Context -->
     * <pre>
     * Response&lt;SearchDocument&gt; resultResponse = SEARCH_CLIENT.getDocumentWithResponse&#40;&quot;hotelId&quot;,
     *     SearchDocument.class, null, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.println&#40;&quot;The status code of the response is &quot; + resultResponse.getStatusCode&#40;&#41;&#41;;
     * for &#40;Map.Entry&lt;String, Object&gt; keyValuePair : resultResponse.getValue&#40;&#41;.entrySet&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Document key %s, Document value %s&quot;, keyValuePair.getKey&#40;&#41;, keyValuePair.getValue&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.SearchClient.getDocumentWithResponse#String-Class-List-Context -->
     *
     * @param <T> Convert document to the generic type.
     * @param key The key of the document to retrieve.
     * @param modelClass The model class converts to.
     * @param selectedFields List of field names to retrieve for the document; Any field not retrieved will have null or
     * default as its corresponding property value in the returned object.
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return response containing a document object
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/Lookup-Document">Lookup document</a>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Response<T> getDocumentWithResponse(String key, Class<T> modelClass, List<String> selectedFields,
        Context context) {

        try {
            Response<Map<String, Object>> response = restClient.getDocuments()
                .getWithResponse(key, selectedFields, null, context);

            return new SimpleResponse<>(response, serializer.deserializeFromBytes(
                serializer.serializeToBytes(response.getValue()), createInstance(modelClass)));
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(Utility.mapErrorResponseException(ex));
        } catch (RuntimeException ex) {
            throw LOGGER.logExceptionAsError(ex);
        }
    }

    /**
     * Queries the number of documents in the search index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get document count. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchClient.getDocumentCount -->
     * <pre>
     * long count = SEARCH_CLIENT.getDocumentCount&#40;&#41;;
     * System.out.printf&#40;&quot;There are %d documents in service.&quot;, count&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.SearchClient.getDocumentCount -->
     *
     * @return the number of documents.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public long getDocumentCount() {
        return getDocumentCountWithResponse(Context.NONE).getValue();
    }

    /**
     * Queries the number of documents in the search index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get document count. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchClient.getDocumentCountWithResponse#Context -->
     * <pre>
     * Response&lt;Long&gt; countResponse = SEARCH_CLIENT.getDocumentCountWithResponse&#40;new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.println&#40;&quot;The status code of the response is &quot; + countResponse.getStatusCode&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;There are %d documents in service.&quot;, countResponse.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.search.documents.SearchClient.getDocumentCountWithResponse#Context -->
     *
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return response containing the number of documents.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Long> getDocumentCountWithResponse(Context context) {
        return Utility.executeRestCallWithExceptionHandling(() -> restClient.getDocuments()
            .countWithResponse(null, context), LOGGER);
    }

    /**
     * Searches for documents in the Azure AI Search index.
     * <p>
     * If {@code searchText} is set to null or {@code "*"} all documents will be matched, see
     * <a href="https://docs.microsoft.com/rest/api/searchservice/Simple-query-syntax-in-Azure-Search">simple query
     * syntax in Azure AI Search</a> for more information about search query syntax.
     * <p>
     * The {@link SearchPagedIterable} will iterate through search result pages until all search results are returned.
     * Each page is determined by the {@code $skip} and {@code $top} values and the Search service has a limit on the
     * number of documents that can be skipped, more information about the {@code $skip} limit can be found at
     * <a href="https://learn.microsoft.com/rest/api/searchservice/search-documents">Search Documents REST API</a> and
     * reading the {@code $skip} description. If the total number of results exceeds the {@code $skip} limit the
     * {@link SearchPagedIterable} won't prevent you from exceeding the {@code $skip} limit. To prevent exceeding the
     * limit you can track the number of documents returned and stop requesting new pages when the limit is reached.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Search text from documents in service. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchClient.search#String -->
     * <pre>
     * SearchPagedIterable searchPagedIterable = SEARCH_CLIENT.search&#40;&quot;searchText&quot;&#41;;
     * System.out.printf&#40;&quot;There are around %d results.&quot;, searchPagedIterable.getTotalCount&#40;&#41;&#41;;
     *
     * long numberOfDocumentsReturned = 0;
     * for &#40;SearchPagedResponse resultResponse: searchPagedIterable.iterableByPage&#40;&#41;&#41; &#123;
     *     System.out.println&#40;&quot;The status code of the response is &quot; + resultResponse.getStatusCode&#40;&#41;&#41;;
     *     numberOfDocumentsReturned += resultResponse.getValue&#40;&#41;.size&#40;&#41;;
     *     resultResponse.getValue&#40;&#41;.forEach&#40;searchResult -&gt; &#123;
     *         for &#40;Map.Entry&lt;String, Object&gt; keyValuePair: searchResult
     *             .getDocument&#40;SearchDocument.class&#41;.entrySet&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;Document key %s, document value %s&quot;, keyValuePair.getKey&#40;&#41;,
     *                 keyValuePair.getValue&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     *
     *     if &#40;numberOfDocumentsReturned &gt;= SEARCH_SKIP_LIMIT&#41; &#123;
     *         &#47;&#47; Reached the $skip limit, stop requesting more documents.
     *         break;
     *     &#125;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.SearchClient.search#String -->
     *
     * @param searchText A full-text search query expression.
     * @return A {@link SearchPagedIterable} that iterates over {@link SearchResult} objects and provides access to the
     * {@link SearchPagedResponse} object for each page containing HTTP response and count, facet, and coverage
     * information.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/Search-Documents">Search documents</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SearchPagedIterable search(String searchText) {
        return search(searchText, null, Context.NONE);
    }

    /**
     * Searches for documents in the Azure AI Search index.
     * <p>
     * If {@code searchText} is set to null or {@code "*"} all documents will be matched, see
     * <a href="https://docs.microsoft.com/rest/api/searchservice/Simple-query-syntax-in-Azure-Search">simple query
     * syntax in Azure AI Search</a> for more information about search query syntax.
     * <p>
     * The {@link SearchPagedIterable} will iterate through search result pages until all search results are returned.
     * Each page is determined by the {@code $skip} and {@code $top} values and the Search service has a limit on the
     * number of documents that can be skipped, more information about the {@code $skip} limit can be found at
     * <a href="https://learn.microsoft.com/rest/api/searchservice/search-documents">Search Documents REST API</a> and
     * reading the {@code $skip} description. If the total number of results exceeds the {@code $skip} limit the
     * {@link SearchPagedIterable} won't prevent you from exceeding the {@code $skip} limit. To prevent exceeding the
     * limit you can track the number of documents returned and stop requesting new pages when the limit is reached.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Search text from documents in service with option. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchClient.search#String-SearchOptions-Context -->
     * <pre>
     * SearchPagedIterable searchPagedIterable = SEARCH_CLIENT.search&#40;&quot;searchText&quot;,
     *     new SearchOptions&#40;&#41;.setOrderBy&#40;&quot;hotelId desc&quot;&#41;, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * System.out.printf&#40;&quot;There are around %d results.&quot;, searchPagedIterable.getTotalCount&#40;&#41;&#41;;
     *
     * long numberOfDocumentsReturned = 0;
     * for &#40;SearchPagedResponse resultResponse: searchPagedIterable.iterableByPage&#40;&#41;&#41; &#123;
     *     System.out.println&#40;&quot;The status code of the response is &quot; + resultResponse.getStatusCode&#40;&#41;&#41;;
     *     numberOfDocumentsReturned += resultResponse.getValue&#40;&#41;.size&#40;&#41;;
     *     resultResponse.getValue&#40;&#41;.forEach&#40;searchResult -&gt; &#123;
     *         for &#40;Map.Entry&lt;String, Object&gt; keyValuePair: searchResult
     *             .getDocument&#40;SearchDocument.class&#41;.entrySet&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;Document key %s, document value %s&quot;, keyValuePair.getKey&#40;&#41;,
     *                 keyValuePair.getValue&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     *
     *     if &#40;numberOfDocumentsReturned &gt;= SEARCH_SKIP_LIMIT&#41; &#123;
     *         &#47;&#47; Reached the $skip limit, stop requesting more documents.
     *         break;
     *     &#125;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.SearchClient.search#String-SearchOptions-Context -->
     *
     * @param searchText A full-text search query expression.
     * @param searchOptions Parameters to further refine the search query
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return A {@link SearchPagedIterable} that iterates over {@link SearchResult} objects and provides access to the
     * {@link SearchPagedResponse} object for each page containing HTTP response and count, facet, and coverage
     * information.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/Search-Documents">Search documents</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SearchPagedIterable search(String searchText, SearchOptions searchOptions, Context context) {
        SearchRequest request = createSearchRequest(searchText, searchOptions);
        // The firstPageResponse shared among all functional calls below.
        // Do not initial new instance directly in func call.
        final SearchFirstPageResponseWrapper firstPageResponseWrapper = new SearchFirstPageResponseWrapper();
        Function<String, SearchPagedResponse> func = continuationToken ->
            search(request, continuationToken, firstPageResponseWrapper, context);
        return new SearchPagedIterable(() -> func.apply(null), func);
    }

    private SearchPagedResponse search(SearchRequest request, String continuationToken,
                                             SearchFirstPageResponseWrapper firstPageResponseWrapper, Context context) {
        if (continuationToken == null && firstPageResponseWrapper.getFirstPageResponse() != null) {
            return firstPageResponseWrapper.getFirstPageResponse();
        }
        SearchRequest requestToUse = (continuationToken == null)
            ? request
            : SearchContinuationToken.deserializeToken(serviceVersion.getVersion(), continuationToken);

        return Utility.executeRestCallWithExceptionHandling(() -> {
            Response<SearchDocumentsResult> response = restClient.getDocuments()
                .searchPostWithResponse(requestToUse, null, context);
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
        }, LOGGER);
    }

    /**
     * Suggests documents in the index that match the given partial query.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Suggest text from documents in service. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchClient.suggest#String-String -->
     * <pre>
     * SuggestPagedIterable suggestPagedIterable = SEARCH_CLIENT.suggest&#40;&quot;searchText&quot;, &quot;sg&quot;&#41;;
     * for &#40;SuggestResult result: suggestPagedIterable&#41; &#123;
     *     SearchDocument searchDocument = result.getDocument&#40;SearchDocument.class&#41;;
     *     for &#40;Map.Entry&lt;String, Object&gt; keyValuePair: searchDocument.entrySet&#40;&#41;&#41; &#123;
     *         System.out.printf&#40;&quot;Document key %s, document value %s&quot;, keyValuePair.getKey&#40;&#41;, keyValuePair.getValue&#40;&#41;&#41;;
     *     &#125;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.SearchClient.suggest#String-String -->
     *
     * @param searchText The search text on which to base suggestions
     * @param suggesterName The name of the suggester as specified in the suggesters collection that's part of the index
     * definition
     * @return A {@link SuggestPagedIterable} that iterates over {@link SuggestResult} objects and provides access to
     * the {@link SuggestPagedResponse} object for each page containing HTTP response and coverage information.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/Suggestions">Suggestions</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SuggestPagedIterable suggest(String searchText, String suggesterName) {
        return suggest(searchText, suggesterName, null, Context.NONE);
    }

    /**
     * Suggests documents in the index that match the given partial query.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Suggest text from documents in service with option. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchClient.suggest#String-String-SuggestOptions-Context -->
     * <pre>
     * SuggestPagedIterable suggestPagedIterable = SEARCH_CLIENT.suggest&#40;&quot;searchText&quot;, &quot;sg&quot;,
     *     new SuggestOptions&#40;&#41;.setOrderBy&#40;&quot;hotelId desc&quot;&#41;, new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * for &#40;SuggestResult result: suggestPagedIterable&#41; &#123;
     *     SearchDocument searchDocument = result.getDocument&#40;SearchDocument.class&#41;;
     *     for &#40;Map.Entry&lt;String, Object&gt; keyValuePair: searchDocument.entrySet&#40;&#41;&#41; &#123;
     *         System.out.printf&#40;&quot;Document key %s, document value %s&quot;, keyValuePair.getKey&#40;&#41;, keyValuePair.getValue&#40;&#41;&#41;;
     *     &#125;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.SearchClient.suggest#String-String-SuggestOptions-Context -->
     *
     * @param searchText The search text on which to base suggestions
     * @param suggesterName The name of the suggester as specified in the suggesters collection that's part of the index
     * definition
     * @param suggestOptions Parameters to further refine the suggestion query.
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return A {@link SuggestPagedIterable} that iterates over {@link SuggestResult} objects and provides access to
     * the {@link SuggestPagedResponse} object for each page containing HTTP response and coverage information.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/Suggestions">Suggestions</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SuggestPagedIterable suggest(String searchText, String suggesterName, SuggestOptions suggestOptions,
        Context context) {
        SuggestRequest suggestRequest = createSuggestRequest(searchText,
            suggesterName, Utility.ensureSuggestOptions(suggestOptions));
        return new SuggestPagedIterable(() -> suggest(suggestRequest, context));
    }

    private SuggestPagedResponse suggest(SuggestRequest suggestRequest, Context context) {
        return Utility.executeRestCallWithExceptionHandling(() -> {
            Response<SuggestDocumentsResult> response = restClient.getDocuments()
                .suggestPostWithResponse(suggestRequest, null, context);
            SuggestDocumentsResult result = response.getValue();
            return new SuggestPagedResponse(new SimpleResponse<>(response, getSuggestResults(result, serializer)),
                result.getCoverage());
        }, LOGGER);
    }

    /**
     * Autocompletes incomplete query terms based on input text and matching terms in the index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Autocomplete text from documents in service. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchClient.autocomplete#String-String -->
     * <pre>
     * AutocompletePagedIterable autocompletePagedIterable = SEARCH_CLIENT.autocomplete&#40;&quot;searchText&quot;, &quot;sg&quot;&#41;;
     * for &#40;AutocompleteItem result: autocompletePagedIterable&#41; &#123;
     *     System.out.printf&#40;&quot;The complete term is %s&quot;, result.getText&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.SearchClient.autocomplete#String-String -->
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @return auto complete result.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public AutocompletePagedIterable autocomplete(String searchText, String suggesterName) {
        return autocomplete(searchText, suggesterName, null, Context.NONE);
    }

    /**
     * Autocompletes incomplete query terms based on input text and matching terms in the index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Autocomplete text from documents in service with option. </p>
     *
     * <!-- src_embed com.azure.search.documents.SearchClient.autocomplete#String-String-AutocompleteOptions-Context -->
     * <pre>
     * AutocompletePagedIterable autocompletePagedIterable = SEARCH_CLIENT.autocomplete&#40;&quot;searchText&quot;, &quot;sg&quot;,
     *     new AutocompleteOptions&#40;&#41;.setAutocompleteMode&#40;AutocompleteMode.ONE_TERM_WITH_CONTEXT&#41;,
     *     new Context&#40;KEY_1, VALUE_1&#41;&#41;;
     * for &#40;AutocompleteItem result: autocompletePagedIterable&#41; &#123;
     *     System.out.printf&#40;&quot;The complete term is %s&quot;, result.getText&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.search.documents.SearchClient.autocomplete#String-String-AutocompleteOptions-Context -->
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @param autocompleteOptions autocomplete options
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return auto complete result.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public AutocompletePagedIterable autocomplete(String searchText, String suggesterName,
        AutocompleteOptions autocompleteOptions, Context context) {
        AutocompleteRequest request = createAutoCompleteRequest(searchText, suggesterName, autocompleteOptions);

        return new AutocompletePagedIterable(() -> autocomplete(request, context));
    }

    private AutocompletePagedResponse autocomplete(AutocompleteRequest request, Context context) {
        return Utility.executeRestCallWithExceptionHandling(() -> {
            Response<AutocompleteResult> response = restClient.getDocuments()
                .autocompletePostWithResponse(request, null, context);
            return new AutocompletePagedResponse(new SimpleResponse<>(response, response.getValue()));
        }, LOGGER);
    }
}
