package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpMethod;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import org.junit.Test;
import io.reactivex.Single;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.*;

public class ProtocolPolicyTests {

    @Test
    public void withOverwrite() throws MalformedURLException {
        final RequestPolicy policy = createProtocolPolicy("ftp", "ftp://www.bing.com");
        policy.sendAsync(createHttpRequest("http://www.bing.com"));
    }

    @Test
    public void withNoOverwrite() throws MalformedURLException {
        final RequestPolicy policy = createProtocolPolicy("ftp", false, "https://www.bing.com");
        policy.sendAsync(createHttpRequest("https://www.bing.com"));
    }

    private static RequestPolicy createMockRequestPolicy(final String expectedUrl) {
        return new RequestPolicy() {
            @Override
            public Single<HttpResponse> sendAsync(HttpRequest request) {
                assertEquals(expectedUrl, request.url().toString());
                return null;
            }
        };
    }

    private static RequestPolicy createProtocolPolicy(String protocol, String expectedUrl) {
        return new ProtocolPolicyFactory(protocol).create(createMockRequestPolicy(expectedUrl), null);
    }

    private static RequestPolicy createProtocolPolicy(String protocol, boolean overwrite, String expectedUrl) {
        return new ProtocolPolicyFactory(protocol, overwrite).create(createMockRequestPolicy(expectedUrl), null);
    }

    private static HttpRequest createHttpRequest(String url) throws MalformedURLException {
        return new HttpRequest("mock.caller", HttpMethod.GET, new URL(url));
    }
}
