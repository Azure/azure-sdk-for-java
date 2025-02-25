// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.test;

import io.clientcore.annotation.processor.test.implementation.TestInterfaceClientService;
import io.clientcore.core.http.RestProxy;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.implementation.http.ContentType;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.implementation.utils.JsonSerializer;
import io.clientcore.core.models.binarydata.BinaryData;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link RestProxy}.
 */
public class RestProxyTests {

    @Test
    public void contentTypeHeaderPriorityOverBodyParamAnnotationTest() throws IOException {
        HttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterfaceClientService testInterface = TestInterfaceClientService.getNewInstance(pipeline, new JsonSerializer());
        byte[] bytes = "hello".getBytes();
        try (Response<Void> response
            = testInterface.testMethod(ByteBuffer.wrap(bytes), "application/json", (long) bytes.length)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    // TODO (vcolin7): Re-enable this test if we ever compose HttpResponse into a stream Response type.
    /*@Test
    public void streamResponseShouldHaveHttpResponseReference() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .build();
        TestInterfaceClientService testInterface = TestInterfaceClientService.getNewInstance(pipeline, new JsonSerializer());
        StreamResponse streamResponse = testInterface.testDownload();

        streamResponse.close();

        // This indirectly tests that StreamResponse has HttpResponse reference.
        assertTrue(client.closeCalledOnResponse);
    }*/

    @ParameterizedTest
    @MethodSource("knownLengthBinaryDataIsPassthroughArgumentProvider")
    public void knownLengthBinaryDataIsPassthrough(BinaryData data, long contentLength) {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterfaceClientService testInterface = TestInterfaceClientService.getNewInstance(pipeline, new JsonSerializer());
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
        Class<? extends BinaryData> expectedContentClazz = data.getClass();

        TestInterfaceClientService testInterface = TestInterfaceClientService.getNewInstance(pipeline, new JsonSerializer());
        Response<Void> response = testInterface.testMethod(data, ContentType.APPLICATION_JSON, contentLength);

        assertEquals(200, response.getStatusCode());

        Class<? extends BinaryData> actualContentClazz = client.getLastHttpRequest().getBody().getClass();

        assertEquals(expectedContentClazz, actualContentClazz);
    }

    @Test
    public void voidReturningApiClosesResponse() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterfaceClientService testInterface = TestInterfaceClientService.getNewInstance(pipeline, new JsonSerializer());

        testInterface.testMethodReturnsVoid();

        assertTrue(client.closeCalledOnResponse);
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
            Arguments.of(Named.of("serializable", BinaryData.fromObject(bytes)),
                BinaryData.fromObject(bytes).getLength()));
    }


    @Test
    public void doesNotChangeEncodedPath() throws IOException {
        String nextLinkUri
            = "https://management.somecloud.com:443/subscriptions/000/resourceGroups/rg/providers/Microsoft.Compute/virtualMachineScaleSets/vmss1/virtualMachines?api-version=2021-11-01&$skiptoken=Mzk4YzFjMzMtM2IwMC00OWViLWI2NGYtNjg4ZTRmZGQ1Nzc2IS9TdWJzY3JpcHRpb25zL2VjMGFhNWY3LTllNzgtNDBjOS04NWNkLTUzNWM2MzA1YjM4MC9SZXNvdXJjZUdyb3Vwcy9SRy1XRUlEWFUtVk1TUy9WTVNjYWxlU2V0cy9WTVNTMS9WTXMvNzc=";
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient((request) -> {
            assertEquals(nextLinkUri, request.getUri().toString());

            return new MockHttpResponse(null, 200);
        }).build();

        TestInterfaceClientService testInterface = TestInterfaceClientService.getNewInstance(pipeline, new JsonSerializer());

        testInterface.testListNext(nextLinkUri).close();
    }
}
