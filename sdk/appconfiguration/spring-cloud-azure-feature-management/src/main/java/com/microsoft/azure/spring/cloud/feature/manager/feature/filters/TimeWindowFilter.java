// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.feature.manager.feature.filters;

import static com.microsoft.azure.spring.cloud.feature.manager.FilterParameters.TIME_WINDOW_FILTER_SETTING_END;
import static com.microsoft.azure.spring.cloud.feature.manager.FilterParameters.TIME_WINDOW_FILTER_SETTING_START;

import com.microsoft.azure.spring.cloud.feature.manager.FeatureFilter;
import com.microsoft.azure.spring.cloud.feature.manager.entities.FeatureFilterEvaluationContext;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * A feature filter that can be used at activate a feature based on a time window.
 */
@Component("TimeWindowFilter")
public class TimeWindowFilter implements FeatureFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeWindowFilter.class);

    /**
     * Evaluates whether a feature is enabled based on a configurable time window.
     *
     * @param context The feature evaluation context.
     * @return True if the feature is enabled, false otherwise.
     */
    @Override
    public boolean evaluate(FeatureFilterEvaluationContext context) {
        String start = (String) context.getParameters().get(TIME_WINDOW_FILTER_SETTING_START);
        String end = (String) context.getParameters().get(TIME_WINDOW_FILTER_SETTING_END);

        ZonedDateTime now = ZonedDateTime.now();

        if (!StringUtils.hasText(start) && !StringUtils.hasText(end)) {
            LOGGER.warn("The {} feature filter is not valid for feature {}. It must specify either {}, {}, or both.",
                this.getClass().getSimpleName(), context.getName(), TIME_WINDOW_FILTER_SETTING_START,
                TIME_WINDOW_FILTER_SETTING_END);
            return false;
        }

        ZonedDateTime startTime = StringUtils.hasText(start)
            ? ZonedDateTime.parse(start, DateTimeFormatter.RFC_1123_DATE_TIME)
            : null;
        ZonedDateTime endTime = StringUtils.hasText(end)
            ? ZonedDateTime.parse(end, DateTimeFormatter.RFC_1123_DATE_TIME)
            : null;

        return (!StringUtils.hasText(start) || now.isAfter(startTime))
            && (!StringUtils.hasText(end) || now.isBefore(endTime));
    }

}
