// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

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
 * never logged with their body or passed to a custom logger.
 * <p>
 * Requests that aren't session acquisitions are logged by the wrapped policy exactly as configured. Session
 * acquisition requests, identified by {@link SessionAcquirer#isAcquisitionRequest(HttpPipelineCallContext)}, are
 * logged by the redacted policy, or not logged by this policy at all when no redacted policy is provided.
 */
final class SessionAwareLoggingPolicy implements HttpPipelinePolicy {
    private final HttpPipelinePolicy loggingPolicy;
    private final HttpPipelinePolicy acquisitionLoggingPolicy;

    /**
     * Creates a session-aware logging policy.
     *
     * @param loggingPolicy The logging policy used for all requests other than session acquisition.
     * @param acquisitionLoggingPolicy The body-redacted logging policy used for session acquisition, or {@code null}
     * to skip logging session acquisition in this policy.
     */
    SessionAwareLoggingPolicy(HttpPipelinePolicy loggingPolicy, HttpPipelinePolicy acquisitionLoggingPolicy) {
        this.loggingPolicy = Objects.requireNonNull(loggingPolicy, "'loggingPolicy' cannot be null.");
        this.acquisitionLoggingPolicy = acquisitionLoggingPolicy;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (!SessionAcquirer.isAcquisitionRequest(context)) {
            return loggingPolicy.process(context, next);
        }
        return acquisitionLoggingPolicy == null ? next.process() : acquisitionLoggingPolicy.process(context, next);
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        if (!SessionAcquirer.isAcquisitionRequest(context)) {
            return loggingPolicy.processSync(context, next);
        }
        return acquisitionLoggingPolicy == null
            ? next.processSync()
            : acquisitionLoggingPolicy.processSync(context, next);
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        return loggingPolicy.getPipelinePosition();
    }
}
