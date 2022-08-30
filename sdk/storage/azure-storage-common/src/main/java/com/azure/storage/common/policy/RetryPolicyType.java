// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.util.Configuration;

import static com.azure.core.util.Configuration.getGlobalConfiguration;

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
    FIXED;

    static final String EXPONENTIAL_VALUE = "exponential";
    static final String FIXED_VALUE = "fixed";

    static final RetryPolicyType ENVIRONMENT_RETRY_POLICY_TYPE = fromConfiguration(getGlobalConfiguration());

    static RetryPolicyType fromConfiguration(Configuration configuration) {
        String rawType = configuration.get("AZURE_STORAGE_RETRY_TYPE", "none");

        RetryPolicyType type;
        if (EXPONENTIAL_VALUE.equalsIgnoreCase(rawType)) {
            type = EXPONENTIAL;
        } else if (FIXED_VALUE.equalsIgnoreCase(rawType)) {
            type = FIXED;
        } else {
            type = EXPONENTIAL;
        }

        return type;
    }
}
