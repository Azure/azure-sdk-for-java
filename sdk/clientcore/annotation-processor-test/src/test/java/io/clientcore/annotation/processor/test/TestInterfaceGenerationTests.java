// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.test;

import io.clientcore.annotation.processor.test.implementation.TestInterfaceClientService;
import io.clientcore.annotation.processor.test.implementation.models.Foo;
import io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.models.ResponseBodyMode;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.shared.HttpClientTestsServer;
import io.clientcore.core.shared.LocalTestServer;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.models.binarydata.ByteArrayBinaryData;
import io.clientcore.core.models.binarydata.InputStreamBinaryData;
import io.clientcore.http.okhttp3.OkHttpHttpClientProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static io.clientcore.core.http.models.ResponseBodyMode.BUFFER;
import static io.clientcore.core.http.models.ResponseBodyMode.DESERIALIZE;
import static io.clientcore.core.http.models.ResponseBodyMode.IGNORE;
import static io.clientcore.core.http.models.ResponseBodyMode.STREAM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestInterfaceGenerationTests {
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

        TestInterfaceClientService testInterface = TestInterfaceClientService.getNewInstance(pipeline, null);
        assertNotNull(testInterface);
    }

    @Test
    public void testGetFoo() {
        String wireValue
            =
            "{\"bar\":\"hello.world\",\"baz\":[\"hello\",\"hello.world\"],\"qux\":{\"a.b\":\"c.d\",\"bar.a\":\"ttyy\",\"bar.b\":\"uuzz\",\"hello\":\"world\"},\"additionalProperties\":{\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}}";

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient((request) -> {
            // what is the default response body mode?
            request.setRequestOptions(new RequestOptions().setResponseBodyMode(ResponseBodyMode.DESERIALIZE));
            return new MockHttpResponse(request, 200,
                BinaryData.fromString(wireValue));
        }).build();

        TestInterfaceClientService testInterface = TestInterfaceClientService.getNewInstance(pipeline, null);
        assertNotNull(testInterface);

        // test getFoo method
        Response<Foo> response = testInterface.getFoo("key", "label", "sync-token-value");
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals(wireValue, response.getBody().toString());

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
    public void bodyIsEmptyWhenIgnoreBodyIsSet() throws IOException {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();

        TestInterfaceClientService testInterface = TestInterfaceClientService.getNewInstance(pipeline, null);
        assertNotNull(testInterface);
        RequestOptions requestOptions = new RequestOptions().setResponseBodyMode(IGNORE);
        HttpBinJSON httpBinJSON = testInterface.putConvenience(getServerUri(false), 42, requestOptions);

        assertNull(httpBinJSON);

        try (Response<HttpBinJSON> response = testInterface.putResponse(getServerUri(false), 42, requestOptions)) {
            assertNotNull(response.getBody());
            assertEquals(0, response.getBody().getLength());
            assertNull(response.getValue());
        }
    }

    @Test
    public void bodyIsEmptyWhenIgnoreBodyIsSetForStreamResponse() throws IOException {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientService testInterface = TestInterfaceClientService.getNewInstance(pipeline, null);
        assertNotNull(testInterface);
        RequestOptions requestOptions = new RequestOptions().setResponseBodyMode(IGNORE);
        HttpBinJSON httpBinJSON = testInterface.postStreamConvenience(getServerUri(false), 42, requestOptions);

        assertNull(httpBinJSON);

        try (
            Response<HttpBinJSON> response = testInterface.postStreamResponse(getServerUri(false), 42, requestOptions)) {
            assertNotNull(response.getBody());
            assertEquals(0, response.getBody().getLength());
            assertNull(response.getValue());
        }
    }

    @Test
    public void bodyIsStreamedWhenResponseBodyModeIndicatesIt() throws IOException {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientService testInterface = TestInterfaceClientService.getNewInstance(pipeline, null);
        assertNotNull(testInterface);
        RequestOptions requestOptions = new RequestOptions().setResponseBodyMode(STREAM);

        try (
            Response<HttpBinJSON> response = testInterface.postStreamResponse(getServerUri(false), 42, requestOptions)) {
            assertNotNull(response.getBody());
            assertNotEquals(0, response.getBody().getLength());
            assertTrue(response.getBody() instanceof InputStreamBinaryData);
        }
    }

    @Test
    public void bodyIsBufferedWhenResponseBodyModeIndicatesIt() throws IOException {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientService testInterface = TestInterfaceClientService.getNewInstance(pipeline, null);
        assertNotNull(testInterface);
        RequestOptions requestOptions = new RequestOptions().setResponseBodyMode(BUFFER);
        HttpBinJSON httpBinJSON = testInterface.postStreamConvenience(getServerUri(false), 42, requestOptions);

        assertNotNull(httpBinJSON);

        try (
            Response<HttpBinJSON> response = testInterface.postStreamResponse(getServerUri(false), 42, requestOptions)) {
            assertNotNull(response.getBody());
            assertNotEquals(0, response.getBody().getLength());
            assertTrue(response.getBody() instanceof ByteArrayBinaryData);
        }
    }

    @Test
    public void bodyIsDeserializedWhenResponseBodyModeIndicatesIt() throws IOException {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(getHttpClient()).build();
        TestInterfaceClientService testInterface = TestInterfaceClientService.getNewInstance(pipeline, null);
        assertNotNull(testInterface);
        RequestOptions requestOptions = new RequestOptions().setResponseBodyMode(DESERIALIZE);
        HttpBinJSON httpBinJSON = testInterface.postStreamConvenience(getServerUri(false), 42, requestOptions);

        assertNotNull(httpBinJSON);

        try (
            Response<HttpBinJSON> response = testInterface.postStreamResponse(getServerUri(false), 42, requestOptions)) {
            assertNotNull(response.getBody());
            assertNotEquals(0, response.getBody().getLength());
            assertNotNull(response.getValue());
        }
    }

    protected HttpClient getHttpClient() {
        return new OkHttpHttpClientProvider().getSharedInstance();
    }

    protected String getServerUri(boolean secure) {
        return secure ? server.getHttpsUri() : server.getHttpUri();
    }
}
