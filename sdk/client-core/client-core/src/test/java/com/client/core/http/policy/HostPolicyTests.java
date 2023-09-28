// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.client.core.http.policy;

import com.client.core.SyncAsyncExtension;
import com.client.core.SyncAsyncTest;
import com.client.core.http.HttpMethod;
import com.client.core.http.HttpPipeline;
import com.client.core.http.HttpPipelineBuilder;
import com.client.core.http.HttpRequest;
import com.client.core.http.HttpResponse;
import com.client.core.http.clients.NoOpHttpClient;
import com.client.core.util.Context;

import java.net.MalformedURLException;

import static com.client.core.CoreTestUtils.createUrl;
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
        return new HttpRequest(HttpMethod.GET, createUrl(url));
    }

    private HttpResponse sendRequest(HttpPipeline pipeline, HttpRequest httpRequest) {
        return pipeline.send(httpRequest).block();
    }

    private HttpResponse sendRequestSync(HttpPipeline pipeline, HttpRequest httpRequest) {
        return pipeline.sendSync(httpRequest, Context.NONE);
    }
}
