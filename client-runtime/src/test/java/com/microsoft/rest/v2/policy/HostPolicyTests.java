package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpMethod;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import org.junit.Test;
import io.reactivex.Single;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class HostPolicyTests {
    @Test
    public void withNoPort() throws MalformedURLException {
        final RequestPolicy policy = createHostPolicy("localhost", "ftp://localhost");
        policy.sendAsync(createHttpRequest("ftp://www.example.com"));
    }

    @Test
    public void withPort() throws MalformedURLException {
        final RequestPolicy policy = createHostPolicy("localhost", "ftp://localhost:1234");
        policy.sendAsync(createHttpRequest("ftp://www.example.com:1234"));
    }

    private static RequestPolicy createMockRequestPolicy(final String expectedUrlString) {
        return new RequestPolicy() {
            @Override
            public Single<HttpResponse> sendAsync(HttpRequest request) {
                assertEquals(expectedUrlString, request.url().toString());
                return null;
            }
        };
    }

    private static RequestPolicy createHostPolicy(String host, String expectedUrl) {
        return new HostPolicyFactory(host).create(createMockRequestPolicy(expectedUrl), null);
    }

    private static HttpRequest createHttpRequest(String url) throws MalformedURLException {
        return new HttpRequest("mock.caller", HttpMethod.GET, new URL(url));
    }
}
