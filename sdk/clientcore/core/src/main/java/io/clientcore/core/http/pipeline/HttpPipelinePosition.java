// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.http.pipeline;

/**
 * Enum representing the positions where {@link HttpPipelinePolicy}s can be added when configuring an
 * {@link HttpPipelineBuilder}.
 * <p>
 * For policies that are optional in an {@link HttpPipeline} if the policy isn't present the policy added will be
 * positioned adjacent to where it would have existed. For example, if the {@link HttpPipeline} doesn't have the
 * {@link HttpPipelineBuilder#setInstrumentationPolicy(HttpInstrumentationPolicy)} policy configured but
 * {@link HttpPipelineBuilder#addPolicyBefore(HttpPipelinePolicy, HttpPipelinePosition)} is called with
 * {@link #INSTRUMENTATION} the policy added will be positioned before the placeholder for the instrumentation policy.
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
     * The policy will be positioned adjacent to the {@link HttpInstrumentationPolicy}.
     */
    INSTRUMENTATION
}
