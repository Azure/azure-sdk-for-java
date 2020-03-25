// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SearchServiceClientBuilderTests {
    private final SearchApiKeyCredential searchApiKeyCredential = new SearchApiKeyCredential("0123");
    private final String searchEndpoint = "https://test.search.windows.net";
    private final SearchServiceVersion apiVersion = SearchServiceVersion.V2019_05_06;

    @Test
    public void buildSyncClientTest() {
        SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .serviceVersion(apiVersion)
            .buildClient();

        assertNotNull(client);
        assertEquals(SearchServiceClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildSyncClientUsingDefaultApiVersionTest() {
        SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .buildClient();

        assertNotNull(client);
        assertEquals(SearchServiceClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientTest() {
        SearchServiceAsyncClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .serviceVersion(apiVersion)
            .buildAsyncClient();

        assertNotNull(client);
        assertEquals(SearchServiceAsyncClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientUsingDefaultApiVersionTest() {
        SearchServiceAsyncClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .buildAsyncClient();

        assertNotNull(client);
        assertEquals(SearchServiceAsyncClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void whenApiVersionSpecifiedThenSpecifiedValueExists() {
        SearchServiceVersion expectedApiVersion = SearchServiceVersion.V2019_05_06;

        SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .serviceVersion(expectedApiVersion)
            .buildClient();

        assertEquals(expectedApiVersion.getVersion(), client.getServiceVersion().getVersion());

        SearchServiceAsyncClient asyncClient = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .serviceVersion(expectedApiVersion)
            .buildAsyncClient();
        assertEquals(expectedApiVersion.getVersion(), asyncClient.getServiceVersion().getVersion());
    }

    @Test
    public void whenBuildClientAndVerifyPropertiesThenSuccess() {
        SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .buildClient();

        assertEquals(searchEndpoint, client.getEndpoint());
        assertEquals(apiVersion, client.getServiceVersion());

        SearchServiceAsyncClient asyncClient = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .serviceVersion(apiVersion)
            .buildAsyncClient();

        assertEquals(searchEndpoint, asyncClient.getEndpoint());
        assertEquals(apiVersion, asyncClient.getServiceVersion());
    }

    @Test
    public void emptyEndpointThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SearchServiceClientBuilder().endpoint(""));
    }

    @Test
    public void nullCredentialThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SearchServiceClientBuilder().credential(null));
    }

    @Test
    public void credentialWithEmptyApiKeyThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SearchServiceClientBuilder()
            .credential(new SearchApiKeyCredential("")));
    }

    @Test
    void nullApiVersionSetsLatest() {
        SearchServiceClientBuilder builder = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .serviceVersion(null);

        assertEquals(SearchServiceVersion.getLatest(), builder.buildAsyncClient().getServiceVersion());
        assertEquals(SearchServiceVersion.getLatest(), builder.buildClient().getServiceVersion());
    }

    @Test
    public void verifyEmptyApiVersionSetsLatest() {
        SearchServiceClient searchServiceClient = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .buildClient();

        assertEquals(SearchServiceVersion.getLatest(), searchServiceClient.getServiceVersion());
    }

    @Test
    public void verifyEmptyApiVersionSetsLatestAsync() {
        SearchServiceAsyncClient searchServiceAsyncClient = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .buildAsyncClient();

        assertEquals(SearchServiceVersion.getLatest(), searchServiceAsyncClient.getServiceVersion());
    }
}
