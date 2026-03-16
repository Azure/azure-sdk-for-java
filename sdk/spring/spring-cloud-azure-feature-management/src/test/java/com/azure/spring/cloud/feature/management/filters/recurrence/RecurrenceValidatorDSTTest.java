// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.filters.recurrence;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.azure.spring.cloud.feature.management.implementation.models.Recurrence;
import com.azure.spring.cloud.feature.management.implementation.models.RecurrencePattern;
import com.azure.spring.cloud.feature.management.implementation.models.RecurrenceRange;
import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowFilterSettings;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceValidator;

/**
 * Tests for Daylight Saving Time (DST) handling in RecurrenceValidator.
 * These tests verify that validation uses UTC time to avoid DST-related issues
 * when calculating time window durations and validating recurrence patterns.
 * 
 * Tests use time windows close to 23-24 hours (the minimum gap between daily occurrences)
 * to ensure that DST transitions don't cause incorrect validation failures.
 */
public class RecurrenceValidatorDSTTest {

    /**
     * Test that a 23.5-hour window between Monday and Wednesday is valid when using UTC,
     * even during a DST transition week. Without UTC, the validator might incorrectly
     * calculate the gap as 22.5 or 24.5 hours depending on DST.
     * 
     * This test uses a reference time on the DST transition day (Sunday, March 31, 2024)
     * to ensure the validator correctly calculates gaps in UTC.
     */
    @Test
    public void validate23HourWindowDuringDSTSpringForward() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Wednesday")); // 48-hour gap minimum
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(1);
        
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        
        // 23.5-hour window (just under 24 hours, well within 48-hour gap)
        settings.setStart("2024-03-25T10:00:00+01:00"); // Monday, before DST
        settings.setEnd("2024-03-26T09:30:00+01:00"); // Tuesday, 23.5 hours later
        settings.setRecurrence(recurrence);
        
        // Reference time during DST transition week (Sunday, March 31, 2024 in UTC)
        // If validator used local time incorrectly, it might miscalculate the Monday-Wednesday gap
        final ZonedDateTime referenceDuringDST = ZonedDateTime.of(2024, 3, 31, 12, 0, 0, 0, ZoneOffset.UTC);
        
        // Should pass because 23.5 hours < 48 hours (Monday to Wednesday gap)
        assertDoesNotThrow(() -> RecurrenceValidator.validateSettings(settings, referenceDuringDST),
            "23.5-hour window should be valid with 48-hour gap, even during DST");
    }

    /**
     * Test that a 23-hour window is valid with consecutive day recurrence (Monday, Tuesday).
     * Without UTC, if the validator ran on the DST transition day, it might incorrectly
     * calculate the Monday-Tuesday gap as 23 hours (losing 1 hour), causing validation to fail.
     */
    @Test
    public void validate23HourWindowWithConsecutiveDays() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Tuesday")); // 24-hour gap minimum
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(1);
        
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        
        // 23-hour window (1 hour less than 24-hour gap)
        settings.setStart("2024-03-25T10:00:00+01:00"); // Monday
        settings.setEnd("2024-03-26T09:00:00+01:00"); // Tuesday, 23 hours later
        settings.setRecurrence(recurrence);
        
        // Reference time on DST transition day - without UTC, the Monday-Tuesday gap
        // might be calculated as 23 hours due to DST, causing incorrect failure
        final ZonedDateTime referenceDuringDST = ZonedDateTime.of(2024, 3, 31, 2, 30, 0, 0, 
            ZoneId.of("Europe/Paris"));
        
        // Should pass because 23 hours < 24 hours (UTC calculation)
        assertDoesNotThrow(() -> RecurrenceValidator.validateSettings(settings, referenceDuringDST),
            "23-hour window should be valid with 24-hour gap when calculated in UTC");
    }

    /**
     * Test that a window GREATER than the gap fails validation with consecutive day recurrence.
     * The window must not exceed the gap between occurrences.
     */
    @Test
    public void validate25HourWindowWithConsecutiveDaysShouldFail() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Tuesday")); // 24-hour gap minimum
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(1);
        
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        
        // 25-hour window (greater than 24-hour gap, should fail)
        settings.setStart("2024-03-25T10:00:00+01:00"); // Monday
        settings.setEnd("2024-03-26T11:00:00+01:00"); // Tuesday, 25 hours later
        settings.setRecurrence(recurrence);
        
        final ZonedDateTime reference = ZonedDateTime.of(2024, 3, 31, 12, 0, 0, 0, ZoneOffset.UTC);
        
        // Should fail because window duration (25h) > gap (24h)
        assertThrows(IllegalArgumentException.class, 
            () -> RecurrenceValidator.validateSettings(settings, reference),
            "25-hour window should fail validation with 24-hour gap");
    }

    /**
     * Test validation across DST fall back transition.
     * A 23.5-hour window on Sunday-Tuesday should be valid even though
     * the Sunday night has an extra hour (25-hour day).
     */
    @Test
    public void validate23HourWindowDuringDSTFallBack() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Sunday", "Tuesday")); // 48-hour gap minimum
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(1);
        
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        
        // 23.5-hour window starting on Sunday before DST fall back
        settings.setStart("2024-10-27T10:00:00+02:00"); // Sunday (daylight time)
        settings.setEnd("2024-10-28T09:30:00+01:00"); // Monday, 23.5 hours later (standard time)
        settings.setRecurrence(recurrence);
        
        // Reference time during DST fall back week
        final ZonedDateTime referenceDuringFallBack = ZonedDateTime.of(2024, 10, 27, 12, 0, 0, 0, 
            ZoneOffset.UTC);
        
        // Should pass because 23.5 hours < 48 hours (Sunday to Tuesday gap in UTC)
        assertDoesNotThrow(() -> RecurrenceValidator.validateSettings(settings, referenceDuringFallBack),
            "23.5-hour window should be valid during DST fall back when calculated in UTC");
    }

    /**
     * Test that a time window spanning the DST spring forward transition hour
     * is correctly validated using UTC duration calculation.
     */
    @Test
    public void validateTimeWindowSpanningDSTSpringForwardHour() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Sunday")); // Single day, no gap check
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(1);
        
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        
        // DST spring forward in Europe/Paris: 2024-03-31 at 2:00 AM -> 3:00 AM
        // Time window from 1:30 to 3:30 - appears to be 2 hours locally but is actually 1 hour in UTC
        settings.setStart("2024-03-31T01:30:00+01:00"); // Sunday, 00:30 UTC
        settings.setEnd("2024-03-31T03:30:00+02:00"); // Sunday, 01:30 UTC (1 hour duration)
        settings.setRecurrence(recurrence);
        
        final ZonedDateTime reference = ZonedDateTime.of(2024, 3, 31, 12, 0, 0, 0, ZoneOffset.UTC);
        
        // Should pass - window is only 1 hour in UTC despite appearing as 2 hours locally
        assertDoesNotThrow(() -> RecurrenceValidator.validateSettings(settings, reference),
            "Window spanning DST transition should be validated using UTC duration");
    }

    /**
     * Test with daily recurrence and 23-hour window.
     * Daily recurrence has a 24-hour minimum gap, so 23-hour window should be valid.
     */
    @Test
    public void validateDailyRecurrence23HourWindow() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        
        pattern.setType("Daily");
        pattern.setInterval(1);
        
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        
        // 23-hour window
        settings.setStart("2024-03-30T10:00:00+01:00");
        settings.setEnd("2024-03-31T09:00:00+01:00"); // 23 hours later
        settings.setRecurrence(recurrence);
        
        final ZonedDateTime reference = ZonedDateTime.of(2024, 3, 31, 12, 0, 0, 0, ZoneOffset.UTC);
        
        assertDoesNotThrow(() -> RecurrenceValidator.validateSettings(settings, reference),
            "23-hour window should be valid for daily recurrence (< 24-hour gap)");
    }

    /**
     * Test that a window GREATER than the interval fails with daily recurrence.
     * Daily recurrence with interval=1 has a 24-hour interval.
     * A 25-hour window should fail because it's greater than the interval.
     */
    @Test
    public void validateDailyRecurrence25HourWindowShouldFail() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        
        pattern.setType("Daily");
        pattern.setInterval(1);
        
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        
        // 25-hour window (in UTC): 
        // Start: 2024-03-30 10:00 +01:00 = 09:00 UTC
        // End:   2024-03-31 12:00 +02:00 = 10:00 UTC (25 hours later in UTC)
        settings.setStart("2024-03-30T10:00:00+01:00");
        settings.setEnd("2024-03-31T12:00:00+02:00"); // 25 hours later in UTC
        settings.setRecurrence(recurrence);
        
        final ZonedDateTime reference = ZonedDateTime.of(2024, 3, 31, 12, 0, 0, 0, ZoneOffset.UTC);
        
        assertThrows(IllegalArgumentException.class,
            () -> RecurrenceValidator.validateSettings(settings, reference),
            "25-hour window should fail for daily recurrence (greater than 24-hour interval)");
    }

    /**
     * Test Monday-Friday-Sunday pattern with 23.5-hour window.
     * Minimum gap is Monday-Friday (96 hours), so this should pass.
     */
    @Test
    public void validateMultipleDaysOfWeekWith23HourWindow() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Friday", "Sunday")); 
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(1);
        
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        
        // 23.5-hour window
        settings.setStart("2024-03-25T10:00:00+01:00"); // Monday
        settings.setEnd("2024-03-26T09:30:00+01:00"); // Tuesday, 23.5 hours later
        settings.setRecurrence(recurrence);
        
        // Reference on DST transition day
        final ZonedDateTime referenceDuringDST = ZonedDateTime.of(2024, 3, 31, 12, 0, 0, 0, ZoneOffset.UTC);
        
        // Minimum gap is Friday->Sunday (48 hours), so 23.5 hours should pass
        assertDoesNotThrow(() -> RecurrenceValidator.validateSettings(settings, referenceDuringDST),
            "23.5-hour window should be valid with Friday-Sunday 48-hour minimum gap");
    }

    /**
     * Test with consecutive days spanning DST transition.
     * Saturday-Sunday-Monday where Sunday is the DST transition day.
     */
    @Test
    public void validateConsecutiveDaysSpanningDSTTransition() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Saturday", "Sunday", "Monday"));
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(1);
        
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        
        // 23-hour window on Saturday
        settings.setStart("2024-03-30T10:00:00+01:00"); // Saturday
        settings.setEnd("2024-03-31T09:00:00+01:00"); // Sunday, 23 hours later
        settings.setRecurrence(recurrence);
        
        // Reference on the DST transition day itself
        final ZonedDateTime reference = ZonedDateTime.of(2024, 3, 31, 2, 30, 0, 0, 
            ZoneId.of("Europe/Paris"));
        
        // Minimum gap is Saturday-Sunday or Sunday-Monday (24 hours in UTC)
        // 23 hours < 24 hours, so should pass
        assertDoesNotThrow(() -> RecurrenceValidator.validateSettings(settings, reference),
            "23-hour window should be valid spanning DST transition with UTC calculation");
    }

    /**
     * Test validation with US timezone DST transition.
     * US DST spring forward is second Sunday of March (different from Europe).
     */
    @Test
    public void validate23HourWindowDuringUSDSTTransition() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Sunday", "Tuesday"));
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(1);
        
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        
        // 23-hour window on Sunday (US DST transition day: March 10, 2024)
        settings.setStart("2024-03-10T10:00:00-08:00"); // Sunday, PST
        settings.setEnd("2024-03-11T09:00:00-07:00"); // Monday, PDT (23 hours later in UTC)
        settings.setRecurrence(recurrence);
        
        // Reference during US DST transition
        final ZonedDateTime reference = ZonedDateTime.of(2024, 3, 10, 18, 0, 0, 0, ZoneOffset.UTC);
        
        // Sunday-Tuesday gap is 48 hours, 23-hour window should pass
        assertDoesNotThrow(() -> RecurrenceValidator.validateSettings(settings, reference),
            "23-hour window should be valid during US DST transition with UTC calculation");
    }

    /**
     * Test with multi-week interval and 23-hour window.
     * Even with larger gaps, the window must still be validated correctly.
     */
    @Test
    public void validateMultiWeekIntervalWith23HourWindow() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Friday"));
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(2); // Every 2 weeks
        
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        
        // 23-hour window
        settings.setStart("2024-03-04T10:00:00+01:00"); // Monday
        settings.setEnd("2024-03-05T09:00:00+01:00"); // Tuesday, 23 hours later
        settings.setRecurrence(recurrence);
        
        final ZonedDateTime reference = ZonedDateTime.of(2024, 3, 31, 12, 0, 0, 0, ZoneOffset.UTC);
        
        // Minimum gap is Monday-Friday (96 hours), so 23 hours should pass
        assertDoesNotThrow(() -> RecurrenceValidator.validateSettings(settings, reference),
            "23-hour window should be valid with multi-week interval");
    }
}
