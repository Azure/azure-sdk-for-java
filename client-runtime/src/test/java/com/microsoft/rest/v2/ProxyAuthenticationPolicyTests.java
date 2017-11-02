package com.microsoft.rest.v2;

import com.microsoft.rest.v2.http.HttpClient;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.http.MockHttpClient;
import com.microsoft.rest.v2.policy.ProxyAuthenticationPolicy;
import com.microsoft.rest.v2.policy.RequestPolicy;
import org.junit.Test;
import rx.Single;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ProxyAuthenticationPolicyTests {
    boolean auditorVisited = false;

    @Test
    public void test() {
        final String username = "testuser";
        final String password = "testpass";

        RequestPolicy.Factory auditorFactory = new RequestPolicy.Factory() {
            @Override
            public RequestPolicy create(final RequestPolicy next) {
                return new RequestPolicy() {
                    @Override
                    public Single<HttpResponse> sendAsync(HttpRequest request) {
                        assertEquals("dGVzdHVzZXI6dGVzdHBhc3M=", request.headers().value("Proxy-Authentication"));
                        auditorVisited = true;
                        return next.sendAsync(request);
                    }
                };
            }
        };

        HttpClient client = new MockHttpClient(
                new ProxyAuthenticationPolicy.Factory(username, password),
                auditorFactory);

        client.sendRequestAsync(new HttpRequest("test", "GET", "localhost"))
                .toBlocking().value();

        if (!auditorVisited) {
            fail();
        }
    }
}
