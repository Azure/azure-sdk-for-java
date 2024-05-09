// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.filters.recurrence;

import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowFilterSettings;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceConstants;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceValidator;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.Recurrence;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrencePattern;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceRange;
import com.azure.spring.cloud.feature.management.models.FilterParameters;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RecurrenceValidatorTest {
    private final String pattern = "Recurrence.Pattern";
    private final String patternType = "Recurrence.Pattern.Type";
    private final String interval = "Recurrence.Pattern.Interval";
    private final String daysOfWeek = "Recurrence.Pattern.DaysOfWeek";
    private final String firstDayOfWeek = "Recurrence.Pattern.FirstDayOfWeek";
    private final String rangeType = "Recurrence.Range.Type";
    private final String numberOfOccurrences = "Recurrence.Range.NumberOfOccurrences";
    private final String endDate = "Recurrence.Range.EndDate";

    @Test
    public void generalRequiredParameterTest() {
        final ZonedDateTime startTime = ZonedDateTime.now();
        final ZonedDateTime endTime = startTime.plusHours(2);

        // no end parameter
        final TimeWindowFilterSettings settings1 = new TimeWindowFilterSettings();
        settings1.setStart(startTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        settings1.setRecurrence(new Recurrence());
        consumeValidationTestData(settings1, FilterParameters.TIME_WINDOW_FILTER_SETTING_END,
            RecurrenceConstants.REQUIRED_PARAMETER);

        // no start parameter
        final TimeWindowFilterSettings settings2 = new TimeWindowFilterSettings();
        settings2.setEnd(endTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        settings2.setRecurrence(new Recurrence());
        consumeValidationTestData(settings2, FilterParameters.TIME_WINDOW_FILTER_SETTING_START,
            RecurrenceConstants.REQUIRED_PARAMETER);

        // no pattern in recurrence parameter
        final TimeWindowFilterSettings settings3 = new TimeWindowFilterSettings();
        settings3.setStart(startTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        settings3.setEnd(endTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        final Recurrence recurrence3 = new Recurrence();
        recurrence3.setRange(new RecurrenceRange());
        settings3.setRecurrence(recurrence3);
        consumeValidationTestData(settings3, pattern, RecurrenceConstants.REQUIRED_PARAMETER);

        // no type in recurrence pattern parameter
        final TimeWindowFilterSettings settings5 = new TimeWindowFilterSettings();
        settings5.setStart(startTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        settings5.setEnd(endTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        final Recurrence recurrence5 = new Recurrence();
        recurrence5.setPattern(new RecurrencePattern());
        recurrence5.setRange(new RecurrenceRange());
        settings5.setRecurrence(recurrence5);
        consumeValidationTestData(settings5, patternType, RecurrenceConstants.UNRECOGNIZED_VALUE);
    }

    @Test
    public void invalidPatternTypeTest() {
        final RecurrencePattern pattern = new RecurrencePattern();
        pattern.setType("");

        final RecurrenceRange range = new RecurrenceRange();
        range.setType("NoEnd");

        final Recurrence recurrence = new Recurrence();
        recurrence.setPattern(pattern);
        recurrence.setRange(range);

        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T02:00:00+08:00");
        settings.setRecurrence(recurrence);

        consumeValidationTestData(settings, patternType, RecurrenceConstants.UNRECOGNIZED_VALUE);
    }

    @Test
    public void invalidPatternIntervalTest() {
        final RecurrencePattern pattern = new RecurrencePattern();
        pattern.setType("Daily");
        pattern.setInterval(0);

        final RecurrenceRange range = new RecurrenceRange();
        range.setType("NoEnd");

        final Recurrence recurrence = new Recurrence();
        recurrence.setPattern(pattern);
        recurrence.setRange(range);

        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T02:00:00+08:00");
        settings.setRecurrence(recurrence);

        consumeValidationTestData(settings, interval, RecurrenceConstants.OUT_OF_RANGE);
    }

    @Test
    public void invalidPatternFirstDayOfWeekTest() {
        final RecurrencePattern pattern = new RecurrencePattern();
        pattern.setType("Weekly");
        pattern.setFirstDayOfWeek("");
        pattern.setDaysOfWeek(List.of("Monday"));

        final RecurrenceRange range = new RecurrenceRange();
        range.setType("NoEnd");

        final Recurrence recurrence = new Recurrence();
        recurrence.setPattern(pattern);
        recurrence.setRange(range);

        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T02:00:00+08:00");
        settings.setRecurrence(recurrence);

        consumeValidationTestData(settings, firstDayOfWeek, RecurrenceConstants.UNRECOGNIZED_VALUE);
    }

    @Test
    public void invalidPatternDaysOfWeekTest() {
        final RecurrencePattern pattern = new RecurrencePattern();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("day"));

        final RecurrenceRange range = new RecurrenceRange();
        range.setType("NoEnd");

        final Recurrence recurrence = new Recurrence();
        recurrence.setPattern(pattern);
        recurrence.setRange(range);

        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T02:00:00+08:00");
        settings.setRecurrence(recurrence);

        consumeValidationTestData(settings, daysOfWeek, RecurrenceConstants.UNRECOGNIZED_VALUE);
    }

    @Test
    public void invalidRangeTypeTest() {
        final RecurrencePattern pattern = new RecurrencePattern();
        pattern.setType("Daily");

        final RecurrenceRange range = new RecurrenceRange();
        range.setType("");

        final Recurrence recurrence = new Recurrence();
        recurrence.setPattern(pattern);
        recurrence.setRange(range);

        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T02:00:00+08:00");
        settings.setRecurrence(recurrence);

        consumeValidationTestData(settings, rangeType, RecurrenceConstants.UNRECOGNIZED_VALUE);
    }

    @Test
    public void invalidRangeNumberOfOccurrencesTest() {
        final RecurrencePattern pattern = new RecurrencePattern();
        pattern.setType("Daily");

        final RecurrenceRange range = new RecurrenceRange();
        range.setType("Numbered");
        range.setNumberOfRecurrences(0);

        final Recurrence recurrence = new Recurrence();
        recurrence.setPattern(pattern);
        recurrence.setRange(range);

        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T02:00:00+08:00");
        settings.setRecurrence(recurrence);

        consumeValidationTestData(settings, numberOfOccurrences, RecurrenceConstants.OUT_OF_RANGE);
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
        pattern.setType("Daily");
        range.setType("NoEnd");
        settings.setStart("2023-09-25T00:00:00+08:00");
        settings.setEnd("2023-09-25T00:00:00+08:00");
        consumeValidationTestData(settings, FilterParameters.TIME_WINDOW_FILTER_SETTING_END, RecurrenceConstants.OUT_OF_RANGE);

        // time window is bigger than interval when pattern is daily
        settings.setEnd("2023-09-27T00:00:01+08:00");
        pattern.setInterval(2);
        consumeValidationTestData(settings, FilterParameters.TIME_WINDOW_FILTER_SETTING_END, RecurrenceConstants.OUT_OF_RANGE);

        // time window is bigger than interval when pattern is weekly
        settings.setEnd("2023-10-02T00:00:01+08:00");
        pattern.setType("Weekly");
        pattern.setInterval(1);
        pattern.setDaysOfWeek(List.of("Monday"));
        consumeValidationTestData(settings, FilterParameters.TIME_WINDOW_FILTER_SETTING_END, RecurrenceConstants.OUT_OF_RANGE);

        // time window is bigger than interval when pattern is weekly
        settings.setEnd("2023-09-27T00:00:01+08:00");
        pattern.setInterval(1);
        pattern.setDaysOfWeek(List.of("Monday", "Thursday", "Sunday"));
        consumeValidationTestData(settings, FilterParameters.TIME_WINDOW_FILTER_SETTING_END, RecurrenceConstants.OUT_OF_RANGE);

        // endDate is before first start time
        final ZonedDateTime startTime6 = ZonedDateTime.parse("2023-09-01T00:00:00+08:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        final ZonedDateTime endTime6 = ZonedDateTime.parse("2023-09-01T00:00:01+08:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        settings.setStart(startTime6.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        settings.setEnd(endTime6.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        pattern.setType("Daily");
        range.setType("EndDate");
        range.setEndDate("2023-08-31T00:00:00+08:00");
        consumeValidationTestData(settings, endDate, RecurrenceConstants.OUT_OF_RANGE);

    }

    @Test
    public void weeklyPatternRequiredParameterTest() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T02:00:00+08:00");
        final RecurrenceRange range = new RecurrenceRange();
        range.setType("NoEnd");
        final Recurrence recurrence = new Recurrence();
        recurrence.setRange(range);
        settings.setRecurrence(recurrence);

        // daysOfWeek parameter is required
        final RecurrencePattern pattern = new RecurrencePattern();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(null);
        recurrence.setPattern(pattern);
        consumeValidationTestData(settings, daysOfWeek, RecurrenceConstants.REQUIRED_PARAMETER);
    }

    @Test
    public void startParameterNotMatchTest() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T02:00:00+08:00");
        final RecurrenceRange range = new RecurrenceRange();
        range.setType("NoEnd");
        final Recurrence recurrence = new Recurrence();
        recurrence.setRange(range);
        settings.setRecurrence(recurrence);

        // start time need to match the first recurrence
        final RecurrencePattern pattern1 = new RecurrencePattern();
        pattern1.setType("Weekly");
        pattern1.setDaysOfWeek(List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Saturday", "Sunday"));
        recurrence.setPattern(pattern1);
        consumeValidationTestData(settings, FilterParameters.TIME_WINDOW_FILTER_SETTING_START, RecurrenceConstants.NOT_MATCHED);
    }

    private void consumeValidationTestData(TimeWindowFilterSettings settings, String parameterName, String errorMessage) {

        try {
            final RecurrenceValidator validator = new RecurrenceValidator(settings);
            validator.validateSettings();
        } catch (final Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertEquals(e.getMessage(), String.format(errorMessage, parameterName));
        }
    }
}
