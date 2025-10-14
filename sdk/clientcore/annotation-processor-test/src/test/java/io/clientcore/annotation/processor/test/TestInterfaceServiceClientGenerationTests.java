// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.test;

import io.clientcore.annotation.processor.test.implementation.HostEdgeCase1Service;
import io.clientcore.annotation.processor.test.implementation.HostEdgeCase2Service;
import io.clientcore.annotation.processor.test.implementation.ParameterizedHostService;
import io.clientcore.annotation.processor.test.implementation.ParameterizedMultipleHostService;
import io.clientcore.annotation.processor.test.implementation.TestInterfaceClientImpl;
import io.clientcore.annotation.processor.test.implementation.models.Foo;
import io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON;
import io.clientcore.annotation.processor.test.implementation.models.OperationError;
import io.clientcore.annotation.processor.test.implementation.models.ServiceError;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.models.ServerSentEvent;
import io.clientcore.core.http.models.ServerSentEventListener;
import io.clientcore.core.http.pipeline.HttpInstrumentationOptions;
import io.clientcore.core.http.pipeline.HttpInstrumentationPolicy;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.implementation.http.ContentType;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.ObjectSerializer;
import io.clientcore.core.serialization.SerializationFormat;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.shared.HttpClientTests;
import io.clientcore.core.shared.HttpClientTestsServer;
import io.clientcore.core.shared.LocalTestServer;
import io.clientcore.core.utils.UriBuilder;
import io.clientcore.http.okhttp3.OkHttpHttpClientProvider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static io.clientcore.core.implementation.utils.ImplUtils.bomAwareToString;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class TestInterfaceServiceClientGenerationTests {
    private static LocalTestServer server;
    private static final byte[] EXPECTED_RETURN_BYTES = "Hello World!".getBytes(StandardCharsets.UTF_8);
    private static final ClientLogger LOGGER = new ClientLogger(HttpClientTests.class);
    private static final String PLAIN_RESPONSE = "plainBytesNoHeader";
    private static final String HEADER_RESPONSE = "plainBytesWithHeader";
    private static final String INVALID_HEADER_RESPONSE = "plainBytesInvalidHeader";
    private static final String UTF_8_BOM_RESPONSE = "utf8BomBytes";
    private static final String UTF_16BE_BOM_RESPONSE = "utf16BeBomBytes";
    private static final String UTF_16LE_BOM_RESPONSE = "utf16LeBomBytes";
    private static final String UTF_32BE_BOM_RESPONSE = "utf32BeBomBytes";
    private static final String UTF_32LE_BOM_RESPONSE = "utf32LeBomBytes";
    private static final String BOM_WITH_DIFFERENT_HEADER = "bomBytesWithDifferentHeader";
    private static final String ECHO_RESPONSE = "echo";

    @BeforeAll
    public static void startTestServer() {
        server = HttpClientTestsServer.getHttpClientTestsServer(HttpProtocolVersion.HTTP_1_1, false);

        server.start();
    }

    @AfterAll
    public static void stopTestServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void testGetNewInstance() {
        HttpClient client = new LocalHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        TestInterfaceClientImpl.TestInterfaceClientService service
            = TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        assertNotNull(service);
    }

    @Test
    public void testGetFoo() {
        String wireValue
            =
            "{\"bar\":\"hello.world\",\"baz\":[\"hello\",\"hello.world\"],\"qux\":{\"a.b\":\"c.d\",\"bar.a\":\"ttyy\",\"bar.b\":\"uuzz\",\"hello\":\"world\"},\"additionalProperties\":{\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}}";

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> new MockHttpResponse(request, 200, BinaryData.fromString(wireValue)))
            .build();

        TestInterfaceClientImpl.TestInterfaceClientService service
            = TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        assertNotNull(service);

        // test getFoo method
        Response<Foo> response = service.getFoo("key", "label", "sync-token-value");
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        Foo foo = response.getValue();
        assertNotNull(foo);
        assertEquals("hello.world", foo.bar());
        assertEquals(4, foo.qux().size());
        assertNotNull(foo.additionalProperties());
        assertEquals("baz", foo.additionalProperties().get("bar"));
        assertEquals("c.d", foo.additionalProperties().get("a.b"));
        assertEquals("barbar", foo.additionalProperties().get("properties.bar"));
    }

    @Test
    public void requestWithByteArrayReturnType() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);
        final byte[] result = service.getByteArray(getServerUri(false));

        assertNotNull(result);
        assertEquals(100, result.length);
    }

    /**
     * Tests that the response body is correctly returned as a byte array.
     */
    @Test
    public void requestWithByteArrayReturnTypeAndParameterizedHostAndPath() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();

        ParameterizedHostService service
            = ParameterizedHostService.getNewInstance(pipeline);
        final byte[] result = service.getByteArray("http", "localhost:" + server.getPort(), 100);

        assertNotNull(result);
        assertEquals(100, result.length);
    }

    /**
     * Tests that the response body is correctly returned as a byte array.
     */
    @Test
    public void requestWithByteArrayReturnTypeAndHostEdgeCase1() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();

        byte[] result = HostEdgeCase1Service.getNewInstance(pipeline)
            .getByteArray("http://localhost:" + server.getPort(), 100);

        assertNotNull(result);
        assertEquals(100, result.length);
    }

    /**
     * Tests that the response body is correctly returned as a byte array.
     */
    @Test
    public void requestWithByteArrayReturnTypeAndHostEdgeCase2() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();

        byte[] result = HostEdgeCase2Service.getNewInstance(pipeline)
            .getByteArray("http://localhost:" + server.getPort(), 100);

        assertNotNull(result);
        assertEquals(100, result.length);
    }

    /**
     * Tests that a response with no return type is correctly handled.
     */
    @Test
    public void getRequestWithNoReturn() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);
        assertDoesNotThrow(() -> service.getNothing(getServerUri(false)));
    }

    @Test
    public void getRequestWithAnything() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);
        final HttpBinJSON json = service.getAnything(getServerUri(false));

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything", json.uri());
    }

    @Test
    public void getRequestWithAnythingWithPlus() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);
        final HttpBinJSON json = service.getAnythingWithPlus(getServerUri(false));

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with+plus", json.uri());
    }

    @Test
    public void getRequestWithAnythingWithPathParam() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);
        final HttpBinJSON json = service.getAnythingWithPathParam(getServerUri(false), "withpathparam");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/withpathparam", json.uri());
    }

    @Test
    public void getRequestWithAnythingWithPathParamWithSpace() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);
        final HttpBinJSON json = service.getAnythingWithPathParam(getServerUri(false), "with path param");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with path param", json.uri());
    }

    @Test
    public void getRequestWithAnythingWithPathParamWithPlus() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);
        final HttpBinJSON json = service.getAnythingWithPathParam(getServerUri(false), "with+path+param");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with+path+param", json.uri());
    }

    @Test
    public void getRequestWithAnythingWithEncodedPathParam() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);
        final HttpBinJSON json = service.getAnythingWithEncodedPathParam(getServerUri(false), "withpathparam");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/withpathparam", json.uri());
    }

    @Test
    public void getRequestWithAnythingWithEncodedPathParamWithPercent20() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);
        final HttpBinJSON json
            = service.getAnythingWithEncodedPathParam(getServerUri(false), "with%20path%20param");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with path param", json.uri());
    }

    @Test
    public void getRequestWithAnythingWithEncodedPathParamWithPlus() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);
        final HttpBinJSON json = service.getAnythingWithEncodedPathParam(getServerUri(false), "with+path+param");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with+path+param", json.uri());
    }

    @Test
    public void getRequestWithQueryParametersAndAnything() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);
        final HttpBinJSON json = service.getAnything(getServerUri(false), "A", 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything?a=A&b=15", json.uri());
    }

    @Test
    public void getRequestWithQueryParametersAndAnythingWithPercent20() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);
        final HttpBinJSON json = service.getAnything(getServerUri(false), "A%20Z", 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything?a=A%2520Z&b=15", json.uri());
    }

    @Test
    public void getRequestWithQueryParametersAndAnythingWithEncodedWithPercent20() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);
        final HttpBinJSON json = service.getAnythingWithEncoded(getServerUri(false), "x%20y", 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything?a=x y&b=15", json.uri());
    }

    @Test
    public void getRequestWithNullQueryParameter() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);
        final HttpBinJSON json = service.getAnything(getServerUri(false), null, 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything?b=15", json.uri());
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithEmptyBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);
        Response<HttpBinJSON> response
            = service.putWithHeaderApplicationOctetStreamContentTypeAndStringBody(getServerUri(false), "");
        assertNotNull(response);
        assertEquals("application/octet-stream",
            response.getRequest().getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));

        assertEquals("", response.getValue().data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithEmptyBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result
            = service.putWithHeaderApplicationJsonContentTypeAndByteArrayBody(getServerUri(false), new byte[0]);

        assertEquals("\"\"", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndStringBodyWithNullBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result = service.putWithNoContentTypeAndStringBody(getServerUri(false), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndStringBodyWithEmptyBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result = service.putWithNoContentTypeAndStringBody(getServerUri(false), "");

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndStringBodyWithNonEmptyBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result = service.putWithNoContentTypeAndStringBody(getServerUri(false), "hello");

        assertEquals("hello", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndByteArrayBodyWithNullBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result = service.putWithNoContentTypeAndByteArrayBody(getServerUri(false), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndByteArrayBodyWithEmptyBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result = service.putWithNoContentTypeAndByteArrayBody(getServerUri(false), new byte[0]);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndByteArrayBodyWithNonEmptyBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result
            = service.putWithNoContentTypeAndByteArrayBody(getServerUri(false), new byte[] { 0, 1, 2, 3, 4 });

        assertEquals(new String(new byte[] { 0, 1, 2, 3, 4 }), result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithNullBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result
            = service.putWithHeaderApplicationJsonContentTypeAndStringBody(getServerUri(false), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithEmptyBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result
            = service.putWithHeaderApplicationJsonContentTypeAndStringBody(getServerUri(false), "");

        assertEquals("\"\"", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithNonEmptyBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result = service
            .putWithHeaderApplicationJsonContentTypeAndStringBody(getServerUri(false), "soups and stuff");

        assertEquals("\"soups and stuff\"", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithNullBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result
            = service.putWithHeaderApplicationJsonContentTypeAndByteArrayBody(getServerUri(false), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithNonEmptyBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);
        final byte[] requestBody = new byte[] { 0, 1, 2, 3, 4 };

        final HttpBinJSON result
            = service.putWithHeaderApplicationJsonContentTypeAndByteArrayBody(getServerUri(false), requestBody);
        assertEquals("\"AAECAwQ=\"", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithNullBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result
            = service.putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(getServerUri(false), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithEmptyBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result
            = service.putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(getServerUri(false), "");

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithNonEmptyBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);
        String requestBody = "soups and stuff";
        final HttpBinJSON result = service
            .putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(getServerUri(false), requestBody);

        assertEquals("soups and stuff", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithNullBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        Response<HttpBinJSON> response
            = service.putWithHeaderApplicationOctetStreamContentTypeAndStringBody(getServerUri(false), null);
        assertNotNull(response);

        assertEquals("", response.getValue().data());
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithNonEmptyBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final Response<HttpBinJSON> response = service
            .putWithHeaderApplicationOctetStreamContentTypeAndStringBody(getServerUri(false), "penguins");
        assertNotNull(response);
        assertEquals("application/octet-stream",
            response.getRequest().getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));

        assertEquals("penguins", response.getValue().data());
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithNullBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result
            = service.putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(getServerUri(false), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithEmptyBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result = service
            .putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(getServerUri(false), new byte[0]);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithNonEmptyBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result = service.putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(
            getServerUri(false), new byte[] { 0, 1, 2, 3, 4 });

        assertEquals(new String(new byte[] { 0, 1, 2, 3, 4 }), result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithNullBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final Response<HttpBinJSON> response
            = service.putWithBodyParamApplicationJsonContentTypeAndStringBody(getServerUri(false), null);
        assertNotNull(response);
        assertEquals("", response.getValue().data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithEmptyBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final Response<HttpBinJSON> response
            = service.putWithBodyParamApplicationJsonContentTypeAndStringBody(getServerUri(false), "");
        assertNotNull(response);
        assertEquals("application/json", response.getRequest().getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
        assertEquals("\"\"", response.getValue().data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithNonEmptyBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final Response<HttpBinJSON> response = service
            .putWithBodyParamApplicationJsonContentTypeAndStringBody(getServerUri(false), "soups and stuff");
        assertNotNull(response);
        assertEquals("application/json", response.getRequest().getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
        assertEquals("\"soups and stuff\"", response.getValue().data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithNullBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result = service
            .putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(getServerUri(false), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithEmptyBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result
            = service.putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(getServerUri(false), "");

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithNonEmptyBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result = service
            .putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(getServerUri(false), "soups and stuff");

        assertEquals("soups and stuff", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithNullBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result
            = service.putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(getServerUri(false), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithEmptyBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result = service
            .putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(getServerUri(false), new byte[0]);

        assertEquals("\"\"", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithNonEmptyBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result = service.putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(
            getServerUri(false), new byte[] { 0, 1, 2, 3, 4 });

        assertEquals("\"AAECAwQ=\"", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithNullBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result
            = service.putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(getServerUri(false), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithEmptyBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result
            = service.putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(getServerUri(false), "");

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithNonEmptyBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result = service
            .putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(getServerUri(false), "penguins");

        assertEquals("penguins", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithNullBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result = service
            .putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(getServerUri(false), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithEmptyBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result = service
            .putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(getServerUri(false), new byte[0]);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithNonEmptyBody() {
        TestInterfaceClientImpl.TestInterfaceClientService service =
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final HttpBinJSON result = service.putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(
            getServerUri(false), new byte[] { 0, 1, 2, 3, 4 });

        assertEquals(new String(new byte[] { 0, 1, 2, 3, 4 }), result.data());
    }

    /**
     * Tests that a response without a byte order mark or a 'Content-Type' header encodes using UTF-8.
     */
    @Test
    public void plainResponse() throws IOException {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        assertEquals(expected, new String(sendRequest(PLAIN_RESPONSE), StandardCharsets.UTF_8));
    }

    /**
     * Tests that a response with a 'Content-Type' header encodes using the specified charset.
     */
    @Test
    public void headerResponse() throws IOException {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_16BE);

        assertEquals(expected, new String(sendRequest(HEADER_RESPONSE), StandardCharsets.UTF_16BE));
    }

    /**
     * Tests that a response with a 'Content-Type' containing an invalid or unsupported charset encodes using UTF-8.
     */
    @Test
    public void invalidHeaderResponse() throws IOException {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);

        assertEquals(expected, new String(sendRequest(INVALID_HEADER_RESPONSE), StandardCharsets.UTF_8));
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf8BomResponse() throws IOException {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);
        byte[] response = sendRequest(UTF_8_BOM_RESPONSE);

        assertEquals(expected, bomAwareToString(response, 0, response.length, null));
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf16BeBomResponse() throws IOException {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_16BE);
        byte[] response = sendRequest(UTF_16BE_BOM_RESPONSE);

        assertEquals(expected, bomAwareToString(response, 0, response.length, null));
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf16LeBomResponse() throws IOException {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_16LE);
        byte[] response = sendRequest(UTF_16LE_BOM_RESPONSE);

        assertEquals(expected, bomAwareToString(response, 0, response.length, null));
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf32BeBomResponse() throws IOException {
        String expected = new String(EXPECTED_RETURN_BYTES, Charset.forName("UTF-32BE"));

        assertEquals(expected, new String(sendRequest(UTF_32BE_BOM_RESPONSE), Charset.forName("UTF-32BE")));
    }

    /**
     * Tests that a response with a byte order mark encodes using the specified charset.
     */
    @Test
    public void utf32LeBomResponse() throws IOException {
        String expected = new String(EXPECTED_RETURN_BYTES, Charset.forName("UTF-32LE"));

        assertEquals(expected, new String(sendRequest(UTF_32LE_BOM_RESPONSE), Charset.forName("UTF-32LE")));
    }

    /**
     * Tests that a response with a byte order marker and 'Content-Type' header will defer to using the BOM encoding.
     */
    @Test
    public void bomWithSameHeader() throws IOException {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);
        byte[] response = sendRequest(BOM_WITH_DIFFERENT_HEADER);

        assertEquals(expected, bomAwareToString(response, 0, response.length, "charset=utf-8"));
    }

    /**
     * Tests that a response with a byte order marker and 'Content-Type' header will defer to using the BOM encoding.
     */
    @Test
    public void bomWithDifferentHeader() throws IOException {
        String expected = new String(EXPECTED_RETURN_BYTES, StandardCharsets.UTF_8);
        byte[] response = sendRequest(BOM_WITH_DIFFERENT_HEADER);

        assertEquals(expected, bomAwareToString(response, 0, response.length, "charset=utf-16"));
    }

    /**
     * Tests that unbuffered response body can be accessed.
     */
    @Test
    public void canAccessResponseBody() throws IOException {
        BinaryData requestBody = BinaryData.fromString("test body");
        HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.PUT).setUri(getRequestUri(ECHO_RESPONSE)).setBody(requestBody);

        try (Response<BinaryData> response = getHttpClient().send(request)) {
            assertEquals(requestBody.toString(), response.getValue().toString());
            assertArrayEquals(requestBody.toBytes(), response.getValue().toBytes());
        }
    }

    /**
     * Tests that buffered response is indeed buffered, i.e. content can be accessed many times.
     */
    @Test
    public void bufferedResponseCanBeReadMultipleTimes() throws IOException {
        BinaryData requestBody = BinaryData.fromString("test body");
        HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.PUT).setUri(getRequestUri(ECHO_RESPONSE)).setBody(requestBody);

        try (Response<BinaryData> response = getHttpClient().send(request)) {
            // Read response twice using all accessors.
            assertEquals(requestBody.toString(), response.getValue().toString());
            assertEquals(requestBody.toString(), response.getValue().toString());

            assertArrayEquals(requestBody.toBytes(), response.getValue().toBytes());
            assertArrayEquals(requestBody.toBytes(), response.getValue().toBytes());
        }
    }

    /**
     * Tests that send random bytes in various forms to an endpoint that echoes bytes back to sender.
     *
     * @param requestBody The BinaryData that contains random bytes.
     * @param expectedResponseBody The expected bytes in the echo response.
     */
    @ParameterizedTest
    @MethodSource("getBinaryDataBodyVariants")
    public void canSendBinaryData(BinaryData requestBody, byte[] expectedResponseBody) throws IOException {
        HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.PUT).setUri(getRequestUri(ECHO_RESPONSE)).setBody(requestBody);

        try (Response<BinaryData> response = getHttpClient().send(request)) {
            assertArrayEquals(expectedResponseBody, response.getValue().toBytes());
        }
    }

    private static Stream<Arguments> getBinaryDataBodyVariants() {
        return Stream.of(1, 2, 10, 127, 1024, 1024 + 157, 8 * 1024 + 3, 10 * 1024 * 1024 + 13).flatMap(size -> {
            try {
                byte[] bytes = new byte[size];

                ThreadLocalRandom.current().nextBytes(bytes);

                BinaryData byteArrayData = BinaryData.fromBytes(bytes);
                String randomString = new String(bytes, StandardCharsets.UTF_8);
                byte[] randomStringBytes = randomString.getBytes(StandardCharsets.UTF_8);
                BinaryData stringBinaryData = BinaryData.fromString(randomString);
                BinaryData streamData = BinaryData.fromStream(new ByteArrayInputStream(bytes), (long) bytes.length);

                BinaryData objectBinaryData = BinaryData.fromObject(bytes, new ByteArraySerializer());
                Path wholeFile = Files.createTempFile("http-client-tests", null);

                wholeFile.toFile().deleteOnExit();

                Files.write(wholeFile, bytes);
                BinaryData fileData = BinaryData.fromFile(wholeFile);
                Path sliceFile = Files.createTempFile("http-client-tests", null);

                sliceFile.toFile().deleteOnExit();
                Files.write(sliceFile, new byte[size], StandardOpenOption.APPEND);
                Files.write(sliceFile, bytes, StandardOpenOption.APPEND);
                Files.write(sliceFile, new byte[size], StandardOpenOption.APPEND);

                BinaryData sliceFileData = BinaryData.fromFile(sliceFile, Long.valueOf(size), Long.valueOf(size));

                return Stream.of(
                    Arguments.of(Named.named("byte[]", byteArrayData), Named.named(String.valueOf(size), bytes)),
                    Arguments.of(Named.named("String", stringBinaryData),
                        Named.named(String.valueOf(randomStringBytes.length), randomStringBytes)),
                    Arguments.of(Named.named("InputStream", streamData), Named.named(String.valueOf(size), bytes)),
                    Arguments.of(Named.named("Object", objectBinaryData), Named.named(String.valueOf(size), bytes)),
                    Arguments.of(Named.named("File", fileData), Named.named(String.valueOf(size), bytes)),
                    Arguments.of(Named.named("File slice", sliceFileData), Named.named(String.valueOf(size), bytes)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private byte[] sendRequest(String requestPath) throws IOException {
        try (Response<BinaryData> response
            = getHttpClient().send(new HttpRequest().setMethod(HttpMethod.GET).setUri(getRequestUri(requestPath)))) {
            return response.getValue().toBytes();
        }
    }

    /**
     * Gets the request URI for given path.
     *
     * @param requestPath The path.
     * @return The request URI for given path.
     * @throws RuntimeException if uri is invalid.
     */
    protected URI getRequestUri(String requestPath) {
        try {
            return UriBuilder.parse(getServerUri(isSecure()) + "/" + requestPath).toUri();
        } catch (URISyntaxException e) {
            throw LOGGER.throwableAtError().log(e, RuntimeException::new);
        }
    }

    private static class ByteArraySerializer implements ObjectSerializer {
        @Override
        public <T> T deserializeFromBytes(byte[] data, Type type) {
            return null;
        }

        @Override
        public <T> T deserializeFromStream(InputStream stream, Type type) {
            return null;
        }

        @Override
        public byte[] serializeToBytes(Object value) {
            return (byte[]) value;
        }

        @Override
        public void serializeToStream(OutputStream stream, Object value) {
            try {
                stream.write((byte[]) value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean supportsFormat(SerializationFormat format) {
            return false;
        }
    }

    /**
     * Tests that the response body is correctly returned as a byte array.
     */
    @Test
    public void requestWithEmptyByteArrayReturnTypeAndParameterizedHostAndPath() {
        final byte[] result = createService(ParameterizedHostService.class)
            .getByteArray(getRequestScheme(), "localhost:" + getPort(), 0);

        assertEquals(0, result.length);
    }

    private static final HttpHeaderName HEADER_A = HttpHeaderName.fromString("A");
    private static final HttpHeaderName HEADER_B = HttpHeaderName.fromString("B");

    @Test
    public void getRequestWithHeaderParametersAndAnythingReturn() {
        final HttpBinJSON json = createService(TestInterfaceClientImpl.TestInterfaceClientService.class)
            .getAnythingWithHeaderParam(getRequestUri(), "A", 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything", json.uri());
        assertNotNull(json.headers());
        HttpHeaders headers = toHttpHeaders(json.headers());

        assertEquals("A", headers.getValue(HEADER_A));
        assertListEquals(Collections.singletonList("A"), headers.getValues(HEADER_A));

        assertEquals("15", headers.getValue(HEADER_B));
        assertListEquals(Collections.singletonList("15"), headers.getValues(HEADER_B));
    }

    @Test
    public void getRequestWithNullHeader() {
        final HttpBinJSON json = createService(TestInterfaceClientImpl.TestInterfaceClientService.class)
            .getAnythingWithHeaderParam(getRequestUri(), null, 15);
        HttpHeaders headers = toHttpHeaders(json.headers());

        assertNull(headers.getValue(HEADER_A));
        assertListEquals(null, headers.getValues(HEADER_A));

        assertEquals("15", headers.getValue(HEADER_B));
        assertListEquals(Collections.singletonList("15"), headers.getValues(HEADER_B));
    }

    @Test
    public void postRequestWithStringBody() {
        final HttpBinJSON json = createService(TestInterfaceClientImpl.TestInterfaceClientService.class)
            .post(getRequestUri(), "I'm a post body!");

        assertEquals(String.class, json.data().getClass());
        assertEquals("I'm a post body!", json.data());
    }

    @Test
    public void postRequestWithNullBody() {
        final HttpBinJSON result
            = createService(TestInterfaceClientImpl.TestInterfaceClientService.class).post(getRequestUri(), null);

        assertEquals("", result.data());
    }

    @Test
    public void putRequestWithIntBody() {
        final HttpBinJSON json
            = createService(TestInterfaceClientImpl.TestInterfaceClientService.class).put(getRequestUri(), 42);

        assertEquals(String.class, json.data().getClass());
        assertEquals("42", json.data());
    }

    // Test all scenarios for the body length and content length comparison for sync API
    @Test
    public void putRequestWithBodyAndEqualContentLength() {
        ByteBuffer body = ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8));
        final HttpBinJSON json = createService(TestInterfaceClientImpl.TestInterfaceClientService.class)
            .putBodyAndContentLength(getRequestUri(), body, 4L);

        assertEquals("test", json.data());
        assertEquals(ContentType.APPLICATION_OCTET_STREAM, json.getHeaderValue("Content-Type"));
        assertEquals("4", json.getHeaderValue("Content-Length"));
    }

    @Test
    public void putRequestWithUnexpectedResponseAndNoFallthroughExceptionType() {
        HttpResponseException e = assertThrows(HttpResponseException.class,
            () -> createService(TestInterfaceClientImpl.TestInterfaceClientService.class)
                .putWithUnexpectedResponseAndNoFallthroughExceptionType(getRequestUri(), "I'm the body!"));

        assertNotNull(e.getValue());
        assertInstanceOf(LinkedHashMap.class, e.getValue());

        @SuppressWarnings("unchecked")
        final LinkedHashMap<String, String> expectedBody = (LinkedHashMap<String, String>) e.getValue();

        assertEquals("I'm the body!", expectedBody.get("data"));
    }

    @Test
    public void unexpectedResponseWithStatusCodeAndExceptionType400ReturnsError() {
        String errorJson = "{\"error\":{\"code\":\"BadRequest\",\"message\":\"bad body\"}}";
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> new MockHttpResponse(request, 400, BinaryData.fromString(errorJson)))
            .build();

        TestInterfaceClientImpl.TestInterfaceClientService service =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        HttpResponseException e = assertThrows(HttpResponseException.class, () ->
            service.unexpectedResponseWithStatusCodeAndExceptionType(getRequestUri(), "bad body")
        );
        assertNotNull(e.getValue());
        assertInstanceOf(ServiceError.class, e.getValue());
        ServiceError serviceError = (ServiceError) e.getValue();
        assertInstanceOf(OperationError.class, serviceError.getError());
        OperationError operationError = serviceError.getError();
        assertEquals("bad body", operationError.getMessage());
        assertEquals("BadRequest", operationError.getCode());
    }

    @Test
    public void unexpectedResponseWithStatusCodeAndExceptionType403ReturnsOperationError() {
        // The error JSON should match the OperationError structure
        String errorJson = "{\"code\":\"Forbidden\",\"message\":\"forbidden body\"}";
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> new MockHttpResponse(request, 403, BinaryData.fromString(errorJson)))
            .build();

        TestInterfaceClientImpl.TestInterfaceClientService service =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        HttpResponseException e = assertThrows(HttpResponseException.class, () ->
            service.unexpectedResponseWithStatusCodeAndExceptionType(getRequestUri(), "forbidden body")
        );
        assertNotNull(e.getValue());
        assertInstanceOf(OperationError.class, e.getValue());
        OperationError operationError = (OperationError) e.getValue();
        assertEquals("forbidden body", operationError.getMessage());
        assertEquals("Forbidden", operationError.getCode());
    }

    @Test
    public void putRequestWithUnexpectedResponse() {
        HttpResponseException e = assertThrows(HttpResponseException.class,
            () -> createService(TestInterfaceClientImpl.TestInterfaceClientService.class).putWithUnexpectedResponse(getRequestUri(), "I'm the body!"));

        assertNotNull(e.getValue());
        assertInstanceOf(LinkedHashMap.class, e.getValue());

        @SuppressWarnings("unchecked")
        final LinkedHashMap<String, String> expectedBody = (LinkedHashMap<String, String>) e.getValue();

        assertEquals("I'm the body!", expectedBody.get("data"));
    }

    @Test
    public void putWithUnexpectedResponseNoStatusCodeAndExceptionTypeReturnsObject() {
        // This should use Error.class for any unexpected status code
        String errorJson = "{\"error\":{\"code\":\"SomeCode\",\"message\":\"some message\"}}";
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> new MockHttpResponse(request, 400, BinaryData.fromString(errorJson)))
            .build();

        TestInterfaceClientImpl.TestInterfaceClientService service =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        HttpResponseException e = assertThrows(HttpResponseException.class, () ->
            service.putWithUnexpectedResponseAndExceptionType(getRequestUri(), "body")
        );
        assertNotNull(e.getValue());
        assertInstanceOf(ServiceError.class, e.getValue());
        @SuppressWarnings("unchecked")
        final ServiceError error = (ServiceError) e.getValue();
        assertNotNull(error.getError());
        @SuppressWarnings("unchecked")
        OperationError innerError = error.getError();
        assertEquals("SomeCode", innerError.getCode());
        assertEquals("some message", innerError.getMessage());
    }

    @Test
    public void putWithUnexpectedResponseAndDeterminedExceptionTypeReturnsErrorOn200() {
        // This should use Error.class for status 200 only
        String errorJson = "{\"error\":{\"code\":\"OKButError\",\"message\":\"should not happen\"}}";
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> new MockHttpResponse(request, 200, BinaryData.fromString(errorJson)))
            .build();

        TestInterfaceClientImpl.TestInterfaceClientService service =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        HttpResponseException e = assertThrows(HttpResponseException.class, () ->
            service.putWithUnexpectedResponseAndDeterminedExceptionType(getRequestUri(), "body")
        );
        assertNotNull(e.getValue());
        assertInstanceOf(ServiceError.class, e.getValue());
        ServiceError serviceError = (ServiceError) e.getValue();
        assertInstanceOf(OperationError.class, serviceError.getError());
        OperationError operationError = serviceError.getError();
        assertEquals("should not happen", operationError.getMessage());
        assertEquals("OKButError", operationError.getCode());
    }

    @Test
    public void putWithUnexpectedResponseAndFallthroughExceptionTypeReturnsObjectOn400AndErrorOn403() {
        // 400 should fall back to LinkedHashMap, 403 should use Error.class if mapped
        String errorJson = "{\"error\":{\"code\":\"Forbidden\",\"message\":\"forbidden body\"}}";
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> {
                int status = request.getUri().toString().contains("403") ? 403 : 400;
                return new MockHttpResponse(request, status, BinaryData.fromString(errorJson));
            })
            .build();

        TestInterfaceClientImpl.TestInterfaceClientService service =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        // 400: fallback to LinkedHashMap
        HttpResponseException e400 = assertThrows(HttpResponseException.class, () ->
            service.putWithUnexpectedResponseAndFallthroughExceptionType(getRequestUri(), "body")
        );
        assertNotNull(e400.getValue());
        assertInstanceOf(LinkedHashMap.class, e400.getValue());

        // 403: mapped to Error.class
        HttpPipeline pipeline403 = new HttpPipelineBuilder()
            .httpClient(request -> new MockHttpResponse(request, 403, BinaryData.fromString(errorJson)))
            .build();
        TestInterfaceClientImpl.TestInterfaceClientService service403 =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline403);

        HttpResponseException e403 = assertThrows(HttpResponseException.class, () ->
            service403.putWithUnexpectedResponseAndFallthroughExceptionType(getRequestUri(), "body")
        );
        assertNotNull(e403.getValue());
        assertInstanceOf(ServiceError.class, e403.getValue());
        ServiceError serviceError = (ServiceError) e403.getValue();
        assertInstanceOf(OperationError.class, serviceError.getError());
        OperationError operationError = serviceError.getError();
        assertEquals("forbidden body", operationError.getMessage());
        assertEquals("Forbidden", operationError.getCode());
    }

    @Test
    public void headRequest() {
        try (Response<Void> response
            = createService(TestInterfaceClientImpl.TestInterfaceClientService.class).head(getRequestUri())) {
            assertNull(response.getValue());
        }
    }

    @Test
    public void headBooleanRequestReturnsResult() {
        final boolean result
            = createService(TestInterfaceClientImpl.TestInterfaceClientService.class).headBoolean(getRequestUri());

        assertTrue(result);
    }

    @Test
    public void voidHeadRequest() {
        createService(TestInterfaceClientImpl.TestInterfaceClientService.class).voidHead(getRequestUri());
    }

    @Test
    public void deleteRequest() {
        final HttpBinJSON json
            = createService(TestInterfaceClientImpl.TestInterfaceClientService.class).delete(getRequestUri(), false);

        assertEquals(String.class, json.data().getClass());
        assertEquals("false", json.data());
    }

    @Test
    public void patchRequest() {
        final HttpBinJSON json = createService(TestInterfaceClientImpl.TestInterfaceClientService.class)
            .patch(getRequestUri(), "body-contents");

        assertEquals(String.class, json.data().getClass());
        assertEquals("body-contents", json.data());
    }

    private static final HttpHeaderName MY_HEADER = HttpHeaderName.fromString("MyHeader");
    private static final HttpHeaderName MY_OTHER_HEADER = HttpHeaderName.fromString("MyOtherHeader");

    @Test
    public void headersRequest() {
        final HttpBinJSON json
            = createService(TestInterfaceClientImpl.TestInterfaceClientService.class).get(getRequestUri());

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything", json.uri());
        assertNotNull(json.headers());
        HttpHeaders headers = toHttpHeaders(json.headers());

        assertEquals("MyHeaderValue", headers.getValue(MY_HEADER));
        assertListEquals(Collections.singletonList("MyHeaderValue"), headers.getValues(MY_HEADER));
        assertEquals("My,Header,Value", headers.getValue(MY_OTHER_HEADER));
        assertListEquals(Arrays.asList("My", "Header", "Value"), headers.getValues(MY_OTHER_HEADER));
    }

    private static HttpHeaders toHttpHeaders(Map<String, List<String>> jsonHeaders) {
        HttpHeaders headers = new HttpHeaders();
        for (Map.Entry<String, List<String>> entry : jsonHeaders.entrySet()) {
            HttpHeaderName headerName = HttpHeaderName.fromString(entry.getKey());
            for (String value : entry.getValue()) {
                headers.add(headerName, value);
            }
        }
        return headers;
    }

    @Test
    public void service16Put() {
        final TestInterfaceClientImpl.TestInterfaceClientService service16
            = createService(TestInterfaceClientImpl.TestInterfaceClientService.class);
        final byte[] expectedBytes = new byte[] { 1, 2, 3, 4 };
        final HttpBinJSON httpBinJSON = service16.putByteArray(getRequestUri(), expectedBytes);

        // httpbin sends the data back as a string like "\u0001\u0002\u0003\u0004"
        assertInstanceOf(String.class, httpBinJSON.data());

        final String base64String = (String) httpBinJSON.data();
        final byte[] actualBytes = base64String.getBytes();

        assertArrayEquals(expectedBytes, actualBytes);
    }

    @Test
    public void requestWithMultipleHostParams() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();

        ParameterizedMultipleHostService service
            = ParameterizedMultipleHostService.getNewInstance(pipeline);
        final HttpBinJSON result = service
            .get(getRequestScheme(), "local", "host:" + getPort());

        assertNotNull(result);
        assertMatchWithHttpOrHttps("localhost/get", result.uri());
    }

    @Test
    public void service18GetStatus200() {
        createService(TestInterfaceClientImpl.TestInterfaceClientService.class).getStatus200(getRequestUri());
    }

    @Test
    public void service18GetStatus200WithExpectedResponse200() {
        assertDoesNotThrow(() -> createService(TestInterfaceClientImpl.TestInterfaceClientService.class)
            .getStatus200WithExpectedResponse200(getRequestUri()));
    }

    @Test
    public void service18GetStatus300() {
        assertThrows(HttpResponseException.class,
            () -> createService(TestInterfaceClientImpl.TestInterfaceClientService.class)
                .getStatus300(getRequestUri()));
    }

    @Test
    public void service18GetStatus300WithExpectedResponse300() {
        assertDoesNotThrow(() -> createService(TestInterfaceClientImpl.TestInterfaceClientService.class)
            .getStatus300WithExpectedResponse300(getRequestUri()));
    }

    @Test
    public void service18GetStatus400() {
        assertThrows(HttpResponseException.class,
            () -> createService(TestInterfaceClientImpl.TestInterfaceClientService.class)
                .getStatus400(getRequestUri()));
    }

    @Test
    public void service18GetStatus400WithExpectedResponse400() {
        assertDoesNotThrow(() -> createService(TestInterfaceClientImpl.TestInterfaceClientService.class)
            .getStatus400WithExpectedResponse400(getRequestUri()));
    }

    @Test
    public void service18GetStatus500() {
        assertThrows(HttpResponseException.class,
            () -> createService(TestInterfaceClientImpl.TestInterfaceClientService.class)
                .getStatus500(getRequestUri()));
    }

    @Test
    public void service18GetStatus500WithExpectedResponse500() {
        assertDoesNotThrow(() -> createService(TestInterfaceClientImpl.TestInterfaceClientService.class)
            .getStatus500WithExpectedResponse500(getRequestUri()));
    }

    @Test
    public void service20PutBodyAndHeaders() {
        /*final Response<HttpBinHeaders, HttpBinJSON> response = createService(TestInterfaceClientImpl.TestInterfaceClientService20.class)
            .putBodyAndHeaders(getRequestUri(), "body string");
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        assertEquals(Headers.class, response.getHeaders().getClass());

        final HttpBinJSON body = response.getValue();
        assertNotNull(body);
        assertMatchWithHttpOrHttps("localhost/put", body.uri());
        assertEquals("body string", body.data());

        final HttpBinHeaders headers = response.getDeserializedHeaders();
        assertNotNull(headers);
        assertTrue(headers.accessControlAllowCredentials());
        assertNotNull(headers.date());
        assertNotEquals(0, (Object) headers.xProcessedTime());

         */
    }

    @Test
    public void service20GetVoidResponseBuffersBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();

        TestInterfaceClientImpl.TestInterfaceClientService service
            = TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        final Response<Void> response = service.getVoidResponse(getRequestUri());

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNull(response.getValue());
    }

    @Test
    public void service20GetResponseBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();

        TestInterfaceClientImpl.TestInterfaceClientService service
            = TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        final Response<HttpBinJSON> response = service.putBody(getRequestUri(), "body string");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        final HttpBinJSON body = response.getValue();

        assertNotNull(body);
        assertMatchWithHttpOrHttps("localhost/put", body.uri());
        assertEquals("body string", body.data());

        final HttpHeaders headers = response.getHeaders();

        assertNotNull(headers);
    }

    @Test
    public void unexpectedHTTPOK() throws IOException {
        HttpResponseException e = assertThrows(HttpResponseException.class,
            () -> createService(TestInterfaceClientImpl.TestInterfaceClientService.class).getBytes(getRequestUri()));

        Map<String, String> exContext = parseExceptionContext(e);

        assertEquals("200", exContext.get("http.response.status_code"));
        assertEquals("1024", exContext.get("http.response.header.content-length"));
        assertEquals("application/octet-stream", exContext.get("http.response.header.content-type"));
        assertNull(exContext.get("http.response.body.content"));
    }

    @Test
    public void service21GetBytes100() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();

        TestInterfaceClientImpl.TestInterfaceClientService service
            = TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        final byte[] bytes = service.getBytes100(getRequestUri());

        assertNotNull(bytes);
        assertEquals(100, bytes.length);
    }

    @Test
    @Disabled("None of the provided serializers support the format: TEXT.")
    public void binaryDataUploadTest() throws Exception {
        Path filePath = Paths.get(getClass().getClassLoader().getResource("upload.txt").toURI());
        BinaryData data = BinaryData.fromFile(filePath);

        final HttpClient httpClient = getHttpClient();
        // Scenario: Log the body so that body buffering/replay behavior is exercised.

        // Order in which policies applied will be the order in which they added to builder
        final HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(httpClient)
            .addPolicy(new HttpInstrumentationPolicy(new HttpInstrumentationOptions()
                .setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.BODY_AND_HEADERS)))
            .build();
        Response<HttpBinJSON> response;

        TestInterfaceClientImpl.TestInterfaceClientService service
            = TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(httpPipeline);
        response = service.put(getServerUri(isSecure()), data, Files.size(filePath));

        assertEquals("The quick brown fox jumps over the lazy dog", response.getValue().data());
    }

    @Test
    @Disabled("Add support for header collection")
    public void service24Put() {
        final Map<String, String> headerCollection = new HashMap<>();

        headerCollection.put("DEF", "GHIJ");
        headerCollection.put("123", "45");

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();

        TestInterfaceClientImpl.TestInterfaceClientService service
            = TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        final HttpBinJSON result = service.put(getRequestUri(), headerCollection);

        assertNotNull(result.headers());

        HttpHeaders resultHeaders = toHttpHeaders(result.headers());

        assertEquals("GHIJ", resultHeaders.getValue(HttpHeaderName.fromString("ABCDEF")));
        assertEquals("45", resultHeaders.getValue(HttpHeaderName.fromString("ABC123")));
    }

    @Test
    public void requestContextChangesBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService service
            = TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        HttpBinJSON response = service.put(getServerUri(isSecure()), 42,
            RequestContext.builder()
                .addRequestCallback(httpRequest -> httpRequest.setBody(BinaryData.fromString("24")))
                .build());

        assertNotNull(response);
        assertNotNull(response.data());
        assertInstanceOf(String.class, response.data());
        assertEquals("24", response.data());
    }

    @Test
    public void requestContextChangesBodyAndContentLength() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService service
            = TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        HttpBinJSON response = service.put(getServerUri(isSecure()), 42,
            RequestContext.builder()
                .addRequestCallback(httpRequest -> httpRequest.setBody(BinaryData.fromString("4242"))
                    .getHeaders()
                    .add(HttpHeaderName.CONTENT_LENGTH, "4"))
                .build());

        assertNotNull(response);
        assertNotNull(response.data());
        assertInstanceOf(String.class, response.data());
        assertEquals("4242", response.data());
        assertEquals("4", response.getHeaderValue("Content-Length"));
    }

    private static final HttpHeaderName RANDOM_HEADER = HttpHeaderName.fromString("randomHeader");

    @Test
    public void requestContextAddAHeader() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService service
            = TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        HttpBinJSON response = service.put(getServerUri(isSecure()), 42,
            RequestContext.builder()
                .addRequestCallback(
                    httpRequest -> httpRequest.getHeaders().add(new HttpHeader(RANDOM_HEADER, "randomValue")))
                .build());

        assertNotNull(response);
        assertNotNull(response.data());
        assertInstanceOf(String.class, response.data());
        assertEquals("42", response.data());
        assertEquals("randomValue", response.getHeaderValue("randomHeader"));
    }

    @Test
    public void requestContextSetsAHeader() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService service
            = TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        HttpBinJSON response = service.put(getServerUri(isSecure()), 42,
            RequestContext.builder()
                .addRequestCallback(httpRequest -> httpRequest.getHeaders()
                    .add(new HttpHeader(RANDOM_HEADER, "randomValue"))
                    .add(RANDOM_HEADER, "randomValue2"))
                .build());

        assertNotNull(response);
        assertNotNull(response.data());
        assertInstanceOf(String.class, response.data());
        assertEquals("42", response.data());
        assertListEquals(Arrays.asList("randomValue", "randomValue2"), response.getHeaderValues("randomHeader"));
    }

    @ParameterizedTest
    @MethodSource("voidDoesNotEagerlyReadResponseSupplier")
    public void voidDoesNotEagerlyReadResponse(
        BiConsumer<String, TestInterfaceClientImpl.TestInterfaceClientService> executable) {
        assertDoesNotThrow(() -> executable.accept(getServerUri(isSecure()),
            createService(TestInterfaceClientImpl.TestInterfaceClientService.class)));
    }

    private static Stream<BiConsumer<String, TestInterfaceClientImpl.TestInterfaceClientService>>
        voidDoesNotEagerlyReadResponseSupplier() {
        return Stream.of((uri, service28) -> service28.headvoid(uri), (uri, service28) -> service28.headVoid(uri),
            (uri, service28) -> service28.headResponseVoid(uri));
    }

    @Test
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/44746")
    public void canReceiveServerSentEvents() {
        final int[] i = { 0 };
        TestInterfaceClientImpl.TestInterfaceClientService service
            = createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        service.get(getServerUri(isSecure()), sse -> {
            String expected;
            String id;
            if (i[0] == 0) {
                expected = "first event";
                id = "1";
                Assertions.assertEquals("test stream", sse.getComment());
            } else {
                expected = "This is the second message, it";
                String line2 = "has two lines.";

                id = "2";
                Assertions.assertEquals(line2, sse.getData().get(1));
            }
            Assertions.assertEquals(expected, sse.getData().get(0));
            Assertions.assertEquals(id, sse.getId());
            if (++i[0] > 2) {
                fail("Should not have received more than two messages.");
            }
        }).close();

        assertEquals(2, i[0]);
    }

    /**
     * Tests that eagerly converting implementation HTTP headers to Client Core Headers is done.
     */
    @Test
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/44746")
    public void canRecognizeServerSentEvent() {
        BinaryData requestBody = BinaryData.fromString("test body");
        TestInterfaceClientImpl.TestInterfaceClientService service
            = createService(TestInterfaceClientImpl.TestInterfaceClientService.class);
        List<String> expected = Arrays.asList("YHOO", "+2", "10");

        try (Response<BinaryData> response
            = service.post(getServerUri(isSecure()), requestBody, sse -> assertEquals(expected, sse.getData()), null)) {
            assertNotNull(response.getValue());
            assertNotEquals(0, response.getValue().getLength());
            assertNotNull(response.getValue());
            assertEquals(String.join("\n", expected), response.getValue().toString());
        }
    }

    @Test
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/44746")
    public void onErrorServerSentEvents() throws IOException {
        TestInterfaceClientImpl.TestInterfaceClientService service
            = createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final int[] i = { 0 };
        service.get(getServerUri(isSecure()), new ServerSentEventListener() {
            @Override
            public void onEvent(ServerSentEvent sse) throws IOException {
                throw new IOException("test exception");
            }

            @Override
            public void onError(Throwable throwable) {
                assertEquals("test exception", throwable.getMessage());
                i[0]++;
            }
        }).close();

        assertEquals(1, i[0]);
    }

    @Test
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/44746")
    public void onRetryWithLastEventIdReceiveServerSentEvents() throws IOException {
        TestInterfaceClientImpl.TestInterfaceClientService service
            = createService(TestInterfaceClientImpl.TestInterfaceClientService.class);

        final int[] i = { 0 };
        service.get(getServerUri(isSecure()), new ServerSentEventListener() {
            @Override
            public void onEvent(ServerSentEvent sse) {
                i[0]++;
                if (i[0] == 1) {
                    assertEquals("test stream", sse.getComment());
                    assertEquals("first event", sse.getData().get(0));
                    assertEquals("1", sse.getId());
                } else if (i[0] == 2) {
                    assertTimeout(Duration.ofMillis(100L), () -> assertEquals("2", sse.getId()));
                    assertEquals("This is the second message, it", sse.getData().get(0));
                    assertEquals("has two lines.", sse.getData().get(1));
                }
                if (i[0] >= 3) {
                    fail("Should not have received more than two messages.");
                }
            }
        }).close();

        assertEquals(2, i[0]);
    }

    /**
     * Test throws Runtime exception for no listener attached.
     */
    @Test
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/44746")
    public void throwsExceptionForNoListener() {
        TestInterfaceClientImpl.TestInterfaceClientService service
            = createService(TestInterfaceClientImpl.TestInterfaceClientService.class);
        BinaryData requestBody = BinaryData.fromString("test body");

        assertThrows(RuntimeException.class, () -> service.put(getServerUri(isSecure()), requestBody, null).close());
    }

    @Test
    public void bodyIsPresentWhenNoBodyHandlingOptionIsSet() {
        TestInterfaceClientImpl.TestInterfaceClientService service
            = createService(TestInterfaceClientImpl.TestInterfaceClientService.class);
        HttpBinJSON httpBinJSON = service.put(getServerUri(isSecure()), 42, null);

        assertNotNull(httpBinJSON);

        try (Response<HttpBinJSON> response = service.putResponse(getServerUri(isSecure()), 42, null)) {
            assertNotNull(response.getValue());
        }
    }

    @Test
    public void queryParamsRequest() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();

        TestInterfaceClientImpl.TestInterfaceClientService service
            = TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        final HttpBinJSON json = service.get1(getRequestUri(), "variableValue");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything", json.uri().substring(0, json.uri().indexOf('?')));

        Map<String, List<String>> queryParams = json.queryParams();

        assertNotNull(queryParams);
        assertEquals(3, queryParams.size());
        assertEquals(1, queryParams.get("constantParam1").size());
        assertEquals("constantValue1", queryParams.get("constantParam1").get(0));
        assertEquals(1, queryParams.get("constantParam2").size());
        assertEquals("constantValue2", queryParams.get("constantParam2").get(0));
        assertEquals(1, queryParams.get("variableParam").size());
        assertEquals("variableValue", queryParams.get("variableParam").get(0));
    }

    @Test
    public void queryParamsRequestWithMultipleValuesForSameName() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();

        TestInterfaceClientImpl.TestInterfaceClientService service
            = TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        final HttpBinJSON json = service.get2(getRequestUri(), "variableValue");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything", json.uri().substring(0, json.uri().indexOf('?')));

        Map<String, List<String>> queryParams = json.queryParams();

        assertNotNull(queryParams);
        assertEquals(1, queryParams.size());
        assertEquals("constantValue1", queryParams.get("param").get(0));
        assertEquals("constantValue2", queryParams.get("param").get(1));
        // Assert that same key name static param not overwritten
        assertEquals(2, queryParams.get("param").size());
    }

    @Test
    public void queryParamsRequestWithMultipleValuesForSameNameAndValueArray() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();

        TestInterfaceClientImpl.TestInterfaceClientService service
            = TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON json = service.get3(getRequestUri(), "variableValue1,variableValue2,variableValue3");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything", json.uri().substring(0, json.uri().indexOf('?')));

        Map<String, List<String>> queryParams = json.queryParams();

        assertNotNull(queryParams);
        assertEquals(1, queryParams.size());
        // Assert that static value same key name query param not overwritten
        assertEquals(2, queryParams.get("param").size());
        assertEquals("constantValue1,constantValue2", queryParams.get("param").get(0));
        assertEquals("constantValue3", queryParams.get("param").get(1));
    }

    @Test
    public void queryParamsRequestWithEmptyValues() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();

        TestInterfaceClientImpl.TestInterfaceClientService service
            = TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        final HttpBinJSON json = service.get4(getRequestUri());

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything", json.uri().substring(0, json.uri().indexOf('?')));

        Map<String, List<String>> queryParams = json.queryParams();

        assertNotNull(queryParams);
        assertEquals(1, queryParams.size());
        assertTrue(queryParams.containsKey("queryparamwithequalsandnovalue"));
        assertNull(queryParams.get("queryparamwithnoequals"));
    }

    @Test
    public void queryParamsRequestWithMoreThanOneEqualsSign() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();

        TestInterfaceClientImpl.TestInterfaceClientService service
            = TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        final HttpBinJSON json = service.get5(getRequestUri());

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything", json.uri().substring(0, json.uri().indexOf('?')));

        Map<String, List<String>> queryParams = json.queryParams();

        assertNotNull(queryParams);
        assertEquals(1, queryParams.size());
        assertTrue(json.uri().substring(json.uri().indexOf('?')).contains("some=value"),
            "Expected URI query to contain 'some=value', but it didn't and was: " + json.uri());
    }

    @Test
    public void queryParamsRequestWithEmptyName() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();

        TestInterfaceClientImpl.TestInterfaceClientService service
            = TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        final HttpBinJSON json = service.get6(getRequestUri());

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything", json.uri());

        Map<String, List<String>> queryParams = json.queryParams();

        assertNull(queryParams);

        final HttpBinJSON json7 = service.get7(getRequestUri());

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything", json7.uri());

        Map<String, List<String>> queryParams7 = json.queryParams();

        assertNull(queryParams7);
    }

    // Helpers
    @SuppressWarnings("unchecked")
    protected <T> T createService(Class<T> serviceClass) {
        final HttpClient httpClient = getHttpClient();
        final HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(httpClient).build();

        try {
            // Invoke getNewInstance(HttpPipeline) using reflection
            return (T) serviceClass.getMethod("getNewInstance", HttpPipeline.class).invoke(null, httpPipeline);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to create service instance for " + serviceClass.getName(), e);
        }
    }

    // Helpers
    private static void assertListEquals(List<?> source, List<?> target) {
        if (source != null && target != null) {
            assertEquals(source.size(), target.size());

            for (int i = 0; i < source.size(); i++) {
                assertEquals(source.get(i), target.get(i));
            }
        } else if (source != null || target != null) {
            fail("One list is null but the other is not.");
        }
    }

    /**
     * Get a flag indicating if communication should be secured or not (https or http).
     *
     * @return A flag indicating if communication should be secured or not (https or http).
     */
    private boolean isSecure() {
        return false;
    }

    private HttpClient getHttpClient() {
        return new OkHttpHttpClientProvider().getSharedInstance();
    }

    private String getRequestUri() {
        return getServerUri(isSecure());
    }

    /**
     * Gets the dynamic URI the server is using to properly route the request.
     *
     * @param secure Flag indicating if the URI should be for a secure connection or not.
     * @return The URI the server is using.
     */
    private String getServerUri(boolean secure) {
        return secure ? server.getHttpsUri() : server.getUri();
    }

    private int getPort() {
        return server.getPort();
    }

    private String getRequestScheme() {
        return isSecure() ? "https" : "http";
    }

    private static void assertMatchWithHttpOrHttps(String uri1, String uri2) {
        final String s1 = "http://" + uri1;

        if (s1.equalsIgnoreCase(uri2)) {
            return;
        }

        final String s2 = "https://" + uri1;

        if (s2.equalsIgnoreCase(uri2)) {
            return;
        }

        fail("'" + uri2 + "' does not match with '" + s1 + "' or '" + s2 + "'.");
    }

    private static Map<String, String> parseExceptionContext(Throwable ex) throws IOException {
        int jsonPartStart = ex.getMessage().indexOf(";");
        assertTrue(jsonPartStart > 0, "Expected JSON part in the exception message");
        try (JsonReader reader = JsonReader.fromString(ex.getMessage().substring(jsonPartStart + 1))) {
            return reader.readMap(JsonReader::getString);
        }
    }
}
