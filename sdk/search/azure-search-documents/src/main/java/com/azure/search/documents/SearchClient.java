// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.documents.indexes.models.IndexDocumentsBatch;
import com.azure.search.documents.models.AutocompleteOptions;
import com.azure.search.documents.models.IndexBatchException;
import com.azure.search.documents.models.IndexDocumentsOptions;
import com.azure.search.documents.models.IndexDocumentsResult;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.models.SuggestOptions;
import com.azure.search.documents.models.SuggestResult;
import com.azure.search.documents.util.AutocompletePagedIterable;
import com.azure.search.documents.util.SearchPagedIterable;
import com.azure.search.documents.util.SearchPagedResponse;
import com.azure.search.documents.util.SuggestPagedIterable;
import com.azure.search.documents.util.SuggestPagedResponse;

import java.util.List;

/**
 * This class provides a client that contains the operations for querying an index and uploading, merging, or deleting
 * documents in an Azure Cognitive Search service.
 *
 * @see SearchClientBuilder
 */
@ServiceClient(builder = SearchClientBuilder.class)
public final class SearchClient {

    private final SearchAsyncClient asyncClient;

    /**
     * Package private constructor to be used by {@link SearchClientBuilder}
     *
     * @param searchAsyncClient Async SearchIndex Client
     */
    SearchClient(SearchAsyncClient searchAsyncClient) {
        this.asyncClient = searchAsyncClient;
    }

    /**
     * Gets the name of the Azure Cognitive Search index.
     *
     * @return the indexName value.
     */
    public String getIndexName() {
        return asyncClient.getIndexName();
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return the pipeline.
     */
    HttpPipeline getHttpPipeline() {
        return this.asyncClient.getHttpPipeline();
    }

    /**
     * Gets the endpoint for the Azure Cognitive Search service.
     *
     * @return the endpoint value.
     */
    public String getEndpoint() {
        return asyncClient.getEndpoint();
    }

    /**
     * Uploads a collection of documents to the target index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Upload dynamic SearchDocument. </p>
     *
     * {@codesnippet com.azure.search.documents.SearchClient.uploadDocuments#Iterable}
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
     * {@codesnippet com.azure.search.documents.SearchClient.uploadDocumentsWithResponse#Iterable-IndexDocumentsOptions-Context}
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
        return asyncClient.uploadDocumentsWithResponse(documents, options, context).block();
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
     * {@codesnippet com.azure.search.documents.SearchClient.mergeDocuments#Iterable}
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
     * {@codesnippet com.azure.search.documents.SearchClient.mergeDocumentsWithResponse#Iterable-IndexDocumentsOptions-Context}
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
        return asyncClient.mergeDocumentsWithResponse(documents, options, context).block();
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
     * {@codesnippet com.azure.search.documents.SearchClient.mergeOrUploadDocuments#Iterable}
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
     * {@codesnippet com.azure.search.documents.SearchClient.mergeOrUploadDocumentsWithResponse#Iterable-IndexDocumentsOptions-Context}
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
        return asyncClient.mergeOrUploadDocumentsWithResponse(documents, options, context).block();
    }

    /**
     * Deletes a collection of documents from the target index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Delete dynamic SearchDocument. </p>
     *
     * {@codesnippet com.azure.search.documents.SearchClient.deleteDocuments#Iterable}
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
     * {@codesnippet com.azure.search.documents.SearchClient.deleteDocumentsWithResponse#Iterable-IndexDocumentsOptions-Context}
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
        return asyncClient.deleteDocumentsWithResponse(documents, options, context).block();
    }

    /**
     * Sends a batch of upload, merge, and/or delete actions to the search index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Index batch operation on dynamic SearchDocument. </p>
     *
     * {@codesnippet com.azure.search.documents.SearchClient.indexDocuments#IndexDocumentsBatch}
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
     * {@codesnippet com.azure.search.documents.SearchClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-Context}
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
        return asyncClient.indexDocumentsWithResponse(batch, options, context).block();
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
     * {@codesnippet com.azure.search.documents.SearchClient.getDocuments#String-Class}
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
     * {@codesnippet com.azure.search.documents.SearchClient.getDocumentWithResponse#String-Class-List-Context}
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
        return asyncClient.getDocumentWithResponse(key, modelClass, selectedFields, context).block();
    }

    /**
     * Queries the number of documents in the search index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Get document count. </p>
     *
     * {@codesnippet com.azure.search.documents.SearchClient.getDocumentCount}
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
     * {@codesnippet com.azure.search.documents.SearchClient.getDocumentCountWithResponse#Context}
     *
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return response containing the number of documents.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Long> getDocumentCountWithResponse(Context context) {
        return asyncClient.getDocumentCountWithResponse(context).block();
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
     * {@codesnippet com.azure.search.documents.SearchClient.search#String}
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
     * {@codesnippet com.azure.search.documents.SearchClient.search#String-SearchOptions-Context}
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
        return new SearchPagedIterable(asyncClient.search(searchText, searchOptions, context));
    }

    /**
     * Suggests documents in the index that match the given partial query.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Suggest text from documents in service. </p>
     *
     * {@codesnippet com.azure.search.documents.SearchClient.suggest#String-String}
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
     * {@codesnippet com.azure.search.documents.SearchClient.suggest#String-String-SuggestOptions-Context}
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
        return new SuggestPagedIterable(asyncClient.suggest(searchText, suggesterName, suggestOptions, context));
    }

    /**
     * Autocompletes incomplete query terms based on input text and matching terms in the index.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p> Autocomplete text from documents in service. </p>
     *
     * {@codesnippet com.azure.search.documents.SearchClient.autocomplete#String-String}
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
     * {@codesnippet com.azure.search.documents.SearchClient.autocomplete#String-String-AutocompleteOptions-Context}
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
        return new AutocompletePagedIterable(asyncClient.autocomplete(searchText, suggesterName, autocompleteOptions,
            context));
    }
}
