package com.azure.search.data.examples;

import com.azure.search.data.common.SearchPipelinePolicy;
import com.azure.search.data.customization.SearchIndexClientBuilderImpl;
import com.azure.search.data.customization.SearchIndexClientImpl;
import com.azure.search.data.generated.models.DocumentSearchResult;
import com.azure.search.data.generated.models.SearchParameters;
import com.azure.search.data.generated.models.SearchRequestOptions;

/**
 * This example will be moved to a separate project
 */
public class SearchIndexClientExample {

    public static void main(String[] args) {
        String searchServiceName = "";
        String apiKey = "";
        String dnsSuffix = "search.windows.net";
        String indexName = "hotels";
        String apiVersion = "2019-05-06";


        SearchIndexClientImpl searchClient = new SearchIndexClientBuilderImpl()
                .serviceName(searchServiceName)
                .searchDnsSuffix(dnsSuffix)
                .indexName(indexName)
                .apiVersion(apiVersion)
                .policy(new SearchPipelinePolicy(apiKey))
                .buildClient();

        DocumentSearchResult result = searchClient.search("*", new SearchParameters(), new SearchRequestOptions());
    }
}
