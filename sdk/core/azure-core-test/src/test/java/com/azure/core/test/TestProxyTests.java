// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.RedirectPolicy;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.test.http.TestProxyTestServer;
import com.azure.core.test.implementation.TestingHelpers;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.HttpURLConnectionHttpClient;
import com.azure.core.test.utils.TestProxyUtils;
import com.azure.core.util.Context;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
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
public class TestProxyTests extends TestProxyTestBase {
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
    public static void teardownClass() {
        server.close();
    }

    @Test
    @Tag("Record")
    public void testBasicRecord() {
        HttpURLConnectionHttpClient client = new HttpURLConnectionHttpClient();
        HttpPipeline pipeline
            = new HttpPipelineBuilder().httpClient(client).policies(interceptorManager.getRecordPolicy()).build();

        testResourceNamer.randomName("test", 10);
        testResourceNamer.now();
        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost:" + server.port());

        try (HttpResponse response = pipeline.sendSync(request, Context.NONE)) {
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

        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost:" + server.port() + "/first/path");
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> client.sendSync(request, Context.NONE));
        assertTrue(thrown.getMessage().contains("Uri doesn't match"));
    }

    @Test
    @Tag("Record")
    @RecordWithoutRequestBody
    public void testRecordWithPath() {
        HttpURLConnectionHttpClient client = new HttpURLConnectionHttpClient();
        HttpPipeline pipeline
            = new HttpPipelineBuilder().httpClient(client).policies(interceptorManager.getRecordPolicy()).build();

        testResourceNamer.randomName("test", 10);
        testResourceNamer.now();
        HttpRequest request
            = new HttpRequest(HttpMethod.POST, "http://localhost:" + server.port() + "/first/path").setBody(TEST_DATA)
                .setHeader(HttpHeaderName.CONTENT_TYPE, "application/json")
                .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(TEST_DATA.length()));

        try (HttpResponse response = pipeline.sendSync(request, Context.NONE)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    @Tag("Record")
    public void testRecordWithHeaders() {
        HttpURLConnectionHttpClient client = new HttpURLConnectionHttpClient();
        HttpPipeline pipeline
            = new HttpPipelineBuilder().httpClient(client).policies(interceptorManager.getRecordPolicy()).build();

        testResourceNamer.randomName("test", 10);
        testResourceNamer.now();
        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost:" + server.port() + "/echoheaders")
            .setHeader(HttpHeaderName.fromString("header1"), "value1")
            .setHeader(HttpHeaderName.fromString("header2"), "value2");

        try (HttpResponse response = pipeline.sendSync(request, Context.NONE)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    @Tag("Playback")
    public void testPlayback() {
        HttpClient client = interceptorManager.getPlaybackClient();
        interceptorManager.addMatchers(new CustomMatcher().setExcludedHeaders(Collections.singletonList("Connection")));

        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost:" + server.port() + "/first/path")
            // For this test set an Accept header as most HttpClients will use a default which could result in this
            // test being flaky
            .setHeader(HttpHeaderName.ACCEPT, "*/*");

        try (HttpResponse response = client.sendSync(request, Context.NONE)) {
            assertEquals("first path", response.getBodyAsBinaryData().toString());
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
    public void testRecordWithRedaction() {
        interceptorManager.addSanitizers(CUSTOM_SANITIZER);
        interceptorManager.addMatchers(new CustomMatcher().setExcludedHeaders(Collections.singletonList("Connection")));
        HttpClient client = interceptorManager.getPlaybackClient();

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost:" + server.port() + "/fr/path/1")
            .setHeader(OCP_APIM_SUBSCRIPTION_KEY, "SECRET_API_KEY")
            .setHeader(HttpHeaderName.CONTENT_TYPE, "application/json")
            // For this test set an Accept header as most HttpClients will use a default which could result in this
            // test being flaky
            .setHeader(HttpHeaderName.ACCEPT, "*/*");

        try (HttpResponse response = pipeline.sendSync(request, Context.NONE)) {

            assertEquals(response.getStatusCode(), 200);

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
    public void testPlaybackWithRedaction() {
        interceptorManager.addSanitizers(CUSTOM_SANITIZER);
        interceptorManager.addMatchers(Collections.singletonList(
            new CustomMatcher().setExcludedHeaders(Arrays.asList("Ocp-Apim-Subscription-Key", "Connection"))));
        HttpClient client = interceptorManager.getPlaybackClient();

        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost:" + server.port() + "/fr/models")
            .setHeader(OCP_APIM_SUBSCRIPTION_KEY, "SECRET_API_KEY")
            .setHeader(HttpHeaderName.CONTENT_TYPE, "application/json")
            // For this test set an Accept header as most HttpClients will use a default which could result in this
            // test being flaky
            .setHeader(HttpHeaderName.ACCEPT, "*/*");

        try (HttpResponse response = client.sendSync(request, Context.NONE)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    @Tag("Playback")
    public void testBodyRegexRedactRecord() {
        HttpClient client = interceptorManager.getPlaybackClient();

        interceptorManager.addSanitizers(CUSTOM_SANITIZER);
        interceptorManager.addMatchers(new CustomMatcher().setHeadersKeyOnlyMatch(Collections.singletonList("Accept"))
            .setExcludedHeaders(Collections.singletonList("Connection")));

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost:" + server.port() + "/fr/path/2");
        request.setHeader(HttpHeaderName.CONTENT_TYPE, "application/json");

        try (HttpResponse response = pipeline.sendSync(request, Context.NONE)) {
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
        assertEquals(record.getResponse().get("TableName"), REDACTED);
    }

    @Test
    @Tag("Playback")
    public void testRedactRequestBodyRegex() {

        HttpClient client = interceptorManager.getPlaybackClient();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).build();
        interceptorManager.addMatchers(new CustomMatcher().setHeadersKeyOnlyMatch(Collections.singletonList("Accept"))
            .setExcludedHeaders(Collections.singletonList("Connection")));

        //        HttpClient client = new HttpURLConnectionHttpClient();
        //        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).policies(interceptorManager.getRecordPolicy()).build();

        HttpRequest request = new HttpRequest(HttpMethod.POST, "http://localhost:" + server.port() + "/post");
        request.setHeader(HttpHeaderName.CONTENT_TYPE, "application/x-www-form-urlencoded");
        request.setBody("first_value=value&client_secret=aVerySecretSecret&other=value&is=cool");

        try (HttpResponse response = pipeline.sendSync(request, Context.NONE)) {
            assertEquals(200, response.getStatusCode());
        }

        RecordedTestProxyData recordedTestProxyData = readDataFromFile();
        RecordedTestProxyData.TestProxyDataRecord record = recordedTestProxyData.getTestProxyDataRecords().get(0);

        assertEquals(record.getRequestBody(), "first_value=value&client_secret=REDACTED&other=value&is=cool");

    }

    @Test
    @Tag("Live")
    public void canGetTestProxyVersion() {
        String version = TestProxyUtils.getTestProxyVersion(this.getTestClassPath());
        assertNotNull(version);
    }

    @Test
    @Tag("Record")
    public void testResetTestProxyData() {
        HttpURLConnectionHttpClient client = new HttpURLConnectionHttpClient();

        final HttpPipeline pipeline
            = new HttpPipelineBuilder().httpClient(client).policies(interceptorManager.getRecordPolicy()).build();

        try (HttpResponse response
            = pipeline.sendSync(new HttpRequest(HttpMethod.GET, "http://localhost:" + server.port()), Context.NONE)) {
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
    public void testRecordWithRedirect() {
        HttpURLConnectionHttpClient client = new HttpURLConnectionHttpClient();

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client)
            .policies(new RedirectPolicy(), interceptorManager.getRecordPolicy())
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost:" + server.port() + "/getRedirect");

        try (HttpResponse response = pipeline.sendSync(request, Context.NONE)) {
            assertEquals(200, response.getStatusCode());

            assertEquals("http://localhost:" + server.port() + "/echoheaders",
                response.getRequest().getUrl().toString());
            assertNull(
                response.getRequest().getHeaders().get(HttpHeaderName.fromString("x-recording-upstream-base-uri")));
        }
    }

    private RecordedTestProxyData readDataFromFile() {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(interceptorManager.getRecordingFileLocation()));
            JsonReader jsonReader = JsonProviders.createReader(reader)) {
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
