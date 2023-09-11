// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.filters;

import com.azure.spring.cloud.feature.management.models.FeatureFilterEvaluationContext;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_END;
import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_FILTERS;
import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_START;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TimeWindowFilterTest {

    @Test
    public void middleTest() {
        TimeWindowFilter filter = new TimeWindowFilter();
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();
        Map<String, Object> parameters = new LinkedHashMap<>();
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
        Map<String, Object> parameters = new LinkedHashMap<>();
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
        Map<String, Object> parameters = new LinkedHashMap<>();
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
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put(TIME_WINDOW_FILTER_SETTING_END,
            ZonedDateTime.now().plusDays(1).format(DateTimeFormatter.RFC_1123_DATE_TIME));
        context.setParameters(parameters);
        assertTrue(filter.evaluate(context));
    }

    @Test
    public void noEndTest() {
        TimeWindowFilter filter = new TimeWindowFilter();
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put(TIME_WINDOW_FILTER_SETTING_START,
            ZonedDateTime.now().minusDays(1).format(DateTimeFormatter.RFC_1123_DATE_TIME));
        context.setParameters(parameters);
        assertTrue(filter.evaluate(context));
    }

    @Test
    public void noInputsTest() {
        TimeWindowFilter filter = new TimeWindowFilter();
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();
        Map<String, Object> parameters = new LinkedHashMap<>();
        context.setParameters(parameters);
        assertFalse(filter.evaluate(context));
    }

    @Test
    public void weeklyFiltersTest() {
        TimeWindowFilter filter = new TimeWindowFilter();
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();
        Map<String, Object> parameters = new LinkedHashMap<>();
        List<String> filters = new ArrayList<>();
        filters.add("* * * * 4");   // Enabled on Thursday every week
        parameters.put(TIME_WINDOW_FILTER_SETTING_FILTERS, filters);
        context.setParameters(parameters);
        ZonedDateTime now = ZonedDateTime.now();
        assertEquals(filter.evaluate(context), now.getDayOfWeek().getValue() == 4);
    }

    @Test
    public void monthlyFiltersTest() {
        TimeWindowFilter filter = new TimeWindowFilter();
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();
        Map<String, Object> parameters = new LinkedHashMap<>();
        List<String> filters = new ArrayList<>();
        filters.add("* * * 3-4 *");   // Enabled on March April every year
        parameters.put(TIME_WINDOW_FILTER_SETTING_FILTERS, filters);
        context.setParameters(parameters);
        ZonedDateTime now = ZonedDateTime.now();
        assertEquals(filter.evaluate(context), now.getMonthValue() == 3 || now.getMonthValue() == 4);
    }

    @Test
    public void dailyFiltersTest() {
        TimeWindowFilter filter = new TimeWindowFilter();
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();
        Map<String, Object> parameters = new LinkedHashMap<>();
        List<String> filters = new ArrayList<>();
        filters.add("* * 15 1 *");   // Enabled on 15th Jan every year
        parameters.put(TIME_WINDOW_FILTER_SETTING_FILTERS, filters);
        context.setParameters(parameters);
        ZonedDateTime now = ZonedDateTime.now();
        assertEquals(filter.evaluate(context), now.getDayOfMonth() == 15 && now.getMonthValue() == 1);
    }

    @Test
    public void hourFiltersTest() {
        TimeWindowFilter filter = new TimeWindowFilter();
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();
        Map<String, Object> parameters = new LinkedHashMap<>();
        List<String> filters = new ArrayList<>();
        filters.add("* 18-19 * * *");   // Enabled on 18:00-20:00 every day
        parameters.put(TIME_WINDOW_FILTER_SETTING_FILTERS, filters);
        context.setParameters(parameters);
        ZonedDateTime now = ZonedDateTime.now();
        assertEquals(filter.evaluate(context), now.getHour() == 18 || now.getHour() == 19);
    }

    @Test
    public void minuteFiltersTest() {
        TimeWindowFilter filter = new TimeWindowFilter();
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();
        Map<String, Object> parameters = new LinkedHashMap<>();
        List<String> filters = new ArrayList<>();
        filters.add("55-59 * * * *");   // Enabled on every last five minutes
        parameters.put(TIME_WINDOW_FILTER_SETTING_FILTERS, filters);
        context.setParameters(parameters);
        ZonedDateTime now = ZonedDateTime.now();
        assertEquals(filter.evaluate(context), now.getSecond() >= 55);
    }


}
