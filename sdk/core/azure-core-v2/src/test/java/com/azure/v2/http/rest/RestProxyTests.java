// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.v2.annotation.BodyParam;
import com.azure.core.v2.annotation.ExpectedResponses;
import com.azure.core.v2.annotation.Get;
import com.azure.core.v2.annotation.HeaderParam;
import com.azure.core.v2.annotation.Host;
import com.azure.core.v2.annotation.PathParam;
import com.azure.core.v2.annotation.Post;
import com.azure.core.v2.annotation.ServiceInterface;
import io.clientcore.core.http.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.HttpMethod;
import io.clientcore.core.http.HttpPipeline;
import io.clientcore.core.http.HttpPipelineBuilder;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.MockHttpResponse;
import com.azure.core.v2.implementation.http.rest.RestProxyUtils;
import com.azure.core.v2.implementation.util.BinaryDataContent;
import com.azure.core.v2.implementation.util.BinaryDataHelper;
import com.azure.core.v2.util.BinaryData;
import io.clientcore.core.util.Context;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;

import reactor.test.StepVerifier;

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
        @ExpectedResponses({ 200 })
        Response<Void>> testMethod(@BodyParam("application/octet-stream") Flux<ByteBuffer> request,
            @HeaderParam("Content-Type") String contentType, @HeaderParam("Content-Length") Long contentLength);

        @Post("my/url/path")
        @ExpectedResponses({ 200 })
        Response<Void>> testMethod(@BodyParam("application/octet-stream") BinaryData data,
            @HeaderParam("Content-Type") String contentType, @HeaderParam("Content-Length") Long contentLength);

        @Get("{nextLink}")
        @ExpectedResponses({ 200 })
        Response<Void>> testListNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

        @Get("my/url/path")
        @ExpectedResponses({ 200 })
        Void> testMethodReturnsMonoVoid();

        @Get("my/url/path")
        @ExpectedResponses({ 200 })
        void testVoidMethod();

        @Get("my/url/path")
        @ExpectedResponses({ 200 })
        Response<Void>> testMethodReturnsMonoResponseVoid();

        @Get("my/url/path")
        @ExpectedResponses({ 200 })
        Response<Void> testMethodReturnsResponseVoid();

        @Get("my/url/path")
        @ExpectedResponses({ 200 })
        StreamResponse testDownload();

        @Get("my/url/path")
        @ExpectedResponses({ 200 })
        StreamResponse> testDownloadAsync();
    }

    @Test
    public void contentTypeHeaderPriorityOverBodyParamAnnotationTest() {
        HttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        byte[] bytes = "hello".getBytes();
        Response<Void> response
            = testInterface.testMethod(Flux.just(ByteBuffer.wrap(bytes)), "application/json", (long) bytes.length)
                .block();
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void streamResponseShouldHaveHttpResponseReferenceSync() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        StreamResponse streamResponse = testInterface.testDownload();
        streamResponse.close();
        // This indirectly tests that StreamResponse has HttpResponse reference
        assertTrue(client.closeCalledOnResponse);
    }

    @Test
    public void streamResponseShouldHaveHttpResponseReferenceAsync() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        StepVerifier.create(testInterface.testDownloadAsync().doOnNext(StreamResponse::close))
            .expectNextCount(1)
            .verifyComplete();
        // This indirectly tests that StreamResponse has HttpResponse reference
        assertTrue(client.closeCalledOnResponse);
    }

    @ParameterizedTest
    @MethodSource("knownLengthBinaryDataIsPassthroughArgumentProvider")
    public void knownLengthBinaryDataIsPassthrough(BinaryData data, long contentLength) {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        Response<Void> response = testInterface.testMethod(data, "application/json", contentLength).block();
        assertEquals(200, response.getStatusCode());
        assertSame(data, client.getLastHttpRequest().getBodyAsBinaryData());
    }

    private static Stream<Arguments> knownLengthBinaryDataIsPassthroughArgumentProvider() throws Exception {
        String string = "hello";
        byte[] bytes = string.getBytes();
        Path file = Files.createTempFile("knownLengthBinaryDataIsPassthroughArgumentProvider", null);
        file.toFile().deleteOnExit();
        Files.write(file, bytes);
        return Stream.of(Arguments.of(Named.of("bytes", BinaryData.fromBytes(bytes)), bytes.length),
            Arguments.of(Named.of("string", BinaryData.fromString(string)), bytes.length),
            Arguments.of(Named.of("file", BinaryData.fromFile(file)), bytes.length), Arguments
                .of(Named.of("serializable", BinaryData.fromObject(bytes)), BinaryData.fromObject(bytes).getLength()));
    }

    @ParameterizedTest
    @MethodSource("doesNotChangeBinaryDataContentTypeDataProvider")
    public void doesNotChangeBinaryDataContentType(BinaryData data, long contentLength) {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();
        Class<? extends BinaryDataContent> expectedContentClazz = BinaryDataHelper.getContent(data).getClass();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        Response<Void> response = testInterface.testMethod(data, "application/json", contentLength).block();
        assertEquals(200, response.getStatusCode());

        Class<? extends BinaryDataContent> actualContentClazz
            = BinaryDataHelper.getContent(client.getLastHttpRequest().getBodyAsBinaryData()).getClass();
        assertEquals(expectedContentClazz, actualContentClazz);
    }

    @Test
    public void monoVoidReturningApiClosesResponse() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        StepVerifier.create(testInterface.testMethodReturnsMonoVoid()).verifyComplete();

        assertTrue(client.closeCalledOnResponse);
    }

    @Test
    public void voidReturningApiClosesResponse() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);

        testInterface.testVoidMethod();

        assertTrue(client.closeCalledOnResponse);
    }

    @Test
    public void voidReturningApiIgnoresResponseBody() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);

        testInterface.testVoidMethod();

        assertFalse(client.lastContext.getData("azure-eagerly-read-response").isPresent());
        assertTrue(client.lastContext.getData("azure-ignore-response-body").isPresent());
        assertTrue((boolean) client.lastContext.getData("azure-ignore-response-body").get());
    }

    @Test
    public void monoVoidReturningApiIgnoresResponseBody() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        StepVerifier.create(testInterface.testMethodReturnsMonoVoid()).verifyComplete();

        assertFalse(client.lastContext.getData("azure-eagerly-read-response").isPresent());
        assertTrue(client.lastContext.getData("azure-ignore-response-body").isPresent());
        assertTrue((boolean) client.lastContext.getData("azure-ignore-response-body").get());
    }

    @Test
    public void monoResponseVoidReturningApiIgnoresResponseBody() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        StepVerifier.create(testInterface.testMethodReturnsMonoResponseVoid()).expectNextCount(1).verifyComplete();

        assertFalse(client.lastContext.getData("azure-eagerly-read-response").isPresent());
        assertTrue(client.lastContext.getData("azure-ignore-response-body").isPresent());
        assertTrue((boolean) client.lastContext.getData("azure-ignore-response-body").get());
    }

    @Test
    public void responseVoidReturningApiIgnoresResponseBody() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        testInterface.testMethodReturnsResponseVoid();

        assertFalse(client.lastContext.getData("azure-eagerly-read-response").isPresent());
        assertTrue(client.lastContext.getData("azure-ignore-response-body").isPresent());
        assertTrue((boolean) client.lastContext.getData("azure-ignore-response-body").get());
    }

    @Test
    public void streamResponseDoesNotEagerlyReadsResponse() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        testInterface.testDownload();

        assertFalse(client.lastContext.getData("azure-eagerly-read-response").isPresent());
    }

    @Test
    public void monoWithStreamResponseDoesNotEagerlyReadsResponse() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        StepVerifier.create(testInterface.testDownloadAsync().doOnNext(StreamResponse::close))
            .expectNextCount(1)
            .verifyComplete();

        assertFalse(client.lastContext.getData("azure-eagerly-read-response").isPresent());
    }

    private static Stream<Arguments> doesNotChangeBinaryDataContentTypeDataProvider() throws Exception {
        String string = "hello";
        byte[] bytes = string.getBytes();
        Path file = Files.createTempFile("doesNotChangeBinaryDataContentTypeDataProvider", null);
        file.toFile().deleteOnExit();
        Files.write(file, bytes);
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        return Stream.of(Arguments.of(Named.of("bytes", BinaryData.fromBytes(bytes)), bytes.length),
            Arguments.of(Named.of("string", BinaryData.fromString(string)), bytes.length),
            Arguments.of(Named.of("file", BinaryData.fromFile(file)), bytes.length),
            Arguments.of(Named.of("stream", BinaryData.fromStream(stream, (long) bytes.length)), bytes.length),
            Arguments.of(
                Named.of("eager flux with length", BinaryData.fromFlux(Flux.just(ByteBuffer.wrap(bytes))).block()),
                bytes.length),
            Arguments.of(
                Named.of("lazy flux", BinaryData.fromFlux(Flux.just(ByteBuffer.wrap(bytes)), null, false).block()),
                bytes.length),
            Arguments.of(
                Named.of("lazy flux with length",
                    BinaryData.fromFlux(Flux.just(ByteBuffer.wrap(bytes)), (long) bytes.length, false).block()),
                bytes.length),
            Arguments.of(Named.of("serializable", BinaryData.fromObject(bytes)),
                BinaryData.fromObject(bytes).getLength()));
    }

    private static final class LocalHttpClient implements HttpClient {

        private volatile HttpRequest lastHttpRequest;
        private volatile Context lastContext;
        private volatile boolean closeCalledOnResponse;

        @Override
        public Response<?>> send(HttpRequest request) {
            return send(request);
        }

        @Override
        public Response<?>> send(HttpRequest request, Context context) {
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
            });
        }

        public HttpRequest getLastHttpRequest() {
            return lastHttpRequest;
        }
    }

    @ParameterizedTest
    @MethodSource("mergeRequestOptionsContextSupplier")
    public void mergeRequestOptionsContext(Context context, RequestOptions options,
        Map<Object, Object> expectedContextValues) {
        Map<Object, Object> actualContextValues
            = RestProxyUtils.mergeRequestOptionsContext(context, options).getValues();

        assertEquals(expectedContextValues.size(), actualContextValues.size());
        for (Map.Entry<Object, Object> expectedKvp : expectedContextValues.entrySet()) {
            assertTrue(actualContextValues.containsKey(expectedKvp.getKey()),
                () -> "Missing expected key '" + expectedKvp.getKey() + "'.");
            assertEquals(expectedKvp.getValue(), actualContextValues.get(expectedKvp.getKey()));
        }
    }

    private static Stream<Arguments> mergeRequestOptionsContextSupplier() {
        Map<Object, Object> twoValuesMap = new HashMap<>();
        twoValuesMap.put("key", "value");
        twoValuesMap.put("key2", "value2");

        return Stream.of(
            // Cases where the RequestOptions or it's Context doesn't exist.
            Arguments.of(Context.none(), null, Collections.emptyMap()),
            Arguments.of(Context.none(), new RequestOptions(), Collections.emptyMap()),
            Arguments.of(Context.none(), new RequestOptions().setContext(Context.none()), Collections.emptyMap()),

            // Case where the RequestOptions Context is merged into an empty Context.
            Arguments.of(Context.none(), new RequestOptions().setContext(new Context("key", "value")),
                Collections.singletonMap("key", "value")),

            // Case where the RequestOptions Context is merged, without replacement, into an existing Context.
            Arguments.of(new Context("key", "value"), new RequestOptions().setContext(new Context("key2", "value2")),
                twoValuesMap),

            // Case where the RequestOptions Context is merged and overrides an existing Context.
            Arguments.of(new Context("key", "value"), new RequestOptions().setContext(new Context("key", "value2")),
                Collections.singletonMap("key", "value2")));
    }

    @Test
    public void doesNotChangeEncodedPath() {
        String nextLinkUrl
            = "https://management.azure.com:443/subscriptions/000/resourceGroups/rg/providers/Microsoft.Compute/virtualMachineScaleSets/vmss1/virtualMachines?api-version=2021-11-01&$skiptoken=Mzk4YzFjMzMtM2IwMC00OWViLWI2NGYtNjg4ZTRmZGQ1Nzc2IS9TdWJzY3JpcHRpb25zL2VjMGFhNWY3LTllNzgtNDBjOS04NWNkLTUzNWM2MzA1YjM4MC9SZXNvdXJjZUdyb3Vwcy9SRy1XRUlEWFUtVk1TUy9WTVNjYWxlU2V0cy9WTVNTMS9WTXMvNzc=";
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> {
            assertEquals(nextLinkUrl, request.getUrl().toString());
            return new MockHttpResponse(null, 200));
        }).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);

        StepVerifier.create(testInterface.testListNext(nextLinkUrl)).expectNextCount(1).verifyComplete();
    }
}
