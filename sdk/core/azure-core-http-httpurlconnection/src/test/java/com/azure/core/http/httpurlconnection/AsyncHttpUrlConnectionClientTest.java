package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.http.LocalTestServer;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AsyncHttpUrlConnectionClientTest {

    private static LocalTestServer localTestServer;
    private static AsyncHttpUrlConnectionClient asyncClient;

    @BeforeAll
    public static void setup() {
        localTestServer = new LocalTestServer((request, response, requestBody) -> {
            response.setStatus(200);
            response.setContentType("text/plain");
            response.getWriter().write("Test response");
        });
        localTestServer.start();

        asyncClient = new AsyncHttpUrlConnectionClient();
    }

    @AfterAll
    public static void teardown() {
        localTestServer.stop();
    }

    @Test
    public void testAsyncRequest() throws Exception {
        HttpRequest httpRequest = new HttpRequest(
            HttpMethod.GET, new URL(localTestServer.getHttpUri()));

        asyncClient.send(httpRequest).thenAccept(response -> {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        }).join();
    }
}

