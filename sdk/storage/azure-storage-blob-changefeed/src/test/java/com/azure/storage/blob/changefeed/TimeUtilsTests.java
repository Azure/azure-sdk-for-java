// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.changefeed.implementation.util.TimeUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimeUtilsTests {
    @ParameterizedTest
    @MethodSource("convertPathToTimeSupplier")
    public void convertPathToTime(String path, OffsetDateTime time) {
        assertEquals(time, TimeUtils.convertPathToTime(path));
    }

    private static Stream<Arguments> convertPathToTimeSupplier() {
        // path || time
        return Stream.of(Arguments.of(null, null),
            Arguments.of("idx/segments/2019", OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)),
            Arguments.of("idx/segments/2019/", OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)),
            Arguments.of("idx/segments/2019/11", OffsetDateTime.of(2019, 11, 1, 0, 0, 0, 0, ZoneOffset.UTC)),
            Arguments.of("idx/segments/2019/11/", OffsetDateTime.of(2019, 11, 1, 0, 0, 0, 0, ZoneOffset.UTC)),
            Arguments.of("idx/segments/2019/11/02", OffsetDateTime.of(2019, 11, 2, 0, 0, 0, 0, ZoneOffset.UTC)),
            Arguments.of("idx/segments/2019/11/02/", OffsetDateTime.of(2019, 11, 2, 0, 0, 0, 0, ZoneOffset.UTC)),
            Arguments.of("idx/segments/2019/11/02/1700", OffsetDateTime.of(2019, 11, 2, 17, 0, 0, 0, ZoneOffset.UTC)),
            Arguments.of("idx/segments/2019/11/02/1700/", OffsetDateTime.of(2019, 11, 2, 17, 0, 0, 0, ZoneOffset.UTC)),
            Arguments.of("idx/segments/2019/11/02/1700/meta.json",
                OffsetDateTime.of(2019, 11, 2, 17, 0, 0, 0, ZoneOffset.UTC)));
    }

    @ParameterizedTest
    @MethodSource("roundDownToNearestHourSupplier")
    public void roundDownToNearestHour(OffsetDateTime time, OffsetDateTime roundedTime) {
        assertEquals(roundedTime, TimeUtils.roundDownToNearestHour(time));
    }

    private static Stream<Arguments> roundDownToNearestHourSupplier() {
        // time || roundedTime
        return Stream.of(Arguments.of(null, null),
            Arguments.of(OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)),
            Arguments.of(OffsetDateTime.of(2020, 3, 17, 20, 25, 30, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2020, 3, 17, 20, 0, 0, 0, ZoneOffset.UTC)));
    }

    @ParameterizedTest
    @MethodSource("roundUpToNearestHourSupplier")
    public void roundUpToNearestHour(OffsetDateTime time, OffsetDateTime roundedTime) {
        assertEquals(roundedTime, TimeUtils.roundUpToNearestHour(time));
    }

    private static Stream<Arguments> roundUpToNearestHourSupplier() {
        // time || roundedTime
        return Stream.of(Arguments.of(null, null),
            Arguments.of(OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)),
            Arguments.of(OffsetDateTime.of(2020, 3, 17, 20, 25, 30, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2020, 3, 17, 21, 0, 0, 0, ZoneOffset.UTC)),
            Arguments.of(OffsetDateTime.of(2020, 3, 17, 23, 25, 30, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2020, 3, 18, 0, 0, 0, 0, ZoneOffset.UTC)));
    }

    @ParameterizedTest
    @MethodSource("roundDownToNearestYearSupplier")
    public void roundDownToNearestYear(OffsetDateTime time, OffsetDateTime roundedTime) {
        assertEquals(roundedTime, TimeUtils.roundDownToNearestYear(time));
    }

    private static Stream<Arguments> roundDownToNearestYearSupplier() {
        // time || roundedTime
        return Stream.of(Arguments.of(null, null),
            Arguments.of(OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)),
            Arguments.of(OffsetDateTime.of(2020, 3, 17, 20, 25, 30, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)));
    }

    @ParameterizedTest
    @MethodSource("validSegmentSupplier")
    public void validSegment(OffsetDateTime start, String segment, OffsetDateTime end, boolean valid) {
        assertEquals(valid, TimeUtils.validSegment(segment, start, end));
    }

    private static Stream<Arguments> validSegmentSupplier() {
        // start || segment || end || valid
        return Stream.of(
            // Null checks
            Arguments.of(null, null, null, false),
            Arguments.of(OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), null, null, false),
            Arguments.of(null, "idx/segments/2019/11/02/1700/meta.json", null, false),
            Arguments.of(null, null, OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), false),

            // All equal, not valid since end time is exclusive
            Arguments.of(OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                "idx/segments/2019/01/01/0000/meta.json", OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                false),

            // Increasing
            Arguments.of(OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                "idx/segments/2019/01/01/0000/meta.json", OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                true),
            Arguments.of(OffsetDateTime.of(2019, 3, 17, 20, 25, 30, 0, ZoneOffset.UTC),
                "idx/segments/2019/06/01/0000/meta.json", OffsetDateTime.of(2019, 8, 10, 0, 0, 0, 0, ZoneOffset.UTC),
                true),

            // Decreasing
            Arguments.of(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                "idx/segments/2020/01/01/0000/meta.json", OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                false),
            Arguments.of(OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                "idx/segments/2020/01/01/0000/meta.json", OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                false));
    }

    @ParameterizedTest
    @MethodSource("validYearSupplier")
    public void validYear(OffsetDateTime start, String year, OffsetDateTime end, boolean valid) {
        assertEquals(valid, TimeUtils.validYear(year, start, end));
    }

    private static Stream<Arguments> validYearSupplier() {
        // start || year || end || valid
        return Stream.of(
            // Null checks
            Arguments.of(null, null, null, false),
            Arguments.of(OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), null, null, false),
            Arguments.of(null, "idx/segments/2019", null, false),
            Arguments.of(null, null, OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), false),

            // All equal
            Arguments.of(OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), "idx/segments/2019",
                OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), true),

            // Increasing
            Arguments.of(OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), "idx/segments/2019",
                OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), true),
            Arguments.of(OffsetDateTime.of(2019, 3, 17, 20, 25, 30, 0, ZoneOffset.UTC), "idx/segments/2019",
                OffsetDateTime.of(2019, 8, 10, 0, 0, 0, 0, ZoneOffset.UTC), true),

            // Decreasing
            Arguments.of(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), "idx/segments/2020",
                OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), false),
            Arguments.of(OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), "idx/segments/2020",
                OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), false));
    }
}
