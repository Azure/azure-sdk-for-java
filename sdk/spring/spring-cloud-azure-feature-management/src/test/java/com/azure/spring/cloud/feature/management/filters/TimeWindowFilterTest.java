// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.filters;

import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_END;
import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_START;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.azure.spring.cloud.feature.management.models.FeatureFilterEvaluationContext;

public class TimeWindowFilterTest {

    @Test
    public void middleTest() {
        TimeWindowFilter filter = new TimeWindowFilter();
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();
        Map<String, Object> parameters = new LinkedHashMap<String, Object>();
        parameters.put(TIME_WINDOW_FILTER_SETTING_START,
            ZonedDateTime.now().minusDays(1).format(DateTimeFormatter.RFC_1123_DATE_TIME));
        parameters.put(TIME_WINDOW_FILTER_SETTING_END,
            ZonedDateTime.now().plusDays(1).format(DateTimeFormatter.RFC_1123_DATE_TIME));
        context.setParameters(parameters);
        assertTrue(filter.evaluate(context));
    }

    @Test
    public void beforeTest() {
        TimeWindowFilter filter = new TimeWindowFilter();
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();
        Map<String, Object> parameters = new LinkedHashMap<String, Object>();
        parameters.put(TIME_WINDOW_FILTER_SETTING_START,
            ZonedDateTime.now().plusDays(1).format(DateTimeFormatter.RFC_1123_DATE_TIME));
        parameters.put(TIME_WINDOW_FILTER_SETTING_END,
            ZonedDateTime.now().plusDays(2).format(DateTimeFormatter.RFC_1123_DATE_TIME));
        context.setParameters(parameters);
        assertFalse(filter.evaluate(context));
    }

    @Test
    public void afterTest() {
        TimeWindowFilter filter = new TimeWindowFilter();
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();
        Map<String, Object> parameters = new LinkedHashMap<String, Object>();
        parameters.put(TIME_WINDOW_FILTER_SETTING_START,
            ZonedDateTime.now().minusDays(1).format(DateTimeFormatter.RFC_1123_DATE_TIME));
        parameters.put(TIME_WINDOW_FILTER_SETTING_END,
            ZonedDateTime.now().minusDays(2).format(DateTimeFormatter.RFC_1123_DATE_TIME));
        context.setParameters(parameters);
        assertFalse(filter.evaluate(context));
    }

    @Test
    public void noStartTest() {
        TimeWindowFilter filter = new TimeWindowFilter();
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();
        Map<String, Object> parameters = new LinkedHashMap<String, Object>();
        parameters.put(TIME_WINDOW_FILTER_SETTING_END,
            ZonedDateTime.now().plusDays(1).format(DateTimeFormatter.RFC_1123_DATE_TIME));
        context.setParameters(parameters);
        assertTrue(filter.evaluate(context));
    }

    @Test
    public void noEndTest() {
        TimeWindowFilter filter = new TimeWindowFilter();
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();
        Map<String, Object> parameters = new LinkedHashMap<String, Object>();
        parameters.put(TIME_WINDOW_FILTER_SETTING_START,
            ZonedDateTime.now().minusDays(1).format(DateTimeFormatter.RFC_1123_DATE_TIME));
        context.setParameters(parameters);
        assertTrue(filter.evaluate(context));
    }

    @Test
    public void noInputsTest() {
        TimeWindowFilter filter = new TimeWindowFilter();
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();
        Map<String, Object> parameters = new LinkedHashMap<String, Object>();
        context.setParameters(parameters);
        assertFalse(filter.evaluate(context));
    }

}
