// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Header;
import com.azure.search.documents.indexes.SearchIndexClientBuilderTests;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.MalformedURLException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Collections;

import static com.azure.search.documents.indexes.SearchIndexClientBuilderTests.request;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchClientBuilderTests {
    private final AzureKeyCredential searchApiKeyCredential = new AzureKeyCredential("0123");
    private final String searchEndpoint = "https://test.search.windows.net";
    private final String indexName = "myindex";
    private final SearchServiceVersion apiVersion = SearchServiceVersion.V2020_06_30;

    @Test
    public void buildSyncClientTest() {
        SearchClient client = new SearchClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .serviceVersion(apiVersion)
            .buildClient();

        assertNotNull(client);
        assertEquals(SearchClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildSyncClientUsingDefaultApiVersionTest() {
        SearchClient client = new SearchClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .buildClient();

        assertNotNull(client);
        assertEquals(SearchClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientTest() {
        SearchAsyncClient client = new SearchClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .serviceVersion(apiVersion)
            .buildAsyncClient();

        assertNotNull(client);
        assertEquals(SearchAsyncClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientUsingDefaultApiVersionTest() {
        SearchAsyncClient client = new SearchClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .buildAsyncClient();

        assertNotNull(client);
        assertEquals(SearchAsyncClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void whenBuildClientAndVerifyPropertiesThenSuccess() {
        SearchClient client = new SearchClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .buildClient();

        assertEquals(searchEndpoint, client.getEndpoint());
        assertEquals(indexName, client.getIndexName());

        SearchAsyncClient asyncClient = new SearchClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName(indexName)
            .buildAsyncClient();

        assertEquals(searchEndpoint, asyncClient.getEndpoint());
        assertEquals(indexName, asyncClient.getIndexName());
    }

    @Test
    public void emptyEndpointThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SearchClientBuilder().endpoint(""));
    }

    @Test
    public void nullIndexNameThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SearchClientBuilder().indexName(null));
    }

    @Test
    public void emptyIndexNameThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SearchClientBuilder().indexName(""));
    }

    @Test
    public void credentialWithEmptyApiKeyThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SearchClientBuilder()
            .credential(new AzureKeyCredential("")));
    }

    @Test
    public void indexClientFreshDateOnRetry() throws MalformedURLException {
        byte[] randomData = new byte[256];
        new SecureRandom().nextBytes(randomData);
        SearchAsyncClient searchAsyncClient = new SearchClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName("test_builder")
            .httpClient(new SearchIndexClientBuilderTests.FreshDateTestClient())
            .buildAsyncClient();

        StepVerifier.create(searchAsyncClient.getHttpPipeline().send(
            request(searchAsyncClient.getEndpoint())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void clientOptionsIsPreferredOverLogOptions() {
        SearchClient searchClient = new SearchClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName("test_builder")
            .httpLogOptions(new HttpLogOptions().setApplicationId("anOldApplication"))
            .clientOptions(new ClientOptions().setApplicationId("aNewApplication"))
            .retryPolicy(new RetryPolicy(new FixedDelay(3, Duration.ofMillis(1))))
            .httpClient(httpRequest -> {
                assertTrue(httpRequest.getHeaders().getValue("User-Agent").contains("aNewApplication"));
                return Mono.just(new MockHttpResponse(httpRequest, 400));
            })
            .buildClient();

        assertThrows(HttpResponseException.class, searchClient::getDocumentCount);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void applicationIdFallsBackToLogOptions() {
        SearchClient searchClient = new SearchClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName("test_builder")
            .httpLogOptions(new HttpLogOptions().setApplicationId("anOldApplication"))
            .retryPolicy(new RetryPolicy(new FixedDelay(3, Duration.ofMillis(1))))
            .httpClient(httpRequest -> {
                assertTrue(httpRequest.getHeaders().getValue("User-Agent").contains("anOldApplication"));
                return Mono.just(new MockHttpResponse(httpRequest, 400));
            })
            .buildClient();

        assertThrows(HttpResponseException.class, searchClient::getDocumentCount);
    }

    @Test
    public void clientOptionHeadersAreAddedLast() {
        SearchClient searchClient = new SearchClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .indexName("test_builder")
            .clientOptions(new ClientOptions()
                .setHeaders(Collections.singletonList(new Header("User-Agent", "custom"))))
            .retryPolicy(new RetryPolicy(new FixedDelay(3, Duration.ofMillis(1))))
            .httpClient(httpRequest -> {
                assertEquals("custom", httpRequest.getHeaders().getValue("User-Agent"));
                return Mono.just(new MockHttpResponse(httpRequest, 400));
            })
            .buildClient();

        assertThrows(HttpResponseException.class, searchClient::getDocumentCount);
    }
}
