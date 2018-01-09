package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpMethod;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import org.junit.Test;
import io.reactivex.Single;

import static org.junit.Assert.assertEquals;

public class HostPolicyTests {
    @Test
    public void withNoProtocolAndNoPort() {
        final HostPolicy policy = createHostPolicy("localhost", "localhost");
        policy.sendAsync(createHttpRequest("www.bing.com"));
    }

    @Test
    public void withProtocolAndNoPort() {
        final HostPolicy policy = createHostPolicy("localhost", "ftp://localhost");
        policy.sendAsync(createHttpRequest("ftp://www.example.com"));
    }

    @Test
    public void withNoProtocolAndPort() {
        final HostPolicy policy = createHostPolicy("localhost", "localhost:8080");
        policy.sendAsync(createHttpRequest("www.bing.com:8080"));
    }

    @Test
    public void withProtocolAndPort() {
        final HostPolicy policy = createHostPolicy("localhost", "ftp://localhost:1234");
        policy.sendAsync(createHttpRequest("ftp://www.example.com:1234"));
    }

    private static RequestPolicy createMockRequestPolicy(final String expectedUrl) {
        return new RequestPolicy() {
            @Override
            public Single<HttpResponse> sendAsync(HttpRequest request) {
                assertEquals(expectedUrl, request.url());
                return null;
            }
        };
    }

    private static HostPolicy createHostPolicy(String host, String expectedUrl) {
        return new HostPolicy.Factory(host).create(createMockRequestPolicy(expectedUrl), null);
    }

    private static HttpRequest createHttpRequest(String url) {
        return new HttpRequest("mock.caller", HttpMethod.GET, url);
    }
}
