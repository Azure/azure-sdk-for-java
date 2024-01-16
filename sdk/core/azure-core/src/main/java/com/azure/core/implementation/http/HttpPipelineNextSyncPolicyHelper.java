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

    private HttpPipelineNextSyncPolicyHelper() { }

    public interface HttpPipelineNextSyncPolicyAccessor {
        HttpPipelineNextPolicy toAsyncPolicy(HttpPipelineNextSyncPolicy syncPolicy);
    }

    public static void setAccessor(final HttpPipelineNextSyncPolicyAccessor callContextAccessor) {
        accessor = callContextAccessor;
    }

    public static HttpPipelineNextPolicy toAsyncPolicy(HttpPipelineNextSyncPolicy syncPolicy) {
        return accessor.toAsyncPolicy(syncPolicy);
    }
}
