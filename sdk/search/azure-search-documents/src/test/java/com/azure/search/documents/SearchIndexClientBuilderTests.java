// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.net.MalformedURLException;
import java.security.SecureRandom;

import static com.azure.search.documents.SearchServiceClientBuilderTests.request;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SearchIndexClientBuilderTests {
    private final AzureKeyCredential searchApiKeyCredential = new AzureKeyCredential("0123");
    private final String searchEndpoint = "https://test.search.windows.net";
    private final String indexName = "myindex";
    private final SearchServiceVersion apiVersion = SearchServiceVersion.V2019_05_06;

    @Test
    public void buildSyncClientTest() {
        SearchIndexClient client = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .serviceVersion(apiVersion)
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
            .serviceVersion(apiVersion)
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
            .serviceVersion(expectedVersion)
            .buildClient();

        assertEquals(expectedVersion, searchIndexClient.getServiceVersion());

        SearchIndexAsyncClient asyncClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .serviceVersion(expectedVersion)
            .buildAsyncClient();
        assertEquals(expectedVersion, asyncClient.getServiceVersion());
    }

    @Test
    public void whenBuildAsyncClientUsingDefaultApiVersionThenSuccess() {
        SearchIndexClient client = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .buildClient();

        assertEquals(apiVersion, client.getServiceVersion());

        SearchIndexAsyncClient asyncClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .buildAsyncClient();

        assertEquals(apiVersion, asyncClient.getServiceVersion());
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
        assertEquals(apiVersion, client.getServiceVersion());

        SearchIndexAsyncClient asyncClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .buildAsyncClient();

        assertEquals(searchEndpoint, asyncClient.getEndpoint());
        assertEquals(indexName, asyncClient.getIndexName());
        assertEquals(apiVersion, asyncClient.getServiceVersion());
    }

    @Test
    public void emptyEndpointThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SearchIndexClientBuilder().endpoint(""));
    }

    @Test
    public void nullIndexNameThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SearchIndexClientBuilder().indexName(null));
    }

    @Test
    public void emptyIndexNameThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SearchIndexClientBuilder().indexName(""));
    }

    @Test
    public void nullCredentialThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SearchIndexClientBuilder().credential(null));
    }

    @Test
    public void credentialWithEmptyApiKeyThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SearchIndexClientBuilder()
            .credential(new AzureKeyCredential("")));
    }

    @Test
    public void nullApiVersionUsesLatest() {
        SearchIndexClientBuilder builder = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .serviceVersion(null);

        assertEquals(SearchServiceVersion.getLatest(), builder.buildAsyncClient().getServiceVersion());
        assertEquals(SearchServiceVersion.getLatest(), builder.buildClient().getServiceVersion());
    }

    @Test
    public void verifyNewBuilderSetsLatestVersion() {
        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName("indexName")
            .buildClient();

        assertEquals(SearchServiceVersion.getLatest().getVersion(),
            searchIndexClient.getServiceVersion().getVersion());
    }

    @Test
    public void verifyNewBuilderSetsLatestVersionAsync() {
        SearchIndexAsyncClient searchIndexAsyncClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName("indexName")
            .buildAsyncClient();

        assertEquals(SearchServiceVersion.getLatest().getVersion(),
            searchIndexAsyncClient.getServiceVersion().getVersion());
    }

    @Test
    public void indexClientFreshDateOnRetry() throws MalformedURLException {
        byte[] randomData = new byte[256];
        new SecureRandom().nextBytes(randomData);
        SearchIndexAsyncClient searchIndexAsyncClient = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName("test_builder")
            .httpClient(new SearchServiceClientBuilderTests.FreshDateTestClient())
            .buildAsyncClient();

        StepVerifier.create(searchIndexAsyncClient.getHttpPipeline().send(
            request(searchIndexAsyncClient.getEndpoint())))
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());
            })
            .verifyComplete();
    }
}
