// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.timezone;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.time.Duration;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.models.GeoPosition;
import com.azure.maps.timezone.models.TimezoneCoordinateOptions;
import com.azure.maps.timezone.models.TimezoneIdOptions;
import com.azure.maps.timezone.models.TimezoneOptions;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import reactor.test.StepVerifier;

public class TimezoneAsyncClientTest extends TimezoneClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    @BeforeAll
    public static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    public static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    private TimezoneAsyncClient getTimezoneAsyncClient(HttpClient httpClient, TimezoneServiceVersion serviceVersion) {
        return getTimezoneAsyncClientBuilder(httpClient, serviceVersion).buildAsyncClient();
    }

    // Test async get timezone by id
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testAsyncGetDataForPoints(HttpClient httpClient, TimezoneServiceVersion serviceVersion) throws IOException {
        TimezoneAsyncClient client = getTimezoneAsyncClient(httpClient, serviceVersion);
        TimezoneIdOptions options = new TimezoneIdOptions("Asia/Bahrain").setOptions(TimezoneOptions.ALL).setLanguage(null)
            .setTimestamp(null).setDaylightSavingsTime(null).setDaylightSavingsTimeLastingYears(null);
        StepVerifier.create(client.getTimezoneById(options))
            .assertNext(actualResults -> {
                try {
                    validateGetTimezoneById(TestUtils.getExpectedTimezoneById(), actualResults);
                } catch (IOException e) {
                    Assertions.fail("Unable to get timezone by id");
                }
            }).verifyComplete();
    }

    // Test async get timezone by id with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testAsyncGetDataForPointsWithResponse(HttpClient httpClient, TimezoneServiceVersion serviceVersion) {
        TimezoneAsyncClient client = getTimezoneAsyncClient(httpClient, serviceVersion);
        TimezoneIdOptions options = new TimezoneIdOptions("Asia/Bahrain").setOptions(TimezoneOptions.ALL).setLanguage(null)
            .setTimestamp(null).setDaylightSavingsTime(null).setDaylightSavingsTimeLastingYears(null);
        StepVerifier.create(client.getTimezoneByIdWithResponse(options, null))
            .assertNext(response -> {
                try {
                    validateGetTimezoneByIdWithResponse(TestUtils.getExpectedTimezoneById(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("Unable to get timezone by id");
                }
            }).verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testAsyncInvalidGetDataForPointsWithResponse(HttpClient httpClient, TimezoneServiceVersion serviceVersion) {
        TimezoneAsyncClient client = getTimezoneAsyncClient(httpClient, serviceVersion);
        TimezoneIdOptions options = new TimezoneIdOptions("").setOptions(TimezoneOptions.ALL).setLanguage(null)
            .setTimestamp(null).setDaylightSavingsTime(null).setDaylightSavingsTimeLastingYears(null);
        StepVerifier.create(client.getTimezoneByIdWithResponse(options, null))
            .verifyErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            });
    }

    // Test async get timezone by coordinates
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testAsyncGetTimezoneByCoordinates(HttpClient httpClient, TimezoneServiceVersion serviceVersion) throws IOException {
        TimezoneAsyncClient client = getTimezoneAsyncClient(httpClient, serviceVersion);
        GeoPosition coordinate = new GeoPosition(-122, 47.0);
        TimezoneCoordinateOptions options = new TimezoneCoordinateOptions(coordinate).setTimezoneOptions(TimezoneOptions.ALL);
        StepVerifier.create(client.getTimezoneByCoordinates(options))
            .assertNext(actualResults -> {
                try {
                    validateGetTimezoneByCoordinates(TestUtils.getExpectedTimezoneByCoordinates(), actualResults);
                } catch (IOException e) {
                    Assertions.fail("Unable to get timezone by coordinates");
                }
            }).verifyComplete();
    }

    // Test async get timezone by coordinates with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testAsyncGetTimezoneByCoordinatesWithResponse(HttpClient httpClient, TimezoneServiceVersion serviceVersion) {
        TimezoneAsyncClient client = getTimezoneAsyncClient(httpClient, serviceVersion);
        GeoPosition coordinate = new GeoPosition(-122, 47.0);
        TimezoneCoordinateOptions options = new TimezoneCoordinateOptions(coordinate).setTimezoneOptions(TimezoneOptions.ALL);
        StepVerifier.create(client.getTimezoneByCoordinatesWithResponse(options, null))
            .assertNext(response -> {
                try {
                    validateGetTimezoneByCoordinatesWithResponse(TestUtils.getExpectedTimezoneByCoordinates(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("Unable to get timezone by coordinates");
                }
            }).verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testInvalidAsyncGetTimezoneByCoordinatesWithResponse(HttpClient httpClient, TimezoneServiceVersion serviceVersion) {
        TimezoneAsyncClient client = getTimezoneAsyncClient(httpClient, serviceVersion);
        GeoPosition coordinate = new GeoPosition(-10000, 47.0);
        TimezoneCoordinateOptions options = new TimezoneCoordinateOptions(coordinate).setTimezoneOptions(TimezoneOptions.ALL);
        StepVerifier.create(client.getTimezoneByCoordinatesWithResponse(options, null))
            .verifyErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            });
    }

    // Test async get windows timezone ids
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testAsyncGetWindowsTimezoneIds(HttpClient httpClient, TimezoneServiceVersion serviceVersion) throws IOException {
        TimezoneAsyncClient client = getTimezoneAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getWindowsTimezoneIds())
            .assertNext(actualResults -> {
                try {
                    validateGetWindowsTimezoneIds(TestUtils.getExpectedWindowsTimezoneIds(), actualResults);
                } catch (IOException e) {
                    Assertions.fail("Unable to get windows timezone ids");
                }
            }).verifyComplete();
    }

    // Test async get windows timezone ids with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testInvalidAsyncGetWindowsTimezoneIds(HttpClient httpClient, TimezoneServiceVersion serviceVersion) {
        TimezoneAsyncClient client = getTimezoneAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getWindowsTimezoneIdsWithResponse(null))
            .assertNext(response -> {
                try {
                    validateGetWindowsTimezoneIdsWithResponse(TestUtils.getExpectedWindowsTimezoneIds(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("Unable to get windows timezone ids");
                }
            }).verifyComplete();
    }

    // Test async get iana timezone ids
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testAsyncGetIanaTimezoneIds(HttpClient httpClient, TimezoneServiceVersion serviceVersion) throws IOException {
        TimezoneAsyncClient client = getTimezoneAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getIanaTimezoneIds())
            .assertNext(actualResults -> {
                try {
                    validateGetIanaTimezoneIds(TestUtils.getExpectedIanaTimezoneIds(), actualResults);
                } catch (IOException e) {
                    Assertions.fail("Unable to get iana timezone ids");
                }
            }).verifyComplete();
    }

    // Test async get iana timezone ids with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetIanaTimezoneIdsWithResponseWithResponse(HttpClient httpClient, TimezoneServiceVersion serviceVersion) {
        TimezoneAsyncClient client = getTimezoneAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getIanaTimezoneIdsWithResponse(null))
            .assertNext(response -> {
                try {
                    validateGetIanaTimezoneIdsWithResponse(TestUtils.getExpectedIanaTimezoneIds(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("Unable to get iana timezone ids");
                }
            }).verifyComplete();
    }

    // Test async get iana version 
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testAsyncGetIanaVersion(HttpClient httpClient, TimezoneServiceVersion serviceVersion) throws IOException {
        TimezoneAsyncClient client = getTimezoneAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getIanaVersion())
            .assertNext(actualResults -> {
                try {
                    validateGetIanaVersion(TestUtils.getExpectedIanaVersion(), actualResults);
                } catch (IOException e) {
                    Assertions.fail("Unable to get iana version");
                }
            }).verifyComplete();
    }

    // Test async get iana version with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testAsyncGetIanaVersionWithResponse(HttpClient httpClient, TimezoneServiceVersion serviceVersion) {
        TimezoneAsyncClient client = getTimezoneAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getIanaVersionWithResponse(null))
            .assertNext(response -> {
                try {
                    validateGetIanaVersionWithResponse(TestUtils.getExpectedIanaVersion(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("Unable to get iana version");
                }
            }).verifyComplete();
    }

    // Test async get convert windows timezone to iana
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testAsyncGetConvertWindowsTimezoneToIana(HttpClient httpClient, TimezoneServiceVersion serviceVersion) throws IOException {
        TimezoneAsyncClient client = getTimezoneAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.convertWindowsTimezoneToIana("pacific standard time", null))
            .assertNext(actualResults -> {
                try {
                    validateConvertWindowsTimezoneToIana(TestUtils.getExpectedConvertWindowsTimezoneToIana(), actualResults);
                } catch (IOException e) {
                    Assertions.fail("Unable to get convert windows timezone to iana");
                }
            }).verifyComplete();
    }

    // Test async get convert windows timezone to iana with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testAsyncGetConvertWindowsTimezoneToIanaWithResponse(HttpClient httpClient, TimezoneServiceVersion serviceVersion) {
        TimezoneAsyncClient client = getTimezoneAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.convertWindowsTimezoneToIanaWithResponse("pacific standard time", null, null))
            .assertNext(response -> {
                try {
                    validateConvertWindowsTimezoneToIanaWithResponse(TestUtils.getExpectedConvertWindowsTimezoneToIana(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("Unable to get convert windows timezone to iana");
                }
            }).verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testAsyncInvalidGetConvertWindowsTimezoneToIanaWithResponse(HttpClient httpClient, TimezoneServiceVersion serviceVersion) {
        TimezoneAsyncClient client = getTimezoneAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.convertWindowsTimezoneToIanaWithResponse("", null, null))
            .verifyErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            });
    }
}
