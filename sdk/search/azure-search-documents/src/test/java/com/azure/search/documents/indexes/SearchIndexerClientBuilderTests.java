// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.indexes;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.core.util.Header;
import com.azure.search.documents.SearchServiceVersion;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchIndexerClientBuilderTests {
    private final AzureKeyCredential searchApiKeyCredential = new AzureKeyCredential("0123");
    private final String searchEndpoint = "https://test.search.windows.net";
    private final SearchServiceVersion apiVersion = SearchServiceVersion.V2020_06_30;

    @Test
    public void buildSyncClientTest() {
        SearchIndexerClient client = new SearchIndexerClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .serviceVersion(apiVersion)
            .buildClient();

        assertNotNull(client);
        assertEquals(SearchIndexerClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildSyncClientUsingDefaultApiVersionTest() {
        SearchIndexerClient client = new SearchIndexerClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .buildClient();

        assertNotNull(client);
        assertEquals(SearchIndexerClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientTest() {
        SearchIndexerAsyncClient client = new SearchIndexerClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .serviceVersion(apiVersion)
            .buildAsyncClient();

        assertNotNull(client);
        assertEquals(SearchIndexerAsyncClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientUsingDefaultApiVersionTest() {
        SearchIndexerAsyncClient client = new SearchIndexerClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .buildAsyncClient();

        assertNotNull(client);
        assertEquals(SearchIndexerAsyncClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void whenBuildClientAndVerifyPropertiesThenSuccess() {
        SearchIndexerClient client = new SearchIndexerClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .buildClient();

        assertEquals(searchEndpoint, client.getEndpoint());

        SearchIndexerAsyncClient asyncClient = new SearchIndexerClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .serviceVersion(apiVersion)
            .buildAsyncClient();

        assertEquals(searchEndpoint, asyncClient.getEndpoint());
    }

    @Test
    public void emptyEndpointThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SearchIndexerClientBuilder().endpoint(""));
    }

    @Test
    public void nullCredentialThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SearchIndexerClientBuilder().credential(null));
    }

    @Test
    public void credentialWithEmptyApiKeyThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SearchIndexerClientBuilder()
            .credential(new AzureKeyCredential("")));
    }

    @Test
    public void serviceClientFreshDateOnRetry() throws MalformedURLException {
        byte[] randomData = new byte[256];
        new SecureRandom().nextBytes(randomData);
        SearchIndexerAsyncClient searchIndexerAsyncClient = new SearchIndexerClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .httpClient(new FreshDateTestClient())
            .buildAsyncClient();


        StepVerifier.create(searchIndexerAsyncClient.getHttpPipeline().send(
            request(searchIndexerAsyncClient.getEndpoint())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    static HttpRequest request(String url) throws MalformedURLException {
        return new HttpRequest(HttpMethod.HEAD,
            new URL(url), new HttpHeaders().set("Content-Length", "0"),
            Flux.empty());
    }

    static final class FreshDateTestClient implements HttpClient {
        private DateTimeRfc1123 firstDate;

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            if (firstDate == null) {
                firstDate = convertToDateObject(request.getHeaders().getValue("Date"));
                return Mono.error(new IOException("IOException!"));
            }

            assert !firstDate.equals(convertToDateObject(request.getHeaders().getValue("Date")));
            return Mono.just(new MockHttpResponse(request, 200));
        }

        private static DateTimeRfc1123 convertToDateObject(String dateHeader) {
            if (CoreUtils.isNullOrEmpty(dateHeader)) {
                throw new RuntimeException("Failed to set 'Date' header.");
            }

            return new DateTimeRfc1123(dateHeader);
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void clientOptionsIsPreferredOverLogOptions() {
        SearchIndexerClient searchIndexerClient = new SearchIndexerClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .httpLogOptions(new HttpLogOptions().setApplicationId("anOldApplication"))
            .clientOptions(new ClientOptions().setApplicationId("aNewApplication"))
            .retryPolicy(new RetryPolicy(new FixedDelay(3, Duration.ofMillis(1))))
            .httpClient(httpRequest -> {
                assertTrue(httpRequest.getHeaders().getValue("User-Agent").contains("aNewApplication"));
                return Mono.just(new MockHttpResponse(httpRequest, 400));
            })
            .buildClient();

        assertThrows(HttpResponseException.class, () -> searchIndexerClient.getIndexer("anindexer"));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void applicationIdFallsBackToLogOptions() {
        SearchIndexerClient searchIndexerClient = new SearchIndexerClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .httpLogOptions(new HttpLogOptions().setApplicationId("anOldApplication"))
            .retryPolicy(new RetryPolicy(new FixedDelay(3, Duration.ofMillis(1))))
            .httpClient(httpRequest -> {
                assertTrue(httpRequest.getHeaders().getValue("User-Agent").contains("anOldApplication"));
                return Mono.just(new MockHttpResponse(httpRequest, 400));
            })
            .buildClient();

        assertThrows(HttpResponseException.class, () -> searchIndexerClient.getIndexer("anindexer"));
    }

    @Test
    public void clientOptionHeadersAreAddedLast() {
        SearchIndexerClient searchIndexerClient = new SearchIndexerClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .clientOptions(new ClientOptions()
                .setHeaders(Collections.singletonList(new Header("User-Agent", "custom"))))
            .retryPolicy(new RetryPolicy(new FixedDelay(3, Duration.ofMillis(1))))
            .httpClient(httpRequest -> {
                assertEquals("custom", httpRequest.getHeaders().getValue("User-Agent"));
                return Mono.just(new MockHttpResponse(httpRequest, 400));
            })
            .buildClient();

        assertThrows(HttpResponseException.class, () -> searchIndexerClient.getIndexer("anindexer"));
    }
}
