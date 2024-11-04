// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.filters;

import com.azure.spring.cloud.feature.management.implementation.FeatureFilterUtils;
import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowFilterSettings;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceConstants;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceEvaluator;
import com.azure.spring.cloud.feature.management.models.FeatureFilterEvaluationContext;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Map;

import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_END;
import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_RECURRENCE;
import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_START;

/**
 * A feature filter that can be used at activate a feature based on a time window.
 */
public final class TimeWindowFilter implements FeatureFilter {

    /**
     * Creates an instance of {@link TimeWindowFilter}
     */
    public TimeWindowFilter() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeWindowFilter.class);
    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).build();

    /**
     * Evaluates whether a feature is enabled based on a configurable time window.
     *
     * @param context The feature evaluation context.
     * @return True if the feature is enabled, false otherwise.
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean evaluate(FeatureFilterEvaluationContext context) {
        final Map<String, Object> parameters = context.getParameters();
        final Object recurrenceObject = parameters.get(FeatureFilterUtils.getKeyCase(parameters, TIME_WINDOW_FILTER_SETTING_RECURRENCE));
        if (recurrenceObject != null) {
            final Map<String, Object> recurrenceParameters = (Map<String, Object>) recurrenceObject;
            final Object patternObj = recurrenceParameters.get(FeatureFilterUtils.getKeyCase(recurrenceParameters, RecurrenceConstants.RECURRENCE_PATTERN));
            if (patternObj != null) {
                FeatureFilterUtils.updateValueFromMapToList((Map<String, Object>) patternObj, FeatureFilterUtils.getKeyCase((Map<String, Object>) patternObj, RecurrenceConstants.RECURRENCE_PATTERN_DAYS_OF_WEEK));
            }
        }

        final TimeWindowFilterSettings settings = OBJECT_MAPPER.convertValue(context.getParameters(), TimeWindowFilterSettings.class);
        final ZonedDateTime now = ZonedDateTime.now();

        if (settings.getStart() == null && settings.getEnd() == null) {
            LOGGER.warn("The {} feature filter is not valid for feature {}. It must specify either {}, {}, or both.",
                this.getClass().getSimpleName(), context.getName(), TIME_WINDOW_FILTER_SETTING_START,
                TIME_WINDOW_FILTER_SETTING_END);
            return false;
        }
        if (settings.getRecurrence() != null) {
            if (settings.getStart() != null && settings.getEnd() != null) {
                try {
                    return RecurrenceEvaluator.isMatch(settings, now);
                } catch (final IllegalArgumentException e) {
                    LOGGER.warn("The {} feature filter is not valid for feature {}. {}",
                        this.getClass().getSimpleName(), context.getName(), e.getMessage());
                    throw e;
                }
            }
            LOGGER.warn("The {} feature filter is not valid for feature {}. It must specify both {} and {} when Recurrence is not null.",
                this.getClass().getSimpleName(), context.getName(), TIME_WINDOW_FILTER_SETTING_START,
                TIME_WINDOW_FILTER_SETTING_END);
            return false;
        }

        return (settings.getStart() == null || now.isAfter(settings.getStart()))
            && (settings.getEnd() == null || now.isBefore(settings.getEnd()));
    }
}
