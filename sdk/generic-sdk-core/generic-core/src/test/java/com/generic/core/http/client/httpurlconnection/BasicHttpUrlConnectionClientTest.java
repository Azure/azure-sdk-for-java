package com.generic.core.http.client.httpurlconnection;

import com.generic.core.http.client.HttpClient;
import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.models.Context;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.servlet.ServletException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BasicHttpUrlConnectionClientTest {
    private static final byte[] SHORT_BODY = "hi there".getBytes(StandardCharsets.UTF_8);

    private static LocalTestServer server;

    @BeforeAll
    public static void setupClass() {
        server = new LocalTestServer((req, resp, requestBody) -> {

            String path = req.getServletPath();
            boolean get = "GET".equalsIgnoreCase(req.getMethod());

            if (get && "/echo".equals(path)) {
                resp.setStatus(200);
            } else {
                throw new ServletException("Unexpected request: " + req.getMethod() + " " + req.getServletPath());
            }
        });
        server.start();
    }

    @AfterAll
    public static void teardownClass() {
        server.stop();
    }

    @Test
    public void testBasicSendRequest() {
        HttpClient client = new HttpUrlConnectionClientBuilder().build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, "/echo").toString(), null);

        try (HttpResponse response = client.send(request, Context.NONE)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    static URL url(LocalTestServer server, String path) {
        try {
            return new URI(server.getHttpUri() + path).toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
