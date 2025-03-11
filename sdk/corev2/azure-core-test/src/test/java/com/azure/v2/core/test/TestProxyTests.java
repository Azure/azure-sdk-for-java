// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.test;

import com.azure.v2.core.test.annotation.DoNotRecord;
import com.azure.v2.core.test.annotation.RecordWithoutRequestBody;
import com.azure.v2.core.test.implementation.TestingHelpers;
import com.azure.v2.core.test.models.CustomMatcher;
import com.azure.v2.core.test.models.TestProxySanitizer;
import com.azure.v2.core.test.models.TestProxySanitizerType;
import com.azure.v2.core.test.utils.HttpUrlConnectionHttpClient;
import com.azure.v2.core.test.utils.TestProxyTestServer;
import com.azure.v2.core.test.utils.TestProxyUtils;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.http.pipeline.HttpRedirectPolicy;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonWriter;
import io.clientcore.core.models.binarydata.BinaryData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for testing Test proxy functionality of record, playback and redaction.
 */

// These tests override the environment variable so they can test playback and record in the same test run.
// This strategy fails if we are in a LIVE test mode, so we'll just skip these entirely if that's the case.
@DisabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "(LIVE|live|Live)")
@DisabledIfSystemProperty(named = "AZURE_TEST_MODE", matches = "(LIVE|live|Live)")
public class TestProxyTests extends TestBase {
    public static final String TEST_DATA = "{\"test\":\"proxy\"}";
    static TestProxyTestServer server;

    private static final List<TestProxySanitizer> CUSTOM_SANITIZER = new ArrayList<>();

    public static final String REDACTED = "REDACTED";
    private static final HttpHeaderName OCP_APIM_SUBSCRIPTION_KEY
        = HttpHeaderName.fromString("Ocp-Apim-Subscription-Key");

    static {
        CUSTOM_SANITIZER.add(new TestProxySanitizer("$..modelId", null, REDACTED, TestProxySanitizerType.BODY_KEY));
        CUSTOM_SANITIZER.add(new TestProxySanitizer("TableName\\\"*:*\\\"(?<tablename>.*)\\\"", REDACTED,
            TestProxySanitizerType.BODY_REGEX).setGroupForReplace("tablename"));
    }

    @BeforeAll
    public static void setupClass() {
        server = new TestProxyTestServer();
    }

    @AfterAll
    public static void teardownClass() throws IOException {
        server.close();
    }

    @Test
    @Tag("Record")
    public void testBasicRecord() throws IOException {
        HttpUrlConnectionHttpClient client = new HttpUrlConnectionHttpClient();
        HttpPipeline pipeline
            = new HttpPipelineBuilder().httpClient(client).addPolicy(interceptorManager.getRecordPolicy()).build();

        testResourceNamer.randomName("test", 10);
        testResourceNamer.now();
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.GET).setUri("http://localhost:" + server.port());

        try (Response<?> response = pipeline.send(request)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    @Tag("Playback")
    public void testOrdering() {
        // this proves that regardless of where in your test method you might try and get a variable it works.
        String name = testResourceNamer.randomName("test", 10);
        assertEquals("test32950", name);
    }

    @Test
    @Tag("Record")
    @DoNotRecord
    public void testDoNotRecord() {
        testResourceNamer.now();
    }

    @Test
    @Tag("Playback")
    @DoNotRecord
    public void testDoNotPlayback() {
        testResourceNamer.now();
    }

    @Test
    @Tag("Playback")
    public void testMismatch() {
        HttpClient client = interceptorManager.getPlaybackClient();

        HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.GET).setUri("http://localhost:" + server.port() + "/first/path");
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> client.send(request).close());
        assertTrue(thrown.getMessage().contains("Uri doesn't match"));
    }

    @Test
    @Tag("Record")
    @RecordWithoutRequestBody
    public void testRecordWithPath() throws IOException {
        HttpUrlConnectionHttpClient client = new HttpUrlConnectionHttpClient();
        HttpPipeline pipeline
            = new HttpPipelineBuilder().httpClient(client).addPolicy(interceptorManager.getRecordPolicy()).build();

        testResourceNamer.randomName("test", 10);
        testResourceNamer.now();
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.POST)
            .setUri("http://localhost:" + server.port() + "/first/path")
            .setBody(BinaryData.fromString(TEST_DATA));
        request.getHeaders()
            .set(HttpHeaderName.CONTENT_TYPE, "application/json")
            .set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(TEST_DATA.length()));

        try (Response<?> response = pipeline.send(request)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    @Tag("Record")
    public void testRecordWithHeaders() throws IOException {
        HttpUrlConnectionHttpClient client = new HttpUrlConnectionHttpClient();
        HttpPipeline pipeline
            = new HttpPipelineBuilder().httpClient(client).addPolicy(interceptorManager.getRecordPolicy()).build();

        testResourceNamer.randomName("test", 10);
        testResourceNamer.now();
        HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.GET).setUri("http://localhost:" + server.port() + "/echoheaders");
        request.getHeaders()
            .set(HttpHeaderName.fromString("header1"), "value1")
            .set(HttpHeaderName.fromString("header2"), "value2");

        try (Response<?> response = pipeline.send(request)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    @Tag("Playback")
    public void testPlayback() throws IOException {
        HttpClient client = interceptorManager.getPlaybackClient();
        interceptorManager.addMatchers(new CustomMatcher().setExcludedHeaders(Collections.singletonList("Connection")));

        HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.GET).setUri("http://localhost:" + server.port() + "/first/path");
        // For this test set an Accept header as most HttpClients will use a default which could result in this
        // test being flaky
        request.getHeaders().set(HttpHeaderName.ACCEPT, "*/*");

        try (Response<BinaryData> response = client.send(request)) {
            assertEquals("first path", response.getValue().toString());
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    @Tag("Live")
    public void testCannotGetPlaybackClient() {
        RuntimeException thrown
            = assertThrows(IllegalStateException.class, () -> interceptorManager.getPlaybackClient());
        assertEquals("A playback client can only be requested in PLAYBACK mode.", thrown.getMessage());
    }

    @Test
    @Tag("Live")
    public void testCannotGetRecordPolicy() {
        RuntimeException thrown = assertThrows(IllegalStateException.class, () -> interceptorManager.getRecordPolicy());
        assertEquals("A recording policy can only be requested in RECORD mode.", thrown.getMessage());
    }

    @Test
    @Tag("Playback")
    public void testRecordWithRedaction() throws IOException {
        interceptorManager.addSanitizers(CUSTOM_SANITIZER);
        interceptorManager.addMatchers(new CustomMatcher().setExcludedHeaders(Collections.singletonList("Connection")));
        HttpClient client = interceptorManager.getPlaybackClient();

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.GET).setUri("http://localhost:" + server.port() + "/fr/path/1");
        request.getHeaders()
            .set(OCP_APIM_SUBSCRIPTION_KEY, "SECRET_API_KEY")
            .set(HttpHeaderName.CONTENT_TYPE, "application/json")
            // For this test set an Accept header as most HttpClients will use a default which could result in this
            // test being flaky
            .set(HttpHeaderName.ACCEPT, "*/*");

        try (Response<?> response = pipeline.send(request)) {
            assertEquals(200, response.getStatusCode());
            RecordedTestProxyData recordedTestProxyData = readDataFromFile();
            RecordedTestProxyData.TestProxyDataRecord record = recordedTestProxyData.getTestProxyDataRecords().get(0);
            // default sanitizers
            assertEquals("http://REDACTED/fr/path/1", record.getUri());
            assertEquals(REDACTED, record.getHeaders().get("Ocp-Apim-Subscription-Key"));
            assertTrue(record.getResponseHeaders()
                .get("Operation-Location")
                .startsWith("https://REDACTED/fr/models//905a58f9-131e-42b8-8410-493ab1517d62"));
            // custom sanitizers
            assertEquals(REDACTED, record.getResponse().get("modelId"));
            assertEquals(REDACTED, record.getResponse().get("client_secret"));
        }
    }

    @Test
    @Tag("Playback")
    public void testPlaybackWithRedaction() throws IOException {
        interceptorManager.addSanitizers(CUSTOM_SANITIZER);
        interceptorManager.addMatchers(Collections.singletonList(
            new CustomMatcher().setExcludedHeaders(Arrays.asList("Ocp-Apim-Subscription-Key", "Connection"))));
        HttpClient client = interceptorManager.getPlaybackClient();

        HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.GET).setUri("http://localhost:" + server.port() + "/fr/models");
        request.getHeaders()
            .set(OCP_APIM_SUBSCRIPTION_KEY, "SECRET_API_KEY")
            .set(HttpHeaderName.CONTENT_TYPE, "application/json")
            // For this test set an Accept header as most HttpClients will use a default which could result in this
            // test being flaky
            .set(HttpHeaderName.ACCEPT, "*/*");

        try (Response<?> response = client.send(request)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    @Tag("Playback")
    public void testBodyRegexRedactRecord() throws IOException {
        HttpClient client = interceptorManager.getPlaybackClient();

        interceptorManager.addSanitizers(CUSTOM_SANITIZER);
        interceptorManager.addMatchers(new CustomMatcher().setHeadersKeyOnlyMatch(Collections.singletonList("Accept"))
            .setExcludedHeaders(Collections.singletonList("Connection")));

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.GET).setUri("http://localhost:" + server.port() + "/fr/path/2");
        request.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");

        try (Response<?> response = pipeline.send(request)) {
            assertEquals(200, response.getStatusCode());
        }

        RecordedTestProxyData recordedTestProxyData = readDataFromFile();
        RecordedTestProxyData.TestProxyDataRecord record = recordedTestProxyData.getTestProxyDataRecords().get(0);
        // default regex sanitizers
        assertEquals("http://REDACTED/fr/path/2", record.getUri());

        // user delegation sanitizers
        assertTrue(record.getResponse()
            .get("Body")
            .contains("<UserDelegationKey><SignedTid>REDACTED</SignedTid></UserDelegationKey>"));
        assertTrue(record.getResponse().get("primaryKey").contains("<PrimaryKey>REDACTED</PrimaryKey>"));

        // custom body regex
        assertEquals(REDACTED, record.getResponse().get("TableName"));
    }

    @Test
    @Tag("Playback")
    public void testRedactRequestBodyRegex() throws IOException {

        HttpClient client = interceptorManager.getPlaybackClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();
        interceptorManager.addMatchers(new CustomMatcher().setHeadersKeyOnlyMatch(Collections.singletonList("Accept"))
            .setExcludedHeaders(Collections.singletonList("Connection")));

        //        HttpClient client = new HttpURLConnectionHttpClient();
        //        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).policies(interceptorManager.getRecordPolicy()).build();

        HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.POST).setUri("http://localhost:" + server.port() + "/post");
        request.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/x-www-form-urlencoded");
        request.setBody(BinaryData.fromString("first_value=value&client_secret=aVerySecretSecret&other=value&is=cool"));

        try (Response<?> response = pipeline.send(request)) {
            assertEquals(200, response.getStatusCode());
        }

        RecordedTestProxyData recordedTestProxyData = readDataFromFile();
        RecordedTestProxyData.TestProxyDataRecord record = recordedTestProxyData.getTestProxyDataRecords().get(0);

        assertEquals("first_value=value&client_secret=REDACTED&other=value&is=cool", record.getRequestBody());

    }

    @Test
    @Tag("Live")
    public void canGetTestProxyVersion() {
        String version = TestProxyUtils.getTestProxyVersion(this.getTestClassPath());
        assertNotNull(version);
    }

    @Test
    @Tag("Record")
    public void testResetTestProxyData() throws IOException {
        HttpUrlConnectionHttpClient client = new HttpUrlConnectionHttpClient();

        final HttpPipeline pipeline
            = new HttpPipelineBuilder().httpClient(client).addPolicy(interceptorManager.getRecordPolicy()).build();

        try (Response<?> response
            = pipeline.send(new HttpRequest().setMethod(HttpMethod.GET).setUri("http://localhost:" + server.port()))) {
            assertEquals(200, response.getStatusCode());
            HttpHeaders headers = response.getRequest().getHeaders();
            assertNull(headers.get(HttpHeaderName.fromString("x-recording-upstream-base-uri")));
            assertNull(headers.get(HttpHeaderName.fromString("x-recording-mode")));
            assertNull(headers.get(HttpHeaderName.fromString("x-recording-id")));
            assertNull(headers.get(HttpHeaderName.fromString("x-recording-skip")));
        }
    }

    @Test
    @Tag("Record")
    public void testRecordWithRedirect() throws IOException {
        HttpUrlConnectionHttpClient client = new HttpUrlConnectionHttpClient();

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client)
            .addPolicy(new HttpRedirectPolicy())
            .addPolicy(interceptorManager.getRecordPolicy())
            .build();

        HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.GET).setUri("http://localhost:" + server.port() + "/getRedirect");

        try (Response<?> response = pipeline.send(request)) {
            assertEquals(200, response.getStatusCode());

            assertEquals("http://localhost:" + server.port() + "/echoheaders",
                response.getRequest().getUri().toString());
            assertNull(
                response.getRequest().getHeaders().get(HttpHeaderName.fromString("x-recording-upstream-base-uri")));
        }
    }

    private RecordedTestProxyData readDataFromFile() {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(interceptorManager.getRecordingFileLocation()));
            JsonReader jsonReader = JsonReader.fromReader(reader)) {
            return RecordedTestProxyData.fromJson(jsonReader);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    static class RecordedTestProxyData implements JsonSerializable<RecordedTestProxyData> {
        private final LinkedList<TestProxyDataRecord> testProxyDataRecords;

        RecordedTestProxyData() {
            testProxyDataRecords = new LinkedList<>();
        }

        public LinkedList<TestProxyDataRecord> getTestProxyDataRecords() {
            return testProxyDataRecords;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject()
                .writeArrayField("Entries", testProxyDataRecords, JsonWriter::writeJson)
                .writeEndObject();
        }

        /**
         * Deserializes an instance of RecordedTestProxyData from the input JSON.
         *
         * @param jsonReader The JSON reader to deserialize the data from.
         * @return An instance of RecordedTestProxyData deserialized from the JSON.
         * @throws IOException If the JSON reader encounters an error while reading the JSON.
         */
        public static RecordedTestProxyData fromJson(JsonReader jsonReader) throws IOException {
            return TestingHelpers.readObject(jsonReader, RecordedTestProxyData::new,
                (recordedData, fieldName, reader) -> {
                    if ("Entries".equals(fieldName)) {
                        recordedData.testProxyDataRecords.addAll(reader.readArray(TestProxyDataRecord::fromJson));
                    } else {
                        reader.skipChildren();
                    }
                });
        }

        static class TestProxyDataRecord implements JsonSerializable<TestProxyDataRecord> {
            private String method;
            private String uri;
            private Map<String, String> headers;
            private Map<String, String> response;
            private Map<String, String> responseHeaders;
            private String requestBody;

            public String getMethod() {
                return method;
            }

            public String getUri() {
                return uri;
            }

            public Map<String, String> getHeaders() {
                return headers;
            }

            public Map<String, String> getResponse() {
                return response;
            }

            public Map<String, String> getResponseHeaders() {
                return responseHeaders;
            }

            public String getRequestBody() {
                return requestBody;
            }

            @Override
            public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
                return jsonWriter.writeStartObject()
                    .writeStringField("RequestMethod", method)
                    .writeStringField("RequestUri", uri)
                    .writeMapField("RequestHeaders", headers, JsonWriter::writeString)
                    .writeMapField("ResponseBody", response, JsonWriter::writeString)
                    .writeMapField("ResponseHeaders", responseHeaders, JsonWriter::writeString)
                    .writeStringField("RequestBody", requestBody)
                    .writeEndObject();
            }

            /**
             * Deserializes an instance of TestProxyDataRecord from the input JSON.
             *
             * @param jsonReader The JSON reader to deserialize the data from.
             * @return An instance of TestProxyDataRecord deserialized from the JSON.
             * @throws IOException If the JSON reader encounters an error while reading the JSON.
             */
            public static TestProxyDataRecord fromJson(JsonReader jsonReader) throws IOException {
                return TestingHelpers.readObject(jsonReader, TestProxyDataRecord::new,
                    (dataRecord, fieldName, reader) -> {
                        if ("RequestMethod".equals(fieldName)) {
                            dataRecord.method = reader.getString();
                        } else if ("RequestUri".equals(fieldName)) {
                            dataRecord.uri = reader.getString();
                        } else if ("RequestHeaders".equals(fieldName)) {
                            dataRecord.headers = reader.readMap(JsonReader::getString);
                        } else if ("ResponseBody".equals(fieldName)) {
                            dataRecord.response = reader.readMap(JsonReader::getString);
                        } else if ("ResponseHeaders".equals(fieldName)) {
                            dataRecord.responseHeaders = reader.readMap(JsonReader::getString);
                        } else if ("RequestBody".equals(fieldName)) {
                            dataRecord.requestBody = reader.getString();
                        } else {
                            reader.skipChildren();
                        }
                    });
            }
        }
    }
}
