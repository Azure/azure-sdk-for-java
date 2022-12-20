// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.TestProxyTestServer;
import com.azure.core.test.implementation.TestingHelpers;
import com.azure.core.test.utils.HttpURLConnectionHttpClient;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.UrlBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;


import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SuppressWarnings("deprecation")
public class TestProxyRecordTests extends TestBase {
    static TestProxyTestServer server;
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
}

