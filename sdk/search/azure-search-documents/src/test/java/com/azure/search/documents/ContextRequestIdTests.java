// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.SearchIndexerAsyncClient;
import com.azure.search.documents.indexes.SearchIndexerClient;
import com.azure.search.documents.indexes.SearchIndexerClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests passing {@code x-ms-client-request-id} using {@link Context}.
 */
@Execution(ExecutionMode.CONCURRENT)
public class ContextRequestIdTests {
    private static final HttpHeaderName REQUEST_ID_HEADER = HttpHeaderName.fromString("x-ms-client-request-id");
    private static final RetryPolicy RETRY_POLICY = new RetryPolicy(new FixedDelay(0, Duration.ofMillis(1)));

    @Test
    public void searchClient() {
        String expectedRequestId = CoreUtils.randomUuid().toString();

        SearchClient client = new SearchClientBuilder().indexName("index")
            .endpoint("https://test.search.windows.net")
            .credential(new MockTokenCredential())
            .retryPolicy(RETRY_POLICY)
            .httpClient(new RequestIdVerifyingHttpClient(expectedRequestId))
            .buildClient();

        Context context = new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY,
            createRequestIdHeaders(expectedRequestId));

        verifySync(() -> client.getDocumentCountWithResponse(context), expectedRequestId);
    }

    @Test
    public void searchAsyncClient() {
        String expectedRequestId = CoreUtils.randomUuid().toString();

        SearchAsyncClient client = new SearchClientBuilder().indexName("index")
            .endpoint("https://test.search.windows.net")
            .credential(new MockTokenCredential())
            .retryPolicy(RETRY_POLICY)
            .httpClient(new RequestIdVerifyingHttpClient(expectedRequestId))
            .buildAsyncClient();

        reactor.util.context.Context subscriberContext = reactor.util.context.Context
            .of(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, createRequestIdHeaders(expectedRequestId));

        verifyAsync(client.getDocumentCountWithResponse().contextWrite(subscriberContext), expectedRequestId);
    }

    @Test
    public void searchIndexClient() {
        String expectedRequestId = CoreUtils.randomUuid().toString();

        SearchIndexClient client = new SearchIndexClientBuilder().endpoint("https://test.search.windows.net")
            .credential(new MockTokenCredential())
            .retryPolicy(RETRY_POLICY)
            .httpClient(new RequestIdVerifyingHttpClient(expectedRequestId))
            .buildClient();

        Context context = new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY,
            createRequestIdHeaders(expectedRequestId));

        verifySync(() -> client.getIndexWithResponse("index", context), expectedRequestId);
    }

    @Test
    public void searchIndexAsyncClient() {
        String expectedRequestId = CoreUtils.randomUuid().toString();

        SearchIndexAsyncClient client = new SearchIndexClientBuilder().endpoint("https://test.search.windows.net")
            .credential(new MockTokenCredential())
            .retryPolicy(RETRY_POLICY)
            .httpClient(new RequestIdVerifyingHttpClient(expectedRequestId))
            .buildAsyncClient();

        reactor.util.context.Context subscriberContext = reactor.util.context.Context
            .of(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, createRequestIdHeaders(expectedRequestId));

        verifyAsync(client.getIndexStatisticsWithResponse("index").contextWrite(subscriberContext), expectedRequestId);
    }

    @Test
    public void searchIndexerClient() {
        String expectedRequestId = CoreUtils.randomUuid().toString();

        SearchIndexerClient client = new SearchIndexerClientBuilder().endpoint("https://test.search.windows.net")
            .credential(new MockTokenCredential())
            .retryPolicy(RETRY_POLICY)
            .httpClient(new RequestIdVerifyingHttpClient(expectedRequestId))
            .buildClient();

        Context context = new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY,
            createRequestIdHeaders(expectedRequestId));

        verifySync(() -> client.getIndexerWithResponse("indexer", context), expectedRequestId);
    }

    @Test
    public void searchIndexerAsyncClient() {
        String expectedRequestId = CoreUtils.randomUuid().toString();

        SearchIndexerAsyncClient client = new SearchIndexerClientBuilder().endpoint("https://test.search.windows.net")
            .credential(new MockTokenCredential())
            .retryPolicy(RETRY_POLICY)
            .httpClient(new RequestIdVerifyingHttpClient(expectedRequestId))
            .buildAsyncClient();

        reactor.util.context.Context subscriberContext = reactor.util.context.Context
            .of(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, createRequestIdHeaders(expectedRequestId));

        verifyAsync(client.getIndexerWithResponse("indexer").contextWrite(subscriberContext), expectedRequestId);
    }

    private static HttpHeaders createRequestIdHeaders(String requestId) {
        return new HttpHeaders().set(REQUEST_ID_HEADER, requestId);
    }

    private static void verifySync(Runnable requestRunner, String expectedRequestId) {
        RuntimeException ex = assertThrows(RuntimeException.class, requestRunner::run);
        assertEquals(expectedRequestId, ex.getMessage());
    }

    private static void verifyAsync(Mono<?> requestMono, String expectedRequestId) {
        StepVerifier.create(requestMono).verifyErrorSatisfies(throwable -> {
            RuntimeException ex = assertInstanceOf(RuntimeException.class, throwable);
            assertEquals(expectedRequestId, ex.getMessage());
        });
    }

    private static final class RequestIdVerifyingHttpClient implements HttpClient {
        private final String expectedRequestId;

        private RequestIdVerifyingHttpClient(String expectedRequestId) {
            this.expectedRequestId = expectedRequestId;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            String requestId = request.getHeaders().getValue(REQUEST_ID_HEADER);
            assertEquals(expectedRequestId, requestId);

            return Mono.error(new RuntimeException(requestId));
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request, Context context) {
            String requestId = request.getHeaders().getValue(REQUEST_ID_HEADER);
            assertEquals(expectedRequestId, requestId);

            return Mono.error(new RuntimeException(requestId));
        }

        @Override
        public HttpResponse sendSync(HttpRequest request, Context context) {
            String requestId = request.getHeaders().getValue(REQUEST_ID_HEADER);
            assertEquals(expectedRequestId, requestId);

            throw new RuntimeException(requestId);
        }
    }
}
