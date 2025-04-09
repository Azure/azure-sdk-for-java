// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.test;

import io.clientcore.annotation.processor.test.implementation.TestInterfaceClientImpl;
import io.clientcore.annotation.processor.test.implementation.models.Foo;
import io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.shared.HttpClientTestsServer;
import io.clientcore.core.shared.LocalTestServer;
import io.clientcore.http.okhttp3.OkHttpHttpClientProvider;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class TestInterfaceServiceClientGenerationTests {
    private static LocalTestServer server;
    @BeforeAll
    public static void startTestServer() {
        server = HttpClientTestsServer.getHttpClientTestsServer();

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

        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        assertNotNull(testInterface);
    }

    @Test
    public void testGetFoo() {
        String wireValue
            =
            "{\"bar\":\"hello.world\",\"baz\":[\"hello\",\"hello.world\"],\"qux\":{\"a.b\":\"c.d\",\"bar.a\":\"ttyy\",\"bar.b\":\"uuzz\",\"hello\":\"world\"},\"additionalProperties\":{\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}}";

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request ->
            new MockHttpResponse(request, 200, BinaryData.fromString(wireValue))).build();

        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        assertNotNull(testInterface);

        // test getFoo method
        Response<Foo> response = testInterface.getFoo("key", "label", "sync-token-value");
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
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        final byte[] result = testInterface.getByteArray(getServerUri(false));

        assertNotNull(result);
        assertEquals(100, result.length);
    }

    /**
     * Tests that the response body is correctly returned as a byte array.
     */
    @Test
    @Disabled("Disabled until we confirm the behavior of the HostParam annotation")
    public void requestWithByteArrayReturnTypeAndParameterizedHostAndPath() {
        //https://github.com/Azure/azure-sdk-for-java/issues/44298
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        final byte[] result
            = testInterface.getByteArray("http", "localhost:" + server.getHttpPort(), 100);

        assertNotNull(result);
        assertEquals(result.length, 100);
    }

    /**
     * Tests that a response with no return type is correctly handled.
     */
    @Test
    public void getRequestWithNoReturn() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        assertDoesNotThrow(() -> testInterface.getNothing(getServerUri(false)));
    }

    @Test
    public void getRequestWithAnything() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        final HttpBinJSON json = testInterface.getAnything(getServerUri(false));

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything", json.uri());
    }

    @Test
    public void getRequestWithAnythingWithPlus() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        final HttpBinJSON json = testInterface.getAnythingWithPlus(getServerUri(false));

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with+plus", json.uri());
    }

    @Test
    public void getRequestWithAnythingWithPathParam() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        final HttpBinJSON json
            = testInterface.getAnythingWithPathParam(getServerUri(false), "withpathparam");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/withpathparam", json.uri());
    }

    @Test
    public void getRequestWithAnythingWithPathParamWithSpace() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        final HttpBinJSON json
            = testInterface.getAnythingWithPathParam(getServerUri(false), "with path param");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with path param", json.uri());
    }

    @Test
    public void getRequestWithAnythingWithPathParamWithPlus() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        final HttpBinJSON json
            = testInterface.getAnythingWithPathParam(getServerUri(false), "with+path+param");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with+path+param", json.uri());
    }

    @Test
    public void getRequestWithAnythingWithEncodedPathParam() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        final HttpBinJSON json
            = testInterface.getAnythingWithEncodedPathParam(getServerUri(false), "withpathparam");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/withpathparam", json.uri());
    }

    @Test
    public void getRequestWithAnythingWithEncodedPathParamWithPercent20() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        final HttpBinJSON json
            = testInterface.getAnythingWithEncodedPathParam(getServerUri(false), "with%20path%20param");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with path param", json.uri());
    }

    @Test
    public void getRequestWithAnythingWithEncodedPathParamWithPlus() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        final HttpBinJSON json
            = testInterface.getAnythingWithEncodedPathParam(getServerUri(false), "with+path+param");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with+path+param", json.uri());
    }

    @Test
    public void getRequestWithQueryParametersAndAnything() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        final HttpBinJSON json
            = testInterface.getAnything(getServerUri(false), "A", 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything?a=A&b=15", json.uri());
    }

    @Test
    public void getRequestWithQueryParametersAndAnythingWithPercent20() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        final HttpBinJSON json
            = testInterface.getAnything(getServerUri(false), "A%20Z", 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything?a=A%2520Z&b=15", json.uri());
    }

    @Test
    public void getRequestWithQueryParametersAndAnythingWithEncodedWithPercent20() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        final HttpBinJSON json
            = testInterface.getAnythingWithEncoded(getServerUri(false), "x%20y", 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything?a=x y&b=15", json.uri());
    }

    @Test
    public void getRequestWithNullQueryParameter() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        final HttpBinJSON json
            = testInterface.getAnything(getServerUri(false), null, 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything?b=15", json.uri());
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithEmptyBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        Response<HttpBinJSON> response = testInterface
            .putWithHeaderApplicationOctetStreamContentTypeAndStringBody(getServerUri(false), "");
        assertNotNull(response);
        assertEquals("application/octet-stream",
            response.getRequest().getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));

        assertEquals("", response.getValue().data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithEmptyBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface
            .putWithHeaderApplicationJsonContentTypeAndByteArrayBody(getServerUri(false), new byte[0]);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndStringBodyWithNullBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface.putWithNoContentTypeAndStringBody(getServerUri(false), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndStringBodyWithEmptyBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface.putWithNoContentTypeAndStringBody(getServerUri(false), "");

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndStringBodyWithNonEmptyBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface.putWithNoContentTypeAndStringBody(getServerUri(false), "hello");

        assertEquals("hello", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndByteArrayBodyWithNullBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface.putWithNoContentTypeAndByteArrayBody(getServerUri(false), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndByteArrayBodyWithEmptyBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface.putWithNoContentTypeAndByteArrayBody(getServerUri(false), new byte[0]);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndByteArrayBodyWithNonEmptyBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface.putWithNoContentTypeAndByteArrayBody(getServerUri(false),
            new byte[] { 0, 1, 2, 3, 4 });

        assertEquals(new String(new byte[] { 0, 1, 2, 3, 4 }), result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithNullBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface
            .putWithHeaderApplicationJsonContentTypeAndStringBody(getServerUri(false), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithEmptyBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface.putWithHeaderApplicationJsonContentTypeAndStringBody(getServerUri(false), "");

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithNonEmptyBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface
            .putWithHeaderApplicationJsonContentTypeAndStringBody(getServerUri(false), "soups and stuff");

        assertEquals("soups and stuff", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithNullBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface
            .putWithHeaderApplicationJsonContentTypeAndByteArrayBody(getServerUri(false), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithNonEmptyBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        final byte[] requestBody = new byte[] { 0, 1, 2, 3, 4 };

        final HttpBinJSON result = testInterface
            .putWithHeaderApplicationJsonContentTypeAndByteArrayBody(getServerUri(false), requestBody);
        String expected = new String(requestBody, StandardCharsets.UTF_8);
        assertEquals( expected, result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithNullBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface
            .putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(getServerUri(false), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithEmptyBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface
            .putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(getServerUri(false), "");

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithNonEmptyBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);
        String requestBody = "soups and stuff";
        final HttpBinJSON result = testInterface
            .putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(getServerUri(false), requestBody);

        assertEquals("soups and stuff", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithNullBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        Response<HttpBinJSON> response = testInterface
            .putWithHeaderApplicationOctetStreamContentTypeAndStringBody(getServerUri(false), null);
        assertNotNull(response);
        assertEquals("application/octet-stream", response.getRequest().getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));

        assertEquals("", response.getValue().data());
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithNonEmptyBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final Response<HttpBinJSON> response = testInterface
            .putWithHeaderApplicationOctetStreamContentTypeAndStringBody(getServerUri(false), "penguins");
        assertNotNull(response);
        assertEquals("application/octet-stream", response.getRequest().getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));

        assertEquals("penguins", response.getValue().data());
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithNullBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface
            .putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(getServerUri(false), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithEmptyBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface
            .putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(getServerUri(false), new byte[0]);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithNonEmptyBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface.putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(
            getServerUri(false), new byte[] { 0, 1, 2, 3, 4 });

        assertEquals(new String(new byte[] { 0, 1, 2, 3, 4 }), result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithNullBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final Response<HttpBinJSON> response = testInterface
            .putWithBodyParamApplicationJsonContentTypeAndStringBody(getServerUri(false), null);
        assertNotNull(response);
        assertEquals("application/json", response.getRequest().getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
        assertEquals("", response.getValue().data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithEmptyBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final Response<HttpBinJSON> response = testInterface
            .putWithBodyParamApplicationJsonContentTypeAndStringBody(getServerUri(false), "");
        assertNotNull(response);
        assertEquals("application/json", response.getRequest().getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
        assertEquals("", response.getValue().data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithNonEmptyBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final Response<HttpBinJSON> response = testInterface
            .putWithBodyParamApplicationJsonContentTypeAndStringBody(getServerUri(false), "soups and stuff");
        assertNotNull(response);
        assertEquals("application/json", response.getRequest().getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
        assertEquals("soups and stuff", response.getValue().data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithNullBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface
            .putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(getServerUri(false), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithEmptyBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface
            .putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(getServerUri(false), "");

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithNonEmptyBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface
            .putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(getServerUri(false), "soups and stuff");

        assertEquals("soups and stuff", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithNullBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface
            .putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(getServerUri(false), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithEmptyBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface
            .putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(getServerUri(false), new byte[0]);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithNonEmptyBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface
            .putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(getServerUri(false), new byte[] { 0, 1, 2, 3, 4 });

        assertEquals( new String(new byte[] { 0, 1, 2, 3, 4 }), result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithNullBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface
            .putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(getServerUri(false), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithEmptyBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface
            .putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(getServerUri(false), "");

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithNonEmptyBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface
            .putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(getServerUri(false), "penguins");

        assertEquals("penguins", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithNullBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface
            .putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(getServerUri(false), null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithEmptyBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface
            .putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(getServerUri(false), new byte[0]);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithNonEmptyBody() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientImpl.TestInterfaceClientService testInterface =
            TestInterfaceClientImpl.TestInterfaceClientService.getNewInstance(pipeline);

        final HttpBinJSON result = testInterface.putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(
            getServerUri(false), new byte[] { 0, 1, 2, 3, 4 });

        assertEquals(new String(new byte[] { 0, 1, 2, 3, 4 }), result.data());
    }

    private HttpClient getHttpClient() {
        return new OkHttpHttpClientProvider().getSharedInstance();
    }

    private String getServerUri(boolean secure) {
        return secure ? server.getHttpsUri() : server.getHttpUri();
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

}
