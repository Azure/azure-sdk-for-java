// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.test.http.TestProxyTestServer;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.HttpURLConnectionHttpClient;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.Context;
import com.azure.core.util.UrlBuilder;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for testing Test proxy functionality of record, playback and redaction.
 */
public class TestProxyTests extends TestProxyTestBase {
    static TestProxyTestServer server;
    private static final ObjectMapper RECORD_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private static List<TestProxySanitizer> customSanitizer = new ArrayList<>();

    public static final String REDACTED = "REDACTED";

    static {
        customSanitizer.add(new TestProxySanitizer("$..modelId", REDACTED, TestProxySanitizerType.BODY_KEY));
        customSanitizer.add(new TestProxySanitizer("TableName\\\"*:*\\\"(?<tablename>.*)\\\"", REDACTED, TestProxySanitizerType.BODY_REGEX).setGroupForReplace("tablename"));
    }

    @BeforeAll
    public static void setupClass() {
        server = new TestProxyTestServer();
        TestProxyTestBase.setup();
    }
    @AfterAll
    public static void teardownClass() {
        TestProxyTestBase.teardown();
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
        HttpResponse response = pipeline.sendSync(request, Context.NONE);

        assertEquals(200, response.getStatusCode());
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

    }

    @Test
    @Tag("Playback")
    @DoNotRecord
    public void testDoNotPlayback() {

    }

    @Test
    @Tag("Record")
    public void testRecordWithPath() {
        HttpURLConnectionHttpClient client = new HttpURLConnectionHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .policies(interceptorManager.getRecordPolicy()).build();
        URL url = null;
        try {
            url = new UrlBuilder().setHost("localhost").setPort(3000).setPath("first/path").setScheme("http").toUrl();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        testResourceNamer.randomName("test", 10);
        testResourceNamer.now();
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        HttpResponse response = pipeline.sendSync(request, Context.NONE);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    @Tag("Record")
    public void testRecordWithHeaders() {
        HttpURLConnectionHttpClient client = new HttpURLConnectionHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .policies(interceptorManager.getRecordPolicy()).build();
        URL url = null;
        try {
            url = new UrlBuilder().setHost("localhost").setPort(3000).setPath("echoheaders").setScheme("http").toUrl();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        testResourceNamer.randomName("test", 10);
        testResourceNamer.now();
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader("header1", "value1");
        request.setHeader("header2", "value2");
        HttpResponse response = pipeline.sendSync(request, Context.NONE);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    @Tag("Playback")
    public void testPlayback() {

        HttpClient client = interceptorManager.getPlaybackClient();

        URL url = null;
        try {
            url = new UrlBuilder().setHost("localhost").setPort(3000).setPath("first/path").setScheme("http").toUrl();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        HttpResponse response = client.sendSync(request, Context.NONE);
        assertEquals("first path", response.getBodyAsString().block());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    @Tag("Record")
    public void testRecordWithRedaction() {
        HttpURLConnectionHttpClient client = new HttpURLConnectionHttpClient();

        interceptorManager.addSanitizers(customSanitizer);

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .policies(interceptorManager.getRecordPolicy()).build();
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

        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader("Ocp-Apim-Subscription-Key", "SECRET_API_KEY");
        request.setHeader("Content-Type", "application/json");

        HttpResponse response = pipeline.sendSync(request, Context.NONE);

        assertEquals(response.getStatusCode(), 200);

        assertEquals(200, response.getStatusCode());
        RecordedTestProxyData recordedTestProxyData = readDataFromFile();
        RecordedTestProxyData.TestProxyDataRecord record = recordedTestProxyData.getTestProxyDataRecords().get(0);
        // default sanitizers
        assertEquals("http://REDACTED/fr/path/1", record.getUri());
        assertEquals(REDACTED, record.getHeaders().get("Ocp-Apim-Subscription-Key"));
        // custom sanitizers
        assertEquals(REDACTED, record.getResponse().get("modelId"));

    }

    @Test
    @Tag("Playback")
    public void testPlaybackWithRedaction() {
        interceptorManager.addSanitizers(customSanitizer);
        interceptorManager.addMatchers(new ArrayList<>(Arrays.asList(new CustomMatcher().setExcludedHeaders(Arrays.asList("Ocp-Apim-Subscription-Key")))));
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
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader("Ocp-Apim-Subscription-Key", "SECRET_API_KEY");
        request.setHeader("Content-Type", "application/json");

        HttpResponse response = client.sendSync(request, Context.NONE);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    @Tag("Record")
    public void testBodyRegexRedactRecord() {
        HttpURLConnectionHttpClient client = new HttpURLConnectionHttpClient();

        interceptorManager.addSanitizers(customSanitizer);

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .policies(interceptorManager.getRecordPolicy()).build();
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
        request.setHeader("Content-Type", "application/json");

        HttpResponse response = pipeline.sendSync(request, Context.NONE);

        assertEquals(response.getStatusCode(), 200);

        assertEquals(200, response.getStatusCode());

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

    private RecordedTestProxyData readDataFromFile() {
        String filePath = Paths.get(TestUtils.getRecordFolder().getPath(), this.testContextManager.getTestPlaybackRecordingName()) + ".json";

        File recordFile = new File(filePath);
        try (BufferedReader reader = Files.newBufferedReader(recordFile.toPath())) {
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

