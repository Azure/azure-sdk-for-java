// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;

/**
 * Indicates the position in an {@link HttpPipeline} to place an {@link HttpPipelinePolicy}.
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
