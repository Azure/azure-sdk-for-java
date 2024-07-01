// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.timezone;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.models.GeoPosition;
import com.azure.maps.timezone.models.IanaId;
import com.azure.maps.timezone.models.TimeZoneCoordinateOptions;
import com.azure.maps.timezone.models.TimeZoneIanaVersionResult;
import com.azure.maps.timezone.models.TimeZoneIdOptions;
import com.azure.maps.timezone.models.TimeZoneOptions;
import com.azure.maps.timezone.models.TimeZoneResult;
import com.azure.maps.timezone.models.TimeZoneWindows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TimeZoneClientTest extends TimeZoneClientTestBase {
    private TimeZoneClient client;
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    private TimeZoneClient getTimeZoneClient(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        return getTimeZoneAsyncClientBuilder(httpClient, serviceVersion).buildClient();
    }

    // Test get timezone by id
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetTimezoneById(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        client = getTimeZoneClient(httpClient, serviceVersion);
        TimeZoneIdOptions options = new TimeZoneIdOptions("Asia/Bahrain").setOptions(TimeZoneOptions.ALL)
            .setLanguage(null)
            .setTimestamp(null)
            .setDaylightSavingsTime(null)
            .setDaylightSavingsTimeLastingYears(null);
        TimeZoneResult actualResult = client.getTimezoneById(options);
        TimeZoneResult expectedResult = TestUtils.getExpectedTimezoneById();
        validateGetTimezoneById(actualResult, expectedResult);
    }

    // Test get timezone by id with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetTimezoneByIdWithResponse(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        client = getTimeZoneClient(httpClient, serviceVersion);
        TimeZoneIdOptions options = new TimeZoneIdOptions("Asia/Bahrain").setOptions(TimeZoneOptions.ALL)
            .setLanguage(null)
            .setTimestamp(null)
            .setDaylightSavingsTime(null)
            .setDaylightSavingsTimeLastingYears(null);
        validateGetTimezoneByIdWithResponse(TestUtils.getExpectedTimezoneById(),
            client.getTimezoneByIdWithResponse(options, null));
    }

    // Case 2: Response 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testInvalidGetTimezoneByIdWithResponse(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        client = getTimeZoneClient(httpClient, serviceVersion);
        TimeZoneIdOptions options = new TimeZoneIdOptions("").setOptions(TimeZoneOptions.ALL)
            .setLanguage(null)
            .setTimestamp(null)
            .setDaylightSavingsTime(null)
            .setDaylightSavingsTimeLastingYears(null);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.getTimezoneById(options));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test get timezone by coordinates
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetTimezoneByCoordinates(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        client = getTimeZoneClient(httpClient, serviceVersion);
        GeoPosition coordinate = new GeoPosition(-122, 47.0);
        TimeZoneCoordinateOptions options = new TimeZoneCoordinateOptions(coordinate).setTimezoneOptions(
            TimeZoneOptions.ALL);
        TimeZoneResult actualResult = client.getTimezoneByCoordinates(options);
        TimeZoneResult expectedResult = TestUtils.getExpectedTimezoneByCoordinates();
        validateGetTimezoneByCoordinates(actualResult, expectedResult);
    }

    // Test get timezone by coordinates with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetTimezoneByCoordinatesWithResponse(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        client = getTimeZoneClient(httpClient, serviceVersion);
        GeoPosition coordinate = new GeoPosition(-122, 47.0);
        TimeZoneCoordinateOptions options = new TimeZoneCoordinateOptions(coordinate).setTimezoneOptions(
            TimeZoneOptions.ALL);
        validateGetTimezoneByCoordinatesWithResponse(TestUtils.getExpectedTimezoneByCoordinates(),
            client.getTimezoneByCoordinatesWithResponse(options, null));
    }

    // Case 2: Response 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testInvalidGetTimezoneByCoordinatesWithResponse(HttpClient httpClient,
        TimeZoneServiceVersion serviceVersion) {
        client = getTimeZoneClient(httpClient, serviceVersion);
        GeoPosition coordinate = new GeoPosition(-1000000, 47.0);
        TimeZoneCoordinateOptions options = new TimeZoneCoordinateOptions(coordinate).setTimezoneOptions(
            TimeZoneOptions.ALL);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.getTimezoneByCoordinatesWithResponse(options, null));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test get windows timezone ids
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetWindowsTimezoneIds(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        client = getTimeZoneClient(httpClient, serviceVersion);
        List<TimeZoneWindows> actualResult = client.getWindowsTimezoneIds();
        List<TimeZoneWindows> expectedResult = TestUtils.getExpectedWindowsTimezoneIds();
        validateGetWindowsTimezoneIds(actualResult, expectedResult);
    }

    // Test get windows timezone ids with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetWindowsTimezoneIdsWithResponse(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        client = getTimeZoneClient(httpClient, serviceVersion);
        validateGetWindowsTimezoneIdsWithResponse(TestUtils.getExpectedWindowsTimezoneIds(),
            client.getWindowsTimezoneIdsWithResponse(null));
    }

    // Test get iana timezone ids
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetIanaTimezoneIds(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        client = getTimeZoneClient(httpClient, serviceVersion);
        List<IanaId> actualResult = client.getIanaTimezoneIds();
        List<IanaId> expectedResult = TestUtils.getExpectedIanaTimezoneIds();
        validateGetIanaTimezoneIds(actualResult, expectedResult);
    }

    // Test get iana timezone ids with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetIanaTimezoneIdsWithResponse(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        client = getTimeZoneClient(httpClient, serviceVersion);
        validateGetIanaTimezoneIdsWithResponse(TestUtils.getExpectedIanaTimezoneIds(),
            client.getIanaTimezoneIdsWithResponse(null));
    }

    // Test get iana version
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetIanaVersion(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        client = getTimeZoneClient(httpClient, serviceVersion);
        TimeZoneIanaVersionResult actualResult = client.getIanaVersion();
        TimeZoneIanaVersionResult expectedResult = TestUtils.getExpectedIanaVersion();
        validateGetIanaVersion(actualResult, expectedResult);
    }

    // Test get iana version with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetIanaVersionWithResponse(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        client = getTimeZoneClient(httpClient, serviceVersion);
        validateGetIanaVersionWithResponse(TestUtils.getExpectedIanaVersion(), client.getIanaVersionWithResponse(null));
    }

    // Test convert windows timezone to iana
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetConvertWindowsTimezoneToIana(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        client = getTimeZoneClient(httpClient, serviceVersion);
        List<IanaId> actualResult = client.convertWindowsTimezoneToIana("pacific standard time", null);
        List<IanaId> expectedResult = TestUtils.getExpectedConvertWindowsTimezoneToIana();
        validateConvertWindowsTimezoneToIana(actualResult, expectedResult);
    }

    // Test convert windows timezone to iana with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetConvertWindowsTimezoneToIanaWithResponse(HttpClient httpClient,
        TimeZoneServiceVersion serviceVersion) {
        client = getTimeZoneClient(httpClient, serviceVersion);
        validateConvertWindowsTimezoneToIanaWithResponse(TestUtils.getExpectedConvertWindowsTimezoneToIana(),
            client.convertWindowsTimezoneToIanaWithResponse("pacific standard time", null, null));
    }

    // Case 2: Response 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testInvalidGetConvertWindowsTimezoneToIanaWithResponse(HttpClient httpClient,
        TimeZoneServiceVersion serviceVersion) {
        client = getTimeZoneClient(httpClient, serviceVersion);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.convertWindowsTimezoneToIanaWithResponse("", null, null));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }
}
