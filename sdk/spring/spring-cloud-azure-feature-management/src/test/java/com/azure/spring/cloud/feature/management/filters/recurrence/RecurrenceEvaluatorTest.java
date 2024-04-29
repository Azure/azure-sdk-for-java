// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.filters.recurrence;

import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowFilterSettings;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceEvaluator;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.models.Recurrence;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.models.RecurrencePattern;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.models.RecurrenceRange;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecurrenceEvaluatorTest {

    @Test
    public void dailyTrue() {
        final ZonedDateTime now1 = ZonedDateTime.parse("2023-09-02T00:00:00+08:00");
        final TimeWindowFilterSettings settings1 = new TimeWindowFilterSettings();
        final RecurrencePattern pattern1 = new RecurrencePattern();
        final RecurrenceRange range1 = new RecurrenceRange();
        final Recurrence recurrence1 = new Recurrence();
        pattern1.setType("Daily");
        recurrence1.setRange(range1);
        recurrence1.setPattern(pattern1);
        settings1.setStart("2023-09-01T00:00:00+08:00");
        settings1.setEnd("2023-09-01T00:00:01+08:00");
        settings1.setRecurrence(recurrence1);
        consumeEvaluationTestData(settings1, now1, true);
    }

    @Test
    public void dailyMultiIntervalTrue() {
        final ZonedDateTime now2 = ZonedDateTime.parse("2023-09-05T00:00:00+08:00");
        final TimeWindowFilterSettings settings2 = new TimeWindowFilterSettings();
        final RecurrencePattern pattern2 = new RecurrencePattern();
        final RecurrenceRange range2 = new RecurrenceRange();
        final Recurrence recurrence2 = new Recurrence();
        pattern2.setType("Daily");
        pattern2.setInterval(4);
        recurrence2.setRange(range2);
        recurrence2.setPattern(pattern2);
        settings2.setStart("2023-09-01T00:00:00+08:00");
        settings2.setEnd("2023-09-03T00:00:00+08:00");
        settings2.setRecurrence(recurrence2);
        consumeEvaluationTestData(settings2, now2, true);
    }

    @Test
    public void dailyMultiIntervalTrue2() {
        final ZonedDateTime now3 = ZonedDateTime.parse("2023-09-06T00:00:00+08:00");
        final TimeWindowFilterSettings settings3 = new TimeWindowFilterSettings();
        final RecurrencePattern pattern3 = new RecurrencePattern();
        final RecurrenceRange range3 = new RecurrenceRange();
        final Recurrence recurrence3 = new Recurrence();
        pattern3.setType("Daily");
        pattern3.setInterval(4);
        recurrence3.setRange(range3);
        recurrence3.setPattern(pattern3);
        settings3.setStart("2023-09-01T00:00:00+08:00");
        settings3.setEnd("2023-09-03T00:00:00+08:00");
        settings3.setRecurrence(recurrence3);
        consumeEvaluationTestData(settings3, now3, true);
    }

    @Test
    public void dailyNumberedRangeFalse() {
        final ZonedDateTime now4 = ZonedDateTime.parse("2023-09-03T00:00:00+08:00");
        final TimeWindowFilterSettings settings4 = new TimeWindowFilterSettings();
        final RecurrencePattern pattern4 = new RecurrencePattern();
        final RecurrenceRange range4 = new RecurrenceRange();
        final Recurrence recurrence4 = new Recurrence();
        pattern4.setType("Daily");
        range4.setType("Numbered");
        range4.setNumberOfRecurrences(2);
        recurrence4.setRange(range4);
        recurrence4.setPattern(pattern4);
        settings4.setStart("2023-09-01T00:00:00+08:00");
        settings4.setEnd("2023-09-01T00:00:01+08:00");
        settings4.setRecurrence(recurrence4);
        consumeEvaluationTestData(settings4, now4, false);
    }

    @Test
    public void weeklyDaysOfWeekTrue() {
        final ZonedDateTime now1 = ZonedDateTime.parse("2023-09-04T00:00:00+08:00");    // Monday in the 2nd week
        final TimeWindowFilterSettings settings1 = new TimeWindowFilterSettings();
        final RecurrencePattern pattern1 = new RecurrencePattern();
        final RecurrenceRange range1 = new RecurrenceRange();
        final Recurrence recurrence1 = new Recurrence();
        pattern1.setType("Weekly");
        pattern1.setDaysOfWeek(List.of("Monday", "Friday"));
        recurrence1.setRange(range1);
        recurrence1.setPattern(pattern1);
        settings1.setStart("2023-09-01T00:00:00+08:00");    // Friday
        settings1.setEnd("2023-09-01T00:00:01+08:00");
        settings1.setRecurrence(recurrence1);
        consumeEvaluationTestData(settings1, now1, true);
    }

    @Test
    public void weeklyDaysOfWeekIntervalTrue() {
        final ZonedDateTime now3 = ZonedDateTime.parse("2023-09-04T00:00:00+08:00");    // Monday in the first week
        final TimeWindowFilterSettings settings3 = new TimeWindowFilterSettings();
        final RecurrencePattern pattern3 = new RecurrencePattern();
        final RecurrenceRange range3 = new RecurrenceRange();
        final Recurrence recurrence3 = new Recurrence();
        pattern3.setType("Weekly");
        pattern3.setDaysOfWeek(List.of("Monday", "Sunday"));
        pattern3.setInterval(2);
        recurrence3.setRange(range3);
        recurrence3.setPattern(pattern3);
        settings3.setStart("2023-09-03T00:00:00+08:00");    // Sunday
        settings3.setEnd("2023-09-03T00:00:01+08:00");
        settings3.setRecurrence(recurrence3);
        consumeEvaluationTestData(settings3, now3, true);
    }

    @Test
    public void weeklyDaysOfWeekIntervalFalse() {
        final ZonedDateTime now2 = ZonedDateTime.parse("2023-09-04T00:00:00+08:00");    // Monday in the second week
        final TimeWindowFilterSettings settings2 = new TimeWindowFilterSettings();
        final RecurrencePattern pattern2 = new RecurrencePattern();
        final RecurrenceRange range2 = new RecurrenceRange();
        final Recurrence recurrence2 = new Recurrence();
        pattern2.setType("Weekly");
        pattern2.setDaysOfWeek(List.of("Monday", "Sunday"));
        pattern2.setInterval(2);
        pattern2.setFirstDayOfWeek("Monday");
        range2.setType("NoEnd");
        recurrence2.setRange(range2);
        recurrence2.setPattern(pattern2);
        settings2.setStart("2023-09-03T00:00:00+08:00");    // Sunday
        settings2.setEnd("2023-09-03T00:00:01+08:00");
        settings2.setRecurrence(recurrence2);
        consumeEvaluationTestData(settings2, now2, false);
    }

    @Test
    public void weeklyDaysOfWeekIntervalNumberedRangeTrue() {
        final ZonedDateTime now4 = ZonedDateTime.parse("2023-09-17T00:00:00+08:00");    // Sunday in the 3rd week
        final TimeWindowFilterSettings settings4 = new TimeWindowFilterSettings();
        final RecurrencePattern pattern4 = new RecurrencePattern();
        final RecurrenceRange range4 = new RecurrenceRange();
        final Recurrence recurrence4 = new Recurrence();
        pattern4.setType("Weekly");
        pattern4.setDaysOfWeek(List.of("Monday", "Sunday"));
        pattern4.setFirstDayOfWeek("Monday");
        pattern4.setInterval(2);
        range4.setType("Numbered");
        range4.setNumberOfRecurrences(3);
        recurrence4.setRange(range4);
        recurrence4.setPattern(pattern4);
        settings4.setStart("2023-09-03T00:00:00+08:00");    // Sunday
        settings4.setEnd("2023-09-03T00:00:01+08:00");
        settings4.setRecurrence(recurrence4);
        consumeEvaluationTestData(settings4, now4, true);
    }

    @Test
    public void weeklyTimeWindowAcrossDaysNumberedRangeTrue() {
        final ZonedDateTime now5 = ZonedDateTime.parse("2023-09-19T00:00:00+08:00");    // Tuesday in the 4th week
        final TimeWindowFilterSettings settings5 = new TimeWindowFilterSettings();
        final RecurrencePattern pattern5 = new RecurrencePattern();
        final RecurrenceRange range5 = new RecurrenceRange();
        final Recurrence recurrence5 = new Recurrence();
        pattern5.setType("Weekly");
        pattern5.setDaysOfWeek(List.of("Monday", "Sunday"));
        pattern5.setFirstDayOfWeek("Monday");
        pattern5.setInterval(2);
        range5.setType("Numbered");
        range5.setNumberOfRecurrences(3);
        recurrence5.setRange(range5);
        recurrence5.setPattern(pattern5);
        settings5.setStart("2023-09-03T00:00:00+08:00");    // Sunday
        settings5.setEnd("2023-09-07T00:00:00+08:00");
        settings5.setRecurrence(recurrence5);
        consumeEvaluationTestData(settings5, now5, true);
    }

    @Test
    public void weeklyTimeWindowAcrossDaysNumberedRangeFalse() {
        final ZonedDateTime now6 = ZonedDateTime.parse("2023-09-19T00:00:00+08:00");    // Tuesday in the 4th week
        final TimeWindowFilterSettings settings6 = new TimeWindowFilterSettings();
        final RecurrencePattern pattern6 = new RecurrencePattern();
        final RecurrenceRange range6 = new RecurrenceRange();
        final Recurrence recurrence6 = new Recurrence();
        pattern6.setType("Weekly");
        pattern6.setDaysOfWeek(List.of("Monday", "Sunday"));
        pattern6.setFirstDayOfWeek("Monday");
        pattern6.setInterval(2);
        range6.setType("Numbered");
        range6.setNumberOfRecurrences(2);
        recurrence6.setRange(range6);
        recurrence6.setPattern(pattern6);
        settings6.setStart("2023-09-03T00:00:00+08:00");    // Sunday
        settings6.setEnd("2023-09-07T00:00:00+08:00");
        settings6.setRecurrence(recurrence6);
        consumeEvaluationTestData(settings6, now6, false);
    }

    private void consumeEvaluationTestData(TimeWindowFilterSettings settings, ZonedDateTime now, boolean isEnabled) {
        final RecurrenceEvaluator evaluator = new RecurrenceEvaluator(settings, now);
        assertEquals(evaluator.isMatch(), isEnabled);
    }
}
