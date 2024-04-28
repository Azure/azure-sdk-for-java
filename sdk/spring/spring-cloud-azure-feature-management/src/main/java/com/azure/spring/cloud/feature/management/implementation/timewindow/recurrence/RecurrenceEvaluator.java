// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence;

import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowFilterSettings;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.models.RecurrencePattern;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.models.RecurrencePatternType;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.models.RecurrenceRange;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.models.RecurrenceRangeType;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RecurrenceEvaluator {
    private final TimeWindowFilterSettings settings;
    private final ZonedDateTime now;
    private final int DAYS_PER_WEEK = 7;

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
        final ZonedDateTime previousOccurrence = getPreviousOccurrence();
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
    private ZonedDateTime getPreviousOccurrence() {
        ZonedDateTime start = settings.getStart();
        if (now.isBefore(start)) {
            return null;
        }

        final RecurrencePatternType patternType = settings.getRecurrence().getPattern().getType();
        OccurrenceInfo occurrenceInfo;
        if (patternType == RecurrencePatternType.DAILY) {
            occurrenceInfo = getDailyPreviousOccurrence();
        } else {
            occurrenceInfo = getWeeklyPreviousOccurrence();
        }

        final RecurrenceRange range = settings.getRecurrence().getRange();
        if (range.getType() == RecurrenceRangeType.END_DATE && occurrenceInfo.previousOccurrence.isAfter(range.getEndDate())) {
            return null;
        }
        if (range.getType() == RecurrenceRangeType.NUMBERED && occurrenceInfo.numberOfOccurrences > range.getNumberOfRecurrences()) {
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
            passingDaysOfWeek(settings.getStart().getDayOfWeek(), pattern.getFirstDayOfWeek())).truncatedTo(ChronoUnit.DAYS);

        final long numberOfInterval = Duration.between(firstDayOfFirstWeek, now).toSeconds()
            / Duration.ofDays((long) interval * DAYS_PER_WEEK).toSeconds();
        final ZonedDateTime firstDayOfMostRecentOccurrence = firstDayOfFirstWeek.plusDays(
            numberOfInterval * (interval * DAYS_PER_WEEK));
        final List<DayOfWeek> sortedDaysOfWeek = sortDaysOfWeek(pattern.getDaysOfWeek(), pattern.getFirstDayOfWeek());
        final int maxDayOffset = passingDaysOfWeek(sortedDaysOfWeek.get(sortedDaysOfWeek.size() - 1), pattern.getFirstDayOfWeek());
        final int minDayOffset = passingDaysOfWeek(sortedDaysOfWeek.get(0), pattern.getFirstDayOfWeek());
        ZonedDateTime mostRecentOccurrence = null;
        int numberOfOccurrences = (int) (numberOfInterval * sortedDaysOfWeek.size() -
                    (sortedDaysOfWeek.indexOf(settings.getStart().getDayOfWeek())));

        // now is after the first week
        if (now.isAfter(firstDayOfMostRecentOccurrence.plusDays(DAYS_PER_WEEK))) {
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
            mostRecentOccurrence = firstDayOfMostRecentOccurrence.minusDays(interval * DAYS_PER_WEEK).plusDays(maxDayOffset);
        } else {
            for (int i = sortedDaysOfWeek.indexOf(dayWithMinOffset.getDayOfWeek()) + 1;
                 i < sortedDaysOfWeek.size() && !now.isBefore(dayWithMinOffset); i++) {
                mostRecentOccurrence = dayWithMinOffset;
                numberOfOccurrences++;
                dayWithMinOffset = firstDayOfMostRecentOccurrence.plusDays(
                    passingDaysOfWeek(sortedDaysOfWeek.get(i), pattern.getFirstDayOfWeek()));
            }
        }
        return new OccurrenceInfo(mostRecentOccurrence, numberOfOccurrences);
    }

    private List<DayOfWeek> sortDaysOfWeek(List<String> daysOfWeek, String firstDayOfWeek) {
        final List<DayOfWeek> result = daysOfWeek.stream()
            .map(str -> DayOfWeek.valueOf(str.toUpperCase()))
            .collect(Collectors.toList());

        final int firstDayNum = convertToWeekDayNumber(firstDayOfWeek);
        Collections.sort(result, (a, b) -> {
            int aIndex = (convertToWeekDayNumber(a) - firstDayNum) % 7;
            int bIndex = (convertToWeekDayNumber(b) - firstDayNum) % 7;
            return aIndex - bIndex;
        });
        return result;
    }

    private int convertToWeekDayNumber(String str) {
        final String strUpperCase = str.toUpperCase();
        return DayOfWeek.valueOf(strUpperCase).getValue() % 7;
    }

    private int convertToWeekDayNumber(DayOfWeek dateTime) {
        return dateTime.getValue() % 7;
    }

    private int passingDaysOfWeek(DayOfWeek today, String firstDayOfWeek) {
        int remainingDays = (convertToWeekDayNumber(today) - convertToWeekDayNumber(firstDayOfWeek));
        if (remainingDays < 0) {
            return remainingDays + DAYS_PER_WEEK;
        } else {
            return remainingDays;
        }
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
