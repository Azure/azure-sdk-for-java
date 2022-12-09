package com.azure.core.test;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.implementation.TestingHelpers;
import com.azure.core.test.utils.HttpURLConnectionHttpClient;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.UrlBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

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

        assertEquals(response.getStatusCode(), 200);
    }
}
