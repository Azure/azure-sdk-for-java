package com.azure.search.data;

import com.azure.core.http.rest.PagedFlux;
import com.azure.search.data.generated.models.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * The public (Customer facing) interface for SearchIndexASyncClient.
 */
public interface SearchIndexASyncClient {
    // Indices

    /**
     * Gets Client Api Version.
     *
     * @return the apiVersion value.
     */
    String getApiVersion();

    /**
     * Gets The name of the Azure Search service.
     *
     * @return the searchServiceName value.
     */
    String getSearchServiceName();

    /**
     * Gets The DNS suffix of the Azure Search service. The default is search.windows.net.
     *
     * @return the searchDnsSuffix value.
     */
    String getSearchDnsSuffix();

    /**
     * Gets The name of the Azure Search index.
     *
     * @return the indexName value.
     */
    String getIndexName();

    /**
     * Sets The name of the Azure Search index.
     *
     * @param indexName the indexName value.
     * @return the service client itself.
     */
    SearchIndexASyncClient setIndexName(String indexName);


    // Index Operations

    /**
     * Gets the number of documents
     *
     * @return the number of documents.
     */
    Mono<Long> countDocuments();

    /**
     * Searches for documents in the Azure Search index
     *
     * @return the document search result.
     */
    PagedFlux<SearchResult> search();

    /**
     * Searches for documents in the Azure Search index
     *
     * @return the document search result.
     */
    PagedFlux<SearchResult> search(String searchText,
                                   SearchParameters searchParameters,
                                   SearchRequestOptions searchRequestOptions);

    /**
     * Retrieves a document from the Azure Search index.
     *
     * @param key the name of the document
     * @return
     */
    Mono<Object> getDocument(String key);

    /**
     * Retrieves a document from the Azure Search index.
     *
     * @param key
     * @param selectedFields
     * @param searchRequestOptions
     * @return
     */
    Mono<Object> getDocument(String key, List<String> selectedFields, SearchRequestOptions searchRequestOptions);

    /**
     * Suggests documents in the Azure Search index that match the given partial query text.
     *
     * @param searchText
     * @param suggesterName
     * @return
     */
    PagedFlux<SuggestResult> suggest(String searchText, String suggesterName);

    /**
     * Suggests documents in the Azure Search index that match the given partial query text.
     *
     * @param searchText
     * @param suggesterName
     * @param suggestParameters
     * @param searchRequestOptions
     * @return
     */
    PagedFlux<SuggestResult> suggest(String searchText, String suggesterName, SuggestParameters suggestParameters, SearchRequestOptions searchRequestOptions);

    /**
     * Sends a batch of document write actions to the Azure Search index.
     *
     * @param batch
     * @return
     */
    Mono<DocumentIndexResult> index(IndexBatch batch);

    /**
     * Autocompletes incomplete query terms based on input text and matching terms in the Azure Search index.
     *
     * @param searchText
     * @param suggesterName
     * @return
     */
    Mono<AutocompleteResult> autocomplete(String searchText, String suggesterName);

    /**
     * Autocompletes incomplete query terms based on input text and matching terms in the Azure Search index.
     *
     * @param searchText
     * @param suggesterName
     * @param searchRequestOptions
     * @param autocompleteParameters
     * @return
     */
    Mono<AutocompleteResult> autocomplete(String searchText, String suggesterName, SearchRequestOptions searchRequestOptions, AutocompleteParameters autocompleteParameters);
}
