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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WeatherAsyncClientTest extends WeatherTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    @BeforeAll
    public static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    public static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    private WeatherAsyncClient getWeatherAsyncClient(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        return getWeatherAsyncClientBuilder(httpClient, serviceVersion).buildAsyncClient();
    }

    // Test async get hourly forecast
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetHourlyForecast(HttpClient httpClient, WeatherServiceVersion serviceVersion) throws IOException {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getHourlyForecast(new GeoPosition(-122.138874, 47.632346), null, 12, null))
        .assertNext(actualResults -> {
            try {
                validateGetHourlyForecast(TestUtils.getExpectedHourlyForecast(), actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get hourly forecast");
            }
        }).verifyComplete();
    }

    // Test async get hourly forecast with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetHourlyForecastWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getHourlyForecastWithResponse(new GeoPosition(-122.138874, 47.632346), null, 12, null))
            .assertNext(response -> {
                try {
                    validateGetHourlyForecastWithResponse(TestUtils.getExpectedHourlyForecast(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("unable to get hourly forecast");
                }
            }).verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetHourlyForecastWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getHourlyForecastWithResponse(new GeoPosition(-100000, 47.632346), null, 12, null))
            .verifyErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            });
    }

    // Test async get minute forecast
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetMinuteForecast(HttpClient httpClient, WeatherServiceVersion serviceVersion) throws IOException {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getMinuteForecast(new GeoPosition(-122.138874, 47.632346), 15, null))
        .assertNext(actualResults -> {
            try {
                validateGetMinuteForecast(TestUtils.getExpectedMinuteForecast(), actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get minute forecast");
            }
        }).verifyComplete();
    }

    // Test async get minute forecast with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetMinuteForecastWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getMinuteForecastWithResponse(new GeoPosition(-122.138874, 47.632346), 15, null, null))
            .assertNext(response -> {
                try {
                    validateGetMinuteForecastWithResponse(TestUtils.getExpectedMinuteForecast(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("unable to get minute forecast");
                }
            }).verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetMinuteForecastWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getMinuteForecastWithResponse(new GeoPosition(-1000000, 47.632346), 15, null, null))
            .verifyErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            });
    }

    // Test async get quarter day forecast
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetQuarterDayForecast(HttpClient httpClient, WeatherServiceVersion serviceVersion) throws IOException {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getQuarterDayForecast(new GeoPosition(-122.138874, 47.632346), null, 1, null))
        .assertNext(actualResults -> {
            try {
                validateGetQuarterDayForecast(TestUtils.getExpectedQuarterDayForecast(), actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get quarter day forecast");
            }
        }).verifyComplete();
    }

    // Test async get quarter day forecast with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetQuarterDayForecastWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getQuarterDayForecastWithResponse(new GeoPosition(-122.138874, 47.632346), null, 1, null, null))
            .assertNext(response -> {
                try {
                    validateGetQuarterDayForecastWithResponse(TestUtils.getExpectedQuarterDayForecast(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("unable to get quarter day forecast");
                }
            }).verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetQuarterDayForecastWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getQuarterDayForecastWithResponse(new GeoPosition(-1000000, 47.632346), null, 1, null, null))
            .verifyErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            });
    }

    // Test async get current conditions
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetCurrentConditions(HttpClient httpClient, WeatherServiceVersion serviceVersion) throws IOException {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getCurrentConditions(new GeoPosition(-122.125679, 47.641268), null, null, null, null))
        .assertNext(actualResults -> {
            try {
                validateGetCurrentConditions(TestUtils.getExpectedCurrentConditions(), actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get current conditions");
            }
        }).verifyComplete();
    }

    // Test async get minute forecast with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetCurrentConditionsWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getCurrentConditionsWithResponse(new GeoPosition(-122.125679, 47.641268), null, null, null, null, null))
            .assertNext(response -> {
                try {
                    validateGetCurrentConditionsWithResponse(TestUtils.getExpectedCurrentConditions(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("unable to get current condition");
                }
            }).verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetCurrentConditionsWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getCurrentConditionsWithResponse(new GeoPosition(-100000, 47.641268), null, null, null, null, null))
            .verifyErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            });
    }

    // Test async get daily forecast
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetDailyForecast(HttpClient httpClient, WeatherServiceVersion serviceVersion) throws IOException {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getDailyForecast(new GeoPosition(30.0734812, 62.6490341), null, 5, null))
        .assertNext(actualResults -> {
            try {
                validateGetDailyForecast(TestUtils.getExpectedDailyForecast(), actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get daily forecast");
            }
        }).verifyComplete();
    }

    // Test async get daily forecast with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetDailyForecastWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getDailyForecastWithResponse(new GeoPosition(30.0734812, 62.6490341), null, 5, null, null))
            .assertNext(response -> {
                try {
                    validateGetDailyForecastWithResponse(TestUtils.getExpectedDailyForecast(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("unable to get daily forecast");
                }
            }).verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetDailyForecastWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getDailyForecastWithResponse(new GeoPosition(-1000000, 62.6490341), null, 5, null, null))
            .verifyErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            });
    }

    // Test async get weather along route
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetWeatherAlongRoute(HttpClient httpClient, WeatherServiceVersion serviceVersion) throws IOException {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        List<Waypoint> waypoints = Arrays.asList(
            new Waypoint(new GeoPosition(-77.037, 38.907), 0.0),
            new Waypoint(new GeoPosition(-77.009, 38.907), 10.0),
            new Waypoint(new GeoPosition(-76.928, 38.926), 20.0),
            new Waypoint(new GeoPosition(-76.852, 39.033), 30.0),
            new Waypoint(new GeoPosition(-76.732, 39.168), 40.0),
            new Waypoint(new GeoPosition(-76.634, 39.269), 50.0),
            new Waypoint(new GeoPosition(-76.612, 39.287), 60.0)
        );
        StepVerifier.create(client.getWeatherAlongRoute(waypoints, "en"))
        .assertNext(actualResults -> {
            try {
                validateGetExpectedWeatherAlongRoute(TestUtils.getExpectedWeatherAlongRoute(), actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get weather along route");
            }
        }).verifyComplete();
    }

    // Test async get weather along route with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetWeatherAlongRouteWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        List<Waypoint> waypoints = Arrays.asList(
            new Waypoint(new GeoPosition(-77.037, 38.907), 0.0),
            new Waypoint(new GeoPosition(-77.009, 38.907), 10.0),
            new Waypoint(new GeoPosition(-76.928, 38.926), 20.0),
            new Waypoint(new GeoPosition(-76.852, 39.033), 30.0),
            new Waypoint(new GeoPosition(-76.732, 39.168), 40.0),
            new Waypoint(new GeoPosition(-76.634, 39.269), 50.0),
            new Waypoint(new GeoPosition(-76.612, 39.287), 60.0)
        );
        StepVerifier.create(client.getWeatherAlongRouteWithResponse(waypoints, "en", null))
            .assertNext(response -> {
                try {
                    validateGetExpectedWeatherAlongRouteWithResponse(TestUtils.getExpectedWeatherAlongRoute(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("unable to get weather along route");
                }
            }).verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testInvalidAsyncGetWeatherAlongRouteWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        List<Waypoint> waypoints = Arrays.asList(
            new Waypoint(new GeoPosition(-77.037, 38.907), 0.0),
            new Waypoint(new GeoPosition(-1000000, 38.907), 10.0),
            new Waypoint(new GeoPosition(-76.928, 38.926), 20.0),
            new Waypoint(new GeoPosition(-76.852, 39.033), 30.0),
            new Waypoint(new GeoPosition(-76.732, 39.168), 40.0),
            new Waypoint(new GeoPosition(-76.634, 39.269), 50.0),
            new Waypoint(new GeoPosition(-76.612, 39.287), 60.0)
        );
        StepVerifier.create(client.getWeatherAlongRouteWithResponse(waypoints, "en", null))
            .verifyErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            });
    }

    // Test async get severe weather alerts
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetSevereWeatherAlerts(HttpClient httpClient, WeatherServiceVersion serviceVersion) throws IOException {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getSevereWeatherAlerts(new GeoPosition(-85.06431274043842, 30.324604968788467), null, true))
        .assertNext(actualResults -> {
            try {
                validateGetSevereWeatherAlerts(TestUtils.getExpectedSevereWeatherAlerts(), actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get severe weather alerts");
            }
        }).verifyComplete();
    }

    // Test async get severe weather alert with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetSevereWeatherAlertsWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getSevereWeatherAlertsWithResponse(new GeoPosition(-85.06431274043842, 30.324604968788467), null, true, null))
            .assertNext(response -> {
                try {
                    validateGetSevereWeatherAlertsWithResponse(TestUtils.getExpectedSevereWeatherAlerts(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("unable to get severe weather alerts");
                }
            }).verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetSevereWeatherAlertsWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getSevereWeatherAlertsWithResponse(new GeoPosition(-100000, 30.324604968788467), null, true, null))
            .verifyErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            });
    }

    // Test async get daily indices
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetDailyIndices(HttpClient httpClient, WeatherServiceVersion serviceVersion) throws IOException {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getDailyIndices(new GeoPosition(-79.37849, 43.84745), null, null, null, 11))
        .assertNext(actualResults -> {
            try {
                validateGetDailyIndices(TestUtils.getExpectedDailyIndices(), actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get daily indices");
            }
        }).verifyComplete();
    }

    // Test async get daily indices with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetDailyIndicesWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getDailyIndicesWithResponse(new GeoPosition(-79.37849, 43.84745), null, null, null, 11, null))
            .assertNext(response -> {
                try {
                    validateGetDailyIndicesWithResponse(TestUtils.getExpectedDailyIndices(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("unable to get daily indices");
                }
            }).verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetDailyIndicesWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getDailyIndicesWithResponse(new GeoPosition(-100000, 43.84745), null, null, null, 11, null))
            .verifyErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            });
    }

    // Test async get tropical storm active
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetTropicalStormActive(HttpClient httpClient, WeatherServiceVersion serviceVersion) throws IOException {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getTropicalStormActive())
        .assertNext(actualResults -> {
            try {
                validateGetExpectedTropicalStormActive(TestUtils.getExpectedTropicalStormActive(), actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get tropical storm active");
            }
        }).verifyComplete();
    }

    // Test async get tropical storm active with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetTropicalStormActiveWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getTropicalStormActiveWithResponse(null))
            .assertNext(response -> {
                try {
                    validateGetExpectedTropicalStormActiveWithResponse(TestUtils.getExpectedTropicalStormActive(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("unable to get tropical storm active");
                }
            }).verifyComplete();
    }

    // Test async search tropical storm
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncSearchTropicalStorm(HttpClient httpClient, WeatherServiceVersion serviceVersion) throws IOException {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        ActiveStormResult result = client.getTropicalStormActive().block();
        if (result.getActiveStorms().size() > 0) {
            ActiveStorm storm = result.getActiveStorms().get(0);
            StepVerifier.create(client.searchTropicalStorm(storm.getYear(), storm.getBasinId(), storm.getGovId()))
            .assertNext(actualResults -> {
                try {
                    validateGetSearchTropicalStorm(TestUtils.getExpectedSearchTropicalStorm(), actualResults);
                } catch (IOException e) {
                    Assertions.fail("Unable to get search tropical storm");
                }
            }).verifyComplete();
        }
    }

    // Test async search tropical storm with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncSearchTropicalStormWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        ActiveStormResult result = client.getTropicalStormActive().block();
        if (result.getActiveStorms().size() > 0) {
            ActiveStorm storm = result.getActiveStorms().get(0);
            StepVerifier.create(client.searchTropicalStormWithResponse(storm.getYear(), storm.getBasinId(), storm.getGovId()))
                .assertNext(response -> {
                    try {
                        validateGetSearchTropicalStormWithResponse(TestUtils.getExpectedSearchTropicalStorm(), 200, response);
                    } catch (IOException e) {
                        Assertions.fail("unable to get search tropical storm");
                    }
                }).verifyComplete();
        }
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidSearchTropicalStormWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        ActiveStormResult result = client.getTropicalStormActive().block();
        if (result.getActiveStorms().size() > 0) {
            ActiveStorm storm = result.getActiveStorms().get(0);
            StepVerifier.create(client.searchTropicalStormWithResponse(-1, storm.getBasinId(), storm.getGovId()))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                });
        }
    }

    // Test async get tropical storm forecast
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetTropicalStormForecast(HttpClient httpClient, WeatherServiceVersion serviceVersion) throws IOException {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        ActiveStormResult result = client.getTropicalStormActive().block();
        if (result.getActiveStorms().size() > 0) {
            ActiveStorm storm = result.getActiveStorms().get(0);
            TropicalStormForecastOptions forecastOptions = new TropicalStormForecastOptions(storm.getYear(),
                storm.getBasinId(), storm.getGovId())
                .setIncludeWindowGeometry(true);
            StepVerifier.create(client.getTropicalStormForecast(forecastOptions))
                .assertNext(actualResults -> {
                    try {
                        validateGetTropicalStormForecast(TestUtils.getExpectedTropicalStormForecast(), actualResults);
                    } catch (IOException e) {
                        Assertions.fail("Unable to get tropical storm forecast");
                    }
                }).verifyComplete();
        }
    }

    // Test async get tropical storm forecast with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetTropicalStormForecastWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        ActiveStormResult result = client.getTropicalStormActive().block();
        if (result.getActiveStorms().size() > 0) {
            ActiveStorm storm = result.getActiveStorms().get(0);
            TropicalStormForecastOptions forecastOptions = new TropicalStormForecastOptions(storm.getYear(),
                storm.getBasinId(), storm.getGovId())
                .setIncludeWindowGeometry(true);
            StepVerifier.create(client.getTropicalStormForecastWithResponse(forecastOptions, null))
                .assertNext(response -> {
                    try {
                        validateGetTropicalStormForecastWithResponse(TestUtils.getExpectedTropicalStormForecast(), 200, response);
                    } catch (IOException e) {
                        Assertions.fail("unable to get tropical storm forecast");
                    }
                }).verifyComplete();
        }
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetTropicalStormForecastWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        ActiveStormResult result = client.getTropicalStormActive().block();
        if (result.getActiveStorms().size() > 0) {
            ActiveStorm storm = result.getActiveStorms().get(0);
            TropicalStormForecastOptions forecastOptions = new TropicalStormForecastOptions(-1,
                storm.getBasinId(), storm.getGovId())
                .setIncludeWindowGeometry(true);
            StepVerifier.create(client.getTropicalStormForecastWithResponse(forecastOptions, null))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                });
        }
    }

    // Test async get tropical storm locations
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetTropicalStormLocations(HttpClient httpClient, WeatherServiceVersion serviceVersion) throws IOException {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        ActiveStormResult result = client.getTropicalStormActive().block();
        if (result.getActiveStorms().size() > 0) {
            ActiveStorm storm = result.getActiveStorms().get(0);
            TropicalStormLocationOptions locationOptions = new TropicalStormLocationOptions(storm.getYear(),
                storm.getBasinId(), storm.getGovId());
            StepVerifier.create(client.getTropicalStormLocations(locationOptions))
                .assertNext(actualResults -> {
                    try {
                        validateGetTropicalStormLocations(TestUtils.getExpectedTropicalStormLocations(), actualResults);
                    } catch (IOException e) {
                        Assertions.fail("Unable to get tropical storm locations");
                    }
                }).verifyComplete();
        }
    }

    // Test async get tropical storm locations with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetTropicalStormLocationsWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        ActiveStormResult result = client.getTropicalStormActive().block();
        if (result.getActiveStorms().size() > 0) {
            ActiveStorm storm = result.getActiveStorms().get(0);
            TropicalStormLocationOptions locationOptions = new TropicalStormLocationOptions(storm.getYear(),
                storm.getBasinId(), storm.getGovId());
            StepVerifier.create(client.getTropicalStormLocationsWithResponse(locationOptions, null))
                .assertNext(response -> {
                    try {
                        validateGetTropicalStormLocationsWithResponse(TestUtils.getExpectedTropicalStormLocations(), 200, response);
                    } catch (IOException e) {
                        Assertions.fail("unable to get tropical storm locations");
                    }
                }).verifyComplete();
        }
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetTropicalStormLocationsWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        ActiveStormResult result = client.getTropicalStormActive().block();
        if (result.getActiveStorms().size() > 0) {
            ActiveStorm storm = result.getActiveStorms().get(0);
            TropicalStormLocationOptions locationOptions = new TropicalStormLocationOptions(-1,
                storm.getBasinId(), storm.getGovId());
            StepVerifier.create(client.getTropicalStormLocationsWithResponse(locationOptions, null))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                });
        }
    }

    // Test async get current air quality
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetCurrentAirQuality(HttpClient httpClient, WeatherServiceVersion serviceVersion) throws IOException {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getCurrentAirQuality(new GeoPosition(-122.138874, 47.632346), "es", false))
            .assertNext(actualResults -> {
                try {
                    validateGetCurrentAirQuality(TestUtils.getExpectedCurrentAirQuality(), actualResults);
                } catch (IOException e) {
                    Assertions.fail("Unable to get current air quality");
                }
            }).verifyComplete();
    }

    // Test async get current air quality with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetCurrentAirQualityWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getCurrentAirQualityWithResponse(new GeoPosition(-122.138874, 47.632346), "es", false, null))
            .assertNext(response -> {
                try {
                    validateGetCurrentAirQualityWithResponse(TestUtils.getExpectedCurrentAirQuality(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("unable to get current air quality");
                }
            }).verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetCurrentAirQualityWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getCurrentAirQualityWithResponse(new GeoPosition(-1000000, 47.632346), "es", false, null))
            .verifyErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            });
    }

    // Test async get air quality daily forecasts
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetAirQualityDailyForecasts(HttpClient httpClient, WeatherServiceVersion serviceVersion) throws IOException {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getAirQualityDailyForecasts(new GeoPosition(-122.138874, 47.632346), "en", DailyDuration.TWO_DAYS))
            .assertNext(actualResults -> {
                try {
                    validateGetAirQualityDailyForecasts(TestUtils.getExpectedAirQualityDailyForecasts(), actualResults);
                } catch (IOException e) {
                    Assertions.fail("Unable to get air quality daily forecast");
                }
            }).verifyComplete();
    }

    // Test async get air quality daily forecast with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetAirQualityDailyForecastsWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getAirQualityDailyForecastsWithResponse(
            new GeoPosition(-122.138874, 47.632346), "en", DailyDuration.TWO_DAYS, null))
            .assertNext(response -> {
                try {
                    validateGetAirQualityDailyForecastsWithResponse(TestUtils.getExpectedAirQualityDailyForecasts(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("unable to get air quality daily forecast");
                }
            }).verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetAirQualityDailyForecastsWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getAirQualityDailyForecastsWithResponse(
            new GeoPosition(-100000, 47.632346), "en", DailyDuration.TWO_DAYS, null))
            .verifyErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            });
    }

    // Test async get air quality hourly forecasts
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetAirQualityHourlyForecasts(HttpClient httpClient, WeatherServiceVersion serviceVersion) throws IOException {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getAirQualityHourlyForecasts(
            new GeoPosition(-122.138874, 47.632346), "fr", HourlyDuration.ONE_HOUR, false))
            .assertNext(actualResults -> {
                try {
                    validateGetAirQualityHourlyForecasts(TestUtils.getExpectedAirQualityHourlyForecasts(), actualResults);
                } catch (IOException e) {
                    Assertions.fail("Unable to get air quality hourly forecast");
                }
            }).verifyComplete();
    }

    // Test async get air quality hourly forecasts with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetAirQualityHourlyForecastsWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getAirQualityHourlyForecastsWithResponse(
            new GeoPosition(-122.138874, 47.632346), "fr", HourlyDuration.ONE_HOUR, false, null))
            .assertNext(response -> {
                try {
                    validateGetAirQualityHourlyForecastsWithResponse(TestUtils.getExpectedAirQualityHourlyForecasts(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("unable to get air quality hourly forecast");
                }
            }).verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetAirQualityHourlyForecastsWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getAirQualityHourlyForecastsWithResponse(
            new GeoPosition(-100000, 47.632346), "fr", HourlyDuration.ONE_HOUR, false, null))
            .verifyErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            });
    }

    // Test async get daily historical actuals
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetDailyHistoricalActuals(HttpClient httpClient, WeatherServiceVersion serviceVersion) throws IOException {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        LocalDate before = testResourceNamer.now().toLocalDate().minusDays(30);
        LocalDate today = testResourceNamer.now().toLocalDate();
        StepVerifier.create(client.getDailyHistoricalActuals(new GeoPosition(30.0734812, 62.6490341), before, today, null))
            .assertNext(actualResults -> {
                try {
                    validateGetDailyHistoricalActuals(TestUtils.getExpectedDailyHistoricalActuals(), actualResults);
                } catch (IOException e) {
                    Assertions.fail("Unable to get daily historical actuals forecast");
                }
            }).verifyComplete();
    }

    // Test async get daily historical actuals with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetDailyHistoricalActualsWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        LocalDate before = testResourceNamer.now().toLocalDate().minusDays(30);
        LocalDate today = testResourceNamer.now().toLocalDate();
        StepVerifier.create(client.getDailyHistoricalActualsWithResponse(new GeoPosition(30.0734812, 62.6490341), before, today, null, null))
            .assertNext(response -> {
                try {
                    validateGetDailyHistoricalActualsWithResponse(TestUtils.getExpectedDailyHistoricalActuals(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("unable to get daily historical actuals forecast");
                }
            }).verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetDailyHistoricalActualsWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        LocalDate before = testResourceNamer.now().toLocalDate().minusDays(30);
        LocalDate today = testResourceNamer.now().toLocalDate();
        StepVerifier.create(client.getDailyHistoricalActualsWithResponse(new GeoPosition(-100000, 62.6490341), before, today, null, null))
            .verifyErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            });
    }

    // Test async get daily historical records
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetDailyHistoricalRecords(HttpClient httpClient, WeatherServiceVersion serviceVersion) throws IOException {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        LocalDate beforeYears = testResourceNamer.now().toLocalDate().minusYears(10);
        LocalDate afterYears = beforeYears.plusDays(30);
        StepVerifier.create(client.getDailyHistoricalRecords(new GeoPosition(-75.165222, 39.952583), beforeYears, afterYears, null))
            .assertNext(actualResults -> {
                try {
                    validateGetDailyHistoricalRecords(TestUtils.getExpectedDailyHistoricalRecords(), actualResults);
                } catch (IOException e) {
                    Assertions.fail("Unable to get daily historical records forecast");
                }
            }).verifyComplete();
    }

    // Test async get daily historical records with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetDailyHistoricalRecordsWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        LocalDate beforeYears = testResourceNamer.now().toLocalDate().minusYears(10);
        LocalDate afterYears = beforeYears.plusDays(30);
        StepVerifier.create(client.getDailyHistoricalRecordsWithResponse(new GeoPosition(-75.165222, 39.952583), beforeYears, afterYears, null))
            .assertNext(response -> {
                try {
                    validateGetDailyHistoricalRecordsWithResponse(TestUtils.getExpectedDailyHistoricalRecords(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("unable to get daily historical records forecast");
                }
            }).verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetDailyHistoricalRecordsWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        LocalDate beforeYears = testResourceNamer.now().toLocalDate().minusYears(10);
        LocalDate afterYears = beforeYears.plusDays(30);
        StepVerifier.create(client.getDailyHistoricalRecordsWithResponse(new GeoPosition(-1000000, 39.952583), beforeYears, afterYears, null))
            .verifyErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            });
    }

    // Test async get daily historical normals
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetDailyHistoricalNormals(HttpClient httpClient, WeatherServiceVersion serviceVersion) throws IOException {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        LocalDate before = testResourceNamer.now().toLocalDate().minusDays(30);
        LocalDate today = testResourceNamer.now().toLocalDate();
        StepVerifier.create(client.getDailyHistoricalNormals(new GeoPosition(30.0734812, 62.6490341), before, today, null))
            .assertNext(actualResults -> {
                try {
                    validateGetDailyHistoricalNormalsResult(TestUtils.getExpectedDailyHistoricalNormalsResult(), actualResults);
                } catch (IOException e) {
                    Assertions.fail("Unable to get daily historical normals result");
                }
            }).verifyComplete();
    }

    // Test async get daily historical normals with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncGetDailyHistoricalNormalsWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        LocalDate before = testResourceNamer.now().toLocalDate().minusDays(30);
        LocalDate today = testResourceNamer.now().toLocalDate();
        StepVerifier.create(client.getDailyHistoricalNormalsWithResponse(new GeoPosition(30.0734812, 62.6490341), before, today, null, null))
            .assertNext(response -> {
                try {
                    validateGetDailyHistoricalNormalsResultWithResponse(TestUtils.getExpectedDailyHistoricalNormalsResult(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("unable to get daily historical normals result");
                }
            }).verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.weather.TestUtils#getTestParameters")
    public void testAsyncInvalidGetDailyHistoricalNormalsWithResponse(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherAsyncClient client = getWeatherAsyncClient(httpClient, serviceVersion);
        LocalDate before = testResourceNamer.now().toLocalDate().minusDays(30);
        LocalDate today = testResourceNamer.now().toLocalDate();
        StepVerifier.create(client.getDailyHistoricalNormalsWithResponse(new GeoPosition(-100000, 62.6490341), before, today, null, null))
            .verifyErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            });
    }
}
