/*
 * // Copyright (c) Microsoft Corporation. All rights reserved.
 * // Licensed under the MIT License.
 */

package com.azure.spring.cloud.feature.management.filters.recurrence;

import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowFilterSettings;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.Recurrence;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceConstants;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceEvaluator;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrencePattern;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceRange;
import com.azure.spring.cloud.feature.management.models.FilterParameters;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RequiredParameterTest {
    private final String Pattern = "Recurrence.Pattern";
    private final String PatternType = "Recurrence.Pattern.Type";
    private final String Interval = "Recurrence.Pattern.Interval";
    private final String Index = "Recurrence.Pattern.Index";
    private final String DaysOfWeek = "Recurrence.Pattern.DaysOfWeek";
    private final String FirstDayOfWeek = "Recurrence.Pattern.FirstDayOfWeek";
    private final String Month = "Recurrence.Pattern.Month";
    private final String DayOfMonth = "Recurrence.Pattern.DayOfMonth";
    private final String Range = "Recurrence.Range";
    private final String RangeType = "Recurrence.Range.Type";
    private final String NumberOfOccurrences = "Recurrence.Range.NumberOfOccurrences";
    private final String RecurrenceTimeZone = "Recurrence.Range.RecurrenceTimeZone";
    private final String EndDate = "Recurrence.Range.EndDate";

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
        consumeValidationTestData(settings3, Pattern, RecurrenceConstants.REQUIRED_PARAMETER);

        // no range in recurrence parameter
        final TimeWindowFilterSettings settings4 = new TimeWindowFilterSettings();
        settings4.setStart(startTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        settings4.setEnd(endTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        final Recurrence recurrence4 = new Recurrence();
        recurrence4.setPattern(new RecurrencePattern());
        settings4.setRecurrence(recurrence4);
        consumeValidationTestData(settings4, Range, RecurrenceConstants.REQUIRED_PARAMETER);

        // no type in recurrence pattern parameter
        final TimeWindowFilterSettings settings5 = new TimeWindowFilterSettings();
        settings5.setStart(startTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        settings5.setEnd(endTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        final Recurrence recurrence5 = new Recurrence();
        recurrence5.setPattern(new RecurrencePattern());
        recurrence5.setRange(new RecurrenceRange());
        settings5.setRecurrence(recurrence5);
        consumeValidationTestData(settings5, PatternType, RecurrenceConstants.REQUIRED_PARAMETER);

        // no type in recurrence range parameter
        final TimeWindowFilterSettings settings6 = new TimeWindowFilterSettings();
        settings6.setStart(startTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        settings6.setEnd(endTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        final Recurrence recurrence6 = new Recurrence();
        final RecurrencePattern pattern6 = new RecurrencePattern();
        pattern6.setType(RecurrenceConstants.DAILY);
        recurrence6.setPattern(pattern6);
        recurrence6.setRange(new RecurrenceRange());
        settings6.setRecurrence(recurrence6);
        consumeValidationTestData(settings6, RangeType, RecurrenceConstants.REQUIRED_PARAMETER);
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

        consumeValidationTestData(settings, PatternType, RecurrenceConstants.UNRECOGNIZED_VALUE);
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

        consumeValidationTestData(settings, Interval, RecurrenceConstants.OUT_OF_RANGE);
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

        consumeValidationTestData(settings, FirstDayOfWeek, RecurrenceConstants.UNRECOGNIZED_VALUE);
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

        consumeValidationTestData(settings, DaysOfWeek, RecurrenceConstants.UNRECOGNIZED_VALUE);
    }

    @Test
    public void invalidPatternIndexTest() {
        final RecurrencePattern pattern = new RecurrencePattern();
        pattern.setType("RelativeMonthly");
        pattern.setIndex("");
        pattern.setDaysOfWeek(List.of("Friday"));

        final RecurrenceRange range = new RecurrenceRange();
        range.setType("NoEnd");

        final Recurrence recurrence = new Recurrence();
        recurrence.setPattern(pattern);
        recurrence.setRange(range);

        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T02:00:00+08:00");
        settings.setRecurrence(recurrence);

        consumeValidationTestData(settings, Index, RecurrenceConstants.UNRECOGNIZED_VALUE);
    }

    @Test
    public void invalidPatternDayOfMonthTest() {
        final RecurrencePattern pattern = new RecurrencePattern();
        pattern.setType("AbsoluteMonthly");
        pattern.setMonth(9);
        pattern.setDayOfMonth(0);

        final RecurrenceRange range = new RecurrenceRange();
        range.setType("NoEnd");

        final Recurrence recurrence = new Recurrence();
        recurrence.setPattern(pattern);
        recurrence.setRange(range);

        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T02:00:00+08:00");
        settings.setRecurrence(recurrence);

        consumeValidationTestData(settings, DayOfMonth, RecurrenceConstants.OUT_OF_RANGE);
    }

    @Test
    public void invalidPatternMonthTest() {
        final RecurrencePattern pattern = new RecurrencePattern();
        pattern.setType("AbsoluteMonthly");
        pattern.setDayOfMonth(1);
        pattern.setMonth(0);

        final RecurrenceRange range = new RecurrenceRange();
        range.setType("NoEnd");

        final Recurrence recurrence = new Recurrence();
        recurrence.setPattern(pattern);
        recurrence.setRange(range);

        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T02:00:00+08:00");
        settings.setRecurrence(recurrence);

        consumeValidationTestData(settings, Month, RecurrenceConstants.OUT_OF_RANGE);
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

        consumeValidationTestData(settings, RangeType, RecurrenceConstants.UNRECOGNIZED_VALUE);
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

        consumeValidationTestData(settings, NumberOfOccurrences, RecurrenceConstants.OUT_OF_RANGE);
    }

    @Test
    public void invalidRangeRecurrenceTimeZoneTest() {
        final RecurrencePattern pattern = new RecurrencePattern();
        pattern.setType("Daily");

        final RecurrenceRange range = new RecurrenceRange();
        range.setType("NoEnd");
        range.setRecurrenceTimeZone("");

        final Recurrence recurrence = new Recurrence();
        recurrence.setPattern(pattern);
        recurrence.setRange(range);

        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T02:00:00+08:00");
        settings.setRecurrence(recurrence);

        consumeValidationTestData(settings, RecurrenceTimeZone, RecurrenceConstants.UNRECOGNIZED_VALUE);
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

        // time window is bigger than interval when pattern is absoluteMonthly
        settings.setStart("2023-02-01T00:00:00+08:00");
        settings.setEnd("2023-03-31T00:00:01+08:00");
        pattern.setType("AbsoluteMonthly");
        pattern.setInterval(2);
        pattern.setDayOfMonth(1);
        consumeValidationTestData(settings, FilterParameters.TIME_WINDOW_FILTER_SETTING_END, RecurrenceConstants.OUT_OF_RANGE);

        // time window is bigger than interval when patter is absoluteYearly
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T00:00:01+08:00");
        pattern.setType("AbsoluteYearly");
        pattern.setInterval(1);
        pattern.setDayOfMonth(1);
        pattern.setMonth(9);
        consumeValidationTestData(settings, FilterParameters.TIME_WINDOW_FILTER_SETTING_END, RecurrenceConstants.OUT_OF_RANGE);

        // endDate is before first start time
        final ZonedDateTime startTime6 = ZonedDateTime.parse("2023-09-01T00:00:00+08:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        final ZonedDateTime endTime6 = ZonedDateTime.parse("2023-09-01T00:00:01+08:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        settings.setStart(startTime6.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        settings.setEnd(endTime6.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        pattern.setType("Daily");
        range.setType("EndDate");
        range.setEndDate("2023-08-31");
        consumeValidationTestData(settings, EndDate, RecurrenceConstants.OUT_OF_RANGE);

        // endDate is before first start time when different zone id
        settings.setStart("2023-09-01T23:00:00+00:00");
        settings.setEnd("2023-09-01T23:00:01+00:00");
        range.setEndDate("2023-09-01");
        range.setRecurrenceTimeZone("UTC+08:00");
        consumeValidationTestData(settings, EndDate, RecurrenceConstants.OUT_OF_RANGE);

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
        consumeValidationTestData(settings, DaysOfWeek, RecurrenceConstants.REQUIRED_PARAMETER);
    }

    @Test
    public void absoluteMonthlyPatternRequiredParameterTest() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T02:00:00+08:00");
        final RecurrenceRange range = new RecurrenceRange();
        range.setType("NoEnd");
        final Recurrence recurrence = new Recurrence();
        recurrence.setRange(range);
        settings.setRecurrence(recurrence);

        // dayOfMonth parameter is required
        final RecurrencePattern pattern = new RecurrencePattern();
        pattern.setType("AbsoluteMonthly");
        recurrence.setPattern(pattern);
        consumeValidationTestData(settings, DayOfMonth, RecurrenceConstants.REQUIRED_PARAMETER);
    }

    @Test
    public void relativeMonthlyPatternRequiredParameterTest() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T02:00:00+08:00");
        final RecurrenceRange range = new RecurrenceRange();
        range.setType("NoEnd");
        final Recurrence recurrence = new Recurrence();
        recurrence.setRange(range);
        settings.setRecurrence(recurrence);

        // daysOfWeek parameter is required
        final RecurrencePattern pattern1 = new RecurrencePattern();
        pattern1.setType("RelativeMonthly");
        recurrence.setPattern(pattern1);
        consumeValidationTestData(settings, DaysOfWeek, RecurrenceConstants.REQUIRED_PARAMETER);

        // index parameter is required
        final RecurrencePattern pattern2 = new RecurrencePattern();
        pattern2.setType("RelativeMonthly");
        pattern2.setDaysOfWeek(List.of("Friday"));
        pattern2.setIndex(null);
        recurrence.setPattern(pattern2);
        consumeValidationTestData(settings, Index, RecurrenceConstants.REQUIRED_PARAMETER);
    }

    @Test
    public void absoluteYearlyPatternRequiredParameterTest() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T02:00:00+08:00");
        final RecurrenceRange range = new RecurrenceRange();
        range.setType("NoEnd");
        final Recurrence recurrence = new Recurrence();
        recurrence.setRange(range);
        settings.setRecurrence(recurrence);

        // month parameter is required
        final RecurrencePattern pattern1 = new RecurrencePattern();
        pattern1.setType("AbsoluteYearly");
        pattern1.setDayOfMonth(1);
        recurrence.setPattern(pattern1);
        consumeValidationTestData(settings, Month, RecurrenceConstants.REQUIRED_PARAMETER);

        // dayOfMonth parameter is required
        final RecurrencePattern pattern2 = new RecurrencePattern();
        pattern2.setType("AbsoluteYearly");
        pattern2.setMonth(9);
        recurrence.setPattern(pattern2);
        consumeValidationTestData(settings, DayOfMonth, RecurrenceConstants.REQUIRED_PARAMETER);
    }

    @Test
    public void relativeYearlyPatternRequiredParameterTest() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T02:00:00+08:00");
        final RecurrenceRange range = new RecurrenceRange();
        range.setType("NoEnd");
        final Recurrence recurrence = new Recurrence();
        recurrence.setRange(range);
        settings.setRecurrence(recurrence);

        // daysOfWeek parameter is required
        final RecurrencePattern pattern1 = new RecurrencePattern();
        pattern1.setType("RelativeYearly");
        pattern1.setMonth(9);
        recurrence.setPattern(pattern1);
        consumeValidationTestData(settings, DaysOfWeek, RecurrenceConstants.REQUIRED_PARAMETER);

        // month parameter is required
        final RecurrencePattern pattern2 = new RecurrencePattern();
        pattern2.setType("RelativeYearly");
        pattern2.setDaysOfWeek(List.of("Friday"));
        recurrence.setPattern(pattern2);
        consumeValidationTestData(settings, Month, RecurrenceConstants.REQUIRED_PARAMETER);

        // index parameter is required
        final RecurrencePattern pattern3 = new RecurrencePattern();
        pattern3.setType("RelativeYearly");
        pattern3.setDaysOfWeek(List.of("Friday"));
        pattern3.setMonth(9);
        pattern3.setIndex(null);
        recurrence.setPattern(pattern3);
        consumeValidationTestData(settings, Index, RecurrenceConstants.REQUIRED_PARAMETER);
    }

    private void consumeValidationTestData(TimeWindowFilterSettings settings, String parameterName, String errorMessage) {
        final ZonedDateTime now = ZonedDateTime.now();

        try {
            final RecurrenceEvaluator evaluator = new RecurrenceEvaluator(settings, now);
            evaluator.matchRecurrence();
        } catch (final Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertEquals(e.getMessage(), String.format(errorMessage, parameterName));
        }
    }
}
