package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.http.MockHttpClient;
import com.microsoft.rest.v2.policy.ProxyAuthenticationPolicy;
import com.microsoft.rest.v2.policy.RequestPolicy;
import org.junit.Test;
import io.reactivex.Single;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ProxyAuthenticationPolicyTests {
    @Test
    public void test() {
        final AtomicBoolean auditorVisited = new AtomicBoolean(false);
        final String username = "testuser";
        final String password = "testpass";

        final HttpPipeline pipeline = HttpPipeline.build(
                new MockHttpClient(),
                new ProxyAuthenticationPolicy.Factory(username, password),
                new RequestPolicy.Factory() {
                    @Override
                    public RequestPolicy create(final RequestPolicy next, RequestPolicy.Options options) {
                        return new RequestPolicy() {
                            @Override
                            public Single<HttpResponse> sendAsync(HttpRequest request) {
                                assertEquals("Basic dGVzdHVzZXI6dGVzdHBhc3M=", request.headers().value("Proxy-Authentication"));
                                auditorVisited.set(true);
                                return next.sendAsync(request);
                            }
                        };
                    }
                });

        pipeline.sendRequestAsync(new HttpRequest("test", "GET", "localhost"))
                .blockingGet();

        if (!auditorVisited.get()) {
            fail();
        }
    }
}
