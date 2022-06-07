// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests passing {@code x-ms-client-request-id} using {@link Context}.
 */
public class ContextRequestIdTests extends SearchTestBase {
    private static final String REQUEST_ID_HEADER = "x-ms-client-request-id";
    private static final Supplier<RetryPolicy> RETRY_POLICY_SUPPLIER = () ->
        new RetryPolicy(new FixedDelay(1, Duration.ofMillis(100)));

    @Test
    public void searchClient() {
        SearchClient client = getSearchClientBuilder("index")
            .retryPolicy(RETRY_POLICY_SUPPLIER.get())
            .buildClient();

        String expectedRequestId = testResourceNamer.randomUuid();
        HttpHeaders headers = createRequestIdHeaders(expectedRequestId);

        Context context = new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers);

        verifySync(() -> client.getDocumentCountWithResponse(context), expectedRequestId);
    }

    @Test
    public void searchAsyncClient() {
        SearchAsyncClient client = getSearchClientBuilder("index")
            .retryPolicy(RETRY_POLICY_SUPPLIER.get())
            .buildAsyncClient();

        String expectedRequestId = testResourceNamer.randomUuid();
        HttpHeaders headers = createRequestIdHeaders(expectedRequestId);

        reactor.util.context.Context subscriberContext = reactor.util.context.Context
            .of(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers);

        Mono<String> request = client.getDocumentCountWithResponse()
            .subscriberContext(subscriberContext)
            .map(ContextRequestIdTests::extractFromResponse)
            .onErrorResume(HttpResponseException.class, ContextRequestIdTests::extractFromHttpRequestException)
            .onErrorResume(RuntimeException.class, ContextRequestIdTests::extractFromRuntimeException);

        verifyAsync(request, expectedRequestId);
    }

    @Test
    public void searchIndexClient() {
        SearchIndexClient client = getSearchIndexClientBuilder()
            .retryPolicy(RETRY_POLICY_SUPPLIER.get())
            .buildClient();

        String expectedRequestId = testResourceNamer.randomUuid();
        HttpHeaders headers = createRequestIdHeaders(expectedRequestId);

        Context context = new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers);

        verifySync(() -> client.getIndexWithResponse("index", context), expectedRequestId);
    }

    @Test
    public void searchIndexAsyncClient() {
        SearchIndexAsyncClient client = getSearchIndexClientBuilder()
            .retryPolicy(RETRY_POLICY_SUPPLIER.get())
            .buildAsyncClient();

        String expectedRequestId = testResourceNamer.randomUuid();
        HttpHeaders headers = createRequestIdHeaders(expectedRequestId);

        reactor.util.context.Context subscriberContext = reactor.util.context.Context
            .of(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers);

        Mono<String> request = client.getIndexStatisticsWithResponse("index")
            .subscriberContext(subscriberContext)
            .map(ContextRequestIdTests::extractFromResponse)
            .onErrorResume(HttpResponseException.class, ContextRequestIdTests::extractFromHttpRequestException)
            .onErrorResume(RuntimeException.class, ContextRequestIdTests::extractFromRuntimeException);

        verifyAsync(request, expectedRequestId);
    }

    @Test
    public void searchIndexerClient() {
        SearchIndexerClient client = getSearchIndexerClientBuilder()
            .retryPolicy(RETRY_POLICY_SUPPLIER.get())
            .buildClient();

        String expectedRequestId = testResourceNamer.randomUuid();
        HttpHeaders headers = createRequestIdHeaders(expectedRequestId);

        Context context = new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers);

        verifySync(() -> client.getIndexerWithResponse("indexer", context), expectedRequestId);
    }

    @Test
    public void searchIndexerAsyncClient() {
        SearchIndexerAsyncClient client = getSearchIndexerClientBuilder()
            .retryPolicy(RETRY_POLICY_SUPPLIER.get())
            .buildAsyncClient();

        String expectedRequestId = testResourceNamer.randomUuid();
        HttpHeaders headers = createRequestIdHeaders(expectedRequestId);

        reactor.util.context.Context subscriberContext = reactor.util.context.Context
            .of(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers);

        Mono<String> request = client.getIndexerWithResponse("indexer")
            .subscriberContext(subscriberContext)
            .map(ContextRequestIdTests::extractFromResponse)
            .onErrorResume(HttpResponseException.class, ContextRequestIdTests::extractFromHttpRequestException)
            .onErrorResume(RuntimeException.class, ContextRequestIdTests::extractFromRuntimeException);

        verifyAsync(request, expectedRequestId);
    }

    private static HttpHeaders createRequestIdHeaders(String requestId) {
        return new HttpHeaders().put(REQUEST_ID_HEADER, requestId);
    }

    private static void verifySync(Supplier<Response<?>> requestRunner, String expectedRequestId) {
        // Doesn't matter if the request was successful or not, it will always return response headers.
        try {
            Response<?> response = requestRunner.get();
            assertEquals(expectedRequestId, response.getHeaders().getValue(REQUEST_ID_HEADER));
        } catch (HttpResponseException ex) {
            assertEquals(expectedRequestId, ex.getResponse().getHeaderValue(REQUEST_ID_HEADER));
        } catch (RuntimeException ex) {
            assertTrue(ex.getCause() instanceof HttpResponseException);

            assertEquals(expectedRequestId, ((HttpResponseException) ex.getCause())
                .getResponse().getHeaderValue(REQUEST_ID_HEADER));
        } catch (Throwable throwable) {
            fail("Unexpected exception type.");
        }
    }

    private static String extractFromResponse(Response<?> response) {
        return response.getHeaders().getValue(REQUEST_ID_HEADER);
    }

    private static Mono<String> extractFromHttpRequestException(HttpResponseException exception) {
        return Mono.just(exception.getResponse().getHeaderValue(REQUEST_ID_HEADER));
    }

    private static Mono<String> extractFromRuntimeException(RuntimeException exception) {
        assertTrue(exception.getCause() instanceof HttpResponseException);

        return extractFromHttpRequestException((HttpResponseException) exception.getCause());
    }

    private static void verifyAsync(Mono<String> requestIdMono, String expectedRequestId) {
        StepVerifier.create(requestIdMono)
            .assertNext(actual -> assertEquals(expectedRequestId, actual))
            .verifyComplete();
    }
}
