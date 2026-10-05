// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.RequestOptions;

/**
 * Provides access to internal request customization on {@link RequestOptions}.
 */
public final class RequestOptionsAccessHelper {
    private static RequestOptionsAccessor accessor;

    /**
     * Defines access to the internal methods on {@link RequestOptions}.
     */
    public interface RequestOptionsAccessor {
        /**
         * Applies the customizations configured on the request options.
         *
         * @param request The HTTP request to customize.
         * @param options The request options.
         */
        void applyRequestOptions(HttpRequest request, RequestOptions options);

        /**
         * Determines whether an unexpected response should produce an exception.
         *
         * @param options The request options.
         * @return Whether an unexpected response should produce an exception.
         */
        boolean shouldThrowException(RequestOptions options);
    }

    /**
     * Applies the customizations configured on the request options.
     *
     * @param request The HTTP request to customize.
     * @param options The request options.
     */
    public static void applyRequestOptions(HttpRequest request, RequestOptions options) {
        accessor.applyRequestOptions(request, options);
    }

    /**
     * Determines whether an unexpected response should produce an exception.
     *
     * @param options The request options.
     * @return Whether an unexpected response should produce an exception.
     */
    public static boolean shouldThrowException(RequestOptions options) {
        return accessor.shouldThrowException(options);
    }

    /**
     * Sets the accessor for the internal methods on {@link RequestOptions}.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(RequestOptionsAccessor accessor) {
        RequestOptionsAccessHelper.accessor = accessor;
    }

    private RequestOptionsAccessHelper() {
    }
}
