// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.filters.recurrence;

import com.azure.spring.cloud.feature.management.implementation.timewindow.TimeWindowFilterSettings;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.Recurrence;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceEvaluator;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrencePattern;
import com.azure.spring.cloud.feature.management.implementation.timewindow.recurrence.RecurrenceRange;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecurrenceEvaluatorTest {
    @Test
    public void matchDailyRecurrenceTest() {
        final List<EvaluationTestData> testDataList = new ArrayList<>();

        // interval is 1
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
        testDataList.add(new EvaluationTestData(now1, settings1, true));

        // inter is 4
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
        testDataList.add(new EvaluationTestData(now2, settings2, true));

        // inter is 4
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
        testDataList.add(new EvaluationTestData(now3, settings3, true));

        // now date matched the pattern but not match numberOfOccurrences
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
        testDataList.add(new EvaluationTestData(now4, settings4, false));

        // now date matched the pattern but not match range end date
        final ZonedDateTime now5 = ZonedDateTime.parse("2023-09-02T17:00:00+00:00");
        final TimeWindowFilterSettings settings5 = new TimeWindowFilterSettings();
        final RecurrencePattern pattern5 = new RecurrencePattern();
        final RecurrenceRange range5 = new RecurrenceRange();
        final Recurrence recurrence5 = new Recurrence();
        pattern5.setType("Daily");
        range5.setType("EndDate");
        range5.setRecurrenceTimeZone("UTC+08:00");
        range5.setEndDate("2023-09-02T00:00:00+08:00");
        recurrence5.setRange(range5);
        recurrence5.setPattern(pattern5);
        settings5.setStart("2023-09-01T17:00:00+00:00");
        settings5.setEnd("2023-09-01T17:30:00+00:00");
        settings5.setRecurrence(recurrence5);
        testDataList.add(new EvaluationTestData(now5, settings5, false));

        consumeEvaluationTestData(testDataList);
    }

    @Test
    public void matchWeeklyRecurrenceTest() {
        final List<EvaluationTestData> testDataList = new ArrayList<>();

        // match one of the daysOfWeek
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
        testDataList.add(new EvaluationTestData(now1, settings1, true));

        // match one of the daysOfWeek not match interval
        final ZonedDateTime now2 = ZonedDateTime.parse("2023-09-04T00:00:00+08:00");    // Monday in the 2nd week
        final TimeWindowFilterSettings settings2 = new TimeWindowFilterSettings();
        final RecurrencePattern pattern2 = new RecurrencePattern();
        final RecurrenceRange range2 = new RecurrenceRange();
        final Recurrence recurrence2 = new Recurrence();
        pattern2.setType("Weekly");
        pattern2.setDaysOfWeek(List.of("Monday", "Sunday"));
        pattern2.setInterval(2);
        recurrence2.setRange(range2);
        recurrence2.setPattern(pattern2);
        settings2.setStart("2023-09-03T00:00:00+08:00");    // Sunday
        settings2.setEnd("2023-09-03T00:00:01+08:00");
        settings2.setRecurrence(recurrence2);
        testDataList.add(new EvaluationTestData(now2, settings2, false));

        // match one of the daysOfWeek & match interval
        final ZonedDateTime now3 = ZonedDateTime.parse("2023-09-04T00:00:00+08:00");    // Monday in the 1st week
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
        testDataList.add(new EvaluationTestData(now3, settings3, true));

        // match one of the daysOfWeek & match interval & match numberOfOccurrences
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
        testDataList.add(new EvaluationTestData(now4, settings4, true));

        // time window across days
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
        testDataList.add(new EvaluationTestData(now5, settings5, true));

        // time window across days & not match numberOfRecurrences
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
        testDataList.add(new EvaluationTestData(now6, settings6, true));

        consumeEvaluationTestData(testDataList);
    }

    @Test
    public void matchAbsoluteMonthlyRecurrenceTest() {

        final List<EvaluationTestData> testDataList = new ArrayList<>();

        // 1st every month
        final ZonedDateTime now1 = ZonedDateTime.parse("2023-10-01T00:00:00+08:00");
        final TimeWindowFilterSettings settings1 = new TimeWindowFilterSettings();
        final RecurrencePattern pattern1 = new RecurrencePattern();
        final RecurrenceRange range1 = new RecurrenceRange();
        final Recurrence recurrence1 = new Recurrence();
        pattern1.setType("AbsoluteMonthly");
        pattern1.setDayOfMonth(1);
        recurrence1.setRange(range1);
        recurrence1.setPattern(pattern1);
        settings1.setStart("2023-09-01T00:00:00+08:00");
        settings1.setEnd("2023-09-01T00:00:01+08:00");
        settings1.setRecurrence(recurrence1);
        testDataList.add(new EvaluationTestData(now1, settings1, true));

        // 1st every 5 months
        final ZonedDateTime now2 = ZonedDateTime.parse("2024-02-01T00:00:00+08:00");
        final TimeWindowFilterSettings settings2 = new TimeWindowFilterSettings();
        final RecurrencePattern pattern2 = new RecurrencePattern();
        final RecurrenceRange range2 = new RecurrenceRange();
        final Recurrence recurrence2 = new Recurrence();
        pattern2.setType("AbsoluteMonthly");
        pattern2.setDayOfMonth(1);
        pattern2.setInterval(5);
        recurrence2.setRange(range2);
        recurrence2.setPattern(pattern2);
        settings2.setStart("2023-09-01T00:00:00+08:00");
        settings2.setEnd("2023-09-01T00:00:01+08:00");
        settings2.setRecurrence(recurrence2);
        testDataList.add(new EvaluationTestData(now2, settings2, true));

        // 1st every 5 months
        final ZonedDateTime now3 = ZonedDateTime.parse("2024-01-01T00:00:00+08:00");
        final TimeWindowFilterSettings settings3 = new TimeWindowFilterSettings();
        final RecurrencePattern pattern3 = new RecurrencePattern();
        final RecurrenceRange range3 = new RecurrenceRange();
        final Recurrence recurrence3 = new Recurrence();
        pattern3.setType("AbsoluteMonthly");
        pattern3.setDayOfMonth(1);
        pattern3.setInterval(5);
        recurrence3.setRange(range3);
        recurrence3.setPattern(pattern3);
        settings3.setStart("2023-09-01T00:00:00+08:00");
        settings3.setEnd("2023-09-01T00:00:01+08:00");
        settings3.setRecurrence(recurrence3);
        testDataList.add(new EvaluationTestData(now3, settings3, false));

        // 1st every 4 months & not match numberOfOccurrences
        final ZonedDateTime now4 = ZonedDateTime.parse("2024-01-01T00:00:00+08:00");
        final TimeWindowFilterSettings settings4 = new TimeWindowFilterSettings();
        final RecurrencePattern pattern4 = new RecurrencePattern();
        final RecurrenceRange range4 = new RecurrenceRange();
        final Recurrence recurrence4 = new Recurrence();
        pattern4.setType("AbsoluteMonthly");
        pattern4.setDayOfMonth(1);
        pattern4.setInterval(4);
        recurrence4.setPattern(pattern4);
        range4.setNumberOfRecurrences(3);
        range4.setType("Numbered");
        recurrence4.setRange(range4);
        settings4.setStart("2023-01-01T00:00:00+08:00");
        settings4.setEnd("2023-01-01T00:00:01+08:00");
        settings4.setRecurrence(recurrence4);
        testDataList.add(new EvaluationTestData(now4, settings4, false));


        // 29th every 2 months & match endDate
        final ZonedDateTime now5 = ZonedDateTime.parse("2024-02-29T00:00:00+08:00");
        final TimeWindowFilterSettings settings5 = new TimeWindowFilterSettings();
        final RecurrencePattern pattern5 = new RecurrencePattern();
        final RecurrenceRange range5 = new RecurrenceRange();
        final Recurrence recurrence5 = new Recurrence();
        pattern5.setType("AbsoluteMonthly");
        pattern5.setDayOfMonth(29);
        pattern5.setInterval(2);
        recurrence5.setPattern(pattern5);
        range5.setEndDate("2024-02-29T00:00:00+08:00");
        range5.setType("EndDate");
        recurrence5.setRange(range5);
        settings5.setStart("2023-04-29T00:00:00+08:00");
        settings5.setEnd("2023-04-29T00:00:01+08:00");
        settings5.setRecurrence(recurrence5);
        testDataList.add(new EvaluationTestData(now5, settings5, true));

        // 29th every month & not match endDate
        final ZonedDateTime now6 = ZonedDateTime.parse("2023-10-29T00:00:00+08:00");
        final TimeWindowFilterSettings settings6 = new TimeWindowFilterSettings();
        final RecurrencePattern pattern6 = new RecurrencePattern();
        final RecurrenceRange range6 = new RecurrenceRange();
        final Recurrence recurrence6 = new Recurrence();
        pattern6.setType("AbsoluteMonthly");
        pattern6.setDayOfMonth(29);
        recurrence6.setPattern(pattern6);
        range6.setEndDate("2023-10-28T00:00:00+08:00");
        range6.setType("EndDate");
        recurrence6.setRange(range6);
        settings6.setStart("2023-09-29T00:00:00+08:00");
        settings6.setEnd("2023-09-29T00:00:01+08:00");
        settings6.setRecurrence(recurrence6);
        testDataList.add(new EvaluationTestData(now6, settings6, false));

        consumeEvaluationTestData(testDataList);
    }


    private void consumeEvaluationTestData(List<EvaluationTestData> testDataList) {
        for (EvaluationTestData testData: testDataList) {
            assertEquals(RecurrenceEvaluator.matchRecurrence(testData.now, testData.settings), testData.isEnabled);
        }
    }

    private static class EvaluationTestData {
        private final ZonedDateTime now;
        private final TimeWindowFilterSettings settings;
        private final boolean isEnabled;
        EvaluationTestData(ZonedDateTime now, TimeWindowFilterSettings settings, boolean isEnabled) {
            this.settings = settings;
            this.now = now;
            this.isEnabled = isEnabled;
        }
    }
}
