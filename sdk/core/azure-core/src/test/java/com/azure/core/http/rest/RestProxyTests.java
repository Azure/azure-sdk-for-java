// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        Mono<Response<Void>> testMethod(
            @BodyParam("application/octet-stream") Flux<ByteBuffer> request,
            @HeaderParam("Content-Type") String contentType,
            @HeaderParam("Content-Length") Long contentLength
        );

        @Post("my/url/path")
        @ExpectedResponses({200})
        Mono<Response<Void>> testMethod(
            @BodyParam("application/octet-stream") BinaryData data,
            @HeaderParam("Content-Type") String contentType,
            @HeaderParam("Content-Length") Long contentLength
        );

        @Get("{nextLink}")
        @ExpectedResponses({200})
        Mono<Response<Void>> testListNext(
            @PathParam(value = "nextLink", encoded = true) String nextLink
        );
    }

    @Test
    public void contentTypeHeaderPriorityOverBodyParamAnnotationTest() {
        HttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        byte[] bytes = "hello".getBytes();
        Response<Void> response = testInterface.testMethod(Flux.just(ByteBuffer.wrap(bytes)),
            "application/json", (long) bytes.length)
            .block();
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
        Response<Void> response = testInterface.testMethod(data,
                "application/json", contentLength)
            .block();
        assertEquals(200, response.getStatusCode());
        assertSame(data, client.getLastHttpRequest().getBodyAsBinaryData());
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
            Arguments.of(Named.of("flux with length",
                BinaryData.fromFlux(Flux.just(ByteBuffer.wrap(bytes))).block()), bytes.length),
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


        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        Response<Void> response = testInterface.testMethod(data,
                "application/json", contentLength)
            .block();
        assertEquals(200, response.getStatusCode());

        Class<? extends BinaryDataContent> actualContentClazz = BinaryDataHelper.getContent(
            client.getLastHttpRequest().getBodyAsBinaryData()).getClass();
        assertEquals(expectedContentClazz, actualContentClazz);
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
            Arguments.of(Named.of("stream", BinaryData.fromStream(stream)), bytes.length),
            Arguments.of(Named.of("eager flux with length",
                BinaryData.fromFlux(Flux.just(ByteBuffer.wrap(bytes))).block()), bytes.length),
            Arguments.of(Named.of("lazy flux",
                BinaryData.fromFlux(Flux.just(ByteBuffer.wrap(bytes)), null, false).block()), bytes.length),
            Arguments.of(Named.of("lazy flux with length",
                BinaryData.fromFlux(Flux.just(ByteBuffer.wrap(bytes)), (long) bytes.length, false).block()), bytes.length),
            Arguments.of(Named.of("serializable", BinaryData.fromObject(bytes)),
                BinaryData.fromObject(bytes).getLength())
        );
    }

    private static final class LocalHttpClient implements HttpClient {

        private volatile HttpRequest lastHttpRequest;

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            lastHttpRequest = request;
            boolean success = request.getHeaders()
                .stream()
                .filter(header -> header.getName().equals("Content-Type"))
                .map(header -> header.getValue())
                .anyMatch(contentType -> contentType.equals("application/json"));
            int statusCode = success ? 200 : 400;
            return Mono.just(new MockHttpResponse(request, statusCode));
        }

        public HttpRequest getLastHttpRequest() {
            return lastHttpRequest;
        }
    }

    @ParameterizedTest
    @MethodSource("mergeRequestOptionsContextSupplier")
    public void mergeRequestOptionsContext(Context context, RequestOptions options,
        Map<Object, Object> expectedContextValues) {
        Map<Object, Object> actualContextValues = RestProxy.mergeRequestOptionsContext(context, options).getValues();

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
        HttpClient client = Mockito.mock(HttpClient.class);
        ArgumentCaptor<HttpRequest> requestArgumentCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        Mockito.when(client.send(requestArgumentCaptor.capture(), Mockito.any()))
            .thenReturn(Mono.just(new MockHttpResponse(null, 200)));
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();

        String nextLinkUrl =
            "https://management.azure.com:443/subscriptions/000/resourceGroups/rg/providers/Microsoft.Compute/virtualMachineScaleSets/vmss1/virtualMachines?api-version=2021-11-01&$skiptoken=Mzk4YzFjMzMtM2IwMC00OWViLWI2NGYtNjg4ZTRmZGQ1Nzc2IS9TdWJzY3JpcHRpb25zL2VjMGFhNWY3LTllNzgtNDBjOS04NWNkLTUzNWM2MzA1YjM4MC9SZXNvdXJjZUdyb3Vwcy9SRy1XRUlEWFUtVk1TUy9WTVNjYWxlU2V0cy9WTVNTMS9WTXMvNzc=";

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline);
        testInterface.testListNext(nextLinkUrl).block();

        Assertions.assertEquals(nextLinkUrl, requestArgumentCaptor.getValue().getUrl().toString());
    }
}
