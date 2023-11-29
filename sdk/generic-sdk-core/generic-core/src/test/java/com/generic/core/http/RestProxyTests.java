// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http;

import com.generic.core.annotation.ServiceInterface;
import com.generic.core.http.annotation.BodyParam;
import com.generic.core.http.annotation.ExpectedResponses;
import com.generic.core.http.annotation.Get;
import com.generic.core.http.annotation.HeaderParam;
import com.generic.core.http.annotation.Host;
import com.generic.core.http.annotation.PathParam;
import com.generic.core.http.annotation.Post;
import com.generic.core.http.client.HttpClient;
import com.generic.core.http.models.HttpHeaderName;
import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.pipeline.HttpPipeline;
import com.generic.core.http.pipeline.HttpPipelineBuilder;
import com.generic.core.implementation.http.ContentType;
import com.generic.core.implementation.http.RestProxy;
import com.generic.core.implementation.http.rest.RestProxyUtils;
import com.generic.core.implementation.http.serializer.DefaultJsonSerializer;
import com.generic.core.implementation.util.BinaryDataContent;
import com.generic.core.implementation.util.BinaryDataHelper;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Context;
import com.generic.core.models.RequestOptions;
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

        @Get("{nextLink}")
        @ExpectedResponses({200})
        Response<Void> testListNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

        @Get("my/url/path")
        @ExpectedResponses({200})
        Void testMethodReturnsMonoVoid();

        @Get("my/url/path")
        @ExpectedResponses({200})
        void testVoidMethod();

        @Get("my/url/path")
        @ExpectedResponses({200})
        Response<Void> testMethodReturnsMonoResponseVoid();

        @Get("my/url/path")
        @ExpectedResponses({200})
        Response<Void> testMethodReturnsResponseVoid();

        @Get("my/url/path")
        @ExpectedResponses({200})
        StreamResponse testDownload();
    }

    @Test
    public void contentTypeHeaderPriorityOverBodyParamAnnotationTest() {
        HttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new DefaultJsonSerializer());
        byte[] bytes = "hello".getBytes();
        Response<Void> response =
            testInterface.testMethod(ByteBuffer.wrap(bytes), "application/json", (long) bytes.length);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void streamResponseShouldHaveHttpResponseReferenceSync() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new DefaultJsonSerializer());
        StreamResponse streamResponse = testInterface.testDownload();

        streamResponse.close();

        // This indirectly tests that StreamResponse has HttpResponse reference.
        assertTrue(client.closeCalledOnResponse);
    }

    @ParameterizedTest
    @MethodSource("knownLengthBinaryDataIsPassthroughArgumentProvider")
    public void knownLengthBinaryDataIsPassthrough(BinaryData data, long contentLength) {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new DefaultJsonSerializer());
        Response<Void> response = testInterface.testMethod(data, "application/json", contentLength);

        assertEquals(200, response.getStatusCode());
        assertSame(data, client.getLastHttpRequest().getBody());
    }

    private static Stream<Arguments> knownLengthBinaryDataIsPassthroughArgumentProvider() throws Exception {
        String string = "hello";
        byte[] bytes = string.getBytes();
        Path file = Files.createTempFile("knownLengthBinaryDataIsPassthroughArgumentProvider", null);

        file.toFile().deleteOnExit();

        Files.write(file, bytes);

        return Stream.of(
            Arguments.of(Named.of("bytes", BinaryData.fromBytes(bytes)), bytes.length),
            Arguments.of(Named.of("string", BinaryData.fromString(string)), bytes.length),
            Arguments.of(Named.of("file", BinaryData.fromFile(file)), bytes.length),
            Arguments.of(Named.of("serializable", BinaryData.fromObject(bytes)),
                BinaryData.fromObject(bytes).getLength())
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

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new DefaultJsonSerializer());
        Response<Void> response = testInterface.testMethod(data, ContentType.APPLICATION_JSON, contentLength);

        assertEquals(200, response.getStatusCode());

        Class<? extends BinaryDataContent> actualContentClazz =
            BinaryDataHelper.getContent(client.getLastHttpRequest().getBody()).getClass();

        assertEquals(expectedContentClazz, actualContentClazz);
    }

    @Test
    public void monoVoidReturningApiClosesResponse() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new DefaultJsonSerializer());

        testInterface.testMethodReturnsMonoVoid();

        assertTrue(client.closeCalledOnResponse);
    }

    @Test
    public void voidReturningApiClosesResponse() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new DefaultJsonSerializer());

        testInterface.testVoidMethod();

        assertTrue(client.closeCalledOnResponse);
    }

    @Test
    public void voidReturningApiIgnoresResponseBody() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new DefaultJsonSerializer());

        testInterface.testVoidMethod();

        assertFalse(client.getLastHttpRequest().getContext().getData("eagerly-read-response").isPresent());
        assertTrue(client.getLastHttpRequest().getContext().getData("ignore-response-body").isPresent());
        assertTrue((boolean) client.getLastHttpRequest().getContext().getData("ignore-response-body").get());
    }

    @Test
    public void monoVoidReturningApiIgnoresResponseBody() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new DefaultJsonSerializer());

        testInterface.testMethodReturnsMonoVoid();

        assertFalse(client.getLastHttpRequest().getContext().getData("eagerly-read-response").isPresent());
        assertTrue(client.getLastHttpRequest().getContext().getData("ignore-response-body").isPresent());
        assertTrue((boolean) client.getLastHttpRequest().getContext().getData("ignore-response-body").get());
    }

    @Test
    public void monoResponseVoidReturningApiIgnoresResponseBody() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new DefaultJsonSerializer());

        testInterface.testMethodReturnsMonoResponseVoid();

        assertFalse(client.getLastHttpRequest().getContext().getData("eagerly-read-response").isPresent());
        assertTrue(client.getLastHttpRequest().getContext().getData("ignore-response-body").isPresent());
        assertTrue((boolean) client.getLastHttpRequest().getContext().getData("ignore-response-body").get());
    }

    @Test
    public void responseVoidReturningApiIgnoresResponseBody() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new DefaultJsonSerializer());

        testInterface.testMethodReturnsResponseVoid();

        assertFalse(client.getLastHttpRequest().getContext().getData("eagerly-read-response").isPresent());
        assertTrue(client.getLastHttpRequest().getContext().getData("ignore-response-body").isPresent());
        assertTrue((boolean) client.getLastHttpRequest().getContext().getData("ignore-response-body").get());
    }

    @Test
    public void streamResponseDoesNotEagerlyReadsResponse() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();


        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new DefaultJsonSerializer());

        testInterface.testDownload();

        assertFalse(client.getLastHttpRequest().getContext().getData("eagerly-read-response").isPresent());
    }

    private static Stream<Arguments> doesNotChangeBinaryDataContentTypeDataProvider() throws Exception {
        String string = "hello";
        byte[] bytes = string.getBytes();
        Path file = Files.createTempFile("doesNotChangeBinaryDataContentTypeDataProvider", null);

        file.toFile().deleteOnExit();

        Files.write(file, bytes);
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

        return Stream.of(
            Arguments.of(Named.of("bytes", BinaryData.fromBytes(bytes)), bytes.length),
            Arguments.of(Named.of("string", BinaryData.fromString(string)), bytes.length),
            Arguments.of(Named.of("file", BinaryData.fromFile(file)), bytes.length),
            Arguments.of(Named.of("stream", BinaryData.fromStream(stream, (long) bytes.length)), bytes.length),
            Arguments.of(Named.of("serializable", BinaryData.fromObject(bytes)),
                BinaryData.fromObject(bytes).getLength())
        );
    }

    private static final class LocalHttpClient implements HttpClient {
        private volatile HttpRequest lastHttpRequest;
        private volatile boolean closeCalledOnResponse;

        @Override
        public HttpResponse send(HttpRequest request) {
            lastHttpRequest = request;
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

    @Test
    public void doesNotChangeEncodedPath() {
        String nextLinkUrl =
            "https://management.azure.com:443/subscriptions/000/resourceGroups/rg/providers/Microsoft.Compute/virtualMachineScaleSets/vmss1/virtualMachines?api-version=2021-11-01&$skiptoken=Mzk4YzFjMzMtM2IwMC00OWViLWI2NGYtNjg4ZTRmZGQ1Nzc2IS9TdWJzY3JpcHRpb25zL2VjMGFhNWY3LTllNzgtNDBjOS04NWNkLTUzNWM2MzA1YjM4MC9SZXNvdXJjZUdyb3Vwcy9SRy1XRUlEWFUtVk1TUy9WTVNjYWxlU2V0cy9WTVNTMS9WTXMvNzc=";
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient((request) -> {
                assertEquals(nextLinkUrl, request.getUrl().toString());

                return new MockHttpResponse(null, 200);
            })
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new DefaultJsonSerializer());

        testInterface.testListNext(nextLinkUrl);
    }
}
