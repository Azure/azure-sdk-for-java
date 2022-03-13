// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.core.http.policy.RetryOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RequestRetryPolicy;

/**
 * This class provides helper methods for client builders.
 *
 * RESERVED FOR INTERNAL USE.
 */
public final class BuilderUtils {
    private BuilderUtils() { }

    public static RequestRetryPolicy createRetryPolicy(
        RequestRetryOptions retryOptions, RetryOptions coreRetryOptions, ClientLogger logger) {
        if (retryOptions != null && coreRetryOptions != null) {
            throw logger.logExceptionAsWarning(
                new IllegalStateException(
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
}
