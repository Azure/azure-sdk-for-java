// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.timezone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.List;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.models.GeoPosition;
import com.azure.maps.timezone.models.IanaId;
import com.azure.maps.timezone.models.TimezoneCoordinateOptions;
import com.azure.maps.timezone.models.TimezoneIdOptions;
import com.azure.maps.timezone.models.TimezoneIanaVersionResult;
import com.azure.maps.timezone.models.TimezoneOptions;
import com.azure.maps.timezone.models.TimezoneResult;
import com.azure.maps.timezone.models.TimezoneWindows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class TimezoneClientTest extends TimezoneClientTestBase {
    private TimezoneClient client;
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    private TimezoneClient getTimezoneClient(HttpClient httpClient, TimezoneServiceVersion serviceVersion) {
        return getTimezoneAsyncClientBuilder(httpClient, serviceVersion).buildClient();
    }

    // Test get timezone by id
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetTimezoneById(HttpClient httpClient, TimezoneServiceVersion serviceVersion) throws IOException {
        client = getTimezoneClient(httpClient, serviceVersion);
        TimezoneIdOptions options = new TimezoneIdOptions("Asia/Bahrain").setOptions(TimezoneOptions.ALL).setLanguage(null)
            .setTimestamp(null).setDaylightSavingsTime(null).setDaylightSavingsTimeLastingYears(null);
        TimezoneResult actualResult = client.getTimezoneById(options);
        TimezoneResult expectedResult = TestUtils.getExpectedTimezoneById();
        validateGetTimezoneById(actualResult, expectedResult);
    }
    

    // Test get timezone by id with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetTimezoneByIdWithResponse(HttpClient httpClient, TimezoneServiceVersion serviceVersion) throws IOException {
        client = getTimezoneClient(httpClient, serviceVersion);
        TimezoneIdOptions options = new TimezoneIdOptions("Asia/Bahrain").setOptions(TimezoneOptions.ALL).setLanguage(null)
            .setTimestamp(null).setDaylightSavingsTime(null).setDaylightSavingsTimeLastingYears(null);
        validateGetTimezoneByIdWithResponse(TestUtils.getExpectedTimezoneById(), 200, client.getTimezoneByIdWithResponse(options, null));
    }

    // Case 2: Respone 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testInvalidGetTimezoneByIdWithResponse(HttpClient httpClient, TimezoneServiceVersion serviceVersion) throws IOException {
        client = getTimezoneClient(httpClient, serviceVersion);
        TimezoneIdOptions options = new TimezoneIdOptions("").setOptions(TimezoneOptions.ALL).setLanguage(null)
            .setTimestamp(null).setDaylightSavingsTime(null).setDaylightSavingsTimeLastingYears(null);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.getTimezoneById(options));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test get timezone by coordinates 
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetTimezoneByCoordinates(HttpClient httpClient, TimezoneServiceVersion serviceVersion) throws IOException {
        client = getTimezoneClient(httpClient, serviceVersion);
        GeoPosition coordinate = new GeoPosition(-122, 47.0);
        TimezoneCoordinateOptions options = new TimezoneCoordinateOptions(coordinate).setTimezoneOptions(TimezoneOptions.ALL);
        TimezoneResult actualResult = client.getTimezoneByCoordinates(options);
        TimezoneResult expectedResult = TestUtils.getExpectedTimezoneByCoordinates();
        validateGetTimezoneByCoordinates(actualResult, expectedResult);
    }

    // Test get timezone by coordinates with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetTimezoneByCoordinatesWithResponse(HttpClient httpClient, TimezoneServiceVersion serviceVersion) throws IOException {
        client = getTimezoneClient(httpClient, serviceVersion);
        GeoPosition coordinate = new GeoPosition(-122, 47.0);
        TimezoneCoordinateOptions options = new TimezoneCoordinateOptions(coordinate).setTimezoneOptions(TimezoneOptions.ALL);
        validateGetTimezoneByCoordinatesWithResponse(TestUtils.getExpectedTimezoneByCoordinates(), 200, client.getTimezoneByCoordinatesWithResponse(options, null));
    }

    // Case 2: Respone 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testInvalidGetTimezoneByCoordinatesWithResponse(HttpClient httpClient, TimezoneServiceVersion serviceVersion) throws IOException {
        client = getTimezoneClient(httpClient, serviceVersion);
        GeoPosition coordinate = new GeoPosition(-1000000, 47.0);
        TimezoneCoordinateOptions options = new TimezoneCoordinateOptions(coordinate).setTimezoneOptions(TimezoneOptions.ALL);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.getTimezoneByCoordinatesWithResponse(options, null));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test get windows timezone ids
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetWindowsTimezoneIds(HttpClient httpClient, TimezoneServiceVersion serviceVersion) throws IOException {
        client = getTimezoneClient(httpClient, serviceVersion);
        List<TimezoneWindows> actualResult = client.getWindowsTimezoneIds();
        List<TimezoneWindows> expectedResult = TestUtils.getExpectedWindowsTimezoneIds();
        validateGetWindowsTimezoneIds(actualResult, expectedResult);
    }

    // Test get windows timezone ids with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetWindowsTimezoneIdsWithResponse(HttpClient httpClient, TimezoneServiceVersion serviceVersion) throws IOException {
        client = getTimezoneClient(httpClient, serviceVersion);
        validateGetWindowsTimezoneIdsWithResponse(TestUtils.getExpectedWindowsTimezoneIds(), 200, client.getWindowsTimezoneIdsWithResponse(null));
    }

    // Test get iana timezone ids
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetIanaTimezoneIds(HttpClient httpClient, TimezoneServiceVersion serviceVersion) throws IOException {
        client = getTimezoneClient(httpClient, serviceVersion);
        List<IanaId> actualResult = client.getIanaTimezoneIds();
        List<IanaId> expectedResult = TestUtils.getExpectedIanaTimezoneIds();
        validateGetIanaTimezoneIds(actualResult, expectedResult);
    }

    // Test get iana timezone ids with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetIanaTimezoneIdsWithResponse(HttpClient httpClient, TimezoneServiceVersion serviceVersion) throws IOException {
        client = getTimezoneClient(httpClient, serviceVersion);
        validateGetIanaTimezoneIdsWithResponse(TestUtils.getExpectedIanaTimezoneIds(), 200, client.getIanaTimezoneIdsWithResponse(null));
    }

    // Test get iana version
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetIanaVersion(HttpClient httpClient, TimezoneServiceVersion serviceVersion) throws IOException {
        client = getTimezoneClient(httpClient, serviceVersion);
        TimezoneIanaVersionResult actualResult = client.getIanaVersion();
        TimezoneIanaVersionResult expectedResult = TestUtils.getExpectedIanaVersion();
        validateGetIanaVersion(actualResult, expectedResult);
    }

    // Test get iana version with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetIanaVersionWithResponse(HttpClient httpClient, TimezoneServiceVersion serviceVersion) throws IOException {
        client = getTimezoneClient(httpClient, serviceVersion);
        validateGetIanaVersionWithResponse(TestUtils.getExpectedIanaVersion(), 200, client.getIanaVersionWithResponse(null));
    }

    // Test convert windows timezone to iana
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetConvertWindowsTimezoneToIana(HttpClient httpClient, TimezoneServiceVersion serviceVersion) throws IOException {
        client = getTimezoneClient(httpClient, serviceVersion);
        List<IanaId> actualResult = client.convertWindowsTimezoneToIana("pacific standard time", null);
        List<IanaId> expectedResult = TestUtils.getExpectedConvertWindowsTimezoneToIana();
        validateConvertWindowsTimezoneToIana(actualResult, expectedResult);
    }

    // Test convert windows timezone to iana with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetConvertWindowsTimezoneToIanaWithResponse(HttpClient httpClient, TimezoneServiceVersion serviceVersion) throws IOException {
        client = getTimezoneClient(httpClient, serviceVersion);
        validateConvertWindowsTimezoneToIanaWithResponse(TestUtils.getExpectedConvertWindowsTimezoneToIana(), 200, client.convertWindowsTimezoneToIanaWithResponse("pacific standard time", null, null));
    }

    // Case 2: Respone 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testInvalidGetConvertWindowsTimezoneToIanaWithResponse(HttpClient httpClient, TimezoneServiceVersion serviceVersion) throws IOException {
        client = getTimezoneClient(httpClient, serviceVersion);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.convertWindowsTimezoneToIanaWithResponse("", null, null));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }
}
