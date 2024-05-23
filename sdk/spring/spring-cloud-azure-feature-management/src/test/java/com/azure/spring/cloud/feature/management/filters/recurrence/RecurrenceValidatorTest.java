// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.filters.recurrence;

import com.azure.spring.cloud.feature.management.filters.TimeWindowFilter;
import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowFilterSettings;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceConstants;
import com.azure.spring.cloud.feature.management.implementation.models.Recurrence;
import com.azure.spring.cloud.feature.management.implementation.models.RecurrencePattern;
import com.azure.spring.cloud.feature.management.implementation.models.RecurrenceRange;
import com.azure.spring.cloud.feature.management.models.FeatureFilterEvaluationContext;
import com.azure.spring.cloud.feature.management.models.FilterParameters;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RecurrenceValidatorTest {
    private final String pattern = "Recurrence.Pattern";
    private final String range = "Recurrence.Range";
    private final String interval = "Recurrence.Pattern.Interval";
    private final String daysOfWeek = "Recurrence.Pattern.DaysOfWeek";
    private final String numberOfOccurrences = "Recurrence.Range.NumberOfOccurrences";
    private final String endDate = "Recurrence.Range.EndDate";

    @Test
    public void generalRequiredParameterTest() {
        final ZonedDateTime startTime = ZonedDateTime.now();
        final ZonedDateTime endTime = startTime.plusHours(2);

        // no pattern in recurrence parameter
        final HashMap<String, Object> range1 = new HashMap<>();
        range1.put("Type", "NoEnd");
        final HashMap<String, Object> recurrence1 = new HashMap<>();
        recurrence1.put("Range", range1);
        final Map<String, Object> parameters1 = new LinkedHashMap<String, Object>();
        parameters1.put("Start", startTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        parameters1.put("End", endTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        parameters1.put("Recurrence", recurrence1);
        consumeValidationTestData(parameters1, String.format(RecurrenceConstants.REQUIRED_PARAMETER, pattern));

        // no range in recurrence parameter
        final HashMap<String, Object> pattern2 = new HashMap<>();
        pattern2.put("Type", "Daily");
        final HashMap<String, Object> recurrence2 = new HashMap<>();
        recurrence2.put("Pattern", pattern2);
        final Map<String, Object> parameters2 = new LinkedHashMap<String, Object>();
        parameters2.put("Start", startTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        parameters2.put("End", endTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        parameters2.put("Recurrence", recurrence2);
        consumeValidationTestData(parameters2, String.format(RecurrenceConstants.REQUIRED_PARAMETER, range));
    }

    @Test
    public void invalidPatternTypeTest() {
        final HashMap<String, Object> pattern1 = new HashMap<>();
        pattern1.put("Type", "");

        final HashMap<String, Object> range1 = new HashMap<>();
        range1.put("Type", "NoEnd");

        final HashMap<String, Object> recurrence1 = new HashMap<>();
        recurrence1.put("Pattern", pattern1);
        recurrence1.put("Range", range1);

        final Map<String, Object> parameters1 = new LinkedHashMap<String, Object>();
        parameters1.put("Start", "2023-09-01T00:00:00+08:00");
        parameters1.put("End", "2023-09-01T02:00:00+08:00");
        parameters1.put("Recurrence", recurrence1);

        consumeValidationTestData(parameters1, "No enum constant");
    }

    @Test
    public void invalidPatternIntervalTest() {
        final HashMap<String, Object> pattern1 = new HashMap<>();
        pattern1.put("Type", "Daily");
        pattern1.put("Interval", 0);

        final HashMap<String, Object> range1 = new HashMap<>();
        range1.put("Type", "NoEnd");

        final HashMap<String, Object> recurrence1 = new HashMap<>();
        recurrence1.put("Pattern", pattern1);
        recurrence1.put("Range", range1);

        final Map<String, Object> parameters1 = new LinkedHashMap<String, Object>();
        parameters1.put("Start", "2023-09-01T00:00:00+08:00");
        parameters1.put("End", "2023-09-01T02:00:00+08:00");
        parameters1.put("Recurrence", recurrence1);

        consumeValidationTestData(parameters1, String.format(RecurrenceConstants.OUT_OF_RANGE, interval));
    }

    @Test
    public void invalidPatternFirstDayOfWeekTest() {
        final HashMap<String, Object> pattern1 = new HashMap<>();
        pattern1.put("Type", "Weekly");
        pattern1.put("FirstDayOfWeek", "");
        pattern1.put("DaysOfWeek", List.of("Monday"));

        final HashMap<String, Object> range1 = new HashMap<>();
        range1.put("Type", "NoEnd");

        final HashMap<String, Object> recurrence1 = new HashMap<>();
        recurrence1.put("Pattern", pattern1);
        recurrence1.put("Range", range1);

        final Map<String, Object> parameters1 = new LinkedHashMap<String, Object>();
        parameters1.put("Start", "2023-09-01T00:00:00+08:00");
        parameters1.put("End", "2023-09-01T02:00:00+08:00");
        parameters1.put("Recurrence", recurrence1);

        consumeValidationTestData(parameters1, "No enum constant");
    }

    @Test
    public void invalidPatternDaysOfWeekTest() {
        final HashMap<String, Object> pattern1 = new HashMap<>();
        pattern1.put("Type", "Weekly");
        pattern1.put("DaysOfWeek", List.of("day"));

        final HashMap<String, Object> range1 = new HashMap<>();
        range1.put("Type", "NoEnd");

        final HashMap<String, Object> recurrence1 = new HashMap<>();
        recurrence1.put("Pattern", pattern1);
        recurrence1.put("Range", range1);

        final Map<String, Object> parameters1 = new LinkedHashMap<String, Object>();
        parameters1.put("Start", "2023-09-01T00:00:00+08:00");
        parameters1.put("End", "2023-09-01T02:00:00+08:00");
        parameters1.put("Recurrence", recurrence1);

        consumeValidationTestData(parameters1, "No enum constant");
    }

    @Test
    public void invalidRangeTypeTest() {
        final HashMap<String, Object> pattern1 = new HashMap<>();
        pattern1.put("Type", "Daily");

        final HashMap<String, Object> range1 = new HashMap<>();
        range1.put("Type", "");

        final HashMap<String, Object> recurrence1 = new HashMap<>();
        recurrence1.put("Pattern", pattern1);
        recurrence1.put("Range", range1);

        final Map<String, Object> parameters1 = new LinkedHashMap<String, Object>();
        parameters1.put("Start", "2023-09-01T00:00:00+08:00");
        parameters1.put("End", "2023-09-01T02:00:00+08:00");
        parameters1.put("Recurrence", recurrence1);

        consumeValidationTestData(parameters1, "No enum constant");
    }

    @Test
    public void invalidRangeNumberOfOccurrencesTest() {
        final HashMap<String, Object> pattern1 = new HashMap<>();
        pattern1.put("Type", "Daily");

        final HashMap<String, Object> range1 = new HashMap<>();
        range1.put("Type", "Numbered");
        range1.put("NumberOfRecurrences", 0);

        final HashMap<String, Object> recurrence1 = new HashMap<>();
        recurrence1.put("Pattern", pattern1);
        recurrence1.put("Range", range1);

        final Map<String, Object> parameters1 = new LinkedHashMap<String, Object>();
        parameters1.put("Start", "2023-09-01T00:00:00+08:00");
        parameters1.put("End", "2023-09-01T02:00:00+08:00");
        parameters1.put("Recurrence", recurrence1);

        consumeValidationTestData(parameters1, String.format(RecurrenceConstants.OUT_OF_RANGE, numberOfOccurrences));
    }

    @Test
    public void invalidTimeWindowTest() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final Recurrence recurrence = new Recurrence();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        recurrence.setPattern(pattern);
        recurrence.setRange(range);
        settings.setRecurrence(recurrence);

        // time window is zero
        final HashMap<String, Object> pattern1 = new HashMap<>();
        pattern1.put("Type", "Daily");
        final HashMap<String, Object> range1 = new HashMap<>();
        range1.put("Type", "NoEnd");
        final HashMap<String, Object> recurrence1 = new HashMap<>();
        recurrence1.put("Pattern", pattern1);
        recurrence1.put("Range", range1);
        final Map<String, Object> parameters1 = new LinkedHashMap<String, Object>();
        parameters1.put("Start", "2023-09-25T00:00:00+08:00");
        parameters1.put("End", "2023-09-25T00:00:00+08:00");
        parameters1.put("Recurrence", recurrence1);
        consumeValidationTestData(parameters1, String.format(RecurrenceConstants.OUT_OF_RANGE, FilterParameters.TIME_WINDOW_FILTER_SETTING_END));

        // time window is bigger than interval when pattern is daily
        final HashMap<String, Object> pattern2 = new HashMap<>();
        pattern2.put("Type", "Daily");
        pattern2.put("Interval", 2);
        final HashMap<String, Object> range2 = new HashMap<>();
        range2.put("Type", "NoEnd");
        final HashMap<String, Object> recurrence2 = new HashMap<>();
        recurrence2.put("Pattern", pattern2);
        recurrence2.put("Range", range2);
        final Map<String, Object> parameters2 = new LinkedHashMap<String, Object>();
        parameters2.put("Start", "2023-09-25T00:00:00+08:00");
        parameters2.put("End", "2023-09-27T00:00:01+08:00");
        parameters2.put("Recurrence", recurrence2);
        consumeValidationTestData(parameters2, String.format(RecurrenceConstants.OUT_OF_RANGE, FilterParameters.TIME_WINDOW_FILTER_SETTING_END));

        // time window is bigger than interval when pattern is weekly
        final HashMap<String, Object> pattern3 = new HashMap<>();
        pattern3.put("Type", "WeekLy");
        pattern3.put("Interval", 1);
        pattern3.put("DaysOfWeek", List.of("Monday"));
        final HashMap<String, Object> range3 = new HashMap<>();
        range3.put("Type", "NoEnd");
        final HashMap<String, Object> recurrence3= new HashMap<>();
        recurrence3.put("Pattern", pattern3);
        recurrence3.put("Range", range3);
        final Map<String, Object> parameters3 = new LinkedHashMap<String, Object>();
        parameters3.put("Start", "2023-09-25T00:00:00+08:00");
        parameters3.put("End", "2023-10-02T00:00:01+08:00");
        parameters3.put("Recurrence", recurrence3);
        consumeValidationTestData(parameters3, String.format(RecurrenceConstants.OUT_OF_RANGE, FilterParameters.TIME_WINDOW_FILTER_SETTING_END));

        // time window is bigger than interval when pattern is weekly
        final HashMap<String, Object> pattern4 = new HashMap<>();
        pattern4.put("Type", "WeekLy");
        pattern4.put("Interval", 1);
        pattern4.put("DaysOfWeek", List.of("Monday", "Thursday", "Sunday"));
        final HashMap<String, Object> range4 = new HashMap<>();
        range4.put("Type", "NoEnd");
        final HashMap<String, Object> recurrence4= new HashMap<>();
        recurrence4.put("Pattern", pattern4);
        recurrence4.put("Range", range4);
        final Map<String, Object> parameters4 = new LinkedHashMap<String, Object>();
        parameters4.put("Start", "2023-09-25T00:00:00+08:00");
        parameters4.put("End", "2023-09-27T00:00:01+08:00");
        parameters4.put("Recurrence", recurrence4);
        consumeValidationTestData(parameters4, String.format(RecurrenceConstants.OUT_OF_RANGE, FilterParameters.TIME_WINDOW_FILTER_SETTING_END));

        // endDate is before first start time
        final HashMap<String, Object> pattern5 = new HashMap<>();
        pattern5.put("Type", "Daily");
        final HashMap<String, Object> range5 = new HashMap<>();
        range5.put("Type", "EndDate");
        range5.put("EndDate", "2023-08-31T00:00:00+08:00");
        final HashMap<String, Object> recurrence5= new HashMap<>();
        recurrence5.put("Pattern", pattern5);
        recurrence5.put("Range", range5);
        final Map<String, Object> parameters5 = new LinkedHashMap<String, Object>();
        parameters5.put("Start", "2023-09-01T00:00:00+08:00");
        parameters5.put("End", "2023-09-01T00:00:01+08:00");
        parameters5.put("Recurrence", recurrence5);
        consumeValidationTestData(parameters5, String.format(RecurrenceConstants.OUT_OF_RANGE, endDate));
    }

    @Test
    public void weeklyPatternRequiredParameterTest() {
        final HashMap<String, Object> pattern1 = new HashMap<>();
        pattern1.put("Type", "Weekly");

        final HashMap<String, Object> range1 = new HashMap<>();
        range1.put("Type", "NoEnd");

        final HashMap<String, Object> recurrence1 = new HashMap<>();
        recurrence1.put("Pattern", pattern1);
        recurrence1.put("Range", range1);

        final Map<String, Object> parameters1 = new LinkedHashMap<String, Object>();
        parameters1.put("Start", "2023-09-01T00:00:00+08:00");
        parameters1.put("End", "2023-09-01T02:00:00+08:00");
        parameters1.put("Recurrence", recurrence1);

        // daysOfWeek parameter is required
        consumeValidationTestData(parameters1, String.format(RecurrenceConstants.REQUIRED_PARAMETER, daysOfWeek));
    }

    @Test
    public void startParameterNotMatchTest() {
        final HashMap<String, Object> pattern1 = new HashMap<>();
        pattern1.put("Type", "Weekly");
        pattern1.put("DaysOfWeek", List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Saturday", "Sunday"));

        final HashMap<String, Object> range1 = new HashMap<>();
        range1.put("Type", "NoEnd");

        final HashMap<String, Object> recurrence1 = new HashMap<>();
        recurrence1.put("Pattern", pattern1);
        recurrence1.put("Range", range1);

        final Map<String, Object> parameters1 = new LinkedHashMap<String, Object>();
        parameters1.put("Start", "2023-09-01T00:00:00+08:00");
        parameters1.put("End", "2023-09-01T02:00:00+08:00");
        parameters1.put("Recurrence", recurrence1);

        consumeValidationTestData(parameters1, String.format(RecurrenceConstants.NOT_MATCHED, FilterParameters.TIME_WINDOW_FILTER_SETTING_START));
    }


    private void consumeValidationTestData(Map<String, Object> parameters, String errorMessage) {
        try {
            final TimeWindowFilter filter = new TimeWindowFilter();
            final FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();
            context.setParameters(parameters);
            filter.evaluate(context);
        } catch (final Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertTrue(e.getMessage().contains(errorMessage));
        }
    }
}
