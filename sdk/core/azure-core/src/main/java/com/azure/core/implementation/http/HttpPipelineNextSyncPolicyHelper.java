// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http;

import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;

/**
 * Helper class to access private values of {@link HttpPipelineNextPolicy} across package boundaries.
 */
public final class HttpPipelineNextSyncPolicyHelper {
    private static HttpPipelineNextSyncPolicyAccessor accessor;

    private HttpPipelineNextSyncPolicyHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link HttpPipelineNextPolicy} instance.
     */
    public interface HttpPipelineNextSyncPolicyAccessor {
        /**
         * Converts the given {@link HttpPipelineNextSyncPolicy} to an {@link HttpPipelineNextPolicy}.
         *
         * @param syncPolicy The {@link HttpPipelineNextSyncPolicy} to convert.
         * @return The converted {@link HttpPipelineNextPolicy}.
         */
        HttpPipelineNextPolicy toAsyncPolicy(HttpPipelineNextSyncPolicy syncPolicy);
    }

    /**
     * Sets the accessor instance.
     *
     * @param callContextAccessor The accessor instance.
     */
    public static void setAccessor(final HttpPipelineNextSyncPolicyAccessor callContextAccessor) {
        accessor = callContextAccessor;
    }

    /**
     * Converts the given {@link HttpPipelineNextSyncPolicy} to an {@link HttpPipelineNextPolicy}.
     *
     * @param syncPolicy The {@link HttpPipelineNextSyncPolicy} to convert.
     * @return The converted {@link HttpPipelineNextPolicy}.
     */
    public static HttpPipelineNextPolicy toAsyncPolicy(HttpPipelineNextSyncPolicy syncPolicy) {
        return accessor.toAsyncPolicy(syncPolicy);
    }
}
