// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.filters.recurrence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.azure.spring.cloud.feature.management.implementation.models.Recurrence;
import com.azure.spring.cloud.feature.management.implementation.models.RecurrencePattern;
import com.azure.spring.cloud.feature.management.implementation.models.RecurrenceRange;
import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowFilterSettings;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceEvaluator;

/**
 * Tests for Daylight Saving Time (DST) handling in RecurrenceEvaluator.
 * These tests verify that recurrence evaluation correctly handles timezone transitions
 * when comparing fixed offset zones with region-based zones.
 */
public class RecurrenceEvaluatorDSTTest {

    /**
     * Test weekly recurrence across DST transition (Spring forward).
     * Verifies that when a start time is defined with a fixed offset (e.g., +01:00)
     * and the current time uses a region zone (e.g., Europe/Paris),
     * the evaluator correctly identifies they represent the same geographic location
     * and handles the DST transition properly.
     */
    @Test
    public void weeklyRecurrenceSpringForwardDSTTransition() {
        // Setup: Weekly recurrence starting before DST transition
        // In Europe/Paris, DST transition happens last Sunday of March (2:00 AM -> 3:00 AM)
        // Start: 2024-03-15 10:00 +01:00 (before DST, standard time)
        // Now: 2024-04-05 10:00 +02:00 (after DST, daylight time)
        
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
        
        // Start time with fixed offset (before DST)
        settings.setStart("2024-03-15T10:00:00+01:00"); // Friday
        settings.setEnd("2024-03-15T12:00:00+01:00");
        settings.setRecurrence(recurrence);
        
        // Current time with region zone (after DST)
        final ZonedDateTime now = ZonedDateTime.of(2024, 4, 5, 10, 30, 0, 0, 
            ZoneId.of("Europe/Paris")); // Friday, during time window
        
        // Should match because Friday is a valid recurrence day
        assertTrue(RecurrenceEvaluator.isMatch(settings, now), 
            "Should match weekly recurrence on Friday after DST transition");
    }

    /**
     * Test weekly recurrence across DST transition (Fall back).
     * Verifies handling when clocks fall back (e.g., from +02:00 to +01:00).
     */
    @Test
    public void weeklyRecurrenceFallBackDSTTransition() {
        // Setup: Weekly recurrence starting before DST fall back
        // In Europe/Paris, DST fall back happens last Sunday of October (3:00 AM -> 2:00 AM)
        // Start: 2024-10-18 10:00 +02:00 (before fall back, daylight time)
        // Now: 2024-11-01 10:00 +01:00 (after fall back, standard time)
        
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
        
        // Start time with fixed offset (before fall back)
        settings.setStart("2024-10-18T10:00:00+02:00"); // Friday
        settings.setEnd("2024-10-18T12:00:00+02:00");
        settings.setRecurrence(recurrence);
        
        // Current time with region zone (after fall back)
        final ZonedDateTime now = ZonedDateTime.of(2024, 11, 1, 10, 30, 0, 0, 
            ZoneId.of("Europe/Paris")); // Friday, during time window
        
        assertTrue(RecurrenceEvaluator.isMatch(settings, now), 
            "Should match weekly recurrence on Friday after DST fall back");
    }

    /**
     * Test that recurrence evaluation works correctly when both start and now
     * use fixed offsets (no DST conversion needed).
     */
    @Test
    public void weeklyRecurrenceBothFixedOffsets() {
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
        
        // Both use fixed offsets
        settings.setStart("2024-03-01T10:00:00+01:00"); // Friday
        settings.setEnd("2024-03-01T12:00:00+01:00");
        settings.setRecurrence(recurrence);
        
        final ZonedDateTime now = ZonedDateTime.of(2024, 3, 8, 10, 30, 0, 0, 
            ZoneOffset.ofHours(1)); // Friday
        
        assertTrue(RecurrenceEvaluator.isMatch(settings, now), 
            "Should match when both use fixed offsets");
    }

    /**
     * Test that recurrence evaluation works correctly when both start and now
     * use region zones (DST-aware zones).
     */
    @Test
    public void weeklyRecurrenceBothRegionZones() {
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
        
        // Both use region zones
        ZonedDateTime startTime = ZonedDateTime.of(2024, 3, 1, 10, 0, 0, 0, 
            ZoneId.of("Europe/Paris")); // Friday
        ZonedDateTime endTime = ZonedDateTime.of(2024, 3, 1, 12, 0, 0, 0, 
            ZoneId.of("Europe/Paris"));
        
        settings.setStart(startTime.toString());
        settings.setEnd(endTime.toString());
        settings.setRecurrence(recurrence);
        
        final ZonedDateTime now = ZonedDateTime.of(2024, 3, 8, 10, 30, 0, 0, 
            ZoneId.of("Europe/Paris")); // Friday
        
        assertTrue(RecurrenceEvaluator.isMatch(settings, now), 
            "Should match when both use region zones");
    }

    /**
     * Test weekly recurrence with different geographic locations.
     * When fixed offset doesn't match the region zone's offset at start time,
     * they represent different geographic locations and should not be converted.
     */
    @Test
    public void weeklyRecurrenceDifferentGeographicLocations() {
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
        
        // Start with offset that doesn't match Europe/Paris
        settings.setStart("2024-03-01T10:00:00+05:00"); // Friday, Asia timezone
        settings.setEnd("2024-03-01T12:00:00+05:00");
        settings.setRecurrence(recurrence);
        
        // Now in Europe/Paris (offset +01:00 at this time)
        final ZonedDateTime now = ZonedDateTime.of(2024, 3, 8, 10, 30, 0, 0, 
            ZoneId.of("Europe/Paris")); // Friday, 10:30 local time
        
        // When offsets don't match (+05:00 vs +01:00), they represent different geographic locations
        // No conversion should happen. The evaluator will compare times as-is.
        // Start: 2024-03-01 10:00 +05:00 = 2024-03-01 05:00 UTC
        // Now:   2024-03-08 10:30 +01:00 = 2024-03-08 09:30 UTC
        // The recurrence day (Friday) matches, and time window should match
        assertEquals(false, RecurrenceEvaluator.isMatch(settings, now),
            "Should not match due to different geographic locations with different time windows");
    }

    /**
     * Test weekly recurrence on the exact day of DST transition.
     * This tests the edge case where 'now' is during the DST transition day.
     */
    @Test
    public void weeklyRecurrenceOnDSTTransitionDay() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Sunday", "Wednesday"));
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(1);
        
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        
        // Start before DST
        settings.setStart("2024-03-20T10:00:00+01:00"); // Wednesday
        settings.setEnd("2024-03-20T12:00:00+01:00");
        settings.setRecurrence(recurrence);
        
        // Now on DST transition day (last Sunday of March 2024)
        // In Europe/Paris, 2024-03-31 is the DST transition day
        final ZonedDateTime now = ZonedDateTime.of(2024, 3, 31, 10, 30, 0, 0, 
            ZoneId.of("Europe/Paris")); // Sunday
        
        assertTrue(RecurrenceEvaluator.isMatch(settings, now), 
            "Should match on DST transition day");
    }

    /**
     * Test weekly recurrence with multiple intervals across DST.
     * Verifies that multi-week intervals work correctly across DST boundaries.
     */
    @Test
    public void weeklyRecurrenceMultiIntervalAcrossDST() {
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
        
        // Start in February (before DST)
        settings.setStart("2024-02-16T10:00:00+01:00"); // Friday
        settings.setEnd("2024-02-16T12:00:00+01:00");
        settings.setRecurrence(recurrence);
        
        // Now in April (after DST), on a recurrence week
        final ZonedDateTime now = ZonedDateTime.of(2024, 4, 12, 10, 30, 0, 0, 
            ZoneId.of("Europe/Paris")); // Friday, 8 weeks later (4th occurrence)
        
        assertTrue(RecurrenceEvaluator.isMatch(settings, now), 
            "Should match multi-interval recurrence across DST");
    }

    /**
     * Test weekly recurrence with numbered range across DST.
     * Ensures that occurrence counting works correctly across DST transitions.
     */
    @Test
    public void weeklyRecurrenceNumberedRangeAcrossDST() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Friday"));
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(1);
        
        range.setType("Numbered");
        range.setNumberOfOccurrences(15); // 15 occurrences
        
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        
        // Start before DST - 2024-03-08 is a Friday
        // Occurrences: 1:Fri 3/8, 2:Mon 3/11, 3:Fri 3/15, 4:Mon 3/18, 5:Fri 3/22, 6:Mon 3/25, 7:Fri 3/29, 
        // 8:Mon 4/1 (after DST on 3/31), 9:Fri 4/5, 10:Mon 4/8, 11:Fri 4/12, 12:Mon 4/15...
        settings.setStart("2024-03-08T10:00:00+01:00"); // Friday
        settings.setEnd("2024-03-08T12:00:00+01:00");
        settings.setRecurrence(recurrence);
        
        // Now after DST - 2024-04-05 is Friday, should be 9th occurrence (within range)
        final ZonedDateTime now = ZonedDateTime.of(2024, 4, 5, 10, 30, 0, 0, 
            ZoneId.of("Europe/Paris")); // Friday
        
        assertTrue(RecurrenceEvaluator.isMatch(settings, now), 
            "Should match numbered occurrence across DST");
        
        // Test after the numbered range ends - 2024-04-22 is Monday, would be 16th occurrence (beyond range)
        final ZonedDateTime afterRange = ZonedDateTime.of(2024, 4, 22, 10, 30, 0, 0, 
            ZoneId.of("Europe/Paris")); // Monday
        
        assertEquals(false, RecurrenceEvaluator.isMatch(settings, afterRange), 
            "Should not match after numbered range ends");
    }

    /**
     * Test weekly recurrence with end date across DST.
     * Verifies that end date comparisons work correctly across DST transitions.
     */
    @Test
    public void weeklyRecurrenceEndDateAcrossDST() {
        final TimeWindowFilterSettings settings = new TimeWindowFilterSettings();
        final RecurrencePattern pattern = new RecurrencePattern();
        final RecurrenceRange range = new RecurrenceRange();
        final Recurrence recurrence = new Recurrence();
        
        pattern.setType("Weekly");
        pattern.setDaysOfWeek(List.of("Monday", "Wednesday", "Friday"));
        pattern.setFirstDayOfWeek("Monday");
        pattern.setInterval(1);
        
        range.setType("EndDate");
        range.setEndDate("2024-04-10T23:59:59+02:00"); // After DST
        
        recurrence.setRange(range);
        recurrence.setPattern(pattern);
        
        // Start before DST
        settings.setStart("2024-03-15T10:00:00+01:00"); // Friday
        settings.setEnd("2024-03-15T12:00:00+01:00");
        settings.setRecurrence(recurrence);
        
        // Now after DST, before end date
        final ZonedDateTime now = ZonedDateTime.of(2024, 4, 5, 10, 30, 0, 0, 
            ZoneId.of("Europe/Paris")); // Friday
        
        assertTrue(RecurrenceEvaluator.isMatch(settings, now), 
            "Should match before end date across DST");
        
        // Test after end date
        final ZonedDateTime afterEnd = ZonedDateTime.of(2024, 4, 12, 10, 30, 0, 0, 
            ZoneId.of("Europe/Paris")); // Friday, after end date
        
        assertEquals(false, RecurrenceEvaluator.isMatch(settings, afterEnd), 
            "Should not match after end date");
    }

    /**
     * Test with US timezone (different DST transition dates than Europe).
     * US DST: Second Sunday of March (2:00 AM -> 3:00 AM)
     */
    @Test
    public void weeklyRecurrenceUSDSTTransition() {
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
        
        // Start before US DST (PST: -08:00)
        settings.setStart("2024-03-01T10:00:00-08:00"); // Friday
        settings.setEnd("2024-03-01T12:00:00-08:00");
        settings.setRecurrence(recurrence);
        
        // Now after US DST (PDT: -07:00)
        final ZonedDateTime now = ZonedDateTime.of(2024, 3, 15, 10, 30, 0, 0, 
            ZoneId.of("America/Los_Angeles")); // Friday, after DST
        
        assertTrue(RecurrenceEvaluator.isMatch(settings, now), 
            "Should match weekly recurrence across US DST transition");
    }

    /**
     * Test that the DST conversion only happens when start uses fixed offset
     * and now uses region zone, not the other way around.
     */
    @Test
    public void weeklyRecurrenceNoConversionWhenStartIsRegionZone() {
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
        
        // Start with region zone
        ZonedDateTime startTime = ZonedDateTime.of(2024, 3, 1, 10, 0, 0, 0, 
            ZoneId.of("Europe/Paris"));
        ZonedDateTime endTime = ZonedDateTime.of(2024, 3, 1, 12, 0, 0, 0, 
            ZoneId.of("Europe/Paris"));
        
        settings.setStart(startTime.toString());
        settings.setEnd(endTime.toString());
        settings.setRecurrence(recurrence);
        
        // Now with fixed offset
        final ZonedDateTime now = ZonedDateTime.of(2024, 4, 5, 10, 30, 0, 0, 
            ZoneOffset.ofHours(2)); // Friday
        
        // The DST conversion logic only applies when start is fixed offset and now is region zone.
        // In this reverse case (start is region zone, now is fixed offset), no conversion happens.
        // Start: 2024-03-01 10:00 Europe/Paris (+01:00) = Friday
        // Now:   2024-04-05 10:30 +02:00 = Friday (matches recurring day)
        // Time window: 10:00-12:00, Now: 10:30 (within window)
        assertTrue(RecurrenceEvaluator.isMatch(settings, now),
            "Should match - region zone start with fixed offset now should evaluate correctly");
    }
}
