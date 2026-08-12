// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.policy.ExpectContinueOnThrottlePolicy;
import com.azure.storage.common.implementation.policy.ExpectContinuePolicy;
import com.azure.storage.common.policy.ExpectContinueOptions;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RequestRetryPolicy;

import java.util.List;

/**
 * This class provides helper methods for client builders.
 *
 * RESERVED FOR INTERNAL USE.
 */
public final class BuilderUtils {
    private BuilderUtils() {
    }

    public static RequestRetryPolicy createRetryPolicy(RequestRetryOptions retryOptions, RetryOptions coreRetryOptions,
        ClientLogger logger) {
        if (retryOptions != null && coreRetryOptions != null) {
            throw logger.logExceptionAsWarning(new IllegalStateException(
                "'retryOptions(RequestRetryOptions)' and 'retryOptions(RetryOptions)' cannot both be set"));
        }
        if (coreRetryOptions != null) {
            retryOptions = RequestRetryOptions.fromRetryOptions(coreRetryOptions, null, null);
        }
        if (retryOptions == null) {
            retryOptions = new RequestRetryOptions();
        }
        return new RequestRetryPolicy(retryOptions);
    }

    /**
     * Adds the policy applying HTTP header {@code Expect: 100-continue}, if the given options call for one.
     * <p>
     * This must be called after the retry policy has been added so that the policy is evaluated on every retry
     * attempt, and before the credential policies as headers may affect the string to sign of the request.
     *
     * @param policies The pipeline policies being built up.
     * @param expectContinueOptions The options, or null to use the default behavior.
     */
    public static void addExpectContinuePolicy(List<HttpPipelinePolicy> policies,
        ExpectContinueOptions expectContinueOptions) {
        ExpectContinueOptions options
            = expectContinueOptions == null ? new ExpectContinueOptions() : expectContinueOptions;
        Long threshold = options.getContentLengthThreshold();

        switch (options.getMode()) {
            case ON:
                policies.add(new ExpectContinuePolicy(threshold));
                break;

            case OFF:
                break;

            case APPLY_ON_THROTTLE:
            default:
                policies.add(new ExpectContinueOnThrottlePolicy(options.getThrottleInterval(), threshold));
                break;
        }
    }
}
