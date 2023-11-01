// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http;

import com.generic.core.annotation.BodyParam;
import com.generic.core.annotation.ExpectedResponses;
import com.generic.core.annotation.Get;
import com.generic.core.annotation.HeaderParam;
import com.generic.core.annotation.Host;
import com.generic.core.annotation.Post;
import com.generic.core.annotation.ServiceInterface;
import com.generic.core.http.client.HttpClient;
import com.generic.core.http.models.HttpHeaderName;
import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.pipeline.HttpPipeline;
import com.generic.core.http.pipeline.HttpPipelineBuilder;
import com.generic.core.implementation.http.rest.RequestOptions;
import com.generic.core.implementation.http.rest.RestProxyUtils;
import com.generic.core.implementation.util.BinaryDataContent;
import com.generic.core.implementation.util.BinaryDataHelper;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Context;
import com.generic.core.models.SimpleClass;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link RestProxy}.
 */
public class RestProxyTests {
    @Host("https://azure.com")
    @ServiceInterface(name = "myService")
    interface TestInterface {
        @Post("my/url/path")
        @ExpectedResponses({200})
        Response<Void> testMethod(
            @BodyParam("application/octet-stream") ByteBuffer request,
            @HeaderParam("Content-Type") String contentType,
            @HeaderParam("Content-Length") Long contentLength
        );

        @Post("my/url/path")
        @ExpectedResponses({200})
        Response<Void> testMethod(
            @BodyParam("application/octet-stream") BinaryData data,
            @HeaderParam("Content-Type") String contentType,
            @HeaderParam("Content-Length") Long contentLength
        );

        @Get("my/url/path")
        @ExpectedResponses({200})
        void testVoidMethod();

        @Get("my/url/path")
        @ExpectedResponses({200})
        Response<Void> testMethodReturnsResponseVoid();
    }

    @Test
    public void contentTypeHeaderPriorityOverBodyParamAnnotationTest() {
        HttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();
        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        byte[] bytes = "hello".getBytes();
        Response<Void> response =
            testInterface.testMethod(ByteBuffer.wrap(bytes), "application/json", (long) bytes.length);

        assertEquals(200, response.getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("knownLengthBinaryDataIsPassthroughArgumentProvider")
    public void knownLengthBinaryDataIsPassthrough(BinaryData data, long contentLength) {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();
        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        Response<Void> response = testInterface.testMethod(data, "application/json", contentLength);

        assertEquals(200, response.getStatusCode());
        assertSame(data, client.getLastHttpRequest().getBody());
    }

    private static Stream<Arguments> knownLengthBinaryDataIsPassthroughArgumentProvider() throws Exception {
        String string = "hello";
        byte[] bytes = string.getBytes();
        Path file = Files.createTempFile("knownLengthBinaryDataIsPassthroughArgumentProvider", null);
        SimpleClass simpleClass = new SimpleClass();

        file.toFile().deleteOnExit();
        Files.write(file, bytes);

        return Stream.of(
            Arguments.of(Named.of("bytes", BinaryData.fromBytes(bytes)), bytes.length),
            Arguments.of(Named.of("string", BinaryData.fromString(string)), bytes.length),
            Arguments.of(Named.of("file", BinaryData.fromFile(file)), bytes.length),
            Arguments.of(Named.of("serializable", BinaryData.fromObject(simpleClass)),
                BinaryData.fromObject(simpleClass).getLength())
        );
    }

    @ParameterizedTest
    @MethodSource("doesNotChangeBinaryDataContentTypeDataProvider")
    public void doesNotChangeBinaryDataContentType(BinaryData data, long contentLength) {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();
        Class<? extends BinaryDataContent> expectedContentClazz = BinaryDataHelper.getContent(data).getClass();
        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        Response<Void> response = testInterface.testMethod(data, "application/json", contentLength);

        assertEquals(200, response.getStatusCode());

        Class<? extends BinaryDataContent> actualContentClazz =
            BinaryDataHelper.getContent(client.getLastHttpRequest().getBody()).getClass();

        assertEquals(expectedContentClazz, actualContentClazz);
    }

    @Test
    public void voidReturningApiClosesResponse() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);

        testInterface.testVoidMethod();

        assertTrue(client.closeCalledOnResponse);
    }

    @Test
    public void voidReturningApiIgnoresResponseBody() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);

        testInterface.testVoidMethod();

        assertFalse(client.lastContext.getData("azure-eagerly-read-response").isPresent());
        assertTrue(client.lastContext.getData("azure-ignore-response-body").isPresent());
        assertTrue((boolean) client.lastContext.getData("azure-ignore-response-body").get());
    }

    @Test
    public void responseVoidReturningApiIgnoresResponseBody() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        Response<Void> response = testInterface.testMethodReturnsResponseVoid();

        assertEquals(200, response.getStatusCode());
        assertFalse(client.lastContext.getData("azure-eagerly-read-response").isPresent());
        assertTrue(client.lastContext.getData("azure-ignore-response-body").isPresent());
        assertTrue((boolean) client.lastContext.getData("azure-ignore-response-body").get());
    }

    private static Stream<Arguments> doesNotChangeBinaryDataContentTypeDataProvider() throws Exception {
        String string = "hello";
        byte[] bytes = string.getBytes();
        Path file = Files.createTempFile("doesNotChangeBinaryDataContentTypeDataProvider", null);
        SimpleClass simpleClass = new SimpleClass();

        file.toFile().deleteOnExit();
        Files.write(file, bytes);

        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

        return Stream.of(
            Arguments.of(Named.of("bytes", BinaryData.fromBytes(bytes)), bytes.length),
            Arguments.of(Named.of("string", BinaryData.fromString(string)), bytes.length),
            Arguments.of(Named.of("file", BinaryData.fromFile(file)), bytes.length),
            Arguments.of(Named.of("stream", BinaryData.fromStream(stream, (long) bytes.length)), bytes.length),
            Arguments.of(Named.of("byte buffer", BinaryData.fromByteBuffer(ByteBuffer.wrap(bytes))), bytes.length),
            Arguments.of(Named.of("serializable", BinaryData.fromObject(simpleClass)),
                BinaryData.fromObject(simpleClass).getLength())
        );
    }

    private static final class LocalHttpClient implements HttpClient {
        private volatile HttpRequest lastHttpRequest;
        private volatile Context lastContext;
        private volatile boolean closeCalledOnResponse;

        @Override
        public HttpResponse send(HttpRequest request, Context context) {
            lastHttpRequest = request;
            lastContext = context;
            boolean success = request.getUrl().getPath().equals("/my/url/path");

            if (request.getHttpMethod().equals(HttpMethod.POST)) {
                success &= "application/json".equals(request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
            } else {
                success &= request.getHttpMethod().equals(HttpMethod.GET);
            }

            return new MockHttpResponse(request, success ? 200 : 400) {
                @Override
                public void close() {
                    closeCalledOnResponse = true;

                    super.close();
                }
            };
        }

        public HttpRequest getLastHttpRequest() {
            return lastHttpRequest;
        }
    }

    @ParameterizedTest
    @MethodSource("mergeRequestOptionsContextSupplier")
    public void mergeRequestOptionsContext(Context context, RequestOptions options,
                                           Map<Object, Object> expectedContextValues) {
        Map<Object, Object> actualContextValues = RestProxyUtils.mergeRequestOptionsContext(context, options)
            .getValues();

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
