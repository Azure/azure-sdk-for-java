// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.search.documents.models.GetDocumentOptions;
import com.azure.search.documents.models.LookupDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetDocumentOptionsTests {
    private static final HttpHeaderName AUTHORIZATION = HttpHeaderName.fromString("x-ms-query-source-authorization");
    private static final HttpHeaderName ELEVATED_READ = HttpHeaderName.fromString("x-ms-enable-elevated-read");
    private static final String DOCUMENT = "{\"id\":\"hotel-1\",\"title\":\"Hotel\"}";

    @Test
    public void defaultsAndFluentAccessors() {
        GetDocumentOptions options = new GetDocumentOptions();
        assertNull(options.getQuerySourceAuthorization());
        assertNull(options.isEnableElevatedRead());
        assertNull(options.getSelectedFields());
        assertSame(options, options.setQuerySourceAuthorization("query-token"));
        assertSame(options, options.setEnableElevatedRead(false));
        assertSame(options, options.setSelectedFields(Arrays.asList("id", "title")));
        assertEquals("query-token", options.getQuerySourceAuthorization());
        assertEquals(Boolean.FALSE, options.isEnableElevatedRead());
        assertEquals(Arrays.asList("id", "title"), options.getSelectedFields());
        options.setQuerySourceAuthorization(null).setEnableElevatedRead(null).setSelectedFields(null);
        assertNull(options.getQuerySourceAuthorization());
        assertNull(options.isEnableElevatedRead());
        assertNull(options.getSelectedFields());
    }

    @ParameterizedTest
    @MethodSource("lookupOptions")
    public void bothClientsForwardLookupOptions(GetDocumentOptions options, String authorization, String elevatedRead,
        String selection) {
        AtomicInteger requests = new AtomicInteger();
        SearchClientBuilder builder = builder(request -> {
            assertEquals(HttpMethod.GET, request.getHttpMethod());
            assertEquals("/indexes('index')/docs('hotel-1')", request.getUrl().getPath());
            assertEquals(authorization, request.getHeaders().getValue(AUTHORIZATION));
            assertEquals(elevatedRead, request.getHeaders().getValue(ELEVATED_READ));
            assertEquals("api-version=2026-10-01" + (selection == null ? "" : "&$select=" + selection), query(request));
            BinaryData body = request.getBodyAsBinaryData();
            assertTrue(body == null || Long.valueOf(0).equals(body.getLength()));
            assertFalse(request.getUrl().toString().contains("query-token"));
            requests.incrementAndGet();
        });
        verifyDocument(false, builder, options, null);
        verifyDocument(true, builder, options, null);
        assertEquals(2, requests.get());
    }

    private static Stream<Arguments> lookupOptions() {
        return Stream.of(Arguments.of(null, null, null, null), Arguments.of(new GetDocumentOptions(), null, null, null),
            Arguments
                .of(new GetDocumentOptions().setQuerySourceAuthorization("query-token"), "query-token", null, null),
            Arguments.of(new GetDocumentOptions().setEnableElevatedRead(true), null, "true", null),
            Arguments.of(new GetDocumentOptions().setQuerySourceAuthorization("query-token")
                .setEnableElevatedRead(false)
                .setSelectedFields(Arrays.asList("id", "title")), "query-token", "false", "id,title"),
            Arguments.of(new GetDocumentOptions().setSelectedFields(Collections.emptyList()), null, null, ""),
            Arguments.of(new GetDocumentOptions().setSelectedFields(Arrays.asList("id", null, "title")), null, null,
                "id,,title"));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void selectionIsEncodedOnce(boolean async) {
        SearchClientBuilder builder = builder(request -> {
            assertEquals("api-version=2026-10-01&$select=parent/child,a b,a+b,a&b,a%b", query(request));
            assertEquals(2, request.getUrl().getQuery().split("&").length);
            assertTrue(request.getUrl().getQuery().contains("%2B"));
            assertTrue(request.getUrl().getQuery().contains("%26"));
            assertTrue(request.getUrl().getQuery().contains("%25"));
        });
        GetDocumentOptions options
            = new GetDocumentOptions().setSelectedFields(Arrays.asList("parent/child", "a b", "a+b", "a&b", "a%b"));
        verifyDocument(async, builder, options, null);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void requestOptionsRetainCallbacksContextAndHeaderOverrides(boolean async) {
        AtomicInteger callbacks = new AtomicInteger();
        Context context = new Context("lookup-test", "request-context");
        RequestOptions requestOptions = new RequestOptions().setContext(context)
            .setHeader(AUTHORIZATION, "request-token")
            .setHeader(ELEVATED_READ, "false")
            .addQueryParam("test", "a b")
            .addRequestCallback(request -> {
                callbacks.incrementAndGet();
                request.setHeader("test-header", "callback");
            });
        SearchClientBuilder builder = builder(request -> {
            assertEquals("request-token", request.getHeaders().getValue(AUTHORIZATION));
            assertEquals("false", request.getHeaders().getValue(ELEVATED_READ));
            assertEquals("callback", request.getHeaders().getValue("test-header"));
            assertTrue(query(request).endsWith("&test=a b"));
        }).addPolicy((pipelineContext, next) -> {
            assertEquals("request-context", pipelineContext.getContext().getData("lookup-test").orElse(null));
            return next.process();
        });
        verifyDocument(async, builder,
            new GetDocumentOptions().setQuerySourceAuthorization("option-token")
                .setEnableElevatedRead(true)
                .setSelectedFields(Collections.singletonList("id")),
            requestOptions);
        verifyDocument(async, builder, null, requestOptions);
        assertEquals(2, callbacks.get());
        assertSame(context, requestOptions.getContext());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void operationOptionsDoNotModifyReusedRequestOptions(boolean async) {
        AtomicInteger callbacks = new AtomicInteger();
        RequestOptions requestOptions = new RequestOptions().addRequestCallback(request -> callbacks.incrementAndGet());
        for (String token : Arrays.asList("first-token", "second-token", null)) {
            SearchClientBuilder builder = builder(request -> {
                assertEquals(token, request.getHeaders().getValue(AUTHORIZATION));
                assertEquals(token == null ? null : "false", request.getHeaders().getValue(ELEVATED_READ));
                assertEquals(token == null ? "api-version=2026-10-01" : "api-version=2026-10-01&$select=id",
                    query(request));
            });
            GetDocumentOptions options = token == null
                ? null
                : new GetDocumentOptions().setQuerySourceAuthorization(token)
                    .setEnableElevatedRead(false)
                    .setSelectedFields(Collections.singletonList("id"));
            verifyDocument(async, builder, options, requestOptions);
        }
        assertEquals(3, callbacks.get());
    }

    @Test
    public void asyncLookupRetainsSubscriberContext() {
        SearchAsyncClient client = builder(request -> {
        }).addPolicy((pipelineContext, next) -> {
            assertEquals("subscriber-context", pipelineContext.getContext().getData("subscriber-test").orElse(null));
            assertEquals("request-context", pipelineContext.getContext().getData("lookup-test").orElse(null));
            return next.process();
        }).buildAsyncClient();
        StepVerifier
            .create(client
                .getDocumentWithResponse("hotel-1", new GetDocumentOptions(),
                    new RequestOptions().setContext(new Context("lookup-test", "request-context")))
                .contextWrite(reactor.util.context.Context.of("subscriber-test", "subscriber-context")))
            .assertNext(GetDocumentOptionsTests::assertDocument)
            .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void lookupPreservesNotFoundErrors(boolean async) {
        SearchClientBuilder builder = builder(request -> {
        }).httpClient(request -> Mono.just(new MockHttpResponse(request, 404,
            new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
            "{\"error\":{\"code\":\"NotFound\",\"message\":\"Missing document\"}}".getBytes(StandardCharsets.UTF_8))));
        if (async) {
            StepVerifier
                .create(builder.buildAsyncClient()
                    .getDocumentWithResponse("hotel-1",
                        new GetDocumentOptions().setQuerySourceAuthorization("query-token"), new RequestOptions()))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof ResourceNotFoundException);
                    assertEquals(404, ((ResourceNotFoundException) error).getResponse().getStatusCode());
                })
                .verify();
        } else {
            ResourceNotFoundException error = assertThrows(ResourceNotFoundException.class,
                () -> builder.buildClient()
                    .getDocumentWithResponse("hotel-1",
                        new GetDocumentOptions().setQuerySourceAuthorization("query-token"), new RequestOptions()));
            assertEquals(404, error.getResponse().getStatusCode());
        }
    }

    @Test
    public void publishedNullAndSelectedFieldOverloadsRemainCompatible() {
        AtomicInteger requests = new AtomicInteger();
        SearchClientBuilder builder = builder(request -> {
            assertNull(request.getHeaders().getValue(AUTHORIZATION));
            assertNull(request.getHeaders().getValue(ELEVATED_READ));
            requests.incrementAndGet();
        });
        SearchClient client = builder.buildClient();
        SearchAsyncClient asyncClient = builder.buildAsyncClient();
        assertEquals("hotel-1", client.getDocument("hotel-1").getAdditionalProperties().get("id"));
        assertEquals("hotel-1", client.getDocument("hotel-1", null).getAdditionalProperties().get("id"));
        assertEquals("hotel-1",
            client.getDocument("hotel-1", Collections.singletonList("id")).getAdditionalProperties().get("id"));
        assertEquals("hotel-1", client.getDocument("hotel-1", null, null, null).getAdditionalProperties().get("id"));
        assertDocument(client.getDocumentWithResponse("hotel-1", null));
        StepVerifier.create(asyncClient.getDocument("hotel-1"))
            .expectNextMatches(document -> "hotel-1".equals(document.getAdditionalProperties().get("id")))
            .verifyComplete();
        StepVerifier.create(asyncClient.getDocument("hotel-1", null))
            .expectNextMatches(document -> "hotel-1".equals(document.getAdditionalProperties().get("id")))
            .verifyComplete();
        StepVerifier.create(asyncClient.getDocument("hotel-1", Collections.singletonList("id")))
            .expectNextMatches(document -> "hotel-1".equals(document.getAdditionalProperties().get("id")))
            .verifyComplete();
        StepVerifier.create(asyncClient.getDocument("hotel-1", null, null, null))
            .expectNextMatches(document -> "hotel-1".equals(document.getAdditionalProperties().get("id")))
            .verifyComplete();
        StepVerifier.create(asyncClient.getDocumentWithResponse("hotel-1", null))
            .assertNext(GetDocumentOptionsTests::assertDocument)
            .verifyComplete();
        assertEquals(10, requests.get());
    }

    private static SearchClientBuilder builder(Consumer<HttpRequest> verifyRequest) {
        return new SearchClientBuilder().endpoint("https://example.search.windows.net")
            .credential(new AzureKeyCredential("key"))
            .indexName("index")
            .httpClient(request -> {
                verifyRequest.accept(request);
                return Mono.just(new MockHttpResponse(request, 200,
                    new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
                    DOCUMENT.getBytes(StandardCharsets.UTF_8)));
            });
    }

    private static String query(HttpRequest request) {
        return URI.create(request.getUrl().toString()).getQuery();
    }

    private static void verifyDocument(boolean async, SearchClientBuilder builder, GetDocumentOptions options,
        RequestOptions requestOptions) {
        if (async) {
            StepVerifier.create(builder.buildAsyncClient().getDocumentWithResponse("hotel-1", options, requestOptions))
                .assertNext(GetDocumentOptionsTests::assertDocument)
                .verifyComplete();
        } else {
            assertDocument(builder.buildClient().getDocumentWithResponse("hotel-1", options, requestOptions));
        }
    }

    private static void assertDocument(Response<LookupDocument> response) {
        assertEquals(200, response.getStatusCode());
        assertEquals("hotel-1", response.getValue().getAdditionalProperties().get("id"));
    }
}
