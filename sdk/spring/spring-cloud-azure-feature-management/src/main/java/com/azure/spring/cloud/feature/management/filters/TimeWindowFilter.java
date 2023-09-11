// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.filters;

import com.azure.spring.cloud.feature.management.models.FeatureFilterEvaluationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.util.StringUtils;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_END;
import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_FILTERS;
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
        final TimeWindowFilterParameters filterParameters = new TimeWindowFilterParameters(context);
        if (!filterParameters.isValid()) {
            return false;
        }

        ZonedDateTime now = ZonedDateTime.now();
        // check if match start time(end time) when specify start time(end time)
        if (filterParameters.startTime != null && now.isBefore(filterParameters.startTime)
            || (filterParameters.endTime != null && now.isAfter(filterParameters.endTime))) {
            return false;
        }
        if (filterParameters.filterCronTabExpressions.size() == 0) {
            return true;
        }

        // Need to set Zone Offset when compare with crontab expression
        final ZoneOffset zoneOffset = filterParameters.startTime != null ? filterParameters.startTime.getOffset()
            : (filterParameters.endTime != null ? filterParameters.endTime.getOffset() : null);
        if (zoneOffset != null) {
            now = now.withZoneSameInstant(zoneOffset);
        }
        for (final CronExpression expression : filterParameters.filterCronTabExpressions) {
            final ZonedDateTime nextTriggerTime = expression.next(now);
            if (nextTriggerTime.isBefore(now.plusSeconds(1L))) {
                return true;
            }
        }
        return false;
    }

    private static final class TimeWindowFilterParameters {
        private final String name;
        private final String start;
        private final String end;
        private final List<String> filters = new ArrayList<>();
        private ZonedDateTime startTime;
        private ZonedDateTime endTime;
        private final List<CronExpression> filterCronTabExpressions = new ArrayList<>();

        private TimeWindowFilterParameters(FeatureFilterEvaluationContext context) {
            this.name = context.getName();
            this.start = (String) context.getParameters().get(TIME_WINDOW_FILTER_SETTING_START);
            this.end = (String) context.getParameters().get(TIME_WINDOW_FILTER_SETTING_END);
            final Object filtersObj = context.getParameters().get(TIME_WINDOW_FILTER_SETTING_FILTERS);
            if (filtersObj instanceof Map) {
                ((Map<?, ?>) filtersObj).values().forEach(filterObj -> this.filters.add(filterObj.toString()));
            } else if (filtersObj instanceof List<?>) {
                ((List<?>) filtersObj).forEach(filterObj -> this.filters.add(filterObj.toString()));
            }
        }

        private boolean isValid() {
            // Must specify at least one parameter
            if (!StringUtils.hasText(start) && !StringUtils.hasText(end) && filters.size() == 0) {
                LOGGER.warn("The {} feature filter is not valid for feature {}. It must specify at least one of {}, {}, {}.",
                    TimeWindowFilter.class.getSimpleName(), this.name, TIME_WINDOW_FILTER_SETTING_START,
                    TIME_WINDOW_FILTER_SETTING_END, TIME_WINDOW_FILTER_SETTING_FILTERS);
                return false;
            }
            // Check if date format is valid
            try {
                startTime = StringUtils.hasText(start)
                    ? ZonedDateTime.parse(start, DateTimeFormatter.ISO_DATE_TIME)
                    : null;
                endTime = StringUtils.hasText(end)
                    ? ZonedDateTime.parse(end, DateTimeFormatter.ISO_DATE_TIME)
                    : null;
            } catch (DateTimeParseException e) {
                startTime = StringUtils.hasText(start)
                    ? ZonedDateTime.parse(start, DateTimeFormatter.RFC_1123_DATE_TIME)
                    : null;
                endTime = StringUtils.hasText(end)
                    ? ZonedDateTime.parse(end, DateTimeFormatter.RFC_1123_DATE_TIME)
                    : null;
            }
            // Check if crontab is valid
            filters.forEach(filter -> filterCronTabExpressions.add(CronExpression.parse("* " + filter)));
            return true;
        }
    }

}
