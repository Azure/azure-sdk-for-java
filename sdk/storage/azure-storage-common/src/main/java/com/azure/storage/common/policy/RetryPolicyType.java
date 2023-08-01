// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

/**
 * This type holds possible options for retry backoff algorithms. They may be used with {@link RequestRetryOptions}.
 */
public enum RetryPolicyType {
    /**
     * Tells the pipeline to use an exponential back-off retry policy.
     */
    EXPONENTIAL,

    /**
     * Tells the pipeline to use a fixed back-off retry policy.
     */
    FIXED
}
