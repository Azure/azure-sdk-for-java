// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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

        assertNotNull(client);
        assertEquals(SearchIndexClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildSyncClientUsingDefaultApiVersionTest() {
        SearchIndexClient client = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .buildClient();

        assertNotNull(client);
        assertEquals(SearchIndexClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientTest() {
        SearchIndexAsyncClient client = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .apiVersion(apiVersion)
            .buildAsyncClient();

        assertNotNull(client);
        assertEquals(SearchIndexAsyncClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientUsingDefaultApiVersionTest() {
        SearchIndexAsyncClient client = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .buildAsyncClient();

        assertNotNull(client);
        assertEquals(SearchIndexAsyncClient.class.getSimpleName(), client.getClass().getSimpleName());
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

        assertEquals(expectedVersion, searchIndexClient.getApiVersion());

        SearchIndexAsyncClient asyncClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .apiVersion(expectedVersion)
            .buildAsyncClient();
        assertEquals(expectedVersion, asyncClient.getApiVersion());
    }

    @Test
    public void whenBuildAsyncClientUsingDefaultApiVersionThenSuccess() {
        SearchIndexClient client = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .buildClient();

        assertEquals(apiVersion, client.getApiVersion());

        SearchIndexAsyncClient asyncClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .buildAsyncClient();

        assertEquals(apiVersion, asyncClient.getApiVersion());
    }

    @Test
    public void whenBuildClientAndVerifyPropertiesThenSuccess() {
        SearchIndexClient client = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .buildClient();

        assertEquals(searchEndpoint, client.getEndpoint());
        assertEquals(indexName, client.getIndexName());
        assertEquals(apiVersion, client.getApiVersion());

        SearchIndexAsyncClient asyncClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .buildAsyncClient();

        assertEquals(searchEndpoint, asyncClient.getEndpoint());
        assertEquals(indexName, asyncClient.getIndexName());
        assertEquals(apiVersion, asyncClient.getApiVersion());
    }

    @Test
    public void verifyEmptyEndpointIsInvalidAsyncTest() {
        expectThrowsWithMessage("'endpoint' must be a valid URL", () -> new SearchIndexClientBuilder()
            .endpoint("")
            .credential(searchApiKeyCredential)
            .apiVersion(apiVersion)
            .indexName(indexName)
            .buildAsyncClient());
    }

    @Test
    public void verifyEmptyEndpointIsInvalidTest() {
        expectThrowsWithMessage("'endpoint' must be a valid URL", () -> new SearchIndexClientBuilder()
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
        expectNullPointerExceptionWithMessage("Empty apiKeyCredentials", () -> new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(null)
            .indexName(indexName)
            .apiVersion(apiVersion)
            .buildAsyncClient());
    }

    @Test
    public void verifyNullApiKeyIsInvalidTest() {
        expectNullPointerExceptionWithMessage("Empty apiKeyCredentials", () -> new SearchIndexClientBuilder()
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

        assertEquals(SearchServiceVersion.getLatest().getVersion(),
            searchIndexClient.getApiVersion().getVersion());
    }

    @Test
    public void verifyNewBuilderSetsLatestVersionAsync() {
        SearchIndexAsyncClient searchIndexAsyncClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName("indexName")
            .buildAsyncClient();

        assertEquals(SearchServiceVersion.getLatest().getVersion(),
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

    @Test
    public void whenCreateUsingClientBuilderThenDefaultPoliciesExists() {
        SearchIndexClientBuilder searchIndexClientBuilder = new SearchIndexClientBuilder();
        searchIndexClientBuilder
            .endpoint(searchEndpoint)
            .indexName(indexName)
            .credential(searchApiKeyCredential)
            .apiVersion(apiVersion)
            .buildAsyncClient();

        int policyCount = searchIndexClientBuilder.getPolicies().size();

        assertEquals(7, policyCount);

        assertEquals(1,
            searchIndexClientBuilder.getPolicies().stream()
                .filter(p -> p.getClass() == RetryPolicy.class).count()
        );
        assertEquals(1,
            searchIndexClientBuilder.getPolicies().stream()
                .filter(p -> p.getClass() == RequestIdPolicy.class).count()
        );
        assertEquals(1,
            searchIndexClientBuilder.getPolicies().stream()
                .filter(p -> p.getClass() == UserAgentPolicy.class).count()
        );
        assertEquals(1,
            searchIndexClientBuilder.getPolicies().stream()
                .filter(p -> p.getClass() == AddHeadersPolicy.class).count()
        );
        assertEquals(1,
            searchIndexClientBuilder.getPolicies().stream()
                .filter(p -> p.getClass() == AddDatePolicy.class).count()
        );
        assertEquals(1,
            searchIndexClientBuilder.getPolicies().stream()
                .filter(p -> p.getClass() == HttpLoggingPolicy.class).count()
        );
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

    private void expectNullPointerExceptionWithMessage(String expectedMessage, Runnable runnable) {
        try {
            runnable.run();
            fail();

        } catch (Exception e) {
            assertEquals(NullPointerException.class, e.getClass());
            assertTrue(e.getMessage().contains(expectedMessage));
        }
    }
}
