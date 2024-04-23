// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexerAsyncClient;
import com.azure.search.documents.indexes.SearchIndexerClient;
import org.junit.jupiter.api.Test;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests passing {@code x-ms-client-request-id} using {@link Context}.
 */
public class ContextRequestIdTests extends SearchTestBase {
    private static final HttpHeaderName REQUEST_ID_HEADER = HttpHeaderName.fromString("x-ms-client-request-id");
    private static final RetryPolicy RETRY_POLICY = new RetryPolicy(new FixedDelay(1, Duration.ofMillis(1)));

    @Test
    public void searchClient() {
        SearchClient client = getSearchClientBuilder("index", true)
            .retryPolicy(RETRY_POLICY)
            .buildClient();

        String expectedRequestId = testResourceNamer.randomUuid();
        HttpHeaders headers = createRequestIdHeaders(expectedRequestId);

        Context context = new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers);

        verifySync(() -> client.getDocumentCountWithResponse(context), expectedRequestId);
    }

    @Test
    public void searchAsyncClient() {
        SearchAsyncClient client = getSearchClientBuilder("index", false)
            .retryPolicy(RETRY_POLICY)
            .buildAsyncClient();

        String expectedRequestId = testResourceNamer.randomUuid();
        HttpHeaders headers = createRequestIdHeaders(expectedRequestId);

        reactor.util.context.Context subscriberContext = reactor.util.context.Context
            .of(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers);

        Mono<String> request = client.getDocumentCountWithResponse()
            .contextWrite(subscriberContext)
            .map(ContextRequestIdTests::extractFromResponse)
            .onErrorResume(ContextRequestIdTests::onErrorResume);

        verifyAsync(request, expectedRequestId);
    }

    @Test
    public void searchIndexClient() {
        SearchIndexClient client = getSearchIndexClientBuilder(true)
            .retryPolicy(RETRY_POLICY)
            .buildClient();

        String expectedRequestId = testResourceNamer.randomUuid();
        HttpHeaders headers = createRequestIdHeaders(expectedRequestId);

        Context context = new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers);

        verifySync(() -> client.getIndexWithResponse("index", context), expectedRequestId);
    }

    @Test
    public void searchIndexAsyncClient() {
        SearchIndexAsyncClient client = getSearchIndexClientBuilder(false)
            .retryPolicy(RETRY_POLICY)
            .buildAsyncClient();

        String expectedRequestId = testResourceNamer.randomUuid();
        HttpHeaders headers = createRequestIdHeaders(expectedRequestId);

        reactor.util.context.Context subscriberContext = reactor.util.context.Context
            .of(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers);

        Mono<String> request = client.getIndexStatisticsWithResponse("index")
            .contextWrite(subscriberContext)
            .map(ContextRequestIdTests::extractFromResponse)
            .onErrorResume(ContextRequestIdTests::onErrorResume);

        verifyAsync(request, expectedRequestId);
    }

    @Test
    public void searchIndexerClient() {
        SearchIndexerClient client = getSearchIndexerClientBuilder(true)
            .retryPolicy(RETRY_POLICY)
            .buildClient();

        String expectedRequestId = testResourceNamer.randomUuid();
        HttpHeaders headers = createRequestIdHeaders(expectedRequestId);

        Context context = new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers);

        verifySync(() -> client.getIndexerWithResponse("indexer", context), expectedRequestId);
    }

    @Test
    public void searchIndexerAsyncClient() {
        SearchIndexerAsyncClient client = getSearchIndexerClientBuilder(false)
            .retryPolicy(RETRY_POLICY)
            .buildAsyncClient();

        String expectedRequestId = testResourceNamer.randomUuid();
        HttpHeaders headers = createRequestIdHeaders(expectedRequestId);

        reactor.util.context.Context subscriberContext = reactor.util.context.Context
            .of(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers);

        Mono<String> request = client.getIndexerWithResponse("indexer")
            .contextWrite(subscriberContext)
            .map(ContextRequestIdTests::extractFromResponse)
            .onErrorResume(ContextRequestIdTests::onErrorResume);

        verifyAsync(request, expectedRequestId);
    }

    private static HttpHeaders createRequestIdHeaders(String requestId) {
        return new HttpHeaders().set(REQUEST_ID_HEADER, requestId);
    }

    private static void verifySync(Supplier<Response<?>> requestRunner, String expectedRequestId) {
        // Doesn't matter if the request was successful or not, it will always return response headers.
        try {
            assertEquals(expectedRequestId, extractFromResponse(requestRunner.get()));
        } catch (Throwable throwable) {
            String errorRequestId = extractFromThrowable(throwable);

            assertNotNull(errorRequestId, "Either unexpected exception type or missing x-ms-client-request-id header. "
                + "Exception type: " + throwable.getClass() + ", header value: " + errorRequestId);
            assertEquals(expectedRequestId, errorRequestId);
        }
    }

    private static String extractFromResponse(Response<?> response) {
        return response.getHeaders().getValue(REQUEST_ID_HEADER);
    }

    private static String extractFromThrowable(Throwable throwable) {
        throwable = Exceptions.unwrap(throwable);
        if (throwable instanceof HttpResponseException) {
            return ((HttpResponseException) throwable).getResponse().getHeaderValue(REQUEST_ID_HEADER);
        } else if (throwable instanceof RuntimeException) {
            Throwable cause = throwable.getCause();
            if (cause instanceof HttpResponseException) {
                return ((HttpResponseException) cause).getResponse().getHeaderValue(REQUEST_ID_HEADER);
            }

            return null;
        } else {
            return null;
        }
    }

    private static Mono<String> onErrorResume(Throwable throwable) {
        String errorRequestId = extractFromThrowable(throwable);

        return (errorRequestId != null) ? Mono.just(errorRequestId) : Mono.error(throwable);
    }

    private static void verifyAsync(Mono<String> requestIdMono, String expectedRequestId) {
        StepVerifier.create(requestIdMono)
            .assertNext(actual -> assertEquals(expectedRequestId, actual))
            .verifyComplete();
    }
}
