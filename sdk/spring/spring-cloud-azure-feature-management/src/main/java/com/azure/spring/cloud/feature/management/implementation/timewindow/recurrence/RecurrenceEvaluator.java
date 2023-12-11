// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence;

import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowFilterSettings;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_END;
import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_RECURRENCE;
import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_START;

public class RecurrenceEvaluator {
    private String paramName;
    private String reason;
    private final TimeWindowFilterSettings settings;
    private final ZonedDateTime now;

    public RecurrenceEvaluator(TimeWindowFilterSettings settings, ZonedDateTime now) {
        this.settings = settings;
        this.now = now;
    }

    /**
     * Checks if a provided timestamp is within any recurring time window specified by the Recurrence section in the time window filter settings.
     * If the time window filter has an invalid recurrence setting, an exception will be thrown.
     * @return True if the time stamp is within any recurring time window, false otherwise.
     * */
    public boolean matchRecurrence() {
        if (!tryValidateSettings()) {
            throw new IllegalArgumentException(String.format(reason, paramName));
        }

        final ZonedDateTime previousOccurrence = tryGetPreviousOccurrence();
        if (previousOccurrence == null) {
            return false;
        }
        final ZonedDateTime occurrenceEndDate = previousOccurrence.plus(
            Duration.between(settings.getStart(), settings.getEnd()));
        return now.isBefore(occurrenceEndDate);
    }

    private boolean tryValidateSettings() {
        if (!tryValidateGeneralRequiredParameter()) {
            return false;
        }
        if (!tryValidateRecurrencePattern()) {
            return false;
        }
        return tryValidateRecurrenceRange();
    }

    private ZonedDateTime tryGetPreviousOccurrence() {
        ZonedDateTime start = settings.getStart();
        if (now.isBefore(start)) {
            return null;
        }

        final String patternType = settings.getRecurrence().getPattern().getType();
        OccurrenceInfo occurrenceInfo;
        if (RecurrenceConstants.DAILY.equalsIgnoreCase(patternType)) {
            occurrenceInfo = getDailyPreviousOccurrence();
        } else if (RecurrenceConstants.WEEKLY.equalsIgnoreCase(patternType)) {
            occurrenceInfo = getWeeklyPreviousOccurrence();
        } else if (RecurrenceConstants.ABSOLUTE_MONTHLY.equalsIgnoreCase(patternType)) {
            occurrenceInfo = getAbsoluteMonthlyPreviousOccurrence();
        } else if (RecurrenceConstants.RELATIVE_MONTHLY.equalsIgnoreCase(patternType)) {
            occurrenceInfo = getRelativeMonthlyPreviousOccurrence();
        } else if (RecurrenceConstants.ABSOLUTE_YEARLY.equalsIgnoreCase(patternType)) {
            occurrenceInfo = getAbsoluteYearlyPreviousOccurrence();
        } else if (RecurrenceConstants.RELATIVE_YEARLY.equalsIgnoreCase(patternType)) {
            occurrenceInfo = getRelativeYearlyPreviousOccurrence();
        } else {
            throw new IllegalArgumentException(String.format(RecurrenceConstants.UNRECOGNIZED_VALUE, RecurrenceConstants.RECURRENCE_PATTERN));
        }

        final RecurrenceRange range = settings.getRecurrence().getRange();
        final ZoneId zoneId = getRecurrenceTimeZoneId();
        if (RecurrenceConstants.END_DATE.equalsIgnoreCase(range.getType())) {
            final ZonedDateTime alignedPreviousOccurrence = occurrenceInfo.previousOccurrence.withZoneSameInstant(zoneId);
            final LocalDate alignedPreviousDate = alignedPreviousOccurrence.toLocalDate();
            if (alignedPreviousDate.isAfter(range.getEndDate())) {
                return null;
            }
        }
        if (RecurrenceConstants.NUMBERED.equalsIgnoreCase(range.getType())) {
            if (occurrenceInfo.numberOfOccurrences > range.getNumberOfRecurrences()) {
                return null;
            }
        }

        return occurrenceInfo.previousOccurrence;
    }

    /**
     * Find the closest previous recurrence occurrence before the provided time stamp according to the "Daily" recurrence pattern.
     * @return The return result contains two property, one is previousOccurrence, the other is numberOfOccurrences.
     * previousOccurrence: The closest previous occurrence.
     * numberOfOccurrences: The number of complete recurrence intervals which have occurred between the time and the recurrence start.
     * */
    private OccurrenceInfo getDailyPreviousOccurrence() {
        final ZonedDateTime start = settings.getStart();
        final int interval = settings.getRecurrence().getPattern().getInterval();
        final int numberOfOccurrences = (int) (Duration.between(start, now).getSeconds() / Duration.ofDays(interval).getSeconds());
        return new OccurrenceInfo(start.plusDays((long) numberOfOccurrences * interval), numberOfOccurrences + 1);
    }

    /**
     * Find the closest previous recurrence occurrence before the provided time stamp according to the "Weekly" recurrence pattern.
     * @return The return result contains two property, one is previousOccurrence, the other is numberOfOccurrences.
     * previousOccurrence: The closest previous occurrence.
     * numberOfOccurrences: The number of recurring days of week which have occurred between the time and the recurrence start.
     * */
    private OccurrenceInfo getWeeklyPreviousOccurrence() {
        final RecurrencePattern pattern = settings.getRecurrence().getPattern();
        final int interval = pattern.getInterval();
        final ZoneId zoneId = getRecurrenceTimeZoneId();
        final ZonedDateTime alignedStart = settings.getStart().withZoneSameInstant(zoneId);

        // calculate teh duration of first interval
        final int firstDayOfWeek = convertToWeekDayNumber(pattern.getFirstDayOfWeek());
        final int totalDaysOfFirstInterval = RecurrenceConstants.WEEK_DAY_NUMBER * (interval - 1) +
            remainingDaysOfWeek(getDayOfWeek(alignedStart), firstDayOfWeek);

        final LocalDate endDateOfFirstInterval = alignedStart.plusDays(totalDaysOfFirstInterval).toLocalDate();
        final Duration totalDurationOfFirstInterval = Duration.between(alignedStart, endDateOfFirstInterval.atStartOfDay(alignedStart.getZone()));

        ZonedDateTime previousOccurrence;
        int numberOfOccurrences = 0;
        final Duration timeGap = Duration.between(alignedStart, now);
        if (totalDurationOfFirstInterval.compareTo(timeGap) <= 0) {
            long numberOfInterval = timeGap.minus(totalDurationOfFirstInterval).toSeconds() /
                Duration.ofDays(interval * RecurrenceConstants.WEEK_DAY_NUMBER).toSeconds();
            previousOccurrence = alignedStart.plusDays(totalDaysOfFirstInterval +
                numberOfInterval * interval * RecurrenceConstants.WEEK_DAY_NUMBER);
            numberOfOccurrences += numberOfInterval * pattern.getDaysOfWeek().size();

            // count the number of occurrence in the first week
            ZonedDateTime dateTime = alignedStart;
            while (getDayOfWeek(dateTime) != firstDayOfWeek) {
                final ZonedDateTime tempDateTime = dateTime;
                if (pattern.getDaysOfWeek().stream().anyMatch(day ->
                    convertToWeekDayNumber(day) == getDayOfWeek(tempDateTime))) {
                    numberOfOccurrences += 1;
                }
                dateTime = dateTime.plusDays(1);
            }
        } else { // time is still within the first interval
            previousOccurrence = alignedStart;
        }

        ZonedDateTime loopedDateTime = previousOccurrence;
        final ZonedDateTime alignedTime = now.withZoneSameInstant(zoneId);
        while (!loopedDateTime.isAfter(alignedTime)) {
            if (!previousOccurrence.isEqual(loopedDateTime) && getDayOfWeek(loopedDateTime) == firstDayOfWeek) {   // Come to the next week
                break;
            }

            final ZonedDateTime tempDateTime = loopedDateTime;
            if (pattern.getDaysOfWeek().stream().anyMatch(day ->
                convertToWeekDayNumber(day) == getDayOfWeek(tempDateTime))) {
                previousOccurrence = loopedDateTime;
                numberOfOccurrences += 1;
            }
            loopedDateTime = loopedDateTime.plusDays(1);
        }
        return new OccurrenceInfo(previousOccurrence, numberOfOccurrences);
    }

    /**
     * Find the closest previous recurrence occurrence before the provided time stamp according to the "AbsoluteMonthly" recurrence pattern.
     * @return The return result contains two property, one is previousOccurrence, the other is numberOfOccurrences.
     * previousOccurrence: The closest previous occurrence.
     * numberOfOccurrences: The number of complete recurrence intervals which have occurred between the time and the recurrence start.
     *  */
    private OccurrenceInfo getAbsoluteMonthlyPreviousOccurrence() {
        final RecurrencePattern pattern = settings.getRecurrence().getPattern();
        final ZonedDateTime start = settings.getStart();
        final int interval = pattern.getInterval();

        final ZoneId zoneId = getRecurrenceTimeZoneId();
        final ZonedDateTime alignedStart = start.withZoneSameInstant(zoneId);
        final ZonedDateTime alignedTime = now.withZoneSameInstant(zoneId);

        int monthGap = (alignedTime.getYear() - alignedStart.getYear()) * 12 + alignedTime.getMonthValue() - alignedStart.getMonthValue();
        final Duration startDuration = Duration.between(alignedStart.toLocalDate().atStartOfDay(alignedStart.getZone()), alignedStart).plus(
            Duration.ofDays(alignedStart.getDayOfMonth()));
        final Duration timeDuration = Duration.between(alignedTime.toLocalDate().atStartOfDay(alignedTime.getZone()), alignedTime).plus(
            Duration.ofDays(alignedTime.getDayOfMonth()));
        if (timeDuration.compareTo(startDuration) < 0) {
            monthGap -= 1;
        }

        final int numberOfInterval = monthGap / interval;
        return new OccurrenceInfo(alignedStart.plusMonths(numberOfInterval * interval), numberOfInterval + 1);
    }

    /**
     * Find the closest previous recurrence occurrence before the provided time stamp according to the "RelativeMonthly" recurrence pattern.
     * @return The return result contains two property, one is previousOccurrence, the other is numberOfOccurrences.
     *  previousOccurrence: The closest previous occurrence.
     *  numberOfOccurrences: The number of complete recurrence intervals which have occurred between the time and the recurrence start.
     *  */
    private OccurrenceInfo getRelativeMonthlyPreviousOccurrence() {
        final ZoneId zoneId = getRecurrenceTimeZoneId();
        final ZonedDateTime alignedStart = settings.getStart().withZoneSameInstant(zoneId);
        final ZonedDateTime alignedTime = now.withZoneSameInstant(zoneId);

        final RecurrencePattern pattern = settings.getRecurrence().getPattern();
        final int interval = pattern.getInterval();

        int monthGap = (alignedTime.getYear() - alignedStart.getYear()) * 12 + alignedTime.getMonthValue() - alignedStart.getMonthValue();
        final Duration startDuration = Duration.between(alignedStart.toLocalDate().atStartOfDay(alignedStart.getZone()), alignedStart);
        if (!pattern.getDaysOfWeek().stream().anyMatch(day ->
            !alignedTime.isBefore(dayOfNthWeekInTheMonth(alignedTime, pattern.getIndex(), day).plus(startDuration)))) {
            // E.g. start: 2023.9.1 (the first Friday in 2023.9) and time: 2023.10.2 (the first Friday in 2023.10 is 2023.10.6)
            // Not a complete monthly interval
            alignedTime.plus(startDuration);
            monthGap -= 1;
        }

        final int numberOfInterval = monthGap / interval;
        final ZonedDateTime alignedPreviousOccurrenceMonth = alignedStart.plusMonths(numberOfInterval * interval);
        ZonedDateTime alignedPreviousOccurrence = null;
        // Find the first occurrence date matched the pattern
        // Only one day of week in the month will be matched
        for (String day: pattern.getDaysOfWeek()) {
            final ZonedDateTime occurrenceDate = dayOfNthWeekInTheMonth(alignedPreviousOccurrenceMonth, pattern.getIndex(), day).plus(startDuration);
            if (alignedPreviousOccurrence == null || alignedPreviousOccurrence.isAfter(occurrenceDate)) {
                alignedPreviousOccurrence = occurrenceDate;
            }
        }

        return new OccurrenceInfo(alignedPreviousOccurrence, numberOfInterval + 1);
    }

    /**
     * Find the closest previous recurrence occurrence before the provided time stamp according to the "AbsoluteYearly" recurrence pattern.
     * @return The return result contains two property, one is previousOccurrence, the other is numberOfOccurrences.
     * previousOccurrence: The closest previous occurrence.
     * numberOfOccurrences: The number of complete recurrence intervals which have occurred between the time and the recurrence start.
     *  */
    private OccurrenceInfo getAbsoluteYearlyPreviousOccurrence() {
        final ZoneId zoneId = getRecurrenceTimeZoneId();
        final ZonedDateTime alignedStart = settings.getStart().withZoneSameInstant(zoneId);
        final ZonedDateTime alignedTime = now.withZoneSameInstant(zoneId);

        int yearGap = alignedTime.getYear() - alignedStart.getYear();
        final Duration startDuration = Duration.between(alignedStart.toLocalDate().atStartOfDay(alignedStart.getZone()), alignedStart).plus(
            Duration.ofDays(alignedStart.getDayOfYear()));
        final Duration timeDuration = Duration.between(alignedTime.toLocalDate().atStartOfDay(alignedTime.getZone()), alignedTime).plus(
            Duration.ofDays(alignedTime.getDayOfYear()));
        if (timeDuration.compareTo(startDuration) < 0) {
            yearGap -= 1;
        }

        final int interval = settings.getRecurrence().getPattern().getInterval();
        final int numberOfInterval = yearGap / interval;
        return new OccurrenceInfo(alignedStart.plusYears(numberOfInterval * interval), numberOfInterval + 1);
    }

    /**
     * Find the closest previous recurrence occurrence before the provided time stamp according to the "RelativeYearly" recurrence pattern.
     * @return The return result contains two property, one is previousOccurrence, the other is numberOfOccurrences.
     * previousOccurrence: The closest previous occurrence.
     * numberOfOccurrences: The number of complete recurrence intervals which have occurred between the time and the recurrence start.
     *  */
    private OccurrenceInfo getRelativeYearlyPreviousOccurrence() {
        final RecurrencePattern pattern = settings.getRecurrence().getPattern();

        final ZoneId zoneId = getRecurrenceTimeZoneId();
        final ZonedDateTime alignedStart = settings.getStart().withZoneSameInstant(zoneId);
        final ZonedDateTime alignedTime = now.withZoneSameInstant(zoneId);

        int yearGap = alignedTime.getYear() - alignedStart.getYear();
        final Duration startDuration = Duration.between(alignedStart.toLocalDate().atStartOfDay(alignedStart.getZone()), alignedStart);
        if (alignedTime.getMonthValue() < alignedStart.getMonthValue()) {
            // E.g. start: 2023.9 and time: 2024.8
            // Not a complete yearly interval
            yearGap -= 1;
        } else if (alignedTime.getMonthValue() == alignedStart.getMonthValue() &&
            !pattern.getDaysOfWeek().stream().anyMatch(day ->
            !alignedTime.isBefore(dayOfNthWeekInTheMonth(alignedTime, pattern.getIndex(), day).plus(startDuration)))) {
            // E.g. start: 2023.9.1 (the first Friday in 2023.9) and time: 2024.9.2 (the first Friday in 2023.9 is 2024.9.6)
            // Not a complete yearly interval
            yearGap -= 1;
        }

        final int interval = pattern.getInterval();
        final int numberOfInterval = yearGap / interval;
        final ZonedDateTime alignedPreviousOccurrenceMonth = alignedStart.plusYears(numberOfInterval * interval);
        ZonedDateTime alignedPreviousOccurrence = null;

        // Find the first occurrence date matched the pattern
        // Only one day of week in the month will be matched
        for (String day: pattern.getDaysOfWeek()) {
            final ZonedDateTime occurrenceDate = dayOfNthWeekInTheMonth(alignedPreviousOccurrenceMonth,
                pattern.getIndex(), day).plus(startDuration);
            if (alignedPreviousOccurrence == null || alignedPreviousOccurrence.isAfter(occurrenceDate)) {
                alignedPreviousOccurrence = occurrenceDate;
            }
        }

        return new OccurrenceInfo(alignedPreviousOccurrence, numberOfInterval + 1);
    }

    private boolean tryValidateGeneralRequiredParameter() {
        final Recurrence recurrence = settings.getRecurrence();
        if (settings.getStart() == null) {
            paramName = TIME_WINDOW_FILTER_SETTING_START;
            reason = RecurrenceConstants.REQUIRED_PARAMETER;
            return false;
        }
        if (settings.getEnd() == null) {
            paramName = TIME_WINDOW_FILTER_SETTING_END;
            reason = RecurrenceConstants.REQUIRED_PARAMETER;
            return false;
        }
        if (recurrence.getPattern() == null) {
            paramName = String.format("%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN);
            reason = RecurrenceConstants.REQUIRED_PARAMETER;
            return false;
        }
        if (recurrence.getRange() == null) {
            paramName = String.format("%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_RANGE);
            reason = RecurrenceConstants.REQUIRED_PARAMETER;
            return false;
        }
        if (recurrence.getPattern().getType() == null) {
            paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
                RecurrenceConstants.RECURRENCE_PATTERN_TYPE);
            reason = RecurrenceConstants.REQUIRED_PARAMETER;
            return false;
        }
        if (recurrence.getRange().getType() == null) {
            paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_RANGE,
                RecurrenceConstants.RECURRENCE_RANGE_TYPE);
            reason = RecurrenceConstants.REQUIRED_PARAMETER;
            return false;
        }
        if (settings.getEnd().isBefore(settings.getStart())) {
            paramName = TIME_WINDOW_FILTER_SETTING_END;
            reason = RecurrenceConstants.OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    private boolean tryValidateRecurrencePattern() {
        if (!tryValidateInterval()) {
            return false;
        }

        final String patternType = settings.getRecurrence().getPattern().getType();
        if (RecurrenceConstants.DAILY.equalsIgnoreCase(patternType)) {
            return tryValidateDailyRecurrencePattern();
        } else if (RecurrenceConstants.WEEKLY.equalsIgnoreCase(patternType)) {
            return tryValidateWeeklyRecurrencePattern();
        } else if (RecurrenceConstants.ABSOLUTE_MONTHLY.equalsIgnoreCase(patternType)) {
            return tryValidateAbsoluteMonthlyRecurrencePattern();
        } else if (RecurrenceConstants.RELATIVE_MONTHLY.equalsIgnoreCase(patternType)) {
            return tryValidateRelativeMonthlyRecurrencePattern();
        } else if (RecurrenceConstants.ABSOLUTE_YEARLY.equalsIgnoreCase(patternType)) {
            return tryValidateAbsoluteYearlyRecurrencePattern();
        } else if (RecurrenceConstants.RELATIVE_YEARLY.equalsIgnoreCase(patternType)) {
            return tryValidateRelativeYearlyRecurrencePattern();
        } else {
            paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
                RecurrenceConstants.RECURRENCE_PATTERN_TYPE);
            reason = RecurrenceConstants.UNRECOGNIZED_VALUE;
            return false;
        }
    }

    private boolean tryValidateRecurrenceRange() {
        if (!tryValidateRecurrenceTimeZone()) {
            return false;
        }

        String rangeType = settings.getRecurrence().getRange().getType();
        if (RecurrenceConstants.NO_END.equalsIgnoreCase(rangeType)) {
            // No parameter is required
            return true;
        } else if (RecurrenceConstants.END_DATE.equalsIgnoreCase(rangeType)) {
            return tryValidateEndDate();
        } else if (RecurrenceConstants.NUMBERED.equalsIgnoreCase(rangeType)) {
            return tryValidateNumberOfOccurrences();
        }
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_RANGE,
            RecurrenceConstants.RECURRENCE_RANGE_TYPE);
        reason = RecurrenceConstants.UNRECOGNIZED_VALUE;
        return false;
    }

    private boolean tryValidateRecurrenceTimeZone() {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_RANGE,
            RecurrenceConstants.RECURRENCE_RANGE_RECURRENCE_TIME_ZONE);
        if (settings.getRecurrence().getRange().getRecurrenceTimeZone() != null &&
            tryParseTimeZone(settings.getRecurrence().getRange().getRecurrenceTimeZone()) == null) {
            reason = RecurrenceConstants.UNRECOGNIZED_VALUE;
            return false;
        }
        return true;
    }

    private boolean tryValidateEndDate() {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_RANGE,
            RecurrenceConstants.RECURRENCE_RANGE_EDN_DATE);
        if (settings.getRecurrence().getRange().getEndDate() == null) {
            reason = RecurrenceConstants.REQUIRED_PARAMETER;
            return false;
        }

        final ZonedDateTime start = settings.getStart();
        final ZonedDateTime alignedStart = start.withZoneSameInstant(getRecurrenceTimeZoneId());
        final LocalDate alignedStartDate = alignedStart.toLocalDate();
        if (settings.getRecurrence().getRange().getEndDate().isBefore(alignedStartDate)) {
            reason = RecurrenceConstants.OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    private boolean tryValidateNumberOfOccurrences() {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_RANGE,
            RecurrenceConstants.RECURRENCE_RANGE_NUMBER_OF_OCCURRENCES);
        if (settings.getRecurrence().getRange().getNumberOfRecurrences() == null) {
            reason = RecurrenceConstants.REQUIRED_PARAMETER;
            return false;
        }
        if (settings.getRecurrence().getRange().getNumberOfRecurrences() < 1) {
            reason = RecurrenceConstants.OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    private boolean tryValidateInterval() {
        if (settings.getRecurrence().getPattern().getInterval() <= 0) {
            paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
                RecurrenceConstants.RECURRENCE_PATTERN_INTERVAL);
            reason = RecurrenceConstants.OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    private boolean tryValidateDailyRecurrencePattern() {
        // No required parameter for "Daily" pattern and "Start" is always a valid first occurrence for "Daily" pattern.
        // Only need to check if time window validated
        final Duration intervalDuration = Duration.ofDays(settings.getRecurrence().getPattern().getInterval());
        return tryValidateTimeWindowDuration(settings, intervalDuration);
    }

    private boolean tryValidateWeeklyRecurrencePattern() {
        final RecurrencePattern pattern = settings.getRecurrence().getPattern();

        // Time window duration must be shorter than how frequently it occurs
        final Duration intervalDuration = Duration.ofDays((long) pattern.getInterval() * RecurrenceConstants.WEEK_DAY_NUMBER);
        final Duration timeWindowDuration = Duration.between(settings.getStart(), settings.getEnd());
        if (!tryValidateTimeWindowDuration(settings, intervalDuration)) {
            return false;
        }

        // Required parameters
        if (!tryValidateDaysOfWeek(settings)) {
            return false;
        }
        if (!tryValidateFirstDayOfWeek(settings)) {
            return false;
        }

        // Check whether "Start" is a valid first occurrence
        final ZonedDateTime alignedStart = settings.getStart().withZoneSameInstant(getRecurrenceTimeZoneId());
        if (pattern.getDaysOfWeek().stream().noneMatch((dayOfWeekStr) ->
            getDayOfWeek(alignedStart) == convertToWeekDayNumber(dayOfWeekStr))) {
            paramName = TIME_WINDOW_FILTER_SETTING_START;
            reason = RecurrenceConstants.NOT_MATCHED;
            return false;
        }

        // Check whether the time window duration is shorter than the minimum gap between days of week
        if (!isDurationCompliantWithDaysOfWeek(timeWindowDuration, pattern.getInterval(), pattern.getDaysOfWeek(), pattern.getFirstDayOfWeek())) {
            paramName = TIME_WINDOW_FILTER_SETTING_END;
            reason = RecurrenceConstants.OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    /**
     * Check whether the duration is shorter than the minimum gap between recurrence of days of week.
     * @param duration The duration of time window.
     * @param daysOfWeek The days of the week when the recurrence will occur.
     * @param firstDayOfWeek The first day of the week.
     * @return True if the duration is compliant with days of week, false otherwise.
     * */
    private boolean isDurationCompliantWithDaysOfWeek(Duration duration, int interval, List<String> daysOfWeek, String firstDayOfWeek) {
        if (daysOfWeek.size() == 1) {
            return true;
        }

        // Get the date of first day of the week
        final ZonedDateTime today = ZonedDateTime.now();
        final int offset = remainingDaysOfWeek(getDayOfWeek(today), convertToWeekDayNumber(firstDayOfWeek));
        final ZonedDateTime firstDateOfWeek = today.plusDays(offset);

        // Loop the whole week to get the min gap between the two consecutive recurrences
        // todo why need to initialize as first day of week?
        ZonedDateTime date = firstDateOfWeek;
        ZonedDateTime prevOccurrence = firstDateOfWeek;
        Duration minGap = Duration.ofDays(7);
        for (int i = 0; i < 6; i++) {
            date = date.plusDays(1);
            final ZonedDateTime finalDate = date;
            if (daysOfWeek.stream().anyMatch(dayOfWeek -> convertToWeekDayNumber(dayOfWeek) == getDayOfWeek(finalDate))) {
                final Duration currentGap = Duration.between(prevOccurrence, date);
                if (currentGap.compareTo(minGap) < 0) {
                    minGap = currentGap;
                }
                prevOccurrence = date;
            }
        }
        if (interval == 1) {
            // It may across weeks. Check the adjacent week
            date = date.plusDays(1);
            final Duration currentGap = Duration.between(prevOccurrence, date);
            if (currentGap.compareTo(minGap) < 0) {
                minGap = currentGap;
            }
        }
        return minGap.compareTo(duration) >= 0;
    }

    private int remainingDaysOfWeek(int today, int firstDayOfWeek) {
        int remainingDays = (today - firstDayOfWeek);
        if (remainingDays < 0) {
            return -remainingDays;
        } else {
            return RecurrenceConstants.WEEK_DAY_NUMBER - remainingDays;
        }
    }

    private boolean tryValidateAbsoluteMonthlyRecurrencePattern() {
        final RecurrencePattern pattern = settings.getRecurrence().getPattern();

        // Time window duration must be shorter than how frequently it occurs
        final Duration intervalDuration = Duration.ofDays((long) pattern.getInterval() * RecurrenceConstants.MIN_MONTH_DAY_NUMBER);
        if (!tryValidateTimeWindowDuration(settings, intervalDuration)) {
            return false;
        }

        // Required parameters
        if (!tryValidateDayOfMonth(settings)) {
            return false;
        }

        // Check whether "Start" is a valid first occurrence
        final ZonedDateTime alignedStart = settings.getStart().withZoneSameInstant(getRecurrenceTimeZoneId());
        if (alignedStart.getDayOfMonth() != pattern.getDayOfMonth()) {
            paramName = TIME_WINDOW_FILTER_SETTING_START;
            reason = RecurrenceConstants.NOT_MATCHED;
            return false;
        }

        return true;
    }

    private boolean tryValidateDayOfMonth(TimeWindowFilterSettings settings) {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN, RecurrenceConstants.RECURRENCE_PATTERN_DAY_OF_MONTH);
        if (settings.getRecurrence().getPattern().getDayOfMonth() == null) {
            reason = RecurrenceConstants.REQUIRED_PARAMETER;
            return false;
        }
        if (settings.getRecurrence().getPattern().getDayOfMonth() < 1 || settings.getRecurrence().getPattern().getDayOfMonth() > 31) {
            reason = RecurrenceConstants.OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    private boolean tryValidateRelativeMonthlyRecurrencePattern() {
        final RecurrencePattern pattern = settings.getRecurrence().getPattern();

        // Time window duration must be shorter than how frequently it occurs
        final Duration intervalDuration = Duration.ofDays((long) pattern.getInterval() * RecurrenceConstants.MIN_MONTH_DAY_NUMBER);
        if (!tryValidateTimeWindowDuration(settings, intervalDuration)) {
            return false;
        }

        // Required parameters
        if (!tryValidateIndex(settings)) {
            return false;
        }
        if (!tryValidateDaysOfWeek(settings)) {
            return false;
        }

        // Check whether "Start" is a valid first occurrence
        final ZonedDateTime alignedStart = settings.getStart().withZoneSameInstant(getRecurrenceTimeZoneId());
        if (pattern.getDaysOfWeek().stream().noneMatch((dayOfWeekStr) ->
            getDayOfWeek(alignedStart) == convertToWeekDayNumber(dayOfWeekStr))) {
            paramName = TIME_WINDOW_FILTER_SETTING_START;
            reason = RecurrenceConstants.NOT_MATCHED;
            return false;
        }
        return true;
    }

    private boolean tryValidateIndex(TimeWindowFilterSettings settings) {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
            RecurrenceConstants.RECURRENCE_PATTERN_INDEX);

        final String index = settings.getRecurrence().getPattern().getIndex();
        if (index == null) {
            reason = RecurrenceConstants.REQUIRED_PARAMETER;
            return false;
        }

        final String[] legalValues = {RecurrenceConstants.FIRST, RecurrenceConstants.SECOND, RecurrenceConstants.THIRD,
            RecurrenceConstants.FOURTH, RecurrenceConstants.LAST};
        if (Arrays.stream(legalValues).noneMatch(index::equalsIgnoreCase)) {
            reason = RecurrenceConstants.UNRECOGNIZED_VALUE;
            return false;
        }
        return true;
    }

    private boolean tryValidateAbsoluteYearlyRecurrencePattern() {
        final RecurrencePattern pattern = settings.getRecurrence().getPattern();

        // Time window duration must be shorter than how frequently it occurs
        final Duration intervalDuration = Duration.ofDays((long) pattern.getInterval() * RecurrenceConstants.MIN_YEAR_DAY_NUMBER);
        if (!tryValidateTimeWindowDuration(settings, intervalDuration)) {
            return false;
        }

        // Required parameters
        if (!tryValidateMonth(settings)) {
            return false;
        }
        if (!tryValidateDayOfMonth(settings)) {
            return false;
        }

        // Check whether "Start" is a valid first occurrence
        final ZonedDateTime alignedStart = settings.getStart().withZoneSameInstant(getRecurrenceTimeZoneId());
        if (alignedStart.getDayOfMonth() != pattern.getDayOfMonth() || alignedStart.getMonthValue() != pattern.getMonth()) {
            paramName = TIME_WINDOW_FILTER_SETTING_START;
            reason = RecurrenceConstants.NOT_MATCHED;
            return false;
        }

        return true;
    }

    private boolean tryValidateMonth(TimeWindowFilterSettings settings) {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
            RecurrenceConstants.RECURRENCE_PATTERN_MONTH);
        if (settings.getRecurrence().getPattern().getMonth() == null) {
            reason = RecurrenceConstants.REQUIRED_PARAMETER;
            return false;
        }
        if (settings.getRecurrence().getPattern().getMonth() < 1 || settings.getRecurrence().getPattern().getMonth() > 12) {
            reason = RecurrenceConstants.OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    private boolean tryValidateRelativeYearlyRecurrencePattern() {
        final RecurrencePattern pattern = settings.getRecurrence().getPattern();

        // Time window duration must be shorter than how frequently it occurs
        final Duration intervalDuration = Duration.ofDays((long) pattern.getInterval() * RecurrenceConstants.MIN_YEAR_DAY_NUMBER);
        if (!tryValidateTimeWindowDuration(settings, intervalDuration)) {
            return false;
        }

        // Required parameters
        if (!tryValidateMonth(settings)) {
            return false;
        }
        if (!tryValidateIndex(settings)) {
            return false;
        }
        if (!tryValidateDaysOfWeek(settings)) {
            return false;
        }

        // Check whether "Start" is a valid first occurrence
        final ZonedDateTime alignedStart = settings.getStart().withZoneSameInstant(getRecurrenceTimeZoneId());
        if (alignedStart.getMonthValue() != pattern.getMonth() ||
            pattern.getDaysOfWeek().stream().noneMatch(day ->
                alignedStart.isEqual(dayOfNthWeekInTheMonth(alignedStart, pattern.getIndex(), day)))) {
            paramName = TIME_WINDOW_FILTER_SETTING_START;
            reason = RecurrenceConstants.NOT_MATCHED;
            return false;
        }
        return true;
    }

    /**
     * Find the nth day of week in the month.
     * @param startDateTime start date time, specifies the month number
     * @param index Specifies on which instance of the allowed days specified in daysOfsWeek the event occurs, counted from the first instance in the month
     * @param dayOfWeek Specifies on which day of the week the event can occur
     * @return The data time of the day in nth week the month.
     * */
    private ZonedDateTime dayOfNthWeekInTheMonth(ZonedDateTime startDateTime, String index, String dayOfWeek) {
        ZonedDateTime dateTime = ZonedDateTime.of(startDateTime.getYear(), startDateTime.getMonthValue(), 1,
            startDateTime.getHour(), startDateTime.getMinute(), startDateTime.getSecond(), startDateTime.getNano(), startDateTime.getZone());

        // Find the first day of week in the month
        while (getDayOfWeek(dateTime) != convertToWeekDayNumber(dayOfWeek)) {
            dateTime = dateTime.plusDays(1);
        }

        final ZonedDateTime tempDateTime = dateTime.plusDays((long) RecurrenceConstants.WEEK_DAY_NUMBER * (convertIndexToNumber(index) - 1));
        if (index.equalsIgnoreCase(RecurrenceConstants.LAST) && tempDateTime.getMonthValue() != startDateTime.getMonthValue()) {
            // There is no the 5th week in the month
            // Add 3 weeks to reach the fourth week in the month
            dateTime = dateTime.plusDays(RecurrenceConstants.WEEK_DAY_NUMBER * 3);
        } else {
            dateTime = dateTime.plusDays((long) RecurrenceConstants.WEEK_DAY_NUMBER * (convertIndexToNumber(index) - 1));
        }
        return dateTime;
    }

    private boolean tryValidateDaysOfWeek(TimeWindowFilterSettings settings) {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
            RecurrenceConstants.RECURRENCE_PATTERN_DAYS_OF_WEEK);
        final List<String> daysOfWeek = settings.getRecurrence().getPattern().getDaysOfWeek();
        if (daysOfWeek == null || daysOfWeek.size() == 0) {
            reason = RecurrenceConstants.REQUIRED_PARAMETER;
            return false;
        }

        final String[] legalValues = {RecurrenceConstants.MONDAY, RecurrenceConstants.TUESDAY, RecurrenceConstants.WEDNESDAY,
            RecurrenceConstants.THURSDAY, RecurrenceConstants.FRIDAY, RecurrenceConstants.SATURDAY, RecurrenceConstants.SUNDAY};
        for (String dayOfWeek: daysOfWeek) {
            if (Arrays.stream(legalValues).noneMatch(dayOfWeek::equalsIgnoreCase)) {
                reason = RecurrenceConstants.UNRECOGNIZED_VALUE;
                return false;
            }
        }
        return true;
    }

    private boolean tryValidateFirstDayOfWeek(TimeWindowFilterSettings settings) {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
            RecurrenceConstants.RECURRENCE_PATTERN_FIRST_DAY_OF_WEEK);
        final String firstDayOfWeek = settings.getRecurrence().getPattern().getFirstDayOfWeek();
        if (firstDayOfWeek == null) {
            reason = RecurrenceConstants.REQUIRED_PARAMETER;
            return false;
        }
        final String[] legalValues = {RecurrenceConstants.MONDAY, RecurrenceConstants.TUESDAY, RecurrenceConstants.WEDNESDAY,
            RecurrenceConstants.THURSDAY, RecurrenceConstants.FRIDAY, RecurrenceConstants.SATURDAY, RecurrenceConstants.SUNDAY};
        if (Arrays.stream(legalValues).noneMatch(firstDayOfWeek::equalsIgnoreCase)) {
            reason = RecurrenceConstants.UNRECOGNIZED_VALUE;
            return false;
        }
        return true;
    }

    /**
     * Validate if time window duration is shorter than how frequently it occurs
     * */
    private boolean tryValidateTimeWindowDuration(TimeWindowFilterSettings settings, Duration intervalDuration) {
        final Duration timeWindowDuration = Duration.between(settings.getStart(), settings.getEnd());
        if (timeWindowDuration.compareTo(intervalDuration) > 0)  {
            paramName = TIME_WINDOW_FILTER_SETTING_END;
            reason = RecurrenceConstants.OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    private ZoneId getRecurrenceTimeZoneId() {
        ZoneId timeZoneId = settings.getStart().getZone();
        final ZoneId rangeZoneId = tryParseTimeZone(settings.getRecurrence().getRange().getRecurrenceTimeZone());
        if (rangeZoneId != null) {
            timeZoneId = rangeZoneId;
        }
        return timeZoneId;
    }

    private ZoneId tryParseTimeZone(String timeZoneStr) {
        if (timeZoneStr == null) {
            return null;
        }
        if (!timeZoneStr.startsWith("UTC+") && !timeZoneStr.startsWith("UTC-")) {
            return null;
        }
        try {
            return ZoneOffset.of(timeZoneStr.replace("UTC", ""));
        } catch (final DateTimeException e) {
            return null;
        }
    }

    private int convertToWeekDayNumber(String str) {
        final String strLowerCase = str.toLowerCase();
        switch (strLowerCase) {
            case RecurrenceConstants.SUNDAY:
                return 0;
            case RecurrenceConstants.MONDAY:
                return 1;
            case RecurrenceConstants.TUESDAY:
                return 2;
            case RecurrenceConstants.WEDNESDAY:
                return 3;
            case RecurrenceConstants.THURSDAY:
                return 4;
            case RecurrenceConstants.FRIDAY:
                return 5;
            case RecurrenceConstants.SATURDAY:
                return 6;
            default:
                throw new IllegalArgumentException("Parameter DaysOfWeek not support " + str);
        }
    }

    private int convertIndexToNumber(String str) {
        final String strLowerCase = str.toLowerCase();
        switch (strLowerCase) {
            case RecurrenceConstants.FIRST:
                return 1;
            case RecurrenceConstants.SECOND:
                return 2;
            case RecurrenceConstants.THIRD:
                return 3;
            case RecurrenceConstants.FOURTH:
                return 4;
            case RecurrenceConstants.LAST:
                return 5;
            default:
                throw new IllegalArgumentException("Parameter Index not support " + str);
        }
    }

    private int getDayOfWeek(ZonedDateTime dateTime) {
        return dateTime.getDayOfWeek().getValue() % 7;
    }

    private class OccurrenceInfo {
        private ZonedDateTime previousOccurrence = null;
        private int numberOfOccurrences;
        public OccurrenceInfo(ZonedDateTime dateTime, int num) {
            this.previousOccurrence = dateTime;
            this.numberOfOccurrences = num;
        }
    }
}
