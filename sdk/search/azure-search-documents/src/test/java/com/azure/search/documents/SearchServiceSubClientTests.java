// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.search.documents.indexes.IndexesTestHelpers;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SearchServiceSubClientTests extends TestBase {

    @Test
    public void canGetIndexClientFromSearchClient() {
        SearchIndexClient serviceClient = getSearchIndexClient();

        SearchClient searchClient = serviceClient.getSearchClient("hotels");

        // Validate the client was created
        assertNotNull(searchClient);

        // Validate the client points to the same instance
        assertEquals(serviceClient.getEndpoint(), searchClient.getEndpoint());

        // Validate that the client uses the same HTTP pipeline for authentication, retries, etc
        HttpPipeline servicePipeline = IndexesTestHelpers.getHttpPipeline(serviceClient);
        HttpPipeline searchPipeline = TestHelpers.getHttpPipeline(searchClient);

        assertEquals(servicePipeline, searchPipeline);

        // Validate that the client uses the specified index
        assertEquals("hotels", searchClient.getIndexName());
    }

    @Test
    public void canGetIndexAsyncClientFromSearchClient() {
        SearchIndexAsyncClient indexAsyncClient = getSearchIndexAsyncClient();

        SearchAsyncClient searchAsyncClient = indexAsyncClient.getSearchAsyncClient("hotels");

        // Validate the client was created
        assertNotNull(searchAsyncClient);

        // Validate the client points to the same instance
        assertEquals(indexAsyncClient.getEndpoint(), searchAsyncClient.getEndpoint());

        // Validate that the client uses the same HTTP pipeline for authentication, retries, etc
        HttpPipeline servicePipeline = IndexesTestHelpers.getHttpPipeline(indexAsyncClient);
        HttpPipeline searchPipeline = TestHelpers.getHttpPipeline(searchAsyncClient);

        assertEquals(servicePipeline, searchPipeline);

        // Validate that the client uses the specified index
        assertEquals("hotels", searchAsyncClient.getIndexName());
    }

    @Test
    public void canGetIndexClientAfterUsingServiceClient() {
        // This will fail and be retried as the index doesn't exist so use a short retry policy.
        SearchIndexClient serviceClient = getSearchIndexClient(new RetryPolicy(
            new FixedDelay(3, Duration.ofMillis(10))));
        try {
            // this is expected to fail
            serviceClient.deleteIndex("thisindexdoesnotexist");
        } catch (Exception e) {
            // deleting the index should fail as it does not exist
        }

        // This should not fail
        SearchClient indexClient = serviceClient.getSearchClient("hotels");
        assertEquals("hotels", indexClient.getIndexName());
    }

    @Test
    public void canGetIndexAsyncClientAfterUsingServiceClient() {
        SearchIndexAsyncClient serviceClient = getSearchIndexAsyncClient();
        try {
            // this is expected to fail
            serviceClient.deleteIndex("thisindexdoesnotexist");
        } catch (Exception e) {
            // deleting the index should fail as it does not exist
        }

        // This should not fail
        SearchAsyncClient indexClient = serviceClient.getSearchAsyncClient("hotels");
        assertEquals("hotels", indexClient.getIndexName());
    }

    private SearchIndexClient getSearchIndexClient() {
        return getSearchIndexClient(null);
    }

    private SearchIndexClient getSearchIndexClient(RetryPolicy retryPolicy) {
        return new SearchIndexClientBuilder()
            .endpoint("https://test1.search.windows.net")
            .credential(new AzureKeyCredential("api-key"))
            .retryPolicy(retryPolicy)
            .buildClient();
    }

    private SearchIndexAsyncClient getSearchIndexAsyncClient() {
        return new SearchIndexClientBuilder()
            .endpoint("https://test1.search.windows.net")
            .credential(new AzureKeyCredential("api-key"))
            .buildAsyncClient();
    }
}
