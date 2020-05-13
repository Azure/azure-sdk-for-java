// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.test.TestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SearchServiceSubClientTests extends TestBase {


    @Test
    public void canGetIndexClientFromSearchClient() {
        SearchServiceClient serviceClient = getSearchService();

        SearchClient indexClient = serviceClient.getSearchClient("hotels");

        // Validate the client was created
        assertNotNull(indexClient);

        // Validate the client points to the same instance
        assertEquals(serviceClient.getEndpoint(), indexClient.getEndpoint());
        assertEquals(serviceClient.getServiceVersion(), indexClient.getServiceVersion());

        // Validate that the client uses the same HTTP pipeline for authentication, retries, etc
        HttpPipeline servicePipeline = serviceClient.getHttpPipeline();
        HttpPipeline indexPipeline = indexClient.getHttpPipeline();

        assertEquals(servicePipeline, indexPipeline);

        // Validate that the client uses the specified index
        assertEquals("hotels", indexClient.getIndexName());
    }

    @Test
    public void canGetIndexAsyncClientFromSearchClient() {
        SearchServiceAsyncClient serviceClient = getAsyncSearchService();

        SearchAsyncClient indexClient = serviceClient.getSearchAsyncClient("hotels");

        // Validate the client was created
        assertNotNull(indexClient);

        // Validate the client points to the same instance
        assertEquals(serviceClient.getEndpoint(), indexClient.getEndpoint());
        assertEquals(serviceClient.getServiceVersion(), indexClient.getServiceVersion());

        // Validate that the client uses the same HTTP pipeline for authentication, retries, etc
        HttpPipeline servicePipeline = serviceClient.getHttpPipeline();
        HttpPipeline indexPipeline = indexClient.getHttpPipeline();

        assertEquals(servicePipeline, indexPipeline);

        // Validate that the client uses the specified index
        assertEquals("hotels", indexClient.getIndexName());
    }

    @Test
    public void canGetIndexClientAfterUsingServiceClient() {
        SearchServiceClient serviceClient = getSearchService();
        try {
            // this is expected to fail
            serviceClient.getSearchIndexClient().delete("thisindexdoesnotexist");
        } catch (Exception e) {
            // deleting the index should fail as it does not exist
        }

        // This should not fail
        SearchClient indexClient = serviceClient.getSearchClient("hotels");
        assertEquals("hotels", indexClient.getIndexName());
    }

    @Test
    public void canGetIndexAsyncClientAfterUsingServiceClient() {
        SearchServiceAsyncClient serviceClient = getAsyncSearchService();
        try {
            // this is expected to fail
            serviceClient.getSearchIndexAsyncClient().delete("thisindexdoesnotexist");
        } catch (Exception e) {
            // deleting the index should fail as it does not exist
        }

        // This should not fail
        SearchAsyncClient indexClient = serviceClient.getSearchAsyncClient("hotels");
        assertEquals("hotels", indexClient.getIndexName());
    }

    private SearchServiceClient getSearchService() {
        return new SearchServiceClientBuilder()
            .endpoint("https://test1.search.windows.net")
            .credential(new AzureKeyCredential("api-key"))
            .buildClient();
    }

    private SearchServiceAsyncClient getAsyncSearchService() {
        return new SearchServiceClientBuilder()
            .endpoint("https://test1.search.windows.net")
            .credential(new AzureKeyCredential("api-key"))
            .buildAsyncClient();
    }
}
