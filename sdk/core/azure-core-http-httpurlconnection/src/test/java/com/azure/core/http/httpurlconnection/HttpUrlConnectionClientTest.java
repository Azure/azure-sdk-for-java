package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class HttpUrlConnectionClientTest {
    private HttpUrlConnectionClient createClient(HttpRequest request) {
        return new HttpUrlConnectionClient(request);
    }
    private URL getURL() {
        try {
            return new URL("https://http-url-connect-client-web.vjr4ig.easypanel.host");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGet() {
        HttpRequest request = new HttpRequest(HttpMethod.GET, getURL());

        final HttpUrlConnectionClient client = createClient(request);
        final HttpResponse response = client.send();
    }

    @Test
    public void testPost() {
        HttpRequest request = new HttpRequest(HttpMethod.POST, getURL());
        request.setBody("{test: \"hello\"}");

        final HttpUrlConnectionClient client = createClient(request);
        final HttpResponse response = client.send();
    }
}
