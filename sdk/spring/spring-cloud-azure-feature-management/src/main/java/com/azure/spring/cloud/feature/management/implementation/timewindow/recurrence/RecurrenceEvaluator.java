// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence;

import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowFilterSettings;

import java.time.ZonedDateTime;

public class RecurrenceEvaluator {
    private final TimeWindowFilterSettings settings;
    private final ZonedDateTime now;

    public RecurrenceEvaluator(TimeWindowFilterSettings settings, ZonedDateTime now) {
        this.settings = settings;
        this.now = now;
    }

    /**
     * Checks if a provided timestamp is within any recurring time window specified
     * by the Recurrence section in the time window filter settings.
     * @return True if the time stamp is within any recurring time window, false otherwise.
     */
    public boolean isMatch() {
        // todo: add evaluation logic
        return false;
    }
}
