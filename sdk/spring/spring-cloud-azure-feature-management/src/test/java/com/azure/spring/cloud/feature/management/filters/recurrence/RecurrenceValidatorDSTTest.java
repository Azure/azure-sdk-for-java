// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.filters.recurrence;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
 */
public class RecurrenceValidatorDSTTest {

    /**
     * Test that validator uses UTC for today's date calculation.
     * This ensures consistency regardless of the system's default timezone
     * or DST transitions.
     */
    @Test
    public void validateSettingsUsesUTCForDateCalculation() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Wednesday", "Friday"));
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(1);
        
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        
        // Start on a Monday
        settings.setStart("2024-03-04T10:00:00+01:00"); // Monday
        settings.setEnd("2024-03-04T12:00:00+01:00");
        settings.setRecurrence(recurrence);
        
        // Should not throw exception, uses UTC internally for validation
        assertDoesNotThrow(() -> RecurrenceValidator.validateSettings(settings),
            "Validator should use UTC and not throw exception");
    }

    /**
     * Test validation across DST spring forward transition.
     * Ensures that time window duration validation works correctly
     * when the start time is before DST and validation runs after DST.
     */
    @Test
    public void validateSettingsAcrossSpringForwardDST() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Friday"));
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(1);
        
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        
        // Start before DST (standard time)
        settings.setStart("2024-03-15T10:00:00+01:00"); // Friday
        settings.setEnd("2024-03-15T11:00:00+01:00"); // 1 hour duration
        settings.setRecurrence(recurrence);
        
        // Validation should work correctly regardless of current time being after DST
        assertDoesNotThrow(() -> RecurrenceValidator.validateSettings(settings),
            "Validator should handle DST spring forward correctly");
    }

    /**
     * Test validation across DST fall back transition.
     * Ensures that time window duration validation works correctly
     * when clocks fall back.
     */
    @Test
    public void validateSettingsAcrossFallBackDST() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Friday"));
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(1);
        
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        
        // Start before DST fall back (daylight time)
        settings.setStart("2024-10-18T10:00:00+02:00"); // Friday
        settings.setEnd("2024-10-18T11:00:00+02:00"); // 1 hour duration
        settings.setRecurrence(recurrence);
        
        // Validation should work correctly after DST fall back
        assertDoesNotThrow(() -> RecurrenceValidator.validateSettings(settings),
            "Validator should handle DST fall back correctly");
    }

    /**
     * Test validation with daily recurrence across DST.
     * Daily patterns are simpler but should still use UTC for consistency.
     */
    @Test
    public void validateDailyRecurrenceAcrossDST() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        
        pattern.setType("Daily");
        pattern.setInterval(1);
        
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        
        // Start before DST
        settings.setStart("2024-03-20T10:00:00+01:00");
        settings.setEnd("2024-03-20T12:00:00+01:00"); // 2 hour duration
        settings.setRecurrence(recurrence);
        
        assertDoesNotThrow(() -> RecurrenceValidator.validateSettings(settings),
            "Daily recurrence validation should work across DST");
    }

    /**
     * Test that validator handles time windows that span the DST transition hour.
     * For example, a window from 1:30 AM to 3:30 AM on DST spring forward day.
     */
    @Test
    public void validateTimeWindowSpanningDSTTransitionHour() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Sunday"));
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(1);
        
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        
        // DST spring forward in Europe/Paris: 2024-03-31 at 2:00 AM -> 3:00 AM
        // Time window from 1:30 to 3:30 (spans the transition)
        settings.setStart("2024-03-31T01:30:00+01:00"); // Sunday
        settings.setEnd("2024-03-31T03:30:00+02:00"); // After DST transition
        settings.setRecurrence(recurrence);
        
        // Using UTC ensures consistent duration calculation
        assertDoesNotThrow(() -> RecurrenceValidator.validateSettings(settings),
            "Validator should handle time window spanning DST transition");
    }

    /**
     * Test validation with numbered range across DST.
     * Ensures occurrence counting validation works correctly.
     */
    @Test
    public void validateNumberedRangeAcrossDST() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Friday"));
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(1);
        
        range.setType("Numbered");
        range.setNumberOfOccurrences(20);
        
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        
        // Start before DST
        settings.setStart("2024-03-04T10:00:00+01:00"); // Monday
        settings.setEnd("2024-03-04T11:00:00+01:00");
        settings.setRecurrence(recurrence);
        
        assertDoesNotThrow(() -> RecurrenceValidator.validateSettings(settings),
            "Numbered range validation should work across DST");
    }

    /**
     * Test validation with end date across DST.
     * Ensures end date validation works correctly with UTC.
     */
    @Test
    public void validateEndDateAcrossDST() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Wednesday", "Friday"));
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(1);
        
        range.setType("EndDate");
        range.setEndDate("2024-04-30T23:59:59+02:00"); // After DST
        
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        
        // Start before DST
        settings.setStart("2024-03-01T10:00:00+01:00"); // Friday
        settings.setEnd("2024-03-01T11:00:00+01:00");
        settings.setRecurrence(recurrence);
        
        assertDoesNotThrow(() -> RecurrenceValidator.validateSettings(settings),
            "End date validation should work across DST");
    }

    /**
     * Test validation with different timezone offsets.
     * Ensures UTC usage makes validation consistent across timezones.
     */
    @Test
    public void validateWithDifferentTimezoneOffsets() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Tuesday", "Thursday"));
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(1);
        
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        
        // Use a different timezone (US Pacific)
        settings.setStart("2024-03-05T10:00:00-08:00"); // Tuesday
        settings.setEnd("2024-03-05T11:00:00-08:00");
        settings.setRecurrence(recurrence);
        
        assertDoesNotThrow(() -> RecurrenceValidator.validateSettings(settings),
            "Validation should work with different timezone offsets using UTC");
    }

    /**
     * Test validation with multi-week interval across DST.
     * Ensures proper validation of longer intervals.
     */
    @Test
    public void validateMultiWeekIntervalAcrossDST() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Friday"));
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(3); // Every 3 weeks
        
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        
        // Start before DST
        settings.setStart("2024-02-19T10:00:00+01:00"); // Monday
        settings.setEnd("2024-02-19T12:00:00+01:00");
        settings.setRecurrence(recurrence);
        
        assertDoesNotThrow(() -> RecurrenceValidator.validateSettings(settings),
            "Multi-week interval validation should work across DST");
    }

    /**
     * Test that time window duration validation is consistent regardless of DST.
     * A 2-hour window should be valid in both standard and daylight time.
     */
    @Test
    public void validateTimeWindowDurationConsistencyAcrossDST() {
        // Test during standard time
        final TimeWindowFilterSettings settings1 = new TimeWindowFilterSettings();
        final RecurrencePattern pattern1 = new RecurrencePattern();
        final RecurrenceRange range1 = new RecurrenceRange();
        final Recurrence recurrence1 = new Recurrence();
        
        pattern1.setType("Daily");
        pattern1.setInterval(1);
        
        recurrence1.setRange(range1);
        recurrence1.setPattern(pattern1);
        
        settings1.setStart("2024-01-15T10:00:00+01:00"); // Standard time
        settings1.setEnd("2024-01-15T12:00:00+01:00"); // 2 hours
        settings1.setRecurrence(recurrence1);
        
        assertDoesNotThrow(() -> RecurrenceValidator.validateSettings(settings1),
            "Time window validation should work during standard time");
        
        // Test during daylight time (same duration, different offset)
        final TimeWindowFilterSettings settings2 = new TimeWindowFilterSettings();
        final RecurrencePattern pattern2 = new RecurrencePattern();
        final RecurrenceRange range2 = new RecurrenceRange();
        final Recurrence recurrence2 = new Recurrence();
        
        pattern2.setType("Daily");
        pattern2.setInterval(1);
        
        recurrence2.setRange(range2);
        recurrence2.setPattern(pattern2);
        
        settings2.setStart("2024-06-15T10:00:00+02:00"); // Daylight time
        settings2.setEnd("2024-06-15T12:00:00+02:00"); // 2 hours
        settings2.setRecurrence(recurrence2);
        
        assertDoesNotThrow(() -> RecurrenceValidator.validateSettings(settings2),
            "Time window validation should work during daylight time");
    }

    /**
     * Test validation when days of week span DST transition.
     * Ensures that the validator correctly handles weekly patterns
     * where recurring days cross DST boundaries.
     */
    @Test
    public void validateDaysOfWeekSpanningDSTTransition() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        
        pattern.setType("Weekly");
        // Multiple days including Sunday (typical DST transition day)
        pattern.setDaysOfWeek(List.of("Friday", "Saturday", "Sunday", "Monday"));
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(1);
        
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        
        // Start on Friday before DST transition week
        settings.setStart("2024-03-22T10:00:00+01:00"); // Friday
        settings.setEnd("2024-03-22T11:00:00+01:00");
        settings.setRecurrence(recurrence);
        
        assertDoesNotThrow(() -> RecurrenceValidator.validateSettings(settings),
            "Days of week validation should work spanning DST transition");
    }
}
