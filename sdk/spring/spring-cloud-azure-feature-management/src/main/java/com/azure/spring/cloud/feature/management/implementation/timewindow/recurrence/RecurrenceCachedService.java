// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence;

import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowFilterSettings;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import java.time.ZonedDateTime;

public class RecurrenceCachedService {
    @Cacheable(value = "settings", key = "#settings")
    public static ZonedDateTime getClosestTime(TimeWindowFilterSettings settings, ZonedDateTime now) {
        final RecurrenceEvaluator evaluator = new RecurrenceEvaluator(settings, now);
        return evaluator.getClosestStart();
    }

    @CachePut(value = "settings", key = "#settings")
    public static ZonedDateTime updateClosestTime(TimeWindowFilterSettings settings, ZonedDateTime now) {
        final RecurrenceEvaluator evaluator = new RecurrenceEvaluator(settings, now);
        return evaluator.getClosestStart();
    }
}
