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
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.search.documents.implementation.SearchIndexClientImpl;
import com.azure.search.documents.implementation.SearchIndexClientImplBuilder;
import com.azure.search.documents.implementation.converters.AutocompleteModeConverter;
import com.azure.search.documents.implementation.converters.FacetResultConverter;
import com.azure.search.documents.implementation.converters.IndexBatchBaseConverter;
import com.azure.search.documents.implementation.converters.IndexDocumentsResultConverter;
import com.azure.search.documents.implementation.converters.QueryTypeConverter;
import com.azure.search.documents.implementation.converters.SearchModeConverter;
import com.azure.search.documents.implementation.converters.SearchResultConverter;
import com.azure.search.documents.implementation.converters.SuggestResultConverter;
import com.azure.search.documents.implementation.models.AutocompleteRequest;
import com.azure.search.documents.implementation.models.SearchContinuationToken;
import com.azure.search.documents.implementation.models.SearchDocumentsResult;
import com.azure.search.documents.implementation.models.SearchFirstPageResponseWrapper;
import com.azure.search.documents.implementation.models.SearchRequest;
import com.azure.search.documents.implementation.models.SuggestDocumentsResult;
import com.azure.search.documents.implementation.models.SuggestRequest;
import com.azure.search.documents.implementation.util.DocumentResponseConversions;
import com.azure.search.documents.implementation.util.MappingUtils;
import com.azure.search.documents.implementation.util.SuggestOptionsHandler;
import com.azure.search.documents.indexes.models.IndexDocumentsBatch;
import com.azure.search.documents.models.AutocompleteOptions;
import com.azure.search.documents.models.FacetResult;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexBatchException;
import com.azure.search.documents.models.IndexDocumentsOptions;
import com.azure.search.documents.models.IndexDocumentsResult;
import com.azure.search.documents.models.ScoringParameter;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.models.SuggestOptions;
import com.azure.search.documents.models.SuggestResult;
import com.azure.search.documents.util.AutocompletePagedFlux;
import com.azure.search.documents.util.AutocompletePagedResponse;
import com.azure.search.documents.util.SearchPagedFlux;
import com.azure.search.documents.util.SearchPagedResponse;
import com.azure.search.documents.util.SuggestPagedFlux;
import com.azure.search.documents.util.SuggestPagedResponse;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.serializer.TypeReference.createInstance;
import static com.azure.search.documents.implementation.util.Utility.initializeSerializerAdapter;

/**
 * This class provides a client that contains the operations for querying an index and uploading, merging, or deleting
 * documents in an Azure Cognitive Search service.
 *
 * @see SearchClientBuilder
 */
@ServiceClient(builder = SearchClientBuilder.class, isAsync = true)
public final class SearchAsyncClient {
    /*
     * Representation of the Multi-Status HTTP response code.
     */
    private static final int MULTI_STATUS_CODE = 207;

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
     * The logger to be used
     */
    private final ClientLogger logger = new ClientLogger(SearchAsyncClient.class);

    /**
     * The underlying AutoRest client used to interact with the Azure Cognitive Search service
     */
    private final SearchIndexClientImpl restClient;

    /**
     * The pipeline that powers this client.
     */
    private final HttpPipeline httpPipeline;

    private final JsonSerializer serializer;

    private static final SerializerAdapter ADAPTER = initializeSerializerAdapter();

    /**
     * Package private constructor to be used by {@link SearchClientBuilder}
     */
    SearchAsyncClient(String endpoint, String indexName, SearchServiceVersion serviceVersion,
        HttpPipeline httpPipeline, JsonSerializer serializer) {
        this.endpoint = endpoint;
        this.indexName = indexName;
        this.serviceVersion = serviceVersion;
        this.httpPipeline = httpPipeline;
        this.serializer = serializer;

        restClient = new SearchIndexClientImplBuilder()
            .endpoint(endpoint)
            .indexName(indexName)
            .pipeline(httpPipeline)
            .serializerAdapter(ADAPTER)
            .buildClient();
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
     * {@codesnippet com.azure.search.documents.SearchAsyncClient.uploadDocuments#Iterable}
     *
     * @param documents collection of documents to upload to the target Index.
     * @return The result of the document indexing actions.
     * @throws IndexBatchException If some of the indexing actions fail but other actions succeed and modify the state
     * of the index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly
     * catch this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing
     * result reports the status of each indexing action in the batch, making it possible to determine the state of the
     * index after a partial failure.
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
     * {@codesnippet com.azure.search.documents.SearchAsyncClient.uploadDocumentsWithResponse#Iterable-IndexDocumentsOptions}
     *
     * @param documents collection of documents to upload to the target Index.
     * @param options Options that allow specifying document indexing behavior.
     * @return A response containing the result of the document indexing actions.
     * @throws IndexBatchException If some of the indexing actions fail but other actions succeed and modify the state
     * of the index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly
     * catch this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing
     * result reports the status of each indexing action in the batch, making it possible to determine the state of the
     * index after a partial failure.
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
     * correctly. If you do not set such a property, it will automatically take its default value (for example, {@code
     * 0} for {@code int} or {@code false} for {@code boolean}), which will override the value of the property currently
     * stored in the index, even if this was not your intent. For this reason, it is strongly recommended that you
     * always declare primitive-typed properties with their class equivalents (for example, an integer property should
     * be of type {@code Integer} instead of {@code int}).
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Merge dynamic SearchDocument. </p>
     *
     * {@codesnippet com.azure.search.documents.SearchAsyncClient.mergeDocuments#Iterable}
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
    public Mono<IndexDocumentsResult> mergeDocuments(Iterable<?> documents) {
        return mergeDocumentsWithResponse(documents, null).map(Response::getValue);
    }

    /**
     * Merges a collection of documents with existing documents in the target index.
     * <p>
     * If the type of the document contains non-nullable primitive-typed properties, these properties may not merge
     * correctly. If you do not set such a property, it will automatically take its default value (for example, {@code
     * 0} for {@code int} or {@code false} for {@code boolean}), which will override the value of the property currently
     * stored in the index, even if this was not your intent. For this reason, it is strongly recommended that you
     * always declare primitive-typed properties with their class equivalents (for example, an integer property should
     * be of type {@code Integer} instead of {@code int}).
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Merge dynamic SearchDocument. </p>
     *
     * {@codesnippet com.azure.search.documents.SearchAsyncClient.mergeDocumentsWithResponse#Iterable-IndexDocumentsOptions}
     *
     * @param documents collection of documents to be merged
     * @param options Options that allow specifying document indexing behavior.
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
     * correctly. If you do not set such a property, it will automatically take its default value (for example, {@code
     * 0} for {@code int} or {@code false} for {@code boolean}), which will override the value of the property currently
     * stored in the index, even if this was not your intent. For this reason, it is strongly recommended that you
     * always declare primitive-typed properties with their class equivalents (for example, an integer property should
     * be of type {@code Integer} instead of {@code int}).
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Merge or upload dynamic SearchDocument. </p>
     *
     * {@codesnippet com.azure.search.documents.SearchAsyncClient.mergeOrUploadDocuments#Iterable}
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
    public Mono<IndexDocumentsResult> mergeOrUploadDocuments(Iterable<?> documents) {
        return mergeOrUploadDocumentsWithResponse(documents, null).map(Response::getValue);
    }

    /**
     * This action behaves like merge if a document with the given key already exists in the index. If the document does
     * not exist, it behaves like upload with a new document.
     * <p>
     * If the type of the document contains non-nullable primitive-typed properties, these properties may not merge
     * correctly. If you do not set such a property, it will automatically take its default value (for example, {@code
     * 0} for {@code int} or {@code false} for {@code boolean}), which will override the value of the property currently
     * stored in the index, even if this was not your intent. For this reason, it is strongly recommended that you
     * always declare primitive-typed properties with their class equivalents (for example, an integer property should
     * be of type {@code Integer} instead of {@code int}).
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Merge or upload dynamic SearchDocument. </p>
     *
     * {@codesnippet com.azure.search.documents.SearchAsyncClient.mergeOrUploadDocumentsWithResponse#Iterable-IndexDocumentsOptions}
     *
     * @param documents collection of documents to be merged, if exists, otherwise uploaded
     * @param options Options that allow specifying document indexing behavior.
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
     * {@codesnippet com.azure.search.documents.SearchAsyncClient.deleteDocuments#Iterable}
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
     * {@codesnippet com.azure.search.documents.SearchAsyncClient.deleteDocumentsWithResponse#Iterable-IndexDocumentsOptions}
     *
     * @param documents collection of documents to delete from the target Index. Fields other than the key are ignored.
     * @param options Options that allow specifying document indexing behavior.
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
     * {@codesnippet com.azure.search.documents.SearchAsyncClient.indexDocuments#IndexDocumentsBatch}
     *
     * @param batch The batch of index actions
     * @return Response containing the status of operations for all actions in the batch.
     * @throws IndexBatchException If some of the indexing actions fail but other actions succeed and modify the state
     * of the index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly
     * catch this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing
     * result reports the status of each indexing action in the batch, making it possible to determine the state of the
     * index after a partial failure.
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
     * {@codesnippet com.azure.search.documents.SearchAsyncClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions}
     *
     * @param batch The batch of index actions
     * @param options Options that allow specifying document indexing behavior.
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
    public Mono<Response<IndexDocumentsResult>> indexDocumentsWithResponse(IndexDocumentsBatch<?> batch,
        IndexDocumentsOptions options) {
        return withContext(context -> indexDocumentsWithResponse(batch, options, context));
    }

    Mono<Response<IndexDocumentsResult>> indexDocumentsWithResponse(IndexDocumentsBatch<?> batch,
        IndexDocumentsOptions options, Context context) {
        try {
            IndexDocumentsOptions documentsOptions = (options == null) ? new IndexDocumentsOptions() : options;
            return restClient.getDocuments()
                .indexWithResponseAsync(IndexBatchBaseConverter.map(batch, serializer), null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .flatMap(response -> (response.getStatusCode() == MULTI_STATUS_CODE
                    && documentsOptions.throwOnAnyError())
                    ? Mono.error(new IndexBatchException(IndexDocumentsResultConverter.map(response.getValue())))
                    : Mono.just(response).map(MappingUtils::mappingIndexDocumentResultResponse));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * {@codesnippet com.azure.search.documents.SearchAsyncClient.getDocuments#String-Class}
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
     * Retrieves a document from the Azure Cognitive Search index.
     * <p>
     * View <a href="https://docs.microsoft.com/rest/api/searchservice/Naming-rules">naming rules</a> for guidelines on
     * constructing valid document keys.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get dynamic SearchDocument. </p>
     *
     * {@codesnippet com.azure.search.documents.SearchAsyncClient.getDocumentWithResponse#String-Class-List}
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

    @SuppressWarnings("unchecked")
    <T> Mono<Response<T>> getDocumentWithResponse(String key, Class<T> modelClass, List<String> selectedFields,
        Context context) {
        try {
            return restClient.getDocuments()
                .getWithResponseAsync(key, selectedFields, null, context)
                .onErrorMap(DocumentResponseConversions::exceptionMapper)
                .map(res -> {
                    if (serializer == null) {
                        try {
                            String serializedJson = ADAPTER.serialize(res.getValue(), SerializerEncoding.JSON);
                            T document = ADAPTER.deserialize(serializedJson, modelClass, SerializerEncoding.JSON);
                            return new SimpleResponse<>(res, document);
                        } catch (IOException ex) {
                            throw logger.logExceptionAsError(
                                new RuntimeException("Something wrong with the serialization."));
                        }
                    }
                    ByteArrayOutputStream sourceStream = new ByteArrayOutputStream();
                    serializer.serialize(sourceStream, res.getValue());
                    T doc = serializer.deserialize(new ByteArrayInputStream(sourceStream.toByteArray()),
                        createInstance(modelClass));
                    return new SimpleResponse<>(res, doc);
                }).map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Queries the number of documents in the search index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get document count. </p>
     *
     * {@codesnippet com.azure.search.documents.SearchAsyncClient.getDocumentCount}
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
     * {@codesnippet com.azure.search.documents.SearchAsyncClient.getDocumentCountWithResponse}
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
            return monoError(logger, ex);
        }
    }

    /**
     * Searches for documents in the Azure Cognitive Search index.
     * <p>
     * If {@code searchText} is set to {@code null} or {@code "*"} all documents will be matched, see
     * <a href="https://docs.microsoft.com/rest/api/searchservice/Simple-query-syntax-in-Azure-Search">simple query
     * syntax in Azure Cognitive Search</a> for more information about search query syntax.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Search text from documents in service. </p>
     *
     * {@codesnippet com.azure.search.documents.SearchAsyncClient.search#String}
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
     * Searches for documents in the Azure Cognitive Search index.
     * <p>
     * If {@code searchText} is set to {@code null} or {@code "*"} all documents will be matched, see
     * <a href="https://docs.microsoft.com/rest/api/searchservice/Simple-query-syntax-in-Azure-Search">simple query
     * syntax in Azure Cognitive Search</a> for more information about search query syntax.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Search text from documents in service with option. </p>
     *
     * {@codesnippet com.azure.search.documents.SearchAsyncClient.search#String-SearchOptions}
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
        // The firstPageResponse shared among all fucntional calls below.
        // Do not initial new instance directly in func call.
        final SearchFirstPageResponseWrapper firstPageResponse = new SearchFirstPageResponseWrapper();
        Function<String, Mono<SearchPagedResponse>> func = continuationToken -> withContext(context ->
            search(request, continuationToken, firstPageResponse, context));
        return new SearchPagedFlux(() -> func.apply(null), func);
    }

    SearchPagedFlux search(String searchText, SearchOptions searchOptions, Context context) {
        SearchRequest request = createSearchRequest(searchText, searchOptions);
        // The firstPageResponse shared among all fucntional calls below.
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
                    new SimpleResponse<>(response, getSearchResults(result)),
                    createContinuationToken(result, serviceVersion), getFacets(result), result.getCount(),
                    result.getCoverage());
                if (continuationToken == null) {
                    firstPageResponseWrapper.setFirstPageResponse(page);
                }
                return page;
            });
    }

    private List<SearchResult> getSearchResults(SearchDocumentsResult result) {
        return result.getResults().stream()
            .map(searchResult -> SearchResultConverter.map(searchResult, serializer))
            .collect(Collectors.toList());
    }

    private static String createContinuationToken(SearchDocumentsResult result, ServiceVersion serviceVersion) {
        return SearchContinuationToken.serializeToken(serviceVersion.getVersion(), result.getNextLink(),
            result.getNextPageParameters());
    }

    private static Map<String, List<FacetResult>> getFacets(SearchDocumentsResult result) {
        if (result.getFacets() == null) {
            return null;
        }

        Map<String, List<FacetResult>> facets = new HashMap<>();

        result.getFacets().forEach((key, values) ->
            facets.put(key, values.stream().map(FacetResultConverter::map).collect(Collectors.toList())));

        return facets;
    }

    /**
     * Suggests documents in the index that match the given partial query.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Suggest text from documents in service. </p>
     *
     * {@codesnippet com.azure.search.documents.SearchAsyncClient.suggest#String-String}
     *
     * @param searchText The search text on which to base suggestions
     * @param suggesterName The name of the suggester as specified in the suggesters collection that's part of the index
     * definition
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
     * {@codesnippet com.azure.search.documents.SearchAsyncClient.suggest#String-String-SuggestOptions}
     *
     * @param searchText The search text on which to base suggestions
     * @param suggesterName The name of the suggester as specified in the suggesters collection that's part of the index
     * definition
     * @param suggestOptions Parameters to further refine the suggestion query.
     * @return A {@link SuggestPagedFlux} that iterates over {@link SuggestResult} objects and provides access to the
     * {@link SuggestPagedResponse} object for each page containing HTTP response and coverage information.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/Suggestions">Suggestions</a>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SuggestPagedFlux suggest(String searchText, String suggesterName, SuggestOptions suggestOptions) {
        SuggestRequest suggestRequest = createSuggestRequest(searchText, suggesterName,
            SuggestOptionsHandler.ensureSuggestOptions(suggestOptions));

        return new SuggestPagedFlux(() -> withContext(context -> suggest(suggestRequest, context)));
    }

    SuggestPagedFlux suggest(String searchText, String suggesterName, SuggestOptions suggestOptions, Context context) {
        SuggestRequest suggestRequest = createSuggestRequest(searchText,
            suggesterName, SuggestOptionsHandler.ensureSuggestOptions(suggestOptions));

        return new SuggestPagedFlux(() -> suggest(suggestRequest, context));
    }

    private Mono<SuggestPagedResponse> suggest(SuggestRequest suggestRequest, Context context) {
        return restClient.getDocuments().suggestPostWithResponseAsync(suggestRequest, null, context)
            .onErrorMap(MappingUtils::exceptionMapper)
            .map(response -> {
                SuggestDocumentsResult result = response.getValue();

                return new SuggestPagedResponse(new SimpleResponse<>(response, getSuggestResults(result)),
                    result.getCoverage());
            });
    }

    private static List<SuggestResult> getSuggestResults(SuggestDocumentsResult result) {
        return result.getResults().stream()
            .map(SuggestResultConverter::map)
            .collect(Collectors.toList());
    }

    /**
     * Autocompletes incomplete query terms based on input text and matching terms in the index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Autocomplete text from documents in service. </p>
     *
     * {@codesnippet com.azure.search.documents.SearchAsyncClient.autocomplete#String-String}
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
     * {@codesnippet com.azure.search.documents.SearchAsyncClient.autocomplete#String-String-AutocompleteOptions}
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
            .map(MappingUtils::mappingAutocompleteResponse);
    }

    /**
     * Create search request from search text and parameters
     *
     * @param searchText search text
     * @param searchOptions search options
     * @return SearchRequest
     */
    private static SearchRequest createSearchRequest(String searchText, SearchOptions searchOptions) {
        SearchRequest searchRequest = new SearchRequest().setSearchText(searchText);

        if (searchOptions != null) {
            List<String> scoringParameters = searchOptions.getScoringParameters() == null ? null
                : searchOptions.getScoringParameters().stream().map(ScoringParameter::toString)
                .collect(Collectors.toList());
            searchRequest.setSearchMode(SearchModeConverter.map(searchOptions.getSearchMode()))
                .setFacets(searchOptions.getFacets())
                .setFilter(searchOptions.getFilter())
                .setHighlightPostTag(searchOptions.getHighlightPostTag())
                .setHighlightPreTag(searchOptions.getHighlightPreTag())
                .setIncludeTotalResultCount(searchOptions.isTotalCountIncluded())
                .setMinimumCoverage(searchOptions.getMinimumCoverage())
                .setQueryType(QueryTypeConverter.map(searchOptions.getQueryType()))
                .setScoringParameters(scoringParameters)
                .setScoringProfile(searchOptions.getScoringProfile())
                .setSkip(searchOptions.getSkip())
                .setTop(searchOptions.getTop());

            if (searchOptions.getHighlightFields() != null) {
                searchRequest.setHighlightFields(String.join(",", searchOptions.getHighlightFields()));
            }

            if (searchOptions.getSearchFields() != null) {
                searchRequest.setSearchFields(String.join(",", searchOptions.getSearchFields()));
            }

            if (searchOptions.getOrderBy() != null) {
                searchRequest.setOrderBy(String.join(",", searchOptions.getOrderBy()));
            }

            if (searchOptions.getSelect() != null) {
                searchRequest.setSelect(String.join(",", searchOptions.getSelect()));
            }
        }

        return searchRequest;
    }

    /**
     * Create suggest request from search text, suggester name, and parameters
     *
     * @param searchText search text
     * @param suggesterName search text
     * @param suggestOptions suggest options
     * @return SuggestRequest
     */
    private static SuggestRequest createSuggestRequest(String searchText, String suggesterName,
        SuggestOptions suggestOptions) {
        SuggestRequest suggestRequest = new SuggestRequest(searchText, suggesterName);
        suggestRequest.validate();

        if (suggestOptions != null) {
            suggestRequest.setFilter(suggestOptions.getFilter())
                .setUseFuzzyMatching(suggestOptions.useFuzzyMatching())
                .setHighlightPostTag(suggestOptions.getHighlightPostTag())
                .setHighlightPreTag(suggestOptions.getHighlightPreTag())
                .setMinimumCoverage(suggestOptions.getMinimumCoverage())
                .setTop(suggestOptions.getTop());

            List<String> searchFields = suggestOptions.getSearchFields();
            if (searchFields != null) {
                suggestRequest.setSearchFields(String.join(",", searchFields));
            }

            List<String> orderBy = suggestOptions.getOrderBy();
            if (orderBy != null) {
                suggestRequest.setOrderBy(String.join(",", orderBy));
            }

            List<String> select = suggestOptions.getSelect();
            if (select != null) {
                suggestRequest.setSelect(String.join(",", select));
            }
        }

        return suggestRequest;
    }

    /**
     * Create Autocomplete request from search text, suggester name, and parameters
     *
     * @param searchText search text
     * @param suggesterName search text
     * @param autocompleteOptions autocomplete options
     * @return AutocompleteRequest
     */
    private static AutocompleteRequest createAutoCompleteRequest(String searchText, String suggesterName,
        AutocompleteOptions autocompleteOptions) {
        AutocompleteRequest autoCompleteRequest = new AutocompleteRequest(searchText, suggesterName);
        autoCompleteRequest.validate();

        if (autocompleteOptions != null) {
            autoCompleteRequest.setFilter(autocompleteOptions.getFilter())
                .setUseFuzzyMatching(autocompleteOptions.useFuzzyMatching())
                .setHighlightPostTag(autocompleteOptions.getHighlightPostTag())
                .setHighlightPreTag(autocompleteOptions.getHighlightPreTag())
                .setMinimumCoverage(autocompleteOptions.getMinimumCoverage())
                .setTop(autocompleteOptions.getTop())
                .setAutocompleteMode(AutocompleteModeConverter.map(autocompleteOptions.getAutocompleteMode()));

            List<String> searchFields = autocompleteOptions.getSearchFields();
            if (searchFields != null) {
                autoCompleteRequest.setSearchFields(String.join(",", searchFields));
            }
        }

        return autoCompleteRequest;
    }

    private static <T> IndexDocumentsBatch<T> buildIndexBatch(Iterable<T> documents, IndexActionType actionType) {
        List<IndexAction<T>> actions = new ArrayList<>();
        documents.forEach(d -> actions.add(new IndexAction<T>()
            .setActionType(actionType)
            .setDocument(d)));

        return new IndexDocumentsBatch<T>().addActions(actions);
    }

}
