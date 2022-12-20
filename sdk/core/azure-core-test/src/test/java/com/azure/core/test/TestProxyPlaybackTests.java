package com.azure.core.test;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.TestProxyTestServer;
import com.azure.core.util.Context;
import com.azure.core.util.UrlBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestProxyPlaybackTests extends TestBase {
    static TestProxyTestServer server;

    @BeforeAll
    public static void setupClass() {
        enableTestProxy();
        TestBase.setupClass();
    }

    /*
     * To make a new recording for this test:
     * 1) Uncomment this test and comment out the other testPlayback
     * 2) Set AZURE_TEST_MODE to "RECORD" above
     * 3) Check in the new recording from target\test-classes\session-records to src\test\resources\session-records\
     */
//    @Test
//    public void testPlayback() {
//
//        HttpURLConnectionHttpClient client = new HttpURLConnectionHttpClient();
//        HttpPipeline pipeline = new HttpPipelineBuilder()
//            .httpClient(client)
//            .policies(interceptorManager.getRecordPolicy()).build();
//        URL url = null;
//        try {
//            url = new UrlBuilder().setHost("localhost").setPort(3000).setPath("first/path").setScheme("http").toUrl();
//        } catch (MalformedURLException e) {
//            throw new RuntimeException(e);
//        }
//        testResourceNamer.now();
//        testResourceNamer.randomName("playbacktest", 20);
//        testResourceNamer.now();
//        testResourceNamer.randomUuid();
//        testResourceNamer.now();
//        testResourceNamer.randomName("playbacktest", 20);
//        testResourceNamer.now();
//        testResourceNamer.randomUuid();
//        testResourceNamer.now();
//        testResourceNamer.randomName("playbacktest", 20);
//        testResourceNamer.now();
//        testResourceNamer.randomUuid();
//        testResourceNamer.now();
//        testResourceNamer.randomName("playbacktest", 20);
//        testResourceNamer.now();
//        testResourceNamer.randomUuid();
//        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
//        HttpResponse response = pipeline.sendSync(request, Context.NONE);
//
//        assertEquals(response.getStatusCode(), 200);
//    }

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
}
