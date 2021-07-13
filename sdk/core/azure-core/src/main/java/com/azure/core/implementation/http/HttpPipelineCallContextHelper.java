// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.util.Context;

/**
 * Helper class to access private values of {@link HttpPipelineCallContext} across package boundaries.
 */
public final class HttpPipelineCallContextHelper {
    private static HttpPipelineCallContextAccessor accessor;

    private HttpPipelineCallContextHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link HttpPipelineCallContext} instance.
     */
    public interface HttpPipelineCallContextAccessor {
        Context getContext(HttpPipelineCallContext callContext);
    }

    /**
     * The method called from {@link HttpPipelineCallContext} to set it's accessor.
     *
     * @param callContextAccessor The accessor.
     */
    public static void setAccessor(final HttpPipelineCallContextAccessor callContextAccessor) {
        accessor = callContextAccessor;
    }

    public static Context getContext(HttpPipelineCallContext callContext) {
        return accessor.getContext(callContext);
    }
}
