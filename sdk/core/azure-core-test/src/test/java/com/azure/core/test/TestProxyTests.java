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
import com.azure.core.test.utils.HttpURLConnectionHttpClient;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.UrlBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SuppressWarnings("deprecation")
public class TestProxyTests extends TestBase {
    static TestProxyTestServer server;
    private String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_FORM_RECOGNIZER_ENDPOINT");
    private String API_KEY = Configuration.getGlobalConfiguration().get("AZURE_FORM_RECOGNIZER_API_KEY");

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
    @Tag("RECORD")
    public void testRecordWithRedaction() {
        HttpURLConnectionHttpClient client = new HttpURLConnectionHttpClient();
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<String> urlSanitizers = new ArrayList<>();
        urlSanitizers.add("^(?:https?:\\\\/\\\\/)?(?:[^@\\\\/\\\\n]+@)?(?:www\\\\.)?([^:\\\\/?\\\\n]+)");

        List<String> bodySanitizers = new ArrayList<>();
        bodySanitizers.add("$..modelId");

        map.put("URL", urlSanitizers);
        map.put("BODY", bodySanitizers);

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(client)
            .policies(interceptorManager.getRecordPolicy(map)).build();
        URL url;
        try {
            url = new UrlBuilder()
                .setHost(ENDPOINT)
                .setPath("/formrecognizer/documentModels")
                .setScheme("https")
                .setQueryParameter("api-version", "2022-08-31")
                .toUrl();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        testResourceNamer.randomName("test", 10);
        testResourceNamer.now();
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader("Ocp-Apim-Subscription-Key", API_KEY);
        request.setHeader("Content-Type", "application/json");

        HttpResponse response = pipeline.sendSync(request, Context.NONE);

        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    @Tag("Playback")
    public void testPlaybackWithRedaction() {
        HttpClient client = interceptorManager.getPlaybackClient();
        URL url;
        try {
            url = new UrlBuilder()
                .setHost(ENDPOINT)
                .setPath("/formrecognizer/documentModels")
                .setScheme("https")
                .setQueryParameter("api-version", "2022-08-31")
                .toUrl();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        testResourceNamer.randomName("test", 10);
        testResourceNamer.now();
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader("Ocp-Apim-Subscription-Key", API_KEY);
        request.setHeader("Content-Type", "application/json");

        HttpResponse response = client.sendSync(request, Context.NONE);

        assertEquals(response.getStatusCode(), 200);
    }

}

