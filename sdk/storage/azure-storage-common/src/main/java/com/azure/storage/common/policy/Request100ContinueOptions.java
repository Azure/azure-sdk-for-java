// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.annotation.Fluent;

import java.time.Duration;

/**
 * This class contains configuration for applying the HTTP header {@code Expect: 100-continue} to requests that carry a
 * body.
 *
 * <p>When the header is applied, the request headers are sent to the service without the body, and the body is only
 * sent once the service has responded {@code 100 Continue}. This costs an additional network round trip, but it avoids
 * sending a large body that the service is going to reject anyway, which can significantly reduce the bandwidth spent
 * retrying while the service is throttling.</p>
 *
 * <p>For more information, see
 * <a href="https://learn.microsoft.com/rest/api/storageservices/setting-timeouts-for-blob-service-operations">the
 * Storage service documentation</a>.</p>
 */
@Fluent
public final class Request100ContinueOptions {
    private static final Duration DEFAULT_AUTO_INTERVAL = Duration.ofMinutes(1);

    private Request100ContinueMode mode = Request100ContinueMode.AUTO;
    private Long contentLengthThreshold;
    private Duration autoInterval = DEFAULT_AUTO_INTERVAL;

    /**
     * Creates a new {@link Request100ContinueOptions} with default parameters applied.
     */
    public Request100ContinueOptions() {
    }

    /**
     * Gets the mode determining when {@code Expect: 100-continue} is applied.
     *
     * @return The mode. Defaults to {@link Request100ContinueMode#AUTO}.
     */
    public Request100ContinueMode getMode() {
        return mode;
    }

    /**
     * Sets the mode determining when {@code Expect: 100-continue} is applied.
     *
     * @param mode The mode. A null value resets this to {@link Request100ContinueMode#AUTO}.
     * @return The updated options.
     */
    public Request100ContinueOptions setMode(Request100ContinueMode mode) {
        this.mode = mode == null ? Request100ContinueMode.AUTO : mode;
        return this;
    }

    /**
     * Gets the minimum request {@code Content-Length} for applying {@code Expect: 100-continue}. Requests whose body
     * length is known and smaller than this value will not have the header applied. Requests whose body length cannot
     * be determined ahead of time are always eligible for the header.
     *
     * @return The threshold in bytes, or null if every request with a body is eligible.
     */
    public Long getContentLengthThreshold() {
        return contentLengthThreshold;
    }

    /**
     * Sets the minimum request {@code Content-Length} for applying {@code Expect: 100-continue}. Requests whose body
     * length is known and smaller than this value will not have the header applied. Requests whose body length cannot
     * be determined ahead of time are always eligible for the header.
     *
     * @param contentLengthThreshold The threshold in bytes. A null value means every request with a body is eligible.
     * @return The updated options.
     */
    public Request100ContinueOptions setContentLengthThreshold(Long contentLengthThreshold) {
        this.contentLengthThreshold = contentLengthThreshold;
        return this;
    }

    /**
     * Gets the interval for which {@code Expect: 100-continue} is applied after a triggering response is received from
     * the service. Only used in mode {@link Request100ContinueMode#AUTO}.
     *
     * @return The interval. Defaults to one minute.
     */
    public Duration getAutoInterval() {
        return autoInterval;
    }

    /**
     * Sets the interval for which {@code Expect: 100-continue} is applied after a triggering response is received from
     * the service. Only used in mode {@link Request100ContinueMode#AUTO}.
     *
     * @param autoInterval The interval. A null value resets this to the default of one minute.
     * @return The updated options.
     */
    public Request100ContinueOptions setAutoInterval(Duration autoInterval) {
        this.autoInterval = autoInterval == null ? DEFAULT_AUTO_INTERVAL : autoInterval;
        return this;
    }
}
