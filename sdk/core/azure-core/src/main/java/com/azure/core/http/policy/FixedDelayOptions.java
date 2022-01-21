package com.azure.core.http.policy;

import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.Objects;

/**
 * The configuration for exponential backoff.
 */
public class FixedDelayOptions {
    private Integer maxRetries;
    private Duration delay;

    /**
     * Gets the max retry attempts that can be made.
     *
     * @return The max retry attempts that can be made.
     */
    public Integer getMaxRetries() {
        return maxRetries;
    }

    /**
     * Sets the max retry attempts that can be made.
     *
     * @param maxRetries the max retry attempts that can be made.
     *
     * @return The updated {@link FixedDelayOptions}
     */
    public FixedDelayOptions setMaxRetries(Integer maxRetries) {
        Objects.requireNonNull(maxRetries, "'maxRetries' cannot be null.");
        if (maxRetries < 0) {
            ClientLogger logger = new ClientLogger(FixedDelayOptions.class);
            throw logger.logExceptionAsError(new IllegalArgumentException("Max retries cannot be less than 0."));
        }
        this.maxRetries = maxRetries;
        return this;
    }

    /**
     * Gets the max retry attempts that can be made.
     *
     * @return The max retry attempts that can be made.
     */
    public Duration getDelay() {
        return delay;
    }

    /**
     * Sets the fixed delay duration between retry attempts.
     *
     * @param delay the fixed delay duration between retry attempts.
     *
     * @return The updated {@link FixedDelayOptions}
     */
    public FixedDelayOptions setDelay(Duration delay) {
        this.delay = Objects.requireNonNull(delay, "'delay' cannot be null.");
        return this;
    }
}
