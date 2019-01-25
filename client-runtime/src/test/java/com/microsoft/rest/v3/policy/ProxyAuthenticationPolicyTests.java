package com.microsoft.rest.v3.policy;

import com.microsoft.rest.v3.http.HttpMethod;
import com.microsoft.rest.v3.http.HttpPipeline;
import com.microsoft.rest.v3.http.HttpRequest;
import com.microsoft.rest.v3.http.HttpResponse;
import com.microsoft.rest.v3.http.MockHttpClient;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ProxyAuthenticationPolicyTests {
    @Test
    public void test() throws MalformedURLException {
        final AtomicBoolean auditorVisited = new AtomicBoolean(false);
        final String username = "testuser";
        final String password = "testpass";

        final HttpPipeline pipeline = HttpPipeline.build(
                new MockHttpClient(),
                new ProxyAuthenticationPolicyFactory(username, password),
                new RequestPolicyFactory() {
                    @Override
                    public RequestPolicy create(final RequestPolicy next, RequestPolicyOptions options) {
                        return new RequestPolicy() {
                            @Override
                            public Mono<HttpResponse> sendAsync(HttpRequest request) {
                                assertEquals("Basic dGVzdHVzZXI6dGVzdHBhc3M=", request.headers().value("Proxy-Authentication"));
                                auditorVisited.set(true);
                                return next.sendAsync(request);
                            }
                        };
                    }
                });

        pipeline.sendRequestAsync(new HttpRequest("test", HttpMethod.GET, new URL("http://localhost"), null))
                .block();

        if (!auditorVisited.get()) {
            fail();
        }
    }
}
