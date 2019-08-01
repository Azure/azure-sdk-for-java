package com.azure.search.data.samples;

import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.common.credentials.SearchCredentials;
import com.azure.search.data.customization.SearchIndexClientBuilder;

public class SearchIndexClientExample {

    public static void main(String[] args )
    {
        String searchServiceName = "service name";
        String apiKey = "api key";
        String dnsSuffix = "search.windows.net";
        String indexName = "hotels";

        SearchCredentials credentials = new SearchCredentials(apiKey);
        SearchIndexClient searchClient = new SearchIndexClientBuilder().serviceName(searchServiceName).searchDnsSuffix(dnsSuffix).indexName(indexName).credentials(credentials).buildClient();
    }
}
