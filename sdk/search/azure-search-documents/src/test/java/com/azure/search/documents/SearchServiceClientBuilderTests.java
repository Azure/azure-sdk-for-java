// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.DateTimeRfc1123;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SearchServiceClientBuilderTests {
    private final AzureKeyCredential searchApiKeyCredential = new AzureKeyCredential("0123");
    private final String searchEndpoint = "https://test.search.windows.net";
    private final SearchServiceVersion apiVersion = SearchServiceVersion.V2019_05_06;

    @Test
    public void buildSyncClientTest() {
        SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .serviceVersion(apiVersion)
            .buildClient();

        assertNotNull(client);
        assertEquals(SearchServiceClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildSyncClientUsingDefaultApiVersionTest() {
        SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .buildClient();

        assertNotNull(client);
        assertEquals(SearchServiceClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientTest() {
        SearchServiceAsyncClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .serviceVersion(apiVersion)
            .buildAsyncClient();

        assertNotNull(client);
        assertEquals(SearchServiceAsyncClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientUsingDefaultApiVersionTest() {
        SearchServiceAsyncClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .buildAsyncClient();

        assertNotNull(client);
        assertEquals(SearchServiceAsyncClient.class.getSimpleName(), client.getClass().getSimpleName());
    }

    @Test
    public void whenApiVersionSpecifiedThenSpecifiedValueExists() {
        SearchServiceVersion expectedApiVersion = SearchServiceVersion.V2019_05_06;

        SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .serviceVersion(expectedApiVersion)
            .buildClient();

        assertEquals(expectedApiVersion.getVersion(), client.getServiceVersion().getVersion());

        SearchServiceAsyncClient asyncClient = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .serviceVersion(expectedApiVersion)
            .buildAsyncClient();
        assertEquals(expectedApiVersion.getVersion(), asyncClient.getServiceVersion().getVersion());
    }

    @Test
    public void whenBuildClientAndVerifyPropertiesThenSuccess() {
        SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .buildClient();

        assertEquals(searchEndpoint, client.getEndpoint());
        assertEquals(apiVersion, client.getServiceVersion());

        SearchServiceAsyncClient asyncClient = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .serviceVersion(apiVersion)
            .buildAsyncClient();

        assertEquals(searchEndpoint, asyncClient.getEndpoint());
        assertEquals(apiVersion, asyncClient.getServiceVersion());
    }

    @Test
    public void emptyEndpointThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SearchServiceClientBuilder().endpoint(""));
    }

    @Test
    public void nullCredentialThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SearchServiceClientBuilder().credential(null));
    }

    @Test
    public void credentialWithEmptyApiKeyThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SearchServiceClientBuilder()
            .credential(new AzureKeyCredential("")));
    }

    @Test
    void nullApiVersionSetsLatest() {
        SearchServiceClientBuilder builder = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .serviceVersion(null);

        assertEquals(SearchServiceVersion.getLatest(), builder.buildAsyncClient().getServiceVersion());
        assertEquals(SearchServiceVersion.getLatest(), builder.buildClient().getServiceVersion());
    }

    @Test
    public void verifyEmptyApiVersionSetsLatest() {
        SearchServiceClient searchServiceClient = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .buildClient();

        assertEquals(SearchServiceVersion.getLatest(), searchServiceClient.getServiceVersion());
    }

    @Test
    public void verifyEmptyApiVersionSetsLatestAsync() {
        SearchServiceAsyncClient searchServiceAsyncClient = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .buildAsyncClient();

        assertEquals(SearchServiceVersion.getLatest(), searchServiceAsyncClient.getServiceVersion());
    }

    @Test
    public void serviceClientFreshDateOnRetry() throws MalformedURLException {
        byte[] randomData = new byte[256];
        new SecureRandom().nextBytes(randomData);
        SearchServiceAsyncClient searchServiceAsyncClient = new SearchServiceClientBuilder()
            .endpoint(searchEndpoint)
            .credential(searchApiKeyCredential)
            .httpClient(new FreshDateTestClient())
            .buildAsyncClient();


        StepVerifier.create(searchServiceAsyncClient.getHttpPipeline().send(
            request(searchServiceAsyncClient.getEndpoint())))
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());
            })
            .verifyComplete();
    }

    static HttpRequest request(String url) throws MalformedURLException {
        return new HttpRequest(HttpMethod.HEAD,
            new URL(url), new HttpHeaders().put("Content-Length", "0"),
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
}
