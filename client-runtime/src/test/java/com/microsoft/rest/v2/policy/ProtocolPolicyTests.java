package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import org.junit.Test;
import rx.Single;

import static org.junit.Assert.*;

public class ProtocolPolicyTests {

    @Test
    public void withOverwriteAndNoProtocol() {
        final ProtocolPolicy policy = createProtocolPolicy("ftp", "ftp://www.bing.com");
        policy.sendAsync(createHttpRequest("www.bing.com"));
    }

    @Test
    public void withOverwriteAndProtocol() {
        final ProtocolPolicy policy = createProtocolPolicy("ftp", "ftp://www.bing.com");
        policy.sendAsync(createHttpRequest("http://www.bing.com"));
    }

    @Test
    public void withNoOverwriteAndNoProtocol() {
        final ProtocolPolicy policy = createProtocolPolicy("ftp", false, "ftp://www.bing.com");
        policy.sendAsync(createHttpRequest("www.bing.com"));
    }

    @Test
    public void withNoOverwriteAndProtocol() {
        final ProtocolPolicy policy = createProtocolPolicy("ftp", false, "https://www.bing.com");
        policy.sendAsync(createHttpRequest("https://www.bing.com"));
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

    private static ProtocolPolicy createProtocolPolicy(String protocol, String expectedUrl) {
        return new ProtocolPolicy.Factory(protocol).create(createMockRequestPolicy(expectedUrl), null);
    }

    private static ProtocolPolicy createProtocolPolicy(String protocol, boolean overwrite, String expectedUrl) {
        return new ProtocolPolicy.Factory(protocol, overwrite).create(createMockRequestPolicy(expectedUrl), null);
    }

    private static HttpRequest createHttpRequest(String url) {
        return new HttpRequest("mock.caller", "GET", url);
    }
}
