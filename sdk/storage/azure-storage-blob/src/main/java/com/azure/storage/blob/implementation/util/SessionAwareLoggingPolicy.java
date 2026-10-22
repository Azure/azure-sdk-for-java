// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Wraps an {@link HttpLoggingPolicy} so that CreateSession responses, which contain the session key and token, are
 * never passed to an HTTP logger. Session-authenticated requests also bypass the logger so an allow-listed
 * {@code Authorization} header cannot expose the session token or signature.
 * <p>
 * Requests that aren't session acquisitions are logged by the wrapped policy exactly as configured. Session
 * acquisition requests, identified by {@link SessionAcquirer#isAcquisitionRequest(HttpPipelineCallContext)}, bypass
 * this policy's logger.
 */
final class SessionAwareLoggingPolicy implements HttpPipelinePolicy {
    private final HttpPipelinePolicy loggingPolicy;

    /**
     * Creates a session-aware logging policy.
     *
     * @param loggingPolicy The logging policy used for all requests other than session acquisition.
     */
    SessionAwareLoggingPolicy(HttpPipelinePolicy loggingPolicy) {
        this.loggingPolicy = Objects.requireNonNull(loggingPolicy, "'loggingPolicy' cannot be null.");
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (!isSensitiveSessionRequest(context)) {
            return loggingPolicy.process(context, next);
        }
        return next.process();
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        if (!isSensitiveSessionRequest(context)) {
            return loggingPolicy.processSync(context, next);
        }
        return next.processSync();
    }

    private static boolean isSensitiveSessionRequest(HttpPipelineCallContext context) {
        if (SessionAcquirer.isAcquisitionRequest(context)) {
            return true;
        }
        String authorization = context.getHttpRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        return authorization != null && authorization.startsWith("Session ");
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        return loggingPolicy.getPipelinePosition();
    }
}
