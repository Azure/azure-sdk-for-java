// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import org.junit.Assert;
import org.junit.Test;

public class SearchServiceClientBuilderTests {
    private final ApiKeyCredentials apiKeyCredentials = new ApiKeyCredentials("0123");
    private final String searchEndpoint = "https://test.search.windows.net";
    private final String expectedSearchServiceName = "test";
    private final String expectedDnsSuffix = "search.windows.net";
    private final String indexName = "myindex";
    private final String apiVersion = "2019-05-06";

    @Test
    public void buildSyncClientTest() {
        SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .apiVersion(apiVersion)
            .buildClient();

        Assert.assertNotNull(client);
        Assert.assertEquals(SearchServiceClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildSyncClientUsingDefaultApiVersionTest() {
        SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .buildClient();

        Assert.assertNotNull(client);
        Assert.assertEquals(SearchServiceClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientTest() {
        SearchServiceAsyncClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .apiVersion(apiVersion)
            .buildAsyncClient();

        Assert.assertNotNull(client);
        Assert.assertEquals(SearchServiceAsyncClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientUsingDefaultApiVersionTest() {
        SearchServiceAsyncClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .buildAsyncClient();

        Assert.assertNotNull(client);
        Assert.assertEquals(SearchServiceAsyncClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void whenApiVersionNotSpecifiedThenDefaultValueExists() {
        String expectedApiVersion = "2019-05-06";

        SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .buildClient();

        Assert.assertEquals(expectedApiVersion, client.getApiVersion());

        SearchServiceAsyncClient asyncClient = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .buildAsyncClient();
        Assert.assertEquals(expectedApiVersion, asyncClient.getApiVersion());
    }

    @Test
    public void whenApiVersionSpecifiedThenSpecifiedValueExists() {
        String expectedApiVersion = "abc";

        SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .apiVersion(expectedApiVersion)
            .buildClient();

        Assert.assertEquals(expectedApiVersion, client.getApiVersion());

        SearchServiceAsyncClient asyncClient = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .apiVersion(expectedApiVersion)
            .buildAsyncClient();
        Assert.assertEquals(expectedApiVersion, asyncClient.getApiVersion());
    }

    @Test
    public void whenBuildClientAndVerifyPropertiesThenSuccess() {
        SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .buildClient();

        Assert.assertEquals(expectedSearchServiceName, client.getSearchServiceName());
        Assert.assertEquals(expectedDnsSuffix, client.getSearchDnsSuffix());
        Assert.assertEquals(apiVersion, client.getApiVersion());

        SearchServiceAsyncClient asyncClient = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .apiVersion(apiVersion)
            .buildAsyncClient();

        Assert.assertEquals(expectedSearchServiceName, asyncClient.getSearchServiceName());
        Assert.assertEquals(expectedDnsSuffix, asyncClient.getSearchDnsSuffix());
        Assert.assertEquals(apiVersion, asyncClient.getApiVersion());
    }

    @Test
    public void verifyEmptyEndpointIsInvalidAsyncTest() {
        expectThrowsWithMessage("Illegal endpoint URL", () -> new SearchServiceClientBuilder()
            .endpoint("")
            .credential(apiKeyCredentials)
            .apiVersion(apiVersion)
            .buildAsyncClient());
    }

    @Test
    public void verifyEmptyEndpointIsInvalidTest() {
        expectThrowsWithMessage("Illegal endpoint URL", () -> new SearchServiceClientBuilder()
            .endpoint("")
            .credential(apiKeyCredentials)
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
            .credential(new ApiKeyCredentials(""))
            .apiVersion(apiVersion)
            .buildAsyncClient());
    }

    @Test
    public void verifyEmptyApiKeyIsInvalidTest() {
        expectThrowsWithMessage("Empty apiKeyCredentials", () -> new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(new ApiKeyCredentials(""))
            .apiVersion(apiVersion)
            .buildClient());
    }

    @Test
    public void verifyNullApiVersionIsInvalidAsyncTest() {
        expectThrowsWithMessage("Invalid apiVersion", () -> new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .apiVersion(null)
            .buildAsyncClient());
    }

    @Test
    public void verifyNullApiVersionIsInvalidTest() {
        expectThrowsWithMessage("Invalid apiVersion", () -> new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .apiVersion(null)
            .buildClient());
    }

    @Test
    public void verifyEmptyApiVersionIsInvalidAsyncTest() {
        expectThrowsWithMessage("Invalid apiVersion", () -> new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .apiVersion("")
            .buildAsyncClient());
    }

    @Test
    public void verifyEmptyApiVersionIsInvalidTest() {
        expectThrowsWithMessage("Invalid apiVersion", () -> new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(apiKeyCredentials)
            .apiVersion("")
            .buildClient());
    }

    @Test
    public void verifyDefaultDnsSuffixIsCorrectTest() {
        SearchIndexClientBuilder clientBuilder = new SearchIndexClientBuilder();
        SearchIndexAsyncClient client = clientBuilder
            .endpoint(searchEndpoint)
            .indexName(indexName)
            .credential(apiKeyCredentials)
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
