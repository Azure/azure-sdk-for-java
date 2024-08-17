// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.filters;

import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_END;
import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_START;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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

    @Test
    public void weeklyTest() {
        final TimeWindowFilter filter = new TimeWindowFilter();
        final FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();
        final ZonedDateTime now = ZonedDateTime.now();

        final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("Start", now.minusDays(7).format(DateTimeFormatter.RFC_1123_DATE_TIME));
        parameters.put("End", now.minusDays(7).plusHours(2).format(DateTimeFormatter.RFC_1123_DATE_TIME));
        final HashMap<String, Object> pattern = new HashMap<>();
        pattern.put("Type", "Weekly");
        pattern.put("DaysOfWeek", List.of(now.minusDays(7).getDayOfWeek().name()));
        final HashMap<String, Object> range = new HashMap<>();
        range.put("Type", "NoEnd");
        final HashMap<String, Object> recurrence = new HashMap<>();
        recurrence.put("Pattern", pattern);
        recurrence.put("Range", range);
        parameters.put("Recurrence", recurrence);

        context.setParameters(parameters);
        assertTrue(filter.evaluate(context));
    }

    @Test
    public void weeklyDaysOfWeekMapTest() {
        final TimeWindowFilter filter = new TimeWindowFilter();
        final FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();
        final ZonedDateTime now = ZonedDateTime.now();

        final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("Start", now.minusDays(7).format(DateTimeFormatter.RFC_1123_DATE_TIME));
        parameters.put("End", now.minusDays(7).plusHours(2).format(DateTimeFormatter.RFC_1123_DATE_TIME));
        final HashMap<String, Object> daysOfWeekMap = new HashMap<>();
        daysOfWeekMap.put("0", now.minusDays(7).getDayOfWeek().name());
        final HashMap<String, Object> pattern = new HashMap<>();
        pattern.put("Type", "Weekly");
        pattern.put("DaysOfWeek", daysOfWeekMap);
        final HashMap<String, Object> range = new HashMap<>();
        range.put("Type", "NoEnd");
        final HashMap<String, Object> recurrence = new HashMap<>();
        recurrence.put("Pattern", pattern);
        recurrence.put("Range", range);
        parameters.put("Recurrence", recurrence);

        context.setParameters(parameters);
        assertTrue(filter.evaluate(context));
    }

    @Test
    public void weeklyLowerCamelCaseTest() {
        final TimeWindowFilter filter = new TimeWindowFilter();
        final FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();
        final ZonedDateTime now = ZonedDateTime.now();

        final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("start", now.minusDays(7).format(DateTimeFormatter.RFC_1123_DATE_TIME));
        parameters.put("end", now.minusDays(7).plusHours(2).format(DateTimeFormatter.RFC_1123_DATE_TIME));
        final HashMap<String, Object> daysOfWeekMap = new HashMap<>();
        daysOfWeekMap.put("0", now.minusDays(7).getDayOfWeek().name());
        final HashMap<String, Object> pattern = new HashMap<>();
        pattern.put("type", "Weekly");
        pattern.put("daysOfWeek", daysOfWeekMap);
        final HashMap<String, Object> range = new HashMap<>();
        range.put("type", "NoEnd");
        final HashMap<String, Object> recurrence = new HashMap<>();
        recurrence.put("pattern", pattern);
        recurrence.put("range", range);
        parameters.put("recurrence", recurrence);

        context.setParameters(parameters);
        assertTrue(filter.evaluate(context));
    }
}
