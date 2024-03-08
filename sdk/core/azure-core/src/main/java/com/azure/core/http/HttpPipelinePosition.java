// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;

/**
 * Enum representing the position in an {@link HttpPipeline} to place an {@link HttpPipelinePolicy}.
 *
 * <p>This enum encapsulates the positions where an HTTP pipeline policy can be placed in the HTTP pipeline. The positions
 * are before or after a {@link RetryPolicy}.</p>
 *
 * <p>Each position is represented by an enum constant. For example, you can use {@link #PER_CALL} to represent the position
 * before a RetryPolicy, and {@link #PER_RETRY} to represent the position after a RetryPolicy.</p>
 *
 * <p>The PER_CALL position means that the policy will only be
 * invoked once per pipeline invocation (service call), and the PER_RETRY position means that the policy will be invoked
 * every time a request is sent (including retries).</p>
 */
public enum HttpPipelinePosition {
    /**
     * Policy is placed before a {@link RetryPolicy} and will only be invoked once per pipeline invocation (service
     * call).
     */
    PER_CALL,

    /**
     * Policy is placed after a {@link RetryPolicy} and will be invoked every time a request is sent.
     * <p>
     * The policy will be invoked at least once for the initial service call and each time the request is retried.
     */
    PER_RETRY
}
