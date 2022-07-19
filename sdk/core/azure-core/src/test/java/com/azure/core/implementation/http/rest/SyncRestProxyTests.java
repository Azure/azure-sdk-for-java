// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.http.rest.StreamResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.Header;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link RestProxy}.
 */
public class SyncRestProxyTests {
    private static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLE = "com.azure.core.http.restproxy.syncproxy.enable";


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
    }

    @Test
    public void contentTypeHeaderPriorityOverBodyParamAnnotationTest() {
        HttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        byte[] bytes = "hello".getBytes();
        Context context =  new Context(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true);
        Response<Void> response = testInterface.testMethod(BinaryData.fromStream(new ByteArrayInputStream(bytes)),
            "application/json", (long) bytes.length, context);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void streamResponseShouldHaveHttpResponseReference() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        Context context =  new Context(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true);
        StreamResponse streamResponse = testInterface.testDownload(context);
        streamResponse.close();
        // This indirectly tests that StreamResponse has HttpResponse reference
        Mockito.verify(client.getLastResponseSpy()).close();
    }

    private static final class LocalHttpClient implements HttpClient {

        private volatile HttpResponse lastResponseSpy;

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.error(new IllegalStateException("Async Send API was Invoked."));
        }

        @Override
        public HttpResponse sendSync(HttpRequest request, Context context) {
            boolean success = request.getUrl().getPath().equals("/my/url/path");
            if (request.getHttpMethod().equals(HttpMethod.POST)) {
                success = success && request.getHeaders()
                    .stream()
                    .filter(header -> header.getName().equals("Content-Type"))
                    .map(Header::getValue)
                    .anyMatch(contentType -> contentType.equals("application/json"));
            } else {
                success = success && request.getHttpMethod().equals(HttpMethod.GET);
            }
            int statusCode = success ? 200 : 400;
            MockHttpResponse response = Mockito.spy(new MockHttpResponse(request, statusCode));
            lastResponseSpy = response;
            return response;
        }

        public HttpResponse getLastResponseSpy() {
            return lastResponseSpy;
        }
    }

    private static byte[] collectRequest(HttpRequest request) {
        return RestProxyUtils.validateLengthSync(request).toBytes();
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
