// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http;

import io.clientcore.core.annotations.ServiceInterface;
import io.clientcore.core.http.annotations.BodyParam;
import io.clientcore.core.http.annotations.HeaderParam;
import io.clientcore.core.http.annotations.HttpRequestInformation;
import io.clientcore.core.http.annotations.PathParam;
import io.clientcore.core.http.annotations.QueryParam;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.ResponseBodyMode;
import io.clientcore.core.implementation.http.ContentType;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.implementation.utils.JsonSerializer;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;
import io.clientcore.core.utils.Base64Uri;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link RestProxy}.
 */
public class RestProxyTests {
    @ServiceInterface(name = "myService", host = "https://somecloud.com")
    interface TestInterface {
        @HttpRequestInformation(method = HttpMethod.POST, path = "my/uri/path", expectedStatusCodes = { 200 })
        Response<Void> testMethod(@BodyParam("application/octet-stream") ByteBuffer request,
            @HeaderParam("Content-Type") String contentType, @HeaderParam("Content-Length") Long contentLength);

        @HttpRequestInformation(method = HttpMethod.POST, path = "my/uri/path", expectedStatusCodes = { 200 })
        Response<Void> testMethod(@BodyParam("application/octet-stream") BinaryData data,
            @HeaderParam("Content-Type") String contentType, @HeaderParam("Content-Length") Long contentLength);

        @HttpRequestInformation(method = HttpMethod.GET, path = "{nextLink}", expectedStatusCodes = { 200 })
        Response<Void> testListNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

        @HttpRequestInformation(method = HttpMethod.GET, path = "my/uri/path", expectedStatusCodes = { 200 })
        Void testMethodReturnsVoid();

        @HttpRequestInformation(method = HttpMethod.HEAD, path = "my/uri/path", expectedStatusCodes = { 200 })
        void testHeadMethod();

        @HttpRequestInformation(method = HttpMethod.GET, path = "my/uri/path", expectedStatusCodes = { 200 })
        Response<Void> testMethodReturnsResponseVoid();

        @HttpRequestInformation(method = HttpMethod.GET, path = "my/uri/path", expectedStatusCodes = { 200 })
        Response<InputStream> testDownload();
    }

    @Test
    public void contentTypeHeaderPriorityOverBodyParamAnnotationTest() throws IOException {
        HttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new JsonSerializer());
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
        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new JsonSerializer());
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

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new JsonSerializer());
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

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new JsonSerializer());
        Response<Void> response = testInterface.testMethod(data, ContentType.APPLICATION_JSON, contentLength);

        assertEquals(200, response.getStatusCode());

        Class<? extends BinaryData> actualContentClazz = client.getLastHttpRequest().getBody().getClass();

        assertEquals(expectedContentClazz, actualContentClazz);
    }

    @Test
    public void voidReturningApiClosesResponse() {
        LocalHttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new JsonSerializer());

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

    private static final class LocalHttpClient implements HttpClient {
        private volatile HttpRequest lastHttpRequest;
        private volatile boolean closeCalledOnResponse;

        @Override
        public Response<?> send(HttpRequest request) {
            lastHttpRequest = request;
            boolean success = request.getUri().getPath().equals("/my/uri/path");

            if (request.getHttpMethod().equals(HttpMethod.POST)) {
                success &= "application/json".equals(request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
            } else {
                success &= request.getHttpMethod().equals(HttpMethod.GET)
                    || request.getHttpMethod().equals(HttpMethod.HEAD);
            }

            return new MockHttpResponse(request, success ? 200 : 400) {
                @Override
                public void close() throws IOException {
                    closeCalledOnResponse = true;

                    super.close();
                }
            };
        }

        public HttpRequest getLastHttpRequest() {
            return lastHttpRequest;
        }
    }

    @Test
    public void doesNotChangeEncodedPath() throws IOException {
        String nextLinkUri
            = "https://management.somecloud.com:443/subscriptions/000/resourceGroups/rg/providers/Microsoft.Compute/virtualMachineScaleSets/vmss1/virtualMachines?api-version=2021-11-01&$skiptoken=Mzk4YzFjMzMtM2IwMC00OWViLWI2NGYtNjg4ZTRmZGQ1Nzc2IS9TdWJzY3JpcHRpb25zL2VjMGFhNWY3LTllNzgtNDBjOS04NWNkLTUzNWM2MzA1YjM4MC9SZXNvdXJjZUdyb3Vwcy9SRy1XRUlEWFUtVk1TUy9WTVNjYWxlU2V0cy9WTVNTMS9WTXMvNzc=";
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient((request) -> {
            assertEquals(nextLinkUri, request.getUri().toString());

            return new MockHttpResponse(null, 200);
        }).build();

        TestInterface testInterface = RestProxy.create(TestInterface.class, pipeline, new JsonSerializer());

        testInterface.testListNext(nextLinkUri).close();
    }

    public enum EnumType implements JsonSerializable<EnumType> {
        ENUM("enum");

        private final String value;

        EnumType(String value) {
            this.value = value;
        }

        public static EnumType fromString(String value) {
            if (value == null) {
                return null;
            }
            EnumType[] items = EnumType.values();
            for (EnumType item : items) {
                if (item.toString().equalsIgnoreCase(value)) {
                    return item;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeString(this.value);
        }

        public static EnumType fromJson(JsonReader jsonReader) throws IOException {
            JsonToken nextToken = jsonReader.nextToken();
            if (nextToken == JsonToken.NULL) {
                return null;
            }
            if (nextToken != JsonToken.STRING) {
                throw new IllegalStateException(String.format("Unexpected JSON token for %s", nextToken));
            }
            return EnumType.fromString(jsonReader.getString());
        }
    }

    @ServiceInterface(name = "typeService", host = "https://somecloud.com")
    interface TestTypeService {
        @HttpRequestInformation(
            method = HttpMethod.POST,
            path = "my/uri/path",
            expectedStatusCodes = { 200 },
            returnValueWireType = OffsetDateTime.class)
        Response<OffsetDateTime> testOffsetDateTime(@QueryParam(value = "query") OffsetDateTime queryParam,
            @BodyParam(value = "application/json") OffsetDateTime requestBody, RequestOptions requestOptions);

        @HttpRequestInformation(method = HttpMethod.POST, path = "my/uri/path", expectedStatusCodes = { 200 })
        Response<byte[]> testBase64(@BodyParam(value = "application/json") byte[] requestBody,
            RequestOptions requestOptions);

        @HttpRequestInformation(
            method = HttpMethod.POST,
            path = "my/uri/path",
            expectedStatusCodes = { 200 },
            returnValueWireType = Base64Uri.class)
        Response<byte[]> testBase64Uri(@QueryParam(value = "query") Base64Uri queryParam,
            @BodyParam(value = "application/json") Base64Uri requestBody, RequestOptions requestOptions);

        @HttpRequestInformation(method = HttpMethod.POST, path = "my/uri/path", expectedStatusCodes = { 200 })
        Response<EnumType> testEnum(@QueryParam(value = "query") EnumType queryParam,
            @BodyParam(value = "application/json") EnumType requestBody, RequestOptions requestOptions);

        @HttpRequestInformation(method = HttpMethod.POST, path = "my/uri/path", expectedStatusCodes = { 200 })
        Response<Duration> testDuration(@QueryParam(value = "query") Duration queryParam,
            @BodyParam(value = "application/json") Duration requestBody, RequestOptions requestOptions);
    }

    // the pipeline mirror a JSON string from request to response
    private static final HttpPipeline MIRROR_PIPELINE = new HttpPipelineBuilder().httpClient(request -> {
        String query = request.getUri().getQuery();
        if (query != null && query.startsWith("query=")) {
            String queryValue = query.substring("query=".length());

            String bodyValue = request.getBody().toObject(String.class);
            Assertions.assertEquals(queryValue, bodyValue);
        }

        return Response.create(request, 200, new HttpHeaders(), request.getBody());
    }).build();

    @Test
    public void canProcessDateTime() {
        TestTypeService testInterface = RestProxy.create(TestTypeService.class, MIRROR_PIPELINE, new JsonSerializer());
        RequestOptions requestOptions = new RequestOptions().setResponseBodyMode(ResponseBodyMode.DESERIALIZE);

        // java.lang.ClassCastException: class java.lang.String cannot be cast to class java.time.OffsetDateTime
        OffsetDateTime request = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime response = testInterface.testOffsetDateTime(request, request, requestOptions).getValue();
        Assertions.assertEquals(request, response);
    }

    @Test
    public void canProcessBase64() {
        TestTypeService testInterface = RestProxy.create(TestTypeService.class, MIRROR_PIPELINE, new JsonSerializer());
        RequestOptions requestOptions = new RequestOptions().setResponseBodyMode(ResponseBodyMode.DESERIALIZE);

        // Base64 in response is not decoded
        byte[] request = "test".getBytes(StandardCharsets.UTF_8);
        byte[] response = testInterface.testBase64(request, requestOptions).getValue();
        Assertions.assertArrayEquals(request, response);
    }

    @Test
    public void canProcessBase64Uri() {
        TestTypeService testInterface = RestProxy.create(TestTypeService.class, MIRROR_PIPELINE, new JsonSerializer());
        RequestOptions requestOptions = new RequestOptions().setResponseBodyMode(ResponseBodyMode.DESERIALIZE);

        // https://github.com/Azure/azure-sdk-for-java/pull/44287
        Base64Uri request = Base64Uri.encode("test".getBytes(StandardCharsets.UTF_8));
        byte[] response = testInterface.testBase64Uri(request, request, requestOptions).getValue();
        Assertions.assertEquals(request, Base64Uri.encode(response));
    }

    @Test
    public void canProcessEnum() {
        TestTypeService testInterface = RestProxy.create(TestTypeService.class, MIRROR_PIPELINE, new JsonSerializer());
        RequestOptions requestOptions = new RequestOptions().setResponseBodyMode(ResponseBodyMode.DESERIALIZE);

        EnumType request = EnumType.ENUM;
        EnumType response = testInterface.testEnum(request, request, requestOptions).getValue();
        Assertions.assertEquals(request, response);
    }

    @Test
    public void canProcessDuration() {
        TestTypeService testInterface = RestProxy.create(TestTypeService.class, MIRROR_PIPELINE, new JsonSerializer());
        RequestOptions requestOptions = new RequestOptions().setResponseBodyMode(ResponseBodyMode.DESERIALIZE);

        // java.lang.ClassCastException: class java.lang.String cannot be cast to class java.time.Duration
        Duration request = Duration.ofMinutes(30);
        Duration response = testInterface.testDuration(request, request, requestOptions).getValue();
        Assertions.assertEquals(request, response);
    }

    @ServiceInterface(name = "typeService", host = "https://somecloud.com")
    interface TestMediaTypeTextService {
        @HttpRequestInformation(method = HttpMethod.GET, path = "my/uri/path", expectedStatusCodes = { 200 })
        Response<String> testText(@HeaderParam("Accept") String accept, RequestOptions requestOptions);
    }

    @Test
    public void canProcessMediaTypeText() {
        TestMediaTypeTextService testInterface
            = RestProxy.create(TestMediaTypeTextService.class,
                new HttpPipelineBuilder().httpClient(request -> Response.create(request, 200,
                    new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "text/plain"), BinaryData.fromString("text")))
                    .build(),
                new JsonSerializer());
        RequestOptions requestOptions = new RequestOptions().setResponseBodyMode(ResponseBodyMode.DESERIALIZE);

        // java.lang.UnsupportedOperationException: None of the provided serializers support the format: TEXT
        String text = testInterface.testText("text/plain", requestOptions).getValue();
        Assertions.assertEquals("text", text);
    }
}
