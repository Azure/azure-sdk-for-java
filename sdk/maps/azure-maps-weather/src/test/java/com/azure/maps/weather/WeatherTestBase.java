// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.weather;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxyRequestMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.maps.weather.models.ActiveStormResult;
import com.azure.maps.weather.models.AirQualityResult;
import com.azure.maps.weather.models.CurrentConditionsResult;
import com.azure.maps.weather.models.DailyAirQualityForecastResult;
import com.azure.maps.weather.models.DailyForecastResult;
import com.azure.maps.weather.models.DailyHistoricalActualsResult;
import com.azure.maps.weather.models.DailyHistoricalNormalsResult;
import com.azure.maps.weather.models.DailyHistoricalRecordsResult;
import com.azure.maps.weather.models.DailyIndicesResult;
import com.azure.maps.weather.models.HourlyForecastResult;
import com.azure.maps.weather.models.MinuteForecastResult;
import com.azure.maps.weather.models.QuarterDayForecastResult;
import com.azure.maps.weather.models.SevereWeatherAlertsResult;
import com.azure.maps.weather.models.StormForecastResult;
import com.azure.maps.weather.models.StormLocationsResult;
import com.azure.maps.weather.models.StormSearchResult;
import com.azure.maps.weather.models.WeatherAlongRouteResult;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WeatherTestBase extends TestProxyTestBase {
    WeatherClientBuilder getWeatherAsyncClientBuilder(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherClientBuilder builder = modifyBuilder(httpClient, new WeatherClientBuilder()).serviceVersion(
            serviceVersion);

        if (interceptorManager.isPlaybackMode()) {
            builder.endpoint("https://localhost:8080");
        }

        return builder;
    }

    WeatherClientBuilder modifyBuilder(HttpClient httpClient, WeatherClientBuilder builder) {
        if (interceptorManager.isRecordMode() || interceptorManager.isPlaybackMode()) {
            interceptorManager.addSanitizers(Collections.singletonList(
                new TestProxySanitizer("subscription-key", ".+", "REDACTED", TestProxySanitizerType.HEADER)));
        }

        if (interceptorManager.isPlaybackMode()) {
            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();

            customMatchers.add(
                new CustomMatcher().setHeadersKeyOnlyMatch(Collections.singletonList("subscription-key")));
            interceptorManager.addMatchers(customMatchers);
        }

        builder.retryPolicy(new RetryPolicy(new ExponentialBackoff(5, Duration.ofSeconds(2), Duration.ofSeconds(16))))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
                .credential(new DefaultAzureCredentialBuilder().build())
                .weatherClientId(Configuration.getGlobalConfiguration().get("MAPS_CLIENT_ID"));
        } else if (interceptorManager.isPlaybackMode()) {
            builder.credential(new MockTokenCredential()).weatherClientId("weatherClientId");
        } else {
            builder.credential(new AzurePowerShellCredentialBuilder().build())
                .weatherClientId(Configuration.getGlobalConfiguration().get("MAPS_CLIENT_ID"));
        }

        return builder.httpClient(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);
    }

    static void validateGetHourlyForecast(HourlyForecastResult expected, HourlyForecastResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getForecasts().size(), actual.getForecasts().size());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetHourlyForecastWithResponse(HourlyForecastResult expected,
        Response<HourlyForecastResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetHourlyForecast(expected, response.getValue());
    }

    static void validateGetMinuteForecast(MinuteForecastResult expected, MinuteForecastResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getIntervals().size(), actual.getIntervals().size());
    }

    static void validateGetMinuteForecastWithResponse(MinuteForecastResult expected,
        Response<MinuteForecastResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetMinuteForecast(expected, response.getValue());
    }

    static void validateGetQuarterDayForecast(QuarterDayForecastResult expected, QuarterDayForecastResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getForecasts().size(), actual.getForecasts().size());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetQuarterDayForecastWithResponse(QuarterDayForecastResult expected,
        Response<QuarterDayForecastResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetQuarterDayForecast(expected, response.getValue());
    }

    static void validateGetCurrentConditions(CurrentConditionsResult expected, CurrentConditionsResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getResults().size(), actual.getResults().size());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetCurrentConditionsWithResponse(CurrentConditionsResult expected,
        Response<CurrentConditionsResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetCurrentConditions(expected, response.getValue());
    }

    static void validateGetDailyForecast(DailyForecastResult expected, DailyForecastResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);

        if (!actual.getForecasts().isEmpty()) {
            assertNotNull(expected.getSummary().getCategory());
            assertNotNull(actual.getSummary().getCategory());
        }
    }

    static void validateGetDailyForecastWithResponse(DailyForecastResult expected,
        Response<DailyForecastResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetDailyForecast(expected, response.getValue());
    }

    static void validateGetExpectedWeatherAlongRoute(WeatherAlongRouteResult expected, WeatherAlongRouteResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getWaypoints().size(), actual.getWaypoints().size());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
    }

    static void validateGetExpectedWeatherAlongRouteWithResponse(WeatherAlongRouteResult expected,
        Response<WeatherAlongRouteResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetExpectedWeatherAlongRoute(expected, response.getValue());
    }

    static void validateGetSevereWeatherAlerts(SevereWeatherAlertsResult expected, SevereWeatherAlertsResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetSevereWeatherAlertsWithResponse(SevereWeatherAlertsResult expected,
        Response<SevereWeatherAlertsResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetSevereWeatherAlerts(expected, response.getValue());
    }

    static void validateGetDailyIndices(DailyIndicesResult expected, DailyIndicesResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getResults().size(), actual.getResults().size());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetDailyIndicesWithResponse(DailyIndicesResult expected,
        Response<DailyIndicesResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetDailyIndices(expected, response.getValue());
    }

    static void validateGetExpectedTropicalStormActive(ActiveStormResult expected, ActiveStormResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);

        if (!actual.getActiveStorms().isEmpty()) {
            assertEquals(expected.getClass().getName(), actual.getClass().getName());
            assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
        }
    }

    static void validateGetExpectedTropicalStormActiveWithResponse(ActiveStormResult expected,
        Response<ActiveStormResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetExpectedTropicalStormActive(expected, response.getValue());
    }

    static void validateGetSearchTropicalStorm(StormSearchResult expected, StormSearchResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
        assertEquals(expected.getNextLink(), actual.getNextLink());
    }

    static void validateGetSearchTropicalStormWithResponse(StormSearchResult expected,
        Response<StormSearchResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetSearchTropicalStorm(expected, response.getValue());
    }

    static void validateGetTropicalStormForecast(StormForecastResult expected, StormForecastResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getNextLink(), actual.getNextLink());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetTropicalStormForecastWithResponse(StormForecastResult expected,
        Response<StormForecastResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetTropicalStormForecast(expected, response.getValue());
    }

    static void validateGetTropicalStormLocations(StormLocationsResult expected, StormLocationsResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getNextLink(), actual.getNextLink());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetTropicalStormLocationsWithResponse(StormLocationsResult expected,
        Response<StormLocationsResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetTropicalStormLocations(expected, response.getValue());
    }

    static void validateGetCurrentAirQuality(AirQualityResult expected, AirQualityResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getAirQualityResults().size(), actual.getAirQualityResults().size());
        assertEquals(expected.getNextLink(), actual.getNextLink());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetCurrentAirQualityWithResponse(AirQualityResult expected,
        Response<AirQualityResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetCurrentAirQuality(expected, response.getValue());
    }

    static void validateGetAirQualityDailyForecasts(DailyAirQualityForecastResult expected,
        DailyAirQualityForecastResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getNextLink(), actual.getNextLink());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetAirQualityDailyForecastsWithResponse(DailyAirQualityForecastResult expected,
        Response<DailyAirQualityForecastResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetAirQualityDailyForecasts(expected, response.getValue());
    }

    static void validateGetAirQualityHourlyForecasts(AirQualityResult expected, AirQualityResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getNextLink(), actual.getNextLink());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetAirQualityHourlyForecastsWithResponse(AirQualityResult expected,
        Response<AirQualityResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetAirQualityHourlyForecasts(expected, response.getValue());
    }

    static void validateGetDailyHistoricalRecords(DailyHistoricalRecordsResult expected,
        DailyHistoricalRecordsResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getNextLink(), actual.getNextLink());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetDailyHistoricalRecordsWithResponse(DailyHistoricalRecordsResult expected,
        Response<DailyHistoricalRecordsResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetDailyHistoricalRecords(expected, response.getValue());
    }

    static void validateGetDailyHistoricalActuals(DailyHistoricalActualsResult expected,
        DailyHistoricalActualsResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getNextLink(), actual.getNextLink());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetDailyHistoricalActualsWithResponse(DailyHistoricalActualsResult expected,
        Response<DailyHistoricalActualsResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetDailyHistoricalActuals(expected, response.getValue());
    }

    static void validateGetDailyHistoricalNormalsResult(DailyHistoricalNormalsResult expected,
        DailyHistoricalNormalsResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getNextLink(), actual.getNextLink());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetDailyHistoricalNormalsResultWithResponse(DailyHistoricalNormalsResult expected,
        Response<DailyHistoricalNormalsResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetDailyHistoricalNormalsResult(expected, response.getValue());
    }
}
