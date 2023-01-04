// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.TestProxyTestServer;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.HttpURLConnectionHttpClient;
import com.azure.core.util.Context;
import com.azure.core.util.UrlBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("deprecation")
public class TestProxyTests extends TestBase {
    static TestProxyTestServer server;

    private static List<TestProxySanitizer> recordSanitizers;

    static {
        recordSanitizers = new ArrayList<>();
        recordSanitizers.add(new TestProxySanitizer("^(?:https?:\\\\/\\\\/)?(?:[^@\\\\/\\\\n]+@)?(?:www\\\\.)?([^:\\\\/?\\\\n]+)", "https://REDACTED.cognitiveservices.azure.com", TestProxySanitizerType.URL));
        recordSanitizers.add(new TestProxySanitizer("$..modelId", "REDACTED", TestProxySanitizerType.BODY));
        recordSanitizers.add(new TestProxySanitizer("Ocp-Apim-Subscription-Key", "REDACTED", TestProxySanitizerType.HEADER));
    }

    @BeforeAll
    public static void setupClass() {
        enableTestProxy();
        server = new TestProxyTestServer();
        TestBase.setupClass();
    }

    @AfterAll
    public static void teardownClass() {
        TestBase.teardownClass();
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

        assertEquals(response.getStatusCode(), 200);
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

        assertEquals(response.getStatusCode(), 200);
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

        assertEquals(response.getStatusCode(), 200);
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
        assertEquals(response.getBodyAsString().block(), "first path");
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    @Tag("Record")
    public void testRecordWithRedaction() throws JsonProcessingException {
        HttpURLConnectionHttpClient client = new HttpURLConnectionHttpClient();

        interceptorManager.addRecordSanitizers(recordSanitizers);

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .policies(interceptorManager.getRecordPolicy()).build();
        URL url;
        try {
            url = new UrlBuilder()
                .setHost("localhost")
                .setPath("/fr/models")
                .setPort(3000)
                .setScheme("http")
                .toUrl();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader("Ocp-Apim-Subscription-Key", "TEST_API_KEY");
        request.setHeader("Content-Type", "application/json");

        HttpResponse response = pipeline.sendSync(request, Context.NONE);

        // TODO (need to get the record file written data to verify redacted content)
        String responseData = response.getBodyAsBinaryData().toString();
        ObjectMapper objectMapper = new ObjectMapper();
        Map jsonMap = objectMapper.readValue(responseData, Map.class);
        assertEquals(response.getStatusCode(), 200);
        assertTrue(response.getRequest().getUrl().toString().contains("REDACTED"));

        String actualValue = jsonMap.get("modelId").toString();
        assertEquals("REDACTED", actualValue);
    }

    @Test
    @Tag("Playback")
    public void testPlaybackWithRedaction() {
        interceptorManager.addRecordSanitizers(recordSanitizers);
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
        request.setHeader("Ocp-Apim-Subscription-Key", "TEST_API_KEY");
        request.setHeader("Content-Type", "application/json");

        HttpResponse response = client.sendSync(request, Context.NONE);

        assertEquals(response.getStatusCode(), 200);
    }

}

