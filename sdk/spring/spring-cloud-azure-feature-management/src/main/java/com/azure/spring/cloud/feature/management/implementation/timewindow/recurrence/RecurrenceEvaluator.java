// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence;

import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowFilterSettings;
import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowUtils;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

public class RecurrenceEvaluator {
    private TimeWindowFilterSettings settings;
    private ZonedDateTime now;

    public RecurrenceEvaluator(TimeWindowFilterSettings settings, ZonedDateTime now) {
        this.settings = settings;
        this.now = now;
    }

    /**
     * Checks if a provided timestamp is within any recurring time window specified
     * by the Recurrence section in the time window filter settings.
     * @return True if the time stamp is within any recurring time window, false otherwise.
     */
    public boolean isMatch() {
        final RecurrenceValidator validator = new RecurrenceValidator(settings);
        if (!validator.validateSettings()) {
            return false;
        }

        final ZonedDateTime previousOccurrence = getPreviousOccurrence().previousOccurrence;
        if (previousOccurrence == null) {
            return false;
        }

        final ZonedDateTime occurrenceEndDate = previousOccurrence.plus(
            Duration.between(settings.getStart(), settings.getEnd()));
        return now.isBefore(occurrenceEndDate);
    }

    /**
     * Calculates the start time of the closest active time window.
     * @return If no previous occurrence, return null.
     * If now is in previous occurrence, return previous occurrence.
     * If now is after the end of previous occurrence, return next occurrence.
     * */
    public ZonedDateTime getClosestStart() {
        final OccurrenceInfo occurrenceInfo = getPreviousOccurrence();
        final ZonedDateTime prevOccurrence = occurrenceInfo.previousOccurrence;
        final ZonedDateTime nextOccurrence = getNextOccurrence(occurrenceInfo);

        if (now.isBefore(settings.getStart())) {
            return nextOccurrence;
        }

        if (prevOccurrence != null) {
            final boolean isWithinPreviousTimeWindow = now.isBefore(
                prevOccurrence.plus(Duration.between(settings.getStart(), settings.getEnd())));
            if (isWithinPreviousTimeWindow) {
                return prevOccurrence;
            }
            return nextOccurrence;
        }
        return null;
    }

    /**
     * Find the most recent recurrence occurrence before the provided time stamp.
     *
     * @return The closest previous occurrence.
     */
    private OccurrenceInfo getPreviousOccurrence() {
        ZonedDateTime start = settings.getStart();
        OccurrenceInfo emptyOccurrence = new OccurrenceInfo();
        if (now.isBefore(start)) {
            return emptyOccurrence;
        }

        final RecurrencePatternType patternType = settings.getRecurrence().getPattern().getType();
        OccurrenceInfo occurrenceInfo;
        if (patternType == RecurrencePatternType.DAILY) {
            occurrenceInfo = getDailyPreviousOccurrence();
        } else {
            occurrenceInfo = getWeeklyPreviousOccurrence();
        }

        final RecurrenceRange range = settings.getRecurrence().getRange();
        if (range.getType() == RecurrenceRangeType.END_DATE
            && occurrenceInfo.previousOccurrence != null
            && occurrenceInfo.previousOccurrence.isAfter(range.getEndDate())) {
            return emptyOccurrence;
        }
        if (range.getType() == RecurrenceRangeType.NUMBERED
            && occurrenceInfo.numberOfOccurrences > range.getNumberOfRecurrences()) {
            return emptyOccurrence;
        }

        return occurrenceInfo;
    }

    /**
     * Find the closest previous recurrence occurrence before the provided time stamp according to the "Daily" recurrence pattern.
     *
     * @return The return result contains two property, one is previousOccurrence, the other is numberOfOccurrences.
     * previousOccurrence: The closest previous occurrence.
     * numberOfOccurrences: The number of complete recurrence intervals which have occurred between the time and the recurrence start.
     */
    private OccurrenceInfo getDailyPreviousOccurrence() {
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
    private OccurrenceInfo getWeeklyPreviousOccurrence() {
        final RecurrencePattern pattern = settings.getRecurrence().getPattern();
        final int interval = pattern.getInterval();
        final ZonedDateTime firstDayOfFirstWeek = settings.getStart().minusDays(
            TimeWindowUtils.passingDaysOfWeek(settings.getStart().getDayOfWeek(), pattern.getFirstDayOfWeek()));

        final long numberOfInterval = Duration.between(firstDayOfFirstWeek, now).toSeconds()
            / Duration.ofDays((long) interval * RecurrenceConstants.DAYS_PER_WEEK).toSeconds();
        final ZonedDateTime firstDayOfMostRecentOccurrence = firstDayOfFirstWeek.plusDays(
            numberOfInterval * (interval * RecurrenceConstants.DAYS_PER_WEEK));
        final List<DayOfWeek> sortedDaysOfWeek = TimeWindowUtils.sortDaysOfWeek(pattern.getDaysOfWeek(), pattern.getFirstDayOfWeek());
        final int maxDayOffset = TimeWindowUtils.passingDaysOfWeek(sortedDaysOfWeek.get(sortedDaysOfWeek.size() - 1), pattern.getFirstDayOfWeek());
        final int minDayOffset = TimeWindowUtils.passingDaysOfWeek(sortedDaysOfWeek.get(0), pattern.getFirstDayOfWeek());
        ZonedDateTime mostRecentOccurrence = null;
        int numberOfOccurrences = (int) (numberOfInterval * sortedDaysOfWeek.size()
            - (sortedDaysOfWeek.indexOf(settings.getStart().getDayOfWeek())));

        // now is after the first week
        if (now.isAfter(firstDayOfMostRecentOccurrence.plusDays(RecurrenceConstants.DAYS_PER_WEEK))) {
            numberOfOccurrences += sortedDaysOfWeek.size();
            mostRecentOccurrence = firstDayOfMostRecentOccurrence.plusDays(maxDayOffset);
            return new OccurrenceInfo(mostRecentOccurrence, numberOfOccurrences);
        }

        // day with the min offset in the most recent occurring week
        ZonedDateTime dayWithMinOffset = firstDayOfMostRecentOccurrence.plusDays(minDayOffset);
        if (settings.getStart().isAfter(dayWithMinOffset)) {
            numberOfOccurrences = 0;
            dayWithMinOffset = settings.getStart();
        }
        if (now.isBefore(dayWithMinOffset)) {  // move to last occurrence
            mostRecentOccurrence = firstDayOfMostRecentOccurrence.minusDays(interval * RecurrenceConstants.DAYS_PER_WEEK).plusDays(maxDayOffset);
        } else {
            mostRecentOccurrence = dayWithMinOffset;
            numberOfOccurrences++;

            for (int i = sortedDaysOfWeek.indexOf(dayWithMinOffset.getDayOfWeek()) + 1; i < sortedDaysOfWeek.size(); i++) {
                dayWithMinOffset = firstDayOfMostRecentOccurrence.plusDays(
                    TimeWindowUtils.passingDaysOfWeek(sortedDaysOfWeek.get(i), pattern.getFirstDayOfWeek()));
                if (now.isBefore(dayWithMinOffset)) {
                    break;
                }
                mostRecentOccurrence = dayWithMinOffset;
                numberOfOccurrences++;
            }
        }
        return new OccurrenceInfo(mostRecentOccurrence, numberOfOccurrences);
    }

    /**
     * Finds the next occurrence after the provided previous occurrence.
     * @param occurrenceInfo previous occurrence time and number of occurrences
     * @return next occurrence
     * */
    private ZonedDateTime getNextOccurrence(OccurrenceInfo occurrenceInfo) {
        if (now.isBefore(settings.getStart())) {
            return settings.getStart();
        }

        ZonedDateTime nextOccurrence = null;
        final ZonedDateTime prevOccurrence = occurrenceInfo.previousOccurrence;
        if (prevOccurrence != null) {
            final RecurrencePattern pattern = settings.getRecurrence().getPattern();

            if (RecurrencePatternType.DAILY.equals(pattern.getType())) {
                nextOccurrence = prevOccurrence.plusDays(pattern.getInterval());
            }

            if (RecurrencePatternType.WEEKLY.equals(pattern.getType())) {
                nextOccurrence = calculateWeeklyNextOccurrence(prevOccurrence);
            }

            final RecurrenceRange range = settings.getRecurrence().getRange();
            if (RecurrenceRangeType.END_DATE.equals(range.getType())) {
                if (nextOccurrence != null && nextOccurrence.isAfter(range.getEndDate())) {
                    nextOccurrence = null;
                }
            }

            if (RecurrenceRangeType.NUMBERED.equals(range.getType())) {
                if (occurrenceInfo.numberOfOccurrences >= range.getNumberOfRecurrences()) {
                    nextOccurrence = null;
                }
            }
        }
        return nextOccurrence;
    }

    /**
     * Finds the next occurrence after the provided previous occurrence according to the "Weekly" recurrence pattern.
     * @param prevOccurrence previous occurrence
     * @return next occurrence
     * */
    private ZonedDateTime calculateWeeklyNextOccurrence(ZonedDateTime prevOccurrence) {
        final RecurrencePattern pattern = settings.getRecurrence().getPattern();
        final List<DayOfWeek> sortedDaysOfWeek = TimeWindowUtils.sortDaysOfWeek(pattern.getDaysOfWeek(), pattern.getFirstDayOfWeek());
        final int i = sortedDaysOfWeek.indexOf(prevOccurrence.getDayOfWeek()) + 1;

        if (i < sortedDaysOfWeek.size()) {
            return prevOccurrence.plusDays(
                TimeWindowUtils.passingDaysOfWeek(sortedDaysOfWeek.get(i), prevOccurrence.getDayOfWeek().toString()));
        }
        return prevOccurrence.plusDays(pattern.getInterval() * RecurrenceConstants.DAYS_PER_WEEK
            - TimeWindowUtils.passingDaysOfWeek(prevOccurrence.getDayOfWeek(), sortedDaysOfWeek.get(0).toString()));
    }

    private class OccurrenceInfo {
        private final ZonedDateTime previousOccurrence;
        private final int numberOfOccurrences;

        OccurrenceInfo() {
            this.previousOccurrence = null;
            this.numberOfOccurrences = 0;
        }

        OccurrenceInfo(ZonedDateTime dateTime, int num) {
            this.previousOccurrence = dateTime;
            this.numberOfOccurrences = num;
        }
    }
}
