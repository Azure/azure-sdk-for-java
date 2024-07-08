// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.SyncAsyncExtension;
import com.azure.core.SyncAsyncTest;
import io.clientcore.core.http.HttpMethod;
import io.clientcore.core.http.HttpPipeline;
import io.clientcore.core.http.HttpPipelineBuilder;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.clients.NoOpHttpClient;
import io.clientcore.core.util.Context;

import java.net.MalformedURLException;

import static com.azure.core.CoreTestUtils.createUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProtocolPolicyTests {
    @SyncAsyncTest
    public void withOverwrite() throws Exception {
        final HttpPipeline pipeline = createPipeline("ftp", "ftp://www.bing.com");
        SyncAsyncExtension.execute(() -> pipeline.send(createHttpRequest("https://www.bing.com"), Context.none()),
            () -> pipeline.send(createHttpRequest("https://www.bing.com")));
    }

    @SyncAsyncTest
    public void withNoOverwrite() throws Exception {
        final HttpPipeline pipeline = createPipeline("ftp", false, "https://www.bing.com");
        SyncAsyncExtension.execute(() -> pipeline.send(createHttpRequest("https://www.bing.com"), Context.none()),
            () -> pipeline.send(createHttpRequest("https://www.bing.com")));
    }

    private static HttpPipeline createPipeline(String protocol, String expectedUrl) {
        return new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(new ProtocolPolicy(protocol, true), (context, next) -> {
                assertEquals(expectedUrl, httpRequest.getUrl().toString());
                return next.process();
            })
            .build();
    }

    private static HttpPipeline createPipeline(String protocol, boolean overwrite, String expectedUrl) {
        return new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(new ProtocolPolicy(protocol, overwrite), (context, next) -> {
                assertEquals(expectedUrl, httpRequest.getUrl().toString());
                return next.process();
            })
            .build();
    }

    private static HttpRequest createHttpRequest(String url) throws MalformedURLException {
        return new HttpRequest(HttpMethod.GET, createUrl(url));
    }
}
