package com.azure.search.data.customization;

import com.azure.search.data.common.DocumentResponseConversions;
import com.azure.search.data.common.SearchPipelinePolicy;
import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.generated.models.*;

import java.util.List;
import java.util.Map;

public class SearchIndexClientImpl extends SearchIndexBaseClientImpl implements SearchIndexClient {

    /**
     * Constructor
     *
     * @param searchServiceName
     * @param indexName
     * @param apiVersion
     */
    public SearchIndexClientImpl(String searchServiceName, String searchDnsSuffix, String indexName, String apiVersion, SearchPipelinePolicy policy) {
        super(searchServiceName, searchDnsSuffix, indexName, apiVersion, policy);
    }

    @Override
    public SearchIndexClient setIndexName(String indexName) {
        this.setIndexNameInternal(indexName);
        return this;
    }

    @Override
    public Long countDocuments() {
        return restClient.documents().countAsync().block();
    }

    @Override
    public DocumentSearchResult search() {
        return restClient.documents().searchGetAsync().block();
    }

    @Override
    public DocumentSearchResult search(String searchText, SearchParameters searchParameters, SearchRequestOptions searchRequestOptions) {
        // to be replaced with calling to the async client instead
        return restClient.documents().searchGetAsync(searchText, searchParameters, searchRequestOptions).block();
    }

    @Override
    public Map<String, Object> getDocument(String key) {
        return restClient.documents().getAsync(key)
            .map(DocumentResponseConversions::convertLinkedHashMapToMap).block();
    }

    @Override
    public Map<String, Object> getDocument(String key, List<String> selectedFields, SearchRequestOptions searchRequestOptions) {
        return restClient.documents().getAsync(key, selectedFields, searchRequestOptions)
            .map(DocumentResponseConversions::convertLinkedHashMapToMap).block();
    }

    @Override
    public DocumentSuggestResult suggest(String searchText, String suggesterName) {
        return null;
    }

    @Override
    public DocumentSuggestResult suggest(String searchText, String suggesterName, SuggestParameters suggestParameters, SearchRequestOptions searchRequestOptions) {
        return null;
    }

    @Override
    public DocumentIndexResult index(IndexBatch batch) {
        return null;
    }

    @Override
    public AutocompleteResult autocomplete(String searchText, String suggesterName) {
        return null;
    }

    @Override
    public AutocompleteResult autocomplete(String searchText, String suggesterName, SearchRequestOptions searchRequestOptions, AutocompleteParameters autocompleteParameters) {
        return null;
    }
}
