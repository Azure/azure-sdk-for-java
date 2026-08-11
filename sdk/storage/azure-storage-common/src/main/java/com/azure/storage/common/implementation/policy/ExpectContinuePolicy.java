// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.policy.HttpPipelineSyncPolicy;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.storage.common.implementation.Constants;

/**
 * Pipeline policy that applies the HTTP header {@code Expect: 100-continue} to requests that carry a body.
 * <p>
 * This policy must be placed after the retry policy so that it is evaluated on every retry attempt.
 * <p>
 * RESERVED FOR INTERNAL USE.
 */
public class ExpectContinuePolicy extends HttpPipelineSyncPolicy {
    private static final String CONTINUE = "100-continue";

    private final long contentLengthThreshold;
    private final boolean disabled;

    /**
     * Creates a policy that applies {@code Expect: 100-continue} to every request with a body at least as large as the
     * given threshold.
     *
     * @param contentLengthThreshold The minimum request {@code Content-Length} for applying the header. A null value
     * means every request with a body is eligible.
     */
    public ExpectContinuePolicy(Long contentLengthThreshold) {
        this(contentLengthThreshold, Configuration.getGlobalConfiguration());
    }

    /**
     * Creates a policy reading the opt out from the given configuration. For testing.
     *
     * @param contentLengthThreshold The minimum request {@code Content-Length} for applying the header.
     * @param configuration The configuration to read the opt out from.
     */
    ExpectContinuePolicy(Long contentLengthThreshold, Configuration configuration) {
        this.contentLengthThreshold = contentLengthThreshold == null ? 0 : contentLengthThreshold;
        // Read once here rather than per request. This is a process-level opt out, so it cannot meaningfully change
        // over the lifetime of a client.
        this.disabled = configuration.get(Constants.PROPERTY_AZURE_STORAGE_DISABLE_EXPECT_CONTINUE_HEADER, false);
    }

    @Override
    protected void beforeSendingRequest(HttpPipelineCallContext context) {
        HttpRequest request = context.getHttpRequest();
        if (shouldApplyExpectContinue(request)) {
            request.getHeaders().set(HttpHeaderName.EXPECT, CONTINUE);
        }
    }

    /**
     * Determines whether {@code Expect: 100-continue} should be applied to the request.
     *
     * @param request The request about to be sent.
     * @return Whether the header should be applied.
     */
    boolean shouldApplyExpectContinue(HttpRequest request) {
        if (disabled) {
            return false;
        }

        BinaryData body = request.getBodyAsBinaryData();
        if (body == null) {
            return false;
        }

        // A body whose length cannot be determined ahead of time is always eligible, matching the behavior of skipping
        // the check rather than buffering the body to measure it.
        Long contentLength = getContentLength(request, body);
        return contentLength == null || contentLength >= contentLengthThreshold;
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
