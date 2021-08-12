// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.models;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link TimeInterval}.
 */
public class TimeIntervalTest {
    private static final OffsetDateTime REFERENCE_UTC_TIME = OffsetDateTime.now(ZoneOffset.UTC)
        .withDayOfMonth(1)
        .withMonth(7)
        .withYear(2021)
        .withSecond(0)
        .withMinute(0)
        .withHour(0)
        .withNano(0);

    private static final OffsetDateTime REFERENCE_NON_UTC_TIME = OffsetDateTime.now(ZoneOffset.ofHours(-7))
        .withDayOfMonth(1)
        .withMonth(7)
        .withYear(2021)
        .withSecond(0)
        .withMinute(0)
        .withHour(0)
        .withNano(0);

    @ParameterizedTest
    @MethodSource("toIso8601FormatSupplier")
    public void testToIso8601FormatSupplier(TimeInterval timeInterval, String expected) {
        assertEquals(expected, timeInterval.toIso8601Format());
    }

    private static Stream<Arguments> toIso8601FormatSupplier() {
        return Stream.of(
            Arguments.of(new TimeInterval(Duration.ofDays(1)), "PT24H"),

            Arguments.of(new TimeInterval(REFERENCE_UTC_TIME, REFERENCE_UTC_TIME.plusDays(5)),
                "2021-07-01T00:00Z/2021-07-06T00:00Z"),
            Arguments.of(new TimeInterval(REFERENCE_NON_UTC_TIME, REFERENCE_NON_UTC_TIME.plusDays(5)),
                "2021-07-01T00:00-07:00/2021-07-06T00:00-07:00"),

            Arguments.of(new TimeInterval(REFERENCE_UTC_TIME, Duration.ofDays(1)), "2021-07-01T00:00Z/PT24H"),
            Arguments.of(new TimeInterval(REFERENCE_NON_UTC_TIME, Duration.ofDays(1)), "2021-07-01T00:00-07:00/PT24H"),

            Arguments.of(new TimeInterval(Duration.ofDays(1), REFERENCE_UTC_TIME), "PT24H/2021-07-01T00:00Z"),
            Arguments.of(new TimeInterval(Duration.ofDays(1), REFERENCE_NON_UTC_TIME), "PT24H/2021-07-01T00:00-07:00")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTimeSpanSupplier")
    public void testInvalidTimeSpan(Executable timeIntervalCreator, Class<? extends Throwable> expectedException) {
        assertThrows(expectedException, timeIntervalCreator);
    }

    private static Stream<Arguments> invalidTimeSpanSupplier() {
        Function<Supplier<TimeInterval>, Executable> converter = supplier -> (Executable) supplier::get;

        return Stream.of(
            Arguments.of(converter.apply(() -> new TimeInterval(null)), NullPointerException.class),
            Arguments.of(converter.apply(() -> new TimeInterval(null, (Duration) null)), NullPointerException.class),
            Arguments.of(converter.apply(() -> new TimeInterval((OffsetDateTime) null, (OffsetDateTime) null)),
                NullPointerException.class),

            Arguments.of(converter.apply(() -> new TimeInterval((OffsetDateTime) null, OffsetDateTime.now())),
                NullPointerException.class),
            Arguments.of(converter.apply(() -> new TimeInterval(OffsetDateTime.now(), (OffsetDateTime) null)),
                NullPointerException.class),

            Arguments.of(converter.apply(() -> new TimeInterval(null, Duration.ofDays(2))), NullPointerException.class),
            Arguments.of(converter.apply(() -> new TimeInterval(OffsetDateTime.now(), (Duration) null)),
                NullPointerException.class),

            // empty string
            Arguments.of(converter.apply(() -> TimeInterval.parse("")), IllegalArgumentException.class),

            // no end time/duration
            Arguments.of(converter.apply(() -> TimeInterval.parse("2021-07-01T00:00Z/")),
                IllegalArgumentException.class),

            // no start time
            Arguments.of(converter.apply(() -> TimeInterval.parse("/2021-07-01T00:00Z")),
                IllegalArgumentException.class),

            // no range
            Arguments.of(converter.apply(() -> TimeInterval.parse("2021-07-01T00:00Z")),
                IllegalArgumentException.class),

            // invalid duration
            Arguments.of(converter.apply(() -> TimeInterval.parse("PT24")), IllegalArgumentException.class),

            // no end time
            Arguments.of(converter.apply(() -> TimeInterval.parse("PT24H/")), IllegalArgumentException.class),

            // start and end both are durations
            Arguments.of(converter.apply(() -> TimeInterval.parse("PT24H/PT48H")), IllegalArgumentException.class),

            // no start time
            Arguments.of(converter.apply(() -> TimeInterval.parse("/PT24H")), IllegalArgumentException.class),

            // invalid datetime
            Arguments.of(converter.apply(() -> TimeInterval.parse("2021-07-01T00:00Z/2021-07-01T00:00")),
                IllegalArgumentException.class)
        );
    }

    @ParameterizedTest
    @MethodSource("parseSupplier")
    @Execution(ExecutionMode.SAME_THREAD)
    public void testParse(String timeIntervalString, Function<TimeInterval, Object> getter1, Object expected1,
        Function<TimeInterval, Object> getter2, Object expected2) {
        TimeInterval timeInterval = TimeInterval.parse(timeIntervalString);

        assertEquals(expected1, getter1.apply(timeInterval));
        if (getter2 != null) {
            assertEquals(expected2, getter2.apply(timeInterval));
        }
    }

    private static Stream<Arguments> parseSupplier() {
        Function<TimeInterval, Object> getStartTime = TimeInterval::getStartTime;
        Function<TimeInterval, Object> getEndTime = TimeInterval::getEndTime;
        Function<TimeInterval, Object> getDuration = TimeInterval::getDuration;

        return Stream.of(
            Arguments.of("2021-07-01T00:00Z/2021-07-06T00:00Z", getStartTime, REFERENCE_UTC_TIME, getEndTime,
                REFERENCE_UTC_TIME.plusDays(5)),

            Arguments.of("PT24H/2021-07-01T00:00Z", getDuration, Duration.ofDays(1), getEndTime, REFERENCE_UTC_TIME),

            Arguments.of("2021-07-01T00:00Z/PT24H", getStartTime, REFERENCE_UTC_TIME, getDuration, Duration.ofDays(1)),

            Arguments.of("2021-07-01T00:00-07:00/2021-07-06T00:00-07:00", getStartTime, REFERENCE_NON_UTC_TIME,
                getEndTime, REFERENCE_NON_UTC_TIME.plusDays(5)),

            Arguments.of("PT24H/2021-07-01T00:00-07:00", getDuration, Duration.ofDays(1), getEndTime,
                REFERENCE_NON_UTC_TIME),

            Arguments.of("2021-07-01T00:00-07:00/PT24H", getStartTime, REFERENCE_NON_UTC_TIME, getDuration,
                Duration.ofDays(1)),

            Arguments.of("PT24H", getDuration, Duration.ofDays(1), null, null)
        );
    }

    @ParameterizedTest
    @MethodSource("equalitySupplier")
    public void testEquality(String timeIntervalString, TimeInterval expected) {
        assertEquals(expected, TimeInterval.parse(timeIntervalString));
    }

    private static Stream<Arguments> equalitySupplier() {
        return Stream.of(
            Arguments.of("PT24H", TimeInterval.LAST_DAY),
            Arguments.of("PT48H", TimeInterval.LAST_2_DAYS),
            Arguments.of("PT24H", new TimeInterval(Duration.ofDays(1))),

            Arguments.of("2021-07-01T00:00Z/PT24H", new TimeInterval(REFERENCE_UTC_TIME, Duration.ofDays(1))),
            Arguments.of("PT24H/2021-07-01T00:00Z", new TimeInterval(Duration.ofDays(1), REFERENCE_UTC_TIME)),
            Arguments.of("2021-07-01T00:00Z/2021-07-03T00:00Z",
                new TimeInterval(REFERENCE_UTC_TIME, REFERENCE_UTC_TIME.plusDays(2))),

            Arguments.of("2021-07-01T00:00-07:00/PT24H", new TimeInterval(REFERENCE_NON_UTC_TIME, Duration.ofDays(1))),
            Arguments.of("PT24H/2021-07-01T00:00-07:00", new TimeInterval(Duration.ofDays(1), REFERENCE_NON_UTC_TIME)),
            Arguments.of("2021-07-01T00:00-07:00/2021-07-03T00:00-07:00",
                new TimeInterval(REFERENCE_NON_UTC_TIME, REFERENCE_NON_UTC_TIME.plusDays(2)))
        );
    }
}
