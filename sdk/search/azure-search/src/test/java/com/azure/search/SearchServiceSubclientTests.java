// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.http.HttpPipeline;
import org.junit.Assert;
import org.junit.Test;

public class SearchServiceSubclientTests {

    @Test
    public void canGetIndexClientFromSearchClient() {
        SearchServiceClient serviceClient = new SearchServiceClientBuilder()
            .endpoint("https://test1.search.windows.net")
            .credential(new ApiKeyCredentials("api-key"))
            .buildClient();

        SearchIndexClient indexClient = serviceClient.getIndexClient("hotels");

        // Validate the client was created
        Assert.assertNotNull(indexClient);

        // Validate the client points to the same instance
        Assert.assertEquals(serviceClient.getSearchServiceName(), indexClient.getSearchServiceName());
        Assert.assertEquals(serviceClient.getSearchDnsSuffix(), indexClient.getSearchDnsSuffix());
        Assert.assertEquals(serviceClient.getApiVersion(), indexClient.getApiVersion());

        // Validate that the client uses the same HTTP pipeline for authentication, retries, etc
        HttpPipeline servicePipeline = serviceClient.getHttpPipeline();
        HttpPipeline indexPipeline = indexClient.getHttpPipeline();

        Assert.assertEquals(servicePipeline, indexPipeline);

        // Validate that the client uses the specified index
        Assert.assertEquals("hotels", indexClient.getIndexName());
    }

    @Test
    public void canGetIndexAsyncClientFromSearchClient() {
        SearchServiceAsyncClient serviceClient = new SearchServiceClientBuilder()
            .endpoint("https://test1.search.windows.net")
            .credential(new ApiKeyCredentials("api-key"))
            .buildAsyncClient();
        SearchIndexAsyncClient indexClient = serviceClient.getIndexClient("hotels");

        // Validate the client was created
        Assert.assertNotNull(indexClient);

        // Validate the client points to the same instance
        Assert.assertEquals(serviceClient.getSearchServiceName(), indexClient.getSearchServiceName());
        Assert.assertEquals(serviceClient.getSearchDnsSuffix(), indexClient.getSearchDnsSuffix());
        Assert.assertEquals(serviceClient.getApiVersion(), indexClient.getApiVersion());

        // Validate that the client uses the same HTTP pipeline for authentication, retries, etc
        HttpPipeline servicePipeline = serviceClient.getHttpPipeline();
        HttpPipeline indexPipeline = indexClient.getHttpPipeline();

        Assert.assertEquals(servicePipeline, indexPipeline);

        // Validate that the client uses the specified index
        Assert.assertEquals("hotels", indexClient.getIndexName());
    }
}
