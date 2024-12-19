// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.http.pipeline;

/**
 * Enum representing the positions where {@link HttpPipelinePolicy}s can be added when configuring an
 * {@link HttpPipelineBuilder}.
 * <p>
 * For policies that are optional in an {@link HttpPipeline} if the policy isn't present the policy added will be
 * positioned adjacent to where it would have existed.
 */
public enum HttpPipelinePosition {
    /**
     * The policy will be positioned adjacent to the {@link HttpRedirectPolicy}.
     */
    REDIRECT,

    /**
     * The policy will be positioned adjacent to the {@link HttpRetryPolicy}.
     */
    RETRY,

    /**
     * The policy will be positioned adjacent to the {@link HttpCredentialPolicy}.
     */
    AUTHENTICATION,

    /**
     * The policy will be positioned adjacent to the {@link HttpLoggingPolicy}.
     */
    LOGGING,

    /**
     * The policy will be positioned adjacent to the {@link HttpTelemetryPolicy}.
     */
    TELEMETRY;
}
