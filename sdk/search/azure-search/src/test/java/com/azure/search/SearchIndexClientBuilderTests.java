// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
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
            .searchServiceVersion(apiVersion)
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
            .searchServiceVersion(apiVersion)
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
            .searchServiceVersion(expectedVersion)
            .buildClient();

        Assert.assertEquals(expectedVersion, searchIndexClient.getServiceVersion());

        SearchIndexAsyncClient asyncClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .searchServiceVersion(expectedVersion)
            .buildAsyncClient();
        Assert.assertEquals(expectedVersion, asyncClient.getServiceVersion());
    }

    @Test
    public void whenBuildAsyncClientUsingDefaultApiVersionThenSuccess() {
        SearchIndexClient client = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .buildClient();

        Assert.assertEquals(apiVersion, client.getServiceVersion());

        SearchIndexAsyncClient asyncClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .buildAsyncClient();

        Assert.assertEquals(apiVersion, asyncClient.getServiceVersion());
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
        Assert.assertEquals(apiVersion, client.getServiceVersion());

        SearchIndexAsyncClient asyncClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .buildAsyncClient();

        Assert.assertEquals(searchEndpoint, asyncClient.getEndpoint());
        Assert.assertEquals(indexName, asyncClient.getIndexName());
        Assert.assertEquals(apiVersion, asyncClient.getServiceVersion());
    }

    @Test
    public void verifyEmptyEndpointIsInvalidAsyncTest() {
        expectThrowsWithMessage("'endpoint' must be a valid URL", () -> new SearchIndexClientBuilder()
            .endpoint("")
            .credential(searchApiKeyCredential)
            .searchServiceVersion(apiVersion)
            .indexName(indexName)
            .buildAsyncClient());
    }

    @Test
    public void verifyEmptyEndpointIsInvalidTest() {
        expectThrowsWithMessage("'endpoint' must be a valid URL", () -> new SearchIndexClientBuilder()
            .endpoint("")
            .credential(searchApiKeyCredential)
            .searchServiceVersion(apiVersion)
            .indexName(indexName)
            .buildClient());
    }

    @Test
    public void verifyNullIndexNameIsInvalidAsyncTest() {
        expectThrowsWithMessage("Invalid indexName", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .searchServiceVersion(apiVersion)
            .indexName(null)
            .buildAsyncClient());
    }

    @Test
    public void verifyNullIndexNameIsInvalidTest() {
        expectThrowsWithMessage("Invalid indexName", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .searchServiceVersion(apiVersion)
            .indexName(null)
            .buildClient());
    }

    @Test
    public void verifyEmptyIndexNameIsInvalidAsyncTest() {
        expectThrowsWithMessage("Invalid indexName", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .searchServiceVersion(apiVersion)
            .indexName("")
            .buildAsyncClient());
    }

    @Test
    public void verifyEmptyIndexNameIsInvalidTest() {
        expectThrowsWithMessage("Invalid indexName", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .searchServiceVersion(apiVersion)
            .indexName("")
            .buildClient());
    }

    @Test
    public void verifyNullApiKeyIsInvalidAsyncTest() {
        expectNullPointerExceptionWithMessage("Empty apiKeyCredentials", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(null)
            .indexName(indexName)
            .searchServiceVersion(apiVersion)
            .buildAsyncClient());
    }

    @Test
    public void verifyNullApiKeyIsInvalidTest() {
        expectNullPointerExceptionWithMessage("Empty apiKeyCredentials", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(null)
            .indexName(indexName)
            .searchServiceVersion(apiVersion)
            .buildClient());
    }

    @Test
    public void verifyEmptyApiKeyIsInvalidAsyncTest() {
        expectThrowsWithMessage("Empty apiKeyCredentials", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(new SearchApiKeyCredential(""))
            .indexName(indexName)
            .searchServiceVersion(apiVersion)
            .buildAsyncClient());
    }

    @Test
    public void verifyEmptyApiKeyIsInvalidTest() {
        expectThrowsWithMessage("Empty apiKeyCredentials", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(new SearchApiKeyCredential(""))
            .indexName(indexName)
            .searchServiceVersion(apiVersion)
            .buildClient());
    }

    @Test
    public void verifyNullApiVersionIsInvalidAsyncTest() {
        expectThrowsWithMessage("Invalid searchServiceVersion", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .searchServiceVersion(null)
            .buildAsyncClient());
    }

    @Test
    public void verifyNullApiVersionIsInvalidTest() {
        expectThrowsWithMessage("Invalid searchServiceVersion", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .searchServiceVersion(null)
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
            searchIndexClient.getServiceVersion().getVersion());
    }

    @Test
    public void verifyNewBuilderSetsLatestVersionAsync() {
        SearchIndexAsyncClient searchIndexAsyncClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName("indexName")
            .buildAsyncClient();

        Assert.assertEquals(SearchServiceVersion.getLatest().getVersion(),
            searchIndexAsyncClient.getServiceVersion().getVersion());
    }

    @Test
    public void verifyEmptyVersionThrowsIllegalArgumentException() {
        expectThrowsWithMessage("Invalid searchServiceVersion",
            () -> new SearchIndexClientBuilder()
                .endpoint(searchEndpoint)
                .credential(searchApiKeyCredential)
                .indexName("indexName")
                .searchServiceVersion(null)
                .buildClient()
        );
    }

    @Test
    public void verifyEmptyVersionThrowsIllegalArgumentExceptionAsync() {
        expectThrowsWithMessage("Invalid searchServiceVersion",
            () ->  new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName("indexName")
            .searchServiceVersion(null)
            .buildAsyncClient()
        );
    }

    @Test
    public void whenCreateUsingClientBuilderThenDefaultPoliciesExists() {
        SearchIndexClientBuilder searchIndexClientBuilder = new SearchIndexClientBuilder();
        searchIndexClientBuilder
            .endpoint(searchEndpoint)
            .indexName(indexName)
            .credential(searchApiKeyCredential)
            .searchServiceVersion(apiVersion)
            .buildAsyncClient();

        int policyCount = searchIndexClientBuilder.getPolicies().size();

        Assert.assertEquals(7, policyCount);

        Assert.assertEquals(1,
            searchIndexClientBuilder.getPolicies().stream()
                .filter(p -> p.getClass() == RetryPolicy.class).count()
        );
        Assert.assertEquals(1,
            searchIndexClientBuilder.getPolicies().stream()
                .filter(p -> p.getClass() == RequestIdPolicy.class).count()
        );
        Assert.assertEquals(1,
            searchIndexClientBuilder.getPolicies().stream()
                .filter(p -> p.getClass() == UserAgentPolicy.class).count()
        );
        Assert.assertEquals(1,
            searchIndexClientBuilder.getPolicies().stream()
                .filter(p -> p.getClass() == AddHeadersPolicy.class).count()
        );
        Assert.assertEquals(1,
            searchIndexClientBuilder.getPolicies().stream()
                .filter(p -> p.getClass() == AddDatePolicy.class).count()
        );
        Assert.assertEquals(1,
            searchIndexClientBuilder.getPolicies().stream()
                .filter(p -> p.getClass() == HttpLoggingPolicy.class).count()
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

    private void expectNullPointerExceptionWithMessage(String expectedMessage, Runnable runnable) {
        try {
            runnable.run();
            Assert.fail();

        } catch (Exception e) {
            Assert.assertEquals(NullPointerException.class, e.getClass());
            Assert.assertTrue(e.getMessage().contains(expectedMessage));
        }
    }
}
