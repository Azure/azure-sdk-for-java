// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence;

import com.azure.spring.cloud.feature.management.implementation.models.RecurrencePattern;
import com.azure.spring.cloud.feature.management.implementation.models.RecurrencePatternType;
import com.azure.spring.cloud.feature.management.implementation.models.RecurrenceRange;
import com.azure.spring.cloud.feature.management.implementation.models.RecurrenceRangeType;
import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowFilterSettings;
import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowUtils;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

public class RecurrenceEvaluator {
    /**
     * Checks if a provided timestamp is within any recurring time window specified
     * by the Recurrence section in the time window filter settings.
     * @return True if the time stamp is within any recurring time window, false otherwise.
     */
    public static boolean isMatch(TimeWindowFilterSettings settings, ZonedDateTime now) {
        RecurrenceValidator.validateSettings(settings);

        final ZonedDateTime previousOccurrence = getPreviousOccurrence(settings, now);
        if (previousOccurrence == null) {
            return false;
        }

        final ZonedDateTime occurrenceEndDate = previousOccurrence.plus(
            Duration.between(settings.getStart(), settings.getEnd()));
        return now.isBefore(occurrenceEndDate);
    }

    /**
     * Find the most recent recurrence occurrence before the provided time stamp.
     *
     * @return The closest previous occurrence.
     */
    private static ZonedDateTime getPreviousOccurrence(TimeWindowFilterSettings settings, ZonedDateTime now) {
        ZonedDateTime start = settings.getStart();
        if (now.isBefore(start)) {
            return null;
        }

        final RecurrencePatternType patternType = settings.getRecurrence().getPattern().getType();
        OccurrenceInfo occurrenceInfo;
        if (patternType == RecurrencePatternType.DAILY) {
            occurrenceInfo = getDailyPreviousOccurrence(settings, now);
        } else {
            occurrenceInfo = getWeeklyPreviousOccurrence(settings, now);
        }

        final RecurrenceRange range = settings.getRecurrence().getRange();
        final RecurrenceRangeType rangeType = range.getType();
        if (rangeType == RecurrenceRangeType.ENDDATE
            && occurrenceInfo.previousOccurrence != null
            && occurrenceInfo.previousOccurrence.isAfter(range.getEndDate())) {
            return null;
        }
        if (rangeType == RecurrenceRangeType.NUMBERED
            && occurrenceInfo.numberOfOccurrences > range.getNumberOfOccurrences()) {
            return null;
        }

        return occurrenceInfo.previousOccurrence;
    }

    /**
     * Find the closest previous recurrence occurrence before the provided time stamp according to the "Daily" recurrence pattern.
     *
     * @return The return result contains two property, one is previousOccurrence, the other is numberOfOccurrences.
     * previousOccurrence: The closest previous occurrence.
     * numberOfOccurrences: The number of complete recurrence intervals which have occurred between the time and the recurrence start.
     */
    private static OccurrenceInfo getDailyPreviousOccurrence(TimeWindowFilterSettings settings, ZonedDateTime now) {
        final ZonedDateTime start = settings.getStart();
        final int interval = settings.getRecurrence().getPattern().getInterval();
        final int numberOfOccurrences = (int) (Duration.between(start, now).getSeconds() / Duration.ofDays(interval).getSeconds());
        return new OccurrenceInfo(start.plusDays((long) numberOfOccurrences * interval), numberOfOccurrences + 1);
    }

    /**
     * Find the closest previous recurrence occurrence before the provided time stamp according to the "Weekly" recurrence pattern.
     *
     * @return The return result contains two property, one is previousOccurrence, the other is numberOfOccurrences.
     * previousOccurrence: The closest previous occurrence.
     * numberOfOccurrences: The number of recurring days of week which have occurred between the time and the recurrence start.
     */
    private static OccurrenceInfo getWeeklyPreviousOccurrence(TimeWindowFilterSettings settings, ZonedDateTime now) {
        final RecurrencePattern pattern = settings.getRecurrence().getPattern();
        final int interval = pattern.getInterval();
        final ZonedDateTime start = settings.getStart();
        final ZonedDateTime firstDayOfFirstWeek = start.minusDays(
            TimeWindowUtils.getPassedWeekDays(start.getDayOfWeek(), pattern.getFirstDayOfWeek()));

        final long numberOfInterval = Duration.between(firstDayOfFirstWeek, now).toSeconds()
            / Duration.ofDays((long) interval * RecurrenceConstants.DAYS_PER_WEEK).toSeconds();
        final ZonedDateTime firstDayOfMostRecentOccurringWeek = firstDayOfFirstWeek.plusDays(
            numberOfInterval * (interval * RecurrenceConstants.DAYS_PER_WEEK));
        final List<DayOfWeek> sortedDaysOfWeek = TimeWindowUtils.sortDaysOfWeek(pattern.getDaysOfWeek(), pattern.getFirstDayOfWeek());
        final int maxDayOffset = TimeWindowUtils.getPassedWeekDays(sortedDaysOfWeek.get(sortedDaysOfWeek.size() - 1), pattern.getFirstDayOfWeek());
        final int minDayOffset = TimeWindowUtils.getPassedWeekDays(sortedDaysOfWeek.get(0), pattern.getFirstDayOfWeek());
        ZonedDateTime mostRecentOccurrence;
        int numberOfOccurrences = (int) (numberOfInterval * sortedDaysOfWeek.size()
            - (sortedDaysOfWeek.indexOf(start.getDayOfWeek())));

        // now is not within the most recent occurring week
        if (now.isAfter(firstDayOfMostRecentOccurringWeek.plusDays(RecurrenceConstants.DAYS_PER_WEEK))) {
            numberOfOccurrences += sortedDaysOfWeek.size();
            mostRecentOccurrence = firstDayOfMostRecentOccurringWeek.plusDays(maxDayOffset);
            return new OccurrenceInfo(mostRecentOccurrence, numberOfOccurrences);
        }

        // day with the min offset in the most recent occurring week
        ZonedDateTime dayWithMinOffset = firstDayOfMostRecentOccurringWeek.plusDays(minDayOffset);
        if (start.isAfter(dayWithMinOffset)) {
            numberOfOccurrences = 0;
            dayWithMinOffset = start;
        }
        if (now.isBefore(dayWithMinOffset)) {
            // move to the last occurrence in the previous occurring week
            mostRecentOccurrence = firstDayOfMostRecentOccurringWeek.minusDays(interval * RecurrenceConstants.DAYS_PER_WEEK).plusDays(maxDayOffset);
        } else {
            mostRecentOccurrence = dayWithMinOffset;
            numberOfOccurrences++;

            for (int i = sortedDaysOfWeek.indexOf(dayWithMinOffset.getDayOfWeek()) + 1; i < sortedDaysOfWeek.size(); i++) {
                dayWithMinOffset = firstDayOfMostRecentOccurringWeek.plusDays(
                    TimeWindowUtils.getPassedWeekDays(sortedDaysOfWeek.get(i), pattern.getFirstDayOfWeek()));
                if (now.isBefore(dayWithMinOffset)) {
                    break;
                }
                mostRecentOccurrence = dayWithMinOffset;
                numberOfOccurrences++;
            }
        }
        return new OccurrenceInfo(mostRecentOccurrence, numberOfOccurrences);
    }

    private static class OccurrenceInfo {
        private final ZonedDateTime previousOccurrence;
        private final int numberOfOccurrences;

        OccurrenceInfo(ZonedDateTime dateTime, int num) {
            this.previousOccurrence = dateTime;
            this.numberOfOccurrences = num;
        }
    }
}
