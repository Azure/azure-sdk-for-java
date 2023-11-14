// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.filters;

import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowFilterSettings;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceEvaluator;
import com.azure.spring.cloud.feature.management.models.FeatureFilterEvaluationContext;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;

import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_END;
import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_START;

/**
 * A feature filter that can be used at activate a feature based on a time window.
 */
public final class TimeWindowFilter implements FeatureFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeWindowFilter.class);

    /**
     * Evaluates whether a feature is enabled based on a configurable time window.
     *
     * @param context The feature evaluation context.
     * @return True if the feature is enabled, false otherwise.
     */
    @Override
    public boolean evaluate(FeatureFilterEvaluationContext context) {
        final ObjectMapper objectMapper = JsonMapper.builder()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).build();
        final TimeWindowFilterSettings settings = objectMapper.convertValue(context.getParameters(), TimeWindowFilterSettings.class);
        final ZonedDateTime now = ZonedDateTime.now();

        if (settings.getStart() == null && settings.getEnd() == null) {
            LOGGER.warn("The {} feature filter is not valid for feature {}. It must specify either {}, {}, or both.",
                this.getClass().getSimpleName(), context.getName(), TIME_WINDOW_FILTER_SETTING_START,
                TIME_WINDOW_FILTER_SETTING_END);
            return false;
        }

        if ((settings.getStart() == null || now.isAfter(settings.getStart()))
            && (settings.getEnd() == null || now.isBefore(settings.getEnd()))) {
            return true;
        }

        if (settings.getRecurrence() != null) {
            return RecurrenceEvaluator.matchRecurrence(now, settings);
        }

        return false;
    }

}
