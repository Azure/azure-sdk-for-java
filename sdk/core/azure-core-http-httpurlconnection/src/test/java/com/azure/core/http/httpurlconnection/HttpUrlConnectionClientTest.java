package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.httpurlconnection.HttpRequest; //] To be replaced with existing HttpRequest/HttpResponse
import com.azure.core.http.httpurlconnection.HttpResponse;//] classes. See issue #1
//import com.azure.core.http.HttpRequest;
//import com.azure.core.http.HttpResponse;
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
            // To be changed to LocalTestServer as noted in issue #4
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

    // InputStream is expected from 100-299 responses
//    @Test
//    public void testClientReceivesInputStreamResponse() {
//        // This test will require a LocalTestServer to be implemented. See Issue #4 for more details.
//    }
//
//    // ErrorStream is expected from 300+ responses
//    @Test
//    public void testClientReceivesErrorStreamResponse() {
//        // This test will require a LocalTestServer to be implemented. See Issue #4 for more details.
//    }
//
//    @Test
//    public void testNullStream() {
//        // This test will require a LocalTestServer to be implemented. See Issue #4 for more details.
//    }
//
//    @Test
//    public void testVerifyResponseWhenRequestBodyIsError {
//
//    }
}
