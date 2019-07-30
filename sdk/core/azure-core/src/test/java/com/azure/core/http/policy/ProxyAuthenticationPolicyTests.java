// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.MockHttpClient;
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
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new MockHttpClient())
            .policies(new ProxyAuthenticationPolicy(username, password),
                (context, next) -> {
                    assertEquals("Basic dGVzdHVzZXI6dGVzdHBhc3M=", context.httpRequest().headers().value("Proxy-Authentication"));
                    auditorVisited.set(true);
                    return next.process();
                })
            .build();

        pipeline.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost")))
                .block();

        if (!auditorVisited.get()) {
            fail();
        }
    }
}
