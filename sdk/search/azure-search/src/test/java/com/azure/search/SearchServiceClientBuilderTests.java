// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class SearchServiceClientBuilderTests {
    private final SearchApiKeyCredential searchApiKeyCredential = new SearchApiKeyCredential("0123");
    private final String searchEndpoint = "https://test.search.windows.net";
    private final SearchServiceVersion apiVersion = SearchServiceVersion.V2019_05_06;

    @Test
    public void buildSyncClientTest() {
        SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .apiVersion(apiVersion)
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
            .apiVersion(apiVersion)
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
            .apiVersion(expectedApiVersion)
            .buildClient();

        assertEquals(expectedApiVersion.getVersion(), client.getApiVersion().getVersion());

        SearchServiceAsyncClient asyncClient = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .apiVersion(expectedApiVersion)
            .buildAsyncClient();
        assertEquals(expectedApiVersion.getVersion(), asyncClient.getApiVersion().getVersion());
    }

    @Test
    public void whenBuildClientAndVerifyPropertiesThenSuccess() {
        SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .buildClient();

        assertEquals(searchEndpoint, client.getEndpoint());
        assertEquals(apiVersion, client.getApiVersion());

        SearchServiceAsyncClient asyncClient = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .apiVersion(apiVersion)
            .buildAsyncClient();

        assertEquals(searchEndpoint, asyncClient.getEndpoint());
        assertEquals(apiVersion, asyncClient.getApiVersion());
    }

    @Test
    public void verifyEmptyEndpointIsInvalidAsyncTest() {
        expectThrowsWithMessage("'endpoint' must be a valid URL", () -> new SearchServiceClientBuilder()
            .endpoint("")
            .credential(searchApiKeyCredential)
            .apiVersion(apiVersion)
            .buildAsyncClient());
    }

    @Test
    public void verifyEmptyEndpointIsInvalidTest() {
        expectThrowsWithMessage("'endpoint' must be a valid URL", () -> new SearchServiceClientBuilder()
            .endpoint("")
            .credential(searchApiKeyCredential)
            .apiVersion(apiVersion)
            .buildClient());
    }

    @Test
    public void verifyNullApiKeyIsInvalidAsyncTest() {
        expectThrowsWithMessage("Empty apiKeyCredentials", () -> new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(null)
            .apiVersion(apiVersion)
            .buildAsyncClient());
    }

    @Test
    public void verifyNullApiKeyIsInvalidTest() {
        expectThrowsWithMessage("Empty apiKeyCredentials", () -> new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(null)
            .apiVersion(apiVersion)
            .buildClient());
    }

    @Test
    public void verifyEmptyApiKeyIsInvalidAsyncTest() {
        expectThrowsWithMessage("Empty apiKeyCredentials", () -> new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(new SearchApiKeyCredential(""))
            .apiVersion(apiVersion)
            .buildAsyncClient());
    }

    @Test
    public void verifyEmptyApiKeyIsInvalidTest() {
        expectThrowsWithMessage("Empty apiKeyCredentials", () -> new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(new SearchApiKeyCredential(""))
            .apiVersion(apiVersion)
            .buildClient());
    }

    @Test
    public void verifyNullApiVersionIsInvalidAsyncTest() {
        expectThrowsWithMessage("Invalid apiVersion", () -> new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .apiVersion(null)
            .buildAsyncClient());
    }

    @Test
    public void verifyNullApiVersionIsInvalidTest() {
        expectThrowsWithMessage("Invalid apiVersion", () -> new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .apiVersion(null)
            .buildClient());
    }

    @Test
    public void verifyEmptyApiVersionSetsLatest() {
        SearchServiceClient searchServiceClient = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .buildClient();

        assertEquals(SearchServiceVersion.getLatest().getVersion(),
            searchServiceClient.getApiVersion().getVersion());
    }

    @Test
    public void verifyEmptyApiVersionSetsLatestAsync() {
        SearchServiceAsyncClient searchServiceAsyncClient = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .buildAsyncClient();

        assertEquals(SearchServiceVersion.getLatest().getVersion(),
            searchServiceAsyncClient.getApiVersion().getVersion());
    }

    private void expectThrowsWithMessage(String expectedMessage, Runnable runnable) {
        try {
            runnable.run();
            fail();

        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertTrue(e.getMessage().contains(expectedMessage));
        }
    }
}
