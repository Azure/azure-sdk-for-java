// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.SyncAsyncTest;
import com.azure.core.http.clients.NoOpHttpClient;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import java.net.MalformedURLException;

import static com.azure.core.CoreTestUtils.createUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HostPolicyTests {
    @SyncAsyncTest
    public void withNoPort() throws Exception {
        final HttpPipeline pipeline = createPipeline("localhost", "ftp://localhost");
        final HttpRequest request = createHttpRequest("ftp://www.example.com");
        sendRequest(pipeline, request);
    }

    @SyncAsyncTest
    public void withPort() throws Exception {
        final HttpPipeline pipeline = createPipeline("localhost", "ftp://localhost:1234");
        final HttpRequest request = createHttpRequest("ftp://www.example.com:1234");
        sendRequest(pipeline, request);
    }

    private static HttpPipeline createPipeline(String host, String expectedUrl) {
        return new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(new HostPolicy(host), (httpRequest, next) -> {
                assertEquals(expectedUrl, httpRequest.getUrl().toString());
                return next.process();
            })
            .build();
    }

    private static HttpRequest createHttpRequest(String url) throws MalformedURLException {
        return new HttpRequest(HttpMethod.GET, createUrl(url));
    }

    private Response<?> sendRequest(HttpPipeline pipeline, HttpRequest httpRequest) {
        return pipeline.send(httpRequest);
    }
}
