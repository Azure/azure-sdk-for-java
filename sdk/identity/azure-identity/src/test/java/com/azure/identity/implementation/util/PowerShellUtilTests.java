// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class PowerShellUtilTests {

    // Test data for valid ISO timestamps
    static Stream<String> validISOTimestamps() {
        return Stream.of("2023-05-15T10:30:00Z", "2023-05-15T10:30:00.123Z", "2023-05-15T10:30:00+00:00",
            "2023-05-15T10:30:00-05:00", "2023-05-15T10:30:00.123456+02:00", "2023-12-31T23:59:59Z",
            "2000-01-01T00:00:00Z");
    }

    // Test data for valid .NET Date format
    static Stream<String> validDotNetDates() {
        return Stream.of("/Date(1684145400000)/",  // 2023-05-15 10:30:00 UTC
            "/Date(0)/",              // Unix epoch
            "/Date(1640995200000)/",  // 2022-01-01 00:00:00 UTC
            "/Date(253402300799000)/" // Very far future date
        );
    }

    // Test data for invalid inputs
    static Stream<String> invalidInputs() {
        return Stream.of("", "   ", "invalid-date", "/Date(/", "/Date())/", "/Date(abc)/", "/Date(123abc)/",
            "/Date(123.456)/", "/Date(-123)/", "/Date(123", "Date(123)/", "/Date(123)//", "2023-13-01T10:30:00Z",  // Invalid month
            "2023-05-32T10:30:00Z",  // Invalid day
            "not-a-date-at-all");
    }

    @Test
    public void testParseExpiresOnWithNull() {
        assertNull(PowerShellUtil.parseExpiresOn(null));
    }

    @Test
    public void testParseExpiresOnWithEmptyString() {
        assertNull(PowerShellUtil.parseExpiresOn(""));
    }

    @Test
    public void testParseExpiresOnWithWhitespace() {
        assertNull(PowerShellUtil.parseExpiresOn("   "));
    }

    @ParameterizedTest
    @MethodSource("validISOTimestamps")
    public void testParseExpiresOnWithValidISOTimestamps(String isoTimestamp) {
        OffsetDateTime result = PowerShellUtil.parseExpiresOn(isoTimestamp);

        assertNotNull(result, "Should parse valid ISO timestamp: " + isoTimestamp);
        assertEquals(ZoneOffset.UTC, result.getOffset(), "Result should be converted to UTC");

        // Verify it's a valid timestamp by converting back
        OffsetDateTime original = OffsetDateTime.parse(isoTimestamp);
        assertEquals(original.withOffsetSameInstant(ZoneOffset.UTC), result);
    }

    @ParameterizedTest
    @MethodSource("validDotNetDates")
    public void testParseExpiresOnWithValidDotNetDates(String dotNetDate) {
        OffsetDateTime result = PowerShellUtil.parseExpiresOn(dotNetDate);

        assertNotNull(result, "Should parse valid .NET date: " + dotNetDate);
        assertEquals(ZoneOffset.UTC, result.getOffset(), "Result should be in UTC");

        // Extract the epoch milliseconds and verify
        String digits = dotNetDate.substring(6, dotNetDate.length() - 2);
        long expectedEpochMs = Long.parseLong(digits);
        OffsetDateTime expected = OffsetDateTime.ofInstant(Instant.ofEpochMilli(expectedEpochMs), ZoneOffset.UTC);

        assertEquals(expected, result);
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    public void testParseExpiresOnWithInvalidInputs(String invalidInput) {
        OffsetDateTime result = PowerShellUtil.parseExpiresOn(invalidInput);
        assertNull(result, "Should return null for invalid input: " + invalidInput);
    }

    @Test
    public void testParseExpiresOnWithSpecificDotNetDate() {
        // Test a specific known date: 2023-05-15 10:10:00 UTC = 1684145400000 ms
        String dotNetDate = "/Date(1684145400000)/";
        OffsetDateTime result = PowerShellUtil.parseExpiresOn(dotNetDate);

        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(5, result.getMonthValue());
        assertEquals(15, result.getDayOfMonth());
        assertEquals(10, result.getHour());
        assertEquals(10, result.getMinute());
        assertEquals(0, result.getSecond());
        assertEquals(ZoneOffset.UTC, result.getOffset());
    }

    @Test
    public void testParseExpiresOnWithSpecificISODate() {
        // Test a specific ISO date with timezone conversion
        String isoDate = "2023-05-15T10:30:00-05:00"; // Eastern time
        OffsetDateTime result = PowerShellUtil.parseExpiresOn(isoDate);

        assertNotNull(result);
        assertEquals(ZoneOffset.UTC, result.getOffset());

        // Should be converted to 15:30 UTC (10:30 - 5 hours = 15:30 UTC)
        assertEquals(15, result.getHour());
        assertEquals(30, result.getMinute());
    }

    @Test
    public void testParseExpiresOnTriesISOFirst() {
        // Test that ISO parsing is attempted first by using a string that could be ambiguous
        String timestamp = "2023-05-15T10:30:00Z";
        OffsetDateTime result = PowerShellUtil.parseExpiresOn(timestamp);

        assertNotNull(result);
        // Verify it was parsed as ISO (not as .NET date format)
        assertEquals(2023, result.getYear());
        assertEquals(5, result.getMonthValue());
        assertEquals(15, result.getDayOfMonth());
    }

    @Test
    public void testParseExpiresOnWithDotNetDateContainingNonDigits() {
        // Test .NET date format with non-digit characters in the number part
        String invalidDotNetDate = "/Date(123abc456)/";
        OffsetDateTime result = PowerShellUtil.parseExpiresOn(invalidDotNetDate);

        assertNull(result, "Should return null when .NET date contains non-digits");
    }

    @Test
    public void testParseExpiresOnWithDotNetDateNumberFormatException() {
        // Test a .NET date that would cause NumberFormatException (too large number)
        String largeDotNetDate = "/Date(999999999999999999999)/";
        OffsetDateTime result = PowerShellUtil.parseExpiresOn(largeDotNetDate);

        assertNull(result, "Should return null when .NET date number is too large");
    }

    @Test
    public void testParseExpiresOnWithEpochZero() {
        // Test Unix epoch (January 1, 1970, 00:00:00 UTC)
        String epochDate = "/Date(0)/";
        OffsetDateTime result = PowerShellUtil.parseExpiresOn(epochDate);

        assertNotNull(result);
        assertEquals(1970, result.getYear());
        assertEquals(1, result.getMonthValue());
        assertEquals(1, result.getDayOfMonth());
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(0, result.getSecond());
        assertEquals(ZoneOffset.UTC, result.getOffset());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "/Date(/",
            "/Date()/",
            "Date(123)/",
            "/Date(123",
            "/Date(123)//",
            "/Date(123)/extra",
            "prefix/Date(123)/" })
    public void testParseExpiresOnWithMalformedDotNetDates(String malformedDate) {
        OffsetDateTime result = PowerShellUtil.parseExpiresOn(malformedDate);
        assertNull(result, "Should return null for malformed .NET date: " + malformedDate);
    }
}
