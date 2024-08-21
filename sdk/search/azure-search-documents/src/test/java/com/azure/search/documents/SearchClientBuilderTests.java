// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Header;
import com.azure.search.documents.indexes.SearchIndexClientBuilderTests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
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

@Execution(ExecutionMode.CONCURRENT)
public class SearchClientBuilderTests {
    private static final MockTokenCredential SEARCH_CREDENTIAL = new MockTokenCredential();
    private static final String SEARCH_ENDPOINT = "https://test.search.windows.net";
    private static final String INDEX_NAME = "myindex";
    private static final SearchServiceVersion API_VERSION = SearchServiceVersion.V2020_06_30;

    @Test
    public void buildSyncClientTest() {
        SearchClient client = new SearchClientBuilder()
            .endpoint(SEARCH_ENDPOINT)
            .credential(SEARCH_CREDENTIAL)
            .indexName(INDEX_NAME)
            .serviceVersion(API_VERSION)
            .buildClient();

        assertNotNull(client);
        assertEquals(SearchClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildSyncClientUsingDefaultApiVersionTest() {
        SearchClient client = new SearchClientBuilder()
            .endpoint(SEARCH_ENDPOINT)
            .credential(SEARCH_CREDENTIAL)
            .indexName(INDEX_NAME)
            .buildClient();

        assertNotNull(client);
        assertEquals(SearchClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientTest() {
        SearchAsyncClient client = new SearchClientBuilder()
            .endpoint(SEARCH_ENDPOINT)
            .credential(SEARCH_CREDENTIAL)
            .indexName(INDEX_NAME)
            .serviceVersion(API_VERSION)
            .buildAsyncClient();

        assertNotNull(client);
        assertEquals(SearchAsyncClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientUsingDefaultApiVersionTest() {
        SearchAsyncClient client = new SearchClientBuilder()
            .endpoint(SEARCH_ENDPOINT)
            .credential(SEARCH_CREDENTIAL)
            .indexName(INDEX_NAME)
            .buildAsyncClient();

        assertNotNull(client);
        assertEquals(SearchAsyncClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void whenBuildClientAndVerifyPropertiesThenSuccess() {
        SearchClient client = new SearchClientBuilder()
            .endpoint(SEARCH_ENDPOINT)
            .credential(SEARCH_CREDENTIAL)
            .indexName(INDEX_NAME)
            .buildClient();

        assertEquals(SEARCH_ENDPOINT, client.getEndpoint());
        assertEquals(INDEX_NAME, client.getIndexName());

        SearchAsyncClient asyncClient = new SearchClientBuilder()
            .endpoint(SEARCH_ENDPOINT)
            .credential(SEARCH_CREDENTIAL)
            .indexName(INDEX_NAME)
            .buildAsyncClient();

        assertEquals(SEARCH_ENDPOINT, asyncClient.getEndpoint());
        assertEquals(INDEX_NAME, asyncClient.getIndexName());
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
            .endpoint(SEARCH_ENDPOINT)
            .credential(SEARCH_CREDENTIAL)
            .indexName("test_builder")
            .retryOptions(new RetryOptions(new FixedDelayOptions(3, Duration.ofSeconds(1))))
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
            .endpoint(SEARCH_ENDPOINT)
            .credential(SEARCH_CREDENTIAL)
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
            .endpoint(SEARCH_ENDPOINT)
            .credential(SEARCH_CREDENTIAL)
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
            .endpoint(SEARCH_ENDPOINT)
            .credential(SEARCH_CREDENTIAL)
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

    @Test
    public void bothRetryOptionsAndRetryPolicySet() {
        assertThrows(IllegalStateException.class, () -> new SearchClientBuilder()
            .endpoint(SEARCH_ENDPOINT)
            .credential(SEARCH_CREDENTIAL)
            .indexName(INDEX_NAME)
            .serviceVersion(API_VERSION)
            .retryOptions(new RetryOptions(new ExponentialBackoffOptions()))
            .retryPolicy(new RetryPolicy())
            .buildClient());
    }
}
