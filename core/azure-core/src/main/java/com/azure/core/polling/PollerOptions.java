// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.polling;

import java.time.Duration;
import java.util.Objects;

/**
 * This class provide configuration options needed to create Poller.
 *
 * <p><strong>Place holder for Poller Options</strong></p>
 */
public class PollerOptions {

    private String negativeValueFormat = "Negative or zero poll interval not allowed.";
    private Duration pollInterval;

    /**
     * constructor
     * @param pollInterval It ensure that polling happens only once in given pollInterval.
     *
     * @throws IllegalArgumentException for {@code null} , zero and negative values.
     */
    public PollerOptions(Duration pollInterval) throws IllegalArgumentException {

        Objects.requireNonNull(pollInterval, "The poll interval input parameter cannot be null.");
        if (pollInterval.toNanos() <= 0) {
            throw new IllegalArgumentException(negativeValueFormat);
        }
        this.pollInterval = pollInterval;
    }

    /*
     * Validatations for non negative values and other validations
     */
    private void validateValuesAndThrow(Duration pollInterval) {


    }

    /**
     * get poll interval
     *
     * @return Duration poll interval
     */
    public Duration pollInterval() {
        return pollInterval;
    }

}
