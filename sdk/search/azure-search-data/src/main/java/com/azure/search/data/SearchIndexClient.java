// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data;

import com.azure.core.http.rest.PagedIterable;
import com.azure.search.data.customization.Document;
import com.azure.search.data.generated.models.AutocompleteItem;
import com.azure.search.data.generated.models.AutocompleteParameters;
import com.azure.search.data.generated.models.DocumentIndexResult;
import com.azure.search.data.generated.models.IndexBatch;
import com.azure.search.data.generated.models.SearchParameters;
import com.azure.search.data.generated.models.SearchRequestOptions;
import com.azure.search.data.generated.models.SearchResult;
import com.azure.search.data.generated.models.SuggestParameters;
import com.azure.search.data.generated.models.SuggestResult;

import java.util.List;

/**
 * The public (Customer facing) interface for SearchIndexClient.
 */
public interface SearchIndexClient {
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
    SearchIndexClient setIndexName(String indexName);


    // Index Operations

    /**
     * Uploads a collection of documents to the target index
     *
     * @param documents collection of documents to upload to the target Index.
     * @param <T> The type of object to serialize.
     * @return document index result.
     */
    <T> DocumentIndexResult  uploadDocuments(List<T> documents);

    /**
     * Gets the number of documents
     *
     * @return the number of documents.
     */
    Long countDocuments();

    /**
     * Searches for documents in the Azure Search index
     *
     * @return A {@link PagedIterable} of SearchResults
     */
    PagedIterable<SearchResult> search();

    /**
     * Searches for documents in the Azure Search index
     *
     * @param searchText Search Test
     * @param searchParameters Search Parameters
     * @param searchRequestOptions Search Request Options
     * @return A {@link PagedIterable} of SearchResults
     */
    PagedIterable<SearchResult> search(String searchText,
                                       SearchParameters searchParameters,
                                       SearchRequestOptions searchRequestOptions);

    /**
     * Retrieves a document from the Azure Search index.
     *
     * @param key the name of the document
     * @return document object
     */
    Document getDocument(String key);

    /**
     * Retrieves a document from the Azure Search index.
     *
     * @param key document key
     * @param selectedFields selected fields to return
     * @param searchRequestOptions search request options
     * @return document object
     */
    Document getDocument(String key, List<String> selectedFields, SearchRequestOptions searchRequestOptions);

    /**
     * Suggests documents in the Azure Search index that match the given partial query text.
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @return suggests result
     */
    PagedIterable<SuggestResult> suggest(String searchText, String suggesterName);

    /**
     * Suggests documents in the Azure Search index that match the given partial query text.
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @param suggestParameters suggest parameters
     * @param searchRequestOptions search request options
     * @return suggests results
     */
    PagedIterable<SuggestResult> suggest(String searchText,
                                         String suggesterName,
                                         SuggestParameters suggestParameters,
                                         SearchRequestOptions searchRequestOptions);

    /**
     * Sends a batch of document write actions to the Azure Search index.
     *
     * @param batch batch of documents to send to the index with the requested action
     * @return document index result
     */
    DocumentIndexResult index(IndexBatch batch);

    /**
     * Autocompletes incomplete query terms based on input text and matching terms in the Azure Search index.
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @return auto complete result
     */
    PagedIterable<AutocompleteItem> autocomplete(String searchText, String suggesterName);

    /**
     * Autocompletes incomplete query terms based on input text and matching terms in the Azure Search index.
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @param searchRequestOptions search request options
     * @param autocompleteParameters auto complete parameters
     * @return auto complete result
     */
    PagedIterable<AutocompleteItem> autocomplete(String searchText, String suggesterName, SearchRequestOptions searchRequestOptions, AutocompleteParameters autocompleteParameters);
}
