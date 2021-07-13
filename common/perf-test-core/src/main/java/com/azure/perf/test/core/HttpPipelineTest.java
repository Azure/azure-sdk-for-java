// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.beust.jcommander.Parameter;
import reactor.core.publisher.Mono;

import java.net.URL;

public class HttpPipelineTest extends PerfStressTest<HttpPipelineTest.HttpPipelineOptions> {
    private final HttpPipeline httpPipeline;

    public HttpPipelineTest(HttpPipelineTest.HttpPipelineOptions options) {
        super(options);

        HttpPipelineBuilder builder = new HttpPipelineBuilder();

        if (httpClient != null) {
            builder = builder.httpClient(httpClient);
        }

        if (policies != null) {
            for (HttpPipelinePolicy policy : policies) {
                builder.addPolicy(policy);
            }
        }

        httpPipeline = builder.build();
    }

    @Override
    public void run() {
        HttpRequest request = new HttpRequest(HttpMethod.GET, options.getUrl());
        httpPipeline.send(request).block();
    }

    @Override
    public Mono<Void> runAsync() {
        HttpRequest request = new HttpRequest(HttpMethod.GET, options.getUrl());
        return httpPipeline.send(request).then();
    }

    public class HttpPipelineOptions extends PerfStressOptions {
        @Parameter(names = { "-u", "--url" }, description = "URL to fetch")
        private URL url;

        public URL getUrl() {
            return url;
        }
    }
}
