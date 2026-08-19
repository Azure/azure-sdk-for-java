// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.storage.common.implementation.Constants;

/**
 * Logic shared by {@link ExpectContinuePolicy} and {@link ExpectContinueOnThrottlePolicy}.
 * <p>
 * RESERVED FOR INTERNAL USE.
 */
final class ExpectContinuePolicyHelper {
    private static final String CONTINUE = "100-continue";

    private ExpectContinuePolicyHelper() {
    }

    /**
     * Reads the opt out allowing {@code Expect: 100-continue} to be turned off without a code change.
     *
     * @param configuration The configuration to read the opt out from.
     * @return Whether the header has been turned off.
     */
    static boolean isDisabled(Configuration configuration) {
        return configuration.get(Constants.PROPERTY_AZURE_STORAGE_DISABLE_EXPECT_CONTINUE_HEADER, false);
    }

    /**
     * Determines whether the request carries a body at least as large as the given threshold. A body whose length
     * cannot be determined ahead of time is always eligible.
     *
     * @param request The request about to be sent.
     * @param contentLengthThreshold The minimum request {@code Content-Length} for applying the header.
     * @return Whether the request is eligible for the header.
     */
    static boolean isEligible(HttpRequest request, long contentLengthThreshold) {
        BinaryData body = request.getBodyAsBinaryData();
        if (body == null) {
            return false;
        }

        Long contentLength = getContentLength(request, body);
        return contentLength == null || contentLength >= contentLengthThreshold;
    }

    /**
     * Applies {@code Expect: 100-continue} to the request.
     *
     * @param request The request about to be sent.
     */
    static void applyHeader(HttpRequest request) {
        request.getHeaders().set(HttpHeaderName.EXPECT, CONTINUE);
    }

    private static Long getContentLength(HttpRequest request, BinaryData body) {
        String headerValue = request.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH);
        if (headerValue != null) {
            try {
                return Long.parseLong(headerValue);
            } catch (NumberFormatException ex) {
                // Fall back to the body's own length.
            }
        }

        return body.getLength();
    }
}
