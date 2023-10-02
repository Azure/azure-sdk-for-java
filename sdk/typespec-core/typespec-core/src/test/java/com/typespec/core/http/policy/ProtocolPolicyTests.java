// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http.policy;

import com.typespec.core.SyncAsyncExtension;
import com.typespec.core.SyncAsyncTest;
import com.typespec.core.http.HttpMethod;
import com.typespec.core.http.HttpPipeline;
import com.typespec.core.http.HttpPipelineBuilder;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.clients.NoOpHttpClient;
import com.typespec.core.util.Context;

import java.net.MalformedURLException;

import static com.typespec.core.CoreTestUtils.createUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProtocolPolicyTests {
    @SyncAsyncTest
    public void withOverwrite() throws Exception {
        final HttpPipeline pipeline = createPipeline("ftp", "ftp://www.bing.com");
        SyncAsyncExtension.execute(
            () -> pipeline.sendSync(createHttpRequest("https://www.bing.com"), Context.NONE),
            () -> pipeline.send(createHttpRequest("https://www.bing.com"))
        );
    }

    @SyncAsyncTest
    public void withNoOverwrite() throws Exception {
        final HttpPipeline pipeline = createPipeline("ftp", false, "https://www.bing.com");
        SyncAsyncExtension.execute(
            () -> pipeline.sendSync(createHttpRequest("https://www.bing.com"), Context.NONE),
            () -> pipeline.send(createHttpRequest("https://www.bing.com"))
        );
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
        return new HttpRequest(HttpMethod.GET, createUrl(url));
    }
}
