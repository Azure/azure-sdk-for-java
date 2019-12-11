// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class SearchIndexClientBuilderTests {
    private final SearchApiKeyCredential searchApiKeyCredential = new SearchApiKeyCredential("0123");
    private final String searchEndpoint = "https://test.search.windows.net";
    private final String indexName = "myindex";
    private final SearchServiceVersion apiVersion = SearchServiceVersion.V2019_05_06;

    @Test
    public void buildSyncClientTest() {
        SearchIndexClient client = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .apiVersion(apiVersion)
            .buildClient();

        Assert.assertNotNull(client);
        Assert.assertEquals(SearchIndexClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildSyncClientUsingDefaultApiVersionTest() {
        SearchIndexClient client = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .buildClient();

        Assert.assertNotNull(client);
        Assert.assertEquals(SearchIndexClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientTest() {
        SearchIndexAsyncClient client = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .apiVersion(apiVersion)
            .buildAsyncClient();

        Assert.assertNotNull(client);
        Assert.assertEquals(SearchIndexAsyncClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientUsingDefaultApiVersionTest() {
        SearchIndexAsyncClient client = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .buildAsyncClient();

        Assert.assertNotNull(client);
        Assert.assertEquals(SearchIndexAsyncClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void whenApiVersionSpecifiedThenSpecifiedValueExists() {
        SearchServiceVersion expectedVersion = SearchServiceVersion.V2019_05_06;

        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .apiVersion(expectedVersion)
            .buildClient();

        Assert.assertEquals(expectedVersion, searchIndexClient.getApiVersion());

        SearchIndexAsyncClient asyncClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .apiVersion(expectedVersion)
            .buildAsyncClient();
        Assert.assertEquals(expectedVersion, asyncClient.getApiVersion());
    }

    @Test
    public void whenBuildAsyncClientUsingDefaultApiVersionThenSuccess() {
        SearchIndexClient client = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .buildClient();

        Assert.assertEquals(apiVersion, client.getApiVersion());

        SearchIndexAsyncClient asyncClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .buildAsyncClient();

        Assert.assertEquals(apiVersion, asyncClient.getApiVersion());
    }

    @Test
    public void whenBuildClientAndVerifyPropertiesThenSuccess() {
        SearchIndexClient client = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .buildClient();

        Assert.assertEquals(searchEndpoint, client.getEndpoint());
        Assert.assertEquals(indexName, client.getIndexName());
        Assert.assertEquals(apiVersion, client.getApiVersion());

        SearchIndexAsyncClient asyncClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .buildAsyncClient();

        Assert.assertEquals(searchEndpoint, asyncClient.getEndpoint());
        Assert.assertEquals(indexName, asyncClient.getIndexName());
        Assert.assertEquals(apiVersion, asyncClient.getApiVersion());
    }

    @Test
    public void verifyEmptyEndpointIsInvalidAsyncTest() {
        expectThrowsWithMessage("Illegal endpoint URL", () -> new SearchIndexClientBuilder()
            .endpoint("")
            .credential(searchApiKeyCredential)
            .apiVersion(apiVersion)
            .indexName(indexName)
            .buildAsyncClient());
    }

    @Test
    public void verifyEmptyEndpointIsInvalidTest() {
        expectThrowsWithMessage("Illegal endpoint URL", () -> new SearchIndexClientBuilder()
            .endpoint("")
            .credential(searchApiKeyCredential)
            .apiVersion(apiVersion)
            .indexName(indexName)
            .buildClient());
    }

    @Test
    public void verifyNullIndexNameIsInvalidAsyncTest() {
        expectThrowsWithMessage("Invalid indexName", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .apiVersion(apiVersion)
            .indexName(null)
            .buildAsyncClient());
    }

    @Test
    public void verifyNullIndexNameIsInvalidTest() {
        expectThrowsWithMessage("Invalid indexName", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .apiVersion(apiVersion)
            .indexName(null)
            .buildClient());
    }

    @Test
    public void verifyEmptyIndexNameIsInvalidAsyncTest() {
        expectThrowsWithMessage("Invalid indexName", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .apiVersion(apiVersion)
            .indexName("")
            .buildAsyncClient());
    }

    @Test
    public void verifyEmptyIndexNameIsInvalidTest() {
        expectThrowsWithMessage("Invalid indexName", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .apiVersion(apiVersion)
            .indexName("")
            .buildClient());
    }

    @Test
    public void verifyNullApiKeyIsInvalidAsyncTest() {
        expectThrowsWithMessage("Empty apiKeyCredentials", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(null)
            .indexName(indexName)
            .apiVersion(apiVersion)
            .buildAsyncClient());
    }

    @Test
    public void verifyNullApiKeyIsInvalidTest() {
        expectThrowsWithMessage("Empty apiKeyCredentials", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(null)
            .indexName(indexName)
            .apiVersion(apiVersion)
            .buildClient());
    }

    @Test
    public void verifyEmptyApiKeyIsInvalidAsyncTest() {
        expectThrowsWithMessage("Empty apiKeyCredentials", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(new SearchApiKeyCredential(""))
            .indexName(indexName)
            .apiVersion(apiVersion)
            .buildAsyncClient());
    }

    @Test
    public void verifyEmptyApiKeyIsInvalidTest() {
        expectThrowsWithMessage("Empty apiKeyCredentials", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(new SearchApiKeyCredential(""))
            .indexName(indexName)
            .apiVersion(apiVersion)
            .buildClient());
    }

    @Test
    public void verifyNullApiVersionIsInvalidAsyncTest() {
        expectThrowsWithMessage("Invalid apiVersion", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .apiVersion(null)
            .buildAsyncClient());
    }

    @Test
    public void verifyNullApiVersionIsInvalidTest() {
        expectThrowsWithMessage("Invalid apiVersion", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .apiVersion(null)
            .buildClient());
    }

    @Test
    public void verifyNewBuilderSetsLatestVersion() {
        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName("indexName")
            .buildClient();

        Assert.assertEquals(SearchServiceVersion.getLatest().getVersion(),
            searchIndexClient.getApiVersion().getVersion());
    }

    @Test
    public void verifyNewBuilderSetsLatestVersionAsync() {
        SearchIndexAsyncClient searchIndexAsyncClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName("indexName")
            .buildAsyncClient();

        Assert.assertEquals(SearchServiceVersion.getLatest().getVersion(),
            searchIndexAsyncClient.getApiVersion().getVersion());
    }

    @Test
    public void verifyEmptyVersionThrowsIllegalArgumentException() {
        expectThrowsWithMessage("Invalid apiVersion",
            () -> new SearchIndexClientBuilder()
                .endpoint(searchEndpoint)
                .credential(searchApiKeyCredential)
                .indexName("indexName")
                .apiVersion(null)
                .buildClient()
        );
    }

    @Test
    public void verifyEmptyVersionThrowsIllegalArgumentExceptionAsync() {
        expectThrowsWithMessage("Invalid apiVersion",
            () ->  new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName("indexName")
            .apiVersion(null)
            .buildAsyncClient()
        );
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
