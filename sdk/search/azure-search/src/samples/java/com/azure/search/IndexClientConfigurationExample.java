// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;

/**
 * This example shows how to use the {@link SearchIndexClientBuilder} to create an index client
 *
 * This sample is based on the hotels-sample index available to install from the portal.
 * See instructions here](https://docs.microsoft.com/en-us/azure/search/search-get-started-portal
 */
public class IndexClientConfigurationExample {

    /**
     * From the Azure portal, get your Azure Cognitive Search service URL and API key,
     * and set the values of these environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_SEARCH_ENDPOINT");
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
            .serviceEndpoint(ENDPOINT)
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
            .serviceEndpoint(ENDPOINT)
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
