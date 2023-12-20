// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.timezone;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.models.GeoPosition;
import com.azure.maps.timezone.models.TimeZoneCoordinateOptions;
import com.azure.maps.timezone.models.TimeZoneIdOptions;
import com.azure.maps.timezone.models.TimeZoneOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimeZoneAsyncClientTest extends TimeZoneClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private TimeZoneAsyncClient getTimeZoneAsyncClient(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        return getTimeZoneAsyncClientBuilder(httpClient, serviceVersion).buildAsyncClient();
    }

    // Test async get timezone by id
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testAsyncGetDataForPoints(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        TimeZoneAsyncClient client = getTimeZoneAsyncClient(httpClient, serviceVersion);
        TimeZoneIdOptions options = new TimeZoneIdOptions("Asia/Bahrain").setOptions(TimeZoneOptions.ALL).setLanguage(null)
            .setTimestamp(null).setDaylightSavingsTime(null).setDaylightSavingsTimeLastingYears(null);
        StepVerifier.create(client.getTimezoneById(options))
            .assertNext(actualResults -> {
                try {
                    validateGetTimezoneById(TestUtils.getExpectedTimezoneById(), actualResults);
                } catch (IOException e) {
                    Assertions.fail("Unable to get timezone by id");
                }
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get timezone by id with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testAsyncGetDataForPointsWithResponse(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        TimeZoneAsyncClient client = getTimeZoneAsyncClient(httpClient, serviceVersion);
        TimeZoneIdOptions options = new TimeZoneIdOptions("Asia/Bahrain").setOptions(TimeZoneOptions.ALL).setLanguage(null)
            .setTimestamp(null).setDaylightSavingsTime(null).setDaylightSavingsTimeLastingYears(null);
        StepVerifier.create(client.getTimezoneByIdWithResponse(options, null))
            .assertNext(response -> {
                try {
                    validateGetTimezoneByIdWithResponse(TestUtils.getExpectedTimezoneById(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("Unable to get timezone by id");
                }
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testAsyncInvalidGetDataForPointsWithResponse(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        TimeZoneAsyncClient client = getTimeZoneAsyncClient(httpClient, serviceVersion);
        TimeZoneIdOptions options = new TimeZoneIdOptions("").setOptions(TimeZoneOptions.ALL).setLanguage(null)
            .setTimestamp(null).setDaylightSavingsTime(null).setDaylightSavingsTimeLastingYears(null);
        StepVerifier.create(client.getTimezoneByIdWithResponse(options, null))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            })
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get timezone by coordinates
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testAsyncGetTimezoneByCoordinates(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        TimeZoneAsyncClient client = getTimeZoneAsyncClient(httpClient, serviceVersion);
        GeoPosition coordinate = new GeoPosition(-122, 47.0);
        TimeZoneCoordinateOptions options = new TimeZoneCoordinateOptions(coordinate).setTimezoneOptions(TimeZoneOptions.ALL);
        StepVerifier.create(client.getTimezoneByCoordinates(options))
            .assertNext(actualResults -> {
                try {
                    validateGetTimezoneByCoordinates(TestUtils.getExpectedTimezoneByCoordinates(), actualResults);
                } catch (IOException e) {
                    Assertions.fail("Unable to get timezone by coordinates");
                }
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get timezone by coordinates with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testAsyncGetTimezoneByCoordinatesWithResponse(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        TimeZoneAsyncClient client = getTimeZoneAsyncClient(httpClient, serviceVersion);
        GeoPosition coordinate = new GeoPosition(-122, 47.0);
        TimeZoneCoordinateOptions options = new TimeZoneCoordinateOptions(coordinate).setTimezoneOptions(TimeZoneOptions.ALL);
        StepVerifier.create(client.getTimezoneByCoordinatesWithResponse(options, null))
            .assertNext(response -> {
                try {
                    validateGetTimezoneByCoordinatesWithResponse(TestUtils.getExpectedTimezoneByCoordinates(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("Unable to get timezone by coordinates");
                }
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testInvalidAsyncGetTimezoneByCoordinatesWithResponse(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        TimeZoneAsyncClient client = getTimeZoneAsyncClient(httpClient, serviceVersion);
        GeoPosition coordinate = new GeoPosition(-10000, 47.0);
        TimeZoneCoordinateOptions options = new TimeZoneCoordinateOptions(coordinate).setTimezoneOptions(TimeZoneOptions.ALL);
        StepVerifier.create(client.getTimezoneByCoordinatesWithResponse(options, null))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            })
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get windows timezone ids
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testAsyncGetWindowsTimezoneIds(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        TimeZoneAsyncClient client = getTimeZoneAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getWindowsTimezoneIds())
            .assertNext(actualResults -> {
                try {
                    validateGetWindowsTimezoneIds(TestUtils.getExpectedWindowsTimezoneIds(), actualResults);
                } catch (IOException e) {
                    Assertions.fail("Unable to get windows timezone ids");
                }
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get windows timezone ids with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testInvalidAsyncGetWindowsTimezoneIds(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        TimeZoneAsyncClient client = getTimeZoneAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getWindowsTimezoneIdsWithResponse(null))
            .assertNext(response -> {
                try {
                    validateGetWindowsTimezoneIdsWithResponse(TestUtils.getExpectedWindowsTimezoneIds(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("Unable to get windows timezone ids");
                }
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get iana timezone ids
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testAsyncGetIanaTimezoneIds(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        TimeZoneAsyncClient client = getTimeZoneAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getIanaTimezoneIds())
            .assertNext(actualResults -> {
                try {
                    validateGetIanaTimezoneIds(TestUtils.getExpectedIanaTimezoneIds(), actualResults);
                } catch (IOException e) {
                    Assertions.fail("Unable to get iana timezone ids");
                }
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get iana timezone ids with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testGetIanaTimezoneIdsWithResponseWithResponse(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        TimeZoneAsyncClient client = getTimeZoneAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getIanaTimezoneIdsWithResponse(null))
            .assertNext(response -> {
                try {
                    validateGetIanaTimezoneIdsWithResponse(TestUtils.getExpectedIanaTimezoneIds(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("Unable to get iana timezone ids");
                }
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get iana version
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testAsyncGetIanaVersion(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        TimeZoneAsyncClient client = getTimeZoneAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getIanaVersion())
            .assertNext(actualResults -> {
                try {
                    validateGetIanaVersion(TestUtils.getExpectedIanaVersion(), actualResults);
                } catch (IOException e) {
                    Assertions.fail("Unable to get iana version");
                }
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get iana version with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testAsyncGetIanaVersionWithResponse(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        TimeZoneAsyncClient client = getTimeZoneAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getIanaVersionWithResponse(null))
            .assertNext(response -> {
                try {
                    validateGetIanaVersionWithResponse(TestUtils.getExpectedIanaVersion(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("Unable to get iana version");
                }
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get convert windows timezone to iana
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testAsyncGetConvertWindowsTimezoneToIana(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        TimeZoneAsyncClient client = getTimeZoneAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.convertWindowsTimezoneToIana("pacific standard time", null))
            .assertNext(actualResults -> {
                try {
                    validateConvertWindowsTimezoneToIana(TestUtils.getExpectedConvertWindowsTimezoneToIana(), actualResults);
                } catch (IOException e) {
                    Assertions.fail("Unable to get convert windows timezone to iana");
                }
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get convert windows timezone to iana with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testAsyncGetConvertWindowsTimezoneToIanaWithResponse(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        TimeZoneAsyncClient client = getTimeZoneAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.convertWindowsTimezoneToIanaWithResponse("pacific standard time", null, null))
            .assertNext(response -> {
                try {
                    validateConvertWindowsTimezoneToIanaWithResponse(TestUtils.getExpectedConvertWindowsTimezoneToIana(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("Unable to get convert windows timezone to iana");
                }
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.timezone.TestUtils#getTestParameters")
    public void testAsyncInvalidGetConvertWindowsTimezoneToIanaWithResponse(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        TimeZoneAsyncClient client = getTimeZoneAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.convertWindowsTimezoneToIanaWithResponse("", null, null))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            })
            .verify(DEFAULT_TIMEOUT);
    }
}
