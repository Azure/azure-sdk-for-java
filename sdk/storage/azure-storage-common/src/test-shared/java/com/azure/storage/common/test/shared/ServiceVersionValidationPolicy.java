// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

public final class ServiceVersionValidationPolicy implements HttpPipelinePolicy {

    private final String expectedServiceVersion;

    public ServiceVersionValidationPolicy(String expectedServiceVersion) {
        this.expectedServiceVersion = expectedServiceVersion;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext httpPipelineCallContext, HttpPipelineNextPolicy httpPipelineNextPolicy) {
        String actualServiceVersion = httpPipelineCallContext.getHttpRequest().getHeaders().getValue("x-ms-version");
        assert expectedServiceVersion.equals(actualServiceVersion);
        return httpPipelineNextPolicy.process();
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.PER_CALL;
    }
}
