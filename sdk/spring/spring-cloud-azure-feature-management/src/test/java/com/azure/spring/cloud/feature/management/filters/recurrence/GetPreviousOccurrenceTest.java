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

public class GetPreviousOccurrenceTest {
    @Test
    public void dailyBeforeStart() {
        final ZonedDateTime now = ZonedDateTime.parse("2024-02-28T00:00:00+08:00");
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Daily");
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2024-03-01T00:00:00+08:00");
        settings.setEnd("2024-03-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, ZonedDateTime.parse("2024-03-01T00:00:00+08:00"));
    }

    @Test
    public void dailyAfterStart() {
        final ZonedDateTime now = ZonedDateTime.parse("2024-02-28T00:00:00+08:00");
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Daily");
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2024-02-01T00:00:00+08:00");
        settings.setEnd("2024-02-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, ZonedDateTime.parse("2024-02-28T00:00:00+08:00"));
    }

    @Test
    public void dailyInterval() {
        final ZonedDateTime now = ZonedDateTime.parse("2024-02-28T00:00:00+08:00");
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Daily");
        pattern.setInterval(2);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2024-02-01T00:00:00+08:00");
        settings.setEnd("2024-02-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, ZonedDateTime.parse("2024-02-29T00:00:00+08:00"));
    }

    @Test
    public void dailyInterval2() {
        final ZonedDateTime now = ZonedDateTime.parse("2024-02-28T00:00:00+08:00");
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Daily");
        pattern.setInterval(3);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2024-02-01T00:00:00+08:00");
        settings.setEnd("2024-02-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, ZonedDateTime.parse("2024-02-28T00:00:00+08:00"));
    }

    @Test
    public void dailyNumbered() {
        final ZonedDateTime now = ZonedDateTime.parse("2024-02-28T00:00:00+08:00");
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Daily");
        range.setType("Numbered");
        range.setNumberOfRecurrences(27);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2024-02-01T00:00:00+08:00");
        settings.setEnd("2024-02-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, null);
    }

    @Test
    public void dailyNumbered2() {
        final ZonedDateTime now = ZonedDateTime.parse("2024-02-28T00:00:00+08:00");
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Daily");
        range.setType("Numbered");
        range.setNumberOfRecurrences(28);
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2024-02-01T00:00:00+08:00");
        settings.setEnd("2024-02-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, ZonedDateTime.parse("2024-02-28T00:00:00+08:00"));
    }

    @Test
    public void dailyEndDate() {
        final ZonedDateTime now = ZonedDateTime.parse("2024-02-28T00:00:00+08:00");
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Daily");
        range.setType("EndDate");
        range.setEndDate("2024-02-27T00:00:00+08:00");
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2024-02-01T00:00:00+08:00");
        settings.setEnd("2024-02-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, null);
    }

    @Test
    public void dailyEndDate2() {
        final ZonedDateTime now = ZonedDateTime.parse("2024-02-28T00:00:00+08:00");
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Daily");
        range.setType("EndDate");
        range.setEndDate("2024-02-28T00:00:00+08:00");
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2024-02-01T00:00:00+08:00");
        settings.setEnd("2024-02-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, ZonedDateTime.parse("2024-02-28T00:00:00+08:00"));
    }

    @Test
    public void weeklyBeforeStart() {
        final ZonedDateTime now = ZonedDateTime.parse("2024-02-01T00:00:00+08:00");
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Thursday"));
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2024-02-29T00:00:00+08:00");
        settings.setEnd("2024-02-29T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, ZonedDateTime.parse("2024-02-29T00:00:00+08:00"));
    }

    @Test
    public void weeklyDaysOfWeek() {
        final ZonedDateTime now = ZonedDateTime.parse("2024-02-29T00:00:00+08:00");
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Thursday"));
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2024-02-29T00:00:00+08:00"); //Thursday
        settings.setEnd("2024-02-29T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, ZonedDateTime.parse("2024-02-29T00:00:00+08:00"));
    }

    @Test
    public void weeklyDaysOfWeek2() {
        final ZonedDateTime now = ZonedDateTime.parse("2024-02-29T00:00:00+08:00");
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Thursday"));
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        settings.setStart("2024-02-01T00:00:00+08:00"); //Thursday
        settings.setEnd("2024-02-01T00:00:01+08:00");
        settings.setRecurrence(recurrence);
        consumeEvaluationTestData(settings, now, ZonedDateTime.parse("2024-02-29T00:00:00+08:00"));
    }




    private void consumeEvaluationTestData(TimeWindowFilterSettings settings, ZonedDateTime now, ZonedDateTime expectedTime) {
        final RecurrenceEvaluator evaluator = new RecurrenceEvaluator(settings, now);
        assertEquals(evaluator.calculateClosestStart(), expectedTime);
    }
}
