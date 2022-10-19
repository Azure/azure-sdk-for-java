// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.SyncAsyncExtension;
import com.azure.core.SyncAsyncTest;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.clients.NoOpHttpClient;
import com.azure.core.util.Context;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HostPolicyTests {
    @SyncAsyncTest
    public void withNoPort() throws Exception {
        final HttpPipeline pipeline = createPipeline("localhost", "ftp://localhost");
        final HttpRequest request = createHttpRequest("ftp://www.example.com");
        SyncAsyncExtension.execute(
            () -> sendRequestSync(pipeline, request),
            () -> sendRequest(pipeline, request)
        );
    }

    @SyncAsyncTest
    public void withPort() throws Exception {
        final HttpPipeline pipeline = createPipeline("localhost", "ftp://localhost:1234");
        final HttpRequest request = createHttpRequest("ftp://www.example.com:1234");
        SyncAsyncExtension.execute(
            () -> sendRequestSync(pipeline, request),
            () -> sendRequest(pipeline, request)
        );
    }

    private static HttpPipeline createPipeline(String host, String expectedUrl) {
        return new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(new HostPolicy(host),
                (context, next) -> {
                    assertEquals(expectedUrl, context.getHttpRequest().getUrl().toString());
                    return next.process();
                })
            .build();
    }

    private static HttpRequest createHttpRequest(String url) throws MalformedURLException {
        return new HttpRequest(HttpMethod.GET, new URL(url));
    }

    private HttpResponse sendRequest(HttpPipeline pipeline, HttpRequest httpRequest) {
        return pipeline.send(httpRequest).block();
    }

    private HttpResponse sendRequestSync(HttpPipeline pipeline, HttpRequest httpRequest) {
        return pipeline.sendSync(httpRequest, Context.NONE);
    }
}
