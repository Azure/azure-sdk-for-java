// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.filters.recurrence;

import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowFilterSettings;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.Recurrence;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceConstants;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceEvaluator;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrencePattern;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceRange;
import com.azure.spring.cloud.feature.management.models.FilterParameters;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RecurrenceEvaluatorTest {
    private final String Pattern = "Recurrence.Pattern";
    public final String PatternType = "Recurrence.Pattern.Type";
    public final String Interval = "Recurrence.Pattern.Interval";
    public final String Index = "Recurrence.Pattern.Index";
    public final String DaysOfWeek = "Recurrence.Pattern.DaysOfWeek";
    public final String FirstDayOfWeek = "Recurrence.Pattern.FirstDayOfWeek";
    public final String Month = "Recurrence.Pattern.Month";
    public final String DayOfMonth = "Recurrence.Pattern.DayOfMonth";
    public final String Range = "Recurrence.Range";
    public final String RangeType = "Recurrence.Range.Type";
    public final String NumberOfOccurrences = "Recurrence.Range.NumberOfOccurrences";
    public final String RecurrenceTimeZone = "Recurrence.Range.RecurrenceTimeZone";
    public final String EndDate = "Recurrence.Range.EndDate";
    @Test
    public void generalRequiredParameterTest() {
        final ZonedDateTime startTime = ZonedDateTime.now();
        final ZonedDateTime endTime = startTime.plusHours(2);

        // no end parameter
        final TimeWindowFilterSettings settings1 = new TimeWindowFilterSettings();
        settings1.setStart(startTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        settings1.setRecurrence(new Recurrence());
        consumeValidationTestData(settings1, FilterParameters.TIME_WINDOW_FILTER_SETTING_END,
            RecurrenceEvaluator.REQUIRED_PARAMETER);

        // no start parameter
        final TimeWindowFilterSettings settings2 = new TimeWindowFilterSettings();
        settings2.setEnd(endTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        settings2.setRecurrence(new Recurrence());
        consumeValidationTestData(settings2, FilterParameters.TIME_WINDOW_FILTER_SETTING_START,
            RecurrenceEvaluator.REQUIRED_PARAMETER);

        // no pattern in recurrence parameter
        final TimeWindowFilterSettings settings3 = new TimeWindowFilterSettings();
        settings3.setStart(startTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        settings3.setEnd(endTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        final Recurrence recurrence3 = new Recurrence();
        recurrence3.setRange(new RecurrenceRange());
        settings3.setRecurrence(recurrence3);
        consumeValidationTestData(settings3, Pattern, RecurrenceEvaluator.REQUIRED_PARAMETER);

        // no range in recurrence parameter
        final TimeWindowFilterSettings settings4 = new TimeWindowFilterSettings();
        settings4.setStart(startTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        settings4.setEnd(endTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        final Recurrence recurrence4 = new Recurrence();
        recurrence4.setPattern(new RecurrencePattern());
        settings4.setRecurrence(recurrence4);
        consumeValidationTestData(settings4, Range, RecurrenceEvaluator.REQUIRED_PARAMETER);

        // no type in recurrence pattern parameter
        final TimeWindowFilterSettings settings5 = new TimeWindowFilterSettings();
        settings5.setStart(startTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        settings5.setEnd(endTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        final Recurrence recurrence5 = new Recurrence();
        recurrence5.setPattern(new RecurrencePattern());
        recurrence5.setRange(new RecurrenceRange());
        settings5.setRecurrence(recurrence5);
        consumeValidationTestData(settings5, PatternType, RecurrenceEvaluator.REQUIRED_PARAMETER);

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
        consumeValidationTestData(settings6, RangeType, RecurrenceEvaluator.REQUIRED_PARAMETER);
    }

    @Test
    public void invalidValueTest() {
        final ZonedDateTime startTime = ZonedDateTime.of(2023, 9, 1, 0, 0, 0, 0, ZoneId.of("UTC+08:00"));
        final ZonedDateTime endTime = startTime.plusHours(2);

        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        settings.setStart(startTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        settings.setEnd(endTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        final RecurrencePattern pattern = new RecurrencePattern();
        pattern.setType("Daily");
        final RecurrenceRange range = new RecurrenceRange();
        range.setType("NoEnd");

        // invalid pattern type parameter
        final Recurrence recurrence1 = new Recurrence();
        final RecurrencePattern pattern1 = new RecurrencePattern();
        pattern1.setType("");
        recurrence1.setPattern(pattern1);
        recurrence1.setRange(range);
        settings.setRecurrence(recurrence1);
        consumeValidationTestData(settings, PatternType, RecurrenceEvaluator.UNRECOGNIZED_VALUE);

        // invalid range type parameter
        final Recurrence recurrence2 = new Recurrence();
        final RecurrenceRange range2 = new RecurrenceRange();
        range2.setType("");
        recurrence2.setPattern(pattern);
        recurrence2.setRange(range2);
        settings.setRecurrence(recurrence2);
        consumeValidationTestData(settings, RangeType, RecurrenceEvaluator.UNRECOGNIZED_VALUE);

        // invalid pattern interval
        final Recurrence recurrence3 = new Recurrence();
        final RecurrencePattern pattern3 = new RecurrencePattern();
        pattern3.setType("Daily");
        pattern3.setInterval(0);
        recurrence3.setPattern(pattern3);
        recurrence3.setRange(range);
        settings.setRecurrence(recurrence3);
        consumeValidationTestData(settings, Interval, RecurrenceEvaluator.OUT_OF_RANGE);

        // invalid firstDayOfWeek parameter
        final Recurrence recurrence4 = new Recurrence();
        final RecurrencePattern pattern4 = new RecurrencePattern();
        pattern4.setType("Weekly");
        pattern4.setFirstDayOfWeek("");
        pattern4.setDaysOfWeek(List.of("Monday"));
        recurrence4.setPattern(pattern4);
        recurrence4.setRange(range);
        settings.setRecurrence(recurrence4);
        consumeValidationTestData(settings, FirstDayOfWeek, RecurrenceEvaluator.UNRECOGNIZED_VALUE);

        // invalid daysOfWeek parameter
        final Recurrence recurrence5 = new Recurrence();
        final RecurrencePattern pattern5 = new RecurrencePattern();
        pattern5.setType("Weekly");
        pattern5.setDaysOfWeek(List.of("day"));
        recurrence5.setPattern(pattern5);
        recurrence5.setRange(range);
        settings.setRecurrence(recurrence5);
        consumeValidationTestData(settings, DaysOfWeek, RecurrenceEvaluator.UNRECOGNIZED_VALUE);

        // invalid pattern index parameter
        final Recurrence recurrence6 = new Recurrence();
        final RecurrencePattern pattern6 = new RecurrencePattern();
        pattern6.setType("RelativeMonthly");
        pattern6.setIndex("");
        pattern6.setDaysOfWeek(List.of("Friday"));
        recurrence6.setPattern(pattern6);
        recurrence6.setRange(range);
        settings.setRecurrence(recurrence6);
        consumeValidationTestData(settings, Index, RecurrenceEvaluator.UNRECOGNIZED_VALUE);

        // invalid pattern dayOfMonth parameter
        final Recurrence recurrence7 = new Recurrence();
        final RecurrencePattern pattern7 = new RecurrencePattern();
        pattern7.setType("AbsoluteMonthly");
        pattern7.setDayOfMonth(0);
        recurrence7.setPattern(pattern7);
        recurrence7.setRange(range);
        settings.setRecurrence(recurrence7);
        consumeValidationTestData(settings, DayOfMonth, RecurrenceEvaluator.OUT_OF_RANGE);

        // invalid pattern month parameter
        final Recurrence recurrence8 = new Recurrence();
        final RecurrencePattern pattern8 = new RecurrencePattern();
        pattern8.setType("AbsoluteMonthly");
        pattern8.setDayOfMonth(1);
        pattern8.setMonth(0);
        recurrence8.setPattern(pattern8);
        recurrence8.setRange(range);
        settings.setRecurrence(recurrence8);
        consumeValidationTestData(settings, Month, RecurrenceEvaluator.OUT_OF_RANGE);

        // invalid range recurrenceTimeZone parameter
        final Recurrence recurrence9 = new Recurrence();
        final RecurrenceRange range9 = new RecurrenceRange();
        range9.setType("NoEnd");
        range9.setRecurrenceTimeZone("");
        recurrence9.setPattern(pattern);
        recurrence9.setRange(range9);
        settings.setRecurrence(recurrence9);
        consumeValidationTestData(settings, RecurrenceTimeZone, RecurrenceEvaluator.UNRECOGNIZED_VALUE);

        // invalid range numberOfOccurrences parameter
        final Recurrence recurrence10 = new Recurrence();
        final RecurrenceRange range10 = new RecurrenceRange();
        range10.setType("Numbered");
        range10.setNumberOfRecurrences(0);
        recurrence10.setPattern(pattern);
        recurrence10.setRange(range10);
        settings.setRecurrence(recurrence10);
        consumeValidationTestData(settings, NumberOfOccurrences, RecurrenceEvaluator.OUT_OF_RANGE);
    }

    @Test
    public void invalidTimeWindowTest() {
        final ZonedDateTime startTime = ZonedDateTime.of(2023, 9, 25, 0, 0, 0, 0, ZoneId.of("UTC+08:00"));
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
        settings.setStart(startTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        settings.setEnd(startTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        consumeValidationTestData(settings, FilterParameters.TIME_WINDOW_FILTER_SETTING_END, RecurrenceEvaluator.OUT_OF_RANGE);

        // time window is bigger than interval when pattern is daily
        final ZonedDateTime endTime1 = startTime.plusDays(2).plusMinutes(1);
        settings.setEnd(endTime1.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        pattern.setInterval(2);
        consumeValidationTestData(settings, FilterParameters.TIME_WINDOW_FILTER_SETTING_END, RecurrenceEvaluator.OUT_OF_RANGE);

        // time window is bigger than interval when pattern is weekly
        final ZonedDateTime endTime2 = startTime.plusDays(7).plusMinutes(1);
        settings.setEnd(endTime2.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        pattern.setType("Weekly");
        pattern.setInterval(1);
        pattern.setDaysOfWeek(List.of("Monday"));
        consumeValidationTestData(settings, FilterParameters.TIME_WINDOW_FILTER_SETTING_END, RecurrenceEvaluator.OUT_OF_RANGE);

        // time window is bigger than interval when pattern is weekly
        final ZonedDateTime endTime3 = startTime.plusDays(2).plusMinutes(1);
        settings.setEnd(endTime3.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        pattern.setInterval(1);
        pattern.setDaysOfWeek(List.of("Monday", "Thursday", "Sunday"));
        consumeValidationTestData(settings, FilterParameters.TIME_WINDOW_FILTER_SETTING_END, RecurrenceEvaluator.OUT_OF_RANGE);

        // time window is bigger than interval when pattern is absoluteMonthly
        final ZonedDateTime startTime4 = ZonedDateTime.of(2023, 2, 1, 0, 0, 0, 0, ZoneId.of("UTC+08:00"));
        final ZonedDateTime endTime4 = ZonedDateTime.of(2023, 3, 31, 0, 1, 0, 0, ZoneId.of("UTC+08:00"));
        settings.setStart(startTime4.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        settings.setEnd(endTime4.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        pattern.setType("AbsoluteMonthly");
        pattern.setInterval(2);
        pattern.setDayOfMonth(1);
        consumeValidationTestData(settings, FilterParameters.TIME_WINDOW_FILTER_SETTING_END, RecurrenceEvaluator.OUT_OF_RANGE);

        // time window is bigger than interval when patter is absoluteYearly
        final ZonedDateTime startTime5 = ZonedDateTime.of(2023, 9, 1, 0, 0, 0, 0, ZoneId.of("UTC+08:00"));
        final ZonedDateTime endTime5 = ZonedDateTime.of(2024, 9, 1, 0, 1, 0, 0, ZoneId.of("UTC+08:00"));
        settings.setStart(startTime5.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        settings.setEnd(endTime5.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        pattern.setType("AbsoluteYearly");
        pattern.setInterval(1);
        pattern.setDayOfMonth(1);
        pattern.setMonth(9);
        consumeValidationTestData(settings, FilterParameters.TIME_WINDOW_FILTER_SETTING_END, RecurrenceEvaluator.OUT_OF_RANGE);

        // endDate is before first start time
        final ZonedDateTime startTime6 = ZonedDateTime.of(2023, 9, 1, 0, 0, 0, 0, ZoneId.of("UTC+08:00"));
        final ZonedDateTime endTime6 = ZonedDateTime.of(2023, 9, 1, 0, 1, 0, 0, ZoneId.of("UTC+08:00"));
        final ZonedDateTime endDate = ZonedDateTime.of(2023, 8, 31, 0, 0, 0, 0, ZoneId.of("UTC+08:00"));
        settings.setStart(startTime6.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        settings.setEnd(endTime6.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        pattern.setType("Daily");
        range.setType("EndDate");
        range.setEndDate(endDate.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        consumeValidationTestData(settings, FilterParameters.TIME_WINDOW_FILTER_SETTING_END, RecurrenceEvaluator.OUT_OF_RANGE);

        // endDate is before first start time when different zone id
        final ZonedDateTime startTime7 = ZonedDateTime.of(2023, 9, 1, 23, 0, 0, 0, ZoneId.of("UTC"));
        final ZonedDateTime endTime7 = ZonedDateTime.of(2023, 9, 1, 23, 1, 0, 0, ZoneId.of("UTC"));
        final ZonedDateTime endDate2 = ZonedDateTime.of(2023, 9, 1, 0, 0, 0, 0, ZoneId.of("UTC+08:00"));
        settings.setStart(startTime7.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        settings.setEnd(endTime7.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        range.setEndDate(endDate2.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        range.setRecurrenceTimeZone("UTC+08:00");
        consumeValidationTestData(settings, FilterParameters.TIME_WINDOW_FILTER_SETTING_END, RecurrenceEvaluator.OUT_OF_RANGE);

    }

    private void consumeValidationTestData(TimeWindowFilterSettings settings, String parameterName, String errorMessage) {
        final ZonedDateTime now = ZonedDateTime.now();

        try {
            RecurrenceEvaluator.matchRecurrence(now, settings);
        } catch (final Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertEquals(e.getMessage(), String.format(errorMessage, parameterName));
        }
    }
}
