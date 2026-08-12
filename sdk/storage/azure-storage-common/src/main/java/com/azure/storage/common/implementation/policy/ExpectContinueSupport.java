// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.storage.common.implementation.Constants;

/**
 * Logic shared by {@link ExpectContinuePolicy} and {@link ExpectContinueOnThrottlePolicy}. The two policies are
 * siblings rather than a hierarchy, so that neither advertises an extension point it does not actually support.
 * <p>
 * RESERVED FOR INTERNAL USE.
 */
final class ExpectContinueSupport {
    private static final String CONTINUE = "100-continue";

    private ExpectContinueSupport() {
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
     * Determines whether the request carries a body large enough to be worth the additional round trip.
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

        // A body whose length cannot be determined ahead of time is always eligible, matching the behavior of skipping
        // the check rather than buffering the body to measure it.
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

    /*
     * Gets the length of the request body, preferring the Content-Length header as that is what is sent on the wire.
     * Returns null when the length cannot be determined.
     */
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
