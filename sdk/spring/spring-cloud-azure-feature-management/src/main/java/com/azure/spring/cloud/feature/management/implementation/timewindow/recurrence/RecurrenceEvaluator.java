// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence;

import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowFilterSettings;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_END;
import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_RECURRENCE;
import static com.azure.spring.cloud.feature.management.models.FilterParameters.TIME_WINDOW_FILTER_SETTING_START;

public class RecurrenceEvaluator {
    // Error Message
    public static final String OUT_OF_RANGE = "The value of parameter %s is out of the accepted range.";
    public static final String UNRECOGNIZED_VALUE = "The value of parameter %s is unrecognizable.";
    public static final String REQUIRED_PARAMETER = "Value cannot be null for required parameter: %s";
    public static final String NOT_MATCHED = "%s date is not a valid first occurrence.";
    private static String paramName;
    private static String reason;

    /**
     * Checks if a provided timestamp is within any recurring time window specified by the Recurrence section in the time window filter settings.
     * If the time window filter has an invalid recurrence setting, an exception will be thrown.
     * @param now time stamp of current time.
     * @param settings the parameters of time window filter
     * @return True if the time stamp is within any recurring time window, false otherwise.
     * */
    public static boolean matchRecurrence(ZonedDateTime now, TimeWindowFilterSettings settings) {
        if (!tryValidateSettings(settings)) {
            throw new IllegalArgumentException(String.format(reason, paramName));
        }

        final ZonedDateTime previousOccurrence = tryGetPreviousOccurrence(now, settings);
        if (previousOccurrence == null) {
            return false;
        }
        final ZonedDateTime occurrenceEndDate = previousOccurrence.plus(
            Duration.between(settings.getStart(), settings.getEnd()));
        return now.isBefore(occurrenceEndDate);
    }

    private static boolean tryValidateSettings(TimeWindowFilterSettings settings) {
        if (!tryValidateGeneralRequiredParameter(settings)) {
            return false;
        }
        if (!tryValidateRecurrencePattern(settings)) {
            return false;
        }
        return tryValidateRecurrenceRange(settings);
    }

    private static ZonedDateTime tryGetPreviousOccurrence(ZonedDateTime now, TimeWindowFilterSettings settings) {
        ZonedDateTime start = settings.getStart();
        if (now.isBefore(start)) {
            return null;
        }

        final String patternType = settings.getRecurrence().getPattern().getType();
        OccurrenceInfo occurrenceInfo;
        if (RecurrenceConstants.DAILY.equalsIgnoreCase(patternType)) {
            occurrenceInfo = getDailyPreviousOccurrence(now, settings);
        } else if (RecurrenceConstants.WEEKLY.equalsIgnoreCase(patternType)) {
            occurrenceInfo = getWeeklyPreviousOccurrence(now, settings);
        } else if (RecurrenceConstants.ABSOLUTE_MONTHLY.equalsIgnoreCase(patternType)) {
            occurrenceInfo = getAbsoluteMonthlyPreviousOccurrence(now, settings);
        } else if (RecurrenceConstants.RELATIVE_MONTHLY.equalsIgnoreCase(patternType)) {
            occurrenceInfo = getRelativeMonthlyPreviousOccurrence(now, settings);
        } else if (RecurrenceConstants.ABSOLUTE_YEARLY.equalsIgnoreCase(patternType)) {
            occurrenceInfo = getAbsoluteYearlyPreviousOccurrence(now, settings);
        } else if (RecurrenceConstants.RELATIVE_YEARLY.equalsIgnoreCase(patternType)) {
            occurrenceInfo = getRelativeYearlyPreviousOccurrence(now, settings);
        } else {
            throw new IllegalArgumentException(String.format(UNRECOGNIZED_VALUE, RecurrenceConstants.RECURRENCE_PATTERN));
        }

        final RecurrenceRange range = settings.getRecurrence().getRange();
        final ZoneId zoneId = getRecurrenceTimeZoneId(settings);
        if (RecurrenceConstants.END_DATE.equalsIgnoreCase(range.getType())) {
            final ZonedDateTime alignedPreviousOccurrence = occurrenceInfo.previousOccurrence.withZoneSameInstant(zoneId);
            if (alignedPreviousOccurrence.isAfter(range.getEndDate())) {
                return null;
            }
        }
        if (RecurrenceConstants.NUMBERED.equalsIgnoreCase(range.getType())) {
            if (occurrenceInfo.numberOfOccurrences >= range.getNumberOfRecurrences()) {
                return null;
            }
        }

        return occurrenceInfo.previousOccurrence;
    }

    /**
     * Find the closest previous recurrence occurrence before the provided time stamp according to the "Daily" recurrence pattern.
     * @param time A time stamp.
     * @param settings The settings of time window filter.
     * @return The return result contains two property, one is previousOccurrence, the other is numberOfOccurrences.
     * previousOccurrence: The closest previous occurrence.
     * numberOfOccurrences: The number of complete recurrence intervals which have occurred between the time and the recurrence start.
     * */
    private static OccurrenceInfo getDailyPreviousOccurrence(ZonedDateTime time, TimeWindowFilterSettings settings) {
        final ZonedDateTime start = settings.getStart();
        final int interval = settings.getRecurrence().getPattern().getInterval();
        final int numberOfOccurrences = (int) (Duration.between(start, time).getSeconds() / Duration.ofDays(interval).getSeconds());
        return new OccurrenceInfo(start.plusDays((long) numberOfOccurrences * interval), numberOfOccurrences);
    }

    /**
     * Find the closest previous recurrence occurrence before the provided time stamp according to the "Weekly" recurrence pattern.
     * @param time A time stamp.
     * @param settings The settings of time window filter.
     * @return The return result contains two property, one is previousOccurrence, the other is numberOfOccurrences.
     * previousOccurrence: The closest previous occurrence.
     * numberOfOccurrences: The number of recurring days of week which have occurred between the time and the recurrence start.
     * */
    private static OccurrenceInfo getWeeklyPreviousOccurrence(ZonedDateTime time, TimeWindowFilterSettings settings) {
        final RecurrencePattern pattern = settings.getRecurrence().getPattern();
        final int interval = pattern.getInterval();
        final ZoneId zoneId = getRecurrenceTimeZoneId(settings);
        final ZonedDateTime alignedStart = settings.getStart().withZoneSameInstant(zoneId);

        // calculate teh duration of first interval
        final int firstDayOfWeek = convertToWeekDayNumber(pattern.getFirstDayOfWeek());
        final int totalDaysOfFirstInterval = RecurrenceConstants.WEEK_DAY_NUMBER * (interval - 1) +
            remainingDaysOfWeek(alignedStart.getDayOfWeek().getValue(), firstDayOfWeek);
        final Instant endDateOfFirstInterval = alignedStart.plusDays(totalDaysOfFirstInterval).toInstant().truncatedTo(ChronoUnit.DAYS);
        final Duration totalDurationOfFirstInterval = Duration.between(alignedStart, endDateOfFirstInterval);

        ZonedDateTime previousOccurrence;
        int numberOfOccurrences = 0;
        final Duration timeGap = Duration.between(alignedStart, time);
        if (totalDurationOfFirstInterval.compareTo(timeGap) <= 0) {
            int numberOfInterval = timeGap.minus(totalDurationOfFirstInterval).toSecondsPart() /
                Duration.ofDays(interval * RecurrenceConstants.WEEK_DAY_NUMBER).toSecondsPart();
            previousOccurrence = alignedStart.plusDays(totalDaysOfFirstInterval +
                numberOfInterval * interval * RecurrenceConstants.WEEK_DAY_NUMBER);
            numberOfOccurrences += numberOfInterval * pattern.getDaysOfWeek().size();

            // count the number of occurrence in the first week
            ZonedDateTime dateTime = alignedStart;
            while (dateTime.getDayOfWeek().getValue() != firstDayOfWeek) {
                final ZonedDateTime tempDateTime = dateTime;
                if (pattern.getDaysOfWeek().stream().anyMatch(day ->
                    convertToWeekDayNumber(day) == tempDateTime.getDayOfWeek().getValue())) {
                    numberOfOccurrences += 1;
                }
                dateTime = dateTime.plusDays(1);
            }
        } else { // time is still within the first interval
            previousOccurrence = alignedStart;
        }

        ZonedDateTime loopedDateTime = previousOccurrence;
        final ZonedDateTime alignedTime = time.withZoneSameInstant(zoneId);
        while (loopedDateTime.isBefore(alignedTime)) {
            if (loopedDateTime.getDayOfWeek().getValue() == firstDayOfWeek) {   // Come to the next week
                break;
            }

            final ZonedDateTime tempDateTime = loopedDateTime;
            if (pattern.getDaysOfWeek().stream().anyMatch(day ->
                convertToWeekDayNumber(day) == tempDateTime.getDayOfWeek().getValue())) {
                // todo why we need to create with timeZoneOffset
//                previousOccurrence = new ZonedDateTime(loopedDateTime, timeZoneOffset);
                previousOccurrence = loopedDateTime;
                numberOfOccurrences += 1;
            }
            loopedDateTime = loopedDateTime.plusDays(1);
        }
        return new OccurrenceInfo(previousOccurrence, numberOfOccurrences);
    }

    /**
     * Find the closest previous recurrence occurrence before the provided time stamp according to the "AbsoluteMonthly" recurrence pattern.
     * @param time A time stamp.
     * @param settings The settings of time window filter.
     * @return The return result contains two property, one is previousOccurrence, the other is numberOfOccurrences.
     * previousOccurrence: The closest previous occurrence.
     * numberOfOccurrences: The number of complete recurrence intervals which have occurred between the time and the recurrence start.
     *  */
    private static OccurrenceInfo getAbsoluteMonthlyPreviousOccurrence(ZonedDateTime time, TimeWindowFilterSettings settings) {
        final RecurrencePattern pattern = settings.getRecurrence().getPattern();
        final ZonedDateTime start = settings.getStart();
        final int interval = pattern.getInterval();

        final ZoneId zoneId = getRecurrenceTimeZoneId(settings);
        final ZonedDateTime alignedStart = start.withZoneSameInstant(zoneId);
        final ZonedDateTime alignedTime = time.withZoneSameInstant(zoneId);

        int monthGap = (alignedTime.getYear() - alignedStart.getYear()) * 12 + alignedTime.getMonthValue() - alignedStart.getMonthValue();
        final Duration startDuration = Duration.between(alignedStart.withHour(0).withSecond(0).withNano(0), alignedStart).plus(
            Duration.ofDays(alignedStart.getDayOfMonth()));
        final Duration timeDuration = Duration.between(alignedTime.withHour(0).withSecond(0).withNano(0), alignedTime).plus(
            Duration.ofDays(alignedTime.getDayOfMonth()));
        if (timeDuration.compareTo(startDuration) < 0) {
            monthGap -= 1;
        }

        final int numberOfInterval = monthGap / interval;
        return new OccurrenceInfo(alignedStart.plusMonths(numberOfInterval * interval), numberOfInterval);
    }

    /**
     * Find the closest previous recurrence occurrence before the provided time stamp according to the "RelativeMonthly" recurrence pattern.
     * @param time A time stamp.
     * @param settings The settings of time window filter.
     * @return The return result contains two property, one is previousOccurrence, the other is numberOfOccurrences.
     *  previousOccurrence: The closest previous occurrence.
     *  numberOfOccurrences: The number of complete recurrence intervals which have occurred between the time and the recurrence start.
     *  */
    private static OccurrenceInfo getRelativeMonthlyPreviousOccurrence(ZonedDateTime time, TimeWindowFilterSettings settings) {
        final ZoneId zoneId = getRecurrenceTimeZoneId(settings);
        final ZonedDateTime alignedStart = settings.getStart().withZoneSameInstant(zoneId);
        final ZonedDateTime alignedTime = time.withZoneSameInstant(zoneId);

        final RecurrencePattern pattern = settings.getRecurrence().getPattern();
        final int interval = pattern.getInterval();

        int monthGap = (alignedTime.getYear() - alignedStart.getYear()) * 12 + alignedTime.getMonthValue() - alignedStart.getMonthValue();
        final Duration startDuration = Duration.between(alignedStart.withHour(0).withMinute(0).withSecond(0).withNano(0), alignedStart);
        if (!pattern.getDaysOfWeek().stream().anyMatch(day ->
            alignedTime.isAfter(dayOfNthWeekInTheMonth(alignedTime, pattern.getIndex(), day).plus(startDuration)))) {
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

        return new OccurrenceInfo(alignedPreviousOccurrence, numberOfInterval);
    }

    /**
     * Find the closest previous recurrence occurrence before the provided time stamp according to the "AbsoluteYearly" recurrence pattern.
     * @param time A time stamp.
     * @param settings The settings of time window filter.
     * @return The return result contains two property, one is previousOccurrence, the other is numberOfOccurrences.
     * previousOccurrence: The closest previous occurrence.
     * numberOfOccurrences: The number of complete recurrence intervals which have occurred between the time and the recurrence start.
     *  */
    private static OccurrenceInfo getAbsoluteYearlyPreviousOccurrence(ZonedDateTime time, TimeWindowFilterSettings settings) {
        final ZoneId zoneId = getRecurrenceTimeZoneId(settings);
        final ZonedDateTime alignedStart = settings.getStart().withZoneSameInstant(zoneId);
        final ZonedDateTime alignedTime = time.withZoneSameInstant(zoneId);

        int yearGap = alignedTime.getYear() - alignedStart.getYear();
        final Duration startDuration = Duration.between(alignedStart.withHour(0).withSecond(0).withNano(0), alignedStart).plus(
            Duration.ofDays(alignedStart.getDayOfYear()));
        final Duration timeDuration = Duration.between(alignedTime.withHour(0).withSecond(0).withNano(0), alignedTime).plus(
            Duration.ofDays(alignedTime.getDayOfYear()));
        if (timeDuration.compareTo(startDuration) < 0) {
            yearGap -= 1;
        }

        final int interval = settings.getRecurrence().getPattern().getInterval();
        final int numberOfInterval = yearGap / interval;
        return new OccurrenceInfo(alignedStart.plusYears(numberOfInterval * interval), numberOfInterval);
    }

    /**
     * Find the closest previous recurrence occurrence before the provided time stamp according to the "RelativeYearly" recurrence pattern.
     * @param time A time stamp.
     * @param settings The settings of time window filter.
     * @return The return result contains two property, one is previousOccurrence, the other is numberOfOccurrences.
     * previousOccurrence: The closest previous occurrence.
     * numberOfOccurrences: The number of complete recurrence intervals which have occurred between the time and the recurrence start.
     *  */
    private static OccurrenceInfo getRelativeYearlyPreviousOccurrence(ZonedDateTime time, TimeWindowFilterSettings settings) {
        final RecurrencePattern pattern = settings.getRecurrence().getPattern();

        final ZoneId zoneId = getRecurrenceTimeZoneId(settings);
        final ZonedDateTime alignedStart = settings.getStart().withZoneSameInstant(zoneId);
        final ZonedDateTime alignedTime = time.withZoneSameInstant(zoneId);

        int yearGap = alignedTime.getYear() - alignedStart.getYear();
        final Duration startDuration = Duration.between(alignedStart.withHour(0).withMinute(0).withSecond(0).withNano(0), alignedStart);
        if (alignedTime.getMonthValue() < alignedStart.getMonthValue()) {
            // E.g. start: 2023.9 and time: 2024.8
            // Not a complete yearly interval
            yearGap -= 1;
        } else if (alignedTime.getMonthValue() == alignedStart.getMonthValue() &&
            !pattern.getDaysOfWeek().stream().anyMatch(day ->
            alignedTime.isAfter(dayOfNthWeekInTheMonth(alignedTime, pattern.getIndex(), day).plus(startDuration)))) {
            // E.g. start: 2023.9.1 (the first Friday in 2023.9) and time: 2024.9.2 (the first Friday in 2023.9 is 2024.9.6)
            // Not a complete yearly interval
            yearGap -= 1;
        }

        final int interval = pattern.getInterval();
        final int numberOfInterval = yearGap / interval;
        final ZonedDateTime alignedPreviousOccurrenceMonth = alignedStart.plusYears(numberOfInterval * interval);
        ZonedDateTime alignedPreviousOccurrence = null;

        // Find the first occurence date matched the pattern
        // Only one day of week in the month will be matched
        for (String day: pattern.getDaysOfWeek()) {
            final ZonedDateTime occurrenceDate = dayOfNthWeekInTheMonth(alignedPreviousOccurrenceMonth,
                pattern.getIndex(), day).plus(startDuration);
            if (alignedPreviousOccurrence == null || alignedPreviousOccurrence.isAfter(occurrenceDate)) {
                alignedPreviousOccurrence = occurrenceDate;
            }
        }

        return new OccurrenceInfo(alignedPreviousOccurrence, numberOfInterval);
    }

    private static boolean tryValidateGeneralRequiredParameter(TimeWindowFilterSettings settings) {
        final Recurrence recurrence = settings.getRecurrence();
        if (settings.getStart() == null) {
            paramName = TIME_WINDOW_FILTER_SETTING_START;
            reason = REQUIRED_PARAMETER;
            return false;
        }
        if (settings.getEnd() == null) {
            paramName = TIME_WINDOW_FILTER_SETTING_END;
            reason = REQUIRED_PARAMETER;
            return false;
        }
        if (recurrence.getPattern() == null) {
            paramName = String.format("%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN);
            reason = REQUIRED_PARAMETER;
            return false;
        }
        if (recurrence.getRange() == null) {
            paramName = String.format("%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_RANGE);
            reason = REQUIRED_PARAMETER;
            return false;
        }
        if (recurrence.getPattern().getType() == null) {
            paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
                RecurrenceConstants.RECURRENCE_PATTERN_TYPE);
            reason = REQUIRED_PARAMETER;
            return false;
        }
        if (recurrence.getRange().getType() == null) {
            paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_RANGE,
                RecurrenceConstants.RECURRENCE_RANGE_TYPE);
            reason = REQUIRED_PARAMETER;
            return false;
        }
        if (settings.getEnd().isBefore(settings.getStart())) {
            paramName = TIME_WINDOW_FILTER_SETTING_END;
            reason = OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    private static boolean tryValidateRecurrencePattern(TimeWindowFilterSettings settings) {
        if (!tryValidateInterval(settings)) {
            return false;
        }

        final String patternType = settings.getRecurrence().getPattern().getType();
        if (RecurrenceConstants.DAILY.equalsIgnoreCase(patternType)) {
            return tryValidateDailyRecurrencePattern(settings);
        } else if (RecurrenceConstants.WEEKLY.equalsIgnoreCase(patternType)) {
            return tryValidateWeeklyRecurrencePattern(settings);
        } else if (RecurrenceConstants.ABSOLUTE_MONTHLY.equalsIgnoreCase(patternType)) {
            return tryValidateAbsoluteMonthlyRecurrencePattern(settings);
        } else if (RecurrenceConstants.RELATIVE_MONTHLY.equalsIgnoreCase(patternType)) {
            return tryValidateRelativeMonthlyRecurrencePattern(settings);
        } else if (RecurrenceConstants.ABSOLUTE_YEARLY.equalsIgnoreCase(patternType)) {
            return tryValidateAbsoluteYearlyRecurrencePattern(settings);
        } else if (RecurrenceConstants.RELATIVE_YEARLY.equalsIgnoreCase(patternType)) {
            return tryValidateRelativeYearlyRecurrencePattern(settings);
        } else {
            paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
                RecurrenceConstants.RECURRENCE_PATTERN_TYPE);
            reason = UNRECOGNIZED_VALUE;
            return false;
        }
    }

    private static boolean tryValidateRecurrenceRange(TimeWindowFilterSettings settings) {
        if (!tryValidateRecurrenceTimeZone(settings)) {
            return false;
        }

        String rangeType = settings.getRecurrence().getRange().getType();
        if (RecurrenceConstants.NO_END.equalsIgnoreCase(rangeType)) {
            // No parameter is required
            return true;
        } else if (RecurrenceConstants.END_DATE.equalsIgnoreCase(rangeType)) {
            return tryValidateEndDate(settings);
        } else if (RecurrenceConstants.NUMBERED.equalsIgnoreCase(rangeType)) {
            return tryValidateNumberOfOccurrences(settings);
        }
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_RANGE,
            RecurrenceConstants.RECURRENCE_RANGE_TYPE);
        reason = UNRECOGNIZED_VALUE;
        return false;
    }

    private static boolean tryValidateRecurrenceTimeZone(TimeWindowFilterSettings settings) {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_RANGE,
            RecurrenceConstants.RECURRENCE_RANGE_RECURRENCE_TIME_ZONE);
        if (settings.getRecurrence().getRange().getRecurrenceTimeZone() != null &&
            tryParseTimeZone(settings.getRecurrence().getRange().getRecurrenceTimeZone()) == null) {
            reason = UNRECOGNIZED_VALUE;
            return false;
        }
        return true;
    }

    private static boolean tryValidateEndDate(TimeWindowFilterSettings settings) {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_RANGE,
            RecurrenceConstants.RECURRENCE_RANGE_EDN_DATE);
        if (settings.getRecurrence().getRange().getEndDate() == null) {
            reason = REQUIRED_PARAMETER;
            return false;
        }

        final ZonedDateTime start = settings.getStart();
        final ZonedDateTime alignedStart = start.withZoneSameInstant(getRecurrenceTimeZoneId(settings));
        if (settings.getRecurrence().getRange().getEndDate().isBefore(alignedStart)) {
            reason = OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    private static boolean tryValidateNumberOfOccurrences(TimeWindowFilterSettings settings) {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_RANGE,
            RecurrenceConstants.RECURRENCE_RANGE_NUMBER_OF_OCCURRENCES);
        if (settings.getRecurrence().getRange().getNumberOfRecurrences() == null) {
            reason = REQUIRED_PARAMETER;
            return false;
        }
        if (settings.getRecurrence().getRange().getNumberOfRecurrences() < 1) {
            reason = OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    private static boolean tryValidateInterval(TimeWindowFilterSettings settings) {
        if (settings.getRecurrence().getPattern().getInterval() <= 0) {
            paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
                RecurrenceConstants.RECURRENCE_PATTERN_INTERVAL);
            reason = OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    private static boolean tryValidateDailyRecurrencePattern(TimeWindowFilterSettings settings) {
        // No required parameter for "Daily" pattern and "Start" is always a valid first occurrence for "Daily" pattern.
        // Only need to check if time window validated
        final Duration intervalDuration = Duration.ofDays(settings.getRecurrence().getPattern().getInterval());
        return tryValidateTimeWindowDuration(settings, intervalDuration);
    }

    private static boolean tryValidateWeeklyRecurrencePattern(TimeWindowFilterSettings settings) {
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
        final ZonedDateTime alignedStart = settings.getStart().withZoneSameInstant(getRecurrenceTimeZoneId(settings));
        if (pattern.getDaysOfWeek().stream().noneMatch((dayOfWeekStr) ->
            alignedStart.getDayOfWeek().getValue() == convertToWeekDayNumber(dayOfWeekStr))) {
            paramName = TIME_WINDOW_FILTER_SETTING_START;
            reason = NOT_MATCHED;
            return false;
        }

        // Check whether the time window duration is shorter than the minimum gap between days of week
        if (!isDurationCompliantWithDaysOfWeek(timeWindowDuration, pattern.getInterval(), pattern.getDaysOfWeek(), pattern.getFirstDayOfWeek())) {
            paramName = TIME_WINDOW_FILTER_SETTING_END;
            reason = OUT_OF_RANGE;
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
    private static boolean isDurationCompliantWithDaysOfWeek(Duration duration, int interval, List<String> daysOfWeek, String firstDayOfWeek) {
        if (daysOfWeek.size() == 1) {
            return true;
        }

        // Get the date of first day of the week
        final ZonedDateTime today = ZonedDateTime.now();
        final int offset = remainingDaysOfWeek(today.getDayOfWeek().getValue(), convertToWeekDayNumber(firstDayOfWeek));
        final ZonedDateTime firstDateOfWeek = today.plusDays(offset);

        // Loop the whole week to get the min gap between the two consecutive recurrences
        // todo why need to initialize as first day of week?
        ZonedDateTime date = firstDateOfWeek;
        ZonedDateTime prevOccurrence = firstDateOfWeek;
        Duration minGap = Duration.ofDays(7);
        for (int i = 0; i < 6; i++) {
            date = date.plusDays(1);
            final ZonedDateTime finalDate = date;
            if (daysOfWeek.stream().anyMatch(dayOfWeek -> convertToWeekDayNumber(dayOfWeek) == finalDate.getDayOfWeek().getValue())) {
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

    private static int remainingDaysOfWeek(int today, int firstDayOfWeek) {
        int remainingDays = today - firstDayOfWeek;
        if (remainingDays < 0) {
            return -remainingDays;
        } else {
            return RecurrenceConstants.WEEK_DAY_NUMBER - remainingDays;
        }
    }

    private static boolean tryValidateAbsoluteMonthlyRecurrencePattern(TimeWindowFilterSettings settings) {
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
        final ZonedDateTime alignedStart = settings.getStart().withZoneSameInstant(getRecurrenceTimeZoneId(settings));
        if (alignedStart.getDayOfMonth() != pattern.getDayOfMonth()) {
            paramName = TIME_WINDOW_FILTER_SETTING_START;
            reason = NOT_MATCHED;
            return false;
        }

        return true;
    }

    private static boolean tryValidateDayOfMonth(TimeWindowFilterSettings settings) {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN, RecurrenceConstants.RECURRENCE_PATTERN_DAY_OF_MONTH);
        if (settings.getRecurrence().getPattern().getDayOfMonth() == null) {
            reason = REQUIRED_PARAMETER;
            return false;
        }
        if (settings.getRecurrence().getPattern().getDayOfMonth() < 1 || settings.getRecurrence().getPattern().getDayOfMonth() > 31) {
            reason = OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    private static boolean tryValidateRelativeMonthlyRecurrencePattern(TimeWindowFilterSettings settings) {
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
        final ZonedDateTime alignedStart = settings.getStart().withZoneSameInstant(getRecurrenceTimeZoneId(settings));
        if (pattern.getDaysOfWeek().stream().noneMatch((dayOfWeekStr) ->
            alignedStart.getDayOfWeek().getValue() == convertToWeekDayNumber(dayOfWeekStr))) {
            paramName = TIME_WINDOW_FILTER_SETTING_START;
            reason = NOT_MATCHED;
            return false;
        }
        return true;
    }

    private static boolean tryValidateIndex(TimeWindowFilterSettings settings) {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
            RecurrenceConstants.RECURRENCE_PATTERN_INDEX);

        final String index = settings.getRecurrence().getPattern().getIndex();
        if (index == null) {
            reason = REQUIRED_PARAMETER;
            return false;
        }

        final String[] legalValues = {RecurrenceConstants.FIRST, RecurrenceConstants.SECOND, RecurrenceConstants.THIRD,
            RecurrenceConstants.FOURTH, RecurrenceConstants.LAST};
        if (Arrays.stream(legalValues).noneMatch(index::equalsIgnoreCase)) {
            reason = UNRECOGNIZED_VALUE;
            return false;
        }
        return true;
    }

    private static boolean tryValidateAbsoluteYearlyRecurrencePattern(TimeWindowFilterSettings settings) {
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
        final ZonedDateTime alignedStart = settings.getStart().withZoneSameInstant(getRecurrenceTimeZoneId(settings));
        if (alignedStart.getDayOfMonth() != pattern.getDayOfMonth() || alignedStart.getMonthValue() != pattern.getMonth()) {
            paramName = TIME_WINDOW_FILTER_SETTING_START;
            reason = NOT_MATCHED;
            return false;
        }

        return true;
    }

    private static boolean tryValidateMonth(TimeWindowFilterSettings settings) {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
            RecurrenceConstants.RECURRENCE_PATTERN_MONTH);
        if (settings.getRecurrence().getPattern().getMonth() == null) {
            reason = REQUIRED_PARAMETER;
            return false;
        }
        if (settings.getRecurrence().getPattern().getMonth() < 1 || settings.getRecurrence().getPattern().getMonth() > 12) {
            reason = OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    private static boolean tryValidateRelativeYearlyRecurrencePattern(TimeWindowFilterSettings settings) {
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
        final ZonedDateTime alignedStart = settings.getStart().withZoneSameInstant(getRecurrenceTimeZoneId(settings));
        if (alignedStart.getMonthValue() != pattern.getMonth() ||
            pattern.getDaysOfWeek().stream().noneMatch(day ->
                dayOfNthWeekInTheMonth(alignedStart, pattern.getIndex(), day) == alignedStart)) {
            paramName = TIME_WINDOW_FILTER_SETTING_START;
            reason = NOT_MATCHED;
            return false;
        }
        return true;
    }

    /**
     * Find the nth day of week in the month of the date time.
     * @param startDateTime start date time
     * @param index Specifies on which instance of the allowed days specified in daysOfsWeek the event occurs, counted from the first instance in the month
     * @param dayOfWeek Specifies on which day(s) of the week the event can occur
     * @return The data time of the day in nth week the month.
     * */
    private static ZonedDateTime dayOfNthWeekInTheMonth(ZonedDateTime startDateTime, String index, String dayOfWeek) {
        ZonedDateTime dateTime = ZonedDateTime.of(startDateTime.getYear(), startDateTime.getMonthValue(), 1,
            startDateTime.getHour(), startDateTime.getMinute(), startDateTime.getSecond(), startDateTime.getNano(), startDateTime.getZone());

        // Find the first day of week in the month
        while (dateTime.getDayOfWeek().getValue() != convertToWeekDayNumber(dayOfWeek)) {
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

    private static boolean tryValidateDaysOfWeek(TimeWindowFilterSettings settings) {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
            RecurrenceConstants.RECURRENCE_PATTERN_DAYS_OF_WEEK);
        final List<String> daysOfWeek = settings.getRecurrence().getPattern().getDaysOfWeek();
        if (daysOfWeek == null || daysOfWeek.size() == 0) {
            reason = REQUIRED_PARAMETER;
            return false;
        }

        final String[] legalValues = {RecurrenceConstants.MONDAY, RecurrenceConstants.TUESDAY, RecurrenceConstants.WEDNESDAY,
            RecurrenceConstants.THURSDAY, RecurrenceConstants.FRIDAY, RecurrenceConstants.SATURDAY, RecurrenceConstants.SUNDAY};
        for (String dayOfWeek: daysOfWeek) {
            if (Arrays.stream(legalValues).noneMatch(dayOfWeek::equalsIgnoreCase)) {
                reason = UNRECOGNIZED_VALUE;
                return false;
            }
        }
        return true;
    }

    private static boolean tryValidateFirstDayOfWeek(TimeWindowFilterSettings settings) {
        paramName = String.format("%s.%s.%s", TIME_WINDOW_FILTER_SETTING_RECURRENCE, RecurrenceConstants.RECURRENCE_PATTERN,
            RecurrenceConstants.RECURRENCE_PATTERN_FIRST_DAY_OF_WEEK);
        final String firstDayOfWeek = settings.getRecurrence().getPattern().getFirstDayOfWeek();
        if (firstDayOfWeek == null) {
            reason = REQUIRED_PARAMETER;
            return false;
        }
        final String[] legalValues = {RecurrenceConstants.MONDAY, RecurrenceConstants.TUESDAY, RecurrenceConstants.WEDNESDAY,
            RecurrenceConstants.THURSDAY, RecurrenceConstants.FRIDAY, RecurrenceConstants.SATURDAY, RecurrenceConstants.SUNDAY};
        if (Arrays.stream(legalValues).noneMatch(firstDayOfWeek::equalsIgnoreCase)) {
            reason = UNRECOGNIZED_VALUE;
            return false;
        }
        return true;
    }

    /**
     * Validate if time window duration is shorter than how frequently it occurs
     * */
    private static boolean tryValidateTimeWindowDuration(TimeWindowFilterSettings settings, Duration intervalDuration) {
        final Duration timeWindowDuration = Duration.between(settings.getStart(), settings.getEnd());
        if (timeWindowDuration.compareTo(intervalDuration) > 0)  {
            paramName = TIME_WINDOW_FILTER_SETTING_END;
            reason = OUT_OF_RANGE;
            return false;
        }
        return true;
    }

    private static ZoneId getRecurrenceTimeZoneId(TimeWindowFilterSettings settings) {
        ZoneId timeZoneId = settings.getStart().getZone();
        final ZoneId rangeZoneId = tryParseTimeZone(settings.getRecurrence().getRange().getRecurrenceTimeZone());
        if (rangeZoneId != null) {
            timeZoneId = rangeZoneId;
        }
        return timeZoneId;
    }

    private static ZoneId tryParseTimeZone(String timeZoneStr) {
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

    private static int convertToWeekDayNumber(String str) {
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

    public static int convertIndexToNumber(String str) {
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

    private static class OccurrenceInfo {
        private ZonedDateTime previousOccurrence = null;
        private int numberOfOccurrences;
        public OccurrenceInfo(ZonedDateTime dateTime, int num) {
            this.previousOccurrence = dateTime;
            this.numberOfOccurrences = num;
        }
    }
}
