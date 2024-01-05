// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ServiceVersionValidationPolicy implements HttpPipelinePolicy {
    private static final HttpHeaderName X_MS_VERSION = HttpHeaderName.fromString("x-ms-version");

    private final String expectedServiceVersion;

    public ServiceVersionValidationPolicy(String expectedServiceVersion) {
        this.expectedServiceVersion = expectedServiceVersion;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext httpPipelineCallContext,
        HttpPipelineNextPolicy httpPipelineNextPolicy) {
        String actualServiceVersion = httpPipelineCallContext.getHttpRequest().getHeaders().getValue(X_MS_VERSION);
        assertEquals(expectedServiceVersion, actualServiceVersion);
        return httpPipelineNextPolicy.process();
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.PER_CALL;
    }
}
