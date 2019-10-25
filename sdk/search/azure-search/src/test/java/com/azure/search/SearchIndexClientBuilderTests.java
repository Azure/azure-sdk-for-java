// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import org.junit.Assert;
import org.junit.Test;

public class SearchIndexClientBuilderTests {
    private final ApiKeyCredentials apiKeyCredentials = new ApiKeyCredentials("0123");
    private final String searchEndpoint = "https://test.search.windows.net";
    private final String expectedSearchServiceName = "test";
    private final String expectedDnsSuffix = "search.windows.net";
    private final String indexName = "myindex";
    private final String apiVersion = "2019-05-06";

    @Test
    public void buildSyncClientTest() {
        SearchIndexClient client = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
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
            .credential(apiKeyCredentials)
            .indexName(indexName)
            .buildClient();

        Assert.assertNotNull(client);
        Assert.assertEquals(SearchIndexClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientTest() {
        SearchIndexAsyncClient client = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
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
            .credential(apiKeyCredentials)
            .indexName(indexName)
            .buildAsyncClient();

        Assert.assertNotNull(client);
        Assert.assertEquals(SearchIndexAsyncClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void whenApiVersionNotSpecifiedThenDefaultValueExists() {
        String expectedVersion = "2019-05-06";

        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .indexName(indexName)
            .buildClient();

        Assert.assertEquals(expectedVersion, searchIndexClient.getApiVersion());

        SearchIndexAsyncClient asyncClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .indexName(indexName)
            .buildAsyncClient();
        Assert.assertEquals(expectedVersion, asyncClient.getApiVersion());
    }

    @Test
    public void whenApiVersionSpecifiedThenSpecifiedValueExists() {
        String expectedVersion = "abc";

        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .indexName(indexName)
            .apiVersion(expectedVersion)
            .buildClient();

        Assert.assertEquals(expectedVersion, searchIndexClient.getApiVersion());

        SearchIndexAsyncClient asyncClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .indexName(indexName)
            .apiVersion(expectedVersion)
            .buildAsyncClient();
        Assert.assertEquals(expectedVersion, asyncClient.getApiVersion());
    }

    @Test
    public void whenBuildAsyncClientUsingDefaultApiVersionThenSuccess() {
        SearchIndexClient client = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .indexName(indexName)
            .buildClient();

        Assert.assertEquals(apiVersion, client.getApiVersion());

        SearchIndexAsyncClient asyncClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .indexName(indexName)
            .buildAsyncClient();

        Assert.assertEquals(apiVersion, asyncClient.getApiVersion());
    }

    @Test
    public void whenBuildClientAndVerifyPropertiesThenSuccess() {
        SearchIndexClient client = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .indexName(indexName)
            .buildClient();

        Assert.assertEquals(expectedSearchServiceName, client.getSearchServiceName());
        Assert.assertEquals(expectedDnsSuffix, client.getSearchDnsSuffix());
        Assert.assertEquals(indexName, client.getIndexName());
        Assert.assertEquals(apiVersion, client.getApiVersion());

        SearchIndexAsyncClient asyncClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .indexName(indexName)
            .buildAsyncClient();

        Assert.assertEquals(expectedSearchServiceName, asyncClient.getSearchServiceName());
        Assert.assertEquals(expectedDnsSuffix, asyncClient.getSearchDnsSuffix());
        Assert.assertEquals(indexName, asyncClient.getIndexName());
        Assert.assertEquals(apiVersion, asyncClient.getApiVersion());
    }

    @Test
    public void verifyEmptyEndpointIsInvalidAsyncTest() {
        expectThrowsWithMessage("Illegal endpoint URL", () -> new SearchIndexClientBuilder()
            .endpoint("")
            .credential(apiKeyCredentials)
            .apiVersion(apiVersion)
            .indexName(indexName)
            .buildAsyncClient());
    }

    @Test
    public void verifyEmptyEndpointIsInvalidTest() {
        expectThrowsWithMessage("Illegal endpoint URL", () -> new SearchIndexClientBuilder()
            .endpoint("")
            .credential(apiKeyCredentials)
            .apiVersion(apiVersion)
            .indexName(indexName)
            .buildClient());
    }

    @Test
    public void verifyNullIndexNameIsInvalidAsyncTest() {
        expectThrowsWithMessage("Invalid indexName", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .apiVersion(apiVersion)
            .indexName(null)
            .buildAsyncClient());
    }

    @Test
    public void verifyNullIndexNameIsInvalidTest() {
        expectThrowsWithMessage("Invalid indexName", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .apiVersion(apiVersion)
            .indexName(null)
            .buildClient());
    }

    @Test
    public void verifyEmptyIndexNameIsInvalidAsyncTest() {
        expectThrowsWithMessage("Invalid indexName", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .apiVersion(apiVersion)
            .indexName("")
            .buildAsyncClient());
    }

    @Test
    public void verifyEmptyIndexNameIsInvalidTest() {
        expectThrowsWithMessage("Invalid indexName", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
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
            .credential(new ApiKeyCredentials(""))
            .indexName(indexName)
            .apiVersion(apiVersion)
            .buildAsyncClient());
    }

    @Test
    public void verifyEmptyApiKeyIsInvalidTest() {
        expectThrowsWithMessage("Empty apiKeyCredentials", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(new ApiKeyCredentials(""))
            .indexName(indexName)
            .apiVersion(apiVersion)
            .buildClient());
    }

    @Test
    public void verifyNullApiVersionIsInvalidAsyncTest() {
        expectThrowsWithMessage("Invalid apiVersion", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .indexName(indexName)
            .apiVersion(null)
            .buildAsyncClient());
    }

    @Test
    public void verifyNullApiVersionIsInvalidTest() {
        expectThrowsWithMessage("Invalid apiVersion", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .indexName(indexName)
            .apiVersion(null)
            .buildClient());
    }

    @Test
    public void verifyEmptyApiVersionIsInvalidAsyncTest() {
        expectThrowsWithMessage("Invalid apiVersion", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .indexName(indexName)
            .apiVersion("")
            .buildAsyncClient());
    }

    @Test
    public void verifyEmptyApiVersionIsInvalidTest() {
        expectThrowsWithMessage("Invalid apiVersion", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .indexName(indexName)
            .apiVersion("")
            .buildClient());
    }

    /**
     * Verify that there is a default for the dns suffix
     */
    @Test
    public void verifyDefaultDnsSuffixIsCorrectTest() {
        SearchIndexClientBuilder clientBuilder = new SearchIndexClientBuilder();
        SearchIndexAsyncClient client = clientBuilder
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .indexName(indexName)
            .apiVersion(apiVersion)
            .buildAsyncClient();

        Assert.assertNotNull(client);
        Assert.assertEquals("search.windows.net", client.getSearchDnsSuffix());
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
