// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.clients.NoOpHttpClient;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProtocolPolicyTests {

    @Test
    public void withOverwrite() throws MalformedURLException {
        final HttpPipeline pipeline = createPipeline("ftp", "ftp://www.bing.com");
        pipeline.send(createHttpRequest("http://www.bing.com"));
    }

    @Test
    public void withNoOverwrite() throws MalformedURLException {
        final HttpPipeline pipeline = createPipeline("ftp", false, "https://www.bing.com");
        pipeline.send(createHttpRequest("https://www.bing.com"));
    }
    private static HttpPipeline createPipeline(String protocol, String expectedUrl) {
        return new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(new ProtocolPolicy(protocol, true),
                (context, next) -> {
                    assertEquals(expectedUrl, context.getHttpRequest().getUrl().toString());
                    return next.process();
                })
            .build();
    }

    private static HttpPipeline createPipeline(String protocol, boolean overwrite, String expectedUrl) {
        return new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(new ProtocolPolicy(protocol, overwrite),
                (context, next) -> {
                    assertEquals(expectedUrl, context.getHttpRequest().getUrl().toString());
                    return next.process();
                })
            .build();
    }

    private static HttpRequest createHttpRequest(String url) throws MalformedURLException {
        return new HttpRequest(HttpMethod.GET, new URL(url));
    }
}
