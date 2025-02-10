// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.filters.recurrence;

import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowFilterSettings;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceEvaluator;
import com.azure.spring.cloud.feature.management.implementation.models.Recurrence;
import com.azure.spring.cloud.feature.management.implementation.models.RecurrencePattern;
import com.azure.spring.cloud.feature.management.implementation.models.RecurrenceRange;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecurrenceEvaluatorTest {

    @Test
    public void dailyTrue() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-02T00:00:00+08:00");
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Daily");
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, true);
    }

    @Test
    public void dailyMultiIntervalTrue() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-05T00:00:00+08:00");
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Daily");
        pattern.setInterval(4);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-03T00:00:00+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, true);
    }

    @Test
    public void dailyMultiIntervalTrue2() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-06T00:00:00+08:00");
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Daily");
        pattern.setInterval(4);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-03T00:00:00+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, true);
    }

    @Test
    public void dailyMultiIntervalTrue3() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-09T00:00:00+08:00"); // Within the recurring time window 2023-09-09T00:00:00+08:00 ~ 2023-09-11T00:00:00+08:00
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Daily");
        pattern.setInterval(4);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-03T00:00:00+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, true);
    }

    @Test
    public void dailyMultiIntervalFalse() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-03T00:00:00+08:00");
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Daily");
        pattern.setInterval(4);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-03T00:00:00+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, false);
    }

    @Test
    public void dailyNumberedRangeTrue() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-03T00:00:00+08:00"); // second occurrences
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Daily");
        pattern.setInterval(2);
        range.setType("Numbered");
        range.setNumberOfOccurrences(2);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, true);
    }

    @Test
    public void dailyNumberedRangeFalse() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-03T00:00:00+08:00"); // third occurrences
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Daily");
        range.setType("Numbered");
        range.setNumberOfOccurrences(2);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, false);
    }

    @Test
    public void dailyEndDateTrue() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-06T00:00:00+08:00");
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Daily");
        pattern.setInterval(3);
        range.setType("EndDate");
        range.setEndDate("2023-09-04T00:00:00+08:00");
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-03T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, true);
    }

    @Test
    public void dailyEndDateFalse() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-04T00:00:00+08:00");
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Daily");
        range.setType("EndDate");
        range.setEndDate("2023-09-03T00:00:00+08:00");
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, false);
    }

    @Test
    public void dailyDiffTimeZoneTrue() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-02T16:00:00+00:00"); // 2023-09-03T00:00:00+08:00
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Daily");
        pattern.setInterval(2);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T00:12:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, true);
    }

    @Test
    public void dailyDiffTimeZoneFalse() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-02T15:59:00+00:00"); // 2023-09-02T23:59:00+08:00
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Daily");
        pattern.setInterval(2);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T00:12:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, false);
    }


    @Test
    public void dailyRFCFormatTrue() {
        final ZonedDateTime now = ZonedDateTime.parse("Sat, 02 Sep 2023 00:00:00 +0800", DateTimeFormatter.RFC_1123_DATE_TIME);
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Daily");
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("Fri, 01 Sep 2023 00:00:00 +0800");
        settings.setEnd("Fri, 01 Sep 2023 00:00:01 +0800");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, true);
    }

    @Test
    public void dailyRFCFormatFalse() {
        final ZonedDateTime now = ZonedDateTime.parse("Sun, 03 Sep 2023 00:00:00 +0800", DateTimeFormatter.RFC_1123_DATE_TIME); // third occurrences
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Daily");
        range.setType("Numbered");
        range.setNumberOfOccurrences(2);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("Fri, 01 Sep 2023 00:00:00 +0800");
        settings.setEnd("Fri, 01 Sep 2023 00:00:01 +0800");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, false);
    }

    @Test
    public void weeklyDaysOfWeekTrue() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-04T00:00:00+08:00"); // Monday in the 2nd week
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Friday"));
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00"); // Friday
        settings.setEnd("2023-09-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, true);
    }

    @Test
    public void weeklyDayOfWeekFalse() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-04T00:00:00+08:00"); // Monday
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Sunday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")); // No Monday
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00"); // Friday
        settings.setEnd("2023-09-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, false);
    }

    @Test
    public void weeklyDaysOfWeekIntervalTrue() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-04T00:00:00+08:00"); // Monday in the first week
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Sunday"));
        pattern.setInterval(2);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-03T00:00:00+08:00"); // Sunday
        settings.setEnd("2023-09-03T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, true);
    }

    @Test
    public void weeklyDayOfWeekIntervalTrue2() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-15T00:00:00+08:00"); // Friday in the third week after the start date
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Friday"));
        pattern.setInterval(2);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00"); // Friday
        settings.setEnd("2023-09-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, true);
    }

    @Test
    public void weeklyDayOfWeekIntervalTrue3() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-04T00:00:00+08:00"); // Monday in the 1st week after the Start date
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Sunday"));
        pattern.setInterval(2);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-03T00:00:00+08:00"); // Sunday
        settings.setEnd("2023-09-03T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, true);
    }

    @Test
    public void weeklyDaysOfWeekIntervalFalse() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-04T00:00:00+08:00"); // Monday in the second week
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Sunday"));
        pattern.setInterval(2);
        pattern.setFirstDayOfWeek("Monday");
        range.setType("NoEnd");
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-03T00:00:00+08:00"); // Sunday
        settings.setEnd("2023-09-03T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, false);
    }

    @Test
    public void weeklyDayOfWeekIntervalFalse2() {
        final ZonedDateTime now = ZonedDateTime.parse("2024-02-12T08:00:00+08:00"); // Monday in the 3rd week after the Start date
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Friday", "Monday"));
        pattern.setFirstDayOfWeek("Sunday");
        pattern.setInterval(2);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2024-02-02T12:00:00+08:00"); // Friday
        settings.setEnd("2024-02-03T12:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, false);
    }

    @Test
    public void weeklyDayOfWeekIntervalFalse3() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-04T00:00:00+08:00"); // Monday in the 2nd week after the Start date
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Sunday"));
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(2);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-03T00:00:00+08:00"); // Sunday
        settings.setEnd("2023-09-03T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, false);
    }

    @Test
    public void weeklyDaysOfWeekNumberedRangeTrue() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-03T00:00:00+08:00"); // Third occurrence
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"));
        range.setType("Numbered");
        range.setNumberOfOccurrences(3);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00"); // Friday
        settings.setEnd("2023-09-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, true);
    }

    @Test
    public void weeklyDaysOfWeekNumberedRangeTrue2() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-22T00:00:00+08:00"); // 4th occurrence
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Friday"));
        range.setType("Numbered");
        range.setNumberOfOccurrences(4);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00"); // Friday
        settings.setEnd("2023-09-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, true);
    }

    @Test
    public void weeklyDaysOfWeekNumberedRangeTrue3() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-25T00:00:00+08:00"); // 4th occurrence
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Sunday"));
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(2);
        range.setType("Numbered");
        range.setNumberOfOccurrences(4);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-03T00:00:00+08:00"); // Sunday
        settings.setEnd("2023-09-03T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, true);
    }

    @Test
    public void weeklyDaysOfWeekNumberedRangeFalse() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-04T00:00:00+08:00"); // 4th occurrence
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"));
        range.setType("Numbered");
        range.setNumberOfOccurrences(3);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00"); // Friday
        settings.setEnd("2023-09-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, false);
    }

    @Test
    public void weeklyDaysOfWeekNumberedRangeFalse2() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-29T00:00:00+08:00"); // 5th occurrence
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Friday"));
        range.setType("Numbered");
        range.setNumberOfOccurrences(4);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00"); // Friday
        settings.setEnd("2023-09-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, false);
    }

    @Test
    public void weeklyDaysOfWeekNumberedRangeFalse3() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-10-01T00:00:00+08:00"); // 5th occurrence
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Sunday"));
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(2);
        range.setType("Numbered");
        range.setNumberOfOccurrences(4);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-03T00:00:00+08:00"); // Sunday
        settings.setEnd("2023-09-03T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, false);
    }

    @Test
    public void weeklyDaysOfWeekEndDateRangeTrue() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-29T00:00:00+08:00");
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Friday"));
        range.setType("EndDate");
        range.setEndDate("2023-09-29T00:00:00+08:00");
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00"); // Friday
        settings.setEnd("2023-09-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, true);
    }

    @Test
    public void weeklyDaysOfWeekEndDateRangeTrue2() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-30T00:00:00+08:00");
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Friday"));
        range.setType("EndDate");
        range.setEndDate("2023-09-29T00:00:00+08:00");
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00"); // Friday
        settings.setEnd("2023-09-02T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, true);
    }

    @Test
    public void weeklyDaysOfWeekEndDateRangeFalse() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-10-06T00:00:00+08:00");
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Friday"));
        range.setType("EndDate");
        range.setEndDate("2023-09-29T00:00:00+08:00");
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00"); // Friday
        settings.setEnd("2023-09-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, false);
    }

    @Test
    public void weeklyDaysOfWeekIntervalNumberedRangeTrue() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-17T00:00:00+08:00"); // Sunday in the 3rd week
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Sunday"));
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(2);
        range.setType("Numbered");
        range.setNumberOfOccurrences(3);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-03T00:00:00+08:00"); // Sunday
        settings.setEnd("2023-09-03T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, true);
    }

    @Test
    public void weeklyTimeWindowAcrossDaysIntervalTrue() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-13T08:00:00+08:00"); // Within the recurring time window 2023-09-11T:00:00:00+08:00 ~ 2023-09-15T:00:00:00+08:00
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Sunday", "Monday")); // Time window occurrences: 9-3 ~ 9-7 (1st week), 9-11 ~ 9-15 (3rd week) and 9-17 ~ 9-21 (3rd week)
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(2);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-03T00:00:00+08:00");
        settings.setEnd("2023-09-07T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, true);
    }

    @Test
    public void weeklyTimeWindowAcrossDaysNumberedRangeTrue() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-19T00:00:00+08:00"); // The 3rd occurrence: 2023-9-17T:00:00:00+08:00 ~ 2023-9-21T:00:00:00+08:00
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Sunday")); // Time window occurrences: 9-3 ~ 9-7 (1st week), 9-11 ~ 9-15 (3rd week) and 9-17 ~ 9-21 (3rd week)
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(2);
        range.setType("Numbered");
        range.setNumberOfOccurrences(3);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-03T00:00:00+08:00"); // Sunday
        settings.setEnd("2023-09-07T00:00:00+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, true);
    }

    @Test
    public void weeklyTimeWindowAcrossDaysNumberedRangeFalse() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-19T00:00:00+08:00"); // 3rd occurrence
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Sunday")); // Time window occurrences: 9-3 ~ 9-7 (1st occurrence), 9-11 ~ 9-15 (2nd occurrence) and 9-17 ~ 9-21 (3rd occurrence)
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(2);
        range.setType("Numbered");
        range.setNumberOfOccurrences(2);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-03T00:00:00+08:00"); // Sunday
        settings.setEnd("2023-09-07T00:00:00+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, false);
    }

    @Test
    public void weeklyDiffTimeZoneTrue() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-03T16:00:00+00:00"); // Monday in the 2nd week after the Start date if timezone is UTC+8
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Friday", "Monday"));
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, true);
    }

    @Test
    public void weeklyDiffTimeZoneTrue2() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-07T16:00:00+00:00"); // Friday in the 2nd week after the Start date if timezone is UTC+8
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Friday", "Monday"));
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, true);
    }

    @Test
    public void weeklyDiffTimeZoneFalse() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-03T15:59:00+00:00"); // Sunday in the 2nd week after the Start date if timezone is UTC+8
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Friday", "Monday"));
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, false);
    }

    @Test
    public void weeklyDiffTimeZoneFalse2() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-07T15:59:00+00:00"); // Thursday in the 2nd week after the Start date if timezone is UTC+8
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Friday", "Monday"));
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-01T00:00:00+08:00");
        settings.setEnd("2023-09-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, false);
    }

    @Test
    public void weeklyDiffTimeZoneIntervalTrue() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-10T16:00:00+00:00"); // Within the recurring time window 2023-09-11T:00:00:00+08:00 ~ 2023-9-15T:00:00:00+08:00
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Sunday", "Monday")); // Time window occurrences: 9-3 ~ 9-7, 9-11 ~ 9-15 and 9-17 ~ 9-21
        pattern.setInterval(2);
        pattern.setFirstDayOfWeek("Monday");
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-03T00:00:00+08:00");
        settings.setEnd("2023-09-07T00:00:00+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, true);
    }

    @Test
    public void weeklyDiffTimeZoneIntervalFalse() {
        final ZonedDateTime now = ZonedDateTime.parse("2023-09-10T15:59:00+00:00"); // Within the recurring time window 2023-09-11T:00:00:00+08:00 ~ 2023-9-15T:00:00:00+08:00
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Sunday", "Monday")); // Time window occurrences: 9-3 ~ 9-7, 9-11 ~ 9-15 and 9-17 ~ 9-21
        pattern.setInterval(2);
        pattern.setFirstDayOfWeek("Monday");
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2023-09-03T00:00:00+08:00");
        settings.setEnd("2023-09-07T00:00:00+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, false);
    }

    @Test
    public void weeklyRFCFormatTrue() {
        final ZonedDateTime now = ZonedDateTime.parse("Mon, 04 Sep 2023 00:00:00 +0800", DateTimeFormatter.RFC_1123_DATE_TIME); // Monday in the 2nd week
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Friday"));
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("Fri, 01 Sep 2023 00:00:00 +0800"); // Friday
        settings.setEnd("Fri, 01 Sep 2023 00:00:01 +0800");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, true);
    }

    @Test
    public void weeklyRFCFormatFalse() {
        final ZonedDateTime now = ZonedDateTime.parse("Tue, 19 Sep 2023 00:00:00 GMT", DateTimeFormatter.RFC_1123_DATE_TIME); // 3rd occurrence
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Sunday")); // Time window occurrences: 9-3 ~ 9-7 (1st occurrence), 9-11 ~ 9-15 (2nd occurrence) and 9-17 ~ 9-21 (3rd occurrence)
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(2);
        range.setType("Numbered");
        range.setNumberOfOccurrences(2);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("Sun, 03 Sep 2023 00:00:00 +0800");
        settings.setEnd("2023-09-07T00:00:00+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, false);
    }

    private void consumeEvaluationTestData(TimeWindowFilterSettings settings, ZonedDateTime now, boolean isEnabled) {
        assertEquals(RecurrenceEvaluator.isMatch(settings, now), isEnabled);
    }
}
