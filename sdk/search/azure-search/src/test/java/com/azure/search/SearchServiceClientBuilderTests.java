// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class SearchServiceClientBuilderTests {
    private final SearchApiKeyCredential searchApiKeyCredential = new SearchApiKeyCredential("0123");
    private final String searchEndpoint = "https://test.search.windows.net";
    private final SearchServiceVersion apiVersion = SearchServiceVersion.V2019_05_06;

    @Test
    public void buildSyncClientTest() {
        SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .searchServiceVersion(apiVersion)
            .buildClient();

        Assert.assertNotNull(client);
        Assert.assertEquals(SearchServiceClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildSyncClientUsingDefaultApiVersionTest() {
        SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .buildClient();

        Assert.assertNotNull(client);
        Assert.assertEquals(SearchServiceClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientTest() {
        SearchServiceAsyncClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .searchServiceVersion(apiVersion)
            .buildAsyncClient();

        Assert.assertNotNull(client);
        Assert.assertEquals(SearchServiceAsyncClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientUsingDefaultApiVersionTest() {
        SearchServiceAsyncClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .buildAsyncClient();

        Assert.assertNotNull(client);
        Assert.assertEquals(SearchServiceAsyncClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void whenApiVersionSpecifiedThenSpecifiedValueExists() {
        SearchServiceVersion expectedApiVersion = SearchServiceVersion.V2019_05_06;

        SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .searchServiceVersion(expectedApiVersion)
            .buildClient();

        Assert.assertEquals(expectedApiVersion.getVersion(), client.getServiceVersion().getVersion());

        SearchServiceAsyncClient asyncClient = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .searchServiceVersion(expectedApiVersion)
            .buildAsyncClient();
        Assert.assertEquals(expectedApiVersion.getVersion(), asyncClient.getServiceVersion().getVersion());
    }

    @Test
    public void whenBuildClientAndVerifyPropertiesThenSuccess() {
        SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .buildClient();

        Assert.assertEquals(searchEndpoint, client.getEndpoint());
        Assert.assertEquals(apiVersion, client.getServiceVersion());

        SearchServiceAsyncClient asyncClient = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .searchServiceVersion(apiVersion)
            .buildAsyncClient();

        Assert.assertEquals(searchEndpoint, asyncClient.getEndpoint());
        Assert.assertEquals(apiVersion, asyncClient.getServiceVersion());
    }

    @Test
    public void verifyEmptyEndpointIsInvalidAsyncTest() {
        expectThrowsWithMessage("'endpoint' must be a valid URL", () -> new SearchServiceClientBuilder()
            .endpoint("")
            .credential(searchApiKeyCredential)
            .searchServiceVersion(apiVersion)
            .buildAsyncClient());
    }

    @Test
    public void verifyEmptyEndpointIsInvalidTest() {
        expectThrowsWithMessage("'endpoint' must be a valid URL", () -> new SearchServiceClientBuilder()
            .endpoint("")
            .credential(searchApiKeyCredential)
            .searchServiceVersion(apiVersion)
            .buildClient());
    }

    @Test
    public void verifyNullApiKeyIsInvalidAsyncTest() {
        expectThrowsWithMessage("Empty apiKeyCredentials", () -> new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(null)
            .searchServiceVersion(apiVersion)
            .buildAsyncClient());
    }

    @Test
    public void verifyNullApiKeyIsInvalidTest() {
        expectThrowsWithMessage("Empty apiKeyCredentials", () -> new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(null)
            .searchServiceVersion(apiVersion)
            .buildClient());
    }

    @Test
    public void verifyEmptyApiKeyIsInvalidAsyncTest() {
        expectThrowsWithMessage("Empty apiKeyCredentials", () -> new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(new SearchApiKeyCredential(""))
            .searchServiceVersion(apiVersion)
            .buildAsyncClient());
    }

    @Test
    public void verifyEmptyApiKeyIsInvalidTest() {
        expectThrowsWithMessage("Empty apiKeyCredentials", () -> new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(new SearchApiKeyCredential(""))
            .searchServiceVersion(apiVersion)
            .buildClient());
    }

    @Test
    public void verifyNullApiVersionIsInvalidAsyncTest() {
        expectThrowsWithMessage("Invalid searchServiceVersion", () -> new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .searchServiceVersion(null)
            .buildAsyncClient());
    }

    @Test
    public void verifyNullApiVersionIsInvalidTest() {
        expectThrowsWithMessage("Invalid searchServiceVersion", () -> new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .searchServiceVersion(null)
            .buildClient());
    }

    @Test
    public void verifyEmptyApiVersionSetsLatest() {
        SearchServiceClient searchServiceClient = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .buildClient();

        Assert.assertEquals(SearchServiceVersion.getLatest().getVersion(),
            searchServiceClient.getServiceVersion().getVersion());
    }

    @Test
    public void verifyEmptyApiVersionSetsLatestAsync() {
        SearchServiceAsyncClient searchServiceAsyncClient = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .buildAsyncClient();

        Assert.assertEquals(SearchServiceVersion.getLatest().getVersion(),
            searchServiceAsyncClient.getServiceVersion().getVersion());
    }

    private void expectThrowsWithMessage(String expectedMessage, Runnable runnable) {
        try {
            runnable.run();
            Assert.fail();

        } catch (Exception e) {
            Assert.assertEquals(IllegalArgumentException.class, e.getClass());
            Assert.assertTrue(e.getMessage().contains(expectedMessage));
        }
    }
}
