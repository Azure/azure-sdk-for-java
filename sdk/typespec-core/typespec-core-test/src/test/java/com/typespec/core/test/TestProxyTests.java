// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test;

import com.typespec.core.http.HttpClient;
import com.typespec.core.http.HttpHeaderName;
import com.typespec.core.http.HttpHeaders;
import com.typespec.core.http.HttpMethod;
import com.typespec.core.http.HttpPipeline;
import com.typespec.core.http.HttpPipelineBuilder;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.http.policy.RedirectPolicy;
import com.typespec.core.test.annotation.DoNotRecord;
import com.typespec.core.test.annotation.RecordWithoutRequestBody;
import com.typespec.core.test.http.TestProxyTestServer;
import com.typespec.core.test.models.CustomMatcher;
import com.typespec.core.test.models.TestProxySanitizer;
import com.typespec.core.test.models.TestProxySanitizerType;
import com.typespec.core.test.utils.HttpURLConnectionHttpClient;
import com.typespec.core.test.utils.TestProxyUtils;
import com.typespec.core.util.Context;
import com.typespec.core.util.UrlBuilder;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static final ObjectMapper RECORD_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private static final List<TestProxySanitizer> CUSTOM_SANITIZER = new ArrayList<>();

    public static final String REDACTED = "REDACTED";
    private static final HttpHeaderName OCP_APIM_SUBSCRIPTION_KEY =
        HttpHeaderName.fromString("Ocp-Apim-Subscription-Key");

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
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .policies(interceptorManager.getRecordPolicy()).build();
        URL url = null;
        try {
            url = new UrlBuilder().setHost("localhost").setPort(3000).setScheme("http").toUrl();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        testResourceNamer.randomName("test", 10);
        testResourceNamer.now();
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);

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
        URL url;
        try {
            url = new UrlBuilder().setHost("localhost").setPort(3000).setScheme("http").setPath("first/path").toUrl();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> client.sendSync(request, Context.NONE));
        assertTrue(thrown.getMessage().contains("Uri doesn't match"));
    }

    @Test
    @Tag("Record")
    @RecordWithoutRequestBody
    public void testRecordWithPath() {
        HttpURLConnectionHttpClient client = new HttpURLConnectionHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .policies(interceptorManager.getRecordPolicy()).build();
        URL url;
        try {
            url = new UrlBuilder().setHost("localhost").setPort(3000).setPath("first/path").setScheme("http").toUrl();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        testResourceNamer.randomName("test", 10);
        testResourceNamer.now();
        HttpRequest request = new HttpRequest(HttpMethod.POST, url).setBody(TEST_DATA)
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
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .policies(interceptorManager.getRecordPolicy()).build();
        URL url;
        try {
            url = new UrlBuilder().setHost("localhost").setPort(3000).setPath("echoheaders").setScheme("http").toUrl();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        testResourceNamer.randomName("test", 10);
        testResourceNamer.now();
        HttpRequest request = new HttpRequest(HttpMethod.GET, url)
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

        URL url;
        try {
            url = new UrlBuilder().setHost("localhost").setPort(3000).setPath("first/path").setScheme("http").toUrl();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        HttpRequest request = new HttpRequest(HttpMethod.GET, url)
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
        RuntimeException thrown = assertThrows(IllegalStateException.class, () -> interceptorManager.getPlaybackClient());
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
        HttpClient client = interceptorManager.getPlaybackClient();

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client).build();
        URL url;
        try {
            url = new UrlBuilder()
                .setHost("localhost")
                .setPath("/fr/path/1")
                .setPort(3000)
                .setScheme("http")
                .toUrl();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        HttpRequest request = new HttpRequest(HttpMethod.GET, url)
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
            assertTrue(record.getResponseHeaders().get("Operation-Location")
                .startsWith("https://REDACTED/fr/models//905a58f9-131e-42b8-8410-493ab1517d62"));
            // custom sanitizers
            assertEquals(REDACTED, record.getResponse().get("modelId"));
        }
    }

    @Test
    @Tag("Playback")
    public void testPlaybackWithRedaction() {
        interceptorManager.addSanitizers(CUSTOM_SANITIZER);
        interceptorManager.addMatchers(new ArrayList<>(Arrays.asList(new CustomMatcher()
            .setExcludedHeaders(Arrays.asList("Ocp-Apim-Subscription-Key")))));
        HttpClient client = interceptorManager.getPlaybackClient();
        URL url;

        try {
            url = new UrlBuilder()
                .setHost("localhost")
                .setPort(3000)
                .setPath("/fr/models")
                .setScheme("http")
                .toUrl();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        HttpRequest request = new HttpRequest(HttpMethod.GET, url)
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
        interceptorManager.addMatchers(new CustomMatcher().setHeadersKeyOnlyMatch(Arrays.asList("Accept")));

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client).build();
        URL url;
        try {
            url = new UrlBuilder()
                .setHost("localhost")
                .setPath("/fr/path/2")
                .setPort(3000)
                .setScheme("http")
                .toUrl();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader(HttpHeaderName.CONTENT_TYPE, "application/json");

        try (HttpResponse response = pipeline.sendSync(request, Context.NONE)) {
            assertEquals(200, response.getStatusCode());
        }

        RecordedTestProxyData recordedTestProxyData = readDataFromFile();
        RecordedTestProxyData.TestProxyDataRecord record = recordedTestProxyData.getTestProxyDataRecords().get(0);
        // default regex sanitizers
        assertEquals("http://REDACTED/fr/path/2", record.getUri());

        // user delegation sanitizers
        assertTrue(record.getResponse().get("Body").contains("<UserDelegationKey><SignedTid>REDACTED</SignedTid></UserDelegationKey>"));
        assertTrue(record.getResponse().get("primaryKey").contains("<PrimaryKey>REDACTED</PrimaryKey>"));

        // custom body regex
        assertEquals(record.getResponse().get("TableName"), REDACTED);
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

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .policies(interceptorManager.getRecordPolicy())
            .build();

        try (HttpResponse response = pipeline.sendSync(new HttpRequest(HttpMethod.GET, "http://localhost:3000"),
            Context.NONE)) {
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

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .policies(new RedirectPolicy(), interceptorManager.getRecordPolicy()).build();
        URL url;
        try {
            url = new UrlBuilder()
                .setHost("localhost")
                .setPath("/getRedirect")
                .setPort(3000)
                .setScheme("http")
                .toUrl();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        HttpRequest request = new HttpRequest(HttpMethod.GET, url);

        try (HttpResponse response = pipeline.sendSync(request, Context.NONE)) {
            assertEquals(200, response.getStatusCode());
            assertEquals("http://localhost:3000/echoheaders", response.getRequest().getUrl().toString());
            assertNull(response.getRequest().getHeaders().get(HttpHeaderName.fromString("x-recording-upstream-base-uri")));
        }
    }

    private RecordedTestProxyData readDataFromFile() {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(interceptorManager.getRecordingFileLocation()));
            return RECORD_MAPPER.readValue(reader, RecordedTestProxyData.class);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class RecordedTestProxyData {
        @JsonProperty("Entries")
        private final LinkedList<TestProxyDataRecord> testProxyDataRecords;
        RecordedTestProxyData() {
            testProxyDataRecords = new LinkedList<>();
        }

        public LinkedList<TestProxyDataRecord> getTestProxyDataRecords() {
            return testProxyDataRecords;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        static class TestProxyDataRecord {
            @JsonProperty("RequestMethod")
            private String method;

            @JsonProperty("RequestUri")
            private String uri;

            @JsonProperty("RequestHeaders")
            private Map<String, String> headers;

            @JsonProperty("ResponseBody")
            private Map<String, String> response;

            @JsonProperty("ResponseHeaders")
            private Map<String, String> responseHeaders;

            @JsonProperty("RequestBody")
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
        }
    }
}

