// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.http.rest;

import com.typespec.core.annotation.BodyParam;
import com.typespec.core.annotation.ExpectedResponses;
import com.typespec.core.annotation.Get;
import com.typespec.core.annotation.HeaderParam;
import com.typespec.core.annotation.Host;
import com.typespec.core.annotation.Post;
import com.typespec.core.annotation.ServiceInterface;
import com.typespec.core.http.HttpClient;
import com.typespec.core.http.HttpHeaderName;
import com.typespec.core.http.HttpMethod;
import com.typespec.core.http.HttpPipeline;
import com.typespec.core.http.HttpPipelineBuilder;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.http.MockHttpResponse;
import com.typespec.core.http.rest.RequestOptions;
import com.typespec.core.http.rest.Response;
import com.typespec.core.http.rest.RestProxy;
import com.typespec.core.http.rest.StreamResponse;
import com.typespec.core.util.BinaryData;
import com.typespec.core.util.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link RestProxy}.
 */
public class SyncRestProxyTests {
    @Host("https://azure.com")
    @ServiceInterface(name = "myService")
    interface TestInterface {
        @Post("my/url/path")
        @ExpectedResponses({200})
        Response<Void> testMethod(
            @BodyParam("application/octet-stream") BinaryData data,
            @HeaderParam("Content-Type") String contentType,
            @HeaderParam("Content-Length") Long contentLength,
            Context context
        );

        @Get("my/url/path")
        @ExpectedResponses({200})
        StreamResponse testDownload(Context context);

        @Get("my/url/path")
        @ExpectedResponses({200})
        void testVoidMethod(Context context);
    }

    @Test
    public void voidReturningApiClosesResponse() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        testInterface.testVoidMethod(Context.NONE);

        assertTrue(client.lastResponseClosed);
    }

    @Test
    public void contextFlagDisablesSyncStack() {
        AtomicBoolean asyncMethodCalled = new AtomicBoolean(false);
        HttpClient client = new HttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                asyncMethodCalled.set(true);
                return Mono.just(new MockHttpResponse(request, 200));
            }

            @Override
            public HttpResponse sendSync(HttpRequest request, Context context) {
                throw new IllegalStateException("Sync send API was Invoked.");
            }
        };

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        testInterface.testVoidMethod(new Context("com.azure.core.http.restproxy.syncproxy.enable", false));
        assertTrue(asyncMethodCalled.get());
    }

    @Test
    public void contentTypeHeaderPriorityOverBodyParamAnnotationTest() {
        HttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        byte[] bytes = "hello".getBytes();
        Response<Void> response = testInterface.testMethod(BinaryData.fromStream(
            new ByteArrayInputStream(bytes), (long) bytes.length), "application/json", (long) bytes.length,
            Context.NONE);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void streamResponseShouldHaveHttpResponseReference() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        StreamResponse streamResponse = testInterface.testDownload(Context.NONE);
        streamResponse.close();
        // This indirectly tests that StreamResponse has HttpResponse reference
        assertTrue(client.lastResponseClosed);
    }

    private static final class LocalHttpClient implements HttpClient {

        private volatile boolean lastResponseClosed;

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.error(new IllegalStateException("Async Send API was Invoked."));
        }

        @Override
        public HttpResponse sendSync(HttpRequest request, Context context) {
            boolean success = request.getUrl().getPath().equals("/my/url/path");
            if (request.getHttpMethod().equals(HttpMethod.POST)) {
                success &= "application/json".equals(request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
            } else {
                success &= request.getHttpMethod().equals(HttpMethod.GET);
            }

            return new MockHttpResponse(request, success ? 200 : 400) {
                @Override
                public void close() {
                    lastResponseClosed = true;
                    super.close();
                }
            };
        }
    }

    @ParameterizedTest
    @MethodSource("mergeRequestOptionsContextSupplier")
    public void mergeRequestOptionsContext(Context context, RequestOptions options,
                                           Map<Object, Object> expectedContextValues) {
        Map<Object, Object> actualContextValues = RestProxyUtils.mergeRequestOptionsContext(context, options).getValues();

        assertEquals(expectedContextValues.size(), actualContextValues.size());
        for (Map.Entry<Object, Object> expectedKvp : expectedContextValues.entrySet()) {
            assertTrue(actualContextValues.containsKey(expectedKvp.getKey()), () ->
                "Missing expected key '" + expectedKvp.getKey() + "'.");
            assertEquals(expectedKvp.getValue(), actualContextValues.get(expectedKvp.getKey()));
        }
    }

    private static Stream<Arguments> mergeRequestOptionsContextSupplier() {
        Map<Object, Object> twoValuesMap = new HashMap<>();
        twoValuesMap.put("key", "value");
        twoValuesMap.put("key2", "value2");

        return Stream.of(
            // Cases where the RequestOptions or it's Context doesn't exist.
            Arguments.of(Context.NONE, null, Collections.emptyMap()),
            Arguments.of(Context.NONE, new RequestOptions(), Collections.emptyMap()),
            Arguments.of(Context.NONE, new RequestOptions().setContext(Context.NONE), Collections.emptyMap()),

            // Case where the RequestOptions Context is merged into an empty Context.
            Arguments.of(Context.NONE, new RequestOptions().setContext(new Context("key", "value")),
                Collections.singletonMap("key", "value")),

            // Case where the RequestOptions Context is merged, without replacement, into an existing Context.
            Arguments.of(new Context("key", "value"), new RequestOptions().setContext(new Context("key2", "value2")),
                twoValuesMap),

            // Case where the RequestOptions Context is merged and overrides an existing Context.
            Arguments.of(new Context("key", "value"), new RequestOptions().setContext(new Context("key", "value2")),
                Collections.singletonMap("key", "value2"))
        );
    }
}
