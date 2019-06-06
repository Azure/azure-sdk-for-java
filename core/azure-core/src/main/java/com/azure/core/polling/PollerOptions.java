// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.polling;

import java.time.Duration;

/**
 * Place to provide configuration options for Poller
 */
public class PollerOptions {

    private String negativeValueFormat = "Negative or zero poll interval not allowed.";
    private Duration pollInterval;

    /**
     * Constructor
     * @param pollInterval     This will ensure that poll happens only once in pollInterval
     */
    public PollerOptions(Duration pollInterval) {
        validateValuesAndThrow(pollInterval);
        this.pollInterval = pollInterval;
    }

    /*
     * Validatations for non negative values and other validations
     */
    private void validateValuesAndThrow(Duration pollInterval) {

        if (pollInterval.toNanos() <= 0) {
            throw new IllegalArgumentException(negativeValueFormat);
        }
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
