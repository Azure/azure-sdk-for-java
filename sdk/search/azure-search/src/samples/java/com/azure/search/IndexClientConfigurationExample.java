// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;

/**
 * This example shows how to use the {@link SearchIndexClientBuilder} to create an index client
 */
public class IndexClientConfigurationExample {
    /*
    * From the Azure portal, get your Azure Cognitive Search service name and API key.
    */
    private static final String SEARCH_SERVICE = Configuration.getGlobalConfiguration().get("AZURE_SEARCH_SERVICE");
    private static final String API_KEY = Configuration.getGlobalConfiguration().get("AZURE_SEARCH_API_KEY");

    public static void main(String[] args) {
        SearchIndexClient minimalClient = createMinimalClient();
        SearchIndexAsyncClient advancedClient = createAdvancedClient();
    }

    /**
     * Builds a {@link SearchIndexClient} with the minimum required configuration
     * @return an index client with all defaults
     */
    private static SearchIndexClient createMinimalClient() {
        return new SearchIndexClientBuilder()
            .serviceName(SEARCH_SERVICE)
            .credential(new ApiKeyCredentials(API_KEY))
            .indexName("hotels")
            .buildClient();
    }

    /**
     * Builds a {@link SearchIndexAsyncClient} with additional configuration
     * @return a customized async index client
     */
    private static SearchIndexAsyncClient createAdvancedClient() {
        return new SearchIndexClientBuilder()
            .serviceName(SEARCH_SERVICE)
            .credential(new ApiKeyCredentials(API_KEY))
            .indexName("hotels")
            .apiVersion("2019-05-06")
            .addPolicy(new RetryPolicy())
            .httpClient(
                new NettyAsyncHttpClientBuilder()
                    .wiretap(true)
                    .build()
            ).buildAsyncClient();
    }
}
