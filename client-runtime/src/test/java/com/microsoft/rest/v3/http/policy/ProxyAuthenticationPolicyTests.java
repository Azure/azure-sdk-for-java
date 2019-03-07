package com.microsoft.rest.v3.http.policy;

import com.microsoft.rest.v3.http.HttpMethod;
import com.microsoft.rest.v3.http.HttpPipeline;
import com.microsoft.rest.v3.http.HttpPipelineOptions;
import com.microsoft.rest.v3.http.HttpRequest;
import com.microsoft.rest.v3.http.MockHttpClient;
import org.junit.Test;

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
        //
        final HttpPipeline pipeline = new HttpPipeline(new MockHttpClient(),
                new HttpPipelineOptions(null),
                new ProxyAuthenticationPolicy(username, password),
                (context, next) -> {
                    assertEquals("Basic dGVzdHVzZXI6dGVzdHBhc3M=", context.httpRequest().headers().value("Proxy-Authentication"));
                    auditorVisited.set(true);
                    return next.process();
                });

        pipeline.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost")))
                .block();

        if (!auditorVisited.get()) {
            fail();
        }
    }
}
