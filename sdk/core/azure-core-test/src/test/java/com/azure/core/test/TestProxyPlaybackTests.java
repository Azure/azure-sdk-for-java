package com.azure.core.test;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.implementation.TestingHelpers;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.UrlBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestProxyPlaybackTests extends TestBase {

    @BeforeAll
    public static void setupClass() {
        Configuration.getGlobalConfiguration().put(TestingHelpers.AZURE_TEST_MODE, "PLAYBACK");
        Configuration.getGlobalConfiguration().put(TestingHelpers.AZURE_TEST_USE_PROXY, "TRUE");
        TestBase.setupClass();
    }

    @AfterAll
    public static void teardownClass() {
        TestBase.teardownClass();
        Configuration.getGlobalConfiguration().remove(TestingHelpers.AZURE_TEST_MODE);
        Configuration.getGlobalConfiguration().remove(TestingHelpers.AZURE_TEST_USE_PROXY);

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
//            url = new UrlBuilder().setHost("example.com").setScheme("https").toUrl();
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
    public void testPlayback() {

        HttpClient client = interceptorManager.getPlaybackClient();


        URL url = null;
        try {
            url = new UrlBuilder().setHost("example.com").setScheme("https").toUrl();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        HttpResponse response = client.sendSync(request, Context.NONE);
        assertEquals(OffsetDateTime.parse("2022-12-16T21:42:13.806425800Z"), testResourceNamer.now());
        assertEquals("playbacktest69112c", testResourceNamer.randomName("playbacktest", 20));
        assertEquals(OffsetDateTime.parse("2022-12-16T21:42:13.807425500Z"), testResourceNamer.now());
        assertEquals("a56e3c54-7325-43ad-a3cc-3314695de323", testResourceNamer.randomUuid());
        assertEquals(OffsetDateTime.parse("2022-12-16T21:42:13.807425500Z"), testResourceNamer.now());
        assertEquals("playbacktest828103", testResourceNamer.randomName("playbacktest", 20));
        assertEquals(OffsetDateTime.parse("2022-12-16T21:42:13.807425500Z"), testResourceNamer.now());
        assertEquals("095a6bad-1c34-4fdc-8174-b61a5b95c217", testResourceNamer.randomUuid());
        assertEquals(OffsetDateTime.parse("2022-12-16T21:42:13.807425500Z"), testResourceNamer.now());
        assertEquals("playbacktest633220", testResourceNamer.randomName("playbacktest", 20));
        assertEquals(OffsetDateTime.parse("2022-12-16T21:42:13.808425Z"), testResourceNamer.now());
        assertEquals("4607aedc-e21c-4130-b947-7c5e9be18280", testResourceNamer.randomUuid());
        assertEquals(OffsetDateTime.parse("2022-12-16T21:42:13.808425Z"), testResourceNamer.now());
        assertEquals("playbacktest595370", testResourceNamer.randomName("playbacktest", 20));
        assertEquals(OffsetDateTime.parse("2022-12-16T21:42:13.808425Z"), testResourceNamer.now());
        assertEquals("a0ba8bd8-ed7c-4c86-83b6-b4bd455da01e", testResourceNamer.randomUuid());
        assertEquals(response.getStatusCode(), 200);
    }
}
