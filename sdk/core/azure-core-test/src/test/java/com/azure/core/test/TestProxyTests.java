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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("deprecation")
public class TestProxyTests extends TestBase {
    static TestProxyTestServer server;

    private static List<TestProxySanitizer> customSanitizer = new ArrayList<>();
    static {
        customSanitizer.add(new TestProxySanitizer("$..modelId", "REDACTED", TestProxySanitizerType.BODY));
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

        assertEquals(200, response.getStatusCode());
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

        interceptorManager.addRecordSanitizers(customSanitizer);

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
        testResourceNamer.randomName("test", 10);
        testResourceNamer.now();
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader("Ocp-Apim-Subscription-Key", "SECRET_API_KEY");
        request.setHeader("Content-Type", "application/json");

        HttpResponse response = pipeline.sendSync(request, Context.NONE);

        assertEquals(response.getStatusCode(), 200);

        // TODO (need to get the record file written data to verify redacted content)
        assertEquals(200, response.getStatusCode());

    }

    @Test
    @Tag("Playback")
    public void testPlaybackWithRedaction() {
        interceptorManager.addRecordSanitizers(customSanitizer);
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
}

