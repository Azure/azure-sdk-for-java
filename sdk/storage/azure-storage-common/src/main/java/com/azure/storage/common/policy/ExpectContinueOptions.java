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
 * <p>For more information on the header itself, see
 * <a href="https://datatracker.ietf.org/doc/html/rfc9110#section-10.1.1">RFC 9110 section 10.1.1</a>.</p>
 *
 * <p><strong>Whether the header has any effect depends on the HTTP client in use.</strong> Setting it does not by
 * itself make a client wait for the service to respond before sending the body. Of the supported transports, only
 * {@code azure-core-http-okhttp} performs the handshake. {@code azure-core-http-netty} and
 * {@code azure-core-http-vertx} send the header but stream the body immediately, and
 * {@code azure-core-http-jdk-httpclient} drops the header altogether, as {@code Expect} is restricted by
 * {@code java.net.http.HttpClient}. On those transports these options are accepted but save no bandwidth.</p>
 */
@Fluent
public final class ExpectContinueOptions {
    private static final Duration DEFAULT_THROTTLE_INTERVAL = Duration.ofMinutes(1);

    private ExpectContinueMode mode = ExpectContinueMode.APPLY_ON_THROTTLE;
    private Long contentLengthThreshold;
    private Duration throttleInterval = DEFAULT_THROTTLE_INTERVAL;

    /**
     * Creates a new {@link ExpectContinueOptions} with default parameters applied.
     */
    public ExpectContinueOptions() {
    }

    /**
     * Gets the mode determining when {@code Expect: 100-continue} is applied.
     *
     * @return The mode. Defaults to {@link ExpectContinueMode#APPLY_ON_THROTTLE}.
     */
    public ExpectContinueMode getMode() {
        return mode;
    }

    /**
     * Sets the mode determining when {@code Expect: 100-continue} is applied.
     *
     * @param mode The mode. A null value resets this to {@link ExpectContinueMode#APPLY_ON_THROTTLE}.
     * @return The updated options.
     */
    public ExpectContinueOptions setMode(ExpectContinueMode mode) {
        this.mode = mode == null ? ExpectContinueMode.APPLY_ON_THROTTLE : mode;
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
    public ExpectContinueOptions setContentLengthThreshold(Long contentLengthThreshold) {
        this.contentLengthThreshold = contentLengthThreshold;
        return this;
    }

    /**
     * Gets the interval for which {@code Expect: 100-continue} is applied after a triggering response is received from
     * the service. Only used in mode {@link ExpectContinueMode#APPLY_ON_THROTTLE}.
     *
     * @return The interval. Defaults to one minute.
     */
    public Duration getThrottleInterval() {
        return throttleInterval;
    }

    /**
     * Sets the interval for which {@code Expect: 100-continue} is applied after a triggering response is received from
     * the service. Only used in mode {@link ExpectContinueMode#APPLY_ON_THROTTLE}.
     *
     * @param throttleInterval The interval. A null value resets this to the default of one minute.
     * @return The updated options.
     */
    public ExpectContinueOptions setThrottleInterval(Duration throttleInterval) {
        this.throttleInterval = throttleInterval == null ? DEFAULT_THROTTLE_INTERVAL : throttleInterval;
        return this;
    }
}
