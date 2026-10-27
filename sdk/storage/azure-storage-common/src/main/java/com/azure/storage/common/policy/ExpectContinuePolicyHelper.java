// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.Constants;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Logic shared by {@link ExpectContinuePolicy} and {@link ExpectContinueOnThrottlePolicy}.
 * <p>
 * RESERVED FOR INTERNAL USE.
 */
final class ExpectContinuePolicyHelper {
    private static final String CONTINUE = "100-continue";

    /*
     * Context key under which an AtomicBoolean holder is placed for the HTTP client to set when it observes an interim
     * "100 Continue" response. This must match the key the Netty client reads
     * (AzureNettyHttpClientContext.EXPECT_CONTINUE_RECEIVED_KEY). It cannot be shared as a constant because the two
     * modules do not share a common module beyond azure-core; ideally this contract is promoted into azure-core.
     */
    static final String EXPECT_CONTINUE_RECEIVED_KEY = "azure-http-client-expect-continue-received";

    private ExpectContinuePolicyHelper() {
    }

    /**
     * Reads the opt out allowing {@code Expect: 100-continue} to be turned off without a code change.
     *
     * @param configuration The configuration to read the opt out from. A null value uses the global configuration.
     * @return Whether the header has been turned off.
     */
    static boolean isDisabled(Configuration configuration) {
        Configuration source = configuration == null ? Configuration.getGlobalConfiguration() : configuration;
        return source.get(Constants.PROPERTY_AZURE_STORAGE_DISABLE_EXPECT_CONTINUE_HEADER, false);
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

    /**
     * Installs a holder in the call context that a supporting HTTP client sets when it observes an interim
     * {@code 100 Continue} response. Only call this when the header has been applied, so the observation is meaningful.
     *
     * @param context The pipeline call context.
     */
    static void installObservationHolder(HttpPipelineCallContext context) {
        context.setData(EXPECT_CONTINUE_RECEIVED_KEY, new AtomicBoolean(false));
    }

    /**
     * Logs whether the service engaged in the {@code Expect: 100-continue} handshake, when an observation was
     * requested for this request.
     *
     * @param context The pipeline call context.
     * @param logger The logger to log to.
     */
    static void logObservation(HttpPipelineCallContext context, ClientLogger logger) {
        Object holder = context.getData(EXPECT_CONTINUE_RECEIVED_KEY).orElse(null);
        if (holder instanceof AtomicBoolean) {
            boolean received = ((AtomicBoolean) holder).get();
            logger.verbose("Expect: 100-continue was {} by the service.", received ? "honored" : "not observed");
        }
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
