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
import com.azure.search.documents.implementation.models.SearchContinuationToken;
import com.azure.search.documents.implementation.models.SearchDocumentsResult;
import com.azure.search.documents.implementation.models.SearchErrorException;
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
 * documents in an Azure Cognitive Search service.
 *
 * @see SearchClientBuilder
 */
@ServiceClient(builder = SearchClientBuilder.class)
public final class SearchClient {
    private static final ClientLogger LOGGER = new ClientLogger(SearchClient.class);

    /**
     * Search REST API Version
     */
    private final SearchServiceVersion serviceVersion;

    /**
     * The endpoint for the Azure Cognitive Search service.
     */
    private final String endpoint;

    /**
     * The name of the Azure Cognitive Search index.
     */
    private final String indexName;

    /**
     * The underlying AutoRest client used to interact with the Azure Cognitive Search service
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
     * Gets the name of the Azure Cognitive Search index.
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
     * Gets the endpoint for the Azure Cognitive Search service.
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
     * Retrieves a document from the Azure Cognitive Search index.
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
     * Retrieves a document from the Azure Cognitive Search index.
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
                .getWithResponse(key, selectedFields, null, Utility.enableSyncRestProxy(context));

            return new SimpleResponse<>(response, serializer.deserializeFromBytes(
                serializer.serializeToBytes(response.getValue()), createInstance(modelClass)));
        } catch (SearchErrorException ex) {
            throw LOGGER.logExceptionAsError(Utility.mapSearchErrorException(ex));
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
            .countWithResponse(null, Utility.enableSyncRestProxy(context)));
    }

    /**
     * Searches for documents in the Azure Cognitive Search index.
     * <p>
     * If {@code searchText} is set to null or {@code "*"} all documents will be matched, see
     * <a href="https://docs.microsoft.com/rest/api/searchservice/Simple-query-syntax-in-Azure-Search">simple query
     * syntax in Azure Cognitive Search</a> for more information about search query syntax.
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
     * for &#40;SearchPagedResponse resultResponse: searchPagedIterable.iterableByPage&#40;&#41;&#41; &#123;
     *     System.out.println&#40;&quot;The status code of the response is &quot; + resultResponse.getStatusCode&#40;&#41;&#41;;
     *     resultResponse.getValue&#40;&#41;.forEach&#40;searchResult -&gt; &#123;
     *         for &#40;Map.Entry&lt;String, Object&gt; keyValuePair: searchResult
     *             .getDocument&#40;SearchDocument.class&#41;.entrySet&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;Document key %s, document value %s&quot;, keyValuePair.getKey&#40;&#41;,
     *                 keyValuePair.getValue&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
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
     * Searches for documents in the Azure Cognitive Search index.
     * <p>
     * If {@code searchText} is set to null or {@code "*"} all documents will be matched, see
     * <a href="https://docs.microsoft.com/rest/api/searchservice/Simple-query-syntax-in-Azure-Search">simple query
     * syntax in Azure Cognitive Search</a> for more information about search query syntax.
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
     * for &#40;SearchPagedResponse resultResponse: searchPagedIterable.iterableByPage&#40;&#41;&#41; &#123;
     *     System.out.println&#40;&quot;The status code of the response is &quot; + resultResponse.getStatusCode&#40;&#41;&#41;;
     *     resultResponse.getValue&#40;&#41;.forEach&#40;searchResult -&gt; &#123;
     *         for &#40;Map.Entry&lt;String, Object&gt; keyValuePair: searchResult
     *             .getDocument&#40;SearchDocument.class&#41;.entrySet&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;Document key %s, document value %s&quot;, keyValuePair.getKey&#40;&#41;,
     *                 keyValuePair.getValue&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
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
            Response<SearchDocumentsResult> response = restClient.getDocuments().searchPostWithResponse(requestToUse, null, Utility.enableSyncRestProxy(context));
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
            Response<SuggestDocumentsResult> response = restClient.getDocuments().suggestPostWithResponse(suggestRequest, null, Utility.enableSyncRestProxy(context));
            SuggestDocumentsResult result = response.getValue();
            return new SuggestPagedResponse(new SimpleResponse<>(response, getSuggestResults(result, serializer)),
                result.getCoverage());
        });
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
            Response<AutocompleteResult> response = restClient.getDocuments().autocompletePostWithResponse(request, null, Utility.enableSyncRestProxy(context));
            return new AutocompletePagedResponse(new SimpleResponse<>(response, response.getValue()));
        });
    }
}
