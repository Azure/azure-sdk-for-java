// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

public class HttpPipelineTest extends PerfStressTest<HttpPipelineOptions> {
    private static final int BUFFER_SIZE = 16 * 1024 * 1024;

    private final HttpPipeline httpPipeline;
    private final byte[] buffer = new byte[BUFFER_SIZE];

    public HttpPipelineTest(HttpPipelineOptions options) {
        super(options);

        HttpPipelineBuilder builder = new HttpPipelineBuilder();

        if (httpClient != null) {
            builder = builder.httpClient(httpClient);
        }

        if (policies != null) {
            ArrayList<HttpPipelinePolicy> policyList = new ArrayList<HttpPipelinePolicy>();
            for (HttpPipelinePolicy policy : policies) {
                policyList.add(policy);
            }
            builder.policies(policyList.toArray(new HttpPipelinePolicy[0]));
        }

        httpPipeline = builder.build();
    }

    @Override
    public void run() {
        runAsync().block();
    }

    @Override
    public Mono<Void> runAsync() {
        HttpRequest request = new HttpRequest(HttpMethod.GET, options.getUrl());
        return httpPipeline
            .send(request)
            .flatMapMany(HttpResponse::getBody)
            .map(b -> {
                int readCount = 0;
                int remaining = b.remaining();
                while (readCount < remaining) {
                    int expectedReadCount = Math.min(remaining - readCount, BUFFER_SIZE);
                    b.get(buffer, 0, expectedReadCount);
                    readCount += expectedReadCount;
                }

                return 1;
            })
            .then();
    }
}
