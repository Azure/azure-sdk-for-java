// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.weather;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.models.GeoPosition;
import com.azure.maps.weather.models.ActiveStorm;
import com.azure.maps.weather.models.ActiveStormResult;
import com.azure.maps.weather.models.DailyDuration;
import com.azure.maps.weather.models.HourlyDuration;
import com.azure.maps.weather.models.TropicalStormForecastOptions;
import com.azure.maps.weather.models.TropicalStormLocationOptions;
import com.azure.maps.weather.models.Waypoint;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WeatherAsyncClientTest extends WeatherTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private WeatherAsyncClient getWeatherAsyncClient(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        return getWeatherAsyncClientBuilder(httpClient, serviceVersion).buildAsyncClient();
    }

    // Test async get hourly forecast
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetHourlyForecast(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getHourlyForecast(new GeoPosition(-122.138874, 47.632346), null, 12, null))
            .assertNext(
                actualResults -> validateGetHourlyForecast(TestUtils.getExpectedHourlyForecast(), actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get hourly forecast with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetHourlyForecastWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(
                client.getHourlyForecastWithResponse(new GeoPosition(-122.138874, 47.632346), null, 12, null))
            .assertNext(
                response -> validateGetHourlyForecastWithResponse(TestUtils.getExpectedHourlyForecast(), response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetHourlyForecastWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getHourlyForecastWithResponse(new GeoPosition(-100000, 47.632346), null, 12, null))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            })
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get minute forecast
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetMinuteForecast(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getMinuteForecast(new GeoPosition(-122.138874, 47.632346), 15, null))
            .assertNext(
                actualResults -> validateGetMinuteForecast(TestUtils.getExpectedMinuteForecast(), actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get minute forecast with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetMinuteForecastWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(
                client.getMinuteForecastWithResponse(new GeoPosition(-122.138874, 47.632346), 15, null, null))
            .assertNext(
                response -> validateGetMinuteForecastWithResponse(TestUtils.getExpectedMinuteForecast(), response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetMinuteForecastWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getMinuteForecastWithResponse(new GeoPosition(-1000000, 47.632346), 15, null, null))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            })
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get quarter day forecast
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetQuarterDayForecast(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getQuarterDayForecast(new GeoPosition(-122.138874, 47.632346), null, 1, null))
            .assertNext(actualResults -> validateGetQuarterDayForecast(TestUtils.getExpectedQuarterDayForecast(),
                actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get quarter day forecast with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetQuarterDayForecastWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(
                client.getQuarterDayForecastWithResponse(new GeoPosition(-122.138874, 47.632346), null, 1, null, null))
            .assertNext(response -> validateGetQuarterDayForecastWithResponse(TestUtils.getExpectedQuarterDayForecast(),
                response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetQuarterDayForecastWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(
                client.getQuarterDayForecastWithResponse(new GeoPosition(-1000000, 47.632346), null, 1, null, null))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            })
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get current conditions
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetCurrentConditions(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(
                client.getCurrentConditions(new GeoPosition(-122.125679, 47.641268), null, null, null, null))
            .assertNext(
                actualResults -> validateGetCurrentConditions(TestUtils.getExpectedCurrentConditions(), actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get minute forecast with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetCurrentConditionsWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(
                client.getCurrentConditionsWithResponse(new GeoPosition(-122.125679, 47.641268), null, null, null, null,
                    null))
            .assertNext(response -> validateGetCurrentConditionsWithResponse(TestUtils.getExpectedCurrentConditions(),
                response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetCurrentConditionsWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(
                client.getCurrentConditionsWithResponse(new GeoPosition(-100000, 47.641268), null, null, null, null, null))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            })
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get daily forecast
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetDailyForecast(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getDailyForecast(new GeoPosition(30.0734812, 62.6490341), null, 5, null))
            .assertNext(actualResults -> validateGetDailyForecast(TestUtils.getExpectedDailyForecast(), actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get daily forecast with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetDailyForecastWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(
                client.getDailyForecastWithResponse(new GeoPosition(30.0734812, 62.6490341), null, 5, null, null))
            .assertNext(
                response -> validateGetDailyForecastWithResponse(TestUtils.getExpectedDailyForecast(), response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetDailyForecastWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(
                client.getDailyForecastWithResponse(new GeoPosition(-1000000, 62.6490341), null, 5, null, null))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            })
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get weather along route
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetWeatherAlongRoute(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        List<Waypoint> waypoints = Arrays.asList(new Waypoint(new GeoPosition(-77.037, 38.907), 0.0),
            new Waypoint(new GeoPosition(-77.009, 38.907), 10.0), new Waypoint(new GeoPosition(-76.928, 38.926), 20.0),
            new Waypoint(new GeoPosition(-76.852, 39.033), 30.0), new Waypoint(new GeoPosition(-76.732, 39.168), 40.0),
            new Waypoint(new GeoPosition(-76.634, 39.269), 50.0), new Waypoint(new GeoPosition(-76.612, 39.287), 60.0));
        StepVerifier.create(client.getWeatherAlongRoute(waypoints, "en"))
            .assertNext(actualResults -> validateGetExpectedWeatherAlongRoute(TestUtils.getExpectedWeatherAlongRoute(),
                actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get weather along route with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetWeatherAlongRouteWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        List<Waypoint> waypoints = Arrays.asList(new Waypoint(new GeoPosition(-77.037, 38.907), 0.0),
            new Waypoint(new GeoPosition(-77.009, 38.907), 10.0), new Waypoint(new GeoPosition(-76.928, 38.926), 20.0),
            new Waypoint(new GeoPosition(-76.852, 39.033), 30.0), new Waypoint(new GeoPosition(-76.732, 39.168), 40.0),
            new Waypoint(new GeoPosition(-76.634, 39.269), 50.0), new Waypoint(new GeoPosition(-76.612, 39.287), 60.0));
        StepVerifier.create(client.getWeatherAlongRouteWithResponse(waypoints, "en", null))
            .assertNext(
                response -> validateGetExpectedWeatherAlongRouteWithResponse(TestUtils.getExpectedWeatherAlongRoute(),
                    response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testInvalidAsyncGetWeatherAlongRouteWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        List<Waypoint> waypoints = Arrays.asList(new Waypoint(new GeoPosition(-77.037, 38.907), 0.0),
            new Waypoint(new GeoPosition(-1000000, 38.907), 10.0), new Waypoint(new GeoPosition(-76.928, 38.926), 20.0),
            new Waypoint(new GeoPosition(-76.852, 39.033), 30.0), new Waypoint(new GeoPosition(-76.732, 39.168), 40.0),
            new Waypoint(new GeoPosition(-76.634, 39.269), 50.0), new Waypoint(new GeoPosition(-76.612, 39.287), 60.0));
        StepVerifier.create(client.getWeatherAlongRouteWithResponse(waypoints, "en", null)).expectErrorSatisfies(ex -> {
            final HttpResponseException httpResponseException = (HttpResponseException) ex;
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
        }).verify(DEFAULT_TIMEOUT);
    }

    // Test async get severe weather alerts
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetSevereWeatherAlerts(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(
                client.getSevereWeatherAlerts(new GeoPosition(-85.06431274043842, 30.324604968788467), null, true))
            .assertNext(actualResults -> validateGetSevereWeatherAlerts(TestUtils.getExpectedSevereWeatherAlerts(),
                actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get severe weather alert with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetSevereWeatherAlertsWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(
                client.getSevereWeatherAlertsWithResponse(new GeoPosition(-85.06431274043842, 30.324604968788467), null,
                    true, null))
            .assertNext(
                response -> validateGetSevereWeatherAlertsWithResponse(TestUtils.getExpectedSevereWeatherAlerts(),
                    response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetSevereWeatherAlertsWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(
                client.getSevereWeatherAlertsWithResponse(new GeoPosition(-100000, 30.324604968788467), null, true, null))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            })
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get daily indices
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetDailyIndices(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getDailyIndices(new GeoPosition(-79.37849, 43.84745), null, null, null, 11))
            .assertNext(actualResults -> validateGetDailyIndices(TestUtils.getExpectedDailyIndices(), actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get daily indices with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetDailyIndicesWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(
                client.getDailyIndicesWithResponse(new GeoPosition(-79.37849, 43.84745), null, null, null, 11, null))
            .assertNext(response -> validateGetDailyIndicesWithResponse(TestUtils.getExpectedDailyIndices(), response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetDailyIndicesWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(
                client.getDailyIndicesWithResponse(new GeoPosition(-100000, 43.84745), null, null, null, 11, null))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            })
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get tropical storm active
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetTropicalStormActive(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getTropicalStormActive())
            .assertNext(
                actualResults -> validateGetExpectedTropicalStormActive(TestUtils.getExpectedTropicalStormActive(),
                    actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get tropical storm active with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetTropicalStormActiveWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getTropicalStormActiveWithResponse(null))
            .assertNext(response -> validateGetExpectedTropicalStormActiveWithResponse(
                TestUtils.getExpectedTropicalStormActive(), response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async search tropical storm
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncSearchTropicalStorm(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        ActiveStormResult result = client.getTropicalStormActive().block();
        if (!result.getActiveStorms().isEmpty()) {
            ActiveStorm storm = result.getActiveStorms().get(0);
            StepVerifier.create(client.searchTropicalStorm(storm.getYear(), storm.getBasinId(), storm.getGovId()))
                .assertNext(actualResults -> validateGetSearchTropicalStorm(TestUtils.getExpectedSearchTropicalStorm(),
                    actualResults))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);
        }
    }

    // Test async search tropical storm with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncSearchTropicalStormWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        ActiveStormResult result = client.getTropicalStormActive().block();
        if (!result.getActiveStorms().isEmpty()) {
            ActiveStorm storm = result.getActiveStorms().get(0);
            StepVerifier.create(
                    client.searchTropicalStormWithResponse(storm.getYear(), storm.getBasinId(), storm.getGovId()))
                .assertNext(
                    response -> validateGetSearchTropicalStormWithResponse(TestUtils.getExpectedSearchTropicalStorm(),
                        response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);
        }
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidSearchTropicalStormWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        ActiveStormResult result = client.getTropicalStormActive().block();
        if (!result.getActiveStorms().isEmpty()) {
            ActiveStorm storm = result.getActiveStorms().get(0);
            StepVerifier.create(client.searchTropicalStormWithResponse(-1, storm.getBasinId(), storm.getGovId()))
                .expectErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                })
                .verify(DEFAULT_TIMEOUT);
        }
    }

    // Test async get tropical storm forecast
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetTropicalStormForecast(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        ActiveStormResult result = client.getTropicalStormActive().block();
        if (!result.getActiveStorms().isEmpty()) {
            ActiveStorm storm = result.getActiveStorms().get(0);
            TropicalStormForecastOptions forecastOptions = new TropicalStormForecastOptions(storm.getYear(),
                storm.getBasinId(), storm.getGovId()).setIncludeWindowGeometry(true);
            StepVerifier.create(client.getTropicalStormForecast(forecastOptions))
                .assertNext(
                    actualResults -> validateGetTropicalStormForecast(TestUtils.getExpectedTropicalStormForecast(),
                        actualResults))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);
        }
    }

    // Test async get tropical storm forecast with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetTropicalStormForecastWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        ActiveStormResult result = client.getTropicalStormActive().block();
        if (!result.getActiveStorms().isEmpty()) {
            ActiveStorm storm = result.getActiveStorms().get(0);
            TropicalStormForecastOptions forecastOptions = new TropicalStormForecastOptions(storm.getYear(),
                storm.getBasinId(), storm.getGovId()).setIncludeWindowGeometry(true);
            StepVerifier.create(client.getTropicalStormForecastWithResponse(forecastOptions, null))
                .assertNext(response -> validateGetTropicalStormForecastWithResponse(
                    TestUtils.getExpectedTropicalStormForecast(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);
        }
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetTropicalStormForecastWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        ActiveStormResult result = client.getTropicalStormActive().block();
        if (!result.getActiveStorms().isEmpty()) {
            ActiveStorm storm = result.getActiveStorms().get(0);
            TropicalStormForecastOptions forecastOptions = new TropicalStormForecastOptions(-1, storm.getBasinId(),
                storm.getGovId()).setIncludeWindowGeometry(true);
            StepVerifier.create(client.getTropicalStormForecastWithResponse(forecastOptions, null))
                .expectErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                })
                .verify(DEFAULT_TIMEOUT);
        }
    }

    // Test async get tropical storm locations
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetTropicalStormLocations(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        ActiveStormResult result = client.getTropicalStormActive().block();
        if (!result.getActiveStorms().isEmpty()) {
            ActiveStorm storm = result.getActiveStorms().get(0);
            TropicalStormLocationOptions locationOptions = new TropicalStormLocationOptions(storm.getYear(),
                storm.getBasinId(), storm.getGovId());
            StepVerifier.create(client.getTropicalStormLocations(locationOptions))
                .assertNext(
                    actualResults -> validateGetTropicalStormLocations(TestUtils.getExpectedTropicalStormLocations(),
                        actualResults))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);
        }
    }

    // Test async get tropical storm locations with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetTropicalStormLocationsWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        ActiveStormResult result = client.getTropicalStormActive().block();
        if (!result.getActiveStorms().isEmpty()) {
            ActiveStorm storm = result.getActiveStorms().get(0);
            TropicalStormLocationOptions locationOptions = new TropicalStormLocationOptions(storm.getYear(),
                storm.getBasinId(), storm.getGovId());
            StepVerifier.create(client.getTropicalStormLocationsWithResponse(locationOptions, null))
                .assertNext(response -> validateGetTropicalStormLocationsWithResponse(
                    TestUtils.getExpectedTropicalStormLocations(), response))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);
        }
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetTropicalStormLocationsWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        ActiveStormResult result = client.getTropicalStormActive().block();
        if (!result.getActiveStorms().isEmpty()) {
            ActiveStorm storm = result.getActiveStorms().get(0);
            TropicalStormLocationOptions locationOptions = new TropicalStormLocationOptions(-1, storm.getBasinId(),
                storm.getGovId());
            StepVerifier.create(client.getTropicalStormLocationsWithResponse(locationOptions, null))
                .expectErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                })
                .verify(DEFAULT_TIMEOUT);
        }
    }

    // Test async get current air quality
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetCurrentAirQuality(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getCurrentAirQuality(new GeoPosition(-122.138874, 47.632346), "es", false))
            .assertNext(
                actualResults -> validateGetCurrentAirQuality(TestUtils.getExpectedCurrentAirQuality(), actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get current air quality with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetCurrentAirQualityWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(
                client.getCurrentAirQualityWithResponse(new GeoPosition(-122.138874, 47.632346), "es", false, null))
            .assertNext(response -> validateGetCurrentAirQualityWithResponse(TestUtils.getExpectedCurrentAirQuality(),
                response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetCurrentAirQualityWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(
                client.getCurrentAirQualityWithResponse(new GeoPosition(-1000000, 47.632346), "es", false, null))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            })
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get air quality daily forecasts
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetAirQualityDailyForecasts(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(
                client.getAirQualityDailyForecasts(new GeoPosition(-122.138874, 47.632346), "en", DailyDuration.TWO_DAYS))
            .assertNext(
                actualResults -> validateGetAirQualityDailyForecasts(TestUtils.getExpectedAirQualityDailyForecasts(),
                    actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get air quality daily forecast with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetAirQualityDailyForecastsWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(
                client.getAirQualityDailyForecastsWithResponse(new GeoPosition(-122.138874, 47.632346), "en",
                    DailyDuration.TWO_DAYS, null))
            .assertNext(response -> validateGetAirQualityDailyForecastsWithResponse(
                TestUtils.getExpectedAirQualityDailyForecasts(), response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetAirQualityDailyForecastsWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getAirQualityDailyForecastsWithResponse(new GeoPosition(-100000, 47.632346), "en",
            DailyDuration.TWO_DAYS, null))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            }).verify(DEFAULT_TIMEOUT);
    }

    // Test async get air quality hourly forecasts
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetAirQualityHourlyForecasts(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(
                client.getAirQualityHourlyForecasts(new GeoPosition(-122.138874, 47.632346), "fr", HourlyDuration.ONE_HOUR,
                    false))
            .assertNext(
                actualResults -> validateGetAirQualityHourlyForecasts(TestUtils.getExpectedAirQualityHourlyForecasts(),
                    actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get air quality hourly forecasts with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetAirQualityHourlyForecastsWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(
                client.getAirQualityHourlyForecastsWithResponse(new GeoPosition(-122.138874, 47.632346), "fr",
                    HourlyDuration.ONE_HOUR, false, null))
            .assertNext(response -> validateGetAirQualityHourlyForecastsWithResponse(
                TestUtils.getExpectedAirQualityHourlyForecasts(), response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetAirQualityHourlyForecastsWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getAirQualityHourlyForecastsWithResponse(new GeoPosition(-100000, 47.632346), "fr",
            HourlyDuration.ONE_HOUR, false, null))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            }).verify(DEFAULT_TIMEOUT);
    }

    // Test async get daily historical actuals
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetDailyHistoricalActuals(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        LocalDate before = testResourceNamer.now().toLocalDate().minusDays(30);
        LocalDate today = testResourceNamer.now().toLocalDate();
        StepVerifier.create(
                client.getDailyHistoricalActuals(new GeoPosition(30.0734812, 62.6490341), before, today, null))
            .assertNext(
                actualResults -> validateGetDailyHistoricalActuals(TestUtils.getExpectedDailyHistoricalActuals(),
                    actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get daily historical actuals with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetDailyHistoricalActualsWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        LocalDate before = testResourceNamer.now().toLocalDate().minusDays(30);
        LocalDate today = testResourceNamer.now().toLocalDate();
        StepVerifier.create(
                client.getDailyHistoricalActualsWithResponse(new GeoPosition(30.0734812, 62.6490341), before, today, null,
                    null))
            .assertNext(
                response -> validateGetDailyHistoricalActualsWithResponse(TestUtils.getExpectedDailyHistoricalActuals(),
                    response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetDailyHistoricalActualsWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        LocalDate before = testResourceNamer.now().toLocalDate().minusDays(30);
        LocalDate today = testResourceNamer.now().toLocalDate();
        StepVerifier.create(
            client.getDailyHistoricalActualsWithResponse(new GeoPosition(-100000, 62.6490341), before, today, null,
                null))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            }).verify(DEFAULT_TIMEOUT);
    }

    // Test async get daily historical records
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetDailyHistoricalRecords(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        LocalDate beforeYears = testResourceNamer.now().toLocalDate().minusYears(10);
        LocalDate afterYears = beforeYears.plusDays(30);
        StepVerifier.create(
                client.getDailyHistoricalRecords(new GeoPosition(-75.165222, 39.952583), beforeYears, afterYears, null))
            .assertNext(
                actualResults -> validateGetDailyHistoricalRecords(TestUtils.getExpectedDailyHistoricalRecords(),
                    actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get daily historical records with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetDailyHistoricalRecordsWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        LocalDate beforeYears = testResourceNamer.now().toLocalDate().minusYears(10);
        LocalDate afterYears = beforeYears.plusDays(30);
        StepVerifier.create(
                client.getDailyHistoricalRecordsWithResponse(new GeoPosition(-75.165222, 39.952583), beforeYears,
                    afterYears, null))
            .assertNext(
                response -> validateGetDailyHistoricalRecordsWithResponse(TestUtils.getExpectedDailyHistoricalRecords(),
                    response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetDailyHistoricalRecordsWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        LocalDate beforeYears = testResourceNamer.now().toLocalDate().minusYears(10);
        LocalDate afterYears = beforeYears.plusDays(30);
        StepVerifier.create(
            client.getDailyHistoricalRecordsWithResponse(new GeoPosition(-1000000, 39.952583), beforeYears, afterYears,
                null))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            }).verify(DEFAULT_TIMEOUT);
    }

    // Test async get daily historical normals
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetDailyHistoricalNormals(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        LocalDate before = testResourceNamer.now().toLocalDate().minusDays(30);
        LocalDate today = testResourceNamer.now().toLocalDate();
        StepVerifier.create(
                client.getDailyHistoricalNormals(new GeoPosition(30.0734812, 62.6490341), before, today, null))
            .assertNext(actualResults -> validateGetDailyHistoricalNormalsResult(
                TestUtils.getExpectedDailyHistoricalNormalsResult(), actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get daily historical normals with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetDailyHistoricalNormalsWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        LocalDate before = testResourceNamer.now().toLocalDate().minusDays(30);
        LocalDate today = testResourceNamer.now().toLocalDate();
        StepVerifier.create(
                client.getDailyHistoricalNormalsWithResponse(new GeoPosition(30.0734812, 62.6490341), before, today, null,
                    null))
            .assertNext(response -> validateGetDailyHistoricalNormalsResultWithResponse(
                TestUtils.getExpectedDailyHistoricalNormalsResult(), response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetDailyHistoricalNormalsWithResponse(HttpClient httpClient,
        WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        LocalDate before = testResourceNamer.now().toLocalDate().minusDays(30);
        LocalDate today = testResourceNamer.now().toLocalDate();
        StepVerifier.create(
            client.getDailyHistoricalNormalsWithResponse(new GeoPosition(-100000, 62.6490341), before, today, null,
                null))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            }).verify(DEFAULT_TIMEOUT);
    }
}
