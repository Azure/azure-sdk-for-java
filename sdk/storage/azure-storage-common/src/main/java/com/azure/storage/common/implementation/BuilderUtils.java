// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.policy.ExpectContinueOnThrottlePolicy;
import com.azure.storage.common.policy.ExpectContinuePolicy;
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
     * Adds the policy applying HTTP header {@code Expect: 100-continue}, if the given options call for one. Must be
     * called after the retry policy has been added, and before the credential policies.
     * <p>
     * The header is only applied when options are supplied. It is not applied by default, as the handshake needs
     * support from the HTTP client and the default transport does not withhold the request body.
     *
     * @param policies The pipeline policies being built up.
     * @param expectContinueOptions The options, or null to leave the header unapplied.
     */
    public static void addExpectContinuePolicy(List<HttpPipelinePolicy> policies,
        ExpectContinueOptions expectContinueOptions) {
        if (expectContinueOptions == null) {
            return;
        }

        ExpectContinueOptions options = expectContinueOptions;
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
