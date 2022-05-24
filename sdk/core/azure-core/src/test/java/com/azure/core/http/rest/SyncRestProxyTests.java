// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
/**
 * Tests {@link RestProxy}.
 */
public class SyncRestProxyTests {
    private static final byte[] EXPECTED = new byte[]{0, 1, 2, 3, 4};

    @Test
    public void emptyRequestBody() {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost");

        try {
            SyncRestProxy.validateLengthSync(httpRequest);
        } catch (Exception e) {
            fail("The test Should not have thrown any exception.");
        }
    }

    @Test
    public void expectedBodyLength() {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
            .setBody(EXPECTED)
            .setHeader("Content-Length", "5");

        assertArrayEquals(EXPECTED, collectRequest(httpRequest));
    }

    @Test
    public void unexpectedBodyLength() {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
            .setBody(EXPECTED);

        UnexpectedLengthException unexpectedLengthException =  assertThrows(UnexpectedLengthException.class,
            () -> collectRequest(httpRequest.setHeader("Content-Length", "4")));
        assertEquals("Request body emitted 5 bytes, more than the expected 4 bytes.",
            unexpectedLengthException.getMessage());

        unexpectedLengthException =  assertThrows(UnexpectedLengthException.class,
            () -> collectRequest(httpRequest.setHeader("Content-Length", "6")));
        assertEquals("Request body emitted 5 bytes, less than the expected 6 bytes.",
            unexpectedLengthException.getMessage());
    }

    @Host("https://azure.com")
    @ServiceInterface(name = "myService")
    interface TestInterface {
        @Post("my/url/path")
        @ExpectedResponses({200})
        Response<Void> testMethod(
            @BodyParam("application/octet-stream") InputStream request,
            @HeaderParam("Content-Type") String contentType,
            @HeaderParam("Content-Length") Long contentLength
        );
    }

    @Test
    public void contentTypeHeaderPriorityOverBodyParamAnnotationTest() {
        HttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = SyncRestProxy.create(TestInterface.class, pipeline);
        byte[] bytes = "hello".getBytes();
        Response<Void> response = testInterface.testMethod(new ByteArrayInputStream(bytes),
            "application/json", (long) bytes.length);

        assertEquals(200, response.getStatusCode());
    }

    private static final class LocalHttpClient implements HttpClient {

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            boolean success = request.getHeaders()
                .stream()
                .filter(header -> header.getName().equals("Content-Type"))
                .map(header -> header.getValue())
                .anyMatch(contentType -> contentType.equals("application/json"));
            int statusCode = success ? 200 : 400;
            return Mono.just(new MockHttpResponse(request, statusCode));
        }
    }

    private static byte[] collectRequest(HttpRequest request) {
        return SyncRestProxy.validateLengthSync(request).toBytes();
    }

    @ParameterizedTest
    @MethodSource("mergeRequestOptionsContextSupplier")
    public void mergeRequestOptionsContext(Context context, RequestOptions options,
        Map<Object, Object> expectedContextValues) {
        Map<Object, Object> actualContextValues = SyncRestProxy.mergeRequestOptionsContext(context, options).getValues();

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
