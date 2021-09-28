// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;

import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * HttpPipelinePolicy to append headers required for Download and Delete in TokenCredential client cases
 */
public final class DownloadDeleteWithTokenCredentialPolicy implements HttpPipelinePolicy {
    private static final String X_MS_HOST_HEADER = "x-ms-host";
    private final String xMsHostValue;

    /**
     * Created with a non-null resourceHostName value
     * @param resourceHostName Host name of the ACS resource
     */
    public DownloadDeleteWithTokenCredentialPolicy(String resourceHostName) {
        Objects.requireNonNull(resourceHostName, "'resourceHostName' cannot be a null value.");
        xMsHostValue = resourceHostName;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (!xMsHostValue.isEmpty()) {
            context.getHttpRequest().setHeader(X_MS_HOST_HEADER, xMsHostValue);
        }
        return next.process();
    }

}
